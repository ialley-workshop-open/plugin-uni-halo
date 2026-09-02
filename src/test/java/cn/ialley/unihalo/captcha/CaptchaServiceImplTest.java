package cn.ialley.unihalo.captcha;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;
import tools.jackson.databind.node.JsonNodeFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证码业务服务单元测试：开关关闭放行 / 参数缺失 / 错误验证码 / 成功后作废 / 生成映射。
 *
 * @author 小莫唐尼
 */
@ExtendWith(MockitoExtension.class)
class CaptchaServiceImplTest {

    @Mock
    CaptchaManager captchaManager;

    @Mock
    ReactiveSettingFetcher settingFetcher;

    @InjectMocks
    CaptchaServiceImpl service;

    // ===== 开关关闭：直接放行 =====

    @Test
    void requireValidPassesWhenDisabled() {
        when(settingFetcher.getSettingValue(anyString()))
                .thenReturn(Mono.just(settings(false)));

        assertThatCode(() -> service.requireValid(mock(ServerRequest.class)).block())
                .doesNotThrowAnyException();
    }

    // ===== 参数缺失：校验失败 =====

    @Test
    void requireValidFailsWhenParamsMissing() {
        when(settingFetcher.getSettingValue(anyString()))
                .thenReturn(Mono.just(settings(true)));
        var request = mock(ServerRequest.class);
        when(request.queryParam("captchaId")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requireValid(request).block())
                .isInstanceOf(CaptchaValidationException.class);
    }

    // ===== 错误验证码：校验失败 =====

    @Test
    void requireValidFailsWhenWrongCode() {
        when(settingFetcher.getSettingValue(anyString()))
                .thenReturn(Mono.just(settings(true)));
        var request = mock(ServerRequest.class);
        when(request.queryParam("captchaId")).thenReturn(Optional.of("id1"));
        when(request.queryParam("captchaCode")).thenReturn(Optional.of("wrong"));
        when(captchaManager.verify("id1", "wrong", true)).thenReturn(Mono.just(false));

        assertThatThrownBy(() -> service.requireValid(request).block())
                .isInstanceOf(CaptchaValidationException.class);
    }

    // ===== 校验成功：放行并作废（一次性） =====

    @Test
    void requireValidPassesWhenCorrectAndInvalidates() {
        when(settingFetcher.getSettingValue(anyString()))
                .thenReturn(Mono.just(settings(true)));
        var request = mock(ServerRequest.class);
        when(request.queryParam("captchaId")).thenReturn(Optional.of("id1"));
        when(request.queryParam("captchaCode")).thenReturn(Optional.of("code1"));
        when(captchaManager.verify("id1", "code1", true)).thenReturn(Mono.just(true));
        when(captchaManager.invalidate("id1")).thenReturn(Mono.empty());

        assertThatCode(() -> service.requireValid(request).block())
                .doesNotThrowAnyException();

        verify(captchaManager).invalidate("id1");
    }

    // ===== 校验失败时不作废 =====

    @Test
    void requireValidFailureDoesNotInvalidate() {
        when(settingFetcher.getSettingValue(anyString()))
                .thenReturn(Mono.just(settings(true)));
        var request = mock(ServerRequest.class);
        when(request.queryParam("captchaId")).thenReturn(Optional.of("id1"));
        when(request.queryParam("captchaCode")).thenReturn(Optional.of("bad"));
        when(captchaManager.verify("id1", "bad", true)).thenReturn(Mono.just(false));

        assertThatThrownBy(() -> service.requireValid(request).block())
                .isInstanceOf(CaptchaValidationException.class);

        verify(captchaManager, never()).invalidate(anyString());
    }

    // ===== 生成：配置映射到 CaptchaManager =====

    @Test
    void generateMapsConfiguredTypeAndLength() {
        when(settingFetcher.getSettingValue(anyString()))
                .thenReturn(Mono.just(settings(true)));
        when(captchaManager.generate(eq(CaptchaType.ALPHANUMERIC), eq(4), eq(10)))
                .thenReturn(Mono.just(new CaptchaManager.Captcha(
                        "id1", "code1", "data:image/png;base64,xxx")));

        var vo = service.generate().block();

        assertThat(vo).isNotNull();
        assertThat(vo.id()).isEqualTo("id1");
        assertThat(vo.imageBase64()).isEqualTo("data:image/png;base64,xxx");
    }

    // ===== 工具 =====

    private static tools.jackson.databind.JsonNode settings(boolean enabled) {
        var node = JsonNodeFactory.instance.objectNode();
        node.put("enabled", enabled);
        return node;
    }
}
