package eu.faircode.xlua.x.hook.interceptors.cell;

import android.telephony.SubscriptionManager;

import eu.faircode.xlua.x.runtime.reflect.ReflectUtil;

public class SubscriptionInfoUtils {
    private static final String SIM_INFO_CLASS = "android.provider.Telephony$SimInfo";

    public static final String NAME_SOURCE_UNKNOWN_NAME = "UNKNOWN";
    public static final int NAME_SOURCE_UNKNOWN =
            ReflectUtil.useFieldValueOrDefaultInt(SubscriptionManager.class, "NAME_SOURCE_UNKNOWN",
                    ReflectUtil.useFieldValueOrDefaultInt(SIM_INFO_CLASS, "NAME_SOURCE_UNKNOWN",
                            -1));

    public static final String NAME_SOURCE_CARRIER_ID_NAME = "CARRIER_ID";
    public static final int NAME_SOURCE_CARRIER_ID =
            ReflectUtil.useFieldValueOrDefaultInt(SubscriptionManager.class, "NAME_SOURCE_CARRIER_ID",
                    ReflectUtil.useFieldValueOrDefaultInt(SIM_INFO_CLASS, "NAME_SOURCE_CARRIER_ID",
                            0));

    public static final String NAME_SOURCE_SIM_SPN_NAME = "SIM_SPN";
    public static final int NAME_SOURCE_SIM_SPN =
            ReflectUtil.useFieldValueOrDefaultInt(SubscriptionManager.class, "NAME_SOURCE_SIM_SPN",
                    ReflectUtil.useFieldValueOrDefaultInt(SIM_INFO_CLASS, "NAME_SOURCE_SIM_SPN",
                            1));

    public static final String NAME_SOURCE_USER_INPUT_NAME = "USER_INPUT";
    public static final int NAME_SOURCE_USER_INPUT =
            ReflectUtil.useFieldValueOrDefaultInt(SubscriptionManager.class, "NAME_SOURCE_USER_INPUT",
                    ReflectUtil.useFieldValueOrDefaultInt(SIM_INFO_CLASS, "NAME_SOURCE_USER_INPUT",
                            2));

    public static final String NAME_SOURCE_CARRIER_NAME = "CARRIER";
    public static final int NAME_SOURCE_CARRIER =
            ReflectUtil.useFieldValueOrDefaultInt(SubscriptionManager.class, "NAME_SOURCE_CARRIER",
                    ReflectUtil.useFieldValueOrDefaultInt(SIM_INFO_CLASS, "NAME_SOURCE_CARRIER",
                            3));

    public static final String NAME_SOURCE_SIM_PNN_NAME = "SIM_PNN";
    public static final int NAME_SOURCE_SIM_PNN =
            ReflectUtil.useFieldValueOrDefaultInt(SubscriptionManager.class, ".NAME_SOURCE_SIM_PNN",
                    ReflectUtil.useFieldValueOrDefaultInt(SIM_INFO_CLASS, ".NAME_SOURCE_SIM_PNN",
                            4));

    /*public static String displayNameSourceToString(int source) {
        switch (source) {
            case NAME_SOURCE_UNKNOWN: return "UNKNOWN";
            case NAME_SOURCE_CARRIER_ID: return "CARRIER_ID";
            case NAME_SOURCE_SIM_SPN: return "SIM_SPN";
            case NAME_SOURCE_USER_INPUT: return "USER_INPUT";
            case NAME_SOURCE_CARRIER: return "CARRIER";
            case NAME_SOURCE_SIM_PNN: return "SIM_PNN";
            default:
                return "UNKNOWN(" + source + ")";
        }
    }*/
}
