package pro.uhalo.uni.constants;

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
     * 基础的域名地址
     */
    public static final String BASIC_DOMAIN_NAME = "uni.uhalo.pro";

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
