package cn.ialley.unihalo.services;

import java.util.List;

import cn.ialley.unihalo.scheme.LoveAlbum;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

/**
 * 恋爱相册服务（自研相册，含密码）
 *
 * @author 小莫唐尼
 */
public interface LoveAlbumService {

    /**
     * 分页列表（含 photoCount 计算；passwordHash 一律置空不回显）。
     */
    Mono<ListResult<LoveAlbum>> list(String keyword, int page, int size);

    Mono<LoveAlbum> getByName(String name);

    /**
     * 新建相册；password 非空则 BCrypt 哈希存储并置 passwordEnabled=true。
     */
    Mono<LoveAlbum> create(LoveAlbum loveAlbum, String password);

    /**
     * 更新相册；password 为空=保持原密码，明文=重设，passwordRemoved=true=清除。
     */
    Mono<LoveAlbum> update(String name, LoveAlbum loveAlbum, String password, boolean passwordRemoved);

    Mono<Void> delete(String name);

    /**
     * 添加照片（自动生成照片 name）。
     */
    Mono<LoveAlbum> addPhoto(String name, LoveAlbum.AlbumPhoto photo);

    /**
     * 整体替换照片列表（用于排序/批量修改）。
     */
    Mono<LoveAlbum> updatePhotos(String name, List<LoveAlbum.AlbumPhoto> photos);

    Mono<LoveAlbum> deletePhoto(String name, String photoName);

    /**
     * 校验相册密码（BCrypt matches）。
     */
    Mono<Boolean> verifyPassword(String name, String password);
}
