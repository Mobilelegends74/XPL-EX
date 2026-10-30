package eu.faircode.xlua.x.systemlab;

import android.content.ContentProvider;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayDeque;

import eu.faircode.xlua.xposed.api101.ModernMethodHook;

/**
 * A limited laboratory extension of XPL-EX's existing system SettingsProvider
 * hook. It never installs a hook into the application being tested.
 * All ordinary callers and failed provider reads remain unchanged.
 */
public final class SettingsProviderIdentityLab {
    private static final String TAG = "XPL-EX/IdentityLab";
    private static final ThreadLocal<ArrayDeque<Boolean>> calls = new ThreadLocal<>();

    private SettingsProviderIdentityLab() { }

    public static void beforeCall(ModernMethodHook.MethodHookParam param) {
        if (!matches(param)) return;
        ArrayDeque<Boolean> stack = calls.get();
        if (stack == null) { stack = new ArrayDeque<>(); calls.set(stack); }
        boolean allowed = false;
        try {
            if (param.thisObject instanceof ContentProvider) {
                final Context ctx = ((ContentProvider) param.thisObject).getContext();
                final int uid = Binder.getCallingUid();
                if (ctx != null)
                    allowed = IdentityLabPolicy.isLabCaller(
                            uid, ctx.getPackageManager().getPackagesForUid(uid));
            }
        } catch (Throwable error) {
            Log.w(TAG, "Cannot resolve caller; preserving system result", error);
        }
        stack.push(allowed);
    }

    public static void afterCall(ModernMethodHook.MethodHookParam param) {
        if (!matches(param)) return;
        ArrayDeque<Boolean> stack = calls.get();
        if (stack == null || stack.isEmpty()) return;
        boolean allowed = stack.pop();
        if (stack.isEmpty()) calls.remove();
        if (!allowed || param.hasThrowable()) return;
        try {
            Object result = param.getResult();
            if (!(result instanceof Bundle)) return;
            Bundle original = (Bundle) result;
            // Keep Android's permission behavior: do not turn denied or
            // missing data into a synthetic successful lookup.
            if (original.getString("value") == null) return;
            if (!IdentityLabPolicy.validId(IdentityLabPolicy.DEMO_ID)) return;
            Bundle copy = new Bundle(original);
            copy.putString("value", IdentityLabPolicy.DEMO_ID);
            param.setResult(copy);
            Log.i(TAG, "Lab-only Settings.Secure Android ID response handled");
        } catch (Throwable error) {
            Log.w(TAG, "Cannot transform lab response", error);
        }
    }

    private static boolean matches(ModernMethodHook.MethodHookParam param) {
        return param != null && param.args != null && param.args.length >= 2
                && param.args[0] instanceof String
                && param.args[1] instanceof String
                && IdentityLabPolicy.isAndroidIdRead(
                        (String) param.args[0], (String) param.args[1]);
    }
}
