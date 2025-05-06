package eu.faircode.xlua.x.hook.interceptors.cell.stubs;

import android.telephony.SubscriptionInfo;

import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.runtime.reflect.DynClass;
import eu.faircode.xlua.x.runtime.reflect.DynField;
import eu.faircode.xlua.x.runtime.reflect.FieldBinding;

public class SubInfoField extends FieldBinding {
    //mSimSlotIndex

    private static final DynClass SUBSCRIPTION_INFO_CLASS = DynClass.create(SubscriptionInfo.class);

    public static SubInfoField create(String fieldName, String settingName) { return new SubInfoField(fieldName, settingName); }

    public String getSettingName() { return settingName; }
    public String getFieldName() { return field != null ? field.getName() : null; }

    public SubInfoField(String fieldName, String settingName) { from(settingName, DynField.create(SUBSCRIPTION_INFO_CLASS.getField(fieldName))); }

    public Object getValue(Object instance) { return isValid() ? this.field.getInstance(instance) : null; }

    public boolean setFromSetting(Object instance, int index, XParam param) {
        if(isValid() && instance != null && param != null) {
            return this.setter.setValue(
                    instance,
                    param.getSetting(Str.combineEx(this.settingName, ".", String.valueOf(index))));
        } else {
            return false;
        }
    }
}
