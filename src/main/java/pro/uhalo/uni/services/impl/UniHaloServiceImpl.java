package pro.uhalo.uni.services.impl;

import pro.uhalo.uni.services.UniHaloService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.ReactiveSettingFetcher;
import java.util.Map;

/**
 * 服务实现
 *
 * @author 小莫唐尼
 */
@Component
@RequiredArgsConstructor
public class UniHaloServiceImpl implements UniHaloService {

    private final ReactiveExtensionClient client;
    private final ReactiveSettingFetcher settingFetcher;

    /**
     * 获取移动端的所有配置
     *
     * @return 配置
     */
    @Override
    public Mono<Map<String, JsonNode>> getAppConfigs() {
        return this.settingFetcher.getValues();
    }

    /*
     * 根据分组名称获取配置
     *
     * @param groupName 分组名称
     * @return 配置
     */
    @Override
    public Mono<JsonNode> getAppConfigsByGroupName(String groupName) {
        return this.settingFetcher.get(groupName);
    }
}