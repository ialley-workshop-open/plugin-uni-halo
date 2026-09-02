package cn.ialley.unihalo.captcha;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * 图形验证码生成器（AWT 绘制，对齐官方 plugin-comment-widget 方案）。
 *
 * <p>支持两种类型：{@link CaptchaType#ALPHANUMERIC}（字符，去掉易混淆字符）、
 * {@link CaptchaType#ARITHMETIC}（算术题，答案是算式结果）。图片输出 PNG
 * base64 data URI。</p>
 *
 * @author 小莫唐尼
 */
public final class CaptchaGenerator {

    /** 去掉易混淆字符（0/O/1/l/I）后的字符集 */
    private static final String CHARS =
            "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";

    private static final Random RANDOM = new Random();

    private CaptchaGenerator() {
    }

    /**
     * 生成的验证码：图片 + 正确答案（答案仅服务端持有，不外发）。
     */
    public record GeneratedCaptcha(BufferedImage image, String code) {
    }

    public static GeneratedCaptcha generateSimpleCaptcha(int length) {
        int len = Math.max(4, length);
        StringBuilder code = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            code.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return new GeneratedCaptcha(draw(code.toString()), code.toString());
    }

    public static GeneratedCaptcha generateMathCaptcha(int range) {
        int r = Math.max(5, range);
        int a = RANDOM.nextInt(r) + 1;
        int b = RANDOM.nextInt(r) + 1;
        boolean plus = RANDOM.nextBoolean();
        int result = plus ? a + b : a - b;
        if (result < 0) {
            plus = true;
            result = a + b;
        }
        String expression = plus
                ? (a + " + " + b + " = ?")
                : (a + " - " + b + " = ?");
        return new GeneratedCaptcha(draw(expression), String.valueOf(result));
    }

    public static String encodeToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("验证码图片生成失败", e);
        }
    }

    private static BufferedImage draw(String text) {
        int width = 40 + text.length() * 18;
        int height = 44;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        // 干扰线
        g.setColor(new Color(200, 200, 200));
        for (int i = 0; i < 5; i++) {
            g.drawLine(RANDOM.nextInt(width), RANDOM.nextInt(height),
                    RANDOM.nextInt(width), RANDOM.nextInt(height));
        }
        // 逐字符绘制（随机颜色 + 轻微旋转）
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        for (int i = 0; i < text.length(); i++) {
            g.setColor(new Color(20 + RANDOM.nextInt(100), 20 + RANDOM.nextInt(100),
                    20 + RANDOM.nextInt(100)));
            Graphics2D g2 = (Graphics2D) g.create();
            g2.rotate((RANDOM.nextDouble() - 0.5) * 0.4, 14 + i * 18, height / 2.0);
            g2.drawString(String.valueOf(text.charAt(i)), 12 + i * 18, height / 2 + 9);
            g2.dispose();
        }
        // 噪点
        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(180 + RANDOM.nextInt(60), 180 + RANDOM.nextInt(60),
                    180 + RANDOM.nextInt(60)));
            g.fillRect(RANDOM.nextInt(width), RANDOM.nextInt(height), 1, 1);
        }
        g.dispose();
        return image;
    }
}
