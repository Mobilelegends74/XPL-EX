package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class XposedUtil {
    private static final String TAG = "XLua.XposedUtil";
    private static Boolean isExp;
    public static boolean isExpModuleActive() {
        if (isExp != null) {
            return isExp;
        }
        try {
            @SuppressLint("PrivateApi") Context context = (Context) Class.forName("android.app.ActivityThread")
                    .getDeclaredMethod("currentApplication", new Class[0]).invoke(null, new Object[0]);
            if (context == null)
                return isExp = false;

            try {
                Bundle call = context.getContentResolver().call(Uri.parse("content://me.weishu.exposed.CP/"), "active", null, null);
                if (call == null)
                    return isExp = false;

                isExp = call.getBoolean("active", false);
                return isExp;
            } catch (Throwable th) {
                return isExp = false;
            }
        } catch (Throwable th2) {
            return isExp = false;
        }
    }

    public static boolean isVirtualXposed() {
        return  !TextUtils.isEmpty(System.getProperty("vxp"))
                || !TextUtils.isEmpty(System.getProperty("exp"))
                || isExpModuleActive();
    }

    public static List<String> getExpApps(Context context) {
        try {
            Bundle call = context.getContentResolver().call(Uri.parse("content://me.weishu.exposed.CP/"), "apps", null, null);
            if (call == null) {
                return Collections.emptyList();
            }
            ArrayList<String> stringArrayList = call.getStringArrayList("apps");
            if (stringArrayList == null) {
                return Collections.emptyList();
            }
            return stringArrayList;
        } catch (Throwable th) {
            return Collections.emptyList();
        }
    }

    public static Collection<ApplicationInfo> getApplications(Context context) {
        Log.i(TAG, "zer0def for TaiChi Support");
        List<String> expApps = getExpApps(context);
        PackageManager packageManager = context.getPackageManager();
        if (expApps.isEmpty()) {
            return packageManager.getInstalledApplications(PackageManager.MATCH_ALL);
        } else {
            List<ApplicationInfo> apps = new ArrayList<>();
            for (String expApp : expApps) {
                try {
                    apps.add(packageManager.getApplicationInfo(expApp, 0));
                } catch (PackageManager.NameNotFoundException e) {

                }
            }
            return apps;
        }
    }
}
