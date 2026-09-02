package cn.ialley.unihalo.reconciler;

import cn.ialley.unihalo.scheme.MiniProgramLinkGroup;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;

/**
 * {@link MiniProgramLinkGroup} 统一删除语义 Reconciler（设计见 .docs/deletion-finalizer-design.md）。
 *
 * <p>删除分组不影响组内链接（链接按 name 引用分组），走基类统一删除流程；
 * 删除中过渡默认 1s，清理钩子默认空。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class MiniProgramLinkGroupFinalizerReconciler
        extends AbstractFinalizerReconciler<MiniProgramLinkGroup> {

    public MiniProgramLinkGroupFinalizerReconciler(ExtensionClient client) {
        super(client);
    }

    @Override
    protected Class<MiniProgramLinkGroup> schemeType() {
        return MiniProgramLinkGroup.class;
    }

    @Override
    protected MiniProgramLinkGroup newExtension() {
        return new MiniProgramLinkGroup();
    }
}
