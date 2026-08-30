package cn.ialley.unihalo.services.impl;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import cn.ialley.unihalo.scheme.AppInfo;
import cn.ialley.unihalo.scheme.AppVersion;
import cn.ialley.unihalo.services.AppInfoService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

import static run.halo.app.extension.index.query.Queries.and;
import static run.halo.app.extension.index.query.Queries.equal;

/**
 * 应用信息服务实现
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class AppInfoServiceImpl implements AppInfoService {

    private final ReactiveExtensionClient client;

    @Override
    public Mono<AppInfo> getByName(String name) {
        return client.fetch(AppInfo.class, name);
    }

    @Override
    public Mono<AppInfo> fetchByAppid(String appid) {
        var listOptions = ListOptions.builder()
                .fieldQuery(equal("spec.appid", appid))
                .build();
        return client.listAll(AppInfo.class, listOptions, Sort.unsorted()).next();
    }

    @Override
    public Flux<AppInfo> listAll() {
        return client.listAll(AppInfo.class, new ListOptions(),
                Sort.by(Sort.Direction.DESC, "metadata.creationTimestamp"));
    }

    @Override
    public Mono<AppInfo> create(AppInfo appInfo) {
        if (appInfo.getSpec() == null || appInfo.getSpec().getAppid() == null
                || appInfo.getSpec().getAppid().isBlank()) {
            return Mono.error(new IllegalArgumentException("appid 不能为空"));
        }
        return fetchByAppid(appInfo.getSpec().getAppid())
                .flatMap(existing -> Mono.<AppInfo>error(
                        new IllegalArgumentException("AppID 已存在：" + existing.getSpec().getAppid())))
                .switchIfEmpty(Mono.defer(() -> {
                    Metadata metadata = new Metadata();
                    metadata.setName(generateName());
                    metadata.setCreationTimestamp(Instant.now());
                    appInfo.setMetadata(metadata);
                    return client.create(appInfo);
                }));
    }

    @Override
    public Mono<AppInfo> update(AppInfo appInfo) {
        String name = appInfo.getMetadata().getName();
        String appid = appInfo.getSpec().getAppid();
        return fetchByAppid(appid)
                .filter(existing -> !existing.getMetadata().getName().equals(name))
                .flatMap(existing -> Mono.<AppInfo>error(
                        new IllegalArgumentException("AppID 已存在：" + existing.getSpec().getAppid())))
                .switchIfEmpty(Mono.defer(() -> client.update(appInfo)));
    }

    @Override
    public Mono<Void> delete(String name) {
        return client.fetch(AppInfo.class, name)
                .flatMap(appInfo -> {
                    String appid = appInfo.getSpec() != null
                            ? appInfo.getSpec().getAppid()
                            : null;
                    if (appid == null || appid.isBlank()) {
                        return Mono.<AppInfo>error(
                                new IllegalArgumentException("应用信息不完整，无法删除"));
                    }
                    var listOptions = ListOptions.builder()
                            .fieldQuery(and(
                                    equal("spec.appid", appid),
                                    equal("spec.stablePublish", true)))
                            .build();
                    return client.listAll(AppVersion.class, listOptions, Sort.unsorted())
                            .next()
                            .flatMap(published -> Mono.<AppInfo>error(
                                    new IllegalArgumentException("该应用已有发布的版本，不能删除")))
                            .switchIfEmpty(Mono.defer(() -> client.delete(appInfo)));
                })
                .then();
    }

    private static String generateName() {
        return "appinfo-" + System.currentTimeMillis() + "-"
                + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000));
    }
}