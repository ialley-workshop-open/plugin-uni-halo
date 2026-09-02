package cn.ialley.unihalo.captcha;

/**
 * 验证码对外视图（公开接口返回给前端）。
 *
 * @param id          验证码 id（提交时随请求带回，一次性）
 * @param imageBase64 验证码图片（PNG base64 data URI）
 * @author 小莫唐尼
 */
public record CaptchaVo(String id, String imageBase64) {
}
