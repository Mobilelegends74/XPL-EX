package eu.faircode.xlua.x.hook.interceptors.cell;

import android.telephony.SubscriptionInfo;
import android.util.Log;

import java.lang.reflect.Field;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.xlua.LibUtil;

public class SubFieldPair {
    private static final String TAG = LibUtil.generateTag(SubFieldPair.class);

    public static final String BUILDER_CLASS_NAME = "android.telephony.SubscriptionInfo$Builder";

    public static SubFieldPair create(String fieldName, String settingName) { return new SubFieldPair(fieldName, settingName); }
    public static SubFieldPair create(String fieldName) { return new SubFieldPair(fieldName, null); }

    /*public static SubFieldPair createLike(String settingName, String key) {
        try {
            Field[] fields = SubscriptionInfo.class.getDeclaredFields();
            for(Field )

        }catch (Exception e) {

        }
    }

    public static SubFieldPair create(String settingName, String... possibleNames) {
        try {
            Field[] fields = SubscriptionInfo.class.getDeclaredFields();


        }catch (Exception e) {

        }
    }*/

    private String fieldName;
    private final DynamicField field;
    private final String settingName;

    private Object defaultValue = null;

    public SubFieldPair setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }


    public String getIndexedName(int index) { return index > -1 ? Str.combine(settingName, "." + String.valueOf(index)) : settingName; }

    public SubFieldPair(String fieldName, String settingName) {
        this.fieldName = fieldName;
        this.field = DynamicField.create(SubscriptionInfo.class, fieldName).setAccessible(true);
        this.settingName = settingName;
    }

    /*public void updateValue(Object o, String settingName, int index) {
        PhoneHookUtils.getSettingValue()
    }*/

    public void updateValue(Object o, Object newValue) {
        try {
            newValue = newValue != null ? newValue : defaultValue;
            if(o == null) {
                Log.e(TAG, Str.fm("Field %s was not Updated, Reason=NULL_OBJECT", fieldName));
                return;
            }

            if(!field.isValid()) {
                Log.e(TAG, Str.fm("Field %s is Not Valid!", fieldName));
                return;
            }

            Object original = field.tryGetValueInstanceEx(o, null);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Updating Field [%s], New Value=(%s)  Old Value=(%s)",
                        fieldName,
                        Str.toStringOrNull(newValue),
                        Str.toStringOrNull(original)));

            if(newValue == null) {
                //skip ??
                return;
            }

            boolean res = field.trySetValueInstanceEx(o, newValue);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Updated Result [%s] Field [%s] New Value=(%s) Old Value=(%s)",
                        String.valueOf(res),
                        fieldName,
                        Str.toStringOrNull(newValue),
                        Str.toStringOrNull(original)));

        }catch (Exception e) {
            Log.e(TAG, "Failed to Update Value for Field: " + fieldName + " Error=" + e);
        }
    }
}
