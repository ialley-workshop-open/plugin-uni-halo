package cn.ialley.unihalo.reconciler;

import cn.ialley.unihalo.scheme.MiniProgramLink;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;

/**
 * {@link MiniProgramLink} 统一删除语义 Reconciler（设计见 .docs/deletion-finalizer-design.md）。
 *
 * <p>删除小程序链接走基类统一删除流程（删除中过渡默认 1s），清理钩子默认空。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class MiniProgramLinkFinalizerReconciler
        extends AbstractFinalizerReconciler<MiniProgramLink> {

    public MiniProgramLinkFinalizerReconciler(ExtensionClient client) {
        super(client);
    }

    @Override
    protected Class<MiniProgramLink> schemeType() {
        return MiniProgramLink.class;
    }

    @Override
    protected MiniProgramLink newExtension() {
        return new MiniProgramLink();
    }
}
