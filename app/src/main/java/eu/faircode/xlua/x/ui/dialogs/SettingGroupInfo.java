package eu.faircode.xlua.x.ui.dialogs;

import android.content.Context;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import eu.faircode.xlua.R;
import eu.faircode.xlua.x.Str;

/** Resolves the beginner-friendly Russian help text for a Setting Values group. */
public final class SettingGroupInfo {
    private static final Map<String, String> MESSAGE_CACHE = new HashMap<>();

    private SettingGroupInfo() { }

    public static String getMessage(Context context, String groupName) {
        if (context == null)
            return Str.EMPTY;

        String normalizedName = normalizeGroupName(groupName);
        if (Str.isEmpty(normalizedName))
            return context.getString(R.string.description_ru_setting_group_unknown);

        String cached = MESSAGE_CACHE.get(normalizedName);
        if (cached != null)
            return cached;

        String resourceName = "description_ru_setting_group_" + normalizedName;
        int resourceId = context.getResources().getIdentifier(
                resourceName,
                "string",
                context.getPackageName());
        String message = context.getString(resourceId == 0
                ? R.string.description_ru_setting_group_unknown
                : resourceId);
        MESSAGE_CACHE.put(normalizedName, message);
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
