package pro.uhalo.uni.extension;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import pro.uhalo.uni.constants.Constants;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * 应用管理
 *
 * @author 小莫唐尼
 * @since 1.0.0
 */
@GVK(kind = "AuthApp", group = "auth-app." + Constants.BASIC_DOMAIN_NAME,
    version = "v1alpha1", singular = "authApp", plural = "authApps")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AuthApp extends AbstractExtension {

    public static final String REQUIRE_SYNC_ON_STARTUP_INDEX_NAME = "requireSyncOnStartup";

    @Schema(requiredMode = REQUIRED)
    private AuthAppSpec spec;


    @Data
    public static class AuthAppSpec {

        @Schema(description = "应用ID", requiredMode = REQUIRED)
        private String appId;

        @Schema(description = "应用名称", requiredMode = REQUIRED)
        private String appName;

        @Schema(description = "应用图标", requiredMode = REQUIRED)
        private String appLogo;

        @Schema(description = "应用介绍", requiredMode = REQUIRED)
        private String appDesc;

        @Schema(description = "应用状态", requiredMode = REQUIRED)
        private String status;

        @Schema(description = "应用作者", requiredMode = REQUIRED)
        private String author;

        @Schema(description = "应用版本", requiredMode = REQUIRED)
        private String version;

        @Schema(description = "应用官网", requiredMode = REQUIRED)
        private String officialWebsite;

    }

}
