package cn.ialley.unihalo.reconciler;

import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequeueException;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;

/**
 * 统一删除语义的 finalizer Reconciler 抽象基类（设计见 .docs/deletion-finalizer-design.md）。
 *
 * <p>目标 Scheme 删除流程统一为：管理端 DELETE 打上 {@code metadata.deletionTimestamp}
 * （软标记，不物理删除）→ 本 Reconciler 在删除分支等待 {@link #deletionDelay()}（默认 1s，
 * 复现 plugin-vote「删除中 → 自动刷新消失」过渡体验）→ 执行清理钩子 {@link #cleanUp(T)}
 * （默认空，子类按需覆写级联清理）→ {@code removeFinalizers} → 框架物理删除。</p>
 *
 * <p>仅处理 finalizer 生命周期，不掺业务逻辑；业务删除前置校验仍在各 Service 层保留。
 * 控制器注册机制同 plugin-vote 的 VoteReconciler（{@link #setupWith(ControllerBuilder)}），
 * 无需在插件入口手动注册。</p>
 *
 * @param <T> 目标 Scheme 类型
 * @author 小莫唐尼
 */
public abstract class AbstractFinalizerReconciler<T extends AbstractExtension>
        implements Reconciler<Reconciler.Request> {

    /**
     * 统一的 finalizer 名（决策 D3，所有纳入 Scheme 共用；finalizer 属于对象自身 metadata）。
     */
    public static final String FINALIZER_NAME = "unihalo.ialley.cn/finalizer";

    /**
     * 删除过渡开始时间注解（防止重启/requeue 后重复计时，随对象持久化）。
     */
    private static final String DELETION_REQUESTED_AT = "unihalo.ialley.cn/deletion-requested-at";

    protected final ExtensionClient client;

    protected AbstractFinalizerReconciler(ExtensionClient client) {
        this.client = client;
    }

    /**
     * 目标 Scheme 类型（子类实现）。
     */
    protected abstract Class<T> schemeType();

    /**
     * Controller 注册用扩展模板实例（子类实现，返回 {@code new Xxx()}）。
     */
    protected abstract T newExtension();

    /**
     * 删除时的清理钩子（默认空；将来级联删除子资源/附件等在此实现）。
     */
    protected void cleanUp(T extension) {
        // no-op
    }

    /**
     * 删除过渡时长：默认 1s（决策 D5）；覆写为 {@link Duration#ZERO} 可立即清除。
     */
    protected Duration deletionDelay() {
        return Duration.ofSeconds(1);
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(schemeType(), request.name()).ifPresent(extension -> {
            if (ExtensionUtil.isDeleted(extension)) {
                handleDeletion(extension);
                return;
            }
            if (addFinalizers(extension.getMetadata(), Set.of(FINALIZER_NAME))) {
                client.update(extension);
            }
        });
        return Result.doNotRetry();
    }

    private void handleDeletion(T extension) {
        Duration delay = deletionDelay();
        if (delay.isZero()) {
            doFinalize(extension);
            return;
        }
        var annotations = MetadataUtil.nullSafeAnnotations(extension);
        String requestedAt = annotations.get(DELETION_REQUESTED_AT);
        if (requestedAt == null) {
            // 首次进入删除分支：记录开始时间并等待过渡窗口
            annotations.put(DELETION_REQUESTED_AT, Instant.now().toString());
            client.update(extension);
            throw new RequeueException(Result.requeue(delay), "wait for deletion transition");
        }
        Instant deadline = Instant.parse(requestedAt).plus(delay);
        Instant now = Instant.now();
        if (now.isBefore(deadline)) {
            throw new RequeueException(Result.requeue(Duration.between(now, deadline)),
                "wait for deletion transition");
        }
        doFinalize(extension);
    }

    private void doFinalize(T extension) {
        cleanUp(extension);
        if (removeFinalizers(extension.getMetadata(), Set.of(FINALIZER_NAME))) {
            client.update(extension);
        }
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder.extension(newExtension()).build();
    }
}
