package pro.uhalo.uni.service;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;
import java.util.Map;

/**
 * UniHalo 基础服务接口
 *
 * @author 小莫唐尼
 * @since 1.0.0
 */
public interface UniHaloService {
    /**
     * 获取移动端的所有配置
     *
     * @return 配置
     */
    Mono<Map<String, JsonNode>> getAppConfigs();

    /*
     * 根据分组名称获取移动端的配置
     *
     * @param groupName 分组名称
     * @return 配置
     */
    Mono<JsonNode> getAppConfigsByGroupName(String groupName);
}
