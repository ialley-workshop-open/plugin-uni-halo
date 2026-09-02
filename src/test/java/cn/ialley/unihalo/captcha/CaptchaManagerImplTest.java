package cn.ialley.unihalo.captcha;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证码管理单元测试：生成 / 校验（忽略大小写）/ 一次性 / 过期 / 不存在。
 *
 * @author 小莫唐尼
 */
class CaptchaManagerImplTest {

    private final CaptchaManagerImpl manager = new CaptchaManagerImpl();

    @Test
    void generateReturnsPngDataUri() {
        var captcha = manager.generate(CaptchaType.ALPHANUMERIC, 4, 10).block();

        assertThat(captcha).isNotNull();
        assertThat(captcha.id()).isNotBlank();
        assertThat(captcha.code()).hasSize(4);
        assertThat(captcha.imageBase64()).startsWith("data:image/png;base64,");
    }

    @Test
    void generateMathCaptchaCodeIsNumeric() {
        var captcha = manager.generate(CaptchaType.ARITHMETIC, 4, 10).block();

        assertThat(captcha).isNotNull();
        assertThat(captcha.code()).matches("\\d+");
    }

    @Test
    void verifyCorrectCodePassesIgnoreCase() {
        var captcha = manager.generate(CaptchaType.ALPHANUMERIC, 4, 10).block();

        assertThat(manager.verify(captcha.id(), captcha.code(), true).block()).isTrue();
        assertThat(manager.verify(captcha.id(), captcha.code().toUpperCase(), true).block()).isTrue();
        assertThat(manager.verify(captcha.id(), captcha.code().toLowerCase(), true).block()).isTrue();
    }

    @Test
    void verifyWrongCodeFails() {
        var captcha = manager.generate(CaptchaType.ALPHANUMERIC, 4, 10).block();

        assertThat(manager.verify(captcha.id(), "zzzz", true).block()).isFalse();
    }

    @Test
    void verifyUnknownIdFails() {
        assertThat(manager.verify("not-exist-id", "abcd", true).block()).isFalse();
    }

    @Test
    void invalidateMakesCaptchaOneTime() {
        var captcha = manager.generate(CaptchaType.ALPHANUMERIC, 4, 10).block();

        manager.invalidate(captcha.id()).block();

        assertThat(manager.verify(captcha.id(), captcha.code(), true).block()).isFalse();
    }

    @Test
    void expiredCaptchaFails() {
        // 注入可控时钟：生成后推进 61 秒，验证过期失效
        var clock = new AtomicLong(System.currentTimeMillis());
        var manager = new CaptchaManagerImpl(clock::get);
        var captcha = manager.generate(CaptchaType.ALPHANUMERIC, 4, 10).block();

        clock.addAndGet(61_000);

        assertThat(manager.verify(captcha.id(), captcha.code(), true).block()).isFalse();
    }
}
