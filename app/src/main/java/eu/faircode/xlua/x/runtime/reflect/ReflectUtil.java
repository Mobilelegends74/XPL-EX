package eu.faircode.xlua.x.runtime.reflect;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.xlua.LibUtil;


public class ReflectUtil {
    private static final String TAG = LibUtil.generateTag(ReflectUtil.class);


    public static <T> T tryCreateNewInstanceWild(String className, Object... args) { return tryCreateNewInstanceWild(tryGetClassForName(className), args); }

    /**
     * Attempts to create a new instance of the specified class using constructor with the provided arguments.
     * Returns null instead of throwing exceptions if instantiation fails.
     *
     * @param clazz The class to instantiate
     * @param args The constructor arguments
     * @return A new instance of the class or null if instantiation fails
     */
    public static <T> T tryCreateNewInstanceWild(Class<?> clazz, Object... args) {
        if (clazz == null) {
            return null;
        }

        try {
            // If no args, try the default constructor
            if (args == null || args.length == 0) {
                try {
                    Constructor<?> defaultConstructor = clazz.getDeclaredConstructor();
                    defaultConstructor.setAccessible(true);
                    return (T)defaultConstructor.newInstance();
                } catch (NoSuchMethodException e) {
                    // No default constructor, continue to try with args
                }
            }

            // Try with provided args
            if (args != null && args.length > 0) {
                // Get the argument types
                Class<?>[] argTypes = new Class<?>[args.length];
                for (int i = 0; i < args.length; i++) {
                    argTypes[i] = args[i] != null ? args[i].getClass() : null;
                }

                // Try first with exact match
                try {
                    Constructor<?> constructor = clazz.getDeclaredConstructor(argTypes);
                    constructor.setAccessible(true);
                    return (T)constructor.newInstance(args);
                } catch (NoSuchMethodException e) {
                    // If exact match fails, try to find a compatible constructor
                    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                    for (Constructor<?> constructor : constructors) {
                        if (isCompatibleConstructor(constructor, args)) {
                            constructor.setAccessible(true);
                            @SuppressWarnings("unchecked")
                            T instance = (T) constructor.newInstance(args);
                            return instance;
                        }
                    }
                }
            }

            // Final fallback: try all constructors in order of parameter count (simplest first)
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Arrays.sort(constructors, new ConstructorComparator());

            for (Constructor<?> constructor : constructors) {
                try {
                    constructor.setAccessible(true);
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    int paramCount = paramTypes.length;
                    Object[] actualArgs = new Object[paramCount];

                    // Fill with default values or provided args (as many as we have)
                    for (int i = 0; i < paramCount; i++) {
                        Class<?> paramType = paramTypes[i];
                        if (i < args.length && args[i] != null) {
                            actualArgs[i] = args[i];
                        } else {
                            actualArgs[i] = getDefaultValue(paramType);
                        }
                    }

                    @SuppressWarnings("unchecked")
                    T instance = (T) constructor.newInstance(actualArgs);
                    return instance;
                } catch (Exception e) {
                    // Try next constructor
                }
            }
        } catch (Exception e) {
            // Any other errors, return null
        }

        return null;
    }

    /**
     * Attempts to create a new instance of the specified class using constructor with the provided arguments.
     * Returns null instead of throwing exceptions if instantiation fails.
     *
     * @param clazz The class to instantiate
     * @param args The constructor arguments
     * @return A new instance of the class or null if instantiation fails
     */
    public static <T> T tryCreateNewInstance(Class<T> clazz, Object... args) {
        if (clazz == null) {
            return null;
        }

        try {
            // If no args, try the default constructor
            if (args == null || args.length == 0) {
                try {
                    Constructor<T> defaultConstructor = clazz.getDeclaredConstructor();
                    defaultConstructor.setAccessible(true);
                    return defaultConstructor.newInstance();
                } catch (NoSuchMethodException e) {
                    // No default constructor, continue to try with args
                }
            }

            // Try with provided args
            if (args != null && args.length > 0) {
                // Get the argument types
                Class<?>[] argTypes = new Class<?>[args.length];
                for (int i = 0; i < args.length; i++) {
                    argTypes[i] = args[i] != null ? args[i].getClass() : null;
                }

                // Try first with exact match
                try {
                    Constructor<T> constructor = clazz.getDeclaredConstructor(argTypes);
                    constructor.setAccessible(true);
                    return constructor.newInstance(args);
                } catch (NoSuchMethodException e) {
                    // If exact match fails, try to find a compatible constructor
                    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                    for (Constructor<?> constructor : constructors) {
                        if (isCompatibleConstructor(constructor, args)) {
                            constructor.setAccessible(true);
                            @SuppressWarnings("unchecked")
                            T instance = (T) constructor.newInstance(args);
                            return instance;
                        }
                    }
                }
            }

            // Final fallback: try all constructors in order of parameter count (simplest first)
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Arrays.sort(constructors, new ConstructorComparator());

            for (Constructor<?> constructor : constructors) {
                try {
                    constructor.setAccessible(true);
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    int paramCount = paramTypes.length;
                    Object[] actualArgs = new Object[paramCount];

                    // Fill with default values or provided args (as many as we have)
                    for (int i = 0; i < paramCount; i++) {
                        Class<?> paramType = paramTypes[i];
                        if (i < args.length && args[i] != null) {
                            actualArgs[i] = args[i];
                        } else {
                            actualArgs[i] = getDefaultValue(paramType);
                        }
                    }

                    @SuppressWarnings("unchecked")
                    T instance = (T) constructor.newInstance(actualArgs);
                    return instance;
                } catch (Exception e) {
                    // Try next constructor
                }
            }
        } catch (Exception e) {
            // Any other errors, return null
        }

        return null;
    }

    /**
     * Comparator for sorting constructors by parameter count.
     */
    private static class ConstructorComparator implements Comparator<Constructor<?>> {
        public int compare(Constructor<?> c1, Constructor<?> c2) {
            return c1.getParameterTypes().length - c2.getParameterTypes().length;
        }
    }

    /**
     * Checks if the constructor is compatible with the given arguments.
     */
    private static boolean isCompatibleConstructor(Constructor<?> constructor, Object[] args) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        if (paramTypes.length != args.length) {
            return false;
        }

        for (int i = 0; i < paramTypes.length; i++) {
            // Handle null arguments (they can match any non-primitive type)
            if (args[i] == null) {
                if (paramTypes[i].isPrimitive()) {
                    return false; // Primitives can't be null
                }
                continue;
            }

            // Check if parameter type is assignable from argument type
            if (!isAssignableWithConversion(paramTypes[i], args[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if a value can be assigned to a parameter, accounting for primitive conversions.
     */
    private static boolean isAssignableWithConversion(Class<?> paramType, Object arg) {
        Class<?> argType = arg.getClass();

        // Direct assignment
        if (paramType.isAssignableFrom(argType)) {
            return true;
        }

        // Primitive conversions
        if (paramType.isPrimitive() && isPrimitiveWrapperOf(argType, paramType)) {
            return true;
        }

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

    /**
     * Checks if a class is the wrapper of a primitive type.
     */
    private static boolean isPrimitiveWrapperOf(Class<?> wrapperClass, Class<?> primitiveClass) {
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
    private static Object getDefaultValue(Class<?> type) {
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

    public static String getObjectTypeOrNull(Class<?> clazz)  { return clazz == null ? "null" : clazz.getName(); }
    public static String getObjectTypeOrNull(Object o) { return o == null ? "null" : o.getClass().getName(); }


    public static Field tryGetField(String className, String fieldName) { return tryGetField(tryGetClassForName(className), fieldName); }
    public static Field tryGetField(Class<?> clazz, String fieldName) {
        return TryRun.get(() -> {
            Field[] methods = clazz.getDeclaredFields();
            for(Field f : methods) {
                if(f.getName().equalsIgnoreCase(fieldName))
                    return f;
            }

            return null;
        });
    }


    public static Method tryGetMethod(String className, String methodName) { return tryGetMethod(tryGetClassForName(className), methodName); }
    public static Method tryGetMethod(Class<?> clazz, String methodName) {
        return TryRun.get(() -> {
            Method[] methods = clazz.getDeclaredMethods();
            for(Method m : methods) {
                if(m.getName().equalsIgnoreCase(methodName))
                    return m;
            }

            return null;
        });
    }


    public static int useFieldValueOrDefaultInt(String clazz, String fieldName, int defaultValue) {
        DynamicField field = new DynamicField(clazz, fieldName).setAccessible(true);
        if(!field.isValid()) return defaultValue;
        Object val = field.tryGetValueStatic();
        if(val == null) return defaultValue;
        try {
            if(val instanceof Integer)
                return (int)val;
        }catch (ClassCastException ignored) { }
        return defaultValue;
    }

    public static int useFieldValueOrDefaultInt(Class<?> clazz, String fieldName, int defaultValue) {
        DynamicField field = new DynamicField(clazz, fieldName).setAccessible(true);
        if(!field.isValid()) return defaultValue;
        Object val = field.tryGetValueStatic();
        if(val == null) return defaultValue;
        try {
            if(val instanceof Integer)
                return (int)val;
        }catch (ClassCastException ignored) { }
        return defaultValue;
    }


    public static int useFieldValueOrDefaultInt(Class<?> clazz, String fieldName, int defaultValue, int badValue) {
        DynamicField field = new DynamicField(clazz, fieldName).setAccessible(true);
        if(!field.isValid()) return defaultValue;
        Object val = field.tryGetValueStatic();
        if(val == null) return defaultValue;
        try {
            if(val instanceof Integer) {
                int vall = (int)val;
                if(vall == badValue)
                    return defaultValue;

                return vall;
            }
        }catch (ClassCastException ignored) { }
        return defaultValue;
    }

    public static String useFieldValueOrDefaultString(Class<?> clazz, String fieldName, String defaultValue) {
        DynamicField field = new DynamicField(clazz, fieldName).setAccessible(true);
        if(!field.isValid()) return defaultValue;
        Object val = field.tryGetValueStatic();
        if(val == null) return defaultValue;
        try {
            if(val instanceof String) {
                return (String)val;
            }
        }catch (ClassCastException ignored) { }
        return defaultValue;
    }

    public static Field tryGetFieldEx(Class<?> clazz, String fieldA, String fieldB) {
        Field field = tryGetField(clazz, fieldA, false);
        if(field != null)
            return field;

        return tryGetField(clazz, fieldB, false);
    }

    public static Method tryGetMethodEx(Class<?> clazz, String methodA, String methodB, Class<?>... params) {
        Method method = tryGetMethod(clazz, methodA, params);
        if(method != null)
            return method;

        return tryGetMethod(clazz, methodB, params);
    }

    public static Method tryGetMethod(String className, String methodName, Class<?>... params) {
        try {
            return tryGetMethod(Class.forName(className), methodName, params);
        }catch (Exception e) {
            Log.e(TAG, "[tryGetMethod] Failed to get Method: " + methodName + " Error: " + e.getMessage());
            return null;
        }
    }

    public static Method tryGetMethodEx(Class<?> clazz, String methodName, Class<?>... params) {
        Method method = tryGetMethod(clazz, methodName, params);
        return method == null ?
                tryGetMethodWilCard(clazz, methodName, params == null || params.length == 0 ? 0 : params.length) : method;
    }

    public static Method tryGetMethod(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            return clazz.getMethod(methodName, params);
        }catch (Exception e) {
            try {
                return clazz.getDeclaredMethod(methodName, params);
            }catch (Exception ee) {
                Log.e(TAG, "[tryGetMethod] Failed to get Method: " + methodName + " Error: " + e.getMessage());
                return null;
            }
        }
    }

    public static Method tryGetMethodWilCard(Class<?> clazz, String methodName, int argCount) {
        try {
            List<Method> allMethods = new ArrayList<>();
            allMethods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
            allMethods.addAll(Arrays.asList(clazz.getMethods()));

            Method lastGoodMethod = null;
            for(Method m : allMethods) {
                if(m.getName().equalsIgnoreCase(methodName)) {
                    lastGoodMethod = m;
                    if(argCount == -1)
                        return m;
                    else {
                        if(m.getParameterCount() == argCount)
                            return m;
                    }
                }
            }
            return lastGoodMethod;
        }catch (Exception e) {
            Log.e(TAG, "Failed to use Reflection to find Method: " + methodName + " using Wild Card Search. Error: " + e);
            return null;
        }
    }

    public static Class<?> tryGetClassForName(String className) {
        try {
            return Class.forName(className);
        }catch (Exception e) {
            Log.e(TAG, "[tryGetClassForName] Failed to Get Class for name: " + e.getMessage());
            return null;
        }
    }

    public static Field tryGetField(String className, String fieldName, boolean setAccessible) { return tryGetField(tryGetClassForName(className), fieldName, setAccessible); }
    public static Field tryGetField(Class<?> clazz, String fieldName, boolean setAccessible) {
        if(clazz == null) {
            Log.e(TAG, "[tryGetField] Failed as Class was Null: " + fieldName);
            return null;
        }

        try {
            Field field = clazz.getField(fieldName);
            if(setAccessible)
                field.setAccessible(true);

            return field;
        }catch (Exception e) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                if(setAccessible)
                    field.setAccessible(true);

                return field;
            }catch (Exception ee) {
                Log.e(TAG, "[tryGetField] Failed to Get Field: " + fieldName + " Set Accessible: " + setAccessible + " Error: " + ee.getMessage());
                return null;
            }
        }
    }

    public static Object tryGetFieldValueInstance(Object instance, String fieldName) { return tryGetFieldValueInstance(instance, fieldName, false); }
    public static Object tryGetFieldValueInstance(Object instance, String fieldName, boolean setAccessible) {
        if(instance == null) {
            Log.e(TAG, "[tryGetFieldValueInstance] Failed to get Field Value as Instance object is Null: " + fieldName + " Set Accessible: " + setAccessible);
            return null;
        }

        Field field = tryGetField(instance.getClass(), fieldName, setAccessible);
        if(field == null) {
            Log.e(TAG, "[tryGetFieldValueInstance] Failed to get Field Value as Field is Null: " + fieldName + " Set Accessible: " + setAccessible);
            return null;
        }

        try {
            return field.get(instance);
        }catch (Exception e) {
            Log.e(TAG, "[tryGetFieldValueInstance] Failed to get Field Value... Class: " + instance.getClass().getName() + " Field: " + fieldName + " Set Accessible: " + setAccessible);
            return null;
        }
    }

    public static Object tryGetFieldValue(String className, String fieldName) { return tryGetFieldValue(className, fieldName, false); }
    public static Object tryGetFieldValue(String className, String fieldName, boolean setAccessible) { return tryGetFieldValue(tryGetClassForName(className), fieldName, setAccessible); }

    public static Object tryGetFieldValue(Class<?> clazz, String fieldName) { return tryGetFieldValue(clazz, fieldName, false); }
    public static Object tryGetFieldValue(Class<?> clazz, String fieldName, boolean setAccessible) {
        Field field = tryGetField(clazz, fieldName, setAccessible);
        if(field == null) {
            Log.e(TAG, "[tryGetFieldValue] Failed to get Field Value as Field is Null: " + fieldName + " Set Accessible: " + setAccessible);
            return null;
        }

        try {
            return field.get(null);
        }catch (Exception e) {
            Log.e(TAG, "[tryGetFieldValue] Failed to get Field Value... Class: " + clazz.getName() + " Field: " + fieldName + " Set Accessible: " + setAccessible);
            return null;
        }
    }
}
