package eu.faircode.xlua.x.hook.interceptors.network;

import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiSsid;
import android.text.TextUtils;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.hook.interceptors.network.utils.WifiInfoDataGenerator;
import eu.faircode.xlua.x.hook.interceptors.network.utils.WifiSsidCreator;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.runtime.reflect.DynamicMethod;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.network.NetUtils;
import eu.faircode.xlua.x.runtime.reflect.DynamicGetSetPairs;
import eu.faircode.xlua.x.runtime.reflect.ReflectUtil;
import eu.faircode.xlua.x.xlua.LibUtil;

public class WifiInfoInterceptor {
    private static final String TAG = LibUtil.generateTag(WifiInfoInterceptor.class);
    public static final String DEFAULT_MAC_ADDRESS = "02:00:00:00:00:00";

    public static final DynamicGetSetPairs MAC_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mMacAddress")
            .bindGetMethod("getMacAddress")
            .bindSetMethod("setMacAddress");

    public static final DynamicGetSetPairs SSID_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mWifiSsid")
            .bindGetMethod("getSSID")
            .bindSetMethod("setSSID")
            .bindGetMethod("getWifiSsid");  //This one is in form of string


    public static final String WIFI_SSID_CLASS = "android.net.wifi.WifiSsid";

    public static final DynamicMethod WIFI_SSID_TO_STRING = new DynamicMethod(WIFI_SSID_CLASS, "toString")
            .setAccessible(true);

    public static final DynamicGetSetPairs WIFI_SSID_CREATE_PAIRS = DynamicGetSetPairs.create(WIFI_SSID_CLASS)
            .bindSetMethod("fromUtf8Text", CharSequence.class)
            .bindSetMethod("fromString", String.class)
            .bindSetMethod("createFromAsciiEncoded", String.class);

    public static final DynamicGetSetPairs BSSID_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mBSSID")
            .bindSetMethod("setBSSID", String.class)
            .bindGetMethod("getBSSID");

    public static final DynamicGetSetPairs NET_ID_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mNetworkId")
            .bindGetMethod("getNetworkId")
            .bindSetMethod("setNetworkId");

    public static final DynamicGetSetPairs ADDRESS_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mIpAddress")
            .bindSetMethod("setInetAddress")
            .bindGetMethod("getIpAddress");




    public static final DynamicField FIELD_IS_HIDDEN_SSID = DynamicField.create(WifiInfo.class, "mIsHiddenSsid")
            .setAccessible(true);

    /*
        @UnsupportedAppUsage
    private InetAddress mIpAddress;
    @UnsupportedAppUsage
    private String mMacAddress = DEFAULT_MAC_ADDRESS;
     */

    //This one returns "android.net.MacAddress"
    public static final DynamicField FIELD_MULTI_LINK_MAC_ADDRESS = new DynamicField(WifiInfo.class, "mApMldMacAddress")
            .setAccessible(true);

    public static final DynamicField FIELD_MULTI_LINK_ID = new DynamicField(WifiInfo.class, "mApMloLinkId")
            .setAccessible(true);

    //mAffiliatedMloLinks
    public static final DynamicField FIELD_AFFILIATED_MLO_LINKS = new DynamicField(WifiInfo.class, "mAffiliatedMloLinks")
            .setAccessible(true);

    public static final DynamicField FIELD_SUBSCRIPTION_ID = new DynamicField(WifiInfo.class, "mSubscriptionId")
            .setAccessible(true);

    public static final DynamicField FIELD_IS_TRUSTED = new DynamicField(WifiInfo.class, "mTrusted")
            .setAccessible(true);

    public static final DynamicGetSetPairs MLO_LINK_ID_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mApMloLinkId")
            .bindSetMethod("setApMloLinkId", int.class);

    public static final DynamicGetSetPairs MLO_LINK_MAC_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mApMldMacAddress")
            .bindSetMethod("setApMldMacAddress");

    public static final DynamicGetSetPairs AFFILIATED_MO_LINKS_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mAffiliatedMloLinks")
            .bindSetMethod("setAffiliatedMloLinks");

    public static final DynamicGetSetPairs TRUSTED_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mTrusted")
            .bindSetMethod("setTrusted");

    public static final DynamicGetSetPairs RESTRICTED_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mRestricted")
            .bindSetMethod("setRestricted");


    public static final DynamicGetSetPairs OSU_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mOsuAp")
            .bindSetMethod("setOsuAp");

    public static final DynamicGetSetPairs FQDN_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mFqdn")
            .bindSetMethod("setFQDN");


    public static final DynamicGetSetPairs PROVIDER_FRIENDLY_NAME_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mProviderFriendlyName")
            .bindSetMethod("setProviderFriendlyName");

    public static final DynamicGetSetPairs REQUESTING_PACKAGE_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mRequestingPackageName")
            .bindSetMethod("setRequestingPackageName");

    public static final DynamicGetSetPairs SUB_ID_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mSubscriptionId")
            .bindSetMethod("setSubscriptionId");

    public static final DynamicGetSetPairs IS_HIDDEN_SSID_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mIsHiddenSsid")
            .bindSetMethod("setHiddenSSID");


    public static final DynamicGetSetPairs SUPPLICANT_STATE_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mSupplicantState")
            .bindSetMethod("setSupplicantState");

    public static final DynamicGetSetPairs OEM_PAID_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mOemPaid")
            .bindSetMethod("setOemPaid");

    public static final DynamicGetSetPairs OEM_PRIVATE_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mOemPrivate")
            .bindSetMethod("setOemPrivate");

    public static final DynamicGetSetPairs PASS_UNIQUE_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mPasspointUniqueId")
            .bindSetMethod("setPasspointUniqueId");

    public static final DynamicGetSetPairs USABLE_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mIsUsable")
            .bindSetMethod("setUsable");

    public static final DynamicGetSetPairs METERED_HINT_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mMeteredHint")
            .bindSetMethod("setMeteredHint");

    public static final DynamicGetSetPairs RSSI_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mRssi")
            .bindSetMethod("setRssi");

    public static final DynamicGetSetPairs FREQ_PAIRS = DynamicGetSetPairs.create(WifiInfo.class)
            .bindField("mFrequency")
            .bindSetMethod("setFrequency");

    public static final DynamicField FIELD_SEC_TYPE = DynamicField.create(WifiInfo.class, "mSecurityType")
            .setAccessible(true);

    public static final DynamicMethod SEC_TYPE_METHOD = new DynamicMethod(WifiInfo.class, "setCurrentSecurityType", int.class)
            .setAccessible(true);

    public static final int SECURITY_TYPE_PSK = ReflectUtil.useFieldValueOrDefaultInt(WifiConfiguration.class, "SECURITY_TYPE_PSK", 2);

    public static final List<DynamicField> SPEED_FIELDS = Arrays.asList(
            new DynamicField(WifiInfo.class, "mTxLinkSpeed").setAccessible(true),
            new DynamicField(WifiInfo.class, "mMaxSupportedTxLinkSpeed").setAccessible(true),
            new DynamicField(WifiInfo.class, "mRxLinkSpeed").setAccessible(true),
            new DynamicField(WifiInfo.class, "mMaxSupportedRxLinkSpeed").setAccessible(true));

    public static boolean intercept(XParam param, boolean getResult) {
        if(param == null) return false;
        //System.nanoTime() spoof ?
        //ToDo: uupdate this sytem to use the force or not system

        try {
            Object obj = getResult ? param.getResult() : param.getThis();
            if(!(obj instanceof WifiInfo))
                throw new Exception("Object is Not instance of WifiIno, Object=" + (obj == null ? "null" : obj.getClass().getName()));

            WifiInfo instance = (WifiInfo) obj;
            if(DebugUtil.isDebug())
                Log.d(TAG, "Intercepting WifiInfo Object, toString=" + instance.toString());

            param.setLogOld(instance.toString());

            GroupedMap map = param.getGroupedMap(NetUtils.GROUP_NAME);
            //Assume its wlan0
            int speed = map.getValueOrDefault(
                    NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                    "generic_speed",
                    RandomGenerator.nextInt(144, 255), false);

            for(DynamicField speedField : SPEED_FIELDS) {
                if(speedField.isValid())
                    speedField.trySetValueInstanceEx(instance, speed);
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Speed Set=" + speed + " (mbps)");

            String newBssid = map.getValueOrSetting(
                    NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                    "bssid",
                    param,
                    "unique.network.bssid", false);
            String oldBssid = BSSID_PAIRS.getValueInstance(instance);
            if(!TextUtils.isEmpty(oldBssid) && !DEFAULT_MAC_ADDRESS.equalsIgnoreCase(oldBssid)) {
                if(!BSSID_PAIRS.setValueInstance(instance, newBssid)) {
                    Log.e(TAG, "Critical Error Failed to Set WifiInfo BSSID, methods seem to be Invalid, please Contact the Developer!");
                } else {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Old BSSID=" + oldBssid + "\n" +
                                        "New BSSID=" + newBssid);
                }
            }

            String newSsid = map.getValueOrSetting(
                    NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                    "ssid",
                    param,
                    "unique.network.ssid", false);
            Object ssidObject = SSID_PAIRS.getValueInstanceMethodsFirst(instance);
            if(ssidObject != null) {
                String oldSsid = null;
                //ToDo: Clean this shit up
                if(!(ssidObject instanceof CharSequence)) {
                    //Not a String but a WifiSsid Object
                    Object ssidVal = WIFI_SSID_TO_STRING.tryInstanceInvokeEx(ssidObject);
                    if(ssidVal instanceof CharSequence)
                        oldSsid = (String) ssidVal;
                    else
                        Log.w(TAG,  "WIFI SSID Object is null... cant resolve SSID name");
                } else {
                    oldSsid = (String) ssidObject;
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, "CURRENT SSID=" + oldSsid);

                if(!Str.isEmpty(oldSsid)) {
                    WifiSsid created = WifiSsidCreator.create(newSsid);
                    if(created == null || !SSID_PAIRS.setValueInstance(instance, created)) {
                        Log.e(TAG, "Critical Error Failed to Set SSID Object, Contact Developer!");
                    } else {
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "Old SSID=" + oldSsid + "\n" +
                                            "New SSID=" + newSsid);
                    }
                }
            }

            String oldMac = MAC_PAIRS.getValueInstance(instance);
            String newMac = map.getValueOrSetting(
                    NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                    "hardware_address",
                    param,
                    "unique.network.mac.address", false);
            if(!Str.isEmpty(oldMac) && !DEFAULT_MAC_ADDRESS.equalsIgnoreCase(oldMac)) {
                if(!MAC_PAIRS.setValueInstance(instance, newMac)) {
                    Log.e(TAG, "Critical Error Failed to Set WifiInfo Hardware Address, methods seem to be Invalid, please Contact the Developer!");
                } else {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Old MAC=" + oldMac + "\n" +
                                        "New MAC=" + newMac);
                }
            }

            //Set this to Null always, but I get that Integer Error expecting Mac
            //We Pretty much erase that it is a Multi Link Wifi 7 Network

            MLO_LINK_MAC_PAIRS.setValueInstance(instance, null);
            MLO_LINK_ID_PAIRS.setValueInstance(instance, 0);
            AFFILIATED_MO_LINKS_PAIRS.setValueInstance(instance, Collections.emptyList());
            TRUSTED_PAIRS.setValueInstance(instance, true);
            RESTRICTED_PAIRS.setValueInstance(instance, false);
            //OEM_PRIVATE_PAIRS.setValueInstance(instance, true);
            OSU_PAIRS.setValueInstance(instance, false);
            FQDN_PAIRS.setValueInstance(instance, null);
            PROVIDER_FRIENDLY_NAME_PAIRS.setValueInstance(instance, null);
            REQUESTING_PACKAGE_PAIRS.setValueInstance(instance, null);
            SUB_ID_PAIRS.setValueInstance(instance, -1);
            IS_HIDDEN_SSID_PAIRS.setValueInstance(instance, false);
            SUPPLICANT_STATE_PAIRS.setValueInstance(instance, SupplicantState.COMPLETED);   //Should we set to completed ?
            //OEM_PAID_PAIRS.setValueInstance(instance, false);   //true ? maybe
            PASS_UNIQUE_PAIRS.setValueInstance(instance, null);
            USABLE_PAIRS.setValueInstance(instance, true);
            METERED_HINT_PAIRS.setValueInstance(instance, false);
            RSSI_PAIRS.setValueInstance(instance, WifiInfoDataGenerator.generateRandomRssi());

            int freq = map.getValueOrDefault(
                    NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                    "frequency_band",
                    WifiInfoDataGenerator.generateRandomFrequency(), false);
            FREQ_PAIRS.setValueInstance(instance, freq);

            if(!FIELD_SEC_TYPE.trySetValueInstanceEx(instance, WifiInfo.SECURITY_TYPE_PSK))
                SEC_TYPE_METHOD.tryInstanceInvokeEx(instance, SECURITY_TYPE_PSK);

            Object netIdObject = NET_ID_PAIRS.getValueInstance(instance);
            if(netIdObject instanceof Integer) {
                NET_ID_PAIRS.setValueInstance(instance, -1);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Old Net ID=" + ((int)netIdObject) + "\n" +
                                    "New Net ID=-1");
            }

            Object addressObject = ADDRESS_PAIRS.getValueInstance(instance);
            if(addressObject != null) {
                String address = "";
                if(addressObject instanceof Integer) {
                    InetAddress toAddress = NetUtils.intToInetAddress((int)addressObject);
                    if(toAddress == null)
                        Log.w(TAG, "IPAddress Object is Null...");
                    else
                        address = toAddress.getHostAddress();
                } else {
                    address = ((InetAddress) addressObject).getHostAddress();
                }

                if(!"127.0.0.1".equalsIgnoreCase(address) && !Str.isEmpty(address)) {
                    String newAddress = map.getValueOrSetting(
                            NetUtils.ASSUMED_WIFI_NET_INF_NAME,
                            address,
                            param,
                            "network.host.address");
                    if(!Str.isEmpty(newAddress)) {
                        if(ADDRESS_PAIRS.setValueInstance(instance, Inet4Address.getByName(newAddress))) {
                            if(DebugUtil.isDebug())
                                Log.d(TAG, "Old Address=" + address + "\n" +
                                                "New Address=" + newAddress);
                        }else {
                            Log.e(TAG, "Failed to Set new Address! Old=" + address + " New=" + newAddress);
                        }
                    }
                } else {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "Skipping Address Spoof=" + address);
                }
            }

            param.setLogNew(instance.toString());
            if(getResult) param.setResult(instance);
            return true;
        }catch (Throwable e) {
            Log.e(TAG, "Failed to Intercept and Clean WifiInfo Object, Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return false;
        }
    }
}
