package cn.ialley.unihalo.utils;

/**
 * 版本号比较工具，移植自 uni-upgrade-center（uni-admin 云函数 checkVersion 的 compare 逻辑）。
 * 支持 "3.0.0.0.0.1.0.1" 与 "3.0.0.1" 等不定长、不定段数版本号的比较。
 *
 * @author 小莫唐尼
 */
public final class VersionComparator {

    private VersionComparator() {
    }

    /**
     * 比较两个版本号。
     *
     * @param v1 版本号 1
     * @param v2 版本号 2
     * @return v1 &gt; v2 返回 1；v1 &lt; v2 返回 -1；相等返回 0
     */
    public static int compare(String v1, String v2) {
        String[] segments1 = split(v1);
        String[] segments2 = split(v2);
        int minLength = Math.min(segments1.length, segments2.length);

        int result = 0;
        for (int i = 0; i < minLength; i++) {
            int curV1 = parse(segments1[i]);
            int curV2 = parse(segments2[i]);
            if (curV1 > curV2) {
                result = 1;
                break;
            }
            if (curV1 < curV2) {
                result = -1;
                break;
            }
        }

        if (result == 0 && segments1.length != segments2.length) {
            boolean v1Bigger = segments1.length > segments2.length;
            String[] longer = v1Bigger ? segments1 : segments2;
            for (int i = minLength; i < longer.length; i++) {
                if (parse(longer[i]) > 0) {
                    result = v1Bigger ? 1 : -1;
                    break;
                }
            }
        }

        return result;
    }

    private static String[] split(String version) {
        if (version == null || version.isBlank()) {
            return new String[] {"0"};
        }
        return version.split("\\.");
    }

    private static int parse(String segment) {
        try {
            return Integer.parseInt(segment.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}