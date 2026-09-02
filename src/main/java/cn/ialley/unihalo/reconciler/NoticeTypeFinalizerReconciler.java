package cn.ialley.unihalo.reconciler;

import cn.ialley.unihalo.scheme.NoticeType;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;

/**
 * {@link NoticeType} 统一删除语义 Reconciler（设计见 .docs/deletion-finalizer-design.md）。
 *
 * <p>删除公告类型不影响已关联公告（公告仅按 name 引用类型），走基类统一删除流程；
 * 删除中过渡默认 1s，清理钩子默认空。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class NoticeTypeFinalizerReconciler extends AbstractFinalizerReconciler<NoticeType> {

    public NoticeTypeFinalizerReconciler(ExtensionClient client) {
        super(client);
    }

    @Override
    protected Class<NoticeType> schemeType() {
        return NoticeType.class;
    }

    @Override
    protected NoticeType newExtension() {
        return new NoticeType();
    }
}
