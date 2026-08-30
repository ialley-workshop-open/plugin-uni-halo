package cn.ialley.unihalo.scheme;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static cn.ialley.unihalo.constants.Constants.BASIC_DOMAIN_NAME;
import static cn.ialley.unihalo.constants.Constants.PLUGIN_API_VERSION;

/**
 * 应用版本（应用升级），对应 uni-upgrade-center 的 opendb-app-versions。
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = BASIC_DOMAIN_NAME, version = PLUGIN_API_VERSION,
        kind = "AppVersion", plural = "appVersions", singular = "appVersion")
public class AppVersion extends AbstractExtension {

    private AppVersionSpec spec;

    @Data
    public static class AppVersionSpec {

        /**
         * 应用标识（关联 AppInfo.appid）
         */
        private String appid;

        /**
         * 应用名称
         */
        private String name;

        /**
         * 更新标题
         */
        private String title;

        /**
         * 更新内容
         */
        private String contents;

        /**
         * 更新平台：Android / iOS / Harmony
         */
        private List<String> platform;

        /**
         * 安装包类型：native_app / wgt
         */
        private String type;

        /**
         * 版本号（应用版本名称），须大于当前线上发行版本；公开接口 checkVersion 以此比较
         */
        private String version;

        /**
         * 应用版本号（整数），须大于该应用已发布的最大值
         */
        private Integer versionCode;

        /**
         * 软删除标记：true 表示已删除（数据保留，列表中标记展示，不可恢复）
         */
        private Boolean isDeleted;

        /**
         * wgt 所需最低原生 App 版本
         */
        private String minUniVersion;

        /**
         * 安装包下载/跳转链接；iOS 为 AppStore 链接
         */
        private String url;

        /**
         * 是否上线发行（同 appid+platform+type 同时仅一个 true）
         */
        private Boolean stablePublish;

        /**
         * 是否静默更新（仅 wgt）
         */
        private Boolean isSilently;

        /**
         * 是否强制更新
         */
        private Boolean isMandatory;
    }
}