package eu.faircode.xlua.x.hook.interceptors.network;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiSsid;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ListUtils;
import eu.faircode.xlua.x.hook.interceptors.network.utils.InformationElementCreator;
import eu.faircode.xlua.x.hook.interceptors.network.utils.WifiRandom;
import eu.faircode.xlua.x.hook.interceptors.network.utils.WifiSsidCreator;
import eu.faircode.xlua.x.runtime.HiddenApi;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.runtime.reflect.DynamicGetSetPairs;
import eu.faircode.xlua.x.runtime.reflect.DynamicMethod;
import eu.faircode.xlua.x.ui.dialogs.wifi.XWifiNetwork;
import eu.faircode.xlua.x.xlua.LibUtil;

public class MockWifiResult {
    private static final String TAG = LibUtil.generateTag(MockWifiResult.class);

    static {
        HiddenApi.bypassHiddenApiRestrictions();
    }

    public static final int WIFI_STANDARD_UNKNOWN = 0;
    public static final int WIFI_STANDARD_LEGACY = 1;
    public static final int WIFI_STANDARD_11N = 4;
    public static final int WIFI_STANDARD_11AC = 5;
    public static final int WIFI_STANDARD_11AX = 6;
    public static final int WIFI_STANDARD_11AD = 7;

    //Set these fields to have SDK Requirements
    //Also put it in some array or map invoke, it will set if SDK meets requirements

    public static final DynamicField FIELD_CREATOR = new DynamicField(ScanResult.class, "CREATOR")
            .setAccessible(true);

    public static final DynamicMethod WIFI_SSID_WRITE_TO_PARCEL = new DynamicMethod("android.net.wifi.WifiSsid", "writeToParcel", Parcel.class, int.class)
            .setAccessible(true);


    public static final DynamicField FIELD_SSID = new DynamicField(ScanResult.class, "SSID")
            .setAccessible(true);

    public static final DynamicField FIELD_WIFI_SSID = new DynamicField(ScanResult.class, "wifiSsid")
            .setAccessible(true);

    public static final DynamicField FIELD_BSSID = new DynamicField(ScanResult.class, "BSSID")
            .setAccessible(true);

    public static final DynamicField FIELD_CAPABILITIES = new DynamicField(ScanResult.class, "capabilities")
            .setAccessible(true);

    public static final DynamicField FIELD_HESSID = new DynamicField(ScanResult.class, "hessid")
            .setAccessible(true);

    public static final DynamicField FIELD_ANQP_DOM_ID = new DynamicField(ScanResult.class, "anqpDomainId")
            .setAccessible(true);

    public static final DynamicField FIELD_LEVEL = new DynamicField(ScanResult.class, "level")
            .setAccessible(true);

    public static final DynamicField FIELD_FREQUENCY = new DynamicField(ScanResult.class, "frequency")
            .setAccessible(true);

    public static final DynamicField FIELD_TIMESTAMP = new DynamicField(ScanResult.class, "timestamp")
            .setAccessible(true);

    public static final DynamicField FIELD_DISTANCE_CM = new DynamicField(ScanResult.class, "distanceCm")
            .setAccessible(true);

    public static final DynamicField FIELD_DISTANCE_SD_CM = new DynamicField(ScanResult.class, "distanceSdCm")
            .setAccessible(true);

    public static final DynamicField FIELD_CHANNEL_WIDTH = new DynamicField(ScanResult.class, "channelWidth")
            .setAccessible(true);

    public static final DynamicField FIELD_CENTER_FREQ_0 = new DynamicField(ScanResult.class, "centerFreq0")
            .setAccessible(true);

    public static final DynamicField FIELD_CENTER_FREQ_1 = new DynamicField(ScanResult.class, "centerFreq1")
            .setAccessible(true);

    public static final DynamicField FIELD_WIFI_STANDARD = new DynamicField(ScanResult.class, "mWifiStandard")
            .setAccessible(true);

    public static final DynamicField FIELD_WIFI_SEEN = new DynamicField(ScanResult.class, "seen")
            .setAccessible(true);

    public static final DynamicField FIELD_WIFI_AUTO_JOIN_STATS = new DynamicField(ScanResult.class, "autoJoinStatus")
            .setAccessible(true);

    public static final DynamicField FIELD_WIFI_UNTRUSTED = new DynamicField(ScanResult.class, "untrusted")
            .setAccessible(true);

    public static final DynamicField FIELD_WIFI_NUM_USAGE = new DynamicField(ScanResult.class, "numUsage")
            .setAccessible(true);

    public static final DynamicField FIELD_WIFI_NUM_CONNECTION = new DynamicField(ScanResult.class, "numConnection")
            .setAccessible(true);

    public static final DynamicField FIELD_WIFI_NUM_CONFIG_F = new DynamicField(ScanResult.class, "numIpConfigFailures")
            .setAccessible(true);

    public static final DynamicField FIELD_WIFI_IS_AUTO_J = new DynamicField(ScanResult.class, "isAutoJoinCandidate")
            .setAccessible(true);

    public static final DynamicField FIELD_WIFI_VENUE = new DynamicField(ScanResult.class, "venueName")
            .setAccessible(true);

    public static final DynamicField FIELD_WIFI_OP_NAME = new DynamicField(ScanResult.class, "operatorFriendlyName")
            .setAccessible(true);

    public static final DynamicField FIELD_WIFI_FLAGS = new DynamicField(ScanResult.class, "flags")
            .setAccessible(true);

    public static final DynamicField FIELD_WIFI_I_FACE = new DynamicField(ScanResult.class, "ifaceName")
            .setAccessible(true);

    public static final DynamicField FIELD_WIFI_INFORMATION_ELEMENTS = new DynamicField(ScanResult.class, "informationElements")
            .setAccessible(true);

    public static ScanResult createEmptyScanResult(String ssid) {
        try {
            Constructor<ScanResult> constructor = ScanResult.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Failed to Create via Constructor, now using Parcel Method...");

            try {
                Object creator = FIELD_CREATOR.tryGetValueStatic();
                Parcel dest = Parcel.obtain();
                dest.writeInt(0);
                dest.writeString(ssid);
                ScanResult empty = ((android.os.Parcelable.Creator<ScanResult>) creator).createFromParcel(dest);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Created Empty Scan Result! Str=" + Str.toStringOrNull(empty));

                return empty;
            }catch (Exception ee) {
                Log.e(TAG, "Failed to Create Empty Wifi Scan Result, Error=" + e);
            }
        }

        return null;
    }

    public static ScanResult fromEx(XWifiNetwork network) {
        if(network == null)
            return null;

        try {
            ScanResult emptyResult = createEmptyScanResult(network.ssid);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Created Empty Wifi ScanResult=" + Str.toStringOrNull(emptyResult));

            //[6.0]     https://android.googlesource.com/platform/frameworks/base/+/android-6.0.1_r25/wifi/java/android/net/wifi/ScanResult.java
            //[7.0]     https://android.googlesource.com/platform/frameworks/base/+/android-7.0.0_r1/wifi/java/android/net/wifi/ScanResult.java
            //[8.0]     https://android.googlesource.com/platform/frameworks/base/+/android-8.0.0_r1/wifi/java/android/net/wifi/ScanResult.java
            //[9.0]     https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r1/wifi/java/android/net/wifi/ScanResult.java
            //[10.0]    https://android.googlesource.com/platform/frameworks/base/+/android-10.0.0_r1/wifi/java/android/net/wifi/ScanResult.java
            //[11.0]    https://android.googlesource.com/platform/frameworks/base/+/android-11.0.0_r1/wifi/java/android/net/wifi/ScanResult.java
            //[12.0]    https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/tags/android-12.0.0_r28/framework/java/android/net/wifi/ScanResult.java
            //[13.0]    https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/tags/android-13.0.0_r28/framework/java/android/net/wifi/ScanResult.java
            //[14.0]    https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/tags/android-14.0.0_r28/framework/java/android/net/wifi/ScanResult.java
            //[15.0]    https://cs.android.com/android/platform/superproject/main/+/main:packages/modules/Wifi/framework/java/android/net/wifi/ScanResult.java

            WifiSsid wifiSsid = WifiSsidCreator.create(network.ssid);
            if(wifiSsid != null)
                FIELD_WIFI_SSID.trySetValueInstanceEx(emptyResult, wifiSsid);

            FIELD_SSID.trySetValueInstanceEx(emptyResult, network.ssid);
            FIELD_BSSID.trySetValueInstanceEx(emptyResult, network.bssid);

            //Android N = Android 7 = SDK 24/25
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FIELD_HESSID.trySetValueInstanceEx(emptyResult, WifiRandom.generateRandomHESSID(true));
                FIELD_ANQP_DOM_ID.trySetValueInstanceEx(emptyResult, WifiRandom.generateRandomANQPDomainId());
            }

            FIELD_CAPABILITIES.trySetValueInstanceEx(emptyResult, WifiRandom.generateRandomCapability());
            FIELD_LEVEL.trySetValueInstanceEx(emptyResult, WifiRandom.generateRandomLevel());   //Level

            int frequency = WifiRandom.generateRandomFrequency();
            FIELD_FREQUENCY.trySetValueInstanceEx(emptyResult, frequency);                      //This

            FIELD_TIMESTAMP.trySetValueInstanceEx(emptyResult, WifiRandom.generateRandomTimestamp());

            int distanceCm = WifiRandom.generateRandomDistanceCm();
            FIELD_DISTANCE_CM.trySetValueInstanceEx(emptyResult, distanceCm);
            FIELD_DISTANCE_SD_CM.trySetValueInstanceEx(emptyResult, WifiRandom.generateRandomDistanceSdCm(distanceCm));

            int channelWidth = WifiRandom.generateRandomChannelWidth();
            FIELD_CHANNEL_WIDTH.trySetValueInstanceEx(emptyResult, channelWidth);

            int[] centerFreq = WifiRandom.generateRandomCenterFrequencies(frequency, channelWidth);
            FIELD_CENTER_FREQ_0.trySetValueInstanceEx(emptyResult, centerFreq[0]);
            FIELD_CENTER_FREQ_1.trySetValueInstanceEx(emptyResult, centerFreq[1]);

            //Android R = Android 11 = SDK = 30
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                FIELD_WIFI_STANDARD.trySetValueInstanceEx(emptyResult, WIFI_STANDARD_UNKNOWN);
            }

            FIELD_WIFI_SEEN.trySetValueInstanceEx(emptyResult, WifiRandom.generateRandomSeen());

            int autoJoinStatus = WifiRandom.generateRandomAutoJoinStatus();
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                //autoJoinStatus
                FIELD_WIFI_AUTO_JOIN_STATS.trySetValueInstanceEx(emptyResult, autoJoinStatus);
            }

            FIELD_WIFI_UNTRUSTED.trySetValueInstanceEx(emptyResult, WifiRandom.generateRandomUntrusted());

            //Android P = Android 9 = SDK 28
            int numConnections = WifiRandom.generateRandomNumConnection();
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                FIELD_WIFI_NUM_CONNECTION.trySetValueInstanceEx(emptyResult, numConnections);
            }

            FIELD_WIFI_NUM_USAGE.trySetValueInstanceEx(emptyResult, WifiRandom.generateRandomNumUsage(numConnections));
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                FIELD_WIFI_NUM_CONFIG_F.trySetValueInstanceEx(emptyResult, WifiRandom.generateRandomNumIpConfigFailures());
            }

            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                FIELD_WIFI_IS_AUTO_J.trySetValueInstanceEx(emptyResult, WifiRandom.generateRandomIsAutoJoinCandidate(autoJoinStatus));
            }


            //        dest.writeString((venueName != null) ? venueName.toString() : "");
            //FIELD_WIFI_VENUE.trySetValueInstanceEx(emptyResult, "");
            //        dest.writeString((operatorFriendlyName != null) ? operatorFriendlyName.toString() : "");
            //FIELD_WIFI_OP_NAME.trySetValueInstanceEx(emptyResult, "");


            FIELD_WIFI_FLAGS.trySetValueInstanceEx(emptyResult, WifiRandom.generateRandomFlags());

            ScanResult.InformationElement[] fakeElements = InformationElementCreator.generateInformationElements(true, true);
            FIELD_WIFI_INFORMATION_ELEMENTS.trySetValueInstanceEx(emptyResult, fakeElements);
            if(DebugUtil.isDebug()) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Log.d(TAG, "Set Fake Elements, Count=" + ListUtil.size(emptyResult.getInformationElements()));
                    }
                }catch (Exception ignored) { }
            }

            //Android S = Android 12 = SDK 31/32
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                FIELD_WIFI_I_FACE.trySetValueInstanceEx(emptyResult, "wlan0");
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Finished Creating Fake ScanResult=" + Str.toStringOrNull(emptyResult));

            return emptyResult;
        }catch (Exception e) {
            Log.e(TAG, "Error Creating Mock Wifi Scan Result, Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return null;
        }
    }

    public static ScanResult from(XWifiNetwork network) {
        if(network == null)
            return null;

        try {
            ScanResult emptyResult = createEmptyScanResult(network.ssid);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Created Empty Wifi ScanResult=" + Str.toStringOrNull(emptyResult));

            Object creator = FIELD_CREATOR.tryGetValueStatic();
            if(creator == null)
                throw new Exception("Failed to Resolve / Get Creator Field!");

            if(!(creator instanceof Parcelable.Creator))
                throw new Exception("Scan Result Creator is Not an Instance of Creator, Type=" + creator.getClass().getName());

            //[6.0]     https://android.googlesource.com/platform/frameworks/base/+/android-6.0.1_r25/wifi/java/android/net/wifi/ScanResult.java
            //[7.0]     https://android.googlesource.com/platform/frameworks/base/+/android-7.0.0_r1/wifi/java/android/net/wifi/ScanResult.java
            //[8.0]     https://android.googlesource.com/platform/frameworks/base/+/android-8.0.0_r1/wifi/java/android/net/wifi/ScanResult.java
            //[9.0]     https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r1/wifi/java/android/net/wifi/ScanResult.java
            //[10.0]    https://android.googlesource.com/platform/frameworks/base/+/android-10.0.0_r1/wifi/java/android/net/wifi/ScanResult.java
            //[11.0]    https://android.googlesource.com/platform/frameworks/base/+/android-11.0.0_r1/wifi/java/android/net/wifi/ScanResult.java
            //[12.0]    https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/tags/android-12.0.0_r28/framework/java/android/net/wifi/ScanResult.java
            //[13.0]    https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/tags/android-13.0.0_r28/framework/java/android/net/wifi/ScanResult.java
            //[14.0]    https://android.googlesource.com/platform/packages/modules/Wifi/+/refs/tags/android-14.0.0_r28/framework/java/android/net/wifi/ScanResult.java
            //[15.0]    https://cs.android.com/android/platform/superproject/main/+/main:packages/modules/Wifi/framework/java/android/net/wifi/ScanResult.java

            Parcel dest = Parcel.obtain();
            WifiSsid wifiSsid = WifiSsidCreator.create(network.ssid);
            //    // TODO(b/231433398): add maxTargetSdk = Build.VERSION_CODES.S
            if(wifiSsid == null) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Wifi SSID Attempted to be Created but Failed! is null...");
            } else {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Created SSID Object, String=" + Str.toStringOrNull(wifiSsid));

                if(WIFI_SSID_WRITE_TO_PARCEL.isValid()) {
                    dest.writeInt(1);
                    WIFI_SSID_WRITE_TO_PARCEL.tryInstanceInvokeEx(wifiSsid, dest, 0);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "WifiSsid [writeToParcel] was written, size=" + dest.dataSize() + " Data Position=" + dest.dataPosition());

                } else {
                    dest.writeInt(0);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "WifiSsid [writeToParcel] is not valid ? " + wifiSsid.getClass().getName());
                }
            }

            //Cache all of this
            //Android 13+ Deprecates the SSID Field, should we write empty null ?
            dest.writeString(network.ssid);                                                             //SSID
            dest.writeString(network.bssid);                                                            //

            //ScanResult emptyParcel = ((android.os.Parcelable.Creator<ScanResult>) creator).createFromParcel(dest);


            //Android N = Android 7 = SDK 24/25
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dest.writeLong(WifiRandom.generateRandomHESSID(true));                  //7.0+ HESSID
                dest.writeInt(WifiRandom.generateRandomANQPDomainId());                                 //7.0+ ANQ P Domain ID
            }

            dest.writeString(WifiRandom.generateRandomCapability());                                    //Capabilities
            network.writeSignalLevel(dest, WifiRandom.generateRandomLevel());                           //Level

            int frequency = WifiRandom.generateRandomFrequency();
            network.writeFrequency(dest, frequency);                                                    //Frequency

            dest.writeLong(WifiRandom.generateRandomTimestamp());                                       //TimeStamp

            int distanceCm = WifiRandom.generateRandomDistanceCm();
            dest.writeInt(distanceCm);                                                                  //Distance CM
            dest.writeInt(WifiRandom.generateRandomDistanceSdCm(distanceCm));                           //Distance SD CM

            int channelWidth = WifiRandom.generateRandomChannelWidth();
            dest.writeInt(channelWidth);                                                                //Channel Width

            int[] centerFreq = WifiRandom.generateRandomCenterFrequencies(frequency, channelWidth);
            dest.writeInt(centerFreq[0]);                                                               //Center Freq 0
            dest.writeInt(centerFreq[1]);                                                               //Center Freq 1

            //Android R = Android 11 = SDK = 30
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                dest.writeInt(1);                                                                   //mWifiStandard
            }

            dest.writeLong(WifiRandom.generateRandomSeen());                                            //Seen

            int autoJoinStatus = WifiRandom.generateRandomAutoJoinStatus();
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                dest.writeInt(autoJoinStatus);                                                              //Auto Join Status
            }

            dest.writeInt(WifiRandom.generateRandomUntrusted());                                        //Is Untrusted ?

            //Android P = Android 9 = SDK 28
            int numConnections = WifiRandom.generateRandomNumConnection();
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                dest.writeInt(numConnections);                                                              //Number Connections
            }

            dest.writeInt(WifiRandom.generateRandomNumUsage(numConnections));                           //Number Usage

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                dest.writeInt(WifiRandom.generateRandomNumIpConfigFailures());                              //Number Ip Config Failure
            }

            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                dest.writeInt(WifiRandom.generateRandomIsAutoJoinCandidate(autoJoinStatus));                //Is Auto Join Candidate
            }

            dest.writeString("");                                                                   //Venue
            dest.writeString("");                                                                   //Friendly Name
            dest.writeLong(WifiRandom.generateRandomFlags());                                           //Flags

            //Android 12 they use a function instead inline
            dest.writeInt(0);                                                                       //Information Elements

            //Android N = Android 7 = SDK 24/25
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dest.writeInt(0);           //ANQP LINES
                dest.writeInt(0);           //ANQP Elements
            }

            //Android P = Android 9 = SDK 28 || Android Q = Android 10 = SDK 29
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.P || Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                dest.writeInt(0);                   //Is Carrier AP
                dest.writeInt(0);                   //Carrier AP EAP Type
                dest.writeString("");               //Carrier Name
            }

            //Android P = Android 9 = SDK 28
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                dest.writeInt(0);                   //Radio Chain Infos
            }

            //Android S = Android 12 = SDK 31/32
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dest.writeString("wlan0");          //Interface Name
            }

            //Android T = Android 13 = SDK 33
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                //MLO Info ?
                dest.writeInt(0);       //Write 0, its the object array or some shit (dest.writeParcelable(mApMldMacAddress, flags)
                dest.writeInt(0);       //dest.writeInt(mApMloLinkId);
                dest.writeInt(0);       //dest.writeTypedList(mAffiliatedMloLinks);
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Finished Mocking Network SSID=" + network.ssid + " BSSID=" + network.bssid + " Now Invoking Creator! Parcel Data Size=" + dest.dataSize() + " Data Position=" + dest.dataPosition());


            ScanResult copy = ((android.os.Parcelable.Creator<ScanResult>) creator).createFromParcel(dest);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Created Mock Copy Scan Result: String=" + Str.toStringOrNull(copy));



            //If Copy has NULL elements we can still force set

            return copy;
        }catch (Exception e) {
            Log.e(TAG, "Error Creating Mock Wifi Scan Result, Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            return null;
        }
    }
}
