package cn.ialley.unihalo.services.impl;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import cn.ialley.unihalo.scheme.AppVersion;
import cn.ialley.unihalo.services.AppVersionService;
import cn.ialley.unihalo.utils.VersionComparator;
import cn.ialley.unihalo.vo.UpgradeResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

import static run.halo.app.extension.index.query.Queries.and;
import static run.halo.app.extension.index.query.Queries.equal;

/**
 * 应用版本服务实现
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class AppVersionServiceImpl implements AppVersionService {

    public static final String TYPE_NATIVE_APP = "native_app";
    public static final String TYPE_WGT = "wgt";

    public static final String PLATFORM_ANDROID = "Android";
    public static final String PLATFORM_IOS = "iOS";
    public static final String PLATFORM_HARMONY = "Harmony";

    private final ReactiveExtensionClient client;

    @Override
    public Mono<AppVersion> getByName(String name) {
        return client.fetch(AppVersion.class, name);
    }

    @Override
    public Flux<AppVersion> listAll(ListOptions options) {
        return client.listAll(AppVersion.class, options,
                Sort.by(Sort.Direction.DESC, "metadata.creationTimestamp"));
    }

    @Override
    public Mono<AppVersion> create(AppVersion appVersion) {
        return Mono.defer(() -> {
            Metadata metadata = new Metadata();
            metadata.setName(generateName());
            metadata.setCreationTimestamp(Instant.now());
            appVersion.setMetadata(metadata);
            return client.create(appVersion);
        }).flatMap(saved -> offlinePreviousStableIfNeeded(saved));
    }

    @Override
    public Mono<AppVersion> update(AppVersion appVersion) {
        return client.update(appVersion)
                .flatMap(saved -> offlinePreviousStableIfNeeded(saved));
    }

    @Override
    public Mono<Void> delete(String name) {
        return client.fetch(AppVersion.class, name)
                .flatMap(client::delete)
                .then();
    }

    @Override
    public Mono<UpgradeResult> checkVersion(String appid, String appVersion, String wgtVersion,
            String platform, boolean isUniappX) {
        if (isBlank(appid) || isBlank(appVersion) || isBlank(wgtVersion)) {
            return Mono.just(UpgradeResult.error(-102, "请检查传参是否填写正确"));
        }

        String targetPlatform = isBlank(platform) ? PLATFORM_ANDROID : platform;
        var listOptions = ListOptions.builder()
                .fieldQuery(and(
                        equal("spec.appid", appid),
                        equal("spec.stablePublish", true)))
                .build();

        // 按创建时间倒序，取各类型最新的一条
        return client.listAll(AppVersion.class, listOptions,
                        Sort.by(Sort.Direction.DESC, "metadata.creationTimestamp"))
                .filter(v -> v.getSpec().getPlatform() != null
                        && v.getSpec().getPlatform().contains(targetPlatform))
                .collectList()
                .flatMap(records -> {
                    if (records.isEmpty()) {
                        return Mono.just(UpgradeResult.error(-101, "暂无更新或检查appid是否填写正确"));
                    }
                    return Mono.just(determineUpgrade(records, appVersion, wgtVersion,
                            targetPlatform, isUniappX));
                });
    }

    /**
     * 核心升级判定，对齐 uni-upgrade-center checkVersion 云函数：
     * 1. 取 native_app 与 wgt 各一条（均为 stable_publish 且平台匹配，按创建时间倒序最新）；
     * 2. 选版本号最大的包（版本相同取 wgt；uni-app x 的 Android 无 wgt 升级）；
     * 3. 库中 version 须同时大于 appVersion 与 wgtVersion 才判定有更新；
     * 4. wgt 可用且 min_uni_version ≤ appVersion → 101（wgt 更新）；
     *    否则 native_app 版本大于 appVersion → 102（整包更新）；否则无更新。
     */
    private UpgradeResult determineUpgrade(List<AppVersion> records, String appVersion,
            String wgtVersion, String platform, boolean isUniappX) {
        AppVersion appPackage = findFirstByType(records, TYPE_NATIVE_APP);
        AppVersion wgtPackage = findFirstByType(records, TYPE_WGT);
        boolean hasAppPackage = appPackage != null;
        boolean hasWgtPackage = wgtPackage != null;

        AppVersion stablePublishDb = pickStable(appPackage, wgtPackage,
                hasAppPackage, hasWgtPackage, platform, isUniappX);

        if (stablePublishDb == null) {
            return UpgradeResult.error(0, "当前版本已经是最新的，不需要更新");
        }

        String dbVersion = stablePublishDb.getSpec().getVersion();
        String minUniVersion = stablePublishDb.getSpec().getMinUniVersion();
        boolean appUpdate = VersionComparator.compare(dbVersion, appVersion) == 1;
        boolean wgtUpdate = VersionComparator.compare(dbVersion, wgtVersion) == 1;

        if (appUpdate && wgtUpdate) {
            if (!isBlank(minUniVersion)
                    && VersionComparator.compare(minUniVersion, appVersion) <= 0) {
                return UpgradeResult.of(101, "wgt更新", stablePublishDb);
            }
            if (hasAppPackage
                    && VersionComparator.compare(appPackage.getSpec().getVersion(), appVersion) == 1) {
                return UpgradeResult.of(102, "整包更新", appPackage);
            }
        }

        return UpgradeResult.error(0, "当前版本已经是最新的，不需要更新");
    }

    private AppVersion pickStable(AppVersion appPackage, AppVersion wgtPackage,
            boolean hasAppPackage, boolean hasWgtPackage, String platform, boolean isUniappX) {
        // uni-app x 项目安卓端没有 wgt 升级
        if (isUniappX && PLATFORM_ANDROID.equals(platform)) {
            return hasAppPackage ? appPackage : null;
        }
        if (hasAppPackage && hasWgtPackage) {
            if (VersionComparator.compare(wgtPackage.getSpec().getVersion(),
                    appPackage.getSpec().getVersion()) >= 0) {
                return wgtPackage;
            }
            return appPackage;
        }
        if (hasAppPackage) {
            return appPackage;
        }
        return wgtPackage;
    }

    private AppVersion findFirstByType(List<AppVersion> records, String type) {
        return records.stream()
                .filter(v -> type.equals(v.getSpec().getType()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 发布新的稳定版时，自动下线同 appid、同平台交集、同 type 的其他稳定版，
     * 保证同一应用同一平台同一类型同时只有一个上线版本。
     */
    private Mono<AppVersion> offlinePreviousStableIfNeeded(AppVersion current) {
        if (!Boolean.TRUE.equals(current.getSpec().getStablePublish())) {
            return Mono.just(current);
        }
        String appid = current.getSpec().getAppid();
        String type = current.getSpec().getType();
        if (isBlank(appid) || isBlank(type)) {
            return Mono.just(current);
        }

        var listOptions = ListOptions.builder()
                .fieldQuery(and(
                        equal("spec.appid", appid),
                        equal("spec.type", type),
                        equal("spec.stablePublish", true)))
                .build();

        return client.listAll(AppVersion.class, listOptions, Sort.unsorted())
                .filter(v -> !v.getMetadata().getName().equals(current.getMetadata().getName()))
                .filter(v -> hasPlatformOverlap(v, current))
                .doOnNext(v -> v.getSpec().setStablePublish(false))
                .flatMap(client::update)
                .then(Mono.just(current));
    }

    private boolean hasPlatformOverlap(AppVersion a, AppVersion b) {
        if (a.getSpec().getPlatform() == null || b.getSpec().getPlatform() == null) {
            return false;
        }
        return a.getSpec().getPlatform().stream()
                .anyMatch(b.getSpec().getPlatform()::contains);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String generateName() {
        return "appversion-" + System.currentTimeMillis() + "-"
                + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000));
    }
}