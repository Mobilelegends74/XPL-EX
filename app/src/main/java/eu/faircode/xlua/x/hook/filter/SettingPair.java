package eu.faircode.xlua.x.hook.filter;

import android.util.Log;

import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.hook.filter.kinds.IPCCallFilterContainer;
import eu.faircode.xlua.x.hook.filter.kinds.IPCQueryFilterContainer;
import eu.faircode.xlua.x.xlua.LibUtil;

public class SettingPair {
    private static final String TAG = LibUtil.generateTag(SettingPair.class);

    public static boolean isValid(SettingPair pair) { return pair != null && pair.isValid(); }
    public String name;
    public String settingName;
    public String authority;

    public boolean isValid() { return !Str.isEmpty(name) && !Str.isEmpty(settingName); }

    public String createCallSetting() { return FilterContainerElement.createCallSetting(name, authority); }
    public String createCallDirectValue() { return FilterContainerElement.createCallDirectValueName(name, authority); }

    public String createQuerySetting() { return IPCQueryFilterContainer.createArgSetting(name, authority); }
    public String createQueryDirectValue() { return FilterContainerElement.createQueryDirectValueName(name, authority); }


    public boolean isDirectValue() { return !Str.isEmpty(settingName) && settingName.startsWith(IPCCallFilterContainer.DIRECT_VAL_SYMBOL); }


    public SettingPair(String name, int index, List<String> settings, String authority) {
        this.name = name;
        this.authority = Str.toLowerCase(authority);
        if(ListUtil.isValid(settings)) {
            if(settings.size() > index) settingName = settings.get(index);
            else settingName = settings.get(0);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Created Setting Pair [%s] Index [%s] Settings=(%s) Setting Selected [%s] Authority [%s]",
                    name,
                    index,
                    Str.joinList(settings),
                    this.settingName,
                    authority));
    }
}