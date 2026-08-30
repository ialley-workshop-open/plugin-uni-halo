package cn.ialley.unihalo.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

/**
 * 恋爱相册解锁 token 工具（HMAC-SHA256 无状态签名）。
 *
 * <p>token 格式：base64url(albumName).expiry.hex(signature)。verify 时校验
 * 相册名匹配、签名一致且未过期（默认有效期 30 分钟）。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class AlbumTokenManager {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * 签名密钥：插件内置常量。token 为短期访问凭证，密钥不随插件配置暴露。
     */
    private static final String SECRET = "uni-halo-love-album-token-secret-v1";

    private static final long TTL_MILLIS = 30 * 60 * 1000L;

    /**
     * 为指定相册签发解锁 token。
     */
    public String issue(String albumName) {
        long expiry = System.currentTimeMillis() + TTL_MILLIS;
        String payload = albumName + "." + expiry;
        String encodedAlbum = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(albumName.getBytes(StandardCharsets.UTF_8));
        return encodedAlbum + "." + expiry + "." + sign(payload);
    }

    /**
     * 校验 token 是否有效且属于指定相册。
     */
    public boolean verify(String albumName, String token) {
        if (albumName == null || token == null || token.isBlank()) {
            return false;
        }
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            String decodedAlbum = new String(
                    Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            long expiry = Long.parseLong(parts[1]);
            if (!albumName.equals(decodedAlbum)) {
                return false;
            }
            if (System.currentTimeMillis() >= expiry) {
                return false;
            }
            String expected = sign(decodedAlbum + "." + expiry);
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    private static String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 签名失败", e);
        }
    }
}
