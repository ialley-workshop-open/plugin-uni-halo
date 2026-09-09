package cn.ialley.unihalo.services.impl;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.GeneralConfig;
import cn.ialley.unihalo.scheme.GeneralConfig.Love;
import cn.ialley.unihalo.scheme.GeneralConfig.Maintenance;
import cn.ialley.unihalo.scheme.GeneralConfig.ModuleSwitch;
import cn.ialley.unihalo.scheme.GeneralConfig.Spec;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.ReactiveSettingFetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通用配置服务单元测试：GET 默认值（含维护分区默认）、保存时间窗口校验、
 * 新建时默认值合并兜底。
 *
 * @author 小莫唐尼
 */
@ExtendWith(MockitoExtension.class)
class GeneralConfigServiceImplTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    ReactiveSettingFetcher settingFetcher;

    @InjectMocks
    GeneralConfigServiceImpl service;

    private static final String SINGLETON = Constants.GENERAL_CONFIG_SINGLETON_NAME;

    @Test
    void getWhenNotExistsReturnsDefaultIncludingMaintenance() {
        when(client.fetch(eq(GeneralConfig.class), eq(SINGLETON))).thenReturn(Mono.empty());
        when(settingFetcher.getSettingValues()).thenReturn(Mono.just(Map.of()));

        var config = service.get().block();

        assertThat(config).isNotNull();
        assertThat(config.getSpec()).isNotNull();
        // 维护分区默认：关闭 + 标题默认「站点维护中」，说明/详情留空
        assertThat(config.getSpec().getMaintenance()).isNotNull();
        assertThat(config.getSpec().getMaintenance().getEnabled()).isFalse();
        assertThat(config.getSpec().getMaintenance().getTitle()).isEqualTo("站点维护中");
        assertThat(config.getSpec().getMaintenance().getNotice()).isEmpty();
        assertThat(config.getSpec().getMaintenance().getDescription()).isEmpty();
    }

    @Test
    void saveWithReversedTimesRejects() {
        // startTime >= endTime → 400（IllegalArgumentException，由 endpoint 映射）
        GeneralConfig config = new GeneralConfig();
        Spec spec = new Spec();
        Maintenance maintenance = new Maintenance();
        maintenance.setEnabled(true);
        maintenance.setStartTime("2026-09-05T00:00:00Z");
        maintenance.setEndTime("2026-09-04T00:00:00Z");
        spec.setMaintenance(maintenance);
        config.setSpec(spec);

        when(client.fetch(eq(GeneralConfig.class), eq(SINGLETON))).thenReturn(Mono.empty());
        when(settingFetcher.getSettingValues()).thenReturn(Mono.just(Map.of()));

        assertThatThrownBy(() -> service.save(config).block())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("维护结束时间必须晚于开始时间");
        // 校验在写入前，未触发 create/update
        verify(client, never()).create(any());
        verify(client, never()).update(any());
    }

    @Test
    void saveWhenNotExistsCreatesWithDefaultsMerged() {
        // 请求体仅置 enabled=true（无 title/窗口）→ 合并默认值后落库
        GeneralConfig config = new GeneralConfig();
        Spec spec = new Spec();
        Maintenance maintenance = new Maintenance();
        maintenance.setEnabled(true);
        spec.setMaintenance(maintenance);
        config.setSpec(spec);

        when(client.fetch(eq(GeneralConfig.class), eq(SINGLETON))).thenReturn(Mono.empty());
        when(settingFetcher.getSettingValues()).thenReturn(Mono.just(Map.of()));
        when(client.create(any(GeneralConfig.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var saved = service.save(config).block();

        assertThat(saved).isNotNull();
        assertThat(saved.getMetadata().getName()).isEqualTo(SINGLETON);
        assertThat(saved.getSpec().getMaintenance().getEnabled()).isTrue();
        // 未提交字段由默认结构兜底（标题默认、窗口为空）
        assertThat(saved.getSpec().getMaintenance().getTitle()).isEqualTo("站点维护中");
        assertThat(saved.getSpec().getMaintenance().getStartTime()).isNull();
        assertThat(saved.getSpec().getMaintenance().getEndTime()).isNull();
    }

    // ===== 恋爱模块入口密码（2026-09-08，BCrypt 语义与恋爱相册一致）=====

    private static final String EXISTING_HASH =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5YjsR1yOVB4C2mz0XkZ4p7H6VzV4K";

    private static GeneralConfig configWithExistingHash() {
        GeneralConfig config = new GeneralConfig();
        Metadata metadata = new Metadata();
        metadata.setName(Constants.GENERAL_CONFIG_SINGLETON_NAME);
        config.setMetadata(metadata);
        Spec spec = new Spec();
        Love love = new Love();
        ModuleSwitch ourStory = new ModuleSwitch();
        ourStory.setEnabled(true);
        ourStory.setPasswordHash(EXISTING_HASH);
        love.setOurStory(ourStory);
        spec.setLove(love);
        config.setSpec(spec);
        return config;
    }

    @Test
    void saveWithNewPasswordEncodesHashAndMasksResponse() {
        // 首次创建：提交明文密码 → 落库 BCrypt 哈希（写字段消费置空）；响应脱敏不回显哈希
        GeneralConfig body = new GeneralConfig();
        Spec spec = new Spec();
        Love love = new Love();
        ModuleSwitch ourStory = new ModuleSwitch();
        ourStory.setEnabled(true);
        ourStory.setPassword("123456");
        love.setOurStory(ourStory);
        spec.setLove(love);
        body.setSpec(spec);

        when(client.fetch(eq(GeneralConfig.class), eq(SINGLETON))).thenReturn(Mono.empty());
        when(settingFetcher.getSettingValues()).thenReturn(Mono.just(Map.of()));
        when(client.create(any(GeneralConfig.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var saved = service.save(body).block();

        // 落库入参：明文 → BCrypt 哈希（可 matches 校验），写请求字段置空
        ArgumentCaptor<GeneralConfig> captor = ArgumentCaptor.forClass(GeneralConfig.class);
        verify(client).create(captor.capture());
        var persisted = captor.getValue().getSpec().getLove().getOurStory();
        assertThat(persisted.getPasswordHash()).isNotBlank();
        assertThat(persisted.getPasswordHash()).doesNotContain("123456");
        assertThat(new BCryptPasswordEncoder().matches("123456", persisted.getPasswordHash()))
            .isTrue();
        assertThat(persisted.getPasswordEnabled()).isTrue();
        assertThat(persisted.getPassword()).isNull();
        assertThat(persisted.getPasswordRemoved()).isNull();
        // 响应脱敏：哈希不回显，passwordEnabled 保留
        assertThat(saved).isNotNull();
        var savedSwitch = saved.getSpec().getLove().getOurStory();
        assertThat(savedSwitch.getPasswordHash()).isNull();
        assertThat(savedSwitch.getPassword()).isNull();
        assertThat(savedSwitch.getPasswordRemoved()).isNull();
        assertThat(savedSwitch.getPasswordEnabled()).isTrue();
    }

    @Test
    void saveWithoutPasswordKeepsExistingHash() {
        // 已有哈希：请求体未提供 password/passwordRemoved → 保持原哈希不变（响应脱敏）
        GeneralConfig body = new GeneralConfig();
        Spec spec = new Spec();
        Love love = new Love();
        ModuleSwitch ourStory = new ModuleSwitch();
        ourStory.setEnabled(true);
        love.setOurStory(ourStory);
        spec.setLove(love);
        body.setSpec(spec);

        when(client.fetch(eq(GeneralConfig.class), eq(SINGLETON)))
            .thenReturn(Mono.just(configWithExistingHash()));
        when(settingFetcher.getSettingValues()).thenReturn(Mono.just(Map.of()));
        when(client.update(any(GeneralConfig.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var saved = service.save(body).block();

        ArgumentCaptor<GeneralConfig> captor = ArgumentCaptor.forClass(GeneralConfig.class);
        verify(client).update(captor.capture());
        var persisted = captor.getValue().getSpec().getLove().getOurStory();
        assertThat(persisted.getPasswordHash()).isEqualTo(EXISTING_HASH);
        assertThat(persisted.getPasswordEnabled()).isTrue();
        assertThat(saved).isNotNull();
        assertThat(saved.getSpec().getLove().getOurStory().getPasswordHash()).isNull();
        assertThat(saved.getSpec().getLove().getOurStory().getPasswordEnabled()).isTrue();
    }

    @Test
    void saveWithPasswordRemovedClearsPassword() {
        // passwordRemoved=true → 清除哈希并关闭验证
        GeneralConfig body = new GeneralConfig();
        Spec spec = new Spec();
        Love love = new Love();
        ModuleSwitch ourStory = new ModuleSwitch();
        ourStory.setEnabled(true);
        ourStory.setPasswordRemoved(true);
        love.setOurStory(ourStory);
        spec.setLove(love);
        body.setSpec(spec);

        when(client.fetch(eq(GeneralConfig.class), eq(SINGLETON)))
            .thenReturn(Mono.just(configWithExistingHash()));
        when(settingFetcher.getSettingValues()).thenReturn(Mono.just(Map.of()));
        when(client.update(any(GeneralConfig.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var saved = service.save(body).block();

        ArgumentCaptor<GeneralConfig> captor = ArgumentCaptor.forClass(GeneralConfig.class);
        verify(client).update(captor.capture());
        var persisted = captor.getValue().getSpec().getLove().getOurStory();
        assertThat(persisted.getPasswordHash()).isNull();
        assertThat(persisted.getPasswordEnabled()).isFalse();
        assertThat(saved).isNotNull();
        assertThat(saved.getSpec().getLove().getOurStory().getPasswordHash()).isNull();
        assertThat(saved.getSpec().getLove().getOurStory().getPasswordEnabled()).isFalse();
    }

    @Test
    void getMasksLovePasswords() {
        // 控制台 GET：哈希/写字段不回显，passwordEnabled 按哈希派生
        when(client.fetch(eq(GeneralConfig.class), eq(SINGLETON)))
            .thenReturn(Mono.just(configWithExistingHash()));

        var config = service.get().block();

        assertThat(config).isNotNull();
        var ourStory = config.getSpec().getLove().getOurStory();
        assertThat(ourStory.getPasswordHash()).isNull();
        assertThat(ourStory.getPassword()).isNull();
        assertThat(ourStory.getPasswordRemoved()).isNull();
        assertThat(ourStory.getPasswordEnabled()).isTrue();
    }
}
