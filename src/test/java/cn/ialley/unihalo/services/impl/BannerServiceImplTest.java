package cn.ialley.unihalo.services.impl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import cn.ialley.unihalo.scheme.Banner;
import cn.ialley.unihalo.scheme.PostRef;
import cn.ialley.unihalo.vo.BannerListVo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 轮播图服务单元测试：source 自动判定、文章快照（Post/User 回填与回退）、
 * 排序、公开列表脱敏、公开详情。
 *
 * @author 小莫唐尼
 */
@ExtendWith(MockitoExtension.class)
class BannerServiceImplTest {

    @Mock
    ReactiveExtensionClient client;

    @InjectMocks
    BannerServiceImpl service;

    // ===== source 自动判定（决策 D2） =====

    @Test
    void createWithPostIdMarksSourcePostAndClearsContent() {
        when(client.fetch(eq(PostRef.class), eq("p1")))
                .thenReturn(Mono.just(postRef("p1", "文章一", "admin")));
        when(client.fetch(eq(User.class), eq("admin")))
                .thenReturn(Mono.just(user("admin", "管理员", "https://avatar")));
        when(client.create(any(Banner.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var banner = banner(null);
        banner.getSpec().setPostId("p1");
        // 前端即使误传 content/source，服务端也应清空/覆盖
        banner.getSpec().setContent("<p>不应保存</p>");
        banner.getSpec().setSource("custom");

        var saved = service.create(banner).block();

        assertThat(saved.getSpec().getSource()).isEqualTo("post");
        assertThat(saved.getSpec().getContent()).isNull();
        assertThat(saved.getMetadata().getName()).startsWith("banner-");
    }

    @Test
    void createFromPostWithoutTitleSucceedsWithSnapshot() {
        when(client.fetch(eq(PostRef.class), eq("p1")))
                .thenReturn(Mono.just(postRef("p1", "文章一", "admin")));
        when(client.fetch(eq(User.class), eq("admin")))
                .thenReturn(Mono.just(user("admin", "管理员", "https://avatar")));
        when(client.create(any(Banner.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // 新建-从文章批量创建仅提交 postId，title 为空也应先快照再通过
        var banner = new Banner();
        banner.setSpec(new Banner.BannerSpec());
        banner.getSpec().setPostId("p1");

        var saved = service.create(banner).block();

        assertThat(saved.getSpec().getTitle()).isEqualTo("文章一");
        assertThat(saved.getSpec().getCover()).isEqualTo("https://cover-post");
        assertThat(saved.getSpec().getSource()).isEqualTo("post");
    }

    @Test
    void createWithoutPostIdMarksSourceCustom() {
        when(client.create(any(Banner.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var banner = banner(null);
        banner.getSpec().setCover("https://cover");

        var saved = service.create(banner).block();

        assertThat(saved.getSpec().getSource()).isEqualTo("custom");
    }

    @Test
    void createCustomWithoutCoverRejects() {
        var banner = banner(null);
        banner.getSpec().setCover(null);

        assertThatThrownBy(() -> service.create(banner).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("封面图不能为空");
    }

    @Test
    void createWithoutTitleRejects() {
        var banner = new Banner();
        banner.setSpec(new Banner.BannerSpec());

        assertThatThrownBy(() -> service.create(banner).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("标题不能为空");
    }

    // ===== 文章快照（决策 D3）：Post/User 回填 =====

    @Test
    void snapshotFillsTitleCoverDateAndAuthorFromPostAndUser() {
        when(client.fetch(eq(PostRef.class), eq("p1")))
                .thenReturn(Mono.just(postRef("p1", "文章一", "admin")));
        when(client.fetch(eq(User.class), eq("admin")))
                .thenReturn(Mono.just(user("admin", "管理员", "https://avatar")));
        when(client.create(any(Banner.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var banner = banner(null);
        banner.getSpec().setPostId("p1");
        banner.getSpec().setCover("");

        var saved = service.create(banner).block();

        assertThat(saved.getSpec().getTitle()).isEqualTo("文章一");
        assertThat(saved.getSpec().getCover()).isEqualTo("https://cover-post");
        assertThat(saved.getSpec().getDate()).isEqualTo("2024-05-25");
        assertThat(saved.getSpec().getAuthorName()).isEqualTo("管理员");
        assertThat(saved.getSpec().getAuthorAvatar()).isEqualTo("https://avatar");
    }

    @Test
    void snapshotFallsBackToOwnerWhenUserMissing() {
        when(client.fetch(eq(PostRef.class), eq("p1")))
                .thenReturn(Mono.just(postRef("p1", "文章一", "admin")));
        when(client.fetch(eq(User.class), eq("admin")))
                .thenReturn(Mono.empty());
        when(client.create(any(Banner.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var banner = banner(null);
        banner.getSpec().setPostId("p1");

        var saved = service.create(banner).block();

        assertThat(saved.getSpec().getAuthorName()).isEqualTo("admin");
        assertThat(saved.getSpec().getAuthorAvatar()).isNull();
    }

    @Test
    void snapshotRejectsWhenPostMissing() {
        when(client.fetch(eq(PostRef.class), eq("gone")))
                .thenReturn(Mono.empty());

        var banner = banner(null);
        banner.getSpec().setPostId("gone");

        assertThatThrownBy(() -> service.create(banner).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文章不存在");
    }

    // ===== 列表：来源筛选参数 + 关键字过滤 + 排序映射 =====

    @Test
    void listFiltersKeywordAndBuildsSourceQuery() {
        var post = banner("b1");
        post.getSpec().setSource("post");
        post.getSpec().setTitle("上线公告");
        var custom = banner("b2");
        custom.getSpec().setSource("custom");
        custom.getSpec().setTitle("618 活动");
        when(client.listAll(eq(Banner.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.just(post, custom));

        var result = service.list("post", "上线", 1, 10, "date_desc").block();

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getMetadata().getName()).isEqualTo("b1");
    }

    @Test
    void listSortMappingDateDescUsesSpecDate() {
        when(client.listAll(eq(Banner.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.empty());
        var captor = ArgumentCaptor.forClass(Sort.class);

        service.list("", "", 1, 10, "date_desc").block();

        verify(client).listAll(eq(Banner.class), any(ListOptions.class), captor.capture());
        assertThat(captor.getValue().getOrderFor("spec.date").isDescending()).isTrue();
    }

    @Test
    void listSortMappingManualUsesPriority() {
        when(client.listAll(eq(Banner.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.empty());
        var captor = ArgumentCaptor.forClass(Sort.class);

        service.list("", "", 1, 10, "manual").block();

        verify(client).listAll(eq(Banner.class), any(ListOptions.class), captor.capture());
        assertThat(captor.getValue().getOrderFor("spec.priority").isDescending()).isTrue();
    }

    // ===== 排序（names[0] 最靠前，priority 从大到小） =====

    @Test
    void sortAssignsPriorityByOrder() {
        when(client.fetch(eq(Banner.class), eq("b1")))
                .thenReturn(Mono.just(banner("b1")));
        when(client.fetch(eq(Banner.class), eq("b2")))
                .thenReturn(Mono.just(banner("b2")));
        when(client.update(any(Banner.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        service.sort(List.of("b1", "b2")).block();

        verify(client, times(2)).update(any(Banner.class));
    }

    // ===== 同步文章快照 =====

    @Test
    void syncSnapshotRefreshesPostSnapshot() {
        var existing = banner("b1");
        existing.getSpec().setPostId("p1");
        existing.getSpec().setSource("post");
        existing.getSpec().setTitle("旧标题");
        when(client.fetch(eq(Banner.class), eq("b1"))).thenReturn(Mono.just(existing));
        when(client.fetch(eq(PostRef.class), eq("p1")))
                .thenReturn(Mono.just(postRef("p1", "文章一（已更新）", "admin")));
        when(client.fetch(eq(User.class), eq("admin")))
                .thenReturn(Mono.just(user("admin", "管理员", "https://avatar")));
        when(client.update(any(Banner.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var updated = service.syncSnapshot("b1").block();

        assertThat(updated.getSpec().getTitle()).isEqualTo("文章一（已更新）");
        assertThat(updated.getSpec().getDate()).isEqualTo("2024-05-25");
        assertThat(updated.getSpec().getAuthorName()).isEqualTo("管理员");
        assertThat(updated.getSpec().getAuthorAvatar()).isEqualTo("https://avatar");
    }

    @Test
    void syncSnapshotRejectsCustomSource() {
        var existing = banner("b1");
        existing.getSpec().setSource("custom");
        when(client.fetch(eq(Banner.class), eq("b1"))).thenReturn(Mono.just(existing));

        assertThatThrownBy(() -> service.syncSnapshot("b1").block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("仅文章来源");
    }

    // ===== 公开列表脱敏（决策 D5）：不含 content/remark =====

    @Test
    void listPublicMasksContentAndRemark() {
        var banner = banner("b1");
        banner.getSpec().setContent("<p>正文</p>");
        banner.getSpec().setRemark("内部备注");
        banner.getSpec().setSource("custom");
        when(client.listAll(eq(Banner.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.just(banner));

        var items = service.listPublic().block();

        assertThat(items).hasSize(1);
        var vo = items.get(0);
        assertThat(vo.getName()).isEqualTo("b1");
        assertThat(vo.getSource()).isEqualTo("custom");
        // 脱敏视图本身不含 content/remark 字段（决策 D5）
        var fieldNames = Arrays.stream(BannerListVo.class.getDeclaredFields())
                .map(Field::getName)
                .toList();
        assertThat(fieldNames).doesNotContain("content", "remark");
    }

    // ===== 公开详情：含 content =====

    @Test
    void getPublicByNameReturnsDetailWithContent() {
        var banner = banner("b1");
        banner.getSpec().setContent("<p>详情</p>");
        when(client.fetch(eq(Banner.class), eq("b1")))
                .thenReturn(Mono.just(banner));

        var detail = service.getPublicByName("b1").block();

        assertThat(detail.getContent()).isEqualTo("<p>详情</p>");
        assertThat(detail.getName()).isEqualTo("b1");
    }

    // ===== 工具 =====

    private static Banner banner(String name) {
        var banner = new Banner();
        banner.setMetadata(name == null ? new Metadata() : name(name));
        var spec = new Banner.BannerSpec();
        spec.setTitle("标题");
        spec.setCover("https://cover");
        banner.setSpec(spec);
        return banner;
    }

    private static PostRef postRef(String name, String title, String owner) {
        var ref = new PostRef();
        ref.setMetadata(name(name));
        var spec = new PostRef.PostRefSpec();
        spec.setTitle(title);
        spec.setCover("https://cover-post");
        spec.setPublishTime("2024-05-25");
        spec.setPublish(true);
        spec.setOwner(owner);
        ref.setSpec(spec);
        return ref;
    }

    private static User user(String name, String displayName, String avatar) {
        var user = new User();
        user.setMetadata(name(name));
        var spec = new User.UserSpec();
        spec.setDisplayName(displayName);
        spec.setAvatar(avatar);
        user.setSpec(spec);
        return user;
    }

    private static Metadata name(String name) {
        var metadata = new Metadata();
        metadata.setName(name);
        return metadata;
    }
}
