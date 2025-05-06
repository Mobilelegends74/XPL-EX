package eu.faircode.xlua.x.hook.interceptors.file;

import android.content.pm.PackageInfo;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.hook.interceptors.pkg.TimeHookUtils;
import eu.faircode.xlua.x.hook.interceptors.zone.RandomDateHelper;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

public class TimeInterceptor {
    public static final long NOW_TIME = System.currentTimeMillis();
    private static final String TAG = LibUtil.generateTag(TimeInterceptor.class);

    public static TimeInterceptor create(String file, XParam param) { return new TimeInterceptor(file, param); }
    public static TimeInterceptor create(PackageInfo packageInfo, XParam param) { return new TimeInterceptor(packageInfo, param);  }

    /*
    public static final String GROUP_MAP_OFFSET = "stat:offset";
    public static final String GROUP_MAP_ORIGINAL = "stat:original";
    public static final String GROUP_MAP_FIRST = "stat:first";

    public static final String GROUP_FIRST = "first";
    public static final String GROUP_CREATED = "created";
    public static final String GROUP_ACCESS = "access";
    public static final String GROUP_MODIFIED = "modified";
    public static final String GROUP_CHANGE = "changed";
    */

    public String fileOrApp;
    public XParam param;

    private long originalCreated = -1;
    private long originalAccess = -1;
    private long originalModified = -1;
    private long originalChange = -1;

    //private long createdOffset;
    //private long accessOffset;
    //private long modifiedOffset;
    //private long changeOffset;

    private boolean subtract = true;
    private boolean sync = false;
    private boolean isCurrentApk = false;
    private boolean isApp = false;

    public boolean isValid() { return !Str.isEmpty(fileOrApp); }

    //public long getChangeOffset() { return isMapsCore(fileOrApp) ? 0 : changeOffset; }
    //public long getCreatedOffset() { return isMapsCore(fileOrApp) ? 0 : createdOffset; }
    //public long getAccessOffset() { return isMapsCore(fileOrApp) ? 0 : accessOffset; }
    //public long getModifiedOffset() { return isMapsCore(fileOrApp) ? 0 : modifiedOffset; }

    public long getCreation(long original) {
        if(isMapsCore(fileOrApp)) {
            return getOriginalCreated(original);
        } else {
            return TimeHookUtils.getTimeStamp(
                    TimeHookUtils.GROUP_CREATION,
                    fileOrApp,
                    TimeHookUtils.getSettingValue(isApp, TimeHookUtils.GROUP_CREATION, param, isCurrentApk),
                    sync,
                    subtract,
                    RandomDateHelper.generateSeconds(),
                    getOriginalCreated(original),
                    isApp,
                    param);
        }
    }

    public long getAccess(long original) {
        if(isMapsCore(fileOrApp)) {
            return getOriginalAccess(original);
        } else {
            return TimeHookUtils.getTimeStamp(
                    TimeHookUtils.GROUP_ACCESS,
                    fileOrApp,
                    TimeHookUtils.getSettingValue(isApp, TimeHookUtils.GROUP_ACCESS, param, isCurrentApk),
                    sync,
                    subtract,
                    RandomDateHelper.generateSeconds(),
                    getOriginalAccess(original),
                    isApp,
                    param);
        }
    }

    public long getModify(long original) {
        if(isMapsCore(fileOrApp)) {
            return getModify(original);
        } else {
            return TimeHookUtils.getTimeStamp(
                    TimeHookUtils.GROUP_MODIFY,
                    fileOrApp,
                    TimeHookUtils.getSettingValue(isApp, TimeHookUtils.GROUP_MODIFY, param, isCurrentApk),
                    sync,
                    subtract,
                    RandomDateHelper.generateSeconds(),
                    getOriginalModify(original),
                    isApp,
                    param);
        }
    }

    public long getChange(long original) {
        if(isMapsCore(fileOrApp)) {
            return getChange(original);
        } else {
            return TimeHookUtils.getTimeStamp(
                    TimeHookUtils.GROUP_CHANGE,
                    fileOrApp,
                    TimeHookUtils.getSettingValue(isApp, TimeHookUtils.GROUP_CHANGE, param, isCurrentApk),
                    sync,
                    subtract,
                    RandomDateHelper.generateSeconds(),
                    getOriginalChange(original),
                    isApp,
                    param);
        }
    }

    public long getOriginalChange(long val) {
        if(val < 10000000L) {
            return val;
        } else {
            if(originalChange <= 0) originalChange = TimeHookUtils.getOriginal(TimeHookUtils.GROUP_CHANGE, fileOrApp, val, isCurrentApk, param);
            return originalChange;
        }
    }

    public long getOriginalCreated(long val) {
        if(val < 10000000L) {
            return val;
        } else {
            if(originalCreated <= 0) originalCreated = TimeHookUtils.getOriginal(TimeHookUtils.GROUP_CREATION, fileOrApp, val, isCurrentApk, param);
            return originalCreated;
        }
    }

    public long getOriginalAccess(long val) {
        if(val < 10000000L) {
            return val;
        } else {
            if(originalAccess <= 0) originalAccess = TimeHookUtils.getOriginal(TimeHookUtils.GROUP_ACCESS, fileOrApp, val, isCurrentApk, param);
            return originalAccess;
        }
    }

    public long getOriginalModify(long val) {
        if(val < 10000000L) {
            return val;
        } else {
            if(originalModified <= 0) originalModified = TimeHookUtils.getOriginal(TimeHookUtils.GROUP_MODIFY, fileOrApp, val, isCurrentApk, param);
            return originalModified;
        }
    }


    public TimeInterceptor(PackageInfo packageInfo, XParam param) {
        if(packageInfo != null && param != null) {
            this.isCurrentApk = TimeHookUtils.isCurrentApk(packageInfo.packageName, param.getPackageName()) || packageInfo.packageName.equalsIgnoreCase(param.getPackageName());
            this.isApp = true;
            this.fileOrApp = packageInfo.packageName;
            this.param = param;


            //We can also just call to our get(val) function
            getOriginalCreated(packageInfo.firstInstallTime);
            getOriginalAccess(packageInfo.firstInstallTime);
            getOriginalChange(packageInfo.lastUpdateTime);
            getOriginalModify(packageInfo.lastUpdateTime);
            internalInitialize();
        }
    }

    public TimeInterceptor(String file, XParam param) {
        if(!Str.isEmpty(file) && param != null) {
            this.isCurrentApk = TimeHookUtils.isCurrentApk(file, param.getPackageName());
            this.fileOrApp = this.isCurrentApk ?
                    param.getPackageName() :
                    file;
            this.param = param;
            internalInitialize();
            /*if(DebugUtil.isDebug())
                Log.d(TAG, "Created File Time Interceptor For File:" + file +
                        " w=" + this.fileOrApp +
                        " Created Time Offset=" + createdOffset +
                        " Access Time Offset=" + accessOffset +
                        " Modify Time Offset=" + modifiedOffset +
                        " Change Time Offset=" + changeOffset +
                        " Is Apk=" + TimeHookUtils.isCurrentApk(file, param.getPackageName()));*/
        }
    }

    public long getFinalValue(long offset, long original) {
        return offset;
    }

    /*public long getFinalValue(long offset, long original) {
        if(original < 10000000L || isMapsCore(file)) return original;
        if(offset == -3)
            return System.currentTimeMillis();
        if(offset == -2)
            return NOW_TIME;
        if(offset < 1)
            return getFinalValueInternal(original);

        if(!subtract) {
            long time = original + offset;
            if(time > System.currentTimeMillis())
                return getFinalValueInternal(original - offset);

            return getFinalValueInternal(time);
        } else {
            return getFinalValueInternal(original - offset);
        }
    }

    public long getFinalValueInternal(long valueIfInvalidOrNull) {
        if(sync) {
            GroupedMap map = param.getGroupedMap(GROUP_MAP_ORIGINAL);
            return map.getValueOrDefault(GROUP_FIRST, file, valueIfInvalidOrNull, false, true);
        } else {
            return valueIfInvalidOrNull;
        }
    }*/
    public static boolean isMapsCore(String file) {
        if(Str.isEmpty(file))
            return false;

        //
        //
        //
        //  Make sure NULLs (0) are not Spoofed!
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

    private void internalInitialize() {
        this.sync = TimeHookUtils.shouldSync(isApp, this.param);
        this.subtract = TimeHookUtils.shouldSubtract(isApp, this.param);
        TimeHookUtils.preInitOffsetsIfNot(fileOrApp, isApp, this.subtract, this.param);
    }

    //if is 1970 or 69, then sync adjust them, so offset then do offset ?
    /*private void initOffsets() {

        try {
            //PS I think your algo is least to greatest so make sure it makes sense
            GroupedMap statMap = param.getGroupedMap(GROUP_MAP_OFFSET);
            if(!isApk(fileOrApp, param.getPackageName())) {
                //SETTING_FILES_OFFSET_SUBTRACT
                subtract = param.getSettingBool(RandomizersCache.SETTING_FILES_OFFSET_SUBTRACT, true);
                sync = param.getSettingBool(RandomizersCache.SETTING_FILE_SYNC_TIME, false);
                //String offInstallSetting = param.getSetting(PackageInfoInterceptor.INSTALL_CURRENT_OFFSET_SETTING);
                long[] times = RandomDateHelper.generateEpochTimeStamps(4, !subtract);

                createdOffset = statMap.getValueOrDefault(GROUP_CREATED, fileOrApp, times[0], false, true);
                accessOffset = statMap.getValueOrDefault(GROUP_ACCESS, fileOrApp, times[1], false, true);
                modifiedOffset = statMap.getValueOrDefault(GROUP_MODIFIED, fileOrApp, times[2], false, true);
                changeOffset = statMap.getValueOrDefault(GROUP_CHANGE, fileOrApp, times[3], false, true);
                //Copy Below
            } else {
                sync = param.getSettingBool(RandomizersCache.SETTING_FILE_SYNC_TIME, false);


                subtract = param.getSettingBool(RandomizersCache.SETTING_APP_OFFSET_SUBTRACT, true);
                long[] times = RandomDateHelper.generateEpochTimeStamps(4, !subtract);

                String offInstallSetting = param.getSetting(PackageInfoInterceptor.INSTALL_CURRENT_OFFSET_SETTING);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "IsApk, File=" + fileOrApp + " Current Install Offset Setting=" + offInstallSetting);

                if(Str.isEmpty(offInstallSetting)) {
                    createdOffset = statMap.getValueOrDefault(GROUP_CREATED, fileOrApp, times[0], false, true);
                } else {
                    if(PackageInfoInterceptor.NOW_ALWAYS.equalsIgnoreCase(offInstallSetting)) {
                        //createdOffset = System.currentTimeMillis();
                        createdOffset = -3;
                    }
                    else if(PackageInfoInterceptor.RAND_ALWAYS.equalsIgnoreCase(offInstallSetting)) {
                        createdOffset = times[0];
                    }
                    else if(PackageInfoInterceptor.RAND_ONCE.equalsIgnoreCase(offInstallSetting)) {
                        createdOffset = statMap.getValueOrDefault(GROUP_CREATED, fileOrApp, times[0], false, true);
                    }
                    else if(PackageInfoInterceptor.NOW_ONCE.equalsIgnoreCase(offInstallSetting)) {
                        createdOffset = statMap.getValueOrDefault(GROUP_CREATED, fileOrApp, -2, false, true);
                    } else {
                        if(!statMap.hasValue(GROUP_CREATED, fileOrApp)) {
                            createdOffset = statMap.getValueOrDefault(GROUP_CREATED, fileOrApp, DateTimeUtil.toTimeMillis(offInstallSetting, times[0]), false, true);
                        } else {
                            createdOffset = statMap.getValueOrDefault(GROUP_CREATED, fileOrApp, times[0], false, true);
                        }
                    }
                }

                String offUpdateSetting = param.getSetting(PackageInfoInterceptor.UPDATE_CURRENT_OFFSET_SETTING);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "IsApk, File=" + fileOrApp + " Current Update Offset Setting=" + offInstallSetting);

                if(Str.isEmpty(offUpdateSetting)) {
                    accessOffset = statMap.getValueOrDefault(GROUP_ACCESS, fileOrApp, times[1], false, true);
                    modifiedOffset = statMap.getValueOrDefault(GROUP_MODIFIED, fileOrApp, times[2], false, true);
                    changeOffset = statMap.getValueOrDefault(GROUP_CHANGE, fileOrApp, times[3], false, true);
                } else {
                    if(PackageInfoInterceptor.NOW_ALWAYS.equalsIgnoreCase(offUpdateSetting)) {
                        //modifiedOffset = System.currentTimeMillis();
                        modifiedOffset = -3;
                        accessOffset = modifiedOffset;
                        changeOffset = modifiedOffset;
                    }
                    else if(PackageInfoInterceptor.RAND_ALWAYS.equalsIgnoreCase(offUpdateSetting)) {
                        accessOffset = times[1];
                        modifiedOffset = times[2];
                        changeOffset = times[3];
                    }
                    else if(PackageInfoInterceptor.RAND_ONCE.equalsIgnoreCase(offUpdateSetting)) {
                        accessOffset = statMap.getValueOrDefault(GROUP_ACCESS, fileOrApp, times[1], false, true);
                        modifiedOffset = statMap.getValueOrDefault(GROUP_MODIFIED, fileOrApp, times[2], false, true);
                        changeOffset = statMap.getValueOrDefault(GROUP_CHANGE, fileOrApp, times[3], false, true);
                    }
                    else if(PackageInfoInterceptor.NOW_ONCE.equalsIgnoreCase(offUpdateSetting)) {
                        //long now = System.currentTimeMillis();
                        long now = -2;
                        accessOffset = statMap.getValueOrDefault(GROUP_ACCESS, fileOrApp, now, false, true);
                        modifiedOffset = statMap.getValueOrDefault(GROUP_MODIFIED, fileOrApp, now, false, true);
                        changeOffset = statMap.getValueOrDefault(GROUP_CHANGE, fileOrApp, now, false, true);
                    } else {
                        if(!statMap.hasValue(GROUP_ACCESS, fileOrApp)) {
                            accessOffset = statMap.getValueOrDefault(GROUP_ACCESS, fileOrApp, DateTimeUtil.toTimeMillis(offUpdateSetting, times[1]), false, true);
                        } else {
                            accessOffset = statMap.getValueOrDefault(GROUP_ACCESS, fileOrApp, times[1], false, true);
                        }

                        if(!statMap.hasValue(GROUP_MODIFIED, fileOrApp)) {
                            modifiedOffset = statMap.getValueOrDefault(GROUP_MODIFIED, fileOrApp, DateTimeUtil.toTimeMillis(offUpdateSetting, times[2]), false, true);
                        } else {
                            modifiedOffset = statMap.getValueOrDefault(GROUP_MODIFIED, fileOrApp, times[2], false, true);
                        }

                        if(!statMap.hasValue(GROUP_CHANGE, fileOrApp)) {
                            changeOffset = statMap.getValueOrDefault(GROUP_CHANGE, fileOrApp, DateTimeUtil.toTimeMillis(offUpdateSetting, times[3]), false, true);
                        } else {
                            changeOffset = statMap.getValueOrDefault(GROUP_CHANGE, fileOrApp, times[3], false, true);
                        }
                    }
                }
            }
        }catch (Exception e) {
            Log.e(TAG, "Failed to Init File Time Interceptor! File=" + fileOrApp + " Error=" + e);
        }
    }*/
}
