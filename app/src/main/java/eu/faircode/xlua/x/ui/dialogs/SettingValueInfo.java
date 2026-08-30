package eu.faircode.xlua.x.ui.dialogs;

import android.content.Context;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import eu.faircode.xlua.R;
import eu.faircode.xlua.utilities.AppLanguage;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.settings.SettingsGlobals;

/** Resolves localized help for an individual Setting Values item. */
public final class SettingValueInfo {
    private static final Map<String, String> MESSAGE_CACHE = new HashMap<>();

    private SettingValueInfo() { }

    public static String getMessage(Context context, String settingName) {
        if (context == null)
            return Str.EMPTY;

        String normalizedName = normalizeSettingName(settingName);
        boolean russian = AppLanguage.isRussian(context);
        if (!Str.isEmpty(normalizedName)) {
            String cacheKey = (russian ? "ru:" : "en:") + normalizedName;
            String cached = MESSAGE_CACHE.get(cacheKey);
            if (cached != null)
                return cached;

            String resourceName = (russian ? "description_ru_setting_value_"
                    : "description_en_setting_value_") + normalizedName;
            int resourceId = context.getResources().getIdentifier(
                    resourceName,
                    "string",
                    context.getPackageName());
            if (resourceId != 0) {
                String message = context.getString(resourceId);
                MESSAGE_CACHE.put(cacheKey, message);
                return message;
            }
            if (!russian) {
                String message = englishFallback(normalizedName);
                MESSAGE_CACHE.put(cacheKey, message);
                return message;
            }
        }

        return context.getString(russian
                ? R.string.description_ru_setting_value_unknown
                : R.string.description_en_setting_value_unknown);
    }

    static String normalizeSettingName(String settingName) {
        if (Str.isEmpty(settingName))
            return Str.EMPTY;

        String baseName = SettingsGlobals.getBaseString(settingName.trim());
        return baseName
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    static String englishFallback(String normalizedName) {
        if (Str.isEmpty(normalizedName))
            return "Controls the value Android reports to the selected application.";
        String[] words = normalizedName.split("_+");
        StringBuilder label = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty())
                continue;
            if (label.length() > 0)
                label.append(' ');
            label.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return "Controls the reported " + label
                + " value for the selected application. Keep it consistent with the active device profile.";
    }
}
