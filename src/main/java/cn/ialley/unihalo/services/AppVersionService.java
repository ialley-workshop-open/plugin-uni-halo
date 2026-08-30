package cn.ialley.unihalo.services;

import cn.ialley.unihalo.scheme.AppVersion;
import cn.ialley.unihalo.vo.UpgradeResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;

/**
 * 应用版本服务（应用升级）
 *
 * @author 小莫唐尼
 */
public interface AppVersionService {

    Mono<AppVersion> getByName(String name);

    Flux<AppVersion> listAll(ListOptions options);

    Mono<AppVersion> create(AppVersion appVersion);

    Mono<AppVersion> update(AppVersion appVersion);

    Mono<Void> delete(String name);

    /**
     * 升级检测（公开接口，对齐 uni-upgrade-center 云函数 checkVersion 逻辑）。
     *
     * @param appid      应用标识
     * @param appVersion 当前原生 App 版本号
     * @param wgtVersion 当前 wgt 资源版本号
     * @param platform   目标平台：Android / iOS / Harmony
     * @param isUniappX  是否 uni-app x（Android 无 wgt 升级）
     * @return 升级检测结果
     */
    Mono<UpgradeResult> checkVersion(String appid, String appVersion, String wgtVersion,
            String platform, boolean isUniappX);
}