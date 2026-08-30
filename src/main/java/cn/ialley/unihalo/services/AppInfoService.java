package cn.ialley.unihalo.services;

import cn.ialley.unihalo.scheme.AppInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 应用信息服务（应用管理）
 *
 * @author 小莫唐尼
 */
public interface AppInfoService {

    Mono<AppInfo> getByName(String name);

    Mono<AppInfo> fetchByAppid(String appid);

    Flux<AppInfo> listAll();

    Mono<AppInfo> create(AppInfo appInfo);

    Mono<AppInfo> update(AppInfo appInfo);

    Mono<Void> delete(String name);
}