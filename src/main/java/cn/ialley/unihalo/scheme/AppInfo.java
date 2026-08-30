package cn.ialley.unihalo.scheme;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static cn.ialley.unihalo.constants.Constants.BASIC_DOMAIN_NAME;
import static cn.ialley.unihalo.constants.Constants.PLUGIN_API_VERSION;

/**
 * 应用信息（应用管理），对应 uni-upgrade-center 的 opendb-app-list。
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = BASIC_DOMAIN_NAME, version = PLUGIN_API_VERSION,
        kind = "AppInfo", plural = "appInfos", singular = "appInfo")
public class AppInfo extends AbstractExtension {

    private AppInfoSpec spec;

    @Data
    public static class AppInfoSpec {

        /**
         * 应用标识（必填，手动填写）
         */
        private String appid;

        /**
         * 应用名称
         */
        private String name;

        /**
         * 应用简介
         */
        private String description;

        /**
         * 应用介绍（详细内容）
         */
        private String intro;

        /**
         * 应用图标（Halo 附件链接）
         */
        private String iconUrl;

        /**
         * 应用截图（Halo 附件链接列表）
         */
        private List<String> screenshot;

        /**
         * Android 平台信息（url 为 apk 直接下载地址）
         */
        private AppPlatformInfo appAndroid;

        /**
         * iOS 平台信息（url 为 AppStore 链接）
         */
        private AppPlatformInfo appIos;

        /**
         * Harmony 平台信息
         */
        private AppPlatformInfo appHarmony;
    }

    @Data
    public static class AppPlatformInfo {

        private String name;

        private String url;
    }
}