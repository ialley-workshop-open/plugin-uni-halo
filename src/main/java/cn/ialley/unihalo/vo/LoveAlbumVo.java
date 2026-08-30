package cn.ialley.unihalo.vo;

import java.util.List;

import lombok.Data;
import cn.ialley.unihalo.scheme.LoveAlbum;

/**
 * 恋爱相册公开视图（脱敏）。
 *
 * <p>加密相册（locked=true）不返回 photos；passwordHash 已由服务层屏蔽，永不外泄。</p>
 *
 * @author 小莫唐尼
 */
@Data
public class LoveAlbumVo {

    private String name;

    private String displayName;

    private String description;

    private String cover;

    private Integer photoCount;

    /**
     * 是否加密：true 表示需要解锁才能查看照片。
     */
    private boolean locked;

    /**
     * 照片列表（locked=true 时为空）。
     */
    private List<LoveAlbum.AlbumPhoto> photos;

    public static LoveAlbumVo from(LoveAlbum album, boolean locked) {
        LoveAlbumVo vo = new LoveAlbumVo();
        vo.setName(album.getMetadata().getName());
        var spec = album.getSpec();
        if (spec != null) {
            vo.setDisplayName(spec.getDisplayName());
            vo.setDescription(spec.getDescription());
            vo.setCover(spec.getCover());
        }
        vo.setPhotos(locked ? List.of()
                : (spec != null && spec.getPhotos() != null ? spec.getPhotos() : List.of()));
        vo.setPhotoCount(album.getStatus() == null ? 0 : album.getStatus().getPhotoCount());
        vo.setLocked(locked);
        return vo;
    }
}
