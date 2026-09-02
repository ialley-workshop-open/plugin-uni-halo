package cn.ialley.unihalo.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import cn.ialley.unihalo.scheme.GeneralConfig;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 公开配置输出合成器单测：内容组重建、敏感/下线字段剔除、其余组透传。
 *
 * @author 小莫唐尼
 */
class PublicConfigAssemblerTest {

    private final PublicConfigAssembler assembler = new PublicConfigAssembler();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造一份"旧 ConfigMap"形态的设置（含 🟦 内容组旧值、敏感/死字段）。
     */
    private static Map<String, JsonNode> legacySettings() {
        Map<String, JsonNode> settings = new LinkedHashMap<>();

        ObjectNode basicConfig = JsonNodeFactory.instance.objectNode();
        ObjectNode tokenConfig = JsonNodeFactory.instance.objectNode();
        tokenConfig.put("personalToken", "should-not-leak");
        basicConfig.set("tokenConfig", tokenConfig);
        basicConfig.put("showAboutSystem", true);
        settings.put("basicConfig", basicConfig);

        ObjectNode pageConfig = JsonNodeFactory.instance.objectNode();
        pageConfig.set("homeConfig", JsonNodeFactory.instance.objectNode());
        settings.put("pageConfig", pageConfig);

        ObjectNode authorConfig = JsonNodeFactory.instance.objectNode();
        ObjectNode blogger = JsonNodeFactory.instance.objectNode();
        blogger.put("nickname", "legacy-nickname");
        authorConfig.set("blogger", blogger);
        settings.put("authorConfig", authorConfig);

        ObjectNode imagesConfig = JsonNodeFactory.instance.objectNode();
        imagesConfig.put("defaultImageUrl", "legacy-image-url");
        settings.put("imagesConfig", imagesConfig);

        ObjectNode appConfig = JsonNodeFactory.instance.objectNode();
        appConfig.set("startConfig", JsonNodeFactory.instance.objectNode());
        ObjectNode appInfo = JsonNodeFactory.instance.objectNode();
        appInfo.put("name", "uni-halo v2.0");
        appInfo.put("appId", "wx-xxx");
        appInfo.put("appSecret", "secret-not-for-public");
        appConfig.set("appInfo", appInfo);
        settings.put("appConfig", appConfig);

        ObjectNode auditConfig = JsonNodeFactory.instance.objectNode();
        auditConfig.put("auditModeEnabled", true);
        auditConfig.set("auditModeData", JsonNodeFactory.instance.objectNode());
        settings.put("auditConfig", auditConfig);

        ObjectNode loveConfig = JsonNodeFactory.instance.objectNode();
        loveConfig.put("loveEnabled", true);
        settings.put("loveConfig", loveConfig);
        return settings;
    }

    private GeneralConfig config(String json) throws Exception {
        return objectMapper.readValue(json, GeneralConfig.class);
    }

    @Test
    void shouldStripSensitiveAndRetireFieldsAndPassThroughOthers() throws Exception {
        // 仅 profile.blogger 有值，其余内容组不输出
        GeneralConfig config = config("""
                {"spec":{"profile":{"blogger":{"nickname":"测试博主"}}}}
                """);

        ObjectNode root = assembler.assemble(legacySettings(), config);

        // 剔除：startConfig / auditModeData / tokenConfig
        assertNull(root.get("appConfig").get("startConfig"), "startConfig 已下线应剔除");
        assertEquals("uni-halo v2.0", root.get("appConfig").get("appInfo").get("name").asText());
        assertNull(root.get("auditConfig").get("auditModeData"), "auditModeData 死字段应剔除");
        assertTrue(root.get("auditConfig").get("auditModeEnabled").asBoolean());
        // 内容组由 GeneralConfig 重建：basicConfig 不再携带 tokenConfig
        assertFalse(root.has("basicConfig"), "profile 仅 blogger 时 basicConfig 不应输出");
        assertTrue(root.has("authorConfig"), "authorConfig 应由 GeneralConfig 重建");
        assertEquals("测试博主",
                root.get("authorConfig").get("blogger").get("nickname").asText());
        // 其余组透传
        assertTrue(root.get("loveConfig").get("loveEnabled").asBoolean());
    }

    @Test
    void shouldRebuildContentGroupsFromGeneralConfigSpec() throws Exception {
        GeneralConfig config = config("""
                {"spec":{
                  "profile":{
                    "blogger":{"nickname":"新博主","avatar":"https://a/1.png","email":"a@b.com","description":"简介"},
                    "social":{"enabled":true,"qq":"123"},
                    "copyrightConfig":{"enabled":true,"content":"© 版权"},
                    "showAboutSystem":false,
                    "disclaimers":{"enabled":false,"content":""},
                    "postDetailConfig":{"showComment":true,"copyrightEnabled":false,
                       "copyrightAuthor":"uni-halo","copyrightDesc":"desc","copyrightViolation":"vio"}
                  },
                  "pages":{
                    "homeConfig":{"pageTitle":"首页","useQuickNavigation":true,"useCategory":false,
                      "bannerConfig":{"enabled":true,"showTitle":true,"showIndicator":true,"height":"400rpx","dotPosition":"right"}},
                    "galleryConfig":{"pageTitle":"图库","useWaterfall":true},
                    "aboutConfig":{"pageTitle":"关于博主","bgImageUrl":"https://bg/1.jpg","waveImageUrl":""}
                  },
                  "assets":{"defaultImageUrl":"https://img/1.png","defaultThumbnailUrl":"https://img/2.png",
                    "defaultStaticThumbnailUrl":"","defaultAvatarUrl":"https://img/3.png","loadingGifUrl":"",
                    "loadingErrUrl":"","loadingEmptyUrl":""}
                }}
                """);

        ObjectNode root = assembler.assemble(legacySettings(), config);

        // authorConfig：来自 profile.blogger/social，旧值被覆盖
        assertEquals("新博主", root.get("authorConfig").get("blogger").get("nickname").asText());
        assertEquals("123", root.get("authorConfig").get("social").get("qq").asText());
        // basicConfig：仅内容子键，无 tokenConfig
        assertEquals("© 版权",
                root.get("basicConfig").get("copyrightConfig").get("content").asText());
        assertFalse(root.get("basicConfig").get("showAboutSystem").asBoolean());
        assertFalse(root.get("basicConfig").has("tokenConfig"));
        // pageConfig：三层嵌套（bannerConfig 渲染参数）与旧值覆盖
        assertEquals("400rpx",
                root.get("pageConfig").get("homeConfig").get("bannerConfig").get("height").asText());
        assertFalse(root.get("pageConfig").get("homeConfig").get("useCategory").asBoolean());
        assertEquals("图库", root.get("pageConfig").get("galleryConfig").get("pageTitle").asText());
        // imagesConfig：来自 assets（覆盖旧值）
        assertEquals("https://img/1.png",
                root.get("imagesConfig").get("defaultImageUrl").asText());
        assertNotNull(root.get("appConfig").get("appInfo"));
    }

    @Test
    void shouldKeepRawSettingsWhenNoContentSpec() throws Exception {
        GeneralConfig config = config("{}");

        ObjectNode root = assembler.assemble(legacySettings(), config);

        assertFalse(root.has("authorConfig"));
        assertFalse(root.has("pageConfig"));
        assertFalse(root.has("imagesConfig"));
        assertFalse(root.has("basicConfig"));
        assertTrue(root.has("loveConfig"));
        assertNull(root.get("appConfig").get("startConfig"));
    }

    @Test
    void shouldExpandDomainGroupsIntoLegacyShape() throws Exception {
        // 方案 B 三域结构（featureConfig/safetyConfig/integrationConfig）
        Map<String, JsonNode> settings = new LinkedHashMap<>();

        ObjectNode featureConfig = JsonNodeFactory.instance.objectNode();
        ObjectNode loveConfig = JsonNodeFactory.instance.objectNode();
        loveConfig.put("loveEnabled", true);
        ObjectNode ourStory = JsonNodeFactory.instance.objectNode();
        ourStory.put("enabled", true);
        loveConfig.set("ourStory", ourStory);
        featureConfig.set("loveConfig", loveConfig);
        ObjectNode linkConfig = JsonNodeFactory.instance.objectNode();
        linkConfig.put("submissionEnabled", false);
        featureConfig.set("linkConfig", linkConfig);
        settings.put("featureConfig", featureConfig);

        ObjectNode safetyConfig = JsonNodeFactory.instance.objectNode();
        ObjectNode captchaConfig = JsonNodeFactory.instance.objectNode();
        captchaConfig.put("enabled", true);
        ObjectNode scope = JsonNodeFactory.instance.objectNode();
        scope.put("linkSubmission", true);
        captchaConfig.set("scope", scope);
        safetyConfig.set("captchaConfig", captchaConfig);
        ObjectNode auditConfig = JsonNodeFactory.instance.objectNode();
        auditConfig.put("auditModeEnabled", true);
        safetyConfig.set("auditConfig", auditConfig);
        settings.put("safetyConfig", safetyConfig);

        ObjectNode integrationConfig = JsonNodeFactory.instance.objectNode();
        ObjectNode pluginConfig = JsonNodeFactory.instance.objectNode();
        ObjectNode votePlugin = JsonNodeFactory.instance.objectNode();
        votePlugin.put("enabled", true);
        pluginConfig.set("votePlugin", votePlugin);
        integrationConfig.set("pluginConfig", pluginConfig);
        settings.put("integrationConfig", integrationConfig);

        // 基本配置（应用信息：仅名称/图标，动态码字段已下线）
        ObjectNode baseConfig = JsonNodeFactory.instance.objectNode();
        ObjectNode appInfo = JsonNodeFactory.instance.objectNode();
        appInfo.put("name", "uni-halo");
        appInfo.put("logo", "https://uni-halo.925i.cn/uni_halo/uni_halo_logo.png");
        baseConfig.set("appInfo", appInfo);
        settings.put("baseConfig", baseConfig);

        ObjectNode root = assembler.assemble(settings, config("{}"));

        // 域组展开为旧模块顶层键
        assertTrue(root.get("loveConfig").get("loveEnabled").asBoolean());
        assertTrue(root.get("loveConfig").get("ourStory").get("enabled").asBoolean());
        assertFalse(root.get("linkConfig").get("submissionEnabled").asBoolean());
        assertTrue(root.get("captchaConfig").get("enabled").asBoolean());
        assertTrue(root.get("captchaConfig").get("scope").get("linkSubmission").asBoolean());
        assertTrue(root.get("auditConfig").get("auditModeEnabled").asBoolean());
        assertTrue(root.get("pluginConfig").get("votePlugin").get("enabled").asBoolean());
        // baseConfig.appInfo 归并为旧 appConfig.appInfo 形态
        assertEquals("uni-halo", root.get("appConfig").get("appInfo").get("name").asText());
        assertFalse(root.get("appConfig").has("startConfig"));
        assertFalse(root.get("appConfig").get("appInfo").has("appId"));
        // 域组/基本配置组本身不再出现在输出
        assertFalse(root.has("featureConfig"));
        assertFalse(root.has("safetyConfig"));
        assertFalse(root.has("integrationConfig"));
        assertFalse(root.has("baseConfig"));
        // 无内容 spec 时内容组不输出
        assertFalse(root.has("authorConfig"));
    }
}
