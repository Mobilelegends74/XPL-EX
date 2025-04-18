package eu.faircode.xlua.x.hook.interceptors.network.utils;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiSsid;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.utilities.ReflectUtilEx;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.runtime.HiddenApi;
import eu.faircode.xlua.x.runtime.reflect.DynamicMethod;
import eu.faircode.xlua.x.runtime.reflect.ReflectUtil;
import eu.faircode.xlua.x.xlua.LibUtil;

public class WifiSsidCreator {
    private static final String TAG = LibUtil.generateTag(WifiSsidCreator.class);

    public static  String CLASS_WIFI_INFO;
    static {
        HiddenApi.bypassHiddenApiRestrictions();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            CLASS_WIFI_INFO = WifiSsid.class.getName();
        } else {
            CLASS_WIFI_INFO = "android.net.wifi.WifiInfo";
        }
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

    public static <T> T fromConstructor(byte[] bytes) { return ReflectUtil.tryCreateNewInstanceWild(CLASS_WIFI_INFO, bytes); }


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
            if(ssid == null) {
                return null;//Return a Default Value like <unknown ssid>
            }

            byte[] bytes = ssidToBytes(ssid);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("WifiSsid (%s) was Converted to Bytes = (%s)", ssid, Str.bytesToHexString(bytes, true)));

            if(ArrayUtils.isValid(bytes)) {
               WifiSsid fBytes = fromConstructor(bytes);
               if(isGoodWifiSsid(fBytes, ssid)) {
                   return fBytes;
               } else {
                   fBytes = fromBytes(bytes);
                   if(isGoodWifiSsid(fBytes, ssid))
                       return fBytes;
               }
            }

            WifiSsid inst = fromString(ssid);
            if(isGoodWifiSsid(inst, ssid))
                return inst;

            inst = fromUtf8Text((CharSequence) ssid);
            if(isGoodWifiSsid(inst, ssid))
                return inst;

            return createFromAsciiEncoded(ssid);
        }catch (Exception e) {
            Log.e(TAG, "Failed to Create WifiSsid Object! Error=" + e);
            return null;
        }
    }



    /**
     * Converts an SSID string to a byte array that can be accepted by WifiSsid.fromBytes()
     *
     * @param ssid The SSID string to convert (can be plain text, quoted, or hex format)
     * @return byte array representation of the SSID
     */
    public static byte[] ssidToBytes(String ssid) {
        if (ssid == null) {
            return new byte[0];
        }

        // If the SSID is in quoted format, remove the quotes and convert to UTF-8 bytes
        if (ssid.length() > 1 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
            String unquotedSsid = ssid.substring(1, ssid.length() - 1);
            return unquotedSsid.getBytes(StandardCharsets.UTF_8);
        }

        // If the SSID is a valid hex string, convert it to bytes
        if (isValidHexString(ssid)) {
            return hexStringToByteArray(ssid);
        }

        // Otherwise treat as plain UTF-8 text
        return ssid.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Converts a hex string to a byte array
     *
     * @param s Hex string to convert (must have even length)
     * @return Byte array representation of the hex string
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    /**
     * Checks if a string is a valid hexadecimal string (even length, hex chars only)
     */
    private static boolean isValidHexString(String s) {
        if (s == null || s.isEmpty() || s.length() % 2 != 0) {
            return false;
        }

        for (char c : s.toCharArray()) {
            if (!isHexDigit(c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if a character is a valid hexadecimal digit
     */
    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'f') ||
                (c >= 'A' && c <= 'F');
    }


    public static boolean isGoodWifiSsid(WifiSsid inst, String ssid) {
        if(inst == null || ssid == null)
            return false;

        boolean res = Str.toLowerCase(Str.toStringOrNull(inst)).contains(Str.toLowerCase(ssid));
        if(DebugUtil.isDebug())
            Log.d(TAG, "Comparing SSID [" + ssid + "] Object=" + Str.toStringOrNull(inst) + " Result=" + String.valueOf(res));

        return res;
    }
}
