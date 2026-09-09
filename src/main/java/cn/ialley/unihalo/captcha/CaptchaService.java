package cn.ialley.unihalo.captcha;

import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

/**
 * 验证码业务服务：生成（公开接口）与校验（受保护公开写接口接入点）。
 *
 * <p>校验规则：设置页 captchaConfig 开关关闭时直接放行；开启时仅对
 * {@link CaptchaScope} 对应 scope 开启的功能要求携带 query 参数
 * {@code captchaId} + {@code captchaCode}，校验通过后作废（一次性）。</p>
 *
 * @author 小莫唐尼
 */
public interface CaptchaService {

    /**
     * 生成验证码（供公开接口返回给前端展示）。
     */
    Mono<CaptchaVo> generate();

    /**
     * 校验请求携带的验证码；失败抛出 {@link CaptchaValidationException}。
     *
     * @param request 当前请求（读取 captchaId/captchaCode query 参数）
     * @param scope   生效范围（总开关开启且该 scope 开启时才要求验证码；
     *                scope 配置缺省视为开启）
     */
    Mono<Void> requireValid(ServerRequest request, CaptchaScope scope);
}
