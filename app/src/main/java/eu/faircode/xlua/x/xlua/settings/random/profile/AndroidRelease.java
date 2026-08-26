package eu.faircode.xlua.x.xlua.settings.random.profile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AndroidRelease {
    private static final Map<String, Integer> API_LEVELS;

    static {
        Map<String, Integer> values = new LinkedHashMap<>();
        values.put("12", 31);
        values.put("12L", 32);
        values.put("13", 33);
        values.put("14", 34);
        values.put("15", 35);
        values.put("16", 36);
        API_LEVELS = Collections.unmodifiableMap(values);
    }

    private AndroidRelease() { }

    public static int apiLevel(String release) {
        Integer apiLevel = API_LEVELS.get(release);
        return apiLevel == null ? -1 : apiLevel;
    }

    public static boolean matches(String release, int apiLevel) {
        return apiLevel(release) == apiLevel;
    }
}
