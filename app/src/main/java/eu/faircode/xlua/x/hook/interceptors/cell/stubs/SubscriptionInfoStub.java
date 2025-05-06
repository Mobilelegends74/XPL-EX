package eu.faircode.xlua.x.hook.interceptors.cell.stubs;

import android.content.Context;
import android.telephony.SubscriptionInfo;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.x.hook.interceptors.cell.SubFieldPairRr;
import eu.faircode.xlua.x.xlua.LibUtil;

public class SubscriptionInfoStub {
    private static final String TAG = LibUtil.generateTag(SubscriptionInfoStub.class);

    private final int index;
    private final SubscriptionInfo subscriptionInfo;

    /*
    private static final DynamicField FIELD_ICC_ID = DynamicField.create(SubscriptionInfo.class, "mIccId").setAccessible(true);
    private static final DynamicField FIELD_DISPLAY_NAME = DynamicField.create(SubscriptionInfo.class, "mDisplayName").setAccessible(true);
    private static final DynamicField FIELD_CARRIER_NAME = DynamicField.create(SubscriptionInfo.class, "mCarrierName").setAccessible(true);
    private static final DynamicField FIELD_NUMBER = DynamicField.create(SubscriptionInfo.class, "mNumber").setAccessible(true);
    private static final DynamicField FIELD_MCC_STR = DynamicField.create(SubscriptionInfo.class, "mMcc").setAccessible(true);
    private static final DynamicField FIELD_MNC_STR = DynamicField.create(SubscriptionInfo.class, "mMnc").setAccessible(true);
    private static final DynamicField FIELD_IS_EMBEDDED = DynamicField.create(SubscriptionInfo.class, "mIsEmbedded").setAccessible(true);
    private static final DynamicField FIELD_CARD_STRING = DynamicField.create(SubscriptionInfo.class, "mCardString").setAccessible(true);
    private static final DynamicField FIELD_COUNTRY_ISO = DynamicField.create(SubscriptionInfo.class, "mCountryIso").setAccessible(true);
    private static final DynamicField FIELD_CARRIER_ID = DynamicField.create(SubscriptionInfo.class, "mCarrierId").setAccessible(true);
    private static final DynamicField FIELD_TYPE = DynamicField.create(SubscriptionInfo.class, "mType").setAccessible(true);*/


    //SettingPair<boolean>.fromClass(...).method("").xplex_setting

    //
    //
    //Sim Card Dialog showing (2) you can edit either or dialog with randomizers etc
    //Options to set the id
    //
    //

    //Cell hooks for settings can use like "settings["cell.*"]"

    public static final List<SubFieldPairRr> FIELDS = Arrays.asList(
            SubFieldPairRr.create("mId", "cell.sim.device.id"),   //Once inserted, it uses that number, so like port index but not, as it will start on (1) and increments for each new SIM
            SubFieldPairRr.create("mIccId", "cell.sim.icc.id"),
            SubFieldPairRr.create("mSimSlotIndex", "cell.sim.slot.index"),    //Index where it is inserted, 0 SIM slot, actual Slot
            SubFieldPairRr.create("mDisplayName", "cell.sim.display.name"),   //Usually the Carrier Name
            SubFieldPairRr.create("mCarrierName", "cell.sim.carrier.name"),   //Can at times display "No Service"
            SubFieldPairRr.create("mDisplayNameSource", "cell.sim.display.name.source"),
            SubFieldPairRr.create("mIconTint", "cell.sim.carrier.icon.tint"),
            SubFieldPairRr.create("mNumber", "cell.sim.phone.number"),
            SubFieldPairRr.create("mDataRoaming", "cell.sim.is.roaming"),
            SubFieldPairRr.create("mMcc", "cell.sim.carrier.mcc"),
            SubFieldPairRr.create("mMnc", "cell.sim.carrier.mnc"),

            SubFieldPairRr.create("mIsEmbedded", "cell.sim.is.e.sim"),
            SubFieldPairRr.create("mCardString"),
            SubFieldPairRr.create("mNativeAccessRules", "cell.sim.carrier.native.access.rules"),
            SubFieldPairRr.create("mCarrierConfigAccessRules", "cell.sim.carrier.config.access.rules"),   //It is a list of Allowed Apps on the Device to talk talk to SIM Card, Cert Signed from App and Package name like "com.tmobile.pr.mytmobile"
            SubFieldPairRr.create("mIsOpportunistic", "cell.sim.carrier.is.opportunistic"),
            SubFieldPairRr.create("mGroupUuid"),
            SubFieldPairRr.create("mCountryIso", "cell.sim.carrier.country.iso"),
            SubFieldPairRr.create("mCarrierId", "cell.sim.carrier.id"),   //carrierIdentification.db -> carrier_id [_id]
            SubFieldPairRr.create("mProfileClass", "cell.sim.profile.class"), //-1 ?
            SubFieldPairRr.create("mType", "cell.sim.type"),  //0/LOCAL_SIM
            SubFieldPairRr.create("mGroupOwner"),
            SubFieldPairRr.create("mAreUiccApplicationsEnabled", "cell.sim.uicc.applications.enabled"),   //true
            SubFieldPairRr.create("mPortIndex", "cell.sim.port.index"),   //Port Index of the Uicc Card Usually (0) not related to Sim Slot Index
            SubFieldPairRr.create("mUsageSetting", "cell.sim.usage.setting"), //DEFAULT
            SubFieldPairRr.create("mTransferStatus"),
            SubFieldPairRr.create("mIconBitmap"),
            SubFieldPairRr.create("mCardId", "cell.sim.card.id"),
            SubFieldPairRr.create("mIsGroupDisabled", "cell.sim.group.disabled"), //false
            SubFieldPairRr.create("mIsOnlyNonTerrestrialNetwork"),
            SubFieldPairRr.create("mServiceCapabilities")
            );

    /*
                SubFieldPairRr.create("mEhplmns", "cell.sim.carrier.ehplmns"),
            SubFieldPairRr.create("mHplmns", "cell.sim.carrier.hplmns"),
     */

    public int id;                      //Incremental number it seems starting from (1) keeps counting, so every new SIM increments even when swapped
    public String iccId;                //890 126 027 575 423 1797
    public int simSlotIndex;            //Indicating what Slot index the SIM Card is in , -1 if it is not inserted 0 through 1
    public String displayName;          //T-Mobile / Verizon
    public String carrierName;          //T-Mobile / Verizon
    public String displayNameSource;    //3 ? (CARRIER)
    public int iconTime;                //-6728704 ?
    public String number;               //Phone Number, it can have prefix like (+1) or sometimes it wont have the "+" but still has the "1" at least if its inserted ?
                                        //Display Number Format usually (1)
    public int dataRoaming;             //Is Roaming 0-1
    public String mcc;                  //310 / 311
    public String mnc;                  //260 / 480
    public String countryIso;           //us
    public boolean isEmbedded;          //Is E SIM 0-1

    public String cardString;
    public int cardId;                  //Card String or ID ? "89148000011450288566"
    public int portIndex;
    public boolean isOpportunistic;     //0-1
    public String groupUuid;            //EMPTY
    public boolean isGroupDisabled;
    public int carrierId;               // 1 / 1839 , seems to be a more unique id as many can share MCC AND MNC Combinations
    public int profileClass;            //-1
    public int type;                    // 0

    //MNC + MCC
    public String[] ehplmns = new String[0];    //311480,310590,310890 or Empty  ( (Equivalent Home PLMN): These are the equivalent home networks that your carrier has agreements with)
    public String[] hplmns = new String[0];     //311480 or 310260,310260,310260,310260 ( (Home PLMN): These represent your primary home networks. The repeated values)
    public String groupOwner;
    //Type array
    public boolean areUiccApplicationsEnabled;
    public int usageSetting;
    public boolean nonTerrestrialNetwork;
    public int networkCapabilities;
    public int transferStatus;
    public boolean satelliteESOSSupported;
    //PhoneNumber = Prefix + Number +1.....

    public SubscriptionInfoStub(SubscriptionInfo subscriptionInfo, int index) {
        this.subscriptionInfo = subscriptionInfo;
        this.index = index;
    }

    public void resolve(Context context) {
        try {



        }catch (Exception e) {
            Log.e(TAG, "Failed to Resolved Sub Info, Index=" + index + " Error=" + e);
        }
    }
}
