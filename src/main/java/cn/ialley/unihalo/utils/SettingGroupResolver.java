package cn.ialley.unihalo.utils;

import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

/**
 * 设置组读取工具（方案 B 三域重组后使用）。
 *
 * <p>插件设置合并为三个功能域组后，原模块级配置（如 loveConfig / linkConfig /
 * captchaConfig）成为域组下的子对象（如 featureConfig.loveConfig）。本工具按
 * 「新路径优先、旧顶层键回退」读取，兼容升级期 ConfigMap 仍是旧结构的情况。</p>
 *
 * @author 小莫唐尼
 */
public final class SettingGroupResolver {

    private SettingGroupResolver() {
    }

    /**
     * 读取域组下的模块子对象。
     *
     * @param fetcher 设置读取器
     * @param domain  新域组名（featureConfig / safetyConfig / integrationConfig）
     * @param module  模块名（loveConfig / linkConfig / auditConfig / captchaConfig /
     *                appConfig / pluginConfig），同时作为旧结构顶层键回退
     */
    public static Mono<JsonNode> group(ReactiveSettingFetcher fetcher, String domain,
            String module) {
        Mono<JsonNode> domainMono = fetcher.getSettingValue(domain);
        if (domainMono == null) {
            // 兼容 Mockito 未 stub 域键（返回 null）的调用方/测试
            return fallback(fetcher, module);
        }
        return domainMono
                .map(node -> {
                    if (node == null) {
                        return JsonNodeFactory.instance.missingNode();
                    }
                    JsonNode child = node.get(module);
                    return child == null ? JsonNodeFactory.instance.missingNode() : child;
                })
                .filter(node -> !node.isMissingNode() && !node.isNull())
                .switchIfEmpty(Mono.defer(() -> fallback(fetcher, module)));
    }

    private static Mono<JsonNode> fallback(ReactiveSettingFetcher fetcher, String module) {
        Mono<JsonNode> moduleMono = fetcher.getSettingValue(module);
        return moduleMono == null ? Mono.empty() : moduleMono;
    }
}
