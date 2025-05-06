package eu.faircode.xlua.x.runtime.reflect;

import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.telephony.SmsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.rootbox.XReflectUtils;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;

public class DynTypeUtils {

    //public static Object prepareForConversions()

    /**
     * Checks if a value can be assigned to a parameter, accounting for primitive conversions.
     */
    public static boolean isAssignableWithConversion(Class<?> paramType, Object arg) {
        if(paramType == null || arg == null)
            return false;

        Class<?> argType = arg.getClass();
        // Direct assignment
        if (paramType.isAssignableFrom(argType))
            return true;
        // Primitive conversions
        if (paramType.isPrimitive() && isPrimitiveWrapperOf(argType, paramType))
            return true;

        // Number conversions (Integer -> Long, etc.)
        if (Number.class.isAssignableFrom(argType)) {
            if (paramType == byte.class || paramType == Byte.class) return true;
            if (paramType == short.class || paramType == Short.class) return true;
            if (paramType == int.class || paramType == Integer.class) return true;
            if (paramType == long.class || paramType == Long.class) return true;
            if (paramType == float.class || paramType == Float.class) return true;
            if (paramType == double.class || paramType == Double.class) return true;
        }

        // String to primitive conversions
        if (arg instanceof String) {
            if (paramType == char.class || paramType == Character.class) {
                String str = (String) arg;
                return str.length() == 1;
            }
        }

        return false;
    }

    public static Class<?>[] ensureGoodTypes(Class<?>[] types) { return ensureGoodTypes(types, true); }
    public static Class<?>[] ensureGoodTypes(Class<?>[] types, boolean returnEmptyIfNull) {
        if(!ArrayUtils.isValid(types)) return returnEmptyIfNull ? new Class[0] : types;
        List<Class<?>> good = new ArrayList<>();
        for(Class<?> c : types) if(c != null) good.add(c);
        return good.toArray(new Class[0]);
    }

    public static boolean paramsMatch(Class<?>[] pOnes, Class<?>[] pTwos) { return paramsMatch(pOnes, pTwos, true); }
    public static boolean paramsMatch(Class<?>[] pOnes, Class<?>[] pTwos, boolean deepEquals) {
        int lengthOne = ArrayUtils.safeLength(pOnes);
        int lengthTwo = ArrayUtils.safeLength(pTwos);
        if(lengthOne != lengthTwo) return false;
        if(lengthOne < 1) return true;
        for(int i = 0; i < lengthOne; i++) {
            Class<?> one = pOnes[i];
            Class<?> two = pTwos[i];
            if(one == null) {
                if(two != null) return false;
                continue;
            } else if(two == null)
                return false;

            if(one.equals(two))
                continue;
            if(deepEquals)
                if(one.isAssignableFrom(two) || two.isAssignableFrom(one) || isPrimitiveBoxPair(one, two))
                    continue;

            return false;
        }

        return true;
    }

    public static boolean isPrimitiveBoxPair(Class<?> c1, Class<?> c2) {
        // Check for null
        if (c1 == null || c2 == null) return false;

        // One must be primitive and one must be non-primitive
        if (c1.isPrimitive() == c2.isPrimitive()) return false;

        // Get the primitive class (either c1 or c2)
        Class<?> primitiveClass = c1.isPrimitive() ? c1 : c2;
        Class<?> boxedClass = c1.isPrimitive() ? c2 : c1;

        // Check primitive-wrapper pairs
        return (primitiveClass == boolean.class && boxedClass == Boolean.class) ||
                (primitiveClass == byte.class && boxedClass == Byte.class) ||
                (primitiveClass == char.class && boxedClass == Character.class) ||
                (primitiveClass == short.class && boxedClass == Short.class) ||
                (primitiveClass == int.class && boxedClass == Integer.class) ||
                (primitiveClass == long.class && boxedClass == Long.class) ||
                (primitiveClass == float.class && boxedClass == Float.class) ||
                (primitiveClass == double.class && boxedClass == Double.class) ||
                (primitiveClass == void.class && boxedClass == Void.class);
    }

    /**
     * Checks if a class is the wrapper of a primitive type.
     */
    public static boolean isPrimitiveWrapperOf(Class<?> wrapperClass, Class<?> primitiveClass) {
        if (primitiveClass == boolean.class) return wrapperClass == Boolean.class;
        if (primitiveClass == byte.class) return wrapperClass == Byte.class;
        if (primitiveClass == char.class) return wrapperClass == Character.class;
        if (primitiveClass == short.class) return wrapperClass == Short.class;
        if (primitiveClass == int.class) return wrapperClass == Integer.class;
        if (primitiveClass == long.class) return wrapperClass == Long.class;
        if (primitiveClass == float.class) return wrapperClass == Float.class;
        if (primitiveClass == double.class) return wrapperClass == Double.class;
        return false;
    }

    /**
     * Gets the default value for a type (0 for numbers, false for boolean, etc.)
     */
    public static Object getDefaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0d;
        return null;
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
}
