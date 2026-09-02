package cn.ialley.unihalo.captcha;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 验证码管理实现（轻量内存缓存，零第三方依赖）。
 *
 * <p>缓存为 ConcurrentHashMap（key = 验证码 id），1 分钟过期（写入时记录
 * 过期时间戳，读取/生成时惰性清理），容量上限 100（超出先清理过期项）。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class CaptchaManagerImpl implements CaptchaManager {

    public static final long CODE_EXPIRATION_MINUTES = 1;
    private static final int MAX_CACHE_SIZE = 100;

    private final Map<String, Entry> cache = new ConcurrentHashMap<>();
    private final Supplier<Long> clock;

    public CaptchaManagerImpl() {
        this(System::currentTimeMillis);
    }

    /**
     * 包级可见构造：测试注入可控时钟（验证过期逻辑）。
     */
    CaptchaManagerImpl(Supplier<Long> clock) {
        this.clock = clock;
    }

    @Override
    public Mono<Captcha> generate(CaptchaType type, int captchaLength, int arithmeticRange) {
        return Mono.fromSupplier(() -> {
            evictExpired();
            var generated = switch (type) {
                case ALPHANUMERIC -> CaptchaGenerator.generateSimpleCaptcha(captchaLength);
                case ARITHMETIC -> CaptchaGenerator.generateMathCaptcha(arithmeticRange);
            };
            var id = UUID.randomUUID().toString();
            long expireAt = clock.get() + CODE_EXPIRATION_MINUTES * 60_000L;
            cache.put(id, new Entry(generated.code(), expireAt));
            return new Captcha(id, generated.code(),
                    CaptchaGenerator.encodeToBase64(generated.image()));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> verify(String id, String code, boolean ignoreCase) {
        return Mono.fromCallable(() -> {
            Entry entry = cache.get(id);
            if (entry == null || entry.expireAt() < clock.get()) {
                cache.remove(id);
                return false;
            }
            return ignoreCase
                    ? entry.code().equalsIgnoreCase(code)
                    : entry.code().equals(code);
        });
    }

    @Override
    public Mono<Void> invalidate(String id) {
        cache.remove(id);
        return Mono.empty();
    }

    /**
     * 超出容量上限时清理过期项（惰性）。
     */
    private void evictExpired() {
        if (cache.size() < MAX_CACHE_SIZE) {
            return;
        }
        long now = clock.get();
        cache.entrySet().removeIf(entry -> entry.getValue().expireAt() < now);
    }

    private record Entry(String code, long expireAt) {
    }
}
