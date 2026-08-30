package eu.faircode.xlua.x.runtime;

import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import eu.faircode.xlua.x.runtime.reflect.StaticFieldWriter;

/** Keeps the current Android version native while exposing the profile's real launch version. */
public final class AndroidVersionSpoofer {
    private static final String TAG = "XLua.AndroidVersion";
    private static boolean propertyHooksInstalled;

    private AndroidVersionSpoofer() { }

    /**
     * Makes every later hook consume the process' native current release/API.
     * This also repairs older saved profiles whose individual version settings
     * came from a different Android generation.
     */
    public static boolean normalizeSettings(Map<String, String> settings) {
        VersionValues values = runtimeValues(settings);
        if (values == null)
            return false;
        try {
            values.normalize(settings);
            return true;
        } catch (RuntimeException error) {
            Log.e(TAG, "Failed normalizing profile Android version settings", error);
            return false;
        }
    }

    public static boolean applyFrameworkFields(Map<String, String> settings) {
        VersionValues values = runtimeValues(settings);
        if (values == null)
            return false;

        normalizeSettings(settings);
        try {
            Class<?> versionClass = Class.forName("android.os.Build$VERSION");

            // Keep every write independent. Some Android releases protect one
            // of the String fields; that must not prevent SDK_INT from changing.
            boolean sdkInt = setStaticFieldIfPresent(versionClass, "SDK_INT", values.currentApi);
            setStaticFieldIfPresent(versionClass, "RESOURCES_SDK_INT", values.currentApi);
            setStaticFieldIfPresent(versionClass, "SDK", String.valueOf(values.currentApi));
            boolean release = setStaticFieldIfPresent(versionClass, "RELEASE", values.currentRelease);
            setStaticFieldIfPresent(versionClass, "RELEASE_OR_CODENAME", values.currentRelease);
            setStaticFieldIfPresent(versionClass, "RELEASE_OR_PREVIEW_DISPLAY", values.currentRelease);
            setStaticFieldIfPresent(versionClass, "DEVICE_INITIAL_SDK_INT", values.firstApi);
            return sdkInt && release;
        } catch (Throwable error) {
            Log.e(TAG, "Failed applying profile Android version fields", error);
            return false;
        }
    }

    public static synchronized boolean installSystemPropertyHooks(Map<String, String> settings) {
        if (propertyHooksInstalled)
            return true;
        final VersionValues values = runtimeValues(settings);
        if (values == null)
            return false;

        try {
            Class<?> properties = Class.forName("android.os.SystemProperties");
            XC_MethodHook callback = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (param.args == null || param.args.length == 0 || !(param.args[0] instanceof String))
                        return;
                    String replacement = values.forProperty((String) param.args[0]);
                    if (replacement == null)
                        return;

                    Class<?> returnType = param.method instanceof Method
                            ? ((Method) param.method).getReturnType() : String.class;
                    if (returnType == int.class)
                        param.setResult(Integer.parseInt(replacement));
                    else if (returnType == long.class)
                        param.setResult(Long.parseLong(replacement));
                    else
                        param.setResult(replacement);
                }
            };
            XposedBridge.hookAllMethods(properties, "get", callback);
            XposedBridge.hookAllMethods(properties, "getInt", callback);
            XposedBridge.hookAllMethods(properties, "getLong", callback);
            propertyHooksInstalled = true;
            return true;
        } catch (Throwable error) {
            Log.e(TAG, "Failed installing Android version property hooks", error);
            return false;
        }
    }

    private static boolean setStaticFieldIfPresent(Class<?> clazz, String fieldName, Object value) {
        try {
            Field target = clazz.getDeclaredField(fieldName);
            StaticFieldWriter.set(target, value);
            return true;
        } catch (NoSuchFieldException ignored) {
            return true;
        } catch (Throwable error) {
            Log.e(TAG, "Failed applying Build.VERSION." + fieldName + "=" + value, error);
            return false;
        }
    }

    private static VersionValues runtimeValues(Map<String, String> settings) {
        // Spoof Device must never change the process Android generation. The
        // selected catalog build is constrained to this same API separately.
        return VersionValues.from(settings, Build.VERSION.RELEASE, Build.VERSION.SDK_INT);
    }

    static final class VersionValues {
        final String currentRelease;
        final int currentApi;
        final String firstRelease;
        final int firstApi;

        private VersionValues(String currentRelease, int currentApi,
                              String firstRelease, int firstApi) {
            this.currentRelease = currentRelease;
            this.currentApi = currentApi;
            this.firstRelease = firstRelease;
            this.firstApi = firstApi;
        }

        static VersionValues from(Map<String, String> settings,
                                  String realRelease, int realApi) {
            if (settings == null)
                return null;
            try {
                String firstApiText = settings.get("android.build.version.min.sdk");
                if (realRelease == null || firstApiText == null)
                    return null;
                int firstApi = Integer.parseInt(firstApiText.trim());
                String firstRelease = releaseForApi(firstApi);
                return new VersionValues(realRelease.trim(), realApi, firstRelease, firstApi);
            } catch (RuntimeException ignored) {
                return null;
            }
        }

        String forProperty(String propertyName) {
            if (propertyName == null)
                return null;
            switch (propertyName) {
                case "ro.product.first_api_level":
                case "ro.board.first_api_level":
                    return String.valueOf(firstApi);
                case "ro.build.version.sdk":
                case "ro.system.build.version.sdk":
                case "ro.product.build.version.sdk":
                case "ro.vendor.build.version.sdk":
                case "ro.vendor.api_level":
                case "ro.board.api_level":
                    return String.valueOf(currentApi);
                case "ro.build.version.release":
                case "ro.build.version.release_or_codename":
                case "ro.build.version.release_or_preview_display":
                case "ro.system.build.version.release":
                case "ro.product.build.version.release":
                case "ro.vendor.build.version.release":
                    return currentRelease;
                default:
                    return null;
            }
        }

        void normalize(Map<String, String> settings) {
            settings.put("android.build.version", currentRelease);
            settings.put("android.build.version.sdk", String.valueOf(currentApi));
        }

        private static String releaseForApi(int api) {
            switch (api) {
                case 28: return "9";
                case 29: return "10";
                case 30: return "11";
                case 31:
                case 32: return "12";
                case 33: return "13";
                case 34: return "14";
                case 35: return "15";
                case 36: return "16";
                default: return String.valueOf(api);
            }
        }

    }
}
