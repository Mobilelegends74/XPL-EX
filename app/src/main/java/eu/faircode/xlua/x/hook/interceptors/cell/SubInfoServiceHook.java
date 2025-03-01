package eu.faircode.xlua.x.hook.interceptors.cell;

import android.telephony.SubscriptionInfo;
import android.util.Log;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;

public class SubInfoServiceHook {
    private static final String TAG = LibUtil.generateTag(SubInfoServiceHook.class);



    public static boolean handleGetActiveSubscriptionInfoList(XC_MethodHook.MethodHookParam param) {
        try {
            List<SubscriptionInfo> subscriptions = (List<SubscriptionInfo>) param.getResult();
            String callingPackage = Str.ensureIsNotNullOrDefault(Str.toStringOrNull(param.args[0]), Str.EMPTY);
            String callingFeatureId = Str.ensureIsNotNullOrDefault(Str.toStringOrNull(param.args[1]), Str.EMPTY);
            boolean isForAllProfiles = (boolean) param.args[2];

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Sub Count=%s Calling package=%s Calling feature id=%s Is For All Profiles=%s",
                        ListUtil.size(subscriptions),
                        callingPackage,
                        callingFeatureId,
                        isForAllProfiles));

        }catch (Exception e) {
            Log.e(TAG, "Got a list of Active Subscription Infos");
            return false;
        }

        return false;
    }


    public static boolean handleGetActiveSubscriptionInfoList(XParam param) {
        try {
            List<SubscriptionInfo> subscriptions = param.tryGetResult(ListUtil.emptyList());
            String callingPackage = param.tryGetArgument(0, Str.EMPTY);
            String callingFeatureId = param.tryGetArgument(1, Str.EMPTY);
            boolean isForAllProfiles = param.tryGetArgument(2, false);

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Sub Count=%s Calling package=%s Calling feature id=%s Is For All Profiles=%s",
                        ListUtil.size(subscriptions),
                        callingPackage,
                        callingFeatureId,
                        isForAllProfiles));

        }catch (Exception e) {
            Log.e(TAG, "Got a list of Active Subscription Infos");
            return false;
        }

        return false;
    }
}
