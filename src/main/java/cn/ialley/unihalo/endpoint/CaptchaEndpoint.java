package cn.ialley.unihalo.endpoint;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.captcha.CaptchaService;
import cn.ialley.unihalo.constants.Constants;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * 验证码公开接口（app 端/小程序端，匿名可访问）。
 *
 * <p>{@code GET plugins/plugin-uni-halo/captcha/generate} 返回 {@code {id, imageBase64}}，
 * 小程序端展示图片并让用户输入，提交申请/解锁时携带 id + 输入码（query 参数）。
 * 匿名放行由 role-anonymous.yaml 的全局规则覆盖。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class CaptchaEndpoint implements CustomEndpoint {

    private final CaptchaService captchaService;

    public CaptchaEndpoint(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.PUBLIC_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.CAPTCHA_API_BASE_PATH, this::generateCaptcha)
                .build();
    }

    private Mono<ServerResponse> generateCaptcha(ServerRequest request) {
        return captchaService.generate()
                .flatMap(captcha -> ServerResponse.ok().bodyValue(captcha));
    }
}
