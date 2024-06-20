package pro.uhalo.uni;

import pro.uhalo.uni.extension.AuthApp;
import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
 * @author 小莫唐尼
 */
@Component
public class UniHaloPlugin extends BasePlugin {

    private final SchemeManager schemeManager;

    public UniHaloPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {

        // 插件启动时注册自定义模型
        schemeManager.register(AuthApp.class);

        System.out.println("logs : [ Plugin Start ]，插件启动成功！");
    }

    @Override
    public void stop() {

        // 插件停用时取消注册自定义模型
        schemeManager.unregister(schemeManager.get(AuthApp.class));

        System.out.println("logs : [ Plugin Stop ]，插件停止！");
    }
}
