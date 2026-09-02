package cn.ialley.unihalo.reconciler;

import cn.ialley.unihalo.scheme.LoveAlbum;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;

/**
 * {@link LoveAlbum} 统一删除语义 Reconciler（设计见 .docs/deletion-finalizer-design.md）。
 *
 * <p>照片内嵌于 spec（非子资源），删除相册即整体删除，走基类统一删除流程；
 * 删除中过渡默认 1s，清理钩子默认空。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class LoveAlbumFinalizerReconciler extends AbstractFinalizerReconciler<LoveAlbum> {

    public LoveAlbumFinalizerReconciler(ExtensionClient client) {
        super(client);
    }

    @Override
    protected Class<LoveAlbum> schemeType() {
        return LoveAlbum.class;
    }

    @Override
    protected LoveAlbum newExtension() {
        return new LoveAlbum();
    }
}
