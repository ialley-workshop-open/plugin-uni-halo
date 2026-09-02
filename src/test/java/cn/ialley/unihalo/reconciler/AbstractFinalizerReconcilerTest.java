package cn.ialley.unihalo.reconciler;

import cn.ialley.unihalo.scheme.Banner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequeueException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AbstractFinalizerReconciler} 单元测试：覆盖补 finalizer / 删除过渡 requeue /
 * 延迟满足后清理移除 finalizer 四条路径（设计见 .docs/deletion-finalizer-design.md）。
 */
@ExtendWith(MockitoExtension.class)
class AbstractFinalizerReconcilerTest {

    private static final String NAME = "test-banner";

    @Mock
    private ExtensionClient client;

    private TestReconciler reconciler;

    @BeforeEach
    void setUp() {
        reconciler = new TestReconciler(client);
    }

    @Test
    void shouldAddFinalizerAndUpdateWhenExtensionAliveWithoutFinalizer() {
        var banner = banner(false, null);
        when(client.fetch(Banner.class, NAME)).thenReturn(Optional.of(banner));

        reconciler.reconcile(new Reconciler.Request(NAME));

        assertThat(banner.getMetadata().getFinalizers())
                .contains(AbstractFinalizerReconciler.FINALIZER_NAME);
        verify(client).update(banner);
    }

    @Test
    void shouldNotUpdateWhenFinalizerAlreadyExists() {
        var banner = banner(false, null);
        banner.getMetadata().setFinalizers(Set.of(AbstractFinalizerReconciler.FINALIZER_NAME));
        when(client.fetch(Banner.class, NAME)).thenReturn(Optional.of(banner));

        reconciler.reconcile(new Reconciler.Request(NAME));

        verify(client, never()).update(any());
    }

    @Test
    void shouldFinalizeImmediatelyWhenDeletingAndDelayIsZero() {
        reconciler = new TestReconciler(client, Duration.ZERO);
        var banner = banner(true, null);
        banner.getMetadata().setFinalizers(Set.of(AbstractFinalizerReconciler.FINALIZER_NAME));
        when(client.fetch(Banner.class, NAME)).thenReturn(Optional.of(banner));

        reconciler.reconcile(new Reconciler.Request(NAME));

        assertThat(reconciler.cleanedUp).containsExactly(banner);
        assertThat(banner.getMetadata().getFinalizers())
                .doesNotContain(AbstractFinalizerReconciler.FINALIZER_NAME);
        verify(client).update(banner);
    }

    @Test
    void shouldRequeueWhenDeletingFirstTimeWithDelay() {
        var banner = banner(true, null);
        when(client.fetch(Banner.class, NAME)).thenReturn(Optional.of(banner));

        assertThatThrownBy(() -> reconciler.reconcile(new Reconciler.Request(NAME)))
                .isInstanceOf(RequeueException.class);

        // 记录删除过渡开始时间并 update
        assertThat(banner.getMetadata().getAnnotations())
                .containsKey("unihalo.ialley.cn/deletion-requested-at");
        verify(client).update(banner);
        assertThat(reconciler.cleanedUp).isEmpty();
    }

    @Test
    void shouldFinalizeAfterDeletionDelayPassed() {
        var banner = banner(true, Instant.now().minusSeconds(10));
        banner.getMetadata().setFinalizers(Set.of(AbstractFinalizerReconciler.FINALIZER_NAME));
        when(client.fetch(Banner.class, NAME)).thenReturn(Optional.of(banner));

        reconciler.reconcile(new Reconciler.Request(NAME));

        assertThat(reconciler.cleanedUp).containsExactly(banner);
        assertThat(banner.getMetadata().getFinalizers())
                .doesNotContain(AbstractFinalizerReconciler.FINALIZER_NAME);
        verify(client).update(banner);
    }

    @Test
    void shouldDoNothingWhenExtensionNotFound() {
        when(client.fetch(Banner.class, NAME)).thenReturn(Optional.empty());

        reconciler.reconcile(new Reconciler.Request(NAME));

        verify(client, never()).update(any());
    }

    private static Banner banner(boolean deleted, Instant deletionRequestedAt) {
        var banner = new Banner();
        var metadata = new Metadata();
        metadata.setName(NAME);
        if (deleted) {
            metadata.setDeletionTimestamp(Instant.now());
        }
        if (deletionRequestedAt != null) {
            metadata.setAnnotations(
                    new java.util.HashMap<>(java.util.Map.of(
                            "unihalo.ialley.cn/deletion-requested-at",
                            deletionRequestedAt.toString())));
        }
        banner.setMetadata(metadata);
        return banner;
    }

    /** 测试用子类：可配置延迟并记录清理钩子调用。 */
    private static class TestReconciler extends AbstractFinalizerReconciler<Banner> {

        private final Duration delay;
        private final List<Banner> cleanedUp = new ArrayList<>();

        TestReconciler(ExtensionClient client) {
            this(client, Duration.ofSeconds(1));
        }

        TestReconciler(ExtensionClient client, Duration delay) {
            super(client);
            this.delay = delay;
        }

        @Override
        protected Class<Banner> schemeType() {
            return Banner.class;
        }

        @Override
        protected Banner newExtension() {
            return new Banner();
        }

        @Override
        protected Duration deletionDelay() {
            return delay;
        }

        @Override
        protected void cleanUp(Banner extension) {
            cleanedUp.add(extension);
        }
    }
}
