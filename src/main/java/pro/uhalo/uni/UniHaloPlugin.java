package pro.uhalo.uni;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.uhalo.uni.scheme.QRCodeInfo;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexSpec;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

import static run.halo.app.extension.index.IndexAttributeFactory.simpleAttribute;

/**
 * @author 小莫唐尼
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
            indexSpecs.add(new IndexSpec()
                    .setName("key")
                    .setIndexFunc(
                            simpleAttribute(QRCodeInfo.class,
                                    QRCodeInfo::getKey)));
            indexSpecs.add(new IndexSpec()
                    .setName("postId")
                    .setIndexFunc(
                            simpleAttribute(QRCodeInfo.class,
                                    QRCodeInfo::getPostId)));
        });

        log.info("【UniHalo】插件启动成功！");
    }

    @Override
    public void stop() {

        schemeManager.unregister(schemeManager.get(QRCodeInfo.class));

        log.info("【UniHalo】插件停止！");
    }
}
