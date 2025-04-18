package eu.faircode.xlua.x.hook;

import android.util.Log;

import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.hooks.LuaHookResolver;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

public class HookResolverUtils {
    private static final String TAG = LibUtil.generateTag(HookResolverUtils.class);

    public static class TypeWild { }

    public static boolean isWildCardType(Class<?> clazz) { return clazz != null && (clazz.equals(WildcardType.class) || clazz.equals(LuaHookResolver.TypeWild.class) || clazz.equals(HookResolverUtils.TypeWild.class)); }

    public static Class<?>[] getParameterTypesArray(String[] params, ClassLoader loader) {
        List<Class<?>> paramTypes = getParameterTypes(params, loader);
        Class<?>[] arr = new Class[paramTypes.size()];
        if(!paramTypes.isEmpty()) {
            for(int i = 0; i < paramTypes.size(); i++) {
                arr[i] = paramTypes.get(i);
            }
        }

        return arr;
    }

    public static List<Class<?>> getParameterTypes(String[] params, ClassLoader loader) {
        List<Class<?>> types = new ArrayList<>();
        if(!ArrayUtils.isValid(params))
            return types;

        try {
            for(String p : params) {
                String trimmed = Str.trimOriginal(p);
                if(Str.isEmpty(trimmed))
                    continue;

                Class<?> resolved = resolveClass(trimmed, loader);
                if(resolved == null)
                    continue;

                types.add(resolved);
            }

            return types;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Resolve Param Types! Error=" + e);
            return types;
        }
    }

    public static boolean isVoid(String s) { return s.equalsIgnoreCase("void") || s.equalsIgnoreCase(Void.class.getName()) || s.equalsIgnoreCase(void.class.getName()) || s.equalsIgnoreCase(Void.TYPE.getName()); }
    public static boolean isFloat(String s) { return s.equalsIgnoreCase("float") || s.equalsIgnoreCase("f") || s.equalsIgnoreCase(Float.class.getName()) || s.equalsIgnoreCase(float.class.getName()); }
    public static boolean isChar(String s) { return s.equalsIgnoreCase("char") || s.equalsIgnoreCase("character") || s.equalsIgnoreCase("c") || s.equalsIgnoreCase(Character.class.getName()) || s.equalsIgnoreCase(char.class.getName()); }
    public static boolean isDouble(String s) { return s.equalsIgnoreCase("double") || s.equalsIgnoreCase("d") || s.equalsIgnoreCase(Double.class.getName()) || s.equalsIgnoreCase(double.class.getName()); }
    public static boolean isBool(String s) { return s.equalsIgnoreCase("bool") || s.equalsIgnoreCase("boolean") || s.equalsIgnoreCase(Boolean.class.getName()) || s.equalsIgnoreCase(boolean.class.getName()); }
    public static boolean isByte(String s) { return s.equalsIgnoreCase("byte") || s.equalsIgnoreCase("b") || s.equalsIgnoreCase(Byte.class.getName()) || s.equalsIgnoreCase(byte.class.getName()); }
    public static boolean isInt16(String s) { return s.equalsIgnoreCase("short") || s.equalsIgnoreCase("s") || s.equalsIgnoreCase(Short.class.getName()) || s.equalsIgnoreCase(short.class.getName()); }
    public static boolean isInt32(String s) { return s.equalsIgnoreCase("int") || s.equalsIgnoreCase("i") || s.equalsIgnoreCase(Integer.class.getName()) || s.equalsIgnoreCase(int.class.getName()); }
    public static boolean isInt64(String s) { return s.equalsIgnoreCase("long") || s.equalsIgnoreCase("l") || s.equalsIgnoreCase(Long.class.getName()) || s.equalsIgnoreCase(long.class.getName()); }


    public static boolean isSomeStringType(String s) {
        //String builders as well ?
        //We can also do a wild card for similar types like String StringBuilder, etc
        //Then the system when setting the value can handle the conversion
        return s.equalsIgnoreCase("string") || s.equalsIgnoreCase(String.class.getName()) || s.equalsIgnoreCase(CharSequence.class.getName()) || s.equalsIgnoreCase("charsequence");
    }

    public static boolean isArray(String s) { return s.startsWith("[") || s.endsWith("[]"); }
    public static String removeArray(String s) {
        //Support 2D / 3D
        String trimmed = Str.trimOriginal(s);
        if(trimmed == null) return s;

        if(trimmed.startsWith("[")) {
            trimmed = trimmed.substring(1);
            if(trimmed.startsWith("L")) trimmed = trimmed.substring(1);
        }

        if(trimmed.endsWith("[]")) trimmed = trimmed.substring(0, trimmed.length() - 2);
        if(trimmed.endsWith(";")) trimmed = trimmed.substring(0, trimmed.length() - 1);

        //if(trimmed.endsWith(".Java")) trimmed = trimmed.substring(0, trimmed.length() - 5);

        return trimmed.trim();
    }

    public static boolean isSameTypes(Class<?>[] aTypes, Class<?>[] bTypes) {
        if(!ArrayUtils.isValid(aTypes)) return !ArrayUtils.isValid(bTypes);
        if(!ArrayUtils.isValid(bTypes)) return false;

        int len = ArrayUtils.safeLength(aTypes);
        if(len != ArrayUtils.safeLength(bTypes)) return false;
        if(len == 0) return true;

        for(int i = 0; i < len; i++) {
            if(!isSameType(aTypes[i], bTypes[i])) {
                return false;
            }
        }

        return true;
    }

    public static boolean isSameType(Class<?> a, Class<?> b) {
        if(a == null) return b == null;
        if(b == null) return false;

        if(a.isAssignableFrom(b)) return true;

        String aName = a.getName();
        String bName = b.getName();
        return isSameType(aName, bName);
    }

    public static boolean isSameType(String a, String b) {
        //Make sure we compare with resolved types
        a = Str.trimOriginal(a);
        b = Str.trimOriginal(b);
        if(Str.isEmpty(a)) return Str.isEmpty(b);   //Assume void ?
        if(Str.isEmpty(b)) return false;

        if(isArray(a)) return isArray(b) && isSameType(removeArray(a), removeArray(b));
        if(isArray(b)) return false;

        if(a.startsWith("java.lang.Collection") && b.startsWith("java.lang.Collection"))
            return true;

        if(a.startsWith("java.lang.List") && b.startsWith("java.lang.List"))
            return true;

        if(isVoid(a) && isVoid(b))
            return true;

        if(isByte(a) && isByte(b))
            return true;

        if(isInt16(a) && isInt16(b))
            return true;

        if(isInt32(a) && isInt32(b))
            return true;

        if(isInt64(a) && isInt64(b))
            return true;

        if(isFloat(a) && isFloat(b))
            return true;

        if(isChar(a) && isChar(b))
            return true;

        if(isDouble(a) && isDouble(b))
            return true;

        if(isBool(a) && isBool(b))
            return true;

        return a.equalsIgnoreCase(b);
    }

    public static Class<?> resolveClass(String name, ClassLoader loader) {
        try {
            //Hmm assume its void ? what if target is whatever but no specify return ?
            //Be careful with this logic
            if(name == null || name.isEmpty() || isVoid(name)) return Void.TYPE;
            if(name.equals("*")) return TypeWild.class;

            if(isArray(name)) {
                String extractedType = removeArray(name);
                if(isBool(extractedType))
                    return boolean[].class;
                if(isByte(extractedType))
                    return byte[].class;
                if(isInt16(extractedType))
                    return short[].class;
                if(isInt32(extractedType))
                    return int[].class;
                if(isInt64(extractedType))
                    return long[].class;
                if(isChar(extractedType))
                    return char[].class;
                if(isFloat(extractedType))
                    return float[].class;
                if(isDouble(extractedType))
                    return double[].class;
            } else {
                if(isBool(name))
                    return boolean.class;
                if(isByte(name))
                    return byte.class;
                if(isInt16(name))
                    return short.class;
                if(isInt32(name))
                    return int.class;
                if(isInt64(name))
                    return long.class;
                if(isChar(name))
                    return char.class;
                if(isFloat(name))
                    return float.class;
                if(isDouble(name))
                    return double.class;
            }

            return Class.forName(name, false, loader);
        }catch (Exception e) {
            Log.e(TAG, "Error Resolving Class, Name=" + name + " Error=" + e);
            return null;
            //Maybe late loading ?
        }
    }
}
