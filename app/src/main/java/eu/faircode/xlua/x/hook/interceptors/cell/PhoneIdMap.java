package eu.faircode.xlua.x.hook.interceptors.cell;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.XParam;
import eu.faircode.xlua.hooks.XHookUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.StrConversionUtils;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.runtime.reflect.DynField;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class PhoneIdMap {
    public static final DynField FIELD_ID_SUBSCRIPTION_INFO = DynField.create(SubscriptionInfo.class, "mId");
    public static final DynField FIELD_SLOT_INDEX_SUBSCRIPTION_INFO = DynField.create(SubscriptionInfo.class, "mSimSlotIndex");

    public static final DynField FIELD_ID_PHONE_ACCOUNT = DynField.create(PhoneAccountHandle.class, "mId");

    public static final Map<String, String> ID_MAP = new HashMap<>();

    public static boolean hasAsModifiedId(int id) { return hasAsModifiedId(String.valueOf(id)); }
    public static boolean hasAsModifiedId(String id) { return ID_MAP.containsValue(id); }

    public static boolean hasAsOriginalId(int id, int index) { return hasAsOriginalId(String.valueOf(id), String.valueOf(index)); }
    public static boolean hasAsOriginalId(String id, String index) { return ID_MAP.containsKey(index + ":" + id); }

    public static void put(int id, int index, int newId) { put(String.valueOf(id), String.valueOf(index), String.valueOf(newId)); }
    public static void put(String id, String index, String newId) { ID_MAP.put(index + ":" + id, newId); }


    public static void init(XParam param) {
        if(ID_MAP.isEmpty()) {
            try {
                SubscriptionManager subscriptionManager =
                        (SubscriptionManager) param.getApplicationContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                @SuppressLint("MissingPermission")
                List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
                int ix = 0;
                for(SubscriptionInfo s : subscriptionInfoList) {
                    int index = SubscriptionInfoInterceptor.getIndex(s, ix);
                    int sub = s.getSubscriptionId();
                    if(!hasAsOriginalId(sub, index)) {
                        boolean found = false;
                        for(String key : new ArrayList<>(ID_MAP.keySet())) {
                            if(key.startsWith(String.valueOf(index))) {
                                String value = ID_MAP.get(key);
                                if(!Str.isEmpty(value) && value.equals(String.valueOf(sub))) {
                                    found = true;
                                    break;
                                }
                            }
                        }

                        if(!found) {
                            getIdFromOriginal(String.valueOf(sub), param, index);
                        }
                    }
                }
            } catch (Exception ignored) { }
        }
    }

    public static int getIdFromPhoneAccountHandle(
            PhoneAccountHandle phoneAccountHandle,
            XParam param,
            int index,
            boolean updateObject) {
        if(phoneAccountHandle != null) {
            String id = getIdFromOriginal(phoneAccountHandle.getId(), param, index);
            if(updateObject) FIELD_ID_PHONE_ACCOUNT.setInstance(phoneAccountHandle, id);
            return StrConversionUtils.tryParseInt(id);
        } else {
            return -1;
        }
    }

    public static int getIdFromSubscriptionInfo(
            SubscriptionInfo subscriptionInfo,
            XParam param,
            int index,
            boolean updateObject) {
        if(subscriptionInfo != null) {
            String id = getIdFromOriginal(String.valueOf(subscriptionInfo.getSubscriptionId()), param, index);
            if(updateObject) FIELD_ID_SUBSCRIPTION_INFO.setInstance(subscriptionInfo, StrConversionUtils.tryParseInt(id));
            return StrConversionUtils.tryParseInt(id);
        } else {
            return -1;
        }
    }

    public static String getIdFromOriginal(String givenId, XParam param, int index) {
        if(param != null) {
            if(!hasAsOriginalId(givenId, String.valueOf(index))) {
                String idSettingName = RandomizersCache.SETTING_SIM_SUBSCRIPTION_ID + "." + index + 1;
                int val = param.getSettingInt(idSettingName, -1);
                if(val == -1) {
                    val = RandomGenerator.nextInt(1, 25);
                    int tries = 0;
                    while (hasAsModifiedId(val)) {
                        tries++;
                        val =  RandomGenerator.nextInt(1, 25);
                        if(tries >= 20)
                            return String.valueOf(val);
                    }
                }

                put(givenId, String.valueOf(index), String.valueOf(val));
                return String.valueOf(val);
            } else {
                return ID_MAP.get(index + ":" + givenId);
            }
        } else {
            return givenId;
        }
    }

    /*public String original;
    public int spoofed;
    public int index;
    public int indexSetting;

    public PhoneIdMap() { }
    public PhoneIdMap(int index, int spoofed, String original) {
        this.index = index;
        this.spoofed = spoofed;
        this.original = original;
        this.indexSetting = index + 1;
    }

    public static PhoneIdMap fromSubId(
            XParam param,
            int subId) {
        //invoke the telephone services get subscribers ?
        //We can also do it with the method below
        GroupedMap groupMap = param.getGroupedMap(MAP_GROUP);
        if(!groupMap.hasValue(MAP_GROUP_ID_REV, String.valueOf(subId))) {
            try {
                SubscriptionManager subscriptionManager = (SubscriptionManager) param.getApplicationContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                @SuppressLint("MissingPermission")
                List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            } catch (Exception ignored) { }
        }

        return null;
    }

    public static PhoneIdMap fromPhoneAccountHandle(
            XParam param,
            PhoneAccountHandle phoneAccountHandle,
            int index,
            boolean updateObject) {
        PhoneIdMap map = new PhoneIdMap();
        map.index = index;
        map.indexSetting = index + 1;
        if(phoneAccountHandle != null) {
            String orgId = phoneAccountHandle.getId();
            String idSettingName = RandomizersCache.SETTING_SIM_SUBSCRIPTION_ID + "." + index + 1;
            int defaultValue = index == 0 ?
                    RandomGenerator.nextInt(1, 8) :
                    RandomGenerator.nextInt(9, 18);

            int fake = param.getSettingInt(idSettingName, defaultValue);
            GroupedMap groupMap = param.getGroupedMap(MAP_GROUP);
            //We also want to put the reverse ?
            map.original = orgId;
            map.spoofed = groupMap.getValueOrDefault(MAP_GROUP_ID, map.original, fake);

            groupMap.pushValue(MAP_GROUP_ID_REV, String.valueOf(fake), map.original);

            if(updateObject) {
                FIELD_ID_PHONE_ACCOUNT.setInstance(phoneAccountHandle, String.valueOf(map.spoofed));
            }
        }

        return map;
    }

    public static PhoneIdMap fromSubscriptionInfo(
            XParam param,
            SubscriptionInfo subscriptionInfo,
            int index,
            boolean updateObject) {
        PhoneIdMap map = new PhoneIdMap();
        map.index = index;
        map.indexSetting = index + 1;
        if(subscriptionInfo != null) {
            String idSettingName = RandomizersCache.SETTING_SIM_SUBSCRIPTION_ID + "." + index + 1;
            int defaultValue = index == 0 ?
                    RandomGenerator.nextInt(1, 8) :
                    RandomGenerator.nextInt(9, 18);

            int fake = param.getSettingInt(idSettingName, defaultValue);
            GroupedMap groupMap = param.getGroupedMap(MAP_GROUP);

            Object res = FIELD_ID_SUBSCRIPTION_INFO.getInstance(subscriptionInfo);
            if(!(res instanceof Integer)) {
                map.original = String.valueOf(fake);
            } else {
                map.original = String.valueOf((int)res);
            }

            groupMap.pushValue(MAP_GROUP_ID_REV, String.valueOf(fake), map.original);

            map.spoofed = groupMap.getValueOrDefault(MAP_GROUP_ID, map.original, fake);
            if(updateObject) {
                FIELD_ID_SUBSCRIPTION_INFO.setInstance(subscriptionInfo, map.spoofed);
            }
        }

        return map;
    }*/
}
