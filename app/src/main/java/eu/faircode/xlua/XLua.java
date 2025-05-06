/*
    This file is part of XPrivacyLua.

    XPrivacyLua is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    XPrivacyLua is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with XPrivacyLua.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2017-2019 Marcel Bokhorst (M66B)
 */

package eu.faircode.xlua;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import org.luaj.vm2.Globals;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.Varargs;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import eu.faircode.xlua.loggers.LogHelper;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.file.FileEx;
import eu.faircode.xlua.x.file.ModePermission;
import eu.faircode.xlua.x.hook.HookCore;
import eu.faircode.xlua.x.hook.filter.HookRepository;
import eu.faircode.xlua.hooks.XHookUtil;

//package eu.faircode.xlua;

import java.util.Collection;

import eu.faircode.xlua.hooks.LuaHookWrapper;
import eu.faircode.xlua.hooks.LuaScriptHolder;
import eu.faircode.xlua.hooks.XReporter;
import eu.faircode.xlua.hooks.LuaHookResolver;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.logger.XReport;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.utilities.ReflectUtilEx;
import eu.faircode.xlua.x.hook.inlined.HashMapHooks;
import eu.faircode.xlua.x.hook.inlined.UpTimeHooks;
import eu.faircode.xlua.x.hook.interceptors.cell.PhoneServicesHook;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.XposedUtility;
import eu.faircode.xlua.x.xlua.commands.GlobalCommandBridge;
import eu.faircode.xlua.x.xlua.commands.call.GetBridgeVersionCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetAssignedHooksExCommand;
import eu.faircode.xlua.x.xlua.database.DatabasePathUtil;
import eu.faircode.xlua.x.xlua.hook.PackageHookContext;
import eu.faircode.xlua.x.xlua.settings.GroupStats;

/*
    ToDO:
        [>] Add the remaining remapped Entries
        [>] Ensure use Proper abstraction, Returning Collection is useless as Caller function Can cast to get it as Collection so have it return something like list
        [>] When doing "toString" function ensure the "appendFieldLine" takes in the Constant for the Field Name if available
        [>] Make a System that for one compresses more of the fragment bullshit
        [>] Make a Application shared system
        [>] Clean up shared
        [>] Make a System for like "onDataEvent(Object o)"
 */

public class XLua implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private static final String TAG = LibUtil.generateTag(XLua.class);
    public XReporter report = new XReporter();
    public static int version = -1;

    public void initZygote(final IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        Log.i(TAG, "initZygote system=" + startupParam.startsSystemServer + " debug=" + BuildConfig.DEBUG);
    }

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        int uid = Process.myUid();
        Log.i(TAG, "Loaded " + lpparam.packageName + ":" + uid);
        XposedBridge.log(TAG + " Loaded " + lpparam.packageName + ":" + uid);

        if ("android".equals(lpparam.packageName))
            hookAndroid(lpparam);
        if ("com.android.providers.settings".equals(lpparam.packageName))
            hookSettings(lpparam);
        if (!"android".equals(lpparam.packageName) &&
                !lpparam.packageName.startsWith(BuildConfig.APPLICATION_ID)) {
            hookApplication(lpparam);
        }
    }

    private void hookSettings(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        GetBridgeVersionCommand.init();
        // https://android.googlesource.com/platform/frameworks/base/+/master/packages/SettingsProvider/src/com/android/providers/settings/SettingsProvider.java
        Class<?> clsSet = Class.forName("com.android.providers.settings.SettingsProvider", false, lpparam.classLoader);
        XposedBridge.hookMethod(
                clsSet.getMethod("call", String.class, String.class, Bundle.class),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        GlobalCommandBridge.handeCall(param, lpparam.packageName);
                    }
                });

        // Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
        XposedBridge.hookMethod(
                clsSet.getMethod("query", Uri.class, String[].class, String.class, String[].class, String.class),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        GlobalCommandBridge.handleQuery(param, lpparam.packageName);
                    }
                });
    }


    private void hookSubscriptionManagerService(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Direct hook to the SubscriptionManagerService class
            Class<?> subManagerService = Class.forName(
                    "com.android.internal.telephony.subscription.SubscriptionManagerService",
                    false,
                    lpparam.classLoader
            );

            XposedUtility.logI_xposed(TAG, "Successfully found SubscriptionManagerService class");

            // Hook all variants of methods that might handle subscription info
            String[] methodsToHook = {
                    "getActiveSubscriptionInfoList",
                    "getActiveSubscriptionInfo",
                    "getSubscriptionInfo",
                    "getSubscriptionInfoInternal",
                    "getDefaultSubId",
                    "getPhoneId"
            };

            for (String methodName : methodsToHook) {
                XposedBridge.hookAllMethods(subManagerService, methodName, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedUtility.logI_xposed(TAG, "BEFORE [" + methodName + "] Called with " +
                                (param.args.length > 0 ? param.args[0] : "no args"));
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedUtility.logI_xposed(TAG, "AFTER [" + methodName + "] Result: " + param.getResult());
                        // Here you can modify the result if needed
                        super.afterHookedMethod(param);
                    }
                });
                XposedUtility.logI_xposed(TAG, "Hooked " + methodName + " method");
            }

        } catch (ClassNotFoundException e) {
            XposedUtility.logE_xposed(TAG, "SubscriptionManagerService class not found: " + e.getMessage());

            // Try the alternative approach - hook TelephonyManager directly
            try {
                Class<?> telephonyManagerClass = Class.forName("android.telephony.TelephonyManager", false, lpparam.classLoader);

                // Methods that directly access IMEI or subscription info
                String[] tmMethods = {
                        "getImei",
                        "getDeviceId",
                        "getSubscriberId",
                        "getSimSerialNumber",
                        "getActiveSubscriptionInfoList",
                        "getActiveSubscriptionInfoCount"
                };

                for (String method : tmMethods) {
                    XposedBridge.hookAllMethods(telephonyManagerClass, method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedUtility.logI_xposed(TAG, "TM BEFORE [" + method + "] Called");
                            super.beforeHookedMethod(param);
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedUtility.logI_xposed(TAG, "TM AFTER [" + method + "] Result: " + param.getResult());
                            super.afterHookedMethod(param);
                        }
                    });
                    XposedUtility.logI_xposed(TAG, "Hooked TelephonyManager." + method);
                }
            } catch (Exception te) {
                XposedUtility.logE_xposed(TAG, "Failed to hook TelephonyManager: " + te);
            }
        } catch (Exception e) {
            XposedUtility.logE_xposed(TAG, "Failed to hook SubscriptionManagerService: " + e);
        }
    }

    private void hookAndroid(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // https://android.googlesource.com/platform/frameworks/base/+/master/services/core/java/com/android/server/am/ActivityManagerService.java
        Class<?> clsAM = Class.forName("com.android.server.am.ActivityManagerService", false, lpparam.classLoader);
        XposedBridge.hookAllMethods(clsAM, "systemReady", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    XposedBridge.log(TAG + " Preparing system");
                    Context context = getContext(param.thisObject);
                    GetBridgeVersionCommand.init();
                    hookPackage(lpparam, Process.myUid(), context);
                } catch (Throwable ex) {
                    Log.e(TAG, Log.getStackTraceString(ex));
                    XposedBridge.log(ex);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    XposedBridge.log(TAG + " System ready");
                    Context context = getContext(param.thisObject);


                    // Store current module version
                    PackageInfo pi = context.getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, 0);
                    version = pi.versionCode;

                    // public static UserManagerService getInstance()
                    Class<?> clsUM = Class.forName("com.android.server.pm.UserManagerService", false, param.thisObject.getClass().getClassLoader());
                    Object um = clsUM.getDeclaredMethod("getInstance").invoke(null);
                    //  public int[] getUserIds()
                    int[] userids = (int[]) um.getClass().getDeclaredMethod("getUserIds").invoke(um);

                    // Listen for package changes
                    for (int userid : userids) {
                        //Log.i(TAG, "Registering package listener user=" + userid);
                        IntentFilter ifPackageAdd = new IntentFilter();
                        ifPackageAdd.addAction(Intent.ACTION_PACKAGE_ADDED);
                        ifPackageAdd.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
                        ifPackageAdd.addDataScheme("package");
                        XUtil.createContextForUser(context, userid).registerReceiver(new ReceiverPackage(), ifPackageAdd);
                    }
                } catch (Throwable ex) {
                    Log.e(TAG, Log.getStackTraceString(ex));
                    XposedBridge.log(ex);
                }
            }

            @NonNull
            private Context getContext(Object am) throws Throwable {
                // Searching for context
                Context context = null;
                Class<?> cAm = am.getClass();
                while (cAm != null && context == null) {
                    for (Field field : cAm.getDeclaredFields())
                        if (field.getType().equals(Context.class)) {
                            field.setAccessible(true);
                            context = (Context) field.get(am);
                            if(DebugUtil.isDebug())
                                Log.i(TAG, "Context found in " + cAm + " as " + field.getName());

                            break;
                        }
                    cAm = cAm.getSuperclass();
                }
                if (context == null)
                    throw new Throwable("Context not found");

                return context;
            }
        });

        //XposedUtility.logI_xposed(TAG, "Starting Sub Hooks!");
        //hookSubscriptionManagerService(lpparam);
    }



    public void hookApplication(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        final int uid = android.os.Process.myUid();
        final boolean tiramisuOrHigher = (Build.VERSION.SDK_INT >= 33);
        // https://android.googlesource.com/platform/frameworks/base/+/169aeafb2d97b810ae123ad036d0c58336961c55%5E%21/#F1
        Class<?> at = Class.forName(tiramisuOrHigher ? "android.app.Instrumentation" : "android.app.LoadedApk", false, lpparam.classLoader);

        XposedBridge.hookAllMethods(at,
                tiramisuOrHigher ? "newApplication" : "makeApplication", new XC_MethodHook() {
                    private boolean made = false;

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            if(!made) {
                                made = true;
                                Context context = (Application) param.getResult();

                                //Check for isolate process
                                int userid = XUtil.getUserId(uid);
                                int start = XUtil.getUserUid(userid, 99000);
                                int end = XUtil.getUserUid(userid, 99999);
                                boolean isolated = (uid >= start && uid <= end);
                                if (isolated) {
                                    Log.i(TAG, "Skipping isolated " + lpparam.packageName + ":" + uid);
                                    return;
                                }

                                if(lpparam.packageName.equalsIgnoreCase("com.android.phone")) {
                                    //XposedUtility.logI_xposed(TAG, "!!! Phone App! =" + lpparam.packageName);
                                    //PhoneServicesHook.deployHook(lpparam, context);
                                } else {
                                    hookPackage(lpparam, uid, context);
                                }
                            }
                        }catch (Throwable ex) {
                            Log.e(TAG, Log.getStackTraceString(ex));
                            XposedBridge.log(ex);
                        }
                    }
                });
    }

    public void hookPackage(final XC_LoadPackage.LoadPackageParam lpparam, int uid, final Context context) throws Throwable {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Starting Initializing Hooks for App: " + lpparam.packageName + " Uid: " + uid);
        HookCore.initHooks(lpparam, uid, context);
    }
}
