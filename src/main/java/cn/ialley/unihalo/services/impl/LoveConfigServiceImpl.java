package cn.ialley.unihalo.services.impl;

import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.LoveConfig;
import cn.ialley.unihalo.services.LoveConfigService;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * 恋爱配置服务实现（单例）
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class LoveConfigServiceImpl implements LoveConfigService {

    private final ReactiveExtensionClient client;

    @Override
    public Mono<LoveConfig> get() {
        return client.fetch(LoveConfig.class, Constants.LOVE_CONFIG_SINGLETON_NAME)
                .switchIfEmpty(Mono.fromSupplier(this::defaultLoveConfig));
    }

    @Override
    public Mono<LoveConfig> save(LoveConfig loveConfig) {
        return client.fetch(LoveConfig.class, Constants.LOVE_CONFIG_SINGLETON_NAME)
                .flatMap(existing -> {
                    existing.setSpec(loveConfig.getSpec());
                    return client.update(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    Metadata metadata = new Metadata();
                    metadata.setName(Constants.LOVE_CONFIG_SINGLETON_NAME);
                    metadata.setCreationTimestamp(Instant.now());
                    loveConfig.setMetadata(metadata);
                    return client.create(loveConfig);
                }));
    }

    /**
     * 默认配置结构（不落库）：仅纪念日 + 恋人信息（决策 D4，图片与模块开关
     * 仍由 setting.yaml 的 loveConfig 组配置）。
     */
    private LoveConfig defaultLoveConfig() {
        LoveConfig config = new LoveConfig();
        Metadata metadata = new Metadata();
        metadata.setName(Constants.LOVE_CONFIG_SINGLETON_NAME);
        config.setMetadata(metadata);

        LoveConfig.LoveConfigSpec spec = new LoveConfig.LoveConfigSpec();
        spec.setLoveDateTitle("这是我们一起走过的");
        spec.setLoveDate("");
        spec.setLoveInfo(new LoveConfig.LoveInfo());

        config.setSpec(spec);
        return config;
    }
}
