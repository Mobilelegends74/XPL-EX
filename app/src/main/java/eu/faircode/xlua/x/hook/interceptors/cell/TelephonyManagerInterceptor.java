package eu.faircode.xlua.x.hook.interceptors.cell;

import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.LibUtil;

public final class TelephonyManagerInterceptor {
    private static final String TAG = LibUtil.generateTag(TelephonyManagerInterceptor.class);

    private TelephonyManagerInterceptor() { }

    public static boolean intercept(XParam param, boolean operatorName) {
        try {
            Object oldResult = param.getResult();
            int slot = resolveSlot(param.getThis());
            Map<String, String> settings = readOperatorSettings(param);
            String replacement = TelephonyOperatorValues.resolve(settings, operatorName, slot);
            if(replacement == null)
                return false;

            param.setLogOld(Str.toStringOrNull(oldResult));
            param.setLogNew(replacement);
            param.setResult(replacement);
            return true;
        } catch (Throwable e) {
            Log.e(TAG, Str.fm("Failed to intercept TelephonyManager operator value: %s", e));
            return false;
        }
    }

    private static Map<String, String> readOperatorSettings(XParam param) {
        Map<String, String> settings = new HashMap<>();
        String[] names = {
                TelephonyOperatorValues.OPERATOR_NAME,
                TelephonyOperatorValues.OPERATOR_NUMERIC,
                TelephonyOperatorValues.OPERATOR_MCC,
                TelephonyOperatorValues.OPERATOR_MNC,
                "gsm.network.carrier",
                "phone.isp",
                "gsm.operator.id",
                "gsm.operator.mcc",
                "gsm.operator.mnc",
                "phone.mmc",
                "phone.mnc"
        };
        for(String name : names) {
            settings.put(name, param.getSetting(name));
            settings.put(name + ".1", param.getSetting(name + ".1"));
            settings.put(name + ".2", param.getSetting(name + ".2"));
        }
        return settings;
    }

    private static int resolveSlot(Object receiver) {
        if(!(receiver instanceof TelephonyManager))
            return -1;

        Integer directSlot = readIntField(receiver, "mPhoneId", "mSlotIndex");
        if(isSlot(directSlot))
            return directSlot;

        Integer subscriptionId = readIntField(receiver, "mSubId");
        if(subscriptionId == null || subscriptionId < 0)
            return -1;

        Integer slot = invokeStaticInt(
                SubscriptionManager.class,
                "getSlotIndex",
                subscriptionId);
        if(!isSlot(slot))
            slot = invokeStaticInt(
                    SubscriptionManager.class,
                    "getSlotId",
                    subscriptionId);
        return isSlot(slot) ? slot : -1;
    }

    private static Integer readIntField(Object receiver, String... names) {
        Class<?> type = receiver.getClass();
        for(String name : names) {
            Class<?> current = type;
            while(current != null) {
                try {
                    Field field = current.getDeclaredField(name);
                    field.setAccessible(true);
                    Object value = field.get(receiver);
                    if(value instanceof Integer)
                        return (Integer) value;
                    break;
                } catch (NoSuchFieldException ignored) {
                    current = current.getSuperclass();
                } catch (Throwable ignored) {
                    break;
                }
            }
        }
        return null;
    }

    private static Integer invokeStaticInt(Class<?> type, String name, int argument) {
        try {
            Method method = type.getDeclaredMethod(name, int.class);
            method.setAccessible(true);
            Object value = method.invoke(null, argument);
            return value instanceof Integer ? (Integer) value : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean isSlot(Integer slot) {
        return slot != null && slot >= 0 && slot <= 1;
    }
}
