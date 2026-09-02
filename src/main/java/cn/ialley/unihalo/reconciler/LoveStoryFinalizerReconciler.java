package cn.ialley.unihalo.reconciler;

import cn.ialley.unihalo.scheme.LoveStory;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;

/**
 * {@link LoveStory} 统一删除语义 Reconciler（设计见 .docs/deletion-finalizer-design.md）。
 *
 * <p>删除故事走基类统一删除流程（删除中过渡默认 1s），清理钩子默认空。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class LoveStoryFinalizerReconciler extends AbstractFinalizerReconciler<LoveStory> {

    public LoveStoryFinalizerReconciler(ExtensionClient client) {
        super(client);
    }

    @Override
    protected Class<LoveStory> schemeType() {
        return LoveStory.class;
    }

    @Override
    protected LoveStory newExtension() {
        return new LoveStory();
    }
}
