package cn.ialley.unihalo.captcha;

/**
 * 验证码校验失败异常（受保护公开写接口返回 403 + 附新验证码）。
 *
 * @author 小莫唐尼
 */
public class CaptchaValidationException extends RuntimeException {

    public CaptchaValidationException(String message) {
        super(message);
    }
}
