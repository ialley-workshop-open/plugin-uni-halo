package cn.ialley.unihalo.services.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.GeneralConfig;
import cn.ialley.unihalo.scheme.GeneralConfig.About;
import cn.ialley.unihalo.scheme.GeneralConfig.Assets;
import cn.ialley.unihalo.scheme.GeneralConfig.Blogger;
import cn.ialley.unihalo.scheme.GeneralConfig.CategoryPage;
import cn.ialley.unihalo.scheme.GeneralConfig.Copyright;
import cn.ialley.unihalo.scheme.GeneralConfig.Disclaimer;
import cn.ialley.unihalo.scheme.GeneralConfig.Gallery;
import cn.ialley.unihalo.scheme.GeneralConfig.Home;
import cn.ialley.unihalo.scheme.GeneralConfig.LinkInfo;
import cn.ialley.unihalo.scheme.GeneralConfig.Love;
import cn.ialley.unihalo.scheme.GeneralConfig.Maintenance;
import cn.ialley.unihalo.scheme.GeneralConfig.MomentPage;
import cn.ialley.unihalo.scheme.GeneralConfig.ModuleSwitch;
import cn.ialley.unihalo.scheme.GeneralConfig.PageImages;
import cn.ialley.unihalo.scheme.GeneralConfig.Pages;
import cn.ialley.unihalo.scheme.GeneralConfig.PostDetail;
import cn.ialley.unihalo.scheme.GeneralConfig.Profile;
import cn.ialley.unihalo.scheme.GeneralConfig.QuickNavigationItem;
import cn.ialley.unihalo.scheme.GeneralConfig.Social;
import cn.ialley.unihalo.scheme.GeneralConfig.Spec;
import cn.ialley.unihalo.services.GeneralConfigService;
import cn.ialley.unihalo.utils.MaintenanceResolver;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.ReactiveSettingFetcher;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * 通用配置服务实现（单例）。
 *
 * <p>默认值对齐旧 setting.yaml 的 value 缺省；历史 ConfigMap 旧组
 * （basicConfig/pageConfig/authorConfig/imagesConfig）在单例尚未创建时合并进
 * 默认结构，作为升级期的一次性导入兜底。</p>
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class GeneralConfigServiceImpl implements GeneralConfigService {

    /**
     * 参与历史导入的旧设置组（🟦 迁出组）
     */
    private static final String[] LEGACY_GROUPS =
        {"basicConfig", "pageConfig", "authorConfig", "imagesConfig"};

    /**
     * 插件 Spring 上下文未注册 Jackson 3 ObjectMapper bean，故内部自行创建
     * （与 EmailService 同套路）。
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 恋爱模块入口密码编码器（与恋爱相册 {@code LoveAlbumServiceImpl} 同套路；
     * 声明处初始化，不进 Lombok 构造器）。
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final ReactiveExtensionClient client;
    private final ReactiveSettingFetcher settingFetcher;

    @Override
    public Mono<GeneralConfig> get() {
        return client.fetch(GeneralConfig.class, Constants.GENERAL_CONFIG_SINGLETON_NAME)
                .switchIfEmpty(Mono.defer(this::defaultWithLegacy))
                // 恋爱模块入口密码一律脱敏（哈希不回显；passwordEnabled 由哈希派生）
                .map(this::maskLovePasswords);
    }

    @Override
    public Mono<GeneralConfig> save(GeneralConfig config) {
        GeneralConfig body = config == null ? new GeneralConfig() : config;
        // 写入前合并：默认值 → 历史 ConfigMap 旧值 → 请求体非空字段，防丢字段/防空写。
        // 先取现有单例（可能不存在）以保留恋爱模块入口密码哈希。
        return client.fetch(GeneralConfig.class, Constants.GENERAL_CONFIG_SINGLETON_NAME)
                .defaultIfEmpty(new GeneralConfig())
                .flatMap(existing -> legacyOverlay().map(overlay -> {
                    ObjectNode merged = merge(defaultSpecTree(), overlay);
                    if (body.getSpec() != null) {
                        merged = merge(merged,
                                (ObjectNode) objectMapper.valueToTree(body.getSpec()));
                    }
                    GeneralConfig target = new GeneralConfig();
                    target.setSpec(treeToValue(merged));
                    // 恋爱模块入口密码：新密码=重设并启用；passwordRemoved=清除；否则保持原哈希
                    applyLovePasswords(target.getSpec().getLove(),
                            existing.getSpec() != null ? existing.getSpec().getLove() : null);
                    validateMaintenance(target.getSpec().getMaintenance());
                    return target;
                }).flatMap(target -> {
                    if (existing.getMetadata() == null) {
                        Metadata metadata = new Metadata();
                        metadata.setName(Constants.GENERAL_CONFIG_SINGLETON_NAME);
                        metadata.setCreationTimestamp(Instant.now());
                        target.setMetadata(metadata);
                        return client.create(target);
                    }
                    existing.setSpec(target.getSpec());
                    return client.update(existing);
                }))
                // 响应同样脱敏（哈希/写请求字段不回显）
                .map(this::maskLovePasswords);
    }

    @Override
    public Mono<Boolean> verifyLoveModulePassword(String module, String password) {
        return fetchRaw().map(config -> {
            ModuleSwitch moduleSwitch = findLoveModule(config, module);
            if (moduleSwitch == null || isBlank(moduleSwitch.getPasswordHash())) {
                return false;
            }
            return passwordEncoder.matches(
                    password == null ? "" : password, moduleSwitch.getPasswordHash());
        }).defaultIfEmpty(false);
    }

    @Override
    public Mono<Boolean> isLoveModuleLocked(String module) {
        return fetchRaw().map(config -> {
            ModuleSwitch moduleSwitch = findLoveModule(config, module);
            return moduleSwitch != null && !isBlank(moduleSwitch.getPasswordHash());
        }).defaultIfEmpty(false);
    }

    /**
     * 读取原始单例（未经脱敏，含恋爱模块密码哈希），供解锁/锁定判断使用。
     */
    private Mono<GeneralConfig> fetchRaw() {
        return client.fetch(GeneralConfig.class, Constants.GENERAL_CONFIG_SINGLETON_NAME)
                .switchIfEmpty(Mono.defer(this::defaultWithLegacy));
    }

    private static ModuleSwitch findLoveModule(GeneralConfig config, String module) {
        if (config == null || config.getSpec() == null || config.getSpec().getLove() == null) {
            return null;
        }
        Love love = config.getSpec().getLove();
        return switch (module) {
            case "ourStory" -> love.getOurStory();
            case "lovePhoto" -> love.getLovePhoto();
            case "loveDaily" -> love.getLoveDaily();
            default -> null;
        };
    }

    // ---------- 默认结构与历史导入 ----------

    /**
     * 默认结构（不落库）：默认值 + 历史 ConfigMap 旧值合并。
     */
    private Mono<GeneralConfig> defaultWithLegacy() {
        return legacyOverlay().map(overlay -> {
            GeneralConfig config = new GeneralConfig();
            Metadata metadata = new Metadata();
            metadata.setName(Constants.GENERAL_CONFIG_SINGLETON_NAME);
            config.setMetadata(metadata);
            config.setSpec(treeToValue(merge(defaultSpecTree(), overlay)));
            return config;
        });
    }

    /**
     * 读取历史旧设置组并整理成 spec 形态的覆盖树
     * （{@code profile: {...}} / {@code pages: {...}} / {@code assets: {...}}）。
     * 旧组不存在时返回空对象。
     */
    private Mono<ObjectNode> legacyOverlay() {
        return settingFetcher.getSettingValues()
                .defaultIfEmpty(Map.of())
                .map(values -> {
                    ObjectNode overlay = JsonNodeFactory.instance.objectNode();
                    JsonNode author = values.get("authorConfig");
                    JsonNode basic = values.get("basicConfig");
                    ObjectNode profile = JsonNodeFactory.instance.objectNode();
                    pick(author, profile, "blogger", "social");
                    pick(basic, profile, "copyrightConfig", "showAboutSystem", "disclaimers",
                            "postDetailConfig");
                    // 应用信息（名称/图标）：优先取「基本配置」baseConfig.appInfo，
                    // 回退旧 appConfig.appInfo（历史组已从 setting.yaml 移除）
                    JsonNode appInfo = null;
                    JsonNode baseCfg = values.get("baseConfig");
                    if (baseCfg != null && baseCfg.isObject() && baseCfg.has("appInfo")) {
                        appInfo = baseCfg.get("appInfo");
                    } else {
                        JsonNode appCfg = values.get("appConfig");
                        if (appCfg != null && appCfg.isObject() && appCfg.has("appInfo")) {
                            appInfo = appCfg.get("appInfo");
                        }
                    }
                    if (appInfo != null && !appInfo.isNull()) {
                        profile.set("appInfo", appInfo);
                    }
                    if (profile.size() > 0) {
                        overlay.set("profile", profile);
                    }
                    JsonNode page = values.get("pageConfig");
                    ObjectNode pages = JsonNodeFactory.instance.objectNode();
                    pick(page, pages, "homeConfig", "galleryConfig", "aboutConfig");
                    if (pages.size() > 0) {
                        overlay.set("pages", pages);
                    }
                    JsonNode images = values.get("imagesConfig");
                    if (images != null && images.isObject() && images.size() > 0) {
                        overlay.set("assets", images);
                    }
                    // 恋爱模块（2026-09-03 迁入）：旧 loveConfig 剩余字段
                    // （loveEnabled/pageImages/模块开关）从 ConfigMap 导入 spec.love。
                    // 兼容两种旧结构：方案 B 域组 featureConfig.loveConfig 与旧顶层键 loveConfig；
                    // 已模型化内容字段（loveDate/loveInfo 等）随本次迁移不再输出（详见
                    // .docs/config-system-v2-redesign.md 实施记录）。
                    // 2026-09-08 起仅显式挑选仍有效的字段（loveEnabled/pageImages.bgImageUrl/
                    // 模块 enabled），旧 iconUrl/waveImageUrl/heartImageUrl 等已下线字段不再导入。
                    JsonNode love = values.get("loveConfig");
                    if (love == null || !love.isObject()) {
                        JsonNode feature = values.get("featureConfig");
                        if (feature != null && feature.isObject()) {
                            love = feature.get("loveConfig");
                        }
                    }
                    if (love != null && love.isObject() && love.size() > 0) {
                        ObjectNode loveOut = JsonNodeFactory.instance.objectNode();
                        pick(love, loveOut, "loveEnabled");
                        JsonNode pageImages = love.get("pageImages");
                        if (pageImages != null && pageImages.isObject()) {
                            ObjectNode pageImagesOut = JsonNodeFactory.instance.objectNode();
                            pick(pageImages, pageImagesOut, "bgImageUrl");
                            if (pageImagesOut.size() > 0) {
                                loveOut.set("pageImages", pageImagesOut);
                            }
                        }
                        for (String moduleKey : new String[]{"ourStory", "lovePhoto", "loveDaily"}) {
                            JsonNode module = love.get(moduleKey);
                            if (module != null && module.isObject()) {
                                ObjectNode moduleOut = JsonNodeFactory.instance.objectNode();
                                pick(module, moduleOut, "enabled");
                                if (moduleOut.size() > 0) {
                                    loveOut.set(moduleKey, moduleOut);
                                }
                            }
                        }
                        if (loveOut.size() > 0) {
                            overlay.set("love", loveOut);
                        }
                    }
                    return overlay;
                });
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

    /**
     * 递归合并：overlay 中非空字段覆盖 base；对象类型递归；null 视为未提供。
     */
    private static ObjectNode merge(JsonNode base, JsonNode overlay) {
        if (!base.isObject() || !overlay.isObject()) {
            return (ObjectNode) base.deepCopy();
        }
        ObjectNode merged = (ObjectNode) base.deepCopy();
        overlay.properties().forEach(entry -> {
            JsonNode value = entry.getValue();
            if (value == null || value.isNull()) {
                return;
            }
            JsonNode child = merged.get(entry.getKey());
            if (child != null && child.isObject() && value.isObject()) {
                merged.set(entry.getKey(), merge(child, value));
            } else {
                merged.set(entry.getKey(), value);
            }
        });
        return merged;
    }

    private ObjectNode defaultSpecTree() {
        return (ObjectNode) objectMapper.valueToTree(buildDefaultSpec());
    }

    private Spec treeToValue(ObjectNode tree) {
        return objectMapper.treeToValue(tree, Spec.class);
    }

    /**
     * 默认 spec（对齐旧 setting.yaml 的 value 缺省）。
     */
    private Spec buildDefaultSpec() {
        Spec spec = new Spec();
        spec.setProfile(buildDefaultProfile());
        spec.setPages(buildDefaultPages());
        spec.setAssets(buildDefaultAssets());
        spec.setPreferences(buildDefaultPreferences());
        spec.setLove(buildDefaultLove());
        spec.setLinkInfo(buildDefaultLinkInfo());
        spec.setMaintenance(buildDefaultMaintenance());
        return spec;
    }

    /**
     * 默认链接配置：全部字段留空（2026-09-08 新增，站长配置后经 getConfigs 覆盖
     * pluginConfig.linksSubmitPlugin 对应键；留空不覆盖 setting 透传的旧值）。
     */
    private static LinkInfo buildDefaultLinkInfo() {
        LinkInfo linkInfo = new LinkInfo();
        linkInfo.setDisplayName("");
        linkInfo.setMiniProgramCode("");
        linkInfo.setLink("");
        linkInfo.setAuthorName("");
        linkInfo.setAvatar("");
        linkInfo.setWebsite("");
        linkInfo.setDescription("");
        linkInfo.setApplyRemark("");
        return linkInfo;
    }

    private static GeneralConfig.Preferences buildDefaultPreferences() {
        // 2026-09-08 起与客户端内置默认（hermes/preferences.md §3.3）对齐：
        // 首页/归档 single + image_bottom，文章列表 double + image_bottom；
        // 卡片样式统一组件 layout 值（image_*），旧 lr_*/tb_* 值体系废弃
        GeneralConfig.Preferences preferences = new GeneralConfig.Preferences();
        preferences.setHomeListLayout("single");
        preferences.setHomeCardType("image_bottom");
        preferences.setArticlesListLayout("double");
        preferences.setArticleCardType("image_bottom");
        preferences.setArchivesListLayout("single");
        preferences.setArchivesCardType("image_bottom");
        preferences.setAvatarRadius(true);
        return preferences;
    }

    private static Profile buildDefaultProfile() {
        Profile profile = new Profile();

        GeneralConfig.AppInfo appInfo = new GeneralConfig.AppInfo();
        appInfo.setName("uni-halo");
        // 应用图标默认引用插件内置静态资源（ReverseProxy：/plugins/plugin-uni-halo/assets/**）
        appInfo.setLogo("/plugins/plugin-uni-halo/assets/logo.png");
        profile.setAppInfo(appInfo);

        Blogger blogger = new Blogger();
        blogger.setNickname("uni-halo");
        blogger.setAvatar("");
        blogger.setEmail("");
        blogger.setDescription("");
        profile.setBlogger(blogger);

        Social social = new Social();
        social.setEnabled(true);
        profile.setSocial(social);

        Copyright copyright = new Copyright();
        copyright.setEnabled(true);
        copyright.setContent("「 2022 uni-halo 丨 开源项目@小莫唐尼 」");
        profile.setCopyrightConfig(copyright);

        Disclaimer disclaimer = new Disclaimer();
        disclaimer.setEnabled(true);
        disclaimer.setContent("");
        profile.setDisclaimers(disclaimer);

        profile.setShowAboutSystem(true);

        PostDetail postDetail = new PostDetail();
        postDetail.setShowComment(true);
        postDetail.setCopyrightEnabled(true);
        postDetail.setCopyrightAuthor("uni-halo");
        postDetail.setCopyrightDesc("使用《非商业性使用-相同方式共享 4.0 国际 (CC BY-NC-SA 4.0)》"
                + "协议授权，文章来源于网上收集或者原创，若未在文章内说明的均为原创文章");
        postDetail.setCopyrightViolation("若侵害到您的权利，请您及时联系我，在收到通知后第一时间处理，"
                + "邮箱：xxxx@xx.com");
        profile.setPostDetailConfig(postDetail);
        return profile;
    }

    private static Pages buildDefaultPages() {
        Pages pages = new Pages();

        Home home = new Home();
        home.setPageTitle("首页");
        home.setUseQuickNavigation(true);
        // 快捷导航默认 5 项（对齐客户端 uh-home-quick-nav 默认 navList，2026-09-08 起
        // 迁入控制台逐项可配置；bgColor 原 bgGlass、visible 原 show）
        home.setQuickNavigation(defaultQuickNavigation());
        home.setUseCategory(true);
        // 首页分类栏选中引用：默认空（未选择时客户端回退内置行为），由站长挑选（固定 3 个）
        home.setCategories(List.of());
        pages.setHomeConfig(home);

        Gallery gallery = new Gallery();
        gallery.setPageTitle("图库");
        pages.setGalleryConfig(gallery);

        // 分类页/瞬间页标题（2026-09-08 新增：默认留空，客户端回退内置标题）
        CategoryPage categoryPage = new CategoryPage();
        categoryPage.setPageTitle("");
        pages.setCategoryConfig(categoryPage);

        MomentPage momentPage = new MomentPage();
        momentPage.setPageTitle("");
        pages.setMomentConfig(momentPage);

        About about = new About();
        about.setPageTitle("关于博主");
        about.setBgImageUrl("/plugins/plugin-uni-halo/assets/uni_halo_profile_bg.jpg");
        about.setWaveImageUrl("/plugins/plugin-uni-halo/assets/uni_halo_about_wave.gif");
        pages.setAboutConfig(about);
        return pages;
    }

    /**
     * 默认快捷导航 5 项（对齐客户端 uh-home-quick-nav 默认 navList；2026-09-08 起
     * 迁入控制台可配置，visible 默认全部显示，站长可逐项隐藏）。
     */
    private static List<QuickNavigationItem> defaultQuickNavigation() {
        List<QuickNavigationItem> items = new ArrayList<>();
        items.add(navItem("archives", "文章归档", "#03A9F4", "rgba(3, 169, 244, 0.14)",
                "uhemoji2-icon", "-mask", "/pages-blog/archives/archives"));
        items.add(navItem("vote", "投票中心", "#00BCD4", "rgba(0, 188, 212, 0.14)",
                "uhemoji2-icon", "-confused", "/pages-blog/votes/votes"));
        items.add(navItem("disclaimers", "友情链接", "#009688", "rgba(0, 150, 136, 0.14)",
                "uhemoji2-icon", "-wink", "/pages-blog/friend-links/friend-links"));
        items.add(navItem("love", "恋爱日记", "#FF4C67", "rgba(255, 76, 103, 0.14)",
                "uhemoji2-icon", "-in-love", "/pages-blog/love/love"));
        items.add(navItem("contact-blogger", "联系博主", "#FF9800", "rgba(255, 152, 0, 0.14)",
                "uhemoji2-icon", "-cool", "/pages-blog/contact/contact"));
        return items;
    }

    private static QuickNavigationItem navItem(String key, String title, String color,
            String bgColor, String iconPrefix, String icon, String path) {
        QuickNavigationItem item = new QuickNavigationItem();
        item.setKey(key);
        item.setTitle(title);
        item.setColor(color);
        item.setBgColor(bgColor);
        item.setIconPrefix(iconPrefix);
        item.setIcon(icon);
        item.setPath(path);
        item.setVisible(true);
        return item;
    }

    /**
     * 默认 assets：加载占位图（2026-09-08 起默认图片/空图片配置已下线，客户端内置
     * 回退兜底）；唯一内置默认 = 加载动图（插件静态资源
     * /plugins/plugin-uni-halo/assets/…），error 图留空走客户端回退。
     */
    private static Assets buildDefaultAssets() {
        Assets assets = new Assets();
        assets.setLoadingGifUrl("/plugins/plugin-uni-halo/assets/uni_halo_img_lazyload.gif");
        assets.setLoadingErrUrl("");
        return assets;
    }

    /**
     * 默认 love（对齐旧 setting.yaml 的 value 缺省；2026-09-03 由
     * featureConfig.loveConfig 迁入）：总开关默认关闭；恋爱页背景图默认留空
     * （原 925i.cn 外链默认图依赖已清除，由站长配置或客户端内置回退）；
     * 2026-09-08 起 pageImages 仅保留背景图、模块入口仅开关+密码（均默认未设置）。
     */
    private static Love buildDefaultLove() {
        Love love = new Love();
        love.setLoveEnabled(false);

        PageImages pageImages = new PageImages();
        pageImages.setBgImageUrl("");
        love.setPageImages(pageImages);

        // 恋爱故事模块默认开启（原 loveConfig.ourStory value：enabled=true）
        ModuleSwitch ourStory = new ModuleSwitch();
        ourStory.setEnabled(true);
        ourStory.setPasswordEnabled(false);
        love.setOurStory(ourStory);

        ModuleSwitch lovePhoto = new ModuleSwitch();
        lovePhoto.setEnabled(false);
        lovePhoto.setPasswordEnabled(false);
        love.setLovePhoto(lovePhoto);

        ModuleSwitch loveDaily = new ModuleSwitch();
        loveDaily.setEnabled(false);
        loveDaily.setPasswordEnabled(false);
        love.setLoveDaily(loveDaily);
        return love;
    }

    /**
     * 维护模式默认：关闭；标题默认「站点维护中」；说明与排期窗口留空
     * （对齐 .docs/maintenance-config-design.md §5；getConfigs 在 enabled=false
     * 时不输出 maintenance 键，客户端视为未维护）。
     */
    private static Maintenance buildDefaultMaintenance() {
        Maintenance maintenance = new Maintenance();
        maintenance.setEnabled(false);
        maintenance.setTitle("站点维护中");
        maintenance.setNotice("");
        maintenance.setDescription("");
        return maintenance;
    }

    /**
     * 恋爱模块入口密码写入语义（与恋爱相册一致）：
     * 新密码非空 = 重设 BCrypt 哈希并启用；passwordRemoved = 清除密码；
     * 均未提供 = 保持现有哈希不变。写请求字段（password/passwordRemoved）
     * 消费后置空，避免落入存储。
     */
    private void applyLovePasswords(Love love, Love existing) {
        if (love == null) {
            return;
        }
        applyModulePassword(love.getOurStory(), existing != null ? existing.getOurStory() : null);
        applyModulePassword(love.getLovePhoto(),
                existing != null ? existing.getLovePhoto() : null);
        applyModulePassword(love.getLoveDaily(), existing != null ? existing.getLoveDaily() : null);
    }

    private void applyModulePassword(ModuleSwitch module, ModuleSwitch existing) {
        if (module == null) {
            return;
        }
        String oldHash = existing != null ? existing.getPasswordHash() : null;
        if (!isBlank(module.getPassword())) {
            module.setPasswordHash(passwordEncoder.encode(module.getPassword()));
            module.setPasswordEnabled(true);
        } else if (Boolean.TRUE.equals(module.getPasswordRemoved())) {
            module.setPasswordHash(null);
            module.setPasswordEnabled(false);
        } else {
            module.setPasswordHash(oldHash);
            module.setPasswordEnabled(oldHash != null);
        }
        module.setPassword(null);
        module.setPasswordRemoved(null);
    }

    /**
     * 控制台读写响应脱敏：passwordHash/password/passwordRemoved 一律置空不回显，
     * passwordEnabled 按哈希是否为空派生（前端据此展示「已设置密码」状态）。
     * 返回深拷贝后的脱敏对象，不改动原始对象（save 场景避免污染落库入参）。
     */
    private GeneralConfig maskLovePasswords(GeneralConfig config) {
        if (config == null || config.getSpec() == null) {
            return config;
        }
        GeneralConfig copy = objectMapper.convertValue(config, GeneralConfig.class);
        Love love = copy.getSpec() != null ? copy.getSpec().getLove() : null;
        if (love != null) {
            maskModulePassword(love.getOurStory());
            maskModulePassword(love.getLovePhoto());
            maskModulePassword(love.getLoveDaily());
        }
        return copy;
    }

    private static void maskModulePassword(ModuleSwitch module) {
        if (module == null) {
            return;
        }
        module.setPasswordEnabled(!isBlank(module.getPasswordHash()));
        module.setPasswordHash(null);
        module.setPassword(null);
        module.setPasswordRemoved(null);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 维护时间窗口校验：startTime 与 endTime 同时存在且 start ≥ end（含等号）时拒绝保存。
     * 时间字符串按 {@link MaintenanceResolver#parseTime} 解析（非法视为未设置，
     * 由输出端按该语义兜底判定，不因脏数据拒绝整个保存）。
     */
    private static void validateMaintenance(Maintenance maintenance) {
        if (maintenance == null) {
            return;
        }
        Instant start = MaintenanceResolver.parseTime(maintenance.getStartTime());
        Instant end = MaintenanceResolver.parseTime(maintenance.getEndTime());
        if (start != null && end != null && !start.isBefore(end)) {
            throw new IllegalArgumentException("维护结束时间必须晚于开始时间");
        }
    }
}
