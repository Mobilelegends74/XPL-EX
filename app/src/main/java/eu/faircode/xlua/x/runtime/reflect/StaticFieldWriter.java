package eu.faircode.xlua.x.runtime.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Writes static final Android framework fields when ordinary reflection is ignored. */
public final class StaticFieldWriter {
    private StaticFieldWriter() { }

    public static void set(Field field, Object value) throws Throwable {
        Throwable failure = null;
        try {
            field.setAccessible(true);
            field.set(null, value);
            if (sameValue(field.get(null), value))
                return;
        } catch (Throwable error) {
            failure = error;
        }

        try {
            setWithUnsafe(field, value);
            if (sameValue(field.get(null), value))
                return;
            failure = append(failure, new IllegalStateException(
                    "Unsafe write was ignored for " + field));
        } catch (Throwable error) {
            failure = append(failure, error);
        }

        throw failure == null
                ? new IllegalStateException("Failed writing " + field)
                : failure;
    }

    private static void setWithUnsafe(Field field, Object value) throws Throwable {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field singleton = unsafeClass.getDeclaredField("theUnsafe");
        singleton.setAccessible(true);
        Object unsafe = singleton.get(null);

        Method staticFieldBase = unsafeClass.getMethod("staticFieldBase", Field.class);
        Method staticFieldOffset = unsafeClass.getMethod("staticFieldOffset", Field.class);
        Object base = staticFieldBase.invoke(unsafe, field);
        long offset = ((Number) staticFieldOffset.invoke(unsafe, field)).longValue();

        Class<?> type = field.getType();
        if (!type.isPrimitive()) {
            invokePut(unsafeClass, unsafe, "putObjectVolatile", Object.class, base, offset, value);
        } else if (type == int.class) {
            invokePut(unsafeClass, unsafe, "putIntVolatile", int.class, base, offset, ((Number) value).intValue());
        } else if (type == long.class) {
            invokePut(unsafeClass, unsafe, "putLongVolatile", long.class, base, offset, ((Number) value).longValue());
        } else if (type == boolean.class) {
            invokePut(unsafeClass, unsafe, "putBooleanVolatile", boolean.class, base, offset, value);
        } else if (type == byte.class) {
            invokePut(unsafeClass, unsafe, "putByteVolatile", byte.class, base, offset, ((Number) value).byteValue());
        } else if (type == short.class) {
            invokePut(unsafeClass, unsafe, "putShortVolatile", short.class, base, offset, ((Number) value).shortValue());
        } else if (type == char.class) {
            invokePut(unsafeClass, unsafe, "putCharVolatile", char.class, base, offset, value);
        } else if (type == float.class) {
            invokePut(unsafeClass, unsafe, "putFloatVolatile", float.class, base, offset, ((Number) value).floatValue());
        } else if (type == double.class) {
            invokePut(unsafeClass, unsafe, "putDoubleVolatile", double.class, base, offset, ((Number) value).doubleValue());
        } else {
            throw new IllegalArgumentException("Unsupported field type " + type);
        }
    }

    private static void invokePut(Class<?> unsafeClass, Object unsafe, String methodName,
                                  Class<?> valueType, Object base, long offset, Object value) throws Throwable {
        Method method = unsafeClass.getMethod(methodName, Object.class, long.class, valueType);
        method.invoke(unsafe, base, offset, value);
    }

    private static Throwable append(Throwable previous, Throwable next) {
        if (previous == null)
            return next;
        previous.addSuppressed(next);
        return previous;
    }

    private static boolean sameValue(Object current, Object expected) {
        return current == expected || (current != null && current.equals(expected));
    }
}
