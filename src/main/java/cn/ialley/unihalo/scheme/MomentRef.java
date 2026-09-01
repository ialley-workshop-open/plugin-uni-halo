package cn.ialley.unihalo.scheme;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * 跨插件查询引用（moment.halo.run/v1alpha1 Moment，由 plugin-moments 注册）。
 *
 * <p>仅用于按 GVK 查询候选数据（审核配置-选择瞬间），非本插件 Scheme，
 * 不要在 {@code UniHaloPlugin} 中注册。注意 Moment 的 {@code spec.content} 是
 * {@link MomentContentRef} 对象（含 raw/html/medium），不是字符串。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "moment.halo.run", version = "v1alpha1", kind = "Moment",
        plural = "moments", singular = "moment")
public class MomentRef extends AbstractExtension {

    private MomentRefSpec spec;

    @Data
    public static class MomentRefSpec {
        private MomentContentRef content;
    }

    @Data
    public static class MomentContentRef {
        private String raw;
        private String html;
        private List<MomentMediaRef> medium;
    }

    @Data
    public static class MomentMediaRef {
        private String type;
        private String url;
        private String originType;
    }
}
