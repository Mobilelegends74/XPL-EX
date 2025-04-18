package eu.faircode.xlua.x.hook.interceptors.user;

import android.os.UserHandle;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.hook.interceptors.zone.RandomDateHelper;
import eu.faircode.xlua.x.xlua.LibUtil;

public class UserCreationTimeInterceptor {
    private static final String TAG = LibUtil.generateTag(UserCreationTimeInterceptor.class);

    public static final String MAP_CATEGORY = "user";
    public static final String MAP_GROUP_OFF = "creation.time.offset";
    public static final String MAP_ORIGINAL = "creation.time.original";

    public static boolean interceptUserManager(XParam param) {
        try {
            UserHandle userHandle = param.tryGetArgument(0, null);
            if(userHandle == null)
                return false;

            String id = String.valueOf(userHandle.hashCode());
            if(Str.isEmpty(id))
                return false;

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Intercepting User (%s) Creation Date!",
                        id));

            long[] times = RandomDateHelper.generateEpochTimeStamps(2, true);

            GroupedMap map = param.getGroupedMap(MAP_CATEGORY);
            long result = param.tryGetResult(0L);
            if(result < 5000)
                return false;

            long offset = times[0];
            if(!map.hasValue(MAP_ORIGINAL, id)) {
                map.pushValue(MAP_ORIGINAL, id, result);
                map.pushValue(MAP_GROUP_OFF, id, offset);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("User (%s) Fake Time was Not Cached! Result (%s) Offset to Add (%s)",
                            id,
                            result,
                            offset));
            }

            offset = map.getValueLong(MAP_GROUP_OFF, id);
            long original = map.getValueLong(MAP_ORIGINAL, id);
            if(original != result) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("User (%s) Fake Time was Not Spoofed! Result (%s) Offset to Add (%s) as the Result does not Equal Original (%s) (Already Spoofed)",
                            id,
                            result,
                            offset,
                            original));

                return false;
            }

            long newResult = original + offset;
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("User (%s) Creation Time from (%s)(%s) was Spoofed to (%s)(%s) With an Offset of (%s)",
                        id,
                        original,
                        formatTimestamp(original),
                        newResult,
                        formatTimestamp(newResult),
                        offset));

            param.setResult(newResult);
            param.setLogNew(formatTimestamp(newResult));
            param.setLogOld(formatTimestamp(original));
            return true;
        }catch (Throwable e) {
            Log.e(TAG, "Failed to Intercept UserManager, Error=" + e);
            return false;
        }
    }

    private static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
