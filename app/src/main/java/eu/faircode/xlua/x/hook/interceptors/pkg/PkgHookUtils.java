package eu.faircode.xlua.x.hook.interceptors.pkg;

import android.util.Log;

import eu.faircode.xlua.XParam;
import eu.faircode.xlua.utilities.DateTimeUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.hook.interceptors.file.FileTimeInterceptor;
import eu.faircode.xlua.x.xlua.LibUtil;

public class PkgHookUtils {
    private static final String TAG = LibUtil.generateTag(PkgHookUtils.class);

    public static final String RAND_ONCE = "%random.once%";
    public static final String RAND_ALWAYS = "%random.always%";

    public static final String NOW_ONCE = "%now.once%";
    public static final String NOW_ALWAYS = "%now.always%";

    public static final String INSTALL_CURRENT_OFFSET_SETTING = "apps.current.install.time.offset";
    public static final String UPDATE_CURRENT_OFFSET_SETTING = "apps.current.update.time.offset";

    public static final String INSTALL_OFFSET_SETTING = "apps.install.time.offset";
    public static final String UPDATE_OFFSET_SETTING = "apps.update.time.offset";

    public static final String INSTALL_GROUP = "installTime";
    public static final String UPDATE_GROUP = "updateTime";


    public static long getTime(
            String groupName,
            String packageName,
            String settingValue,
            GroupedMap map,
            long defValue,
            long originalValue) {

        if(Str.isEmpty(settingValue)) {
            map.pushValueLong(groupName, packageName, originalValue + defValue);
            return originalValue + defValue;
        }

        if(RAND_ALWAYS.equalsIgnoreCase(settingValue))
            return originalValue + defValue;    //Wait what ?

        if(NOW_ALWAYS.equalsIgnoreCase(settingValue))
            return System.currentTimeMillis();

        long val = map.getValueLong(groupName, packageName, false);
        if(val > 0)
            return val;

        if(NOW_ONCE.equalsIgnoreCase(settingValue)) {
            long now = System.currentTimeMillis() - RandomGenerator.nextInt(300, 800);
            map.pushValueLong(groupName, packageName, now);
            return now;
        }

        if(RAND_ONCE.equalsIgnoreCase(settingValue)) {
            long fake = originalValue + defValue;
            map.pushValueLong(groupName, packageName, fake);
            return fake;
        }

        try {
            long[] iTimes = DateTimeUtil.toTimeSpecs(settingValue);
            long seconds = iTimes[0];
            long off = seconds * 1000; // Convert seconds to milliseconds
            long newValue = originalValue + off;
            map.pushValueLong(groupName, packageName, newValue);
            return newValue;
        }catch (Exception e) {
            Log.e(TAG, "Failed to get time, error=" + e + " Pkg=" + packageName + " Value=" + settingValue + " Group=" + groupName);
            long def = originalValue + defValue;
            map.pushValueLong(groupName, packageName, def);
            return def;
        }
    }
}
