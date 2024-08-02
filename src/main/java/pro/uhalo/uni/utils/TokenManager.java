package pro.uhalo.uni.utils;

/**
 * @author: lywq
 * @date: 2024/07/31 23:10
 * @version: v1.0.0
 * @description:
 **/
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class TokenManager {

    private String token;
    private long expiryTime;

    public synchronized void setToken(String token, long ttlInSeconds) {
        this.token = token;
        this.expiryTime = System.currentTimeMillis() + ttlInSeconds * 1000;
    }

    public synchronized String getToken() {
        if (isTokenExpired()) {
            return null;
        }
        return token;
    }

    private synchronized boolean isTokenExpired() {
        return System.currentTimeMillis() >= expiryTime;
    }

    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public synchronized void checkTokenExpiry() {
        if (isTokenExpired()) {
            this.token = null;
        }
    }
}