package cn.ialley.unihalo.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import cn.ialley.unihalo.scheme.GeneralConfig;
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
 *   <li>authorConfig / pageConfig / basicConfig（内容部分）/ imagesConfig 四个顶层键由
 *       GeneralConfig 重建（不再依赖 ConfigMap 旧组）；</li>
 *   <li>剔除已下线/敏感字段：basicConfig.tokenConfig（个人令牌）、appConfig.startConfig
 *       （启动页已下线）、auditConfig.auditModeData（死字段）；</li>
 *   <li>其余设置组（loveConfig / captchaConfig / pluginConfig / linkConfig …）原样透传。</li>
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
     * 合成公开配置输出。
     *
     * @param settings 当前插件设置（ReactiveSettingFetcher.getSettingValues 结果，可为空）
     * @param config   通用配置单例（可能为 null 或 spec 为空，此时内容组不输出）
     */
    public ObjectNode assemble(Map<String, JsonNode> settings, GeneralConfig config) {
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
        }
        JsonNode pages = spec.get("pages");
        if (pages != null && pages.isObject()) {
            ObjectNode pageConfig = JsonNodeFactory.instance.objectNode();
            pick(pages, pageConfig, "homeConfig", "galleryConfig", "aboutConfig");
            if (pageConfig.size() > 0) {
                root.set("pageConfig", pageConfig);
            }
        }
        JsonNode assets = spec.get("assets");
        if (assets != null && !assets.isNull()) {
            root.set("imagesConfig", assets);
        }
        return root;
    }

    /**
     * 把方案 B 三域组与新「基本配置」组展开回「旧模块顶层键」形态（新旧结构二选一，
     * 不会同时存在）：featureConfig → loveConfig / linkConfig；safetyConfig →
     * auditConfig / captchaConfig；integrationConfig → pluginConfig；
     * baseConfig.appInfo → 归并至 appConfig.appInfo（保持客户端旧 shape）。
     * 旧结构原样保留。
     */
    private static Map<String, JsonNode> normalize(Map<String, JsonNode> settings) {
        Map<String, JsonNode> out = new LinkedHashMap<>(settings);
        expand(out, "featureConfig", "loveConfig", "linkConfig");
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

    private static boolean isContentGroup(String group) {
        return "authorConfig".equals(group) || "pageConfig".equals(group)
                || "basicConfig".equals(group) || "imagesConfig".equals(group);
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
}
