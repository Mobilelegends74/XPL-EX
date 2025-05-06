package eu.faircode.xlua.x.runtime.reflect;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.interfaces.IValidator;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.xlua.LibUtil;

@SuppressWarnings({"unused"})
public class DynClass implements IReflect {
    private static final String TAG = LibUtil.generateTag(DynClass.class);

    public static DynClass create(Class<?> clazz) { return new DynClass(clazz); }
    public static DynClass create(String className) { return new DynClass(className); }
    public static DynClass create(String className, Context context) { return new DynClass(className, context); }
    public static DynClass create(String className, Context context, boolean resolveType) { return new DynClass(className, context, resolveType); }
    public static DynClass create(String className, Context context, boolean resolveType, boolean useContextClassLoader) { return new DynClass(className, context, resolveType, useContextClassLoader); }
    public static DynClass create(String className, boolean initialize, Context context) { return new DynClass(className, initialize, context); }
    public static DynClass create(String className, boolean initialize, Context context, boolean resolveType) { return new DynClass(className, initialize, context, resolveType); }
    public static DynClass create(String className, boolean initialize, Context context, boolean resolveType, boolean useContextClassLoader) { return new DynClass(className, initialize, context, resolveType, useContextClassLoader); }
    public static DynClass create(String className, boolean initialize, ClassLoader loader) { return new DynClass(className, initialize, loader); }

    private Class<?> clazz;
    private String name;

    public Class<?> getRawClass() { return clazz; }
    public boolean isInstance(Object obj) { return clazz != null && obj != null && clazz.isInstance(obj); }
    public boolean isAssignableFrom(Class<?> cls) { return clazz != null && cls != null && clazz.isAssignableFrom(cls); }

    @Override
    public boolean isValid() { return !Str.isEmpty(name) && clazz != null; }
    @Override
    public String getName() { return name; }
    @Override
    public DynClass getClazz() { return this; }
    @Override
    public void setAccessible(boolean accessible) { }
    @Override
    public Exception getLastException() { return null; }
    @Override
    public boolean wasSuccessful() { return true; }

    public DynClass(String className, Context context) { this.name = className; this.clazz = forName(className, context); }
    public DynClass(String className, Context context, boolean resolveType) { this.name = className; this.clazz = forName(className, context, resolveType); }
    public DynClass(String className, Context context, boolean resolveType, boolean useContextClassLoader) { this.name = className; this.clazz = forName(className, context, resolveType, useContextClassLoader); }
    public DynClass(String className, boolean initialize, Context context) { this.name = className; this.clazz = forName(className, initialize, context); }
    public DynClass(String className, boolean initialize, Context context, boolean resolveType) { this.name = className; this.clazz = forName(className, initialize, context, resolveType); }
    public DynClass(String className, boolean initialize, Context context, boolean resolveType, boolean useContextClassLoader) { this.name = className; this.clazz = forName(className, initialize, context, resolveType, useContextClassLoader); }
    public DynClass(String className, boolean initialize, ClassLoader loader) { this.name = className; this.clazz = forName(className, initialize, loader); }

    public DynClass(String className) { this.name = className; this.clazz = forName(className); }
    public DynClass(Class<?> clazz) {
        if(clazz != null) {
            this.clazz = clazz;
            this.name = clazz.getName();
        }
    }

    public Field getField(String name) { return getField(true, name); }
    public Field getField(boolean doSetAccessible, String name) {
        if(!isValid()) return null;
        try {
            try {
                Field field = clazz.getDeclaredField(name);
                DynUtils.setAccessible(field, true, doSetAccessible);
                return field;
            }catch (Exception e) {
               Field[] fields = clazz.getDeclaredFields();
               for(Field field : fields) {
                   if(Str.matches(field.getName(), name)) {
                       DynUtils.setAccessible(field, true, doSetAccessible);
                       return field;
                   }
               }
            }
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to Find Field [%s] This [%s] Error=%s",
                    name,
                    toString(),
                    e));
        }

        return null;
    }

    public Method getMethod(String name, Class<?>... paramTypes) { return getMethod(true, name, paramTypes); }
    public Method getMethod(boolean doSetAccessible, String name, Class<?>... paramTypes) {
        if(!isValid()) return null;
        Class<?>[] parsedTypes = DynTypeUtils.ensureGoodTypes(paramTypes);
        try {
            if(!ArrayUtils.isValid(parsedTypes)) {
                try {
                    Method method = clazz.getDeclaredMethod(name);
                    DynUtils.setAccessible(method, true, doSetAccessible);
                    return method;
                }catch (Exception e) {
                    Method[] methods = clazz.getDeclaredMethods();
                    for(Method method : methods) {
                        if(Str.matches(method.getName(), name)) {
                            DynUtils.setAccessible(method, true, doSetAccessible);
                            return method;
                        }
                    }
                }
            } else {
                try {
                    Method method = clazz.getDeclaredMethod(name, paramTypes);
                    DynUtils.setAccessible(method, true, doSetAccessible);
                    return method;
                }catch (Exception e) {
                    Method[] methods = clazz.getDeclaredMethods();
                    for(Method method : methods) {
                        if(Str.matches(method.getName(), name) && DynTypeUtils.paramsMatch(method.getParameterTypes(), parsedTypes)) {
                            DynUtils.setAccessible(method, true, doSetAccessible);
                            return method;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to Find Method [%s] for Types (%s) This [%s] Error=%s",
                    name,
                    Str.toStringOrNull(paramTypes),
                    toString(),
                    e));
        }

        return null;
    }

    public Constructor<?> getConstructor(Class<?>... types) { return getConstructor(true, types); }
    public Constructor<?> getConstructor(boolean doSetAccessible, Class<?>... paramTypes) {
        if(!isValid()) return null;
        Class<?>[] parsedTypes = DynTypeUtils.ensureGoodTypes(paramTypes);
        try {
            if(!ArrayUtils.isValid(parsedTypes)) {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                DynUtils.setAccessible(constructor, true, doSetAccessible);
                return constructor;
            } else {
                try {
                    Constructor<?> constructor = clazz.getDeclaredConstructor(parsedTypes);
                    DynUtils.setAccessible(constructor, true, doSetAccessible);
                    return constructor;
                } catch (Exception e) {
                    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                    for(Constructor<?> constructor : constructors) {
                        if(DynTypeUtils.paramsMatch(constructor.getParameterTypes(), parsedTypes)) {
                            DynUtils.setAccessible(constructor, true, doSetAccessible);
                            return constructor;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Str.fm("Failed to Find Constructor for Types (%s) This [%s] Error=%s",
                    Str.toStringOrNull(paramTypes),
                    toString(),
                    e));
        }

        return null;
    }

    public static Class<?> forName(String className) { return forName(className, false, null, false, false); }

    public static Class<?> forName(String className, Context context) { return forName(className, false, context, false, true); }
    public static Class<?> forName(String className, Context context, boolean resolveType) { return forName(className, false, context, resolveType, false); }
    public static Class<?> forName(String className, Context context, boolean resolveType, boolean useContextClassLoader) { return forName(className, false, context, resolveType, useContextClassLoader); }

    public static Class<?> forName(String className, boolean initialize, Context context) { return forName(className, initialize, context, false, true); }
    public static Class<?> forName(String className, boolean initialize, Context context, boolean resolveType) { return forName(className, initialize, context, resolveType, false); }
    public static Class<?> forName(String className, boolean initialize, Context context, boolean resolveType, boolean useContextClassLoader) {
        return forName(
                resolveType && context != null ? DynTypeUtils.resolveClassName(context, className) : className,
                initialize,
                context != null && useContextClassLoader ? context.getClassLoader() : null);
    }

    public static Class<?> forName(String className, boolean initialize, ClassLoader loader) {
        return TryRun.get(() ->
                loader != null ?
                        Class.forName(className, initialize, loader) :
                        Class.forName(className));
    }

    @Override
    public int hashCode() { return Str.hashCode(Str.toObjectClassName(this.clazz)); }
    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof String || obj instanceof DynClass) return obj.hashCode() == this.hashCode();
        if(obj instanceof Class<?>) return this.equals(DynClass.create((Class<?>) obj));
        return false;
    }

    @NonNull
    @Override
    public String toString() { return toString(false); }
    public String toString(boolean useNewLine) {
        return StrBuilder.create()
                .appendField("Clazz Name", this.name)
                .appendNewLineOrSpace(useNewLine)
                .appendField("Clazz Type", Str.toStringOrNull(this.clazz))
                .appendNewLineOrSpace(useNewLine)
                .appendField("Clazz IsValid", this.isValid())
                .toString(true);
    }
}