package eu.faircode.xlua.x.ui.dialogs;

import android.content.Context;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.settings.SettingsGlobals;

/** Resolves beginner-friendly Russian help for an individual Setting Values item. */
public final class SettingValueInfo {
    private static final Map<String, String> MESSAGE_CACHE = new HashMap<>();

    private SettingValueInfo() { }

    public static String getMessage(Context context, String settingName) {
        if (context == null)
            return Str.EMPTY;

        String normalizedName = normalizeSettingName(settingName);
        if (!Str.isEmpty(normalizedName)) {
            String cached = MESSAGE_CACHE.get(normalizedName);
            if (cached != null)
                return cached;

            String resourceName = "description_ru_setting_value_" + normalizedName;
            int resourceId = context.getResources().getIdentifier(
                    resourceName,
                    "string",
                    context.getPackageName());
            if (resourceId != 0) {
                String message = context.getString(resourceId);
                MESSAGE_CACHE.put(normalizedName, message);
                return message;
            }
        }

        return context.getString(R.string.description_ru_setting_value_unknown);
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
}
