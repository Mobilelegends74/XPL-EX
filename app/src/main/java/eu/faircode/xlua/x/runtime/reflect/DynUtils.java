package eu.faircode.xlua.x.runtime.reflect;

import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.telephony.SmsManager;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import eu.faircode.xlua.rootbox.XReflectUtils;
import eu.faircode.xlua.x.data.utils.ArrayUtils;

@SuppressWarnings("all")
public class DynUtils {


    public static Class<?>[] valueTypes(Object[] values) {
        if(values == null) return null;
        Class<?>[] types = new Class[values.length];
        if(ArrayUtils.isValid(values)) {
            for(int i = 0; i < values.length; i++)
                types[i] = values[i] != null ? values[i].getClass() : null;
        }

        return types;
    }

    public static Class<?>[] getParameterTypes(Object o) { return getParameterTypes(o, false); }
    public static Class<?>[] getParameterTypes(Object o, boolean emptyArrayIfNull) {
        try {
            if(o == null) return emptyArrayIfNull ? new Class[0] : null;
            Class<?>[] types = null;
            if(o instanceof Constructor) {
                Constructor c = (Constructor) o;
                types = c.getParameterTypes();
            }
            else if(o instanceof Method) {
                Method m = (Method) o;
                types = m.getParameterTypes();
            }
            else if(o instanceof Executable) {
                Executable e = (Executable) o;
                types = e.getParameterTypes();
            }

            return types == null ?
                    emptyArrayIfNull ? new Class[0] : null :
                    types;
        }catch (Exception ignored) {
            return emptyArrayIfNull ? new Class[0] : null;
        }
    }


    public static void setAccessible(Object o, boolean accessible) { setAccessible(o, accessible, true); }
    public static void setAccessible(Object o, boolean accessible, boolean doFlag) {
        if(o != null && doFlag) {
            try {
                if(o instanceof Constructor) {
                    Constructor c = (Constructor) o;
                    c.setAccessible(accessible);
                    return;
                }

                if(o instanceof Field) {
                    Field f = (Field) o;
                    f.setAccessible(accessible);
                    return;
                }

                if(o instanceof Method) {
                    Method m = (Method) o;
                    m.setAccessible(accessible);
                    return;
                }

                if(o instanceof Executable) {
                    Executable e = (Executable) o;
                    e.setAccessible(accessible);
                    return;
                }

            }catch (Exception ignored) { }
        }
    }


    public static boolean isCompatibleConstructor(Constructor<?> constructor, Object[] args) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        if (paramTypes.length != args.length)
            return false;

        for (int i = 0; i < paramTypes.length; i++) {
            // Handle null arguments (they can match any non-primitive type)
            if (args[i] == null) {
                if (paramTypes[i].isPrimitive())
                    return false; // Primitives can't be null
                continue;
            }

            if (!DynTypeUtils.isAssignableWithConversion(paramTypes[i], args[i]))
                return false;
        }

        return true;
    }
}
