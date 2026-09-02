package cn.ialley.unihalo;

import cn.ialley.unihalo.scheme.AppInfo;
import cn.ialley.unihalo.scheme.AppVersion;
import cn.ialley.unihalo.scheme.AuditDataConfig;
import cn.ialley.unihalo.scheme.Banner;
import cn.ialley.unihalo.scheme.LoveAlbum;
import cn.ialley.unihalo.scheme.LoveConfig;
import cn.ialley.unihalo.scheme.LoveDailyItem;
import cn.ialley.unihalo.scheme.LoveStory;
import cn.ialley.unihalo.scheme.MiniProgramLink;
import cn.ialley.unihalo.scheme.MiniProgramLinkGroup;
import cn.ialley.unihalo.scheme.MiniProgramLinkSubmission;
import cn.ialley.unihalo.scheme.Notice;
import cn.ialley.unihalo.scheme.NoticeType;
import cn.ialley.unihalo.scheme.QRCodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexSpecs;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
 * <p>Plugin main class to manage the lifecycle of the plugin.</p>
 * <p>This class must be public and have a public constructor.</p>
 * <p>Only one main class extending {@link BasePlugin} is allowed per plugin.</p>
 *
 * @author 小莫唐尼
 * @since 1.0.0
 */
@Slf4j
@Component
public class UniHaloPlugin extends BasePlugin {

    private final SchemeManager schemeManager;

    public UniHaloPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {

        schemeManager.register(QRCodeInfo.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<QRCodeInfo, String>single("key", String.class)
                    .indexFunc(QRCodeInfo::getKey));
            indexSpecs.add(IndexSpecs.<QRCodeInfo, String>single("postId", String.class)
                    .indexFunc(QRCodeInfo::getPostId));
        });

        schemeManager.register(AppInfo.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<AppInfo, String>single("spec.appid", String.class)
                    .indexFunc(appInfo -> appInfo.getSpec().getAppid()));
        });

        schemeManager.register(AppVersion.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<AppVersion, String>single("spec.appid", String.class)
                    .indexFunc(appVersion -> appVersion.getSpec().getAppid()));
            indexSpecs.add(IndexSpecs.<AppVersion, String>single("spec.type", String.class)
                    .indexFunc(appVersion -> appVersion.getSpec().getType()));
            indexSpecs.add(IndexSpecs.<AppVersion, Boolean>single("spec.stablePublish", Boolean.class)
                    .indexFunc(appVersion -> appVersion.getSpec().getStablePublish()));
            indexSpecs.add(IndexSpecs.<AppVersion, String>single("spec.title", String.class)
                    .indexFunc(appVersion -> appVersion.getSpec().getTitle()));
            indexSpecs.add(IndexSpecs.<AppVersion, String>single("spec.version", String.class)
                    .indexFunc(appVersion -> appVersion.getSpec().getVersion()));
        });

        schemeManager.register(LoveConfig.class, indexSpecs -> {
            // 单例模型，无需额外索引
        });

        schemeManager.register(AuditDataConfig.class, indexSpecs -> {
            // 单例模型，无需额外索引
        });

        schemeManager.register(LoveAlbum.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<LoveAlbum, Integer>single("spec.priority", Integer.class)
                    .indexFunc(loveAlbum -> loveAlbum.getSpec() == null ? null
                            : loveAlbum.getSpec().getPriority()));
            indexSpecs.add(IndexSpecs.<LoveAlbum, Boolean>single("spec.passwordEnabled", Boolean.class)
                    .indexFunc(loveAlbum -> loveAlbum.getSpec() == null ? null
                            : loveAlbum.getSpec().getPasswordEnabled()));
        });

        schemeManager.register(LoveDailyItem.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<LoveDailyItem, String>single("spec.status", String.class)
                    .indexFunc(item -> item.getSpec() == null ? null : item.getSpec().getStatus()));
            indexSpecs.add(IndexSpecs.<LoveDailyItem, String>single("spec.completeDate", String.class)
                    .indexFunc(item -> item.getSpec() == null ? null : item.getSpec().getCompleteDate()));
            indexSpecs.add(IndexSpecs.<LoveDailyItem, Integer>single("spec.priority", Integer.class)
                    .indexFunc(item -> item.getSpec() == null ? null
                            : item.getSpec().getPriority()));
        });

        schemeManager.register(LoveStory.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<LoveStory, Integer>single("spec.priority", Integer.class)
                    .indexFunc(story -> story.getSpec() == null ? null
                            : story.getSpec().getPriority()));
        });

        schemeManager.register(Notice.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<Notice, String>single("spec.status", String.class)
                    .indexFunc(notice -> notice.getSpec() == null ? null
                            : notice.getSpec().getStatus()));
            indexSpecs.add(IndexSpecs.<Notice, Integer>single("spec.priority", Integer.class)
                    .indexFunc(notice -> notice.getSpec() == null ? null
                            : notice.getSpec().getPriority()));
            indexSpecs.add(IndexSpecs.<Notice, String>single("spec.typeName", String.class)
                    .indexFunc(notice -> notice.getSpec() == null ? null
                            : notice.getSpec().getTypeName()));
            indexSpecs.add(IndexSpecs.<Notice, String>single("spec.publishTime", String.class)
                    .indexFunc(notice -> notice.getSpec() == null ? null
                            : notice.getSpec().getPublishTime()));
        });

        schemeManager.register(NoticeType.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<NoticeType, Integer>single("spec.priority", Integer.class)
                    .indexFunc(type -> type.getSpec() == null ? null
                            : type.getSpec().getPriority()));
        });

        schemeManager.register(Banner.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<Banner, Integer>single("spec.priority", Integer.class)
                    .indexFunc(banner -> banner.getSpec() == null ? null
                            : banner.getSpec().getPriority()));
            // 来源筛选（fieldQuery equal spec.source）与日期排序（sort spec.date）依赖索引
            indexSpecs.add(IndexSpecs.<Banner, String>single("spec.source", String.class)
                    .indexFunc(banner -> banner.getSpec() == null ? null
                            : banner.getSpec().getSource()));
            indexSpecs.add(IndexSpecs.<Banner, String>single("spec.date", String.class)
                    .indexFunc(banner -> banner.getSpec() == null ? null
                            : banner.getSpec().getDate()));
        });

        schemeManager.register(MiniProgramLinkGroup.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<MiniProgramLinkGroup, Integer>single("spec.priority",
                            Integer.class)
                    .indexFunc(group -> group.getSpec() == null ? null
                            : group.getSpec().getPriority()));
        });

        schemeManager.register(MiniProgramLink.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<MiniProgramLink, Boolean>single("spec.visible", Boolean.class)
                    .indexFunc(link -> link.getSpec() == null ? null
                            : link.getSpec().getVisible()));
            indexSpecs.add(IndexSpecs.<MiniProgramLink, Integer>single("spec.priority", Integer.class)
                    .indexFunc(link -> link.getSpec() == null ? null
                            : link.getSpec().getPriority()));
        });

        schemeManager.register(MiniProgramLinkSubmission.class, indexSpecs -> {
            indexSpecs.add(IndexSpecs.<MiniProgramLinkSubmission, String>single("spec.status",
                            String.class)
                    .indexFunc(submission -> submission.getSpec() == null ? null
                            : submission.getSpec().getStatus()));
            indexSpecs.add(IndexSpecs.<MiniProgramLinkSubmission, String>single("spec.submittedAt",
                            String.class)
                    .indexFunc(submission -> submission.getSpec() == null ? null
                            : submission.getSpec().getSubmittedAt()));
            indexSpecs.add(IndexSpecs.<MiniProgramLinkSubmission, String>single("spec.reviewedAt",
                            String.class)
                    .indexFunc(submission -> submission.getSpec() == null ? null
                            : submission.getSpec().getReviewedAt()));
        });

        log.info("【UniHalo】插件启动成功！");
    }

    @Override
    public void stop() {

        schemeManager.unregister(schemeManager.get(QRCodeInfo.class));
        schemeManager.unregister(schemeManager.get(AppInfo.class));
        schemeManager.unregister(schemeManager.get(AppVersion.class));
        schemeManager.unregister(schemeManager.get(LoveConfig.class));
        schemeManager.unregister(schemeManager.get(AuditDataConfig.class));
        schemeManager.unregister(schemeManager.get(LoveAlbum.class));
        schemeManager.unregister(schemeManager.get(LoveDailyItem.class));
        schemeManager.unregister(schemeManager.get(LoveStory.class));
        schemeManager.unregister(schemeManager.get(Notice.class));
        schemeManager.unregister(schemeManager.get(NoticeType.class));
        schemeManager.unregister(schemeManager.get(Banner.class));
        schemeManager.unregister(schemeManager.get(MiniProgramLink.class));
        schemeManager.unregister(schemeManager.get(MiniProgramLinkGroup.class));
        schemeManager.unregister(schemeManager.get(MiniProgramLinkSubmission.class));

        log.info("【UniHalo】插件停止！");
    }
}