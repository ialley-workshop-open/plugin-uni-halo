package pro.uhalo.uni.enums;

import lombok.Getter;

/**
 * 媒体类型枚举
 *
 * @author 小莫唐尼
 */
@Getter
public enum MediaType {
    /**
     * 图片
     */
    PHOTO("photo"),
    /**
     * 视频
     */
    VIDEO("video"),
    /**
     * 音频
     */
    AUDIO("audio");

    private final String value;

    MediaType(String value) {
        this.value = value;
    }
}
