package cn.ialley.unihalo.reconciler;

import cn.ialley.unihalo.scheme.AppInfo;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;

/**
 * {@link AppInfo} 统一删除语义 Reconciler（设计见 .docs/deletion-finalizer-design.md）。
 *
 * <p>删除前置业务校验（已有已发布 AppVersion 时 400）在 Service 层保留；
 * 校验通过后走基类统一删除流程（删除中过渡默认 1s）。无级联子资源，清理钩子默认空。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class AppInfoFinalizerReconciler extends AbstractFinalizerReconciler<AppInfo> {

    public AppInfoFinalizerReconciler(ExtensionClient client) {
        super(client);
    }

    @Override
    protected Class<AppInfo> schemeType() {
        return AppInfo.class;
    }

    @Override
    protected AppInfo newExtension() {
        return new AppInfo();
    }
}
