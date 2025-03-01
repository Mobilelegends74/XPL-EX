package eu.faircode.xlua.x.hook.interceptors.network;

import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiSsid;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.runtime.reflect.DynamicMethod;
import eu.faircode.xlua.x.ui.dialogs.wifi.XWifiNetwork;
import eu.faircode.xlua.x.ui.dialogs.wifi.XWifiUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class WifiScanFilter {
    private static final String TAG = LibUtil.generateTag(WifiScanFilter.class);

    private static final HashMap<String, XWifiNetwork> CACHED_NAMES = new HashMap<>();
    private static final HashMap<String, XWifiNetwork> CACHED_BSSID = new HashMap<>();
    private static final List<XWifiNetwork> CACHE_FORCE_SHOW = new ArrayList<>();

    //   private MacAddress mApMldMacAddress;

    public static final DynamicMethod METHOD_SSID = new DynamicMethod(ScanResult.class, "getWifiSsid")
            .setAccessible(true);

    public static final DynamicField FIELD_SSID = new DynamicField(NetworkInfo.class, "SSID")
            .setAccessible(true);

    public static String getSSID(ScanResult result) {
        if(result == null)
            return null;

        try {
            String res = FIELD_SSID.tryGetValueInstanceEx(result);
            if(Str.isEmpty(res)) {
                WifiSsid wifiSsid = METHOD_SSID.tryInstanceInvokeEx(result);
                if(wifiSsid == null)
                    return null;

                return wifiSsid.toString();
            }
        }catch (Exception e) {
            Log.e(TAG, "Failed to Resolve and Get SSID, Error=" + e + " String=" + Str.toStringOrNull(result));
        }

        return null;
    }


    public static boolean filter(XParam param) {
        init(param);
        List<ScanResult> filtered = new ArrayList<>();
        try {
            Object res = param.getResult();
            if(!ListUtil.isListType(res))
                throw new Exception("Type is not of List for Wifi Scan Results!");

            List<ScanResult> results = (List<ScanResult>) res;
            if(DebugUtil.isDebug())
                Log.d(TAG, "Filtering Wifi Scan Result, Size=" + ListUtil.size(results));

            if(ListUtil.isValid(results)) {
               for(ScanResult result : results) {
                   XWifiNetwork net = getMockNetworkForResult(result);
                   if(net != null) {
                       if(net.equals(result)) {
                           filtered.add(result);
                           if(DebugUtil.isDebug())
                               Log.d(TAG, "Wifi Scan Result is Allowed=" + Str.toString(result));
                       }
                   }
               }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Finished Filtering Results, Result Size=" + ListUtil.size(filtered) + " Now Ensuring Force Show Networks are Shown! Force Show Count=" + ListUtil.size(CACHE_FORCE_SHOW));

            if(ListUtil.isValid(CACHE_FORCE_SHOW)) {
                for(XWifiNetwork forceShow : CACHE_FORCE_SHOW) {
                    boolean isShown = false;
                    for(ScanResult result : filtered) {
                        if(forceShow.equals(result)) {
                            isShown = true;
                            break;
                        }
                    }

                    if(!isShown) {
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "Creating MOCK Network: " + Str.toStringOrNull(forceShow));

                        ScanResult mockResult = MockWifiResult.fromEx(forceShow);
                        if(mockResult != null) {
                            filtered.add(mockResult);
                            if(DebugUtil.isDebug())
                                Log.d(TAG, "Created MOCK Wifi Scan Result, =" + Str.toStringOrNull(mockResult));
                        }
                    }
                }
            }

            param.setResult(filtered);
            return true;
        }catch (Throwable e) {
            Log.e(TAG, "Failed to Intercept and Filter Wifi Scan Results! Error=" + e);
            return false;
        }
    }

    private static XWifiNetwork getMockNetworkForResult(ScanResult result) {
        if(result == null) return null;
        return !Str.isEmpty(result.BSSID) && CACHED_BSSID.containsKey(result.BSSID) ? CACHED_BSSID.get(result.BSSID) : CACHED_NAMES.get(getSSID(result));
    }

    private static void init(XParam param) {
        if(param == null)
            return;

        if(CACHED_NAMES.isEmpty() && CACHED_BSSID.isEmpty()) {
            try {
                String value = param.getSetting(RandomizersCache.SETTING_NET_ALLOWED_LIST);
                List<XWifiNetwork> networks = XWifiUtils.fromBase64String(value);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Initializing Wifi Scan Results Cache! Networks from JSON Count=" + ListUtil.size(networks));

                if(ListUtil.isValid(networks)) {
                    for(XWifiNetwork network : networks) {
                        if(network.hasSsid())
                            CACHED_NAMES.put(network.ssid, network);

                        if(network.hasBssid())
                            CACHED_BSSID.put(network.bssid, network);

                        if(network.forceShow)
                            CACHE_FORCE_SHOW.add(network);
                    }
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Finished Initializing Wifi Scan Results Cache! From %s Networks, Cached SSIDs Count=%s Cached BSSIDs Count=%s Force Show Count=%s",
                            ListUtil.size(networks),
                            ListUtil.size(CACHED_NAMES),
                            ListUtil.size(CACHED_BSSID),
                            ListUtil.size(CACHE_FORCE_SHOW)));
            } catch (Exception e) {
                Log.e(TAG, "Failed to Init Wifi Scan Results Cache! Error=" + e);
            }
        }
    }

    private static void clearCache() {
        CACHED_NAMES.clear();
        CACHED_BSSID.clear();
        CACHE_FORCE_SHOW.clear();
    }
}
