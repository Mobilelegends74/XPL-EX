package eu.faircode.xlua.x.hook.interceptors.file.cleaners;

import android.system.StructStat;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.hook.interceptors.file.FileTimeInterceptor;
import eu.faircode.xlua.x.hook.interceptors.file.stat.StatMockSettings;
import eu.faircode.xlua.x.hook.interceptors.file.stat.StatUtils;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.xlua.LibUtil;

public class StatStructCleaner {
    private static final String TAG = LibUtil.generateTag(StatStructCleaner.class);

    public static final DynamicField TIME_STRUCT_SEC_FIELD = new DynamicField("android.system.StructTimespec", "tv_sec")
            .setAccessible(true);

    public static final DynamicField TIME_STRUCT_NANO_FIELD = new DynamicField("android.system.StructTimespec", "tv_nsec")
            .setAccessible(true);

    public static final DynamicField STAT_DEV_FIELD = new DynamicField(StructStat.class, "st_dev")
            .setAccessible(true);

    public static final DynamicField STAT_INODE_FIELD = new DynamicField(StructStat.class, "st_ino")
            .setAccessible(true);

    public static final DynamicField STAT_ACCESS_FIELD = new DynamicField(StructStat.class, "st_atime")
            .setAccessible(true);

    public static final DynamicField STAT_ACCESS_S_FIELD = new DynamicField(StructStat.class, "st_atim")
            .setAccessible(true);

    public static final DynamicField STAT_MODIFICATION_FIELD = new DynamicField(StructStat.class, "st_mtime")
            .setAccessible(true);

    public static final DynamicField STAT_MODIFICATION_S_FIELD = new DynamicField(StructStat.class, "st_mtim")
            .setAccessible(true);

    public static final DynamicField STAT_CHANGE_FIELD = new DynamicField(StructStat.class, "st_ctime")
            .setAccessible(true);

    public static final DynamicField STAT_CHANGE_S_FIELD = new DynamicField(StructStat.class, "st_ctim")
            .setAccessible(true);

    public static boolean cleanStructure(FileTimeInterceptor interceptor, Object obj) {
        if(interceptor != null && obj != null) {
            try {
                StrBuilder orgValue = new StrBuilder();
                StrBuilder newValue = new StrBuilder();
                orgValue.appendFieldLine("File", interceptor.file);
                newValue.appendFieldLine("File", interceptor.file);

                long[] access = getTimes(obj, StatMockSettings.TimeKind.ACCESS);
                long originalAccess = interceptor.getOriginalAccess(secondsToMillis(access[0]));
                long fakeAccess = interceptor.getFinalValue(interceptor.getAccessOffset(), originalAccess);
                if(access[0] > 0) {
                    setTimes(obj, StatMockSettings.TimeKind.ACCESS, fakeAccess, access[1]);
                    orgValue.appendFieldLine("Access", StatUtils.timespecToString(access[0], access[1]));
                    newValue.appendFieldLine("Access", StatUtils.timespecToString(millisToSeconds(fakeAccess), access[1]));
                }

                long[] modified = getTimes(obj, StatMockSettings.TimeKind.MODIFY);
                long originalModified = interceptor.getOriginalModified(secondsToMillis(modified[0]));
                long fakeModified = interceptor.getFinalValue(interceptor.getModifiedOffset(), originalModified);
                if(modified[0] > 0) {
                    setTimes(obj, StatMockSettings.TimeKind.MODIFY, fakeModified, modified[1]);
                    orgValue.appendFieldLine("Modify", StatUtils.timespecToString(modified[0], modified[1]));
                    newValue.appendFieldLine("Modify", StatUtils.timespecToString(millisToSeconds(fakeModified), modified[1]));
                }

                long[] change = getTimes(obj, StatMockSettings.TimeKind.CHANGE);
                long originalChange = interceptor.getOriginalChange(secondsToMillis(change[0]));
                long fakeChange = interceptor.getFinalValue(interceptor.getChangeOffset(), originalChange);
                if(modified[0] > 0) {
                    setTimes(obj, StatMockSettings.TimeKind.CHANGE, fakeChange, change[1]);
                    orgValue.appendFieldLine("Change", StatUtils.timespecToString(change[0], change[1]));
                    newValue.appendFieldLine("Change", StatUtils.timespecToString(millisToSeconds(fakeChange), change[1]));
                }

                if(interceptor.param != null) {
                    interceptor.param.setLogOld(orgValue.toString());
                    interceptor.param.setLogNew(newValue.toString());
                }

                if(DebugUtil.isDebug()) {
                    Log.d(TAG, StrBuilder.create()
                            .ensureOneNewLinePer(true)
                            .appendFieldLine("Old=", orgValue.toString(true))
                            .appendFieldLine("New=", newValue.toString(true))
                            .toString(true));
                }

                return true;
            }catch (Exception e) {
                Log.e(TAG, "Internal Error Trying to Clean Stat Struct! Error=" + e);
                return false;
            }
        }

        return false;
    }

    public static long[] getTimes(Object instance, StatMockSettings.TimeKind kind) {
        if(instance == null) return new long[] { 0, 0 };
        DynamicField org = null;
        DynamicField timeStruct = null;
        switch (kind) {
            case ACCESS:
                org = STAT_ACCESS_FIELD;
                timeStruct = STAT_ACCESS_S_FIELD;
                break;
            case CHANGE:
                org = STAT_CHANGE_FIELD;
                timeStruct = STAT_CHANGE_S_FIELD;
                break;
            case MODIFY:
                org = STAT_MODIFICATION_FIELD;
                timeStruct = STAT_MODIFICATION_S_FIELD;
                break;
        }

        long nanos = 0;
        long seconds = 0;
        if(timeStruct != null && timeStruct.isValid()) {
            Object ts = timeStruct.tryGetValueInstanceEx(instance);
            if(ts != null) {
                nanos = TIME_STRUCT_NANO_FIELD.tryGetValueInstanceEx(ts, 0L);
                seconds = TIME_STRUCT_SEC_FIELD.tryGetValueInstanceEx(ts, 0L);
            }
        }

        if(seconds == 0 && org != null && org.isValid()) {
            seconds = org.tryGetValueInstanceEx(instance, 0L);
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Getting STAT Struct Time Stamps, Kind=" + kind.name() + " Seconds=" + seconds + " Nano Seconds=" + nanos);


        return new long[] { seconds, nanos };
    }

    public static long secondsToMillis(long seconds) {
        return seconds * 1000;
    }

    public static long millisToSeconds(long millis) {
        return millis / 1000;
    }

    public static void setTimes(Object instance, StatMockSettings.TimeKind kind, long seconds, long nanoSeconds) {
        if(instance == null) return;
        DynamicField org = null;
        DynamicField timeStruct = null;
        switch (kind) {
            case ACCESS:
                org = STAT_ACCESS_FIELD;
                timeStruct = STAT_ACCESS_S_FIELD;
                break;
            case CHANGE:
                org = STAT_CHANGE_FIELD;
                timeStruct = STAT_CHANGE_S_FIELD;
                break;
            case MODIFY:
                org = STAT_MODIFICATION_FIELD;
                timeStruct = STAT_MODIFICATION_S_FIELD;
                break;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Changing STAT Struct Time Stamps, Kind=" + kind.name() + " Seconds=" + seconds + " Nano Seconds=" + nanoSeconds);

        if(timeStruct != null && timeStruct.isValid()) {
            Object ts = timeStruct.tryGetValueInstanceEx(instance);
            if(ts != null) {
                TIME_STRUCT_NANO_FIELD.trySetValueInstanceEx(ts, nanoSeconds);
                TIME_STRUCT_SEC_FIELD.trySetValueInstanceEx(ts, seconds);
                timeStruct.trySetValueInstanceEx(instance, ts);
            }
        }

        if(org != null && org.isValid())
            org.trySetValueInstanceEx(instance, seconds);
    }
}
