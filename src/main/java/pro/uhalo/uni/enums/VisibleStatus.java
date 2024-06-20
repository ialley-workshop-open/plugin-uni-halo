package pro.uhalo.uni.enums;

import lombok.Getter;
import java.io.Serializable;

/**
 * 状态枚举
 *
 * @author 小莫唐尼
 */
@Getter
public enum VisibleStatus  implements Serializable {
    /**
     * 公开
     */
    PUBLIC("public"),

    /**
     * 私有
     */
    PRIVATE("private");

    private final String value;

    VisibleStatus(String value) {
        this.value = value;
    }
}
