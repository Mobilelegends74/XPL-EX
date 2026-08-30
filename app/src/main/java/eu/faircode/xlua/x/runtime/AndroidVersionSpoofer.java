package eu.faircode.xlua.x.runtime;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import eu.faircode.xlua.x.runtime.reflect.StaticFieldWriter;

/** Applies the exact current and launch Android versions of the selected device profile. */
public final class AndroidVersionSpoofer {
    private static final String TAG = "XLua.AndroidVersion";
    private static boolean propertyHooksInstalled;

    private AndroidVersionSpoofer() { }

    /**
     * Makes every later hook consume the same current release/API as the
     * selected firmware fingerprint. This also repairs older saved profiles
     * whose individual version settings were not updated atomically.
     */
    public static boolean normalizeSettings(Map<String, String> settings) {
        VersionValues values = VersionValues.from(settings);
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
        VersionValues values = VersionValues.from(settings);
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
        final VersionValues values = VersionValues.from(settings);
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

        static VersionValues from(Map<String, String> settings) {
            if (settings == null)
                return null;
            try {
                String currentRelease = releaseFromFingerprint(
                        settings.get("android.build.fingerprint"));
                if (currentRelease == null)
                    currentRelease = settings.get("android.build.version");
                String currentApiText = settings.get("android.build.version.sdk");
                String firstApiText = settings.get("android.build.version.min.sdk");
                if (currentRelease == null || currentApiText == null || firstApiText == null)
                    return null;
                int configuredCurrentApi = Integer.parseInt(currentApiText.trim());
                int currentApi = apiForRelease(currentRelease, configuredCurrentApi);
                int firstApi = Integer.parseInt(firstApiText.trim());
                String firstRelease = releaseForApi(firstApi);
                return new VersionValues(currentRelease.trim(), currentApi, firstRelease, firstApi);
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

        private static int apiForRelease(String release, int fallback) {
            if (release == null)
                return fallback;
            switch (release.trim()) {
                case "9": return 28;
                case "10": return 29;
                case "11": return 30;
                case "12": return 31;
                case "13": return 33;
                case "14": return 34;
                case "15": return 35;
                case "16": return 36;
                default: return fallback;
            }
        }

        private static String releaseFromFingerprint(String fingerprint) {
            if (fingerprint == null)
                return null;
            int colon = fingerprint.indexOf(':');
            if (colon < 0 || colon + 1 >= fingerprint.length())
                return null;
            int slash = fingerprint.indexOf('/', colon + 1);
            if (slash < 0)
                return null;
            String release = fingerprint.substring(colon + 1, slash).trim();
            return release.matches("(?:9|1[0-6])") ? release : null;
        }
    }
}
