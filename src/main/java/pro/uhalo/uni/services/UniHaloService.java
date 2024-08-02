package pro.uhalo.uni.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
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

    /**
     * 获取二维码图片
     *
     * @param serverRequest: 
     * @return: reactor.core.publisher.Mono<org.springframework.web.reactive.function.server.ServerResponse>
     * @author: lywq
     * @date: 2024/07/31 22:43
     **/
    Mono<ServerResponse> getQRCodeImg(ServerRequest serverRequest);

    /**
     * 获取二维码信息
     *
     * @param serverRequest:
     * @return: reactor.core.publisher.Mono<org.springframework.web.reactive.function.server.ServerResponse>
     * @author: lywq
     * @date: 2024/08/01 16:27
     **/
    Mono<ServerResponse> getQRCodeInfo(ServerRequest serverRequest);
}
