package cn.ialley.unihalo.captcha;

import reactor.core.publisher.Mono;

/**
 * 验证码管理：生成 / 校验 / 作废。
 *
 * <p>验证码为一次性（校验通过后 {@link #invalidate(String)} 作废，防重放），
 * 有效期 1 分钟、容量上限 100（超出先清理过期项）。</p>
 *
 * @author 小莫唐尼
 */
public interface CaptchaManager {

    /**
     * 生成的验证码：id（一次性）、code（正确答案，仅服务端持有）、imageBase64（对外图片）。
     */
    record Captcha(String id, String code, String imageBase64) {
    }

    /**
     * 生成验证码并缓存（key = 随机 id）。
     *
     * @param type            验证码类型
     * @param captchaLength   字符长度（ALPHANUMERIC 使用）
     * @param arithmeticRange 算术范围（ARITHMETIC 使用）
     */
    Mono<Captcha> generate(CaptchaType type, int captchaLength, int arithmeticRange);

    /**
     * 校验验证码（存在且未过期），不改变缓存状态。
     */
    Mono<Boolean> verify(String id, String code, boolean ignoreCase);

    /**
     * 作废验证码（校验通过后调用，一次性）。
     */
    Mono<Void> invalidate(String id);
}
