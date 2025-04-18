package eu.faircode.xlua.x.hook;

import android.util.Log;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.xlua.LibUtil;

public class XLuaHooker {
    private static final String TAG = LibUtil.generateTag(XLuaHooker.class);


    public static void handleWildcardParams(final XC_LoadPackage.LoadPackageParam lpparamm, XLuaHook hook) {
        try {



        }catch (Exception e) {
            Log.e(TAG, "Error Handling Wild Card Param Hook! Error=" + e);
        }
    }
}
