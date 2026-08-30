package cn.ialley.unihalo.constants;

/**
 * 插件使用到的常量定义
 *
 * @author 小莫唐尼
 */
public class Constants {
    /**
     * 接口版本
     */
    public static final String PLUGIN_API_VERSION = "v1alpha1";

    /**
     * endpoint 中的接口基础路径
     */
    public static final String END_POINT_API_BASE_PATH = "plugins/plugin-uni-halo";

    /**
     * 应用升级（公开 checkVersion）接口基础路径
     */
    public static final String UPGRADE_API_BASE_PATH = "plugins/plugin-uni-halo/upgrade";

    /**
     * 应用管理（console）接口基础路径
     */
    public static final String APP_INFO_API_BASE_PATH = "plugins/plugin-uni-halo/apps";

    /**
     * 应用升级管理（console）接口基础路径
     */
    public static final String APP_VERSION_API_BASE_PATH = "plugins/plugin-uni-halo/app-versions";

    /**
     * 恋爱配置（console）接口基础路径
     */
    public static final String LOVE_CONFIG_API_BASE_PATH = "plugins/plugin-uni-halo/love-config";

    /**
     * 恋爱相册（console）接口基础路径
     */
    public static final String LOVE_ALBUM_API_BASE_PATH = "plugins/plugin-uni-halo/love-albums";

    /**
     * 恋爱清单（console）接口基础路径
     */
    public static final String LOVE_DAILY_API_BASE_PATH = "plugins/plugin-uni-halo/love-daily-items";

    /**
     * 恋爱故事（console）接口基础路径
     */
    public static final String LOVE_STORY_API_BASE_PATH = "plugins/plugin-uni-halo/love-stories";

    /**
     * 恋爱配置单例名称（metadata.name 固定值）
     */
    public static final String LOVE_CONFIG_SINGLETON_NAME = "love-config";

    /**
     * 基础的域名地址
     */
    public static final String BASIC_DOMAIN_NAME = "unihalo.ialley.cn";

    /**
     * 基础的域名接口地址
     */
    public static final String BASIC_DOMAIN_API_NAME = "api." + BASIC_DOMAIN_NAME;

    /**
     * 自定义接口分组名称（给后台管理提供的接口）
     */
    public static final String CONSOLE_CUSTOM_API_GROUP_NAME =
        "console." + BASIC_DOMAIN_API_NAME + "/" + PLUGIN_API_VERSION;

    /**
     * 自定义接口分组名称（给用户中心提供的接口）
     */
    public static final String UC_CUSTOM_API_GROUP_NAME =
        "uc." + BASIC_DOMAIN_API_NAME + "/" + PLUGIN_API_VERSION;

    /**
     * 自定义接口分组名称
     */
    public static final String PUBLIC_CUSTOM_API_GROUP_NAME =
        BASIC_DOMAIN_API_NAME + "/" + PLUGIN_API_VERSION;

}