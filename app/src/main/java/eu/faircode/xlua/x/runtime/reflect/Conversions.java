package eu.faircode.xlua.x.runtime.reflect;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.x.StrConversionUtils;
import eu.faircode.xlua.x.data.utils.ArrayUtils;

public class Conversions {
    public static final FieldBinding.ITypeConversion<Short> SHORT = new FieldBinding.ITypeConversion<Short>() {
        @Override
        public Short fromString(String s) { return StrConversionUtils.tryParseShort(s); }
        @Override
        public List<Short> list(int count) { return new ArrayList<>(count); }
        @Override
        public Object array(int count, boolean isPrimitive) { return ArrayUtils.createArray(isPrimitive ? short.class : Short.class, count); }
        @Override
        public Object unbox(Object o) { return o instanceof Short ? (short)o : o; }
        @Override
        public boolean isGood(Object v, boolean isArrayOrCollection) { return v != null; }
    };

    public static final FieldBinding.ITypeConversion<Integer> INTEGER = new FieldBinding.ITypeConversion<Integer>() {
        @Override
        public Integer fromString(String s) { return StrConversionUtils.tryParseInt(s); }
        @Override
        public List<Integer> list(int count) { return new ArrayList<>(count); }
        @Override
        public Object array(int count, boolean isPrimitive) { return ArrayUtils.createArray(isPrimitive ? int.class : Integer.class, count); }
        @Override
        public Object unbox(Object o) { return o instanceof Integer ? (int)o : o; }
        @Override
        public boolean isGood(Object v, boolean isArrayOrCollection) { return v != null; }
    };

    public static final FieldBinding.ITypeConversion<Boolean> BOOLEAN = new FieldBinding.ITypeConversion<Boolean>() {
        @Override
        public Boolean fromString(String s) { return StrConversionUtils.tryParseBoolean(s); }
        @Override
        public List<Boolean> list(int count) { return new ArrayList<>(count); }
        @Override
        public Object array(int count, boolean isPrimitive) { return ArrayUtils.createArray(isPrimitive ? boolean.class : Boolean.class, count); }
        @Override
        public Object unbox(Object o) { return o instanceof Boolean ? (boolean)o : o; }
        @Override
        public boolean isGood(Object v, boolean isArrayOrCollection) { return v != null; }
    };

    public static final FieldBinding.ITypeConversion<Long> LONG = new FieldBinding.ITypeConversion<Long>() {
        @Override
        public Long fromString(String s) { return StrConversionUtils.tryParseLong(s); }
        @Override
        public List<Long> list(int count) { return new ArrayList<>(count); }
        @Override
        public Object array(int count, boolean isPrimitive) { return ArrayUtils.createArray(isPrimitive ? long.class : Long.class, count); }
        @Override
        public Object unbox(Object o) { return o instanceof Long ? (long)o : o; }
        @Override
        public boolean isGood(Object v, boolean isArrayOrCollection) { return v != null; }
    };

    public static final FieldBinding.ITypeConversion<Double> DOUBLE = new FieldBinding.ITypeConversion<Double>() {
        @Override
        public Double fromString(String s) { return StrConversionUtils.tryParseDouble(s); }
        @Override
        public List<Double> list(int count) { return new ArrayList<>(count); }
        @Override
        public Object array(int count, boolean isPrimitive) { return ArrayUtils.createArray(isPrimitive ? double.class : Double.class, count); }
        @Override
        public Object unbox(Object o) { return o instanceof Double ? (double)o : o; }
        @Override
        public boolean isGood(Object v, boolean isArrayOrCollection) { return v != null; }
    };

    public static final FieldBinding.ITypeConversion<Byte> BYTE = new FieldBinding.ITypeConversion<Byte>() {
        @Override
        public Byte fromString(String s) { return StrConversionUtils.tryParseByte(s); }
        @Override
        public List<Byte> list(int count) { return new ArrayList<>(count); }
        @Override
        public Object array(int count, boolean isPrimitive) { return ArrayUtils.createArray(isPrimitive ? byte.class : Byte.class, count); }
        @Override
        public Object unbox(Object o) { return o instanceof Byte ? (byte)o : o; }
        @Override
        public boolean isGood(Object v, boolean isArrayOrCollection) { return v != null; }
    };

    public static final FieldBinding.ITypeConversion<Character> CHAR = new FieldBinding.ITypeConversion<Character>() {
        @Override
        public Character fromString(String s) { return StrConversionUtils.tryParseChar(s); }
        @Override
        public List<Character> list(int count) { return new ArrayList<>(count); }
        @Override
        public Object array(int count, boolean isPrimitive) { return ArrayUtils.createArray(isPrimitive ? char.class : Character.class, count); }
        @Override
        public Object unbox(Object o) { return o instanceof Character ? (char)o : o; }
        @Override
        public boolean isGood(Object v, boolean isArrayOrCollection) { return v != null; }
    };

    public static final FieldBinding.ITypeConversion<Float> FLOAT = new FieldBinding.ITypeConversion<Float>() {
        @Override
        public Float fromString(String s) { return StrConversionUtils.tryParseFloat(s); }
        @Override
        public List<Float> list(int count) { return new ArrayList<>(count); }
        @Override
        public Object array(int count, boolean isPrimitive) { return ArrayUtils.createArray(isPrimitive ? float.class : Float.class, count); }
        @Override
        public Object unbox(Object o) { return o instanceof Float ? (float)o : o; }
        @Override
        public boolean isGood(Object v, boolean isArrayOrCollection) { return v != null; }
    };


    public static final FieldBinding.ITypeConversion<String> STRING = new FieldBinding.ITypeConversion<String>() {
        @Override
        public String fromString(String s) { return s; }
        @Override
        public List<String> list(int count) { return new ArrayList<>(count); }
        @Override
        public Object array(int count, boolean isPrimitive) { return ArrayUtils.createArray(String.class, count); }
        @Override
        public Object unbox(Object o) { return o; }
        @Override
        public boolean isGood(Object v, boolean isArrayOrCollection) { return v != null; }
    };

    public static final FieldBinding.ITypeConversion<CharSequence> CHAR_SEQUENCE = new FieldBinding.ITypeConversion<CharSequence>() {
        @Override
        public CharSequence fromString(String s) { return (CharSequence)s; }
        @Override
        public List<CharSequence> list(int count) { return new ArrayList<>(count); }
        @Override
        public Object array(int count, boolean isPrimitive) { return ArrayUtils.createArray(CharSequence.class, count); }
        @Override
        public Object unbox(Object o) { return o; }
        @Override
        public boolean isGood(Object v, boolean isArrayOrCollection) { return v != null; }
    };
}
