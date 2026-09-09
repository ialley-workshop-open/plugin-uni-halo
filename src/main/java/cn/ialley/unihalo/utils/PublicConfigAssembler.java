package cn.ialley.unihalo.utils;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.ialley.unihalo.scheme.GeneralConfig;
import cn.ialley.unihalo.utils.MaintenanceResolver;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * 公开配置输出合成器（getConfigs 出口，只读）。
 *
 * <p>把「设置剩余组（ConfigMap）+ 通用配置单例 {@link GeneralConfig}」合成为小程序端
 * 依赖的旧 shape：</p>
 * <ul>
 *   <li>authorConfig / pageConfig / basicConfig（内容部分）/ imagesConfig / loveConfig
 *       五个顶层键由 GeneralConfig 重建（不再依赖 ConfigMap 旧组）；</li>
 *   <li>剔除已下线/敏感字段：basicConfig.tokenConfig（个人令牌）、appConfig.startConfig
 *       （启动页已下线）、auditConfig.auditModeData（死字段）；</li>
 *   <li>maintenance（additive 顶层键，2026-09-04 新增）：按 GeneralConfig.spec.maintenance
 *       时间窗口与当前时刻计算状态，仅 scheduled/active 时输出；</li>
 *   <li>其余设置组（captchaConfig / pluginConfig / linkConfig …）原样透传。</li>
 * </ul>
 *
 * @author 小莫唐尼
 */
public class PublicConfigAssembler {

    /**
     * 插件 Spring 上下文未注册 Jackson 3 ObjectMapper bean，故内部自行创建
     * （与 EmailService / GeneralConfigServiceImpl 同套路）。
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 合成公开配置输出（维护状态按当前时刻判定）。
     *
     * @param settings 当前插件设置（ReactiveSettingFetcher.getSettingValues 结果，可为空）
     * @param config   通用配置单例（可能为 null 或 spec 为空，此时内容组不输出）
     */
    public ObjectNode assemble(Map<String, JsonNode> settings, GeneralConfig config) {
        return assemble(settings, config, Instant.now());
    }

    /**
     * 合成公开配置输出（维护状态判定时钟可注入，便于测试覆盖
     * scheduled/active/到点自动结束等分支）。
     *
     * @param settings 当前插件设置（可为空）
     * @param config   通用配置单例（可能为 null 或 spec 为空）
     * @param now      维护状态判定的当前时刻（见 {@link MaintenanceResolver}）
     */
    public ObjectNode assemble(Map<String, JsonNode> settings, GeneralConfig config,
            Instant now) {
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        if (settings != null) {
            normalize(settings).forEach((group, node) -> {
                if (node == null || node.isNull()) {
                    return;
                }
                if (isContentGroup(group)) {
                    // 内容组由 GeneralConfig 重建，跳过原样透传
                    return;
                }
                if ("appConfig".equals(group)) {
                    root.set(group, withoutKeys(node, "startConfig"));
                } else if ("auditConfig".equals(group)) {
                    root.set(group, withoutKeys(node, "auditModeData"));
                } else {
                    root.set(group, node);
                }
            });
        }

        JsonNode spec = toSpecTree(config);
        if (spec == null || !spec.isObject()) {
            return root;
        }
        JsonNode profile = spec.get("profile");
        if (profile != null && profile.isObject()) {
            ObjectNode authorConfig = JsonNodeFactory.instance.objectNode();
            pick(profile, authorConfig, "blogger", "social");
            if (authorConfig.size() > 0) {
                root.set("authorConfig", authorConfig);
            }
            ObjectNode basicConfig = JsonNodeFactory.instance.objectNode();
            pick(profile, basicConfig, "copyrightConfig", "showAboutSystem", "disclaimers",
                    "postDetailConfig");
            if (basicConfig.size() > 0) {
                root.set("basicConfig", basicConfig);
            }
            JsonNode appInfo = profile.get("appInfo");
            if (appInfo != null && !appInfo.isNull()) {
                // 应用信息（名称/图标）→ 旧 appConfig.appInfo 形态（覆盖历史遗留设置值，
                // 如旧 ConfigMap 中 baseConfig/appConfig 组残留）
                root.set("appConfig",
                        JsonNodeFactory.instance.objectNode().set("appInfo", appInfo));
            }
        }
        JsonNode pages = spec.get("pages");
        if (pages != null && pages.isObject()) {
            ObjectNode pageConfig = JsonNodeFactory.instance.objectNode();
            pick(pages, pageConfig, "homeConfig", "galleryConfig", "aboutConfig",
                    "categoryConfig", "momentConfig");
            if (pageConfig.size() > 0) {
                root.set("pageConfig", pageConfig);
            }
        }
        JsonNode assets = spec.get("assets");
        if (assets != null && !assets.isNull()) {
            root.set("imagesConfig", assets);
        }
        // 站点级展示偏好默认（L0，additive 顶层键）：客户端 layout.home/cardType/
        // isAvatarRadius 的站点默认来源（客户端可本地覆盖）
        JsonNode preferences = spec.get("preferences");
        if (preferences != null && !preferences.isNull()) {
            root.set("preferences", preferences);
        }
        // 恋爱模块（2026-09-03 迁入）：spec.love → 旧顶层 loveConfig shape
        // （loveEnabled/pageImages/模块开关，客户端无感）。2026-09-08 起模块开关
        // 脱敏输出：仅 enabled + passwordEnabled（是否已设置密码，客户端据此决定
        // 是否弹密码验证），密码相关字段一律不下发小程序端。
        JsonNode love = spec.get("love");
        if (love != null && love.isObject()) {
            root.set("loveConfig", sanitizeLove(love));
        }
        // 维护模式（additive 顶层键，2026-09-04 新增）：状态由时间窗口按 now 计算，
        // 仅 scheduled/active 输出；enabled=false 或已到点自动结束 → 键缺失（客户端视为未维护）
        JsonNode maintenance = spec.get("maintenance");
        if (maintenance != null && maintenance.isObject()) {
            MaintenanceResolver.MaintenanceStatus status = MaintenanceResolver.resolve(
                    jsonBool(maintenance.get("enabled")),
                    jsonText(maintenance.get("startTime")),
                    jsonText(maintenance.get("endTime")), now);
            if (status != MaintenanceResolver.MaintenanceStatus.NONE) {
                ObjectNode maintenanceOut = JsonNodeFactory.instance.objectNode();
                maintenanceOut.put("status", status.name().toLowerCase());
                pick(maintenance, maintenanceOut, "title", "notice", "description",
                        "startTime", "endTime");
                root.set("maintenance", maintenanceOut);
            }
        }
        // 链接配置（2026-09-08 起去映射 + 拆分子结构）：spec.linkInfo 直接下发到
        // pluginConfig.linkInfo，结构 = {miniInfo, siteInfo, authorInfo}：
        // - miniInfo（小程序信息）供小程序端「申请信息」弹窗（uh-links-mini-info）读取；
        // - siteInfo（站点信息）字段对齐 Halo 官方 plugin-links 友链提交 API
        //   （link-applications 请求体：displayName/url/logo/description/email/backlink/feedUrls）；
        // - authorInfo（作者信息）小程序端作者区展示（authorName/avatar/website）。
        // linkInfo 全部留空时不输出（app 端展示「暂未配置」占位）。
        // 旧 linksSubmitPlugin（blogName/blogLogo/blogUrl/blogDesc 等键）映射已移除，不再下发。
        JsonNode linkInfo = spec.get("linkInfo");
        if (linkInfo != null && linkInfo.isObject() && hasNonBlankText(linkInfo)) {
            JsonNode pluginConfigNode = root.get("pluginConfig");
            ObjectNode pluginConfig = pluginConfigNode != null && pluginConfigNode.isObject()
                    ? (ObjectNode) pluginConfigNode.deepCopy()
                    : JsonNodeFactory.instance.objectNode();
            pluginConfig.set("linkInfo", linkInfo);
            root.set("pluginConfig", pluginConfig);
        }
        return root;
    }

    /**
     * 把方案 B 三域组与新「基本配置」组展开回「旧模块顶层键」形态（新旧结构二选一，
     * 不会同时存在）：featureConfig → linkConfig；safetyConfig →
     * auditConfig / captchaConfig；integrationConfig → pluginConfig；
     * baseConfig.appInfo → 归并至 appConfig.appInfo（保持客户端旧 shape）。
     * 旧结构原样保留。恋爱组（loveConfig）已随 2026-09-03 迁移由 spec.love 重建输出，
     * 不再从 featureConfig.loveConfig 展开透传（旧 ConfigMap 残留键在此被丢弃）。
     */
    private static Map<String, JsonNode> normalize(Map<String, JsonNode> settings) {
        Map<String, JsonNode> out = new LinkedHashMap<>(settings);
        expand(out, "featureConfig", "linkConfig");
        expand(out, "safetyConfig", "auditConfig", "captchaConfig");
        expand(out, "integrationConfig", "pluginConfig");
        // 基本配置（应用信息：名称/图标）→ 输出为旧 appConfig.appInfo 形态
        JsonNode base = out.remove("baseConfig");
        if (base != null && base.isObject()) {
            JsonNode appInfo = base.get("appInfo");
            if (appInfo != null && !appInfo.isNull()) {
                JsonNode existing = out.get("appConfig");
                if (existing == null || !existing.isObject()) {
                    out.put("appConfig",
                            JsonNodeFactory.instance.objectNode().set("appInfo", appInfo));
                } else {
                    ObjectNode merged = (ObjectNode) existing.deepCopy();
                    merged.set("appInfo", appInfo);
                    out.put("appConfig", merged);
                }
            }
        }
        return out;
    }

    private static void expand(Map<String, JsonNode> out, String domain, String... modules) {
        JsonNode domainNode = out.remove(domain);
        if (domainNode == null || !domainNode.isObject()) {
            return;
        }
        for (String module : modules) {
            JsonNode value = domainNode.get(module);
            if (value != null && !value.isNull()) {
                out.put(module, value);
            }
        }
    }

    private JsonNode toSpecTree(GeneralConfig config) {
        if (config == null || config.getSpec() == null) {
            return null;
        }
        return objectMapper.valueToTree(config.getSpec());
    }

    /**
     * 恋爱配置公开输出脱敏（getConfigs loveConfig 组）：
     * loveEnabled / pageImages.bgImageUrl 原样保留；模块开关仅输出
     * enabled + passwordEnabled（按 passwordHash 非空派生），
     * password / passwordHash / passwordRemoved 等密码字段一律不下发小程序端。
     */
    private static JsonNode sanitizeLove(JsonNode love) {
        ObjectNode out = JsonNodeFactory.instance.objectNode();
        pick(love, out, "loveEnabled");
        JsonNode pageImages = love.get("pageImages");
        if (pageImages != null && pageImages.isObject()) {
            ObjectNode pageImagesOut = JsonNodeFactory.instance.objectNode();
            pick(pageImages, pageImagesOut, "bgImageUrl");
            if (pageImagesOut.size() > 0) {
                out.set("pageImages", pageImagesOut);
            }
        }
        for (String moduleKey : new String[]{"ourStory", "lovePhoto", "loveDaily"}) {
            JsonNode module = love.get(moduleKey);
            if (module != null && module.isObject()) {
                ObjectNode moduleOut = JsonNodeFactory.instance.objectNode();
                pick(module, moduleOut, "enabled");
                JsonNode hash = module.get("passwordHash");
                moduleOut.put("passwordEnabled",
                        hash != null && !hash.isNull() && !hash.asText().isBlank());
                if (moduleOut.size() > 0) {
                    out.set(moduleKey, moduleOut);
                }
            }
        }
        return out;
    }

    private static boolean isContentGroup(String group) {
        return "authorConfig".equals(group) || "pageConfig".equals(group)
                || "basicConfig".equals(group) || "imagesConfig".equals(group)
                || "loveConfig".equals(group);
    }

    /** 对象中是否存在至少一个非空文本字段（用于判断链接配置是否值得覆盖输出） */
    private static boolean hasNonBlankText(JsonNode node) {
        if (node == null || !node.isObject()) {
            return false;
        }
        var values = node.properties();
        return values.stream().anyMatch(entry -> {
            JsonNode value = entry.getValue();
            return value != null && !value.isNull() && !value.asText().isBlank();
        });
    }

    private static ObjectNode withoutKeys(JsonNode node, String... keys) {
        ObjectNode copy = node.isObject()
                ? (ObjectNode) node.deepCopy()
                : JsonNodeFactory.instance.objectNode();
        for (String key : keys) {
            copy.remove(key);
        }
        return copy;
    }

    private static void pick(JsonNode source, ObjectNode target, String... keys) {
        if (source == null || !source.isObject()) {
            return;
        }
        for (String key : keys) {
            JsonNode value = source.get(key);
            if (value != null && !value.isNull()) {
                target.set(key, value);
            }
        }
    }

    private static String jsonText(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }

    private static Boolean jsonBool(JsonNode node) {
        return node == null || node.isNull() || !node.isBoolean() ? null : node.asBoolean();
    }
}
