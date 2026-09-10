package cn.ialley.unihalo.process;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.theme.dialect.TemplateHeadProcessor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import cn.ialley.unihalo.utils.SettingGroupResolver;

/**
 * 主题悬浮窗注入处理器 注入小程序太阳码展示窗口。
 *
 * <p>向主题页面 {@code <head>} 注入：内联配置块 {@code window.__UNI_HALO_FLOAT_MINI_PROFILE__} +
 * 静态样式/脚本（经插件 ReverseProxy 暴露，带插件版本号防缓存）。所有展示与交互
 * （9 向定位/偏移/拖拽/关闭/贴边隐藏/页面显示范围）由前端 {@code float-mini-profile.js} 完成，服务端
 * 只负责读取设置并按开关 fail closed（关闭/配置异常/未选太阳码均不注入，页面零残留）。</p>
 *
 * <p>配置存放：setting.yaml 新增 {@code themeConfig} 域（Tab「主题展示」）下的
 * {@code floatingWindow} 组，经 {@link SettingGroupResolver#group} 读取（域路径优先、
 * 旧顶层键回退）。设计见 {@code .docs/floating-window-design.md}。</p>
 *
 * @author 小莫唐尼
 */
@Slf4j
@Component
public class FloatingWindowHeadProcessor implements TemplateHeadProcessor {

    private static final String DOMAIN = "themeConfig";
    private static final String MODULE = "floatingWindow";

    private static final String CSS_URL_TEMPLATE =
            "/plugins/plugin-uni-halo/assets/static/floating-window/float-mini-profile.css?version=%s";
    private static final String JS_URL_TEMPLATE =
            "/plugins/plugin-uni-halo/assets/static/floating-window/float-mini-profile.js?version=%s";

    private final ReactiveSettingFetcher settingFetcher;
    private final PluginWrapper pluginWrapper;

    /**
     * 插件 Spring 上下文未注册 Jackson 3 ObjectMapper bean，内部自行创建
     * （与 PublicConfigAssembler / GeneralConfigServiceImpl 同套路）。
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FloatingWindowHeadProcessor(ReactiveSettingFetcher settingFetcher,
            PluginWrapper pluginWrapper) {
        this.settingFetcher = settingFetcher;
        this.pluginWrapper = pluginWrapper;
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
            IElementModelStructureHandler structureHandler) {
        return SettingGroupResolver.group(settingFetcher, DOMAIN, MODULE)
            .flatMap(node -> {
                // fail closed：总开关未开或未选择太阳码时不注入任何内容
                if (!node.path("enabled").asBoolean(false)
                        || node.path("imageUrl").asText("").isBlank()) {
                    return Mono.empty();
                }
                try {
                    IModelFactory factory = context.getModelFactory();
                    model.add(factory.createText(componentScript(buildConfig(node))));
                } catch (Exception e) {
                    // 配置序列化失败按未启用处理，避免注入残缺脚本
                    log.warn("序列化悬浮窗配置失败，本次不注入：{}", e.getMessage());
                }
                return Mono.empty();
            });
    }

    /**
     * 组装前端配置（缺失字段取默认值，字段显式置空时保持为空由前端不渲染）。
     */
    private ObjectNode buildConfig(JsonNode node) {
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.put("enabled", node.path("enabled").asBoolean(false));
        config.put("pageScope", node.path("pageScope").asText("all"));
        config.put("pagePatterns", node.path("pagePatterns").asText(""));
        config.put("position", node.path("position").asText("bottom-right"));
        config.put("offsetX", node.path("offsetX").asInt(0));
        config.put("offsetY", node.path("offsetY").asInt(0));
        config.put("name", node.path("name").asText("小程序"));
        config.put("nameSize", node.path("nameSize").asInt(14));
        config.put("nameColor", node.path("nameColor").asText("#333333"));
        config.put("description", node.path("description").asText(""));
        config.put("descSize", node.path("descSize").asInt(12));
        config.put("descColor", node.path("descColor").asText("#999999"));
        config.put("imageUrl", node.path("imageUrl").asText(""));
        config.put("imageSize", node.path("imageSize").asInt(100));
        config.put("dragEnabled", node.path("dragEnabled").asBoolean(true));
        config.put("closeEnabled", node.path("closeEnabled").asBoolean(true));
        config.put("edgeHideEnabled", node.path("edgeHideEnabled").asBoolean(true));
        config.put("edgeHideDistance", node.path("edgeHideDistance").asInt(24));
        config.put("rememberClosed", node.path("rememberClosed").asBoolean(true));
        return config;
    }

    private String componentScript(ObjectNode config) {
        String version = pluginWrapper.getDescriptor().getVersion();
        // 防名称/描述等文本含 </script> 提前闭合脚本标签
        String configJson = objectMapper.writeValueAsString(config).replace("</", "<\\/");
        return """
            <!-- uni-halo v3.x floating window start -->
            <script>window.__UNI_HALO_FLOAT_MINI_PROFILE__ = %s;</script>
            <link rel="stylesheet" href="%s" />
            <script defer src="%s"></script>
            <!-- uni-halo v3.x floating window end -->
            """.formatted(configJson, CSS_URL_TEMPLATE.formatted(version),
                JS_URL_TEMPLATE.formatted(version));
    }
}
