package cn.ialley.unihalo.services.impl;

import java.time.Instant;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.GeneralConfig;
import cn.ialley.unihalo.scheme.GeneralConfig.About;
import cn.ialley.unihalo.scheme.GeneralConfig.Assets;
import cn.ialley.unihalo.scheme.GeneralConfig.Banner;
import cn.ialley.unihalo.scheme.GeneralConfig.Blogger;
import cn.ialley.unihalo.scheme.GeneralConfig.Copyright;
import cn.ialley.unihalo.scheme.GeneralConfig.Disclaimer;
import cn.ialley.unihalo.scheme.GeneralConfig.Gallery;
import cn.ialley.unihalo.scheme.GeneralConfig.Home;
import cn.ialley.unihalo.scheme.GeneralConfig.Pages;
import cn.ialley.unihalo.scheme.GeneralConfig.PostDetail;
import cn.ialley.unihalo.scheme.GeneralConfig.Profile;
import cn.ialley.unihalo.scheme.GeneralConfig.Social;
import cn.ialley.unihalo.scheme.GeneralConfig.Spec;
import cn.ialley.unihalo.services.GeneralConfigService;
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

    private final ReactiveExtensionClient client;
    private final ReactiveSettingFetcher settingFetcher;

    @Override
    public Mono<GeneralConfig> get() {
        return client.fetch(GeneralConfig.class, Constants.GENERAL_CONFIG_SINGLETON_NAME)
                .switchIfEmpty(Mono.defer(this::defaultWithLegacy));
    }

    @Override
    public Mono<GeneralConfig> save(GeneralConfig config) {
        GeneralConfig body = config == null ? new GeneralConfig() : config;
        // 写入前合并：默认值 → 历史 ConfigMap 旧值 → 请求体非空字段，防丢字段/防空写
        return legacyOverlay().map(overlay -> {
            ObjectNode merged = merge(defaultSpecTree(), overlay);
            if (body.getSpec() != null) {
                merged = merge(merged, (ObjectNode) objectMapper.valueToTree(body.getSpec()));
            }
            GeneralConfig target = new GeneralConfig();
            target.setSpec(treeToValue(merged));
            return target;
        }).flatMap(target -> client
                .fetch(GeneralConfig.class, Constants.GENERAL_CONFIG_SINGLETON_NAME)
                .flatMap(existing -> {
                    existing.setSpec(target.getSpec());
                    return client.update(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    Metadata metadata = new Metadata();
                    metadata.setName(Constants.GENERAL_CONFIG_SINGLETON_NAME);
                    metadata.setCreationTimestamp(Instant.now());
                    target.setMetadata(metadata);
                    return client.create(target);
                })));
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
        return spec;
    }

    private static GeneralConfig.Preferences buildDefaultPreferences() {
        GeneralConfig.Preferences preferences = new GeneralConfig.Preferences();
        preferences.setHomeListLayout("h_row_col1");
        preferences.setArticleCardType("lr_image_text");
        preferences.setAvatarRadius(true);
        return preferences;
    }

    private static Profile buildDefaultProfile() {
        Profile profile = new Profile();

        GeneralConfig.AppInfo appInfo = new GeneralConfig.AppInfo();
        appInfo.setName("uni-halo");
        appInfo.setLogo("https://uni-halo.925i.cn/uni_halo/uni_halo_logo.png");
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
        home.setUseCategory(true);
        Banner banner = new Banner();
        banner.setEnabled(true);
        banner.setShowTitle(true);
        banner.setShowIndicator(true);
        banner.setHeight("400rpx");
        banner.setDotPosition("right");
        home.setBannerConfig(banner);
        pages.setHomeConfig(home);

        Gallery gallery = new Gallery();
        gallery.setPageTitle("图库");
        gallery.setUseWaterfall(true);
        pages.setGalleryConfig(gallery);

        About about = new About();
        about.setPageTitle("关于博主");
        about.setBgImageUrl("https://uni-halo.925i.cn/uni_halo/uni_halo_profile_bg.jpg");
        about.setWaveImageUrl("https://uni-halo.925i.cn/uni_halo/uni_halo_about_wave.gif");
        pages.setAboutConfig(about);
        return pages;
    }

    private static Assets buildDefaultAssets() {
        Assets assets = new Assets();
        assets.setDefaultImageUrl("https://api.7trees.cn/img");
        assets.setDefaultThumbnailUrl("https://tenapi.cn/v2/acg");
        assets.setDefaultStaticThumbnailUrl("");
        assets.setDefaultAvatarUrl("https://api.qjqq.cn/api/MiYouShe");
        assets.setLoadingGifUrl("https://uni-halo.925i.cn/uni_halo/uni_halo_img_lazyload.gif");
        assets.setLoadingErrUrl("https://uni-halo.925i.cn/uni_halo/3gVrtNeEDFeuMK14Vtytb9ml73TZj3dX.gif");
        assets.setLoadingEmptyUrl("");
        return assets;
    }
}
