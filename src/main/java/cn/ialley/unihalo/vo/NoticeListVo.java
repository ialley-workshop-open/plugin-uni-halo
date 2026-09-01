package cn.ialley.unihalo.vo;

import lombok.Data;
import cn.ialley.unihalo.scheme.Notice;
import cn.ialley.unihalo.scheme.NoticeType;

/**
 * 通知公告公开列表视图（脱敏，决策 D5/D10）。
 *
 * <p>不返回 content 富文本正文，仅返回标题 + 摘要 + 封面 + 类型信息等，
 * 控制 app 端流量；详情接口才返回完整 HTML。类型信息内嵌（typeDisplayName/
 * typeColor），app 端无需再拉类型映射。</p>
 *
 * @author 小莫唐尼
 */
@Data
public class NoticeListVo {

    private String name;

    private String title;

    private String summary;

    private String cover;

    private String link;

    private String typeName;

    private String typeDisplayName;

    private String typeColor;

    private Integer priority;

    /**
     * 发布时间（spec.publishTime，ISO-8601）。
     */
    private String publishTime;

    public static NoticeListVo from(Notice notice) {
        return from(notice, null);
    }

    public static NoticeListVo from(Notice notice, NoticeType type) {
        NoticeListVo vo = new NoticeListVo();
        vo.setName(notice.getMetadata().getName());
        var spec = notice.getSpec();
        if (spec != null) {
            vo.setTitle(spec.getTitle());
            vo.setSummary(spec.getSummary());
            vo.setCover(spec.getCover());
            vo.setLink(spec.getLink());
            vo.setTypeName(spec.getTypeName());
            vo.setPriority(spec.getPriority());
            vo.setPublishTime(spec.getPublishTime());
        }
        if (type != null && type.getSpec() != null) {
            vo.setTypeDisplayName(type.getSpec().getDisplayName());
            vo.setTypeColor(type.getSpec().getColor());
        }
        return vo;
    }
}
