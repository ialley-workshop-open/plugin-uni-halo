package cn.ialley.unihalo.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import cn.ialley.unihalo.scheme.AppVersion;

/**
 * 升级检测结果（checkVersion），字段与 uni-upgrade-center-app 的
 * UniUpgradeCenterResult 对齐（snake_case），保证 app 端零改动。
 *
 * @author 小莫唐尼
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpgradeResult {

    /**
     * &gt;0 有更新 / 0 无更新 / &lt;0 错误
     */
    private int code;

    private String message;

    private String appid;

    private String name;

    private String title;

    private String contents;

    /**
     * 安装包下载地址（iOS 为 AppStore 链接）
     */
    private String url;

    /**
     * 适用平台：Android / iOS / Harmony
     */
    private List<String> platform;

    /**
     * 新版本号
     */
    private String version;

    /**
     * 安装包类型：native_app / wgt
     */
    private String type;

    @JsonProperty("is_mandatory")
    private Boolean isMandatory;

    @JsonProperty("is_silently")
    private Boolean isSilently;

    @JsonProperty("min_uni_version")
    private String minUniVersion;

    @JsonProperty("stable_publish")
    private Boolean stablePublish;

    public static UpgradeResult error(int code, String message) {
        UpgradeResult result = new UpgradeResult();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static UpgradeResult of(int code, String message, AppVersion appVersion) {
        UpgradeResult result = new UpgradeResult();
        result.setCode(code);
        result.setMessage(message);
        result.setAppid(appVersion.getSpec().getAppid());
        result.setName(appVersion.getSpec().getName());
        result.setTitle(appVersion.getSpec().getTitle());
        result.setContents(appVersion.getSpec().getContents());
        result.setUrl(appVersion.getSpec().getUrl());
        result.setPlatform(appVersion.getSpec().getPlatform());
        result.setVersion(appVersion.getSpec().getVersion());
        result.setType(appVersion.getSpec().getType());
        result.setIsMandatory(appVersion.getSpec().getIsMandatory());
        result.setIsSilently(appVersion.getSpec().getIsSilently());
        result.setMinUniVersion(appVersion.getSpec().getMinUniVersion());
        result.setStablePublish(appVersion.getSpec().getStablePublish());
        return result;
    }
}