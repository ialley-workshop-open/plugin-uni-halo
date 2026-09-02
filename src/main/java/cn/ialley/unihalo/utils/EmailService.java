package cn.ialley.unihalo.utils;

import java.util.Properties;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import cn.ialley.unihalo.scheme.MiniProgramLinkSubmission;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Secret;
import cn.ialley.unihalo.utils.SettingGroupResolver;
import run.halo.app.plugin.ReactiveSettingFetcher;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * 审核结果邮件通知（决策 D11）。
 *
 * <p>复用 Halo 内置邮件通知器的 SMTP 配置：读取 Secret
 * {@code notifier-setting-secret} 的 {@code default-email-notifier.json}
 * （sender.host/port/username/password/displayName/encryption），不重复提供
 * SMTP 配置页。发送开关由插件设置 {@code linkConfig.sendEmail} 控制；
 * 发送失败仅记日志，不阻塞审核流程。</p>
 *
 * @author 小莫唐尼
 */
@Slf4j
@Component
public class EmailService {

    private static final String NOTIFIER_SECRET_NAME = "notifier-setting-secret";
    private static final String NOTIFIER_SECRET_KEY = "default-email-notifier.json";
    private static final String SETTING_GROUP_LINK_CONFIG = "linkConfig";
    private static final String SETTING_KEY_SEND_EMAIL = "sendEmail";

    private static final String SITE_NAME_DEFAULT = "uni-halo";

    private final ReactiveExtensionClient client;
    private final ReactiveSettingFetcher settingFetcher;

    /**
     * 插件 Spring 上下文未注册 Jackson 3 ObjectMapper bean，故内部自行创建。
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmailService(ReactiveExtensionClient client, ReactiveSettingFetcher settingFetcher) {
        this.client = client;
        this.settingFetcher = settingFetcher;
    }

    /**
     * 发送审核结果邮件（通过/拒绝）。开关关闭或申请人未填邮箱时静默跳过；
     * SMTP 配置缺失/发送异常仅记日志（D11）。
     */
    public Mono<Void> sendAuditEmail(MiniProgramLinkSubmission submission) {
        var spec = submission.getSpec();
        if (spec == null || isBlank(spec.getEmail())) {
            return Mono.empty();
        }
        return SettingGroupResolver.group(settingFetcher, "featureConfig",
                SETTING_GROUP_LINK_CONFIG)
                .defaultIfEmpty(JsonNodeFactory.instance.objectNode())
                .flatMap(config -> {
                    boolean sendEmail = config.path(SETTING_KEY_SEND_EMAIL).asBoolean(false);
                    if (!sendEmail) {
                        return Mono.empty();
                    }
                    String siteName = config.path("siteName").asString(SITE_NAME_DEFAULT);
                    String subject = STATUS_APPROVED.equals(spec.getStatus())
                            ? "【" + siteName + "】友情链接审核通过通知"
                            : "【" + siteName + "】友情链接审核未通过通知";
                    String content = buildHtml(spec, siteName);
                    return sendEmail(spec.getEmail(), subject, content);
                });
    }

    /**
     * 读取 Halo 内置邮件通知器 SMTP 配置并发送；配置缺失或发送失败不抛出（D11）。
     */
    private Mono<Void> sendEmail(String to, String subject, String content) {
        return client.fetch(Secret.class, NOTIFIER_SECRET_NAME)
                .map(Secret::getStringData)
                .mapNotNull(data -> data.get(NOTIFIER_SECRET_KEY))
                .map(value -> parseConfig(value))
                .filter(JsonNode::isObject)
                .flatMap(config -> {
                    try {
                        send(config, to, subject, content);
                        log.info("审核邮件发送成功：to={}, subject={}", to, subject);
                    } catch (Exception e) {
                        log.error("审核邮件发送失败：to={}", to, e);
                    }
                    return Mono.<Void>empty();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("未找到 Halo 邮件通知器 SMTP 配置（Secret={}），跳过邮件发送",
                            NOTIFIER_SECRET_NAME);
                    return Mono.<Void>empty();
                }));
    }

    private void send(JsonNode sender, String to, String subject, String content)
            throws Exception {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(sender.path("host").asString());
        mailSender.setPort(sender.path("port").asInt());
        mailSender.setUsername(sender.path("username").asString());
        mailSender.setPassword(sender.path("password").asString());
        mailSender.setProtocol("smtp");
        mailSender.setDefaultEncoding("UTF-8");

        String encryption = sender.path("encryption").asString("NONE").toUpperCase();
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", "true");
        if (encryption.contains("TLS")) {
            properties.setProperty("mail.smtp.starttls.enable", "true");
        } else if (encryption.contains("SSL")) {
            properties.setProperty("mail.smtp.ssl.enable", "true");
        }
        mailSender.setJavaMailProperties(properties);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(mailSender.getUsername(), sender.path("displayName").asString("Halo"));
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        mailSender.send(message);
    }

    private JsonNode parseConfig(String value) {
        try {
            return objectMapper.readTree(value);
        } catch (Exception e) {
            log.warn("解析邮件通知器配置失败", e);
            return JsonNodeFactory.instance.objectNode();
        }
    }

    private String buildHtml(MiniProgramLinkSubmission.MiniProgramLinkSubmissionSpec spec,
            String siteName) {
        boolean approved = STATUS_APPROVED.equals(spec.getStatus());
        String reason = spec.getReason();
        String linkName = spec.getDisplayName();
        String reasonBlock = isBlank(reason) ? ""
                : "<p style='margin:8px 0 0;color:#6b7280;'>审核说明：" + escape(reason) + "</p>";
        String result = approved
                ? "<p style='margin:0;color:#059669;font-size:16px;font-weight:600;'>审核通过，已收录 🎉</p>"
                : "<p style='margin:0;color:#dc2626;font-size:16px;font-weight:600;'>很遗憾，本次申请未通过</p>";
        return "<div style='max-width:520px;margin:0 auto;padding:24px;font-family:-apple-system,"
                + "Segoe UI,Roboto,sans-serif;font-size:14px;color:#1f2937;line-height:1.7;'>"
                + "<p style='margin:0 0 12px;color:#111827;font-size:18px;font-weight:600;'>"
                + siteName + " 友情链接审核通知</p>"
                + "<p style='margin:0 0 8px;'>您好，您提交的小程序链接「" + escape(linkName) + "」：</p>"
                + result + reasonBlock
                + "<p style='margin:16px 0 0;color:#9ca3af;font-size:12px;'>此邮件由 "
                + siteName + " 自动发送，请勿直接回复。</p></div>";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static final String STATUS_APPROVED = "APPROVED";
}
