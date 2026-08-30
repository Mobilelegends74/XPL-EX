package eu.faircode.xlua.x.ui.dialogs;

import android.content.Context;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import eu.faircode.xlua.R;
import eu.faircode.xlua.utilities.AppLanguage;
import eu.faircode.xlua.x.Str;

/** Resolves localized beginner-friendly help for a Setting Values group. */
public final class SettingGroupInfo {
    private static final Map<String, String> MESSAGE_CACHE = new HashMap<>();

    private SettingGroupInfo() { }

    public static String getMessage(Context context, String groupName) {
        if (context == null)
            return Str.EMPTY;

        String normalizedName = normalizeGroupName(groupName);
        boolean russian = AppLanguage.isRussian(context);
        int unknown = russian ? R.string.description_ru_setting_group_unknown
                : R.string.description_en_setting_group_unknown;
        if (Str.isEmpty(normalizedName))
            return context.getString(unknown);

        String cacheKey = (russian ? "ru:" : "en:") + normalizedName;
        String cached = MESSAGE_CACHE.get(cacheKey);
        if (cached != null)
            return cached;

        String resourceName = (russian ? "description_ru_setting_group_"
                : "description_en_setting_group_") + normalizedName;
        int resourceId = context.getResources().getIdentifier(
                resourceName,
                "string",
                context.getPackageName());
        String message = context.getString(resourceId == 0 ? unknown : resourceId);
        MESSAGE_CACHE.put(cacheKey, message);
        return message;
    }

    static String normalizeGroupName(String groupName) {
        if (Str.isEmpty(groupName))
            return Str.EMPTY;
        return groupName
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
