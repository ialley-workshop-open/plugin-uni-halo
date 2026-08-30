package cn.ialley.unihalo.scheme;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static cn.ialley.unihalo.constants.Constants.BASIC_DOMAIN_NAME;
import static cn.ialley.unihalo.constants.Constants.PLUGIN_API_VERSION;

/**
 * 恋爱清单条目（原 loveDaily.list[] 数组抽离为独立模型）。
 *
 * <p>状态：wait 未开始 / doing 进行中 / complete 已完成（此时 completeDate 必填）。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = BASIC_DOMAIN_NAME, version = PLUGIN_API_VERSION,
        kind = "LoveDailyItem", plural = "loveDailyItems", singular = "loveDailyItem")
public class LoveDailyItem extends AbstractExtension {

    private LoveDailyItemSpec spec;

    @Data
    public static class LoveDailyItemSpec {

        /**
         * 标题（必填）
         */
        private String title;

        /**
         * 内容
         */
        private String content;

        /**
         * 状态：wait / doing / complete，默认 wait
         */
        private String status;

        /**
         * 计划时间（yyyy-MM-dd，可选）
         */
        private String planDate;

        /**
         * 完成时间（yyyy-MM-dd），status=complete 时必填
         */
        private String completeDate;

        /**
         * 完成感想（可选），status=complete 时保留
         */
        private String completeRemark;

        /**
         * 图片列表（多图，Halo 附件 URL 列表）
         */
        private List<String> images;

        /**
         * 排序，越大越前，默认 0
         */
        private Integer priority;
    }
}
