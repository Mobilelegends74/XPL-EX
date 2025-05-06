package eu.faircode.xlua.x.runtime.reflect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ObjectUtils;

@SuppressWarnings({"unchecked", "unused"})
public class DynMethod implements IReflect {
    private DynClass clazz;
    private String name;
    private Method method;

    public Method getMethod() { return method; }

    private Object instance = null;
    private Exception lastException = null;
    private boolean returnDefaultIfNull = false;

    public static DynMethod create(Class<?> clazz, String methodName, Class<?>... paramTypes) { return new DynMethod(clazz, methodName, paramTypes); }
    public static DynMethod create(String className, String methodName, Class<?>... paramTypes) { return new DynMethod(className, methodName, paramTypes); }
    public static DynMethod create(DynClass clazz, String methodName, Class<?>... paramTypes) { return new DynMethod(clazz, methodName, paramTypes); }
    public static DynMethod create(Method method) { return new DynMethod(method); }

    public DynMethod clearLastException() { this.lastException = null; return this; }
    public DynMethod bindInstance(Object instance) { this.instance = instance; return this; }
    public DynMethod setReturnDefaultIfNull(boolean returnDefaultIfNull) { this.returnDefaultIfNull = returnDefaultIfNull; return this; }

    @Override
    public Exception getLastException() { return lastException; }
    @Override
    public boolean wasSuccessful() { return lastException == null; }
    @Override
    public String getName() { return name; }
    @Override
    public DynClass getClazz() { return clazz; }
    @Override
    public void setAccessible(boolean accessible) { DynUtils.setAccessible(method, accessible); }
    @Override
    public boolean isValid() { return clazz != null && clazz.isValid() && method != null && !Str.isEmpty(name); }

    public DynMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) { this(DynClass.create(clazz), methodName, paramTypes); }
    public DynMethod(String className, String methodName, Class<?>... paramTypes) { this(DynClass.create(DynClass.forName(className)), methodName, paramTypes); }
    public DynMethod(DynClass clazz, String methodName, Class<?>... paramTypes) {
        if(clazz != null) {
            this.clazz = clazz;
            this.method = this.clazz.getMethod(methodName, paramTypes);
            this.name = Str.toStringOrNull(this.method);
        }
    }

    public DynMethod(Method method) {
        if(method != null) {
            this.clazz = DynClass.create(method.getClass());
            this.method = method;
            DynUtils.setAccessible(this.method, true);
            this.name = Str.toStringOrNull(this.method);
        }
    }

    public <T> T invokeTStatic(Object... args) { return invokeTStaticOrDefault(null, args); }
    public <T> T invokeTStaticOrDefault(T defaultValue, Object... args) {
        lastException = null;
        try {
            Object res = this.method.invoke(null, args);
            return res == null ? returnDefaultIfNull ? defaultValue : null : (T)res;
        }catch (Exception e) {
            lastException = e;
            return defaultValue;
        }
    }

    public <T> T invokeTBind(Object... args) { return invokeTOrDefault(this.instance, null, args); }
    public <T> T invokeTOrDefaultBind(T defaultValue, Object... args) { return invokeTOrDefault(this.instance, defaultValue, args); }

    public <T> T invokeT(Object instance, Object... args) { return invokeTOrDefault(instance, null, args); }
    public <T> T invokeTOrDefault(Object instance, T defaultValue, Object... args) {
        lastException = null;
        try {
            Object res = this.method.invoke(ObjectUtils.firstNonNullValue(instance, this.instance), args);
            return res == null ? returnDefaultIfNull ? defaultValue : null : (T)res;
        }catch (Exception e) {
            lastException = e;
            return defaultValue;
        }
    }

    public Object invokeStatic(Object... args) { return invokeStaticOrDefault(null, args); }
    public Object invokeStaticOrDefault(Object defaultValue, Object... args) {
        lastException = null;
        try {
            Object res = this.method.invoke(null, args);
            return res == null ? returnDefaultIfNull ? defaultValue : null : res;
        }catch (Exception e) {
            lastException = e;
            return defaultValue;
        }
    }

    public Object invokeBind(Object... args) { return invokeOrDefault(this.instance, null, args); }
    public Object invokeOrDefaultBind(Object defaultValue, Object... args) { return invokeOrDefault(this.instance, defaultValue, args); }

    public Object invoke(Object instance, Object... args) { return invokeOrDefault(instance, null, args); }
    public Object invokeOrDefault(Object instance, Object defaultValue, Object... args) {
        lastException = null;
        try {
            Object res = this.method.invoke(ObjectUtils.firstNonNullValue(instance, this.instance), args);
            return res == null ? returnDefaultIfNull ? defaultValue : null : res;
        }catch (Exception e) {
            lastException = e;
            return defaultValue;
        }
    }

    @Override
    public int hashCode() { return Str.hashCode(Str.combine(Str.toStringOrNull(this.clazz), this.name)); }
    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof String || obj instanceof DynMethod) return obj.hashCode() == this.hashCode();
        if(obj instanceof Method) return this.equals(DynMethod.create((Method) obj));
        return false;
    }

    @NonNull
    @Override
    public String toString() { return toString(false); }
    public String toString(boolean useNewLine) {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .append(Str.toStringOrNull(clazz))
                .appendNewLineOrSpace(useNewLine)
                .appendField("Method IsValid", this.isValid())
                .appendNewLineOrSpace(useNewLine)
                .append("Was last Invoke Successful=")
                .append(this.wasSuccessful())
                .appendNewLineOrSpace(useNewLine)
                .append("Last Exception=")
                .append(Str.ensureNoDoubleNewLines(Str.toStringOrNull(this.lastException)))
                .append(Str.toObjectClassSimpleName(clazz))
                .append("::")
                .append(this.name)
                .append("(")
                .appendTypes(DynUtils.getParameterTypes(method))
                .append(")")
                .toString(true);
    }
}
