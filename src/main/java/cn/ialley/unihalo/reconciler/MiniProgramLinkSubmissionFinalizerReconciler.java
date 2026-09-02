package cn.ialley.unihalo.reconciler;

import cn.ialley.unihalo.scheme.MiniProgramLinkSubmission;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;

/**
 * {@link MiniProgramLinkSubmission} 统一删除语义 Reconciler（设计见 .docs/deletion-finalizer-design.md）。
 *
 * <p>删除公开投稿走基类统一删除流程（删除中过渡默认 1s），清理钩子默认空；
 * 注意删除中窗口内公开接口须排除该对象（读路径过滤规则）。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class MiniProgramLinkSubmissionFinalizerReconciler
        extends AbstractFinalizerReconciler<MiniProgramLinkSubmission> {

    public MiniProgramLinkSubmissionFinalizerReconciler(ExtensionClient client) {
        super(client);
    }

    @Override
    protected Class<MiniProgramLinkSubmission> schemeType() {
        return MiniProgramLinkSubmission.class;
    }

    @Override
    protected MiniProgramLinkSubmission newExtension() {
        return new MiniProgramLinkSubmission();
    }
}
