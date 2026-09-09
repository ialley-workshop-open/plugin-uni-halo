package cn.ialley.unihalo.utils;

import java.time.Instant;
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
        appInfo.put("name", "UniHalo v3.x");
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
        // loveConfig（2026-09-03 迁入 GeneralConfig.spec.love）：旧 ConfigMap 值仅作
        // 导入源，不再透传输出；loveConfig 顶层键由 spec.love 重建
        return settings;
    }

    private GeneralConfig config(String json) throws Exception {
        return objectMapper.readValue(json, GeneralConfig.class);
    }

    @Test
    void shouldStripSensitiveAndRetireFieldsAndPassThroughOthers() throws Exception {
        // 内容组由 GeneralConfig 重建；love 分区映射回旧 loveConfig 顶层键（脱敏输出）
        GeneralConfig config = config("""
                {"spec":{"profile":{"blogger":{"nickname":"测试博主"}},
                  "love":{"loveEnabled":true,
                    "pageImages":{"bgImageUrl":"https://img/bg.png",
                      "waveImageUrl":"/plugins/plugin-uni-halo/assets/uni_halo_about_wave.gif"},
                    "ourStory":{"enabled":true,"iconUrl":"","passwordHash":"$2a$10$fakehash"},
                    "lovePhoto":{"enabled":false,"iconUrl":""},
                    "loveDaily":{"enabled":false,"iconUrl":""}}}}
                """);

        ObjectNode root = assembler.assemble(legacySettings(), config);

        // 剔除：startConfig / auditModeData / tokenConfig
        assertNull(root.get("appConfig").get("startConfig"), "startConfig 已下线应剔除");
        assertEquals("UniHalo v3.x", root.get("appConfig").get("appInfo").get("name").asText());
        assertNull(root.get("auditConfig").get("auditModeData"), "auditModeData 死字段应剔除");
        assertTrue(root.get("auditConfig").get("auditModeEnabled").asBoolean());
        // 内容组由 GeneralConfig 重建：basicConfig 不再携带 tokenConfig
        assertFalse(root.has("basicConfig"), "profile 仅 blogger 时 basicConfig 不应输出");
        assertTrue(root.has("authorConfig"), "authorConfig 应由 GeneralConfig 重建");
        assertEquals("测试博主",
                root.get("authorConfig").get("blogger").get("nickname").asText());
        // loveConfig 由 spec.love 重建（不再透传旧 ConfigMap loveConfig 值）
        assertTrue(root.get("loveConfig").get("loveEnabled").asBoolean());
        assertTrue(root.get("loveConfig").get("ourStory").get("enabled").asBoolean());
        // 2026-09-08 脱敏：pageImages 仅 bgImageUrl；模块仅 enabled + passwordEnabled
        assertEquals("https://img/bg.png",
                root.get("loveConfig").get("pageImages").get("bgImageUrl").asText());
        assertFalse(root.get("loveConfig").get("pageImages").has("waveImageUrl"),
                "已下线 waveImageUrl 不应输出");
        assertTrue(root.get("loveConfig").get("ourStory").get("passwordEnabled").asBoolean(),
                "ourStory 有密码哈希 → passwordEnabled=true");
        assertFalse(root.get("loveConfig").get("lovePhoto").get("passwordEnabled").asBoolean());
        assertFalse(root.get("loveConfig").get("ourStory").has("passwordHash"),
                "密码哈希一律不下发");
        assertFalse(root.get("loveConfig").get("ourStory").has("iconUrl"),
                "已下线 iconUrl 不应输出");
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
                    "homeConfig":{"pageTitle":"首页","useQuickNavigation":true,"useCategory":false},
                    "galleryConfig":{"pageTitle":"图库"},
                    "categoryConfig":{"pageTitle":"分类"},
                    "momentConfig":{"pageTitle":"瞬间"},
                    "aboutConfig":{"pageTitle":"关于博主","bgImageUrl":"https://bg/1.jpg","waveImageUrl":""}
                  },
                  "assets":{"loadingGifUrl":"https://img/loading.gif","loadingErrUrl":"https://img/err.png"}
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
        // pageConfig：homeConfig 轮播渲染参数已下线（2026-09-08），分类页/瞬间页标题随行输出
        assertFalse(root.get("pageConfig").get("homeConfig").get("useCategory").asBoolean());
        assertFalse(root.get("pageConfig").get("homeConfig").has("bannerConfig"),
                "bannerConfig 已下线不应输出");
        assertEquals("图库", root.get("pageConfig").get("galleryConfig").get("pageTitle").asText());
        assertEquals("分类",
                root.get("pageConfig").get("categoryConfig").get("pageTitle").asText());
        assertEquals("瞬间",
                root.get("pageConfig").get("momentConfig").get("pageTitle").asText());
        // imagesConfig：来自 assets（2026-09-08 起仅 loading/error 两键，默认图片配置已下线）
        assertEquals("https://img/loading.gif",
                root.get("imagesConfig").get("loadingGifUrl").asText());
        assertEquals("https://img/err.png",
                root.get("imagesConfig").get("loadingErrUrl").asText());
        assertFalse(root.get("imagesConfig").has("defaultImageUrl"),
                "defaultImageUrl 配置已下线不应输出");
        assertFalse(root.get("imagesConfig").has("loadingEmptyUrl"),
                "loadingEmptyUrl 配置已下线不应输出");
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
        // loveConfig 已属重建组：无 spec.love 时不再透传旧 ConfigMap 值
        assertFalse(root.has("loveConfig"));
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
        // loveConfig 已迁出 setting（2026-09-03）：featureConfig.loveConfig 残留不再展开输出
        assertFalse(root.has("loveConfig"));
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

    @Test
    void shouldRebuildLoveConfigFromSpecLove() throws Exception {
        GeneralConfig config = config("""
                {"spec":{"love":{
                  "loveEnabled":true,
                  "pageImages":{"bgImageUrl":"/plugins/plugin-uni-halo/assets/logo.png",
                    "waveImageUrl":"/plugins/plugin-uni-halo/assets/uni_halo_about_wave.gif",
                    "heartImageUrl":""},
                  "ourStory":{"enabled":true,"iconUrl":"https://img/our-story.png",
                    "passwordHash":"$2a$10$fakehash","passwordEnabled":true},
                  "lovePhoto":{"enabled":false,"iconUrl":""},
                  "loveDaily":{"enabled":true,"iconUrl":"https://img/love-daily.png",
                    "passwordHash":""}
                }}}
                """);

        ObjectNode root = assembler.assemble(legacySettings(), config);

        // spec.love → 旧顶层 loveConfig shape；2026-09-08 起脱敏输出：
        // pageImages 仅 bgImageUrl，模块仅 enabled + passwordEnabled（密码字段不下发）
        assertTrue(root.get("loveConfig").get("loveEnabled").asBoolean());
        assertEquals("/plugins/plugin-uni-halo/assets/logo.png",
                root.get("loveConfig").get("pageImages").get("bgImageUrl").asText());
        assertFalse(root.get("loveConfig").get("pageImages").has("waveImageUrl"));
        assertFalse(root.get("loveConfig").get("pageImages").has("heartImageUrl"));
        assertTrue(root.get("loveConfig").get("loveDaily").get("enabled").asBoolean());
        // passwordEnabled 按哈希非空派生：ourStory 有哈希 → true；loveDaily 哈希为空 → false
        assertTrue(root.get("loveConfig").get("ourStory").get("passwordEnabled").asBoolean());
        assertFalse(root.get("loveConfig").get("loveDaily").get("passwordEnabled").asBoolean());
        assertFalse(root.get("loveConfig").get("ourStory").has("passwordHash"),
                "密码哈希一律不下发");
        assertFalse(root.get("loveConfig").get("ourStory").has("iconUrl"));
        // legacySettings 顶层 loveConfig 旧值（loveEnabled=true）被 spec.love 覆盖且一致
        // 已模型化内容字段不随 typed 输出
        assertFalse(root.get("loveConfig").has("loveDate"));
        assertFalse(root.get("loveConfig").has("loveInfo"));
    }

    // ===== 维护模式（2026-09-04 新增，additive 顶层键）=====

    private static final Instant NOW = Instant.parse("2026-09-04T12:00:00Z");

    @Test
    void shouldNotOutputMaintenanceWhenDisabled() throws Exception {
        GeneralConfig config = config("""
                {"spec":{"maintenance":{"enabled":false,"title":"维护","description":""}}}
                """);

        ObjectNode root = assembler.assemble(null, config, NOW);

        assertFalse(root.has("maintenance"), "enabled=false 不应输出 maintenance 键");
    }

    @Test
    void shouldOutputMaintenanceWhenActive() throws Exception {
        GeneralConfig config = config("""
                {"spec":{"maintenance":{"enabled":true,"title":"系统升级维护",
                  "notice":"升级维护中，请稍后再试",
                  "description":"<p>维护详情</p>",
                  "startTime":"2026-09-03T22:00:00Z","endTime":"2026-09-05T02:00:00Z"}}}
                """);

        ObjectNode root = assembler.assemble(null, config, NOW);

        var maintenance = root.get("maintenance");
        assertTrue(root.has("maintenance"));
        assertEquals("active", maintenance.get("status").asText());
        assertEquals("系统升级维护", maintenance.get("title").asText());
        assertEquals("升级维护中，请稍后再试", maintenance.get("notice").asText());
        assertEquals("<p>维护详情</p>", maintenance.get("description").asText());
        assertEquals("2026-09-03T22:00:00Z", maintenance.get("startTime").asText());
        assertEquals("2026-09-05T02:00:00Z", maintenance.get("endTime").asText());
    }

    @Test
    void shouldOutputMaintenanceWhenScheduled() throws Exception {
        GeneralConfig config = config("""
                {"spec":{"maintenance":{"enabled":true,"title":"维护预告",
                  "startTime":"2026-09-05T00:00:00Z","endTime":null}}}
                """);

        ObjectNode root = assembler.assemble(null, config, NOW);

        assertEquals("scheduled", root.get("maintenance").get("status").asText());
        // endTime 为空 → 不输出该字段（客户端不渲染倒计时区）
        assertFalse(root.get("maintenance").has("endTime"));
    }

    @Test
    void shouldNotOutputMaintenanceWhenAutoEnded() throws Exception {
        // endTime 已到 → 视为已按计划自动结束（到点自动关闭）
        GeneralConfig config = config("""
                {"spec":{"maintenance":{"enabled":true,"title":"已结束的维护",
                  "startTime":"2026-09-02T00:00:00Z","endTime":"2026-09-03T00:00:00Z"}}}
                """);

        ObjectNode root = assembler.assemble(null, config, NOW);

        assertFalse(root.has("maintenance"), "endTime 已到不应输出 maintenance 键");
    }

    @Test
    void shouldOutputImmediateMaintenanceWithoutTimes() throws Exception {
        // enabled=true 无窗口 → active（立即维护、持续至手动关闭），startTime/endTime 均不输出
        GeneralConfig config = config("""
                {"spec":{"maintenance":{"enabled":true,"title":"立即维护","description":""}}}
                """);

        ObjectNode root = assembler.assemble(null, config, NOW);

        assertEquals("active", root.get("maintenance").get("status").asText());
        assertFalse(root.get("maintenance").has("startTime"));
        assertFalse(root.get("maintenance").has("endTime"));
    }
}
