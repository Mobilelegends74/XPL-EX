package eu.faircode.xlua.x.hook.interceptors.network.utils;

import android.net.wifi.WifiSsid;
import android.os.Build;
import android.util.Log;

import java.nio.charset.StandardCharsets;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.runtime.HiddenApi;
import eu.faircode.xlua.x.runtime.reflect.DynamicMethod;
import eu.faircode.xlua.x.xlua.LibUtil;

public class WifiSsidCreator {
    private static final String TAG = LibUtil.generateTag(WifiSsidCreator.class);

    public static final String CLASS_WIFI_INFO = "android.net.wifi.WifiSsid";

    static {
        HiddenApi.bypassHiddenApiRestrictions();
    }


    public static DynamicMethod METHOD_FROM_BYTES = new DynamicMethod(CLASS_WIFI_INFO, "fromBytes", byte[].class)
            .setHiddenApis()
            .setAccessible(true);

    public static DynamicMethod METHOD_FROM_UTF8 = new DynamicMethod(CLASS_WIFI_INFO, "fromUtf8Text", CharSequence.class)
            .setHiddenApis()
            .setAccessible(true);

    public static DynamicMethod METHOD_FROM_STRING = new DynamicMethod(CLASS_WIFI_INFO, "fromString", String.class)
            .setHiddenApis()
            .setAccessible(true);

    public static DynamicMethod METHOD_FROM_ASCII = new DynamicMethod(CLASS_WIFI_INFO, "createFromAsciiEncoded", String.class)
            .setHiddenApis()
            .setAccessible(true);


    public static WifiSsid fromBytes(byte[] bytes) { return METHOD_FROM_BYTES.tryStaticInvoke(bytes); }
    public static WifiSsid fromUtf8Text(CharSequence utf8Text) { return METHOD_FROM_UTF8.tryStaticInvoke(utf8Text); }
    public static WifiSsid fromString(String string) { return METHOD_FROM_STRING.tryStaticInvoke(string); }

    //// TODO(b/231433398): add maxTargetSdk = Build.VERSION_CODES.S
    public static WifiSsid  createFromAsciiEncoded(String asciiEncoded) { return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? null : METHOD_FROM_ASCII.tryStaticInvoke(asciiEncoded); }

    public static WifiSsid create(String ssid) {
        WifiSsid obj = internalCreate(ssid);
        if(DebugUtil.isDebug())
            Log.d(TAG, "WifiSsid Object from [" + ssid + "] ToString=" + Str.toStringOrNull(obj));

        return obj;
    }

    private static WifiSsid internalCreate(String ssid) {
        try {
            WifiSsid inst = fromString(ssid);
            if(isGoodWifiSsid(inst, ssid))
                return inst;

            inst = fromUtf8Text(ssid);
            if(isGoodWifiSsid(inst, ssid))
                return inst;

            inst = createFromAsciiEncoded(ssid);
            if(isGoodWifiSsid(inst, ssid))
                return inst;

            byte[] bytes = ssid.getBytes(StandardCharsets.UTF_8);
            inst = fromBytes(bytes);
            return inst;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Create WifiSsid Object! Error=" + e);
            return null;
        }
    }

    public static boolean isGoodWifiSsid(WifiSsid inst, String ssid) {
        if(inst == null)
            return false;

        boolean res = inst.toString().toLowerCase().contains(ssid.toLowerCase());
        if(DebugUtil.isDebug())
            Log.d(TAG, "Comparing SSID [" + ssid + "] Object=" + Str.toStringOrNull(inst) + " Result=" + String.valueOf(res));

        return res;
    }
}
