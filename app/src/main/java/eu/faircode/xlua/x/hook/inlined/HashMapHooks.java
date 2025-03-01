package eu.faircode.xlua.x.hook.inlined;

import android.util.ArrayMap;
import android.util.Log;

import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.TypeMap;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.hook.PackageHookContext;

public class HashMapHooks {
    private static final String TAG = LibUtil.generateTag(HashMapHooks.class);

    private static final List<String> IDS = Arrays.asList(
            "PrivacyEx.ID/Settings/Cache(HashMap)/android_id",
            "PrivacyEx.ID/Settings/Cache(ArrayMap)/android_id");

    private static final String CLASS = "android.provider.Settings$NameValueCache";

    private static final TypeMap TYPE_MAP = TypeMap.create()
            .add(HashMap.class, "put")
            .add(ArrayMap.class, "put");

    public static boolean attach(XLuaHook hook, Member member, PackageHookContext app) {
        if(hook == null || member == null || app == null)
            return false;

        try {
            if(!TYPE_MAP.hasDefinition(hook))
                return false;

            String id = hook.getObjectId();
            if(Str.isEmpty(id) || !IDS.contains(id))
                return false;

            if(DebugUtil.isDebug())
                Log.d(TAG, "Found a HashMap / ArrayMap Hook for IDs! Deploying Fast Inlined Java hook Ignoring LUA Script! ID=" + id);

            XposedBridge.hookMethod(member, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args.length >= 2) {
                        //For now we hardcode this to ANDROID_ID, later have Dynamic Resolver
                        Object paramOne = param.args[0];
                        Object paramTwo = param.args[1];
                        if(paramOne instanceof String && paramTwo instanceof String) {
                            String strOne = (String) paramOne;
                            String strTwo = (String) paramTwo;
                            if(strOne.equalsIgnoreCase("android_id") && strTwo.length() == 16) {
                                String newValue = app.settings.get("unique.android.id");
                                if(!Str.isEmpty(newValue)) {
                                    //android.provider.Settings$NameValueCache:getStringForUser
                                    boolean found = RuntimeUtils.hasElementClass(new Exception(), CLASS, 5);
                                    if(DebugUtil.isDebug())
                                        Log.d(TAG, "Failed to Identify Target Class for Hook: " + id + RuntimeUtils.getStackTraceSafeString(new Exception()));

                                    if(found) {
                                        param.args[1] = newValue;
                                        Log.d(TAG, "Replaced Old Android ID {" + strTwo + "} with new {" + newValue + "}");
                                    }
                                }
                            }
                        }
                    }

                    super.beforeHookedMethod(param);
                }
            });

            return true;
        }catch (Exception e) {
            Log.e(TAG, "Error Deploying Hook for :" + hook.getObjectId());
            return false;
        }
    }

}
