package eu.faircode.xlua.x.runtime.reflect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.xlua.LibUtil;

@SuppressWarnings({"unchecked", "unused"})
public class DynConstructor implements IReflect {
    private static final String TAG = LibUtil.generateTag(DynConstructor.class);
    private DynClass clazz;
    private String name;

    private Constructor<?> constructor;
    private Exception lastException = null;

    public Constructor<?> getConstructor() { return constructor; }
    public DynConstructor clearLastException() { this.lastException = null; return this; }

    public static DynConstructor create(String className, Class<?>... paramTypes) { return new DynConstructor(className, paramTypes); }
    public static DynConstructor create(Class<?> clazz, Class<?>... paramTypes) { return new DynConstructor(clazz, paramTypes); }
    public static DynConstructor create(DynClass clazz, Class<?>... paramTypes) { return new DynConstructor(clazz, paramTypes); }
    public static DynConstructor create(Constructor<?> constructor) { return new DynConstructor(constructor); }

    @Override
    public String getName() { return name; }
    @Override
    public DynClass getClazz() { return clazz; }
    @Override
    public void setAccessible(boolean accessible) { DynUtils.setAccessible(clazz, accessible); }
    @Override
    public Exception getLastException() { return lastException; }
    @Override
    public boolean wasSuccessful() { return lastException == null; }
    @Override
    public boolean isValid() { return clazz != null && clazz.isValid() && constructor != null && !Str.isEmpty(name); }

    public DynConstructor(String className, Class<?>... paramTypes) { this(DynClass.forName(className), paramTypes); }
    public DynConstructor(Class<?> clazz, Class<?>... paramTypes) { this(DynClass.create(clazz), paramTypes); }
    public DynConstructor(DynClass clazz, Class<?>... paramTypes) {
        if(clazz != null) {
            this.clazz = clazz;
            this.constructor = this.clazz.getConstructor(paramTypes);
            this.name = Str.combine(Str.toObjectClassSimpleName(clazz), Str.enclose(Str.toStringOrNull(DynUtils.getParameterTypes(clazz))));
        }
    }

    public DynConstructor(Constructor<?> constructor) {
        if(constructor != null) {
            this.clazz = DynClass.create(constructor.getClass());
            this.constructor = constructor;
            DynUtils.setAccessible(this.constructor, true);
            this.name = Str.combine(Str.toObjectClassSimpleName(clazz), Str.enclose(Str.toStringOrNull(DynUtils.getParameterTypes(clazz))));
        }
    }

    public <T> T newInstanceT() {
        lastException = null;
        try {
            return (T)this.clazz.getRawClass().newInstance();
        }catch (Exception e) {
            lastException = e;
            return null;
        }
    }

    public <T> T newInstanceT(Object... args) {
        lastException = null;
        try {
            return (T)constructor.newInstance(args);
        }catch (Exception e) {
            lastException = e;
            return null;
        }
    }

    public Object newInstance() {
        lastException = null;
        try {
            return this.clazz.getRawClass().newInstance();
        } catch (Exception e) {
            lastException = e;
            return null;
        }
    }

    public Object newInstance(Object... args) {
        lastException = null;
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            lastException = e;
            return null;
        }
    }

    @Override
    public int hashCode() { return Str.hashCode(Str.combine(Str.toStringOrNull(this.clazz), this.name)); }
    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof String || obj instanceof DynConstructor) return obj.hashCode() == this.hashCode();
        if(obj instanceof Constructor) return this.equals(DynConstructor.create((Constructor<?>) obj));
        return false;
    }

    @NonNull
    @Override
    public String toString() { return toString(false); }
    public String toString(boolean useNewLine) {
        return StrBuilder.create()
                .append(Str.toStringOrNull(clazz))
                .appendNewLineOrSpace(useNewLine)
                .appendField("Constructor IsValid", this.isValid())
                .appendNewLineOrSpace(useNewLine)
                .append(Str.toObjectClassSimpleName(clazz))
                .append("(")
                .appendTypes(DynUtils.getParameterTypes(constructor))
                .append(")")
                .toString(true);
    }
}
