package cn.ialley.unihalo.reconciler;

import cn.ialley.unihalo.scheme.Banner;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;

/**
 * {@link Banner} 统一删除语义 Reconciler（试点，设计见 .docs/deletion-finalizer-design.md）。
 *
 * <p>删除轮播图条目时：管理端 DELETE 打 {@code deletionTimestamp} → 本 Reconciler 等待
 * {@link #deletionDelay()}（默认 1s，前端 1s 条件轮询可见「删除中」）→ 移除 finalizer →
 * 物理删除。当前无级联子资源，清理钩子使用基类默认空实现。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class BannerFinalizerReconciler extends AbstractFinalizerReconciler<Banner> {

    public BannerFinalizerReconciler(ExtensionClient client) {
        super(client);
    }

    @Override
    protected Class<Banner> schemeType() {
        return Banner.class;
    }

    @Override
    protected Banner newExtension() {
        return new Banner();
    }
}
