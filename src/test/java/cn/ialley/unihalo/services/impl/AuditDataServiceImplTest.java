package cn.ialley.unihalo.services.impl;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.enums.CandidateType;
import cn.ialley.unihalo.scheme.AuditDataConfig;
import cn.ialley.unihalo.scheme.CategoryRef;
import cn.ialley.unihalo.scheme.PhotoGroupRef;
import cn.ialley.unihalo.scheme.PostRef;
import cn.ialley.unihalo.vo.AuditDataCandidateResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 审核配置服务单元测试：单例读写、失效引用剔除、候选查询（映射/过滤/分页/插件缺失容错）。
 *
 * @author 小莫唐尼
 */
@ExtendWith(MockitoExtension.class)
class AuditDataServiceImplTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    SchemeManager schemeManager;

    @InjectMocks
    AuditDataServiceImpl service;

    // ===== 读取 =====

    @Test
    void getWhenNotExistsReturnsDefault() {
        when(client.fetch(eq(AuditDataConfig.class), eq(Constants.AUDIT_DATA_CONFIG_SINGLETON_NAME)))
                .thenReturn(Mono.empty());

        var config = service.get().block();

        assertThat(config).isNotNull();
        assertThat(config.getMetadata().getName()).isEqualTo(Constants.AUDIT_DATA_CONFIG_SINGLETON_NAME);
        assertThat(config.getSpec()).isNotNull();
    }

    @Test
    void getWhenExistsReturnsAsIs() {
        var existing = new AuditDataConfig();
        existing.setSpec(new AuditDataConfig.AuditDataConfigSpec());
        existing.getSpec().setDescription("微信审核");
        when(client.fetch(eq(AuditDataConfig.class), eq(Constants.AUDIT_DATA_CONFIG_SINGLETON_NAME)))
                .thenReturn(Mono.just(existing));

        var config = service.get().block();

        assertThat(config.getSpec().getDescription()).isEqualTo("微信审核");
    }

    // ===== 保存：创建 + 失效剔除 =====

    @Test
    void saveWhenNotExistsCreatesAndDropsInvalidRefs() {
        when(client.fetch(eq(AuditDataConfig.class), eq(Constants.AUDIT_DATA_CONFIG_SINGLETON_NAME)))
                .thenReturn(Mono.empty());
        // 文章：p1 存在，gone 已删除（引用失效）
        when(client.fetch(eq(PostRef.class), eq("p1")))
                .thenReturn(Mono.just(newPostRef("p1")));
        when(client.fetch(eq(PostRef.class), eq("gone")))
                .thenReturn(Mono.empty());
        when(client.create(any(AuditDataConfig.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var config = new AuditDataConfig();
        var spec = new AuditDataConfig.AuditDataConfigSpec();
        spec.setPosts(List.of(ref("p1", "文章一"), ref("gone", "已删除文章")));
        config.setSpec(spec);

        var saved = service.save(config).block();

        verify(client).create(any(AuditDataConfig.class));
        assertThat(saved.getSpec().getPosts()).hasSize(1);
        assertThat(saved.getSpec().getPosts().get(0).getName()).isEqualTo("p1");
        assertThat(saved.getSpec().getPosts().get(0).getTitle()).isEqualTo("文章一");
    }

    // ===== 保存：更新 =====

    @Test
    void saveWhenExistsUpdates() {
        var existing = new AuditDataConfig();
        existing.setMetadata(new Metadata());
        existing.setSpec(new AuditDataConfig.AuditDataConfigSpec());
        when(client.fetch(eq(AuditDataConfig.class), eq(Constants.AUDIT_DATA_CONFIG_SINGLETON_NAME)))
                .thenReturn(Mono.just(existing));
        // 分类 c1 存在（非空列表会触发存在性校验）
        when(client.fetch(eq(CategoryRef.class), eq("c1")))
                .thenReturn(Mono.just(categoryRef("c1")));
        when(client.update(any(AuditDataConfig.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var config = new AuditDataConfig();
        var spec = new AuditDataConfig.AuditDataConfigSpec();
        spec.setCategories(List.of(ref("c1", "分类一")));
        config.setSpec(spec);

        var saved = service.save(config).block();

        verify(client).update(any(AuditDataConfig.class));
        verify(client, never()).create(any(AuditDataConfig.class));
        assertThat(saved.getSpec().getCategories()).hasSize(1);
        assertThat(saved.getSpec().getCategories().get(0).getName()).isEqualTo("c1");
    }

    // ===== 候选查询：插件缺失容错 =====

    @Test
    void listCandidatesWhenPluginMissingReturnsEmptyAndFlag() {
        // schemes() 默认为空集合 → 对应 GVK 未注册 → pluginMissing
        var result = service.listCandidates(CandidateType.galleryGroup, "", 1, 10).block();

        assertThat(result.pluginMissing()).isTrue();
        assertThat(result.items()).isEmpty();
        assertThat(result.total()).isZero();
    }

    // ===== 候选查询：post 映射 / 关键字过滤 / 分页 =====

    @Test
    void listCandidatesPostMapsFiltersAndPages() {
        when(schemeManager.schemes())
                .thenReturn(List.of(Scheme.buildFromType(PostRef.class)));
        // p1 标题不含关键字、p2 含「审核」以验证关键字过滤
        when(client.listAll(eq(PostRef.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.just(
                        postRef("p1", "uni-halo 开源啦", "2024-05-25"),
                        postRef("p2", "微信小程序审核避坑指南", "2024-06-10")));

        var result = service.listCandidates(CandidateType.post, "审核", 1, 10).block();

        assertThat(result.pluginMissing()).isFalse();
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).getName()).isEqualTo("p2");
        assertThat(result.items().get(0).getTitle()).isEqualTo("微信小程序审核避坑指南");
        assertThat(result.items().get(0).getSubTitle()).isEqualTo("2024-06-10");
    }

    @Test
    void listCandidatesPostPagesBySize() {
        when(schemeManager.schemes())
                .thenReturn(List.of(Scheme.buildFromType(PostRef.class)));
        when(client.listAll(eq(PostRef.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.just(
                        postRef("p1", "文章一", "2024-05-25"),
                        postRef("p2", "文章二", "2024-06-10"),
                        postRef("p3", "文章三", "2024-07-01")));

        var page1 = service.listCandidates(CandidateType.post, "", 1, 2).block();
        var page2 = service.listCandidates(CandidateType.post, "", 2, 2).block();

        assertThat(page1.total()).isEqualTo(3);
        assertThat(page1.items()).hasSize(2);
        assertThat(page2.items()).hasSize(1);
    }

    // ===== 候选查询：galleryGroup 映射（extra=照片数） =====

    @Test
    void listCandidatesGalleryGroupMapsPhotoCount() {
        when(schemeManager.schemes())
                .thenReturn(List.of(Scheme.buildFromType(PhotoGroupRef.class)));
        var group = new PhotoGroupRef();
        group.setMetadata(name("g1"));
        var spec = new PhotoGroupRef.PhotoGroupRefSpec();
        spec.setDisplayName("旅行记录");
        group.setSpec(spec);
        var status = new PhotoGroupRef.PhotoGroupRefStatus();
        status.setPhotoCount(12);
        group.setStatus(status);
        when(client.listAll(eq(PhotoGroupRef.class), any(ListOptions.class), any(Sort.class)))
                .thenReturn(Flux.just(group));

        var result = service.listCandidates(CandidateType.galleryGroup, "", 1, 10).block();

        assertThat(result.pluginMissing()).isFalse();
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).getName()).isEqualTo("g1");
        assertThat(result.items().get(0).getTitle()).isEqualTo("旅行记录");
        assertThat(result.items().get(0).getExtra()).isEqualTo("12 张照片");
    }

    // ===== 工具 =====

    private static AuditDataConfig.AuditDataRef ref(String name, String title) {
        var ref = new AuditDataConfig.AuditDataRef();
        ref.setName(name);
        ref.setTitle(title);
        return ref;
    }

    private static PostRef newPostRef(String name) {
        return postRef(name, "标题 " + name, "2024-05-25");
    }

    private static CategoryRef categoryRef(String name) {
        var ref = new CategoryRef();
        ref.setMetadata(name(name));
        var spec = new CategoryRef.CategoryRefSpec();
        spec.setDisplayName("分类 " + name);
        spec.setSlug(name);
        ref.setSpec(spec);
        return ref;
    }

    private static PostRef postRef(String name, String title, String publishTime) {
        var ref = new PostRef();
        ref.setMetadata(name(name));
        var spec = new PostRef.PostRefSpec();
        spec.setTitle(title);
        spec.setPublishTime(publishTime);
        spec.setCategories(List.of("c1"));
        ref.setSpec(spec);
        return ref;
    }

    private static Metadata name(String name) {
        var metadata = new Metadata();
        metadata.setName(name);
        return metadata;
    }
}
