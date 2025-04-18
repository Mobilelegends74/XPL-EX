package eu.faircode.xlua.x.hook.interceptors.file;

import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.utilities.DateTimeUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.hook.interceptors.pkg.PackageInfoInterceptor;
import eu.faircode.xlua.x.hook.interceptors.zone.RandomDateHelper;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

public class FileTimeInterceptor {
    public static final long NOW_TIME = System.currentTimeMillis();
    private static final String TAG = LibUtil.generateTag(FileTimeInterceptor.class);

    public static FileTimeInterceptor create(String file, XParam param) { return new FileTimeInterceptor(file, param); }

    public static final String GROUP_MAP_OFFSET = "stat:offset";
    public static final String GROUP_MAP_ORIGINAL = "stat:original";

    public static final String GROUP_CREATED = "created";
    public static final String GROUP_ACCESS = "access";
    public static final String GROUP_MODIFIED = "modified";
    public static final String GROUP_CHANGE = "changed";

    public final String file;
    public final XParam param;

    private long originalCreated = -1;
    private long originalAccess = -1;
    private long originalModified = -1;
    private long originalChange = -1;

    private long createdOffset;
    private long accessOffset;
    private long modifiedOffset;
    private long changeOffset;

    public long getChangeOffset() { return isMapsCore(file) ? 0 : changeOffset; }
    public long getCreatedOffset() { return isMapsCore(file) ? 0 : createdOffset; }
    public long getAccessOffset() { return isMapsCore(file) ? 0 : accessOffset; }
    public long getModifiedOffset() { return isMapsCore(file) ? 0 : modifiedOffset; }

    public static boolean isApk(String file, String pkg) { return (file.startsWith("/data/app/") && file.contains("/" + pkg) && file.endsWith("/base.apk")) || file.equalsIgnoreCase(pkg); }

    public FileTimeInterceptor(String file, XParam param) {
        this.file = isApk(file, param.getPackageName()) ? param.getPackageName() : file;
        this.param = param;
        initOffsets();
        if(DebugUtil.isDebug())
            Log.d(TAG, "Created File Time Interceptor For File:" + file +
                    " w=" + this.file +
                    " Created Time Offset=" + createdOffset +
                    " Access Time Offset=" + accessOffset +
                    " Modify Time Offset=" + modifiedOffset +
                    " Change Time Offset=" + changeOffset +
                    " Is Apk=" + isApk(file, param.getPackageName()));
    }

    public long getOriginalChange(long val) {
        if(val < 10000000L) {
            return val;
        } else {
            if(originalChange > 0) {
                return originalChange;
            } else {
                GroupedMap map = param.getGroupedMap(GROUP_MAP_ORIGINAL);
                originalChange = map.getValueOrDefault(GROUP_CHANGE, file, val, false, true);
                return originalChange;
            }
        }
    }

    public long getOriginalCreated(long val) {
        if(val < 10000000L) {
            return val;
        } else {
            if(originalCreated > 0) {
                return originalCreated;
            } else {
                GroupedMap map = param.getGroupedMap(GROUP_MAP_ORIGINAL);
                originalCreated = map.getValueOrDefault(GROUP_CREATED, file, val, false, true);
                return originalCreated;
            }
        }
    }

    public long getOriginalAccess(long val) {
        if(val < 10000000L) {
            return val;
        } else {
            if(originalAccess > 0) {
                return originalAccess;
            } else {
                GroupedMap map = param.getGroupedMap(GROUP_MAP_ORIGINAL);
                originalAccess = map.getValueOrDefault(GROUP_ACCESS, file, val, false, true);
                return originalAccess;
            }
        }
    }

    public long getOriginalModified(long val) {
        if(val < 10000000L) {
            return val;
        } else {
            if(originalModified > 0) {
                return originalModified;
            } else {
                GroupedMap map = param.getGroupedMap(GROUP_MAP_ORIGINAL);
                originalModified = map.getValueOrDefault(GROUP_MODIFIED, file, val, false, true);
                return originalModified;
            }
        }
    }

    public long getFinalValue(long offset, long original) {
        if(original < 10000000L || isMapsCore(file)) return original;
        if(offset == -3) return System.currentTimeMillis();
        if(offset == -2) return NOW_TIME;
        if(offset < 1) return original;
        return original + offset;
    }

    public static boolean isMapsCore(String file) {
        if(Str.isEmpty(file)) return false;
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

        boolean res = file.startsWith("/data/user_de") && file.contains("/com.google.android.gms/app_chimera/") && file.endsWith(".apk");
        if(res && DebugUtil.isDebug()) Log.d(TAG, "GMS Core Chimera APK! File=" + file + " Stack=" + RuntimeUtils.getStackTraceSafeString(new Exception()));
        return res;
    }

    //if is 1970 or 69, then sync adjust them, so offset then do offset ?
    private void initOffsets() {
        try {
            //PS I think your algo is least to greatest so make sure it makes sense
            GroupedMap statMap = param.getGroupedMap(GROUP_MAP_OFFSET);
            long[] times = RandomDateHelper.generateEpochTimeStamps(4, true);
            if(!isApk(file, param.getPackageName())) {
                createdOffset = statMap.getValueOrDefault(GROUP_CREATED, file, times[0], false, true);
                accessOffset = statMap.getValueOrDefault(GROUP_ACCESS, file, times[1], false, true);
                modifiedOffset = statMap.getValueOrDefault(GROUP_MODIFIED, file, times[2], false, true);
                changeOffset = statMap.getValueOrDefault(GROUP_CHANGE, file, times[3], false, true);
            } else {
                String offInstallSetting = param.getSetting(PackageInfoInterceptor.INSTALL_CURRENT_OFFSET_SETTING);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "IsApk, File=" + file + " Current Install Offset Setting=" + offInstallSetting);

                if(Str.isEmpty(offInstallSetting)) {
                    createdOffset = statMap.getValueOrDefault(GROUP_CREATED, file, times[0], false, true);
                } else {
                    if(PackageInfoInterceptor.NOW_ALWAYS.equalsIgnoreCase(offInstallSetting)) {
                        //createdOffset = System.currentTimeMillis();
                        createdOffset = -3;
                    }
                    else if(PackageInfoInterceptor.RAND_ALWAYS.equalsIgnoreCase(offInstallSetting)) {
                        createdOffset = times[0];
                    }
                    else if(PackageInfoInterceptor.RAND_ONCE.equalsIgnoreCase(offInstallSetting)) {
                        createdOffset = statMap.getValueOrDefault(GROUP_CREATED, file, times[0], false, true);
                    }
                    else if(PackageInfoInterceptor.NOW_ONCE.equalsIgnoreCase(offInstallSetting)) {
                        createdOffset = statMap.getValueOrDefault(GROUP_CREATED, file, -2, false, true);
                    } else {
                        if(!statMap.hasValue(GROUP_CREATED, file)) {
                            createdOffset = statMap.getValueOrDefault(GROUP_CREATED, file, DateTimeUtil.toTimeMillis(offInstallSetting, times[0]), false, true);
                        } else {
                            createdOffset = statMap.getValueOrDefault(GROUP_CREATED, file, times[0], false, true);
                        }
                    }
                }

                String offUpdateSetting = param.getSetting(PackageInfoInterceptor.UPDATE_CURRENT_OFFSET_SETTING);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "IsApk, File=" + file + " Current Update Offset Setting=" + offInstallSetting);

                if(Str.isEmpty(offUpdateSetting)) {
                    accessOffset = statMap.getValueOrDefault(GROUP_ACCESS, file, times[1], false, true);
                    modifiedOffset = statMap.getValueOrDefault(GROUP_MODIFIED, file, times[2], false, true);
                    changeOffset = statMap.getValueOrDefault(GROUP_CHANGE, file, times[3], false, true);
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
                        accessOffset = statMap.getValueOrDefault(GROUP_ACCESS, file, times[1], false, true);
                        modifiedOffset = statMap.getValueOrDefault(GROUP_MODIFIED, file, times[2], false, true);
                        changeOffset = statMap.getValueOrDefault(GROUP_CHANGE, file, times[3], false, true);
                    }
                    else if(PackageInfoInterceptor.NOW_ONCE.equalsIgnoreCase(offUpdateSetting)) {
                        //long now = System.currentTimeMillis();
                        long now = -2;
                        accessOffset = statMap.getValueOrDefault(GROUP_ACCESS, file, now, false, true);
                        modifiedOffset = statMap.getValueOrDefault(GROUP_MODIFIED, file, now, false, true);
                        changeOffset = statMap.getValueOrDefault(GROUP_CHANGE, file, now, false, true);
                    } else {
                        if(!statMap.hasValue(GROUP_ACCESS, file)) {
                            accessOffset = statMap.getValueOrDefault(GROUP_ACCESS, file, DateTimeUtil.toTimeMillis(offUpdateSetting, times[1]), false, true);
                        } else {
                            accessOffset = statMap.getValueOrDefault(GROUP_ACCESS, file, times[1], false, true);
                        }

                        if(!statMap.hasValue(GROUP_MODIFIED, file)) {
                            modifiedOffset = statMap.getValueOrDefault(GROUP_MODIFIED, file, DateTimeUtil.toTimeMillis(offUpdateSetting, times[2]), false, true);
                        } else {
                            modifiedOffset = statMap.getValueOrDefault(GROUP_MODIFIED, file, times[2], false, true);
                        }

                        if(!statMap.hasValue(GROUP_CHANGE, file)) {
                            changeOffset = statMap.getValueOrDefault(GROUP_CHANGE, file, DateTimeUtil.toTimeMillis(offUpdateSetting, times[3]), false, true);
                        } else {
                            changeOffset = statMap.getValueOrDefault(GROUP_CHANGE, file, times[3], false, true);
                        }
                    }
                }
            }
        }catch (Exception e) {
            Log.e(TAG, "Failed to Init File Time Interceptor! File=" + file + " Error=" + e);
        }
    }
}
