package eu.faircode.xlua.x.hook.interceptors.pkg;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.utilities.DateTimeUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.hook.interceptors.zone.RandomDateHelper;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class TimeHookUtils {
    private static final String TAG = LibUtil.generateTag(TimeHookUtils.class);

    public static final long MIN_TIME = 10000000L;
    public static final long NOW_TIME = System.currentTimeMillis();



    public static boolean isCurrentApk(String file, String pkg) { return (file.startsWith("/data/app/") && file.contains("/" + pkg) && file.endsWith("/base.apk")) || file.equalsIgnoreCase(pkg); }


    public static final String RAND_ONCE = "%random.once%";
    public static final String RAND_ALWAYS = "%random.always%";

    public static final String NOW_ONCE = "%now.once%";
    public static final String NOW_ALWAYS = "%now.always%";

    //public static final String INSTALL_GROUP = "installTime";
    //public static final String UPDATE_GROUP = "updateTime";


    /*
        public static final String SETTING_FILE_MODIFY_OFFSET = "files.time.modify.offset";
    public static final String SETTING_FILE_ACCESS_OFFSET = "files.time.access.offset";
    public static final String SETTING_FILE_CREATION_OFFSET = "files.time.created.offset";
    public static final String SETTING_FILES_OFFSET_SUBTRACT = "files.offset.subtract.bool";
    public static final String SETTING_FILE_SYNC_TIME = "files.time.sync.bool";
     */


    public static final String GROUP_CREATION = "creationTime";
    public static final String GROUP_MODIFY = "modifyTime";
    public static final String GROUP_ACCESS = "accessTime";
    public static final String GROUP_CHANGE = "changeTime";

    public static List<String> SUB_GROUPS = Arrays.asList(GROUP_CREATION, GROUP_ACCESS, GROUP_MODIFY, GROUP_CHANGE);

    public static final String GROUP_SYNC_TIME = "syncTime";

    public static final String GROUP_APPS = "appTimes";
    public static final String GROUP_APPS_ORIGINAL = "appTimesOriginal";
    public static final String GROUP_APPS_OFFSETS = "appTimesOffsets";


    public static final String GROUP_FILES = "fileTimes";
    public static final String GROUP_FILES_ORIGINAL = "fileTimesOriginal";
    public static final String GROUP_FILES_OFFSETS = "fileTimesOffsets";

    public static final String INSTALL_OFFSET_SETTING = RandomizersCache.INSTALL_OFFSET_SETTING;
    public static final String UPDATE_OFFSET_SETTING = RandomizersCache.UPDATE_OFFSET_SETTING;

    public static final String SETTING_APP_SYNC_TIME = RandomizersCache.SETTING_APP_SYNC_TIME;
    public static final String SETTING_APP_OFFSET_SUBTRACT = RandomizersCache.SETTING_APP_OFFSET_SUBTRACT;


    public static final String INSTALL_CURRENT_OFFSET_SETTING = RandomizersCache.INSTALL_CURRENT_OFFSET_SETTING;
    public static final String UPDATE_CURRENT_OFFSET_SETTING = RandomizersCache.UPDATE_CURRENT_OFFSET_SETTING;

    public static final String SETTING_FILE_MODIFY_OFFSET = RandomizersCache.SETTING_FILE_MODIFY_OFFSET;
    public static final String SETTING_FILE_ACCESS_OFFSET = RandomizersCache.SETTING_FILE_ACCESS_OFFSET;
    public static final String SETTING_FILE_CREATION_OFFSET = RandomizersCache.SETTING_FILE_CREATION_OFFSET;

    public static final String SETTING_FILES_OFFSET_SUBTRACT = RandomizersCache.SETTING_FILES_OFFSET_SUBTRACT;
    public static final String SETTING_FILE_SYNC_TIME = RandomizersCache.SETTING_FILE_SYNC_TIME;


    public static long finalizeValue(long org, long off, boolean sub,  long now) {
        return
                org < MIN_TIME ? org :
                sub ?
                org - off :
                org + off < now ?
                        org + off :
                        org - off;
    }

    public static String getSettingValueFile(String group, XParam param) {
        if(!Str.isEmpty(group) && param != null) {
            switch (group) {
                case GROUP_CREATION:
                    return param.getSetting(SETTING_FILE_CREATION_OFFSET);
                case GROUP_CHANGE:
                case GROUP_MODIFY:
                    return param.getSetting(SETTING_FILE_MODIFY_OFFSET);
                case GROUP_ACCESS:
                    return param.getSetting(SETTING_FILE_ACCESS_OFFSET);
            }
        }

        return RAND_ALWAYS;
    }

    public static String getSettingValueApp(String group, XParam param, boolean isCurrentApp) {
        if(!Str.isEmpty(group) && param != null) {
            switch (group) {
                case GROUP_CREATION: return param.getSetting(isCurrentApp ?
                        INSTALL_CURRENT_OFFSET_SETTING :
                        INSTALL_OFFSET_SETTING);
                default: return param.getSetting(isCurrentApp ?
                        UPDATE_CURRENT_OFFSET_SETTING :
                        UPDATE_OFFSET_SETTING);
            }
        }

        return RAND_ALWAYS;
    }

    public static String getSettingValue(boolean isApp, String group, XParam param, boolean isCurrentAppIfApp) {
        return isApp ?
                getSettingValueApp(group, param, isCurrentAppIfApp) :
                getSettingValueFile(group, param);
    }

    public static boolean shouldSync(boolean isApp, XParam param) {
        if(param != null) {
            return param.getSettingBool(isApp ?
                    SETTING_APP_SYNC_TIME :
                    SETTING_FILE_SYNC_TIME, false);
        } else {
            return false;
        }
    }

    public static boolean shouldSubtract(boolean isApp, XParam param) {
        if(param != null) {
            return param.getSettingBool(isApp ?
                    SETTING_APP_OFFSET_SUBTRACT :
                    SETTING_FILES_OFFSET_SUBTRACT, true);
        } else {
            return true;
        }
    }

    public static void preInitOffsetsIfNot(String objectName, boolean isApp, boolean subTime, XParam param) {
        GroupedMap offsets = param.getGroupedMap(isApp ?
                GROUP_APPS_OFFSETS :
                GROUP_FILES_OFFSETS);

        long[] times = RandomDateHelper.generateEpochTimeStamps(4, !subTime);
        for(int i = 0; i < times.length; i++ ) {
            long offset = times[i];
            String group = SUB_GROUPS.get(i);
            if(!offsets.hasValue(group, objectName)) {
                offsets.pushValue(group, objectName, offset);
            }
        }
    }

    public  static long getOriginal(String groupName, String objectName, long defaultIfNull, boolean isApp, XParam param) {
        GroupedMap original = param.getGroupedMap(isApp ?
                GROUP_APPS_ORIGINAL :
                GROUP_FILES_ORIGINAL);
        return original.getValueOrDefault(groupName, objectName, defaultIfNull);
    }

    public static long getTimeStamp(
            String groupName,
            String objectName,

            String settingValue,

            boolean syncTime,
            boolean subTime,

            long offset,
            long originalValue,

            boolean isApp,

            XParam param) {

        if(!isApp && isMapsCore(objectName))
            return originalValue;

        //[1] First always cache in Original
        GroupedMap original = param.getGroupedMap(isApp ? GROUP_APPS_ORIGINAL : GROUP_FILES_ORIGINAL);
        GroupedMap modified = param.getGroupedMap(isApp ? GROUP_APPS : GROUP_FILES);

        //This is for if they pre-init offsets
        GroupedMap offsets = param.getGroupedMap(isApp ? GROUP_APPS_OFFSETS : GROUP_FILES_OFFSETS);

        long actualOffset = offsets.getValueOrDefault(groupName, objectName, offset);
        long originalTime = original.getValueOrDefault(groupName, objectName, originalValue);
        long currentTime = System.currentTimeMillis();

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Get Time Stamps Offset=[%s] Original=[%s] Current=[%s] Object Name=[%s] IsApp=%s Sync=%s Sub=%s Group=[%s]",
                    actualOffset,
                    originalTime,
                    currentTime,
                    objectName,
                    isApp,
                    syncTime,
                    subTime,
                    groupName));

        if(RAND_ALWAYS.equalsIgnoreCase(settingValue))
            return finalizeValue(originalTime, offset, subTime, currentTime);
        else if(NOW_ALWAYS.equalsIgnoreCase(settingValue))
            return currentTime - 20000;
        else {
            if(syncTime) {
                if(modified.hasValue(GROUP_SYNC_TIME, objectName)) {
                    return modified.getValueLong(GROUP_SYNC_TIME, objectName);
                } else {
                    if(RAND_ONCE.equalsIgnoreCase(settingValue)) {
                        long newValue = finalizeValue(originalTime, actualOffset, subTime, currentTime);
                        modified.pushValue(GROUP_SYNC_TIME, objectName, newValue);
                        return newValue;
                    }
                    else if(NOW_ONCE.equalsIgnoreCase(settingValue)) {
                        long newValue = currentTime - 20000;
                        modified.pushValue(GROUP_SYNC_TIME, objectName, newValue);
                        return newValue;
                    }
                    else {
                        try {
                            long[] iTimes = DateTimeUtil.toTimeSpecs(settingValue);
                            long seconds = iTimes[0];
                            long off = seconds * 1000; // Convert seconds to milliseconds
                            offsets.pushValue(groupName, objectName, off);
                            long newValue = finalizeValue(originalTime, off, subTime, currentTime);
                            modified.pushValue(GROUP_SYNC_TIME, objectName, newValue);
                            return newValue;
                        }catch (Exception ignored) {
                            long newValue = finalizeValue(originalTime, actualOffset, subTime, currentTime);
                            modified.pushValue(GROUP_SYNC_TIME, objectName, newValue);
                            return newValue;
                        }
                    }
                }
            } else {
                if(modified.hasValue(groupName, objectName)) {
                    return modified.getValueLong(groupName, objectName);
                } else {
                    if(RAND_ONCE.equalsIgnoreCase(settingValue)) {
                        long newValue = finalizeValue(originalTime, actualOffset, subTime, currentTime);
                        modified.pushValue(groupName, objectName, newValue);
                        return newValue;
                    }
                    else if(NOW_ONCE.equalsIgnoreCase(settingValue)) {
                        long newValue = currentTime - 20000;
                        modified.pushValue(groupName, objectName, newValue);
                        return newValue;
                    }
                    else {
                        try {
                            long[] iTimes = DateTimeUtil.toTimeSpecs(settingValue);
                            long seconds = iTimes[0];
                            long off = seconds * 1000; // Convert seconds to milliseconds
                            long newValue = finalizeValue(originalTime, off, subTime, currentTime);
                            offsets.pushValue(groupName, objectName, off);
                            modified.pushValue(groupName, objectName, newValue);
                            return newValue;
                        }catch (Exception ignored) {
                            long newValue = finalizeValue(originalTime, actualOffset, subTime, currentTime);
                            modified.pushValue(groupName, objectName, newValue);
                            return newValue;
                        }
                    }
                }
            }
        }
    }

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

    public static boolean isMapsCore(String file) {
        if(Str.isEmpty(file))
            return false;

        //Make sure NULLs (0) are not Spoofed!
        //  /system/framework/com.android.location.provider.jar (1230768000000)
        //  /system/framework/com.android.media.remotedisplay.jar (1230768000000)

        //  /data/app/~~Qtb8Mr3KrD1JmUIbaEr02A==/com.google.android.gms-Gz8oxnp2qvPKKtd3tew_kg==/base.apk
        //  (1743828852772) =>  Saturday, 5 April 2025 04:54:12.772

        //  /data/app/~~Qtb8Mr3KrD1JmUIbaErO2A==/com.google.android.gms-Gz8oxnp2qvPKKtd3tew_kg==/split_MeasurementDynamite_installtime.apk
        //  (1743828854364) => Saturday, 5 April 2025 04:54:14.364

        //  /product/media/theme/default/com.google.android.gms (0)

        //  /data/user_de/0/com.google.android.gms/app_chimera/current_config.fb
        //  /data/user_de/0/com.google.android.gms/app_chimera/m/000000a5/dl-MapsCoreDynamite.integ_250625400100400.apk
        //  (1743196486846) => Friday, 28 March 2025 21:14:46.846


        /*
                        createdOffset = statMap.getValueOrDefault(GROUP_CREATED, file, times[0], false, true);
                accessOffset = statMap.getValueOrDefault(GROUP_ACCESS, file, times[1], false, true);
                modifiedOffset = statMap.getValueOrDefault(GROUP_MODIFIED, file, times[2], false, true);
                changeOffset = statMap.getValueOrDefault(GROUP_CHANGE, file, times[3], false, true);
         */
        //RandomDateHelper.generateSecondsInMilliseconds(5, 2000);

        //File.lastModified(/data/app/~~O7rUjcoz9oKfxIKK4-I29A==/com.google.android.gms-Y_u0GvzA9Xzdd9pTsctOlw==/split_MeasurementDynamite_installtime.apk) Interceptor File=(/data/app/~~O7rUjcoz9oKfxIKK4-I29A==/com.google.android.gms-Y_u0GvzA9Xzdd9pTsctOlw==/split_MeasurementDynamite_installtime.apk) Offset=105613860 Original MS=1745120318580 Fake MS=1745225932440

        boolean res = (file.startsWith("/data/user_de") && file.contains("/com.google.android.gms/app_chimera/") && file.endsWith(".apk"))
                || (file.startsWith("/data/app") && file.contains("/com.google.android.gms") && file.endsWith("split_MeasurementDynamite_installtime.apk"));
        if(res && DebugUtil.isDebug())
            Log.d(TAG, "GMS Core Chimera APK! File=" + file + " Stack=" + RuntimeUtils.getStackTraceSafeString(new Exception()));
        return res;
    }
}
