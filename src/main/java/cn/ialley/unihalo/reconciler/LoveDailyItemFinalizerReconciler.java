package cn.ialley.unihalo.reconciler;

import cn.ialley.unihalo.scheme.LoveDailyItem;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;

/**
 * {@link LoveDailyItem} 统一删除语义 Reconciler（设计见 .docs/deletion-finalizer-design.md）。
 *
 * <p>删除日常记录走基类统一删除流程（删除中过渡默认 1s），清理钩子默认空。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class LoveDailyItemFinalizerReconciler
        extends AbstractFinalizerReconciler<LoveDailyItem> {

    public LoveDailyItemFinalizerReconciler(ExtensionClient client) {
        super(client);
    }

    @Override
    protected Class<LoveDailyItem> schemeType() {
        return LoveDailyItem.class;
    }

    @Override
    protected LoveDailyItem newExtension() {
        return new LoveDailyItem();
    }
}
