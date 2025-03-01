package eu.faircode.xlua.x.hook.inlined;

import android.util.Log;

import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.data.TypeMap;
import eu.faircode.xlua.x.xlua.LibUtil;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.TypeMap;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.hook.PackageHookContext;


public class UpTimeHooks {
    private static final String TAG = LibUtil.generateTag(UpTimeHooks.class);

    private static final List<String> IDS = Arrays.asList("PrivacyEx.SystemClock.elapsedRealtime", "PrivacyEx.SystemClock.uptimeMillis");

    private static final TypeMap TYPE_MAP = TypeMap.create()
            .add("android.os.SystemClock", "uptimeMillis")
            .add("android.os.SystemClock", "elapsedRealtime");

    public static boolean attach(XLuaHook hook, Member member) {
        if(hook == null || member == null)
            return false;

        try {
            if(!TYPE_MAP.hasDefinition(hook))
                return false;

            String id = hook.getObjectId();
            if(Str.isEmpty(id) || !IDS.contains(id))
                return false;

            if(DebugUtil.isDebug())
                Log.d(TAG, "Deploying [" + id + "] as a Fast Inlined Java Hook!");

            XposedBridge.hookMethod(member, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param)  {
                    Object res = param.getResult();
                    if(res instanceof Long) {
                        long val = (long)res;
                        long newVal = val + ThreadLocalRandom.current().nextLong(10L, 1001L);
                        param.setResult(newVal);
                    }
                }
            });
            return true;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Attach System UpTime Fast Inlined Hooks! Hook=" + hook.getObjectId() + " Error=" + e);
            return false;
        }
    }
}
