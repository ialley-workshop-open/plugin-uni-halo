package cn.ialley.unihalo.scheme;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static cn.ialley.unihalo.constants.Constants.BASIC_DOMAIN_NAME;
import static cn.ialley.unihalo.constants.Constants.PLUGIN_API_VERSION;

/**
 * 恋爱故事（多条目，决策 D5）。
 *
 * <p>原设置中 ourStory.content 单条 HTML 升级为多条故事，每条含标题、内容、
 * 时间、图片（多图）；模块开关（enabled/iconUrl）仍由 settings 控制。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = BASIC_DOMAIN_NAME, version = PLUGIN_API_VERSION,
        kind = "LoveStory", plural = "loveStories", singular = "loveStory")
public class LoveStory extends AbstractExtension {

    private LoveStorySpec spec;

    @Data
    public static class LoveStorySpec {

        /**
         * 故事标题（必填）
         */
        private String title;

        /**
         * 故事内容（支持 HTML）
         */
        private String content;

        /**
         * 故事时间（可选，yyyy-MM-dd）
         */
        private String date;

        /**
         * 故事地点（可选）
         */
        private String location;

        /**
         * 故事图片（多图，Halo 附件 URL 列表）
         */
        private List<String> images;

        /**
         * 排序，越大越前，默认 0
         */
        private Integer priority;
    }
}
