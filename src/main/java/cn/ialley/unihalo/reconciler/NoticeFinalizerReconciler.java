package cn.ialley.unihalo.reconciler;

import cn.ialley.unihalo.scheme.Notice;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;

/**
 * {@link Notice} 统一删除语义 Reconciler（设计见 .docs/deletion-finalizer-design.md）。
 *
 * <p>删除公告走基类统一删除流程（删除中过渡默认 1s）。无级联子资源，清理钩子默认空。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class NoticeFinalizerReconciler extends AbstractFinalizerReconciler<Notice> {

    public NoticeFinalizerReconciler(ExtensionClient client) {
        super(client);
    }

    @Override
    protected Class<Notice> schemeType() {
        return Notice.class;
    }

    @Override
    protected Notice newExtension() {
        return new Notice();
    }
}
