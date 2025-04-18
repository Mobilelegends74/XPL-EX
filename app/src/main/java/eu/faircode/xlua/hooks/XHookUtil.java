package eu.faircode.xlua.hooks;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.luaj.vm2.Globals;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.hook.XLuaHookAssets;
import eu.faircode.xlua.logger.XLog;
import eu.faircode.xlua.rootbox.XReflectUtils;
import eu.faircode.xlua.utilities.ReflectUtilEx;
import eu.faircode.xlua.utilities.StreamUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.runtime.reflect.DynamicMethod;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.xlua.LibUtil;

public class XHookUtil {
    private static final String TAG = LibUtil.generateTag(XHookUtil.class);

    public static Globals getHookGlobals(
            Context context,
            XHook hook,
            Map<String, String> settings,
            Map<String, Integer> propSettings,
            Map<String, String> propMaps,
            String key,
            boolean useDefault,
            String packageName) {
        Globals globals = JsePlatform.standardGlobals();
        // base, bit32, coroutine, io, math, os, package, string, table, luajava
        if (BuildConfig.DEBUG) globals.load(new DebugLib());
        //This will create the logger and hook instance
        globals.set("log", new LuaLog(context.getPackageName(), context.getApplicationInfo().uid, hook.getObjectId()));
        globals.set("hook", new LuaHook(context, settings, propSettings, propMaps, key, useDefault, packageName));
        return new LuaLocals(globals);
    }

    public static boolean isField(String methodName) { return methodName != null && methodName.startsWith("#"); }


    public static boolean isWildCardReturn(String ret) {
        return (ret.length() > 3 && ret.startsWith("*") && ret.endsWith("*"));
    }

    public static LuaHookResolver resolveTargetHook(Context context, XLuaHook hook) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {

        Class<?> cls = Class.forName(hook.getResolvedClassName(), false, context.getClassLoader());
        String methodName = hook.getMethodName();
        boolean isPreInit = false;
        if (methodName != null) {
            isPreInit =  methodName.startsWith("!");
            if(isPreInit) methodName = methodName.substring(1);

            //CREATOR : createFromParcel
            String[] m = methodName.split(":");

            if (m.length > 1) {
                Field field = cls.getField(m[0]);
                Object obj = field.get(null);
                cls = obj.getClass();
            }

            methodName = m[m.length - 1]; 
        }

        Class<?>[] paramTypes = XHookUtil.getParameterTypes(hook.getParameterTypes(), context);
        final Class<?> returnType =  Str.isEmpty(hook.getReturnType())
                ? null : ReflectUtilEx.resolveClass(hook.getReturnType(), context.getClassLoader());

        return new LuaHookResolver(cls, methodName, paramTypes, returnType, isPreInit);
    }

    public static Class<?>[] getParameterTypes(String[] p, Context context) throws ClassNotFoundException {
        Class<?>[] paramTypes = new Class[p.length];
        for (int i = 0; i < p.length; i++) {
            String pm = Str.trimOriginal(p[i]);
            if(Str.isEmpty(pm))
                continue;

            if(pm.equals("*"))
                paramTypes[i] = LuaHookResolver.TypeWild.class;
            else
                paramTypes[i] = ReflectUtilEx.resolveClass(p[i], context.getClassLoader());
        }

        return paramTypes;
    }

    public static Prototype compileScript(Map<LuaScriptHolder, Prototype> prototypes, XHook hook) {
        if(hook == null) {
            XLog.e("Hooks is NULL not compiling...", new Throwable(), true);
            return null;
        }

        InputStream is = null;
        try {
            LuaScriptHolder sh = new LuaScriptHolder(hook.luaScript);
            if (prototypes.containsKey(sh))
                return prototypes.get(sh);
            else {
                is = new ByteArrayInputStream(sh.script.getBytes());
                Prototype compiledScript = LuaC.instance.compile(is, "script");
                prototypes.put(sh, compiledScript);
                return compiledScript;
            }
        }catch (Exception e) {
            XLog.e("Error Compiling Hook Script. Hook=" + hook.getObjectId(), e, true);
            return null;
        } finally {
            StreamUtil.close(is);
        }
    }

    public static ArrayList<XLuaHook> readHooks(String apk) {
        XLog.i("Reading all Hooks in JSON...");
        ZipFile zipFile = null;
        ArrayList<XLuaHook> hooks_all = new ArrayList<>();
        try {
            zipFile = new ZipFile(apk);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().startsWith("assets/") && entry.getName().endsWith("hooks.json")) {
                    XLog.i("Found Entry for [hooks.json]: " + entry.getName());
                    ArrayList<XLuaHook> read_hooks = readHooksFromEntry(apk, entry, zipFile);
                    if(!read_hooks.isEmpty()) hooks_all.addAll(read_hooks);
                }
            }
        }catch (Exception e){ XLog.e("Failed to read Hooks.", e);
        } finally { StreamUtil.close(zipFile);
        } return hooks_all;
    }

    public static String getLuaScriptEx(String apk, String scriptName) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(apk);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if(entry.getName().endsWith(scriptName)) {
                    InputStream lis = null;
                    try {
                        lis = zipFile.getInputStream(entry);
                        return new Scanner(lis).useDelimiter("\\A").next();
                    } catch (Exception e) { XLog.e("Failed to find LUA Script: " + entry, e);
                    }finally { StreamUtil.close(lis); }
                }
            }
        }catch (Exception e){ XLog.e("Failed to read Hooks.", e);
        } finally { StreamUtil.close(zipFile);
        }

        if(DebugUtil.isDebug())
            Log.w(TAG, "Error Finding LUA Script: " + scriptName);

        return null;
    }

    public static ArrayList<XLuaHook> readHooksFromEntry(String apk, ZipEntry entry, ZipFile zipFile) {
        XLog.i("Parsing Hooks From Entry [" + entry + "]");
        ArrayList<XLuaHook> hooks = new ArrayList<>();
        String entryName = entry.getName();
        String entryPath = entryName.substring(0, entryName.length() - 11);
        boolean isBuiltIn = entryName.endsWith("assets/hooks.json");
        XLog.i("Parsing Zip Entry [" + entryPath + "] Is Built In [" + isBuiltIn + "]");
        InputStream is = null;
        try {
            is = zipFile.getInputStream(entry);
            String json = new Scanner(is).useDelimiter("\\A").next();

            JSONArray jArray = new JSONArray(json);
            for(int i = 0; i < jArray.length(); i++) {
                XLuaHookAssets hookAsset = new XLuaHookAssets();
                hookAsset.fromJSONObject(jArray.getJSONObject(i));

                if(TextUtils.isEmpty(hookAsset.getLuaScript())) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Lua is Empty >> " + hookAsset.getMethodName() + " " + hookAsset.getClassName());

                    hookAsset.setLuaScript("function before(hook, param) end");
                }
                else if (hookAsset.getLuaScript().startsWith("@")) {
                    hookAsset.setIsBuiltIn(isBuiltIn || hookAsset.isBuiltin());
                    //String luaContents = getLuaScript(zipFile, entryPath, hookAsset.getLuaScript().substring(1) + ".lua");

                    String luaContents = getLuaScriptEx(apk, hookAsset.getLuaScript().substring(1) + ".lua");
                    if(luaContents == null) {
                        XLog.e("Failed to Init Hook: " + hookAsset.getObjectId() + " Entry=" + entry, new Throwable(), true);
                        continue;
                    }

                    TryRun.silent(() -> {
                        Log.d(TAG, "Hook=" + hookAsset.getObjectId() + " Lua Contents=" + luaContents);
                    });

                    hookAsset.setLuaScript(luaContents);
                }
                hooks.add(hookAsset);
            }
        }catch (Exception e) {  XLog.e("Failed to Read Hooks from ZIP Entry=" + entry, e, true);
        }finally { StreamUtil.close(is);
        } return hooks;
    }

    public static String resolveClassName(Context context, String className) {
        try {
            if ("android.app.ActivityManager".equals(className)) {
                Object service = context.getSystemService(ActivityManager.class);
                if (service != null) return service.getClass().getName();

            } else if ("android.appwidget.AppWidgetManager".equals(className)) {
                Object service = context.getSystemService(AppWidgetManager.class);
                if (service != null) return service.getClass().getName();

            } else if ("android.media.AudioManager".equals(className)) {
                Object service = context.getSystemService(AudioManager.class);
                if (service != null) return service.getClass().getName();

            } else if ("android.hardware.camera2.CameraManager".equals(className)) {
                Object service = context.getSystemService(CameraManager.class);
                if (service != null) return service.getClass().getName();

            } else if ("android.content.ContentResolver".equals(className)) {
                return context.getContentResolver().getClass().getName();

            } else if ("android.content.pm.PackageManager".equals(className)) {
                return context.getPackageManager().getClass().getName();

            } else if ("android.hardware.SensorManager".equals(className)) {
                Object service = context.getSystemService(SensorManager.class);
                if (service != null) return service.getClass().getName();

            } else if ("android.telephony.SmsManager".equals(className)) {
                Object service = SmsManager.getDefault();
                if (service != null) return service.getClass().getName();

            } else if ("android.telephony.TelephonyManager".equals(className)) {
                Object service = context.getSystemService(Context.TELEPHONY_SERVICE);
                if (service != null) return service.getClass().getName();

            } else if ("android.net.wifi.WifiManager".equals(className)) {
                Object service = context.getSystemService(Context.WIFI_SERVICE);
                if (service != null) return service.getClass().getName();
            }
            else if("android.app.usage.StorageStatsManager".equals(className)) {
                Object service = context.getSystemService(Context.STORAGE_STATS_SERVICE);
                if(service != null) return service.getClass().getName();
            }
            else if("android.os.UserManager".equals(className)) {
                Object service = context.getSystemService(Context.USER_SERVICE);
                if(service != null) return service.getClass().getName();
            }
            else if("android.app.KeyguardManager".equals(className)) {
                Object service = context.getSystemService(Context.KEYGUARD_SERVICE);
                if(service != null) return service.getClass().getName();
            }
            else if("android.os.BatteryManager".equals(className)) {
                Object service = context.getSystemService(Context.BATTERY_SERVICE);
                if(service != null) return service.getClass().getName();
            }
            else if("java.io.FileSystem".equals(className)) {
                //java.io.DefaultFileSystem.getFileSystem();
                //java.io.File.fs
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    FileSystem fs = FileSystems.getDefault();
                    fs.
                }*/

                DynamicMethod mth = new DynamicMethod("java.io.DefaultFileSystem", "getFileSystem").setAccessible(true);
                Object res = mth.tryStaticInvoke();
                if(res == null) {
                    DynamicField fld = new DynamicField(File.class, "fs").setAccessible(true);
                    Object val = fld.tryGetValueStatic();
                    if(val == null) {
                        String unix = "java.io.UnixFileSystem";
                        if(XReflectUtils.classExists(unix)) {
                            return unix;
                        }
                    } else {
                        return val.getClass().getName();
                    }
                } else {
                    return res.getClass().getName();
                }
            }
        }catch (Exception ignored) {  }

        return className;
    }

    public static List<String> getActivityManagerClassNames(Context context) { return createList("android.app.ActivityManager", classFromSystemService(context, ActivityManager.class)); }
    public static List<String> getAppWidgetManagerClassNames(Context context) {return createList("android.appwidget.AppWidgetManager", classFromSystemService(context, AppWidgetManager.class)); }
    public static List<String> getAudioManagerClassNames(Context context) { return createList("android.media.AudioManager", classFromSystemService(context, AudioManager.class)); }
    public static List<String> getCameraManagerClassNames(Context context) { return createList("android.hardware.camera2.CameraManager", classFromSystemService(context, CameraManager.class)); }
    public static List<String> getContentResolverClassNames(Context context) { return createList("android.content.ContentResolver", context == null ? null : context.getContentResolver().getClass().getName()); }
    public static List<String> getPackageManagerClassNames(Context context) {return createList("android.content.pm.PackageManager", context == null ? null : context.getPackageManager().getClass().getName()); }
    public static List<String> getSensorManagerClassNames(Context context) { return createList("android.hardware.SensorManager", classFromSystemService(context, SensorManager.class)); }
    public static List<String> getSmsManagerClassNames(Context context) { return createList("android.telephony.SmsManager", TryRun.getOrDefault(() -> SmsManager.getDefault().getClass().getName(), null)); }
    public static List<String> getTelephonyManagerClassNames(Context context) { return createList("android.telephony.TelephonyManager", classFromSystemService(context, Context.TELEPHONY_SERVICE)); }
    public static List<String> getWifiManagerClassNames(Context context) { return createList("android.net.wifi.WifiManager", classFromSystemService(context, Context.WIFI_SERVICE)); }
    public static List<String> getStorageStatsManagerClassNames(Context context) { return createList("android.app.usage.StorageStatsManager", classFromSystemService(context, Context.STORAGE_STATS_SERVICE)); }
    public static List<String> getUserManagerClassNames(Context context) { return createList("android.os.UserManager", classFromSystemService(context, Context.USER_SERVICE)); }
    public static List<String> getKeyguardManagerClassNames(Context context) { return createList("android.app.KeyguardManager", classFromSystemService(context, Context.KEYGUARD_SERVICE)); }
    public static List<String> getBatteryManagerClassNames(Context context) { return createList("android.os.BatteryManager", classFromSystemService(context, Context.BATTERY_SERVICE)); }
    public static List<String> getFileSystemClassNames(Context context) { return createList("java.io.FileSystem", resolveFileSystem()); }

    /**
     * Special handling to resolve FileSystem implementation
     */
    private static String resolveFileSystem() {
        return TryRun.getOrDefault(() -> {
            DynamicMethod mth = new DynamicMethod("java.io.DefaultFileSystem", "getFileSystem").setAccessible(true);
            Object res = mth.tryStaticInvoke();
            if (res == null) {
                DynamicField fld = new DynamicField(File.class, "fs").setAccessible(true);
                Object val = fld.tryGetValueStatic();
                if (val == null) {
                    String unix = "java.io.UnixFileSystem";
                    if (XReflectUtils.classExists(unix)) {
                        return unix;
                    }
                } else {
                    return val.getClass().getName();
                }
            } else {
                return res.getClass().getName();
            }
            return null;
        }, null);
    }

    public static String classFromSystemService(Context context, Class<?> clazz) {
        return TryRun.getOrDefault(() -> {
            Object service = context.getSystemService(clazz);
            return service.getClass().getName();
        }, null);
    }

    public static String classFromSystemService(Context context, String name) {
        return TryRun.getOrDefault(() -> {
            Object service = context.getSystemService(name);
            return service.getClass().getName();
        }, null);
    }

    private static List<String> createList(String... names) {
        List<String> validNames = new ArrayList<>();
        if(ArrayUtils.isValid(names)) {
            for(String name : names) {
                if(!Str.isEmpty(name) && !validNames.contains(name))
                    validNames.add(name);
            }
        }

        return validNames;
    }
}
