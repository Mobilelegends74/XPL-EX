package eu.faircode.xlua.x.runtime.reflect;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.StrConversionUtils;
import eu.faircode.xlua.x.data.interfaces.IValidator;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.xlua.LibUtil;

public class FieldBinding implements IValidator {
    private static final String TAG = LibUtil.generateTag(FieldBinding.class);

    public interface ITypeConversion<T> {
        T fromString(String s);
        List<T> list(int count);
        Object array(int count, boolean isPrimitive);
        Object unbox(Object o);
        boolean isGood(Object v, boolean isArrayOrCollection);
    }

    public interface ISetValue {  boolean setValue(Object instance, String value); }

    protected String settingName;
    protected DynField field;

    private boolean defaultNullArray = true;
    private boolean defaultNullCollection = true;
    private Object defaultIfNull = null;
    private String delimiter = Str.COMMA;
    private ITypeConversion<?> converter;

    protected ISetValue setter = new ISetValue() {
        @Override
        public boolean setValue(Object instance, String value) {
            if(isValid()) {
                if(String.class.equals(field.getField().getType())) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Field [%s] is Type of String, Setting Name [%s] Value=%s",
                                field.getName(),
                                settingName,
                                Str.ensureNoDoubleNewLines(Str.toStringOrNull(value))));
                    return field.setInstance(instance, value);
                } else {
                    if(converter == null) {
                        if(DebugUtil.isDebug())
                            Log.d(TAG, Str.fm("Field [%s] lacks a Converter, Setting Name [%s] Value=%s",
                                    field.getName(),
                                    settingName,
                                    Str.ensureNoDoubleNewLines(Str.toStringOrNull(value))));
                    } else {
                        if(DebugUtil.isDebug())
                            Log.d(TAG, Str.fm("Field [%s] is being Updated from Converter [%s][%s] , Setting Name [%s] Value=%s",
                                    field.getName(),
                                    Str.toStringOrNull(converter),
                                    Str.toObjectClassName(field.getField().getType()),
                                    settingName,
                                    Str.ensureNoDoubleNewLines(Str.toStringOrNull(value))));
                        return updateDynamic(instance, value, converter);
                    }
                }
            }

            return false;
        }
    };

    public FieldBinding setSetter(ISetValue setter) { this.setter = setter; return this; }
    public FieldBinding setDefaultFlags(boolean defaultNullArray, boolean defaultNullCollection) { this.defaultNullArray = defaultNullArray; this.defaultNullCollection = defaultNullCollection; return this; }
    public FieldBinding setDefaultIfNull(Object defaultIfNullValue) { this.defaultIfNull = defaultIfNullValue; return this; }
    public FieldBinding setDelimiter(String del) { this.delimiter = del; return this; }


    public static FieldBinding create(String settingName, DynField field) { return new FieldBinding(settingName, field); }

    @Override
    public boolean isValid() { return field != null && field.isValid(); }

    public FieldBinding() { }
    public FieldBinding(String settingName, DynField field) { from(settingName, field); }

    protected void from(String settingName, DynField field) {
        if(field != null) {
            this.settingName = settingName;
            this.field = field;
            if(this.field.isValid()) {
                init(this.field.getField().getType());
            }
        }
    }

    public FieldBinding init(Class<?> c) {
        if(c != null) {
            Class<?> clazz = c.isArray() ? c.getComponentType() != null ? c.getComponentType() : c : c;
            if(Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)) {
                init(TypeBind.INTEGER);
            } else if(Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz)) {
                init(TypeBind.LONG);
            } else if(Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz)) {
                init(TypeBind.DOUBLE);
            } else if(Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz)) {
                init(TypeBind.FLOAT);
            } else if(Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz)) {
                init(TypeBind.BOOLEAN);
            } else if(Byte.class.isAssignableFrom(clazz) || byte.class.isAssignableFrom(clazz)) {
                init(TypeBind.BYTE);
            } else if(Character.class.isAssignableFrom(clazz) || char.class.isAssignableFrom(clazz)) {
                init(TypeBind.CHAR);
            } else if(Short.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz)) {
                init(TypeBind.SHORT);
            } else if(String.class.equals(clazz))
                init(TypeBind.STRING);
            else if(CharSequence.class.equals(clazz)) {
                init(TypeBind.CHAR_SEQUENCE);
            }
        }

        return this;
    }

    public FieldBinding init(TypeBind type) {
        switch (type) {
            case BYTE: this.converter = Conversions.BYTE;  break;
            case CHAR: this.converter = Conversions.CHAR; break;
            case BOOLEAN: this.converter = Conversions.BOOLEAN; break;
            case SHORT: this.converter = Conversions.SHORT; break;
            case INTEGER: this.converter = Conversions.INTEGER; break;
            case LONG: this.converter = Conversions.LONG; break;
            case DOUBLE: this.converter = Conversions.DOUBLE; break;
            case FLOAT: this.converter = Conversions.FLOAT; break;
            case STRING: this.converter = Conversions.STRING; break;
            case CHAR_SEQUENCE: this.converter = Conversions.CHAR_SEQUENCE; break;
            default:
                break;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Init Type Conversion for Type Binder [%s] Field Name [%s] Setting Name [%s] is Valid [%s]",
                    type.name(),
                    Str.toStringOrNull(field),
                    settingName,
                    isValid()));

        return this;
    }

    private <T> boolean updateDynamic(
            Object instance,
            String value,
            ITypeConversion<T> converter) {
        if(isValid() && converter != null) {
            boolean isPrimitive = this.field.isPrimitive();
            boolean isArray = this.field.isArray();
            boolean isCollection = this.field.isCollection();

            if(Str.isEmpty(value)) {
                if(isArray || isCollection) {
                    this.field.setInstance(instance,
                            isArray ?
                                    defaultNullArray ? null : converter.array(0, isPrimitive) :
                                    defaultNullCollection ? null : ListUtil.emptyList());
                } else {
                    if(!isPrimitive || defaultIfNull != null) {
                        return this.field.setInstance(instance, defaultIfNull);
                    }
                }
            } else {
                if(isArray || isCollection) {
                    List<String> splits = Str.splitToList(value, delimiter);
                    List<T> items = new ArrayList<>();
                    for(String s : splits) {
                        if(Str.isEmpty(s))
                            continue;

                        T val = converter.fromString(s);
                        if(!converter.isGood(val, true))
                            continue;

                        items.add(val);
                    }

                    if(isArray) {
                        Object array = converter.array(items.size(), isPrimitive);
                        for(int i = 0; i < items.size(); i++) {
                            T item = items.get(i);
                            try {
                                Array.set(array, i, isPrimitive ?
                                        converter.unbox(item) :
                                        item);
                            }catch (Exception ignored) { }
                        }

                        return this.field.setInstance(instance, array);
                    } else {
                        return this.field.setInstance(instance, items);
                    }
                } else {
                    T val = converter.fromString(value);
                    if(converter.isGood(val, false)) {
                        return this.field.setInstance(instance, isPrimitive ?
                                converter.unbox(val) :
                                val);
                    } else {
                        if(!isPrimitive || defaultIfNull != null) {
                            return this.field.setInstance(instance, defaultIfNull);
                        }
                    }
                }
            }
        }

        return false;
    }
}
