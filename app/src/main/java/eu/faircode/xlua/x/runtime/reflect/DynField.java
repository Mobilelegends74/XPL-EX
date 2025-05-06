package eu.faircode.xlua.x.runtime.reflect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ObjectUtils;

@SuppressWarnings({"unchecked", "unused"})
public class DynField implements IReflect {
    private DynClass clazz;
    private Field field;
    private String name;

    public Field getField() { return field; }

    private Exception lastException = null;
    private Object instance = null;
    private boolean returnDefaultIfNull = false;

    public static DynField create(Class<?> clazz, String fieldName) { return new DynField(clazz, fieldName); }
    public static DynField create(String className, String fieldName) { return new DynField(className, fieldName); }
    public static DynField create(DynClass clazz, String fieldName) { return new DynField(clazz, fieldName); }
    public static DynField create(Field field) { return new DynField(field); }

    public DynField clearLastException() { this.lastException = null; return this; }
    public DynField bindInstance(Object instance) { this.instance = instance; return this; }
    public DynField setReturnDefaultIfNull(boolean returnDefaultIfNull) { this.returnDefaultIfNull = returnDefaultIfNull; return this; }

    @Override
    public Exception getLastException() { return lastException; }
    @Override
    public boolean wasSuccessful() { return lastException == null; }
    @Override
    public String getName() { return name; }
    @Override
    public DynClass getClazz() { return clazz; }
    @Override
    public void setAccessible(boolean accessible) { DynUtils.setAccessible(field, accessible); }
    @Override
    public boolean isValid() { return clazz != null && clazz.isValid() && field != null; }

    public boolean isArray() { return isValid() && this.field.getType().isArray(); }

    public boolean isCollection() { return isValid() && Collection.class.isAssignableFrom(this.field.getType()); }

    public boolean isPrimitive() {
        if(isValid()) {
            Class<?> ty = this.field.getType();
            if(ty.isArray()) {
                Class<?> componentType = ty.getComponentType();
                return  componentType != null && componentType.isPrimitive();
            } else {
                return ty.isPrimitive();
            }
        }

        return false;
    }

    public DynField(Class<?> clazz, String fieldName) { this(DynClass.create(clazz), fieldName); }
    public DynField(String className, String fieldName) { this(DynClass.create(DynClass.forName(className)), fieldName); }
    public DynField(DynClass clazz, String fieldName) {
        if(clazz != null && !Str.isEmpty(fieldName)) {
            this.clazz = clazz;
            this.field = this.clazz.getField(fieldName);
            this.name = Str.toStringOrNull(this.field);
        }
    }

    public DynField(Field field) {
        if(field != null) {
            this.clazz = DynClass.create(field.getClass());
            this.field = field;
            DynUtils.setAccessible(this.field, true);
            this.name = Str.toStringOrNull(this.field);
        }
    }

    public <T> T getTStatic() { return getTStaticOrDefault(null); }
    public <T> T getTStaticOrDefault(T defaultValue) {
        lastException = null;
        try {
            Object res = this.field.get(null);
            return res == null ? returnDefaultIfNull ? defaultValue : null : (T)res;
        }catch (Exception e) {
            lastException = e;
            return defaultValue;
        }
    }

    public <T> T getTInstanceBind() { return getTInstanceOrDefault(this.instance, null); }
    public <T> T getTInstance(Object instance) { return getTInstanceOrDefault(instance, null); }
    public <T> T getTInstanceOrDefaultBind(T defaultValue) { return getTInstanceOrDefault(this.instance, defaultValue); }
    public <T> T getTInstanceOrDefault(Object instance, T defaultValue) {
        lastException = null;
        try {
            Object res = this.field.get(ObjectUtils.firstNonNullValue(instance, this.instance));
            return res == null ? returnDefaultIfNull ? defaultValue : null : (T)res;
        }catch (Exception e) {
            lastException = e;
            return defaultValue;
        }
    }

    public Object getStatic() { return getStaticOrDefault(null); }
    public Object getStaticOrDefault(Object defaultValue) {
        lastException = null;
        try {
            Object res = this.field.get(null);
            return res == null ? returnDefaultIfNull ? defaultValue : null : res;
        } catch (Exception e) {
            lastException = e;
            return defaultValue;
        }
    }

    public Object getInstanceBind() { return getInstanceOrDefault(this.instance, null); }
    public Object getInstance(Object instance) { return getInstanceOrDefault(instance, null); }
    public Object getInstanceOrDefaultBind(Object defaultValue) { return getInstanceOrDefault(this.instance, defaultValue); }
    public Object getInstanceOrDefault(Object instance, Object defaultValue) {
        lastException = null;
        try {
            Object res = this.field.get(ObjectUtils.firstNonNullValue(instance, this.instance));
            return res == null ? returnDefaultIfNull ? defaultValue : null : res;
        }catch (Exception e) {
            lastException = e;
            return defaultValue;
        }
    }

    public boolean setStatic(Object value) {
        lastException = null;
        try {
            this.field.set(null, value);
            return true;
        }catch (Exception e) {
            lastException = e;
            return false;
        }
    }

    public boolean setInstanceBind(Object value) { return setInstance(this.instance, value); }
    public boolean setInstance(Object instance, Object value) {
        lastException = null;
        try {
            this.field.set(ObjectUtils.firstNonNullValue(instance, this.instance), value);
            return true;
        }catch (Exception e) {
            lastException = e;
            return false;
        }
    }

    @Override
    public int hashCode() { return Str.hashCode(Str.combine(Str.toStringOrNull(this.clazz), this.name)); }
    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof String || obj instanceof DynField) return obj.hashCode() == this.hashCode();
        if(obj instanceof Field) return this.equals(DynField.create((Field) obj));
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
                .appendField("Field IsValid", this.isValid())
                .appendNewLineOrSpace(useNewLine)
                .append("Was last Action Successful=")
                .append(this.wasSuccessful())
                .appendNewLineOrSpace(useNewLine)
                .append("Last Exception=")
                .append(Str.ensureNoDoubleNewLines(Str.toStringOrNull(this.lastException)))
                .append(Str.toObjectClassSimpleName(clazz))
                .append("::")
                .append(this.name)
                .toString(true);
    }
}
