package cn.ialley.unihalo.services;

import cn.ialley.unihalo.scheme.GeneralConfig;
import reactor.core.publisher.Mono;

/**
 * 通用配置服务（单例，metadata.name 固定为 general-config）。
 *
 * <p>承载原 setting.yaml 迁出的站点内容与外观配置（authorConfig / pageConfig /
 * basicConfig 内容部分 / imagesConfig）。GET 不存在时返回「默认值 + 历史
 * ConfigMap 旧值合并」结构（不落库），保证升级后配置页回填旧值、小程序端
 * getConfigs 输出不回退。</p>
 *
 * @author 小莫唐尼
 */
public interface GeneralConfigService {

    /**
     * 读取通用配置单例；不存在时返回默认结构（与历史 ConfigMap 旧值合并）。
     */
    Mono<GeneralConfig> get();

    /**
     * 保存通用配置单例（不存在则创建；写入前与默认值/历史值做非空合并，防丢字段）。
     */
    Mono<GeneralConfig> save(GeneralConfig config);
}
