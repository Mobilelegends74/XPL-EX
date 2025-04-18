package eu.faircode.xlua.x.runtime.reflect;

import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;

public class DynamicType {
    private static final String TAG = "XLua.DynamicType";


    public static boolean classImplementInterface(Class<?> clazz, Class<?> interfaceClass) {
        if (clazz == null || interfaceClass == null || !interfaceClass.isInterface()) {
            if(DebugUtil.isDebug())
                Log.e(TAG, "Error Type does not Inherit Interface! Type=" + Str.toStringOrNull(clazz) + " Interface=" + Str.toStringOrNull(interfaceClass));

            return false;
        }
        return interfaceClass.isAssignableFrom(clazz);
    }

    /*public static <T> T ensureTypeConversion(Object value, Class<?> targetType) {
        if(value == null) return null;
        try {

            if(value instanceof Number) {
                //Its a Number Type
                Number num = (Number)value;
                Class<?> boxTypeOfTarget = boxType(targetType); //Since Value will be Boxed ?

            }

        } catch (Exception e) {
            Log.e(TAG, "Type conversion failed: " + e.getMessage() +
                    " Value type: " + value.getClass().getName() + " Stack=" + Log.getStackTraceString(e));
            return null;
        }
    }*/

    //Finish this
    /*public static <T> T convertType(Object value, Class<?> targetType) {
        if(value == null) return null;
        try {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Dynamic Type, Converting Value Type=" + value.getClass().getName());

            //Assume UnBox FIRST if missing Target Type ?
            if(value instanceof Number) {
                Number num = (Number) value;

            }


        }catch (Exception e) {
            Log.e(TAG, "Type conversion failed: " + e.getMessage() +
                    " Value type: " + value.getClass().getName() + " Stack=" + Log.getStackTraceString(e));
            return null;
        }
    }*/


    @SuppressWarnings("unchecked")
    public static <T> T convertValue(Object value) {
        if (value == null) return null;
        try {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Dynamic Type, Converting Value Type=" + value.getClass().getName());

            // If the desired type is the same as the value type, return directly
            if (((Class<T>)value.getClass()).isInstance(value)) {
                return (T)value;
            }

            // Handle primitive type conversions
            if (value instanceof Number) {
                Number num = (Number)value;

                // Use type literal comparison to determine the target type
                Class<?> targetType = ((Class<T>)value.getClass());
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Dynamic Type is of Number, Target Type=" + targetType.getName());

                if (Integer.class.equals(targetType) || int.class.equals(targetType)) {
                    return (T)Integer.valueOf(num.intValue());
                }
                if (Long.class.equals(targetType) || long.class.equals(targetType)) {
                    return (T)Long.valueOf(num.longValue());
                }
                if (Float.class.equals(targetType) || float.class.equals(targetType)) {
                    return (T)Float.valueOf(num.floatValue());
                }
                if (Double.class.equals(targetType) || double.class.equals(targetType)) {
                    return (T)Double.valueOf(num.doubleValue());
                }
                if (Short.class.equals(targetType) || short.class.equals(targetType)) {
                    return (T)Short.valueOf(num.shortValue());
                }
                if (Byte.class.equals(targetType) || byte.class.equals(targetType)) {
                    return (T)Byte.valueOf(num.byteValue());
                }
            }


            // If no specific conversion is needed, try direct casting
            return (T)value;
        } catch (ClassCastException e) {
            Log.e(TAG, "Type conversion failed: " + e.getMessage() +
                    " Value type: " + value.getClass().getName() + " Stack=" + Log.getStackTraceString(e));
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static  <T> T tryCast(Object o) {
        if(o != null) {
            try {
                return (T)o;
            }catch (Exception ignored) { }
        }
        return null;
    }

    public static Class<?> boxType(Class<?> type) {
        if (type == boolean.class)
            return Boolean.class;
        else if (type == byte.class)
            return Byte.class;
        else if (type == char.class)
            return Character.class;
        else if (type == short.class)
            return Short.class;
        else if (type == int.class)
            return Integer.class;
        else if (type == long.class)
            return Long.class;
        else if (type == float.class)
            return Float.class;
        else if (type == double.class)
            return Double.class;
        return type;
    }

    public static Object coerceValue(Class<?> returnType, Object value) {
        // TODO: check for null primitives
        Class<?> valueType = value.getClass();
        if(valueType == Double.class || valueType == Float.class || valueType == Long.class || valueType == Integer.class || valueType == String.class) {
            Class<?> boxReturnType = boxType(returnType);
            if(boxReturnType == Double.class || boxReturnType == Float.class || boxReturnType == Long.class || boxReturnType == Integer.class || boxReturnType == String.class) {
                switch (boxReturnType.getName()) {
                    case "java.lang.Integer":
                        return
                                valueType == Double.class ? ((Double) value).intValue() :
                                        valueType == Float.class ? ((Float) value).intValue() :
                                                valueType == Long.class ? ((Long) value).intValue() :
                                                        valueType == String.class ? Str.tryParseInt(String.valueOf(value)) : value;
                    case "java.lang.Double":
                        return
                                valueType == Integer.class ? Double.valueOf((Integer) value) :
                                        valueType == Float.class ? Double.valueOf((Float) value) :
                                                valueType == Long.class ? Double.valueOf((Long) value) :
                                                        valueType == String.class ? Str.tryParseDouble(String.valueOf(value)) : value;
                    case "java.lang.Float":
                        return
                                valueType == Integer.class ? Float.valueOf((Integer) value) :
                                        valueType == Double.class ? ((Double) value).floatValue() :
                                                valueType == Long.class ? ((Long) value).floatValue() :
                                                        valueType == String.class ? Str.tryParseFloat(String.valueOf(value)) : value;
                    case "java.lang.Long":
                        return
                                valueType == Integer.class ? Long.valueOf((Integer) value) :
                                        valueType == Double.class ? ((Double) value).longValue() :
                                                valueType == Float.class ? ((Float) value).longValue() :
                                                        valueType == String.class ? Str.tryParseLong(String.valueOf(value)) : value;
                    case "java.lang.String":
                        return
                                valueType == Integer.class ? Integer.toString((int) value) :
                                        valueType == Double.class ? Double.toString((double) value) :
                                                valueType == Float.class ? Float.toString((float) value) :
                                                        valueType == Long.class ? Long.toString((long) value) : value;
                }
            }
        }


        // Lua 5.2 auto converts numbers into floating or integer values
        if (Integer.class.equals(value.getClass())) {
            if (long.class.equals(returnType)) return (long) (int) value;
            else if (float.class.equals(returnType)) return (float) (int) value;
            else if (double.class.equals(returnType))
                return (double) (int) value;
        } else if (Double.class.equals(value.getClass())) {
            if (float.class.equals(returnType))
                return (float) (double) value;
        } else if (value instanceof String && int.class.equals(returnType)) {
            return Integer.parseInt((String) value);
        }
        else if (value instanceof String && long.class.equals(returnType)) {
            return Long.parseLong((String) value);
        }
        else if (value instanceof String && float.class.equals(returnType))
            return Float.parseFloat((String) value);
        else if (value instanceof String && double.class.equals(returnType))
            return Double.parseDouble((String) value);

        return value;
    }
}
