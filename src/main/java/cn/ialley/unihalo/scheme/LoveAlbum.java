package cn.ialley.unihalo.scheme;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static cn.ialley.unihalo.constants.Constants.BASIC_DOMAIN_NAME;
import static cn.ialley.unihalo.constants.Constants.PLUGIN_API_VERSION;

/**
 * 恋爱相册（自研，不再依赖系统图库分组）。
 *
 * <p>相册支持查看密码：密码以 BCrypt 哈希存储于 spec.passwordHash，任何读取接口
 * 均不回显；公开接口通过 unlock 校验后签发 HMAC 签名 token 换取照片数据。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = BASIC_DOMAIN_NAME, version = PLUGIN_API_VERSION,
        kind = "LoveAlbum", plural = "loveAlbums", singular = "loveAlbum")
public class LoveAlbum extends AbstractExtension {

    private LoveAlbumSpec spec;

    private LoveAlbumStatus status;

    @Data
    public static class LoveAlbumSpec {

        /**
         * 相册名称（必填）
         */
        private String displayName;

        /**
         * 相册描述
         */
        private String description;

        /**
         * 封面图 URL（Halo 附件链接）
         */
        private String cover;

        /**
         * 是否启用查看密码
         */
        private Boolean passwordEnabled;

        /**
         * 密码 BCrypt 哈希（仅写入；读取接口一律不回显）
         */
        private String passwordHash;

        /**
         * 排序，越大越前，默认 0
         */
        private Integer priority;

        /**
         * 照片列表
         */
        private List<AlbumPhoto> photos;
    }

    @Data
    public static class AlbumPhoto {

        /**
         * 照片标识（服务端生成）
         */
        private String name;

        /**
         * 图片地址（Halo 附件 permalink）
         */
        private String url;

        /**
         * 照片标题
         */
        private String title;

        /**
         * 照片描述
         */
        private String description;

        /**
         * 拍摄日期
         */
        private String takenDate;

        /**
         * 拍摄地点（决策 D6）
         */
        private String location;

        /**
         * 相册内排序，越大越前
         */
        private Integer priority;
    }

    @Data
    public static class LoveAlbumStatus {

        /**
         * 照片数量（服务端计算，列表展示用）
         */
        private Integer photoCount;
    }
}
