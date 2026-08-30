package cn.ialley.unihalo.scheme;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static cn.ialley.unihalo.constants.Constants.BASIC_DOMAIN_NAME;
import static cn.ialley.unihalo.constants.Constants.PLUGIN_API_VERSION;

/**
 * 恋爱配置（单例，metadata.name 固定为 love-config）。
 *
 * <p>仅承载内容型数据：纪念日 + 恋人信息（决策 D4）。图片配置（pageImages）与
 * 模块开关（恋爱相册/恋爱清单/恋爱故事的 enabled/iconUrl）继续保留在
 * setting.yaml 的 loveConfig 组中，由插件设置页维护，不进入模型。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = BASIC_DOMAIN_NAME, version = PLUGIN_API_VERSION,
        kind = "LoveConfig", plural = "loveConfigs", singular = "loveConfig")
public class LoveConfig extends AbstractExtension {

    private LoveConfigSpec spec;

    @Data
    public static class LoveConfigSpec {

        /**
         * 纪念日标题，默认"这是我们一起走过的"
         */
        private String loveDateTitle;

        /**
         * 恋爱纪念日（yyyy-MM-dd），用于计算恋爱天数
         */
        private String loveDate;

        /**
         * 恋人信息
         */
        private LoveInfo loveInfo;
    }

    @Data
    public static class LoveInfo {

        /**
         * 男生昵称
         */
        private String boyNickname;

        /**
         * 男生头像
         */
        private String boyAvatar;

        /**
         * 女生昵称
         */
        private String girlNickname;

        /**
         * 女生头像
         */
        private String girlAvatar;
    }
}
