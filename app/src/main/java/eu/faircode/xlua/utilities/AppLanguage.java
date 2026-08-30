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

    private AppLanguage() { }

    public static Context wrap(Context base) {
        if (base == null)
            return null;
        Locale locale = new Locale(resolveLanguage(base));
        Configuration configuration = new Configuration(base.getResources().getConfiguration());
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        return base.createConfigurationContext(configuration);
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

    static String normalizeSelection(String selection) {
        return ENGLISH.equals(selection) || RUSSIAN.equals(selection)
                ? selection : SYSTEM;
    }

    static String languageForSystem(String language) {
        return RUSSIAN.equalsIgnoreCase(language) ? RUSSIAN : ENGLISH;
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
