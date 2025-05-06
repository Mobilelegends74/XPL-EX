package eu.faircode.xlua.x.hook.interceptors.cell;

import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.CellInfoGsm;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.hook.interceptors.cell.stubs.SubInfoField;
import eu.faircode.xlua.x.runtime.reflect.DynField;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class SubscriptionInfoInterceptor {
    private static final String TAG = LibUtil.generateTag(SubscriptionInfoInterceptor.class);

    /*

            SubInfoField.create("mProfileClass", "cell.sim.profile.class"), //-1 ?

            SubInfoField.create("mCardString", ""),
            SubInfoField.create("mNativeAccessRules", "cell.sim.carrier.native.access.rules"),
            SubInfoField.create("mCarrierConfigAccessRules", "cell.sim.carrier.config.access.rules"),   //It is a list of Allowed Apps on the Device to talk talk to SIM Card, Cert Signed from App and Package name like "com.tmobile.pr.mytmobile"
            SubInfoField.create("mGroupUuid", ""),
            SubInfoField.create("mTransferStatus", ""),
            SubInfoField.create("mIconBitmap", ""),

            SubInfoField.create("mIsOnlyNonTerrestrialNetwork", ""),
            SubInfoField.create("mServiceCapabilities", "")
            SubInfoField.create("mGroupOwner", ""),

            //TODo:
            SubInfoField.create("mSimSlotIndex", "cell.sim.slot.index"),    //Index where it is inserted, 0 SIM slot, actual Slot
     */

    //mCard is Android 9- and cardString = ID


    //Android 10+ (mCardString)
    //      Returns the card string of the SIM card which contains the subscription.
    //      Starting with API level 29 Security Patch 2021-04-05, returns the card string if the calling
    //      The card string of the SIM card which contains the subscription or an empty string
    //       if these requirements are not met. The card string is the ICCID for UICCs or the EID for eUICCs
    //
    //      For traditional SIM cards (UICC), mCardString contains the ICCID, same as mIccId
    //      For eSIM cards (eUICC), mCardString contains the EID (eUICC Identifier), not the ICCID
    //
    //Android (mIccId)
    //      Starting with API level 29 Security Patch 2021-04-05, returns the ICC ID if the calling app
    //      return the ICC ID, or an empty string if one of these requirements is not met
    //
    //      mIccId is the ICCID (Integrated Circuit Card Identifier) of the SIM card associated with the subscription. This is a unique identifier for the physical SIM card.
    //      The GID for a SIM that maybe associated with this subscription, empty if unknown
    //
    //Android 9- (mCardId)
    //      Returns as a String like (mCardString) Android 10+ Returns as an INT to a Internal Numeric Identifier
    //      The ID of the SIM card. It is the ICCID of the active profile for a UICC card and the EID for an eUICC card.
    //
    //For Android 10+ Devices
    //  [mCardId]       = (The card ID of the SIM card. This maps uniquely to {@link #mCardString}.)
    //                      Usually (0) or (-1) ? Seems to Not Change can be Wrong
    //                      Also Located in "telephony.db" => [siminfo] => [sim_id] starting from 1 and so On
    //                      Maybe ? my Second SIM Appears ID=10 and CardId=9
    //                              my First  SIM Appears ID=1  and CardId=0
    //                          Seems it is [mId] - 1
    //  [mId]           = (Subscription Identifier, this is a device unique number and not an index into an array)
    //                     Incremental number it seems starting from (1) keeps counting, so every new SIM increments even when swapped
    //                      Also Located in "telephony.db" => [siminfo] => [_id] starting from 1 and so On
    //  [mSimSlotIndex] = (The index of the SIM slot that currently contains the subscription and not necessarily unique and maybe {@link SubscriptionManager#INVALID_SIM_SLOT_INDEX} if unknown or the subscription,  is inactive.)
    //                      Actual Slot the SIM is Inserted into
    //  [mPortIndex]    = (The port index of the Uicc card.)
    //                      Seems if SIM has Multiple Interfaces like (Multiple Numbers) it will increment, starting at 0
    //
    //  If the SIM Card has Multiple Numbers, multiple Subscription Infos Can appear note that!
    //  We typically want [mSimSlotIndex]
    //  In the Future if its an Issue, we have have Multiple Profiles for a SIM Card, Not interested in that Though Currently
    //  Default for PORT Index to (0)
    //  Subscription ID that is used Often By Telephony Manager is the "mId" Field
    //  TelephonyManager Gets the Default Sub ID, we can Assume by Default that is going to be Located in Port Index 0 as Telephony Manager does not give you options to Set Index or Sub Id
    //  On those Functions we make a get function like "getSettingIndex(String name)" this will check both index values ? or get Default ?

    //  SubscriberId = IMSI (310 260 275423179) MMC + MNC + MSIN
    //  Returns the unique subscriber ID, for example, the IMSI for a GSM phone
    //  Most "slot" Functions return from SIM Slot Index
    //  Two Functions from TelephonyManager Returns from PhoneAccountHandle , PhoneAccountHandle ID is the "mId"

    //  We Can Spoof the ID field, yes ? then when the Functions that Take in the PhoneAccountHandle Get the Original ID ?


    public static final DynField FIELD_CARD_ID = DynField.create(SubscriptionInfo.class, "mCardId");
    public static final DynField FIELD_ID = DynField.create(SubscriptionInfo.class, "mId");
    public static final DynField FIELD_SLOT_INDEX = DynField.create(SubscriptionInfo.class, "mSimSlotIndex");
    public static final DynField FIELD_PORT_INDEX = DynField.create(SubscriptionInfo.class, "mPortIndex");

    // SubInfoField.create("mId", RandomizersCache.SETTING_SIM_SUBSCRIPTION_ID),

    /*public static void setupIds(XParam param, Object o, int index) {
        if(o != null) {
            if(o instanceof SubscriptionInfo) {
                int id = FIELD_ID.getTInstance(o);
                int def = param.getSettingInt(RandomizersCache.SETTING_SIM_SUBSCRIPTION_ID + "." + index + 1, );

                GroupedMap map = param.getGroupedMap(MAP_GROUP);
                String org = map.getValueOrDefault(MAP_GROUP_ID, String.valueOf(id), )
            }
        }
    }*/

    //Phone number may start with +1 etc
    public static final List<SubInfoField> FIELDS = Arrays.asList(
            //SubInfoField.create("mId", "cell.sim.device.id"),   //Once inserted, it uses that number, so like port index but not, as it will start on (1) and increments for each new SIM

            SubInfoField.create("mIccId", RandomizersCache.SETTING_UNIQUE_ICC_ID),
            SubInfoField.create("mCardString", RandomizersCache.SETTING_UNIQUE_ICC_ID),


            SubInfoField.create("mCarrierName", RandomizersCache.SETTING_CELL_OPERATOR_NAME),   //Can at times display "No Service"

            SubInfoField.create("mDisplayName", RandomizersCache.SETTING_CELL_DISPLAY_NAME),   //Usually the Carrier Name can be custom from User
            SubInfoField.create("mDisplayNameSource", RandomizersCache.SETTING_CELL_DISPLAY_NAME_SOURCE),

            SubInfoField.create("mIconTint", RandomizersCache.SETTING_OPERATOR_ICON_TINT),

            SubInfoField.create("mNumber", RandomizersCache.SETTING_CELL_PHONE_NUMBER),
            SubInfoField.create("mDataRoaming", RandomizersCache.SETTING_CELL_DATA_ROAMING),

            SubInfoField.create("mMcc", RandomizersCache.SETTING_CELL_OPERATOR_MCC),
            SubInfoField.create("mMnc", RandomizersCache.SETTING_CELL_OPERATOR_MNC),
            SubInfoField.create("mIsEmbedded", RandomizersCache.SETTING_CELL_OPERATOR_E_SIM),

            SubInfoField.create("mIsOpportunistic", RandomizersCache.SETTING_CELL_DATA_IS_OPPORTUNISTIC),

            SubInfoField.create("mCountryIso", RandomizersCache.SETTING_SIM_COUNTRY_ISO),
            SubInfoField.create("mCarrierId", RandomizersCache.SETTING_CELL_OPERATOR_ID),   //carrierIdentification.db -> carrier_id [_id]

            SubInfoField.create("mType", RandomizersCache.SETTING_SIM_KIND) //0/LOCAL_SIM

            //SubInfoField.create("mAreUiccApplicationsEnabled", "cell.sim.uicc.applications.enabled"),   //true
            //SubInfoField.create("mPortIndex", "cell.sim.port.index"),   //Port Index of the Uicc Card Usually (0) not related to Sim Slot Index
            //SubInfoField.create("mUsageSetting", "cell.sim.usage.setting"), //DEFAULT
            //SubInfoField.create("mCardId", "cell.sim.card.id"),
            //SubInfoField.create("mIsGroupDisabled", "cell.sim.group.disabled") //false
    );


    /*public static Object construct(XParam param, int index) {

    }*/
    //We can cache in [index=>id] etc etc

    //We got to intercept the get functions
    //public static boolean interceptSingle(XParam param, )

    public static boolean interceptList(XParam param) {
        try {
            List<SubscriptionInfo> subs = new ArrayList<>();
            Object res = param.getResult();
            if(res instanceof Collection) {
                try {
                    List<SubscriptionInfo> casted = (List<SubscriptionInfo>) res;
                    subs.addAll(casted);
                }catch (Exception e) { }
            }

            Map<Integer, SubscriptionInfo> mapped = new HashMap<>();
            mapped.put(0, null);
            mapped.put(1, null);
            int sz = 0;
            if(!subs.isEmpty()) {
                for(SubscriptionInfo sub : subs) {
                    int index = sub.getSimSlotIndex();
                    if((index == 0 || index == 1) && !mapped.containsKey(index)) {
                        mapped.put(index, sub);
                        sz++;
                    }

                    if(sz == 2)
                        break;
                }
            }

            int wantedCount = Math.min(param.getSettingInt(RandomizersCache.SETTING_CELL_SIM_COUNT, sz), 2);
            for(int i = 0; i < wantedCount; i++) {
                SubscriptionInfo sub = mapped.get(i);

            }


            return true;
        }catch (Throwable e) {
            Log.e(TAG, Str.fm("Failed at Intercepting SubscriptionInfo List Object! Error=%s", e));
            return false;
        }
    }

    /*
        So need a whole emulation, and cache tie in id system to truly make this all work...
        We got
            TelephonyManager
            SubscriptionManager
            TelecomManager

            CellInfos (LAC,TAC)....
            Endless....
                We need to make a whole remapping system, ...
                Im thinking fuck the existing items,
                Create each item raw from user input

                To be Fair CellInfo Shall be Ez in theory
                Different Dialog to Create Generate a List of Cell Info
                All CellInfo is is a list of Towers Near by, Their Strength , if connected, mnc + mcc and more items

                Next Phase, Filter the getList Functions
                Doing so Make some Global Map to map the Old IDs specifically SubID as that is what is used besides Index

     */

    public static boolean interceptObject(
            XParam param,
            boolean isResult,
            int indexOverride,
            Object resultOverride) {
        try {
            Object res = resultOverride == null ?
                    isResult ?
                    param.getResult() :
                    param.getThis() : resultOverride;


            //param.tryGetArgumentInt()

            int index = getIndex(res, indexOverride);
            int indexResolved = index + 1;
            if(DebugUtil.isDebug())
                Log.d(TAG,
                        Str.fm("Cleaning and Intercepting SubscriptionInfo [%s] with Index [%s] Resolved Index of [%s] Index Override [%s] is Result [%s]",
                        Str.ensureNoDoubleNewLines(Str.toStringOrNull(res)),
                        index,
                        indexResolved,
                        indexOverride,
                        isResult));

            if(!(res instanceof SubscriptionInfo))
                return false;


            //int id = PhoneIdMap.getIdFromSubscriptionInfo((SubscriptionInfo) res, param, index, true);
            //PhoneIdMap idMap = PhoneIdMap.fromSubscriptionInfo(param, (SubscriptionInfo) res, index, true);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //FIELD_CARD_ID.setInstance(res, id -1);
            }
            else {
                String name = RandomizersCache.SETTING_UNIQUE_ICC_ID + "." + indexResolved;
                String originalValue = FIELD_CARD_ID.getTInstanceOrDefault(res, Str.EMPTY);
                if(!Str.isNullOrDefaultValue(originalValue) || (
                        param.isForceSetting(RandomizersCache.SETTING_UNIQUE_ICC_ID, originalValue) || param.isForceSetting(name, originalValue))) {
                    String value = param.getSetting(name);
                    FIELD_CARD_ID.setInstance(res, value);
                }
            }

            String old = Str.ensureNoDoubleNewLines(Str.toStringOrNull(res));
            int successful = 0;
            int failed = 0;
            for(SubInfoField field : FIELDS) {
                if(!field.isValid()) {
                    failed++;
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Field [%s] is Not Valid! Setting [%s] Failed Count [%s] ",
                                field.getFieldName(),
                                field.getSettingName(),
                                failed));
                } else {
                    // Field [mIccId] has a Null or Default Value, The Force Setting for Setting [cell.unique.sim.icc.id][cell.unique.sim.icc.id.0] is not Enabled so XPL-EX will be Skipping
                    //ToDO: Check if we need to replace
                    Object oldValue = field.getValue(res);
                    String settingName = Str.combineEx(field.getSettingName(), ".", indexResolved);
                    if(!param.isForceSetting(settingName, oldValue)) {
                        if(DebugUtil.isDebug())
                            Log.d(TAG, Str.fm("Field [%s] has a Null or Default Value, The Force Setting for Setting [%s][%s] is not Enabled so XPL-EX will be Skipping Spoofing this as it is not Forced when Null or Default! If you want to force this Please go into XPL-EX Settings go to Category (xplex) Select Force Settings List to Control the Flag, this is for Setting (%s)",
                                    field.getFieldName(),
                                    field.getSettingName(),
                                    settingName,
                                    settingName));
                        failed++;
                    } else {
                        if(!field.setFromSetting(res, indexResolved, param)) {
                            failed++;
                            if(DebugUtil.isDebug())
                                Log.d(TAG, Str.fm("Field [%s] was Not Updated (error) ! Setting [%s] Failed Count [%s] ",
                                        field.getFieldName(),
                                        field.getSettingName(),
                                        failed));
                        } else {
                            Object newValue = field.getValue(res);
                            successful++;
                            if(DebugUtil.isDebug())
                                Log.d(TAG, Str.fm("Field [%s] was Updated from (%s) to [%s] with Setting Name [%s] Successful Count=%s",
                                        field.getFieldName(),
                                        Str.ensureNoDoubleNewLines(Str.toStringOrNull(oldValue)),
                                        Str.ensureNoDoubleNewLines(Str.toStringOrNull(newValue)),
                                        field.getSettingName(),
                                        successful));
                        }
                    }
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Finished Replacing and Intercepting SubscriptionInfo Object at Index [%s] Resolved Index [%s] Index Override [%s] Successful [%s] Failed [%s] Is Result [%s] Object=%s",
                        index,
                        indexResolved,
                        indexOverride,
                        successful,
                        failed,
                        isResult,
                        Str.ensureNoDoubleNewLines(Str.toStringOrNull(res))));

            if(resultOverride != null) {
                param.setLogOld(old);
                param.setLogNew(Str.ensureNoDoubleNewLines(Str.toStringOrNull(res)));
                if(isResult) {
                    param.setResult(res);   //Ensure result is still pointed to our modified one, should be but can never be too sure
                }
            }

            return successful > 0;
        }catch (Throwable e) {
            Log.e(TAG, Str.fm("Failed at Intercepting SubscriptionInfo Object! Index Override [%s] Error=%s",
                    indexOverride,
                    e));
            return false;
        }
    }

    public static int getIndex(Object o, int indexOverride) {
        int safeIndexOverride = Math.min(1, Math.max(indexOverride, 0));
        if(o instanceof SubscriptionInfo) {
            SubscriptionInfo sub = (SubscriptionInfo) o;
            int index = sub.getSimSlotIndex();
            if(index < 0) return indexOverride;

            return index;
            /*if(FIELD_PORT_INDEX.isValid()) {
                Object res = FIELD_PORT_INDEX.getInstance(o);
                if(res instanceof Integer) {
                    int val = (int) res;
                    if(val == 0 || val == 1)
                        return val;
                }
            }*/
        }

        return safeIndexOverride;
    }
}
