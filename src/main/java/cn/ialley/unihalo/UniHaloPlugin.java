package cn.ialley.unihalo;

import cn.ialley.unihalo.scheme.AppInfo;
import cn.ialley.unihalo.scheme.AppVersion;
import cn.ialley.unihalo.scheme.QRCodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexSpecs;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
 * <p>Plugin main class to manage the lifecycle of the plugin.</p>
 * <p>This class must be public and have a public constructor.</p>
 * <p>Only one main class extending {@link BasePlugin} is allowed per plugin.</p>
 *
 * @author 小莫唐尼
 * @since 1.0.0
 */
@Slf4j
@Component
public class UniHaloPlugin extends BasePlugin {

    private final SchemeManager schemeManager;

    public UniHaloPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {

        schemeManager.register(QRCodeInfo.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<QRCodeInfo, String>single("key", String.class)
                    .indexFunc(QRCodeInfo::getKey));
            indexSpecs.add(IndexSpecs.<QRCodeInfo, String>single("postId", String.class)
                    .indexFunc(QRCodeInfo::getPostId));
        });

        schemeManager.register(AppInfo.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<AppInfo, String>single("spec.appid", String.class)
                    .indexFunc(appInfo -> appInfo.getSpec().getAppid()));
        });

        schemeManager.register(AppVersion.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<AppVersion, String>single("spec.appid", String.class)
                    .indexFunc(appVersion -> appVersion.getSpec().getAppid()));
            indexSpecs.add(IndexSpecs.<AppVersion, String>single("spec.type", String.class)
                    .indexFunc(appVersion -> appVersion.getSpec().getType()));
            indexSpecs.add(IndexSpecs.<AppVersion, Boolean>single("spec.stablePublish", Boolean.class)
                    .indexFunc(appVersion -> appVersion.getSpec().getStablePublish()));
            indexSpecs.add(IndexSpecs.<AppVersion, String>single("spec.title", String.class)
                    .indexFunc(appVersion -> appVersion.getSpec().getTitle()));
            indexSpecs.add(IndexSpecs.<AppVersion, String>single("spec.version", String.class)
                    .indexFunc(appVersion -> appVersion.getSpec().getVersion()));
        });

        log.info("【UniHalo】插件启动成功！");
    }

    @Override
    public void stop() {

        schemeManager.unregister(schemeManager.get(QRCodeInfo.class));
        schemeManager.unregister(schemeManager.get(AppInfo.class));
        schemeManager.unregister(schemeManager.get(AppVersion.class));

        log.info("【UniHalo】插件停止！");
    }
}