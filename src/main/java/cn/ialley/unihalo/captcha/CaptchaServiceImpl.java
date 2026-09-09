package cn.ialley.unihalo.captcha;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import cn.ialley.unihalo.utils.SettingGroupResolver;
import run.halo.app.plugin.ReactiveSettingFetcher;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

/**
 * 验证码业务服务实现。
 *
 * <p>配置读取自设置页 {@code captchaConfig} 分组：enabled（默认开）、scope
 * （生效范围：linkSubmission / loveAlbumUnlock，2026-09-03 起接口侧按范围生效，
 * 缺省视为开启）、type（默认 ALPHANUMERIC）、captchaLength（默认 4）、
 * arithmeticRange（默认 10）。总开关关闭或对应 scope 关闭时
 * {@link #requireValid(ServerRequest, CaptchaScope)} 直接放行。</p>
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private static final String SETTING_GROUP_CAPTCHA = "captchaConfig";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_SCOPE = "scope";
    private static final String KEY_TYPE = "type";
    private static final String KEY_LENGTH = "captchaLength";
    private static final String KEY_RANGE = "arithmeticRange";

    private final CaptchaManager captchaManager;
    private final ReactiveSettingFetcher settingFetcher;

    @Override
    public Mono<CaptchaVo> generate() {
        return captchaConfig()
                .flatMap(config -> captchaManager.generate(config.type(), config.length(),
                        config.range()))
                .map(captcha -> new CaptchaVo(captcha.id(), captcha.imageBase64()));
    }

    @Override
    public Mono<Void> requireValid(ServerRequest request, CaptchaScope scope) {
        return captchaConfig().flatMap(config -> {
            if (!config.enabled() || !config.scopeEnabled(scope)) {
                // 总开关关闭或该功能不在生效范围内：直接放行
                return Mono.empty();
            }
            String id = request.queryParam("captchaId").orElse("");
            String code = request.queryParam("captchaCode").orElse("");
            if (id.isBlank() || code.isBlank()) {
                return Mono.error(new CaptchaValidationException("请先完成验证码"));
            }
            return captchaManager.verify(id, code, true)
                    .flatMap(valid -> {
                        if (!valid) {
                            return Mono.error(new CaptchaValidationException("验证码错误或已过期"));
                        }
                        // 一次性：校验通过即作废，防重放
                        return captchaManager.invalidate(id);
                    });
        });
    }

    private Mono<CaptchaConfig> captchaConfig() {
        return SettingGroupResolver.group(settingFetcher, "safetyConfig",
                SETTING_GROUP_CAPTCHA)
                .defaultIfEmpty(JsonNodeFactory.instance.objectNode())
                .map(node -> new CaptchaConfig(
                        node.path(KEY_ENABLED).asBoolean(true),
                        scopeEnabled(node, CaptchaScope.LINK_SUBMISSION),
                        scopeEnabled(node, CaptchaScope.LOVE_ALBUM_UNLOCK),
                        parseType(node.path(KEY_TYPE).asText(CaptchaType.ALPHANUMERIC.name())),
                        node.path(KEY_LENGTH).asInt(4),
                        node.path(KEY_RANGE).asInt(10)));
    }

    /**
     * scope 子开关缺省视为开启（兼容旧 ConfigMap 无 scope 配置）。
     */
    private static boolean scopeEnabled(JsonNode node, CaptchaScope scope) {
        JsonNode scopeNode = node.path(KEY_SCOPE);
        if (!scopeNode.isObject()) {
            return true;
        }
        return scopeNode.path(scope.configKey()).asBoolean(true);
    }

    private static CaptchaType parseType(String name) {
        try {
            return CaptchaType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return CaptchaType.ALPHANUMERIC;
        }
    }

    private record CaptchaConfig(boolean enabled, boolean linkSubmissionEnabled,
            boolean loveAlbumUnlockEnabled, CaptchaType type, int length, int range) {

        boolean scopeEnabled(CaptchaScope scope) {
            if (scope == null) {
                return true;
            }
            return switch (scope) {
                case LINK_SUBMISSION -> linkSubmissionEnabled;
                case LOVE_ALBUM_UNLOCK -> loveAlbumUnlockEnabled;
            };
        }
    }
}
