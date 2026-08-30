package cn.ialley.unihalo.services;

import cn.ialley.unihalo.scheme.LoveConfig;
import reactor.core.publisher.Mono;

/**
 * 恋爱配置服务（单例）
 *
 * @author 小莫唐尼
 */
public interface LoveConfigService {

    /**
     * 读取恋爱配置单例；不存在时返回默认结构（不落库）。
     */
    Mono<LoveConfig> get();

    /**
     * 保存恋爱配置单例（不存在则创建，metadata.name 固定为 love-config）。
     */
    Mono<LoveConfig> save(LoveConfig loveConfig);
}
