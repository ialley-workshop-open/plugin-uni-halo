package cn.ialley.unihalo.vo;

import java.util.List;

import lombok.Data;
import cn.ialley.unihalo.scheme.MiniProgramLink;

/**
 * 小程序链接分组视图（公开接口 grouped=true 返回，对标 plugin-links LinkGroupVo）。
 *
 * <p>groupName 为分组 metadata.name（空=未分组），displayName 为分组显示名
 * （分组不存在或未分组时为空，消费端兜底「未分组」）；组内 links 按 priority
 * 倒序 + 创建时间倒序。</p>
 *
 * @author 小莫唐尼
 */
@Data
public class MiniProgramLinkGroupVo {

    /**
     * 分组 name（spec.groupName，空字符串=未分组）
     */
    private String groupName;

    /**
     * 分组显示名（分组不存在或未分组时为空字符串）
     */
    private String displayName;

    /**
     * 该分组下的链接列表
     */
    private List<MiniProgramLink> links;

    public static MiniProgramLinkGroupVo of(String groupName, String displayName,
            List<MiniProgramLink> links) {
        MiniProgramLinkGroupVo vo = new MiniProgramLinkGroupVo();
        vo.setGroupName(groupName);
        vo.setDisplayName(displayName);
        vo.setLinks(links);
        return vo;
    }
}
