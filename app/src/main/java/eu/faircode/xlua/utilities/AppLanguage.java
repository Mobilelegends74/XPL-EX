package eu.faircode.xlua.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Locale;

/** Applies the user-selected UI language without changing Android's system locale. */
public final class AppLanguage {
    public static final String PREF_LANGUAGE = "app_language";
    public static final String SYSTEM = "system";
    public static final String ENGLISH = "en";
    public static final String RUSSIAN = "ru";
    public static final String CHINESE = "zh";

    private AppLanguage() { }

    public static Context wrap(Context base) {
        if (base == null)
            return null;
        Locale locale = toLocale(resolveLanguage(base));
        Configuration configuration = new Configuration(base.getResources().getConfiguration());
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        return base.createConfigurationContext(configuration);
    }

    /**
     * Maps an app language code to the locale used for resource selection.
     * Simplified Chinese must carry the CN region to match values-zh-rCN,
     * because values-zh alone is not present in this project.
     */
    static Locale toLocale(String language) {
        return CHINESE.equals(language) ? Locale.SIMPLIFIED_CHINESE : new Locale(language);
    }

    public static String getSelection(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String selected = preferences.getString(PREF_LANGUAGE, null);
        if (selected == null) {
            // One-time migration from the old Force English switch.
            selected = preferences.getBoolean("forceenglish", false) ? ENGLISH : SYSTEM;
            preferences.edit().putString(PREF_LANGUAGE, selected).remove("forceenglish").apply();
        }
        return normalizeSelection(selected);
    }

    public static void setSelection(Context context, String selection) {
        String normalized = normalizeSelection(selection);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(PREF_LANGUAGE, normalized).apply();
    }

    public static String resolveLanguage(Context context) {
        String selected = getSelection(context);
        if (!SYSTEM.equals(selected))
            return selected;
        Locale system = systemLocale(context);
        return languageForSystem(system.getLanguage());
    }

    public static boolean isRussian(Context context) {
        return RUSSIAN.equals(resolveLanguage(context));
    }

    public static boolean isChinese(Context context) {
        return CHINESE.equals(resolveLanguage(context));
    }

    static String normalizeSelection(String selection) {
        return ENGLISH.equals(selection) || RUSSIAN.equals(selection) || CHINESE.equals(selection)
                ? selection : SYSTEM;
    }

    static String languageForSystem(String language) {
        if (language == null)
            return ENGLISH;
        // Callers may pass either a bare code ("zh") or a full tag ("zh_CN",
        // "zh-Hans-CN"), so compare on the leading language subtag only.
        String tag = language.replace('_', '-');
        int dash = tag.indexOf('-');
        String base = dash < 0 ? tag : tag.substring(0, dash);
        if (RUSSIAN.equalsIgnoreCase(base))
            return RUSSIAN;
        if ("zh".equalsIgnoreCase(base))
            return CHINESE;
        return ENGLISH;
    }

    private static Locale systemLocale(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && !configuration.getLocales().isEmpty())
            return configuration.getLocales().get(0);
        //noinspection deprecation
        return configuration.locale == null ? Locale.ENGLISH : configuration.locale;
    }
}
