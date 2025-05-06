package eu.faircode.xlua.x.xlua.settings;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.R;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.ui.core.util.CoreUiUtils;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class GroupStats {
    public static final List<String> UNIQUE_NAMES = Arrays.asList(
            RandomizersCache.SETTING_APP_CURRENT_INSTALL_TIME_OFFSET,
            RandomizersCache.SETTING_APP_CURRENT_UPDATE_TIME_OFFSET,
            RandomizersCache.SETTING_APP_INSTALL_TIME_OFFSET,
            RandomizersCache.SETTING_APP_UPDATE_TIME_OFFSET,

            RandomizersCache.SETTING_SETTING_BOOT_COUNT,
            RandomizersCache.SETTING_BATTERY_CHARGING_CYCLES,

            RandomizersCache.SETTING_FILE_MODIFY_OFFSET,
            RandomizersCache.SETTING_FILE_ACCESS_OFFSET,
            RandomizersCache.SETTING_FILE_CREATION_OFFSET,
            "bluetooth.name");

    public static GroupStats create() { return new GroupStats(); }

    private int totalSettings = 0;
    private int totalSavedSettings = 0;
    private int totalUnSavedSettings = 0;

    private String label = null;

    public int getTotal() { return totalSettings; }
    public int getTotalSaved() { return totalSavedSettings; }
    public int getTotalUnSaved() { return totalUnSavedSettings; }

    public boolean hasUnsaved() { return totalUnSavedSettings > 0; }

    public GroupStats updateIv(ImageView ivWarning) {
        if(ivWarning != null) {
            ivWarning.setVisibility(hasUnsaved() ? View.VISIBLE : View.GONE);
        }

        return this;
    }

    public static boolean isSettingAndroid(String name) {
        if(Str.isEmpty(name))
            return false;
        String lowered = name.toLowerCase();
        if(lowered.startsWith("android.") || lowered.startsWith("device."))
            return true;

        return false;
    }

    public static boolean isSettingUnique(String name) {
        if(Str.isEmpty(name))
            return false;
        String lowered = name.toLowerCase();
        if(lowered.contains("unique"))
            return true;
        if(lowered.startsWith("settings.xiaomi."))
            return true;
        if(lowered.endsWith(".uuid"))
            return true;
        return UNIQUE_NAMES.contains(lowered);
    }

    public GroupStats updateIv(ImageView ivWarning, String name) {
        if(ivWarning != null) {
            if(isSettingUnique(name)) {
                if(!hasUnsaved()) {
                    ivWarning.setVisibility(View.VISIBLE);
                    ivWarning.setImageResource(R.drawable.ic_finger_print18);
                } else {
                    ivWarning.setVisibility(View.VISIBLE);
                    ivWarning.setImageResource(android.R.drawable.ic_dialog_alert);
                }
            } else {
                ivWarning.setVisibility(hasUnsaved() ?
                        View.VISIBLE :
                        View.GONE);
            }
        }

        return this;
    }

    public GroupStats updateColor(TextView tvLabel, Context context) {
        if(tvLabel != null) {
            int c = hasUnsaved() ?  R.attr.colorUnsavedSetting : totalSavedSettings > 0 ? R.attr.colorAccent : R.attr.colorTextOne;
            int color = XUtil.resolveColor(context, c);
            CoreUiUtils.setTextColor(tvLabel, color, false);
        }

        return this;
    }

    public GroupStats updateLabel(TextView tvLabel) {
        if(tvLabel != null) CoreUiUtils.setText(tvLabel, getLabel());
        return this;
    }

    public String getLabel() {
        if(Str.isEmpty(label)) {
            if(totalSettings < 1) label = "---";
            else label = Str.combineEx(String.valueOf(totalSavedSettings), Str.FORWARD_SLASH, String.valueOf(totalSettings));
        }

        return label;
    }

    public GroupStats update(SettingsContainer container) { return update(container, true); }
    public GroupStats update(SettingsContainer container, boolean updateFlag) {
        if(container != null && updateFlag) {
            reset();
            for(SettingHolder holder : container.getSettings()) {
                totalSettings++;
                if(holder.isNotSaved())
                    totalUnSavedSettings++;
                if(holder.hasValue(true))
                    totalSavedSettings++;
            }
        }

        return this;
    }

    public GroupStats update(SettingsGroup group) { return update(group, true); }
    public GroupStats update(SettingsGroup group, boolean updateFlag) {
        if(group != null && updateFlag) {
            reset();
            for(SettingsContainer container : group.getContainers()) {
                for(SettingHolder holder : container.getSettings()) {
                    totalSettings++;
                    if(holder.isNotSaved())
                        totalUnSavedSettings++;
                    if(holder.hasValue(true))
                        totalSavedSettings++;
                }
            }
        }

        return this;
    }

    public GroupStats reset() {
        totalSettings = 0;
        totalSavedSettings = 0;
        totalUnSavedSettings = 0;
        label = null;
        return this;
    }
}
