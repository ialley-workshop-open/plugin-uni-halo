package cn.ialley.unihalo.captcha;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;
import tools.jackson.databind.node.JsonNodeFactory;

/**
 * 验证码业务服务实现。
 *
 * <p>配置读取自设置页 {@code captchaConfig} 分组：enabled（默认开）、type
 * （默认 ALPHANUMERIC）、captchaLength（默认 4）、arithmeticRange（默认 10）。
 * 开关关闭时 {@link #requireValid(ServerRequest)} 直接放行。</p>
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private static final String SETTING_GROUP_CAPTCHA = "captchaConfig";
    private static final String KEY_ENABLED = "enabled";
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
    public Mono<Void> requireValid(ServerRequest request) {
        return captchaConfig().flatMap(config -> {
            if (!config.enabled()) {
                // 开关关闭：直接放行
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
        return settingFetcher.getSettingValue(SETTING_GROUP_CAPTCHA)
                .defaultIfEmpty(JsonNodeFactory.instance.objectNode())
                .map(node -> new CaptchaConfig(
                        node.path(KEY_ENABLED).asBoolean(true),
                        parseType(node.path(KEY_TYPE).asText(CaptchaType.ALPHANUMERIC.name())),
                        node.path(KEY_LENGTH).asInt(4),
                        node.path(KEY_RANGE).asInt(10)));
    }

    private static CaptchaType parseType(String name) {
        try {
            return CaptchaType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return CaptchaType.ALPHANUMERIC;
        }
    }

    private record CaptchaConfig(boolean enabled, CaptchaType type, int length, int range) {
    }
}
