package cn.ialley.unihalo.services.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import cn.ialley.unihalo.scheme.LoveAlbum;
import cn.ialley.unihalo.services.LoveAlbumService;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

import static run.halo.app.extension.index.query.Queries.isNull;

/**
 * 恋爱相册服务实现（自研相册，含 BCrypt 密码）
 *
 * <p>所有对外返回的相册实体均执行 {@link #maskPassword(LoveAlbum)}：
 * passwordHash 置空不回显，并计算 status.photoCount。</p>
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class LoveAlbumServiceImpl implements LoveAlbumService {

    private final ReactiveExtensionClient client;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Mono<ListResult<LoveAlbum>> list(String keyword, int page, int size) {
        return client.listAll(LoveAlbum.class, new ListOptions(),
                        Sort.by(Sort.Direction.DESC, "metadata.creationTimestamp"))
                .filter(album -> matchesKeyword(album, keyword))
                .map(this::maskPassword)
                .collectList()
                .map(list -> new ListResult<>(page, size, list.size(), slice(list, page, size)));
    }

    @Override
    public Mono<ListResult<LoveAlbum>> listPublic(int page, int size) {
        // 公开读路径：排除删除中对象（决策 D7）
        return client.listAll(LoveAlbum.class,
                        ListOptions.builder()
                                .fieldQuery(isNull("metadata.deletionTimestamp"))
                                .build(),
                        Sort.by(Sort.Direction.DESC, "metadata.creationTimestamp"))
                .map(this::maskPassword)
                .collectList()
                .map(list -> new ListResult<>(page, size, list.size(), slice(list, page, size)));
    }

    @Override
    public Mono<LoveAlbum> getByName(String name) {
        return client.fetch(LoveAlbum.class, name)
                .map(this::maskPassword);
    }

    @Override
    public Mono<LoveAlbum> create(LoveAlbum loveAlbum, String password) {
        return Mono.defer(() -> {
            if (loveAlbum.getSpec() == null || isBlank(loveAlbum.getSpec().getDisplayName())) {
                return Mono.error(new IllegalArgumentException("相册名称不能为空"));
            }
            Metadata metadata = new Metadata();
            metadata.setName(generateName());
            metadata.setCreationTimestamp(Instant.now());
            loveAlbum.setMetadata(metadata);
            applyPassword(loveAlbum.getSpec(), password, false);
            if (loveAlbum.getSpec().getPhotos() == null) {
                loveAlbum.getSpec().setPhotos(List.of());
            }
            return client.create(loveAlbum);
        }).map(this::maskPassword);
    }

    @Override
    public Mono<LoveAlbum> update(String name, LoveAlbum loveAlbum, String password,
            boolean passwordRemoved) {
        return client.fetch(LoveAlbum.class, name)
                .flatMap(existing -> {
                    if (loveAlbum.getSpec() == null
                            || isBlank(loveAlbum.getSpec().getDisplayName())) {
                        return Mono.error(new IllegalArgumentException("相册名称不能为空"));
                    }
                    String existingHash = existing.getSpec() == null
                            ? null : existing.getSpec().getPasswordHash();
                    existing.setSpec(loveAlbum.getSpec());
                    applyPassword(existing.getSpec(), password, passwordRemoved);
                    // 未重设密码时保留原哈希
                    if (!passwordRemoved && isBlank(password)) {
                        existing.getSpec().setPasswordHash(existingHash);
                        existing.getSpec().setPasswordEnabled(!isBlank(existingHash));
                    }
                    if (existing.getSpec().getPhotos() == null) {
                        existing.getSpec().setPhotos(List.of());
                    }
                    return client.update(existing);
                })
                .map(this::maskPassword);
    }

    @Override
    public Mono<Void> delete(String name) {
        return client.fetch(LoveAlbum.class, name)
                .flatMap(client::delete)
                .then();
    }

    @Override
    public Mono<LoveAlbum> addPhoto(String name, LoveAlbum.AlbumPhoto photo) {
        return client.fetch(LoveAlbum.class, name)
                .flatMap(album -> {
                    if (photo == null || isBlank(photo.getUrl())) {
                        return Mono.error(new IllegalArgumentException("图片地址不能为空"));
                    }
                    var spec = album.getSpec();
                    if (spec.getPhotos() == null) {
                        spec.setPhotos(new ArrayList<>());
                    }
                    if (isBlank(photo.getName())) {
                        photo.setName(UUID.randomUUID().toString().replace("-", ""));
                    }
                    spec.getPhotos().add(photo);
                    return client.update(album);
                })
                .map(this::maskPassword);
    }

    @Override
    public Mono<LoveAlbum> updatePhotos(String name, List<LoveAlbum.AlbumPhoto> photos) {
        return client.fetch(LoveAlbum.class, name)
                .flatMap(album -> {
                    album.getSpec().setPhotos(photos == null ? List.of() : photos);
                    return client.update(album);
                })
                .map(this::maskPassword);
    }

    @Override
    public Mono<LoveAlbum> deletePhoto(String name, String photoName) {
        return client.fetch(LoveAlbum.class, name)
                .flatMap(album -> {
                    var photos = album.getSpec().getPhotos();
                    if (photos != null) {
                        photos.removeIf(photo -> photoName.equals(photo.getName()));
                    }
                    return client.update(album);
                })
                .map(this::maskPassword);
    }

    @Override
    public Mono<Boolean> verifyPassword(String name, String password) {
        return client.fetch(LoveAlbum.class, name)
                .map(album -> {
                    var spec = album.getSpec();
                    if (spec == null || !Boolean.TRUE.equals(spec.getPasswordEnabled())
                            || isBlank(spec.getPasswordHash())) {
                        return false;
                    }
                    return passwordEncoder.matches(
                            password == null ? "" : password, spec.getPasswordHash());
                })
                .defaultIfEmpty(false);
    }

    /**
     * 应用密码：明文非空=重设并启用；passwordRemoved=清除；否则保持（仅 update 场景使用）。
     */
    private void applyPassword(LoveAlbum.LoveAlbumSpec spec, String password,
            boolean passwordRemoved) {
        if (passwordRemoved) {
            spec.setPasswordHash(null);
            spec.setPasswordEnabled(false);
        } else if (!isBlank(password)) {
            spec.setPasswordHash(passwordEncoder.encode(password));
            spec.setPasswordEnabled(true);
        }
    }

    /**
     * 对外脱敏：passwordHash 置空 + 计算 photoCount。
     */
    private LoveAlbum maskPassword(LoveAlbum album) {
        if (album.getSpec() != null) {
            album.getSpec().setPasswordHash(null);
        }
        if (album.getStatus() == null) {
            album.setStatus(new LoveAlbum.LoveAlbumStatus());
        }
        int count = album.getSpec() != null && album.getSpec().getPhotos() != null
                ? album.getSpec().getPhotos().size()
                : 0;
        album.getStatus().setPhotoCount(count);
        return album;
    }

    private static boolean matchesKeyword(LoveAlbum album, String keyword) {
        if (isBlank(keyword)) {
            return true;
        }
        if (album.getSpec() == null) {
            return false;
        }
        String displayName = album.getSpec().getDisplayName();
        String description = album.getSpec().getDescription();
        return (displayName != null && displayName.contains(keyword))
                || (description != null && description.contains(keyword));
    }

    private static <T> List<T> slice(List<T> list, int page, int size) {
        int from = Math.min((page - 1) * size, list.size());
        int to = Math.min(from + size, list.size());
        return list.subList(from, to);
    }

    private static String generateName() {
        return "lovealbum-" + System.currentTimeMillis() + "-"
                + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
