package eu.faircode.xlua.x.ui.dialogs.wifi;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiSsid;
import android.os.Parcel;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.hook.interceptors.network.WifiScanFilter;
import eu.faircode.xlua.x.xlua.LibUtil;

public class XWifiNetwork {
    private static final String TAG = LibUtil.generateTag(XWifiNetwork.class);

    public static final int RANDOM_INT = -1337;

    public String ssid;
    public String bssid;
    public int signal = -3; // -3 indicates random
    public int frequency = -3; // -3 indicates random
    public boolean forceShow = false;

    public boolean hasSsid() { return !Str.isEmpty(ssid); }
    public boolean hasBssid() { return !Str.isEmpty(bssid); }

    public XWifiNetwork() {
        // Default constructor
    }

    public XWifiNetwork(String ssid, String bssid, int signal, int frequency, boolean forceShow) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.signal = signal;
        this.frequency = frequency;
        this.forceShow = forceShow;
    }

    public void writeFrequency(Parcel dest, int defaultValue) {
        dest.writeInt(defaultValue != RANDOM_INT ? frequency : defaultValue);
    }

    public void writeSignalLevel(Parcel dest, int defaultValue) {
        dest.writeInt(defaultValue != RANDOM_INT ? signal : defaultValue);
    }

    // Create from JSON
    public static XWifiNetwork fromJson(JSONObject json) {
        XWifiNetwork network = new XWifiNetwork();
        try {
            if (json.has("ssid")) network.ssid = json.getString("ssid");
            if (json.has("bssid")) network.bssid = json.getString("bssid");
            if (json.has("signal")) network.signal = json.getInt("signal");
            if (json.has("frequency")) network.frequency = json.getInt("frequency");
            network.forceShow = json.optBoolean("forceShow");
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
        }
        return network;
    }

    // Convert to JSON
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("ssid", ssid);
            json.put("bssid", bssid);
            json.put("signal", signal);
            json.put("frequency", frequency);
            json.put("forceShow", forceShow);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON: " + e.getMessage());
        }
        return json;
    }

    @Override
    public String toString() {
        return ssid + " (" + bssid + ")";
    }

    public boolean isRandomSignal() {
        return signal == -3;
    }

    public boolean isRandomFrequency() {
        return frequency == -3;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof String) {
            String v = (String) obj;
            return (hasSsid() && v.equalsIgnoreCase(ssid)) || (hasBssid() && v.equalsIgnoreCase(bssid));
        }

        if(obj instanceof XWifiNetwork) {
            XWifiNetwork v = (XWifiNetwork) obj;
            return (hasSsid() == v.hasSsid() && hasBssid() == v.hasBssid()) && (!hasBssid() || bssid.equalsIgnoreCase(v.bssid) && (!hasSsid() || ssid.equalsIgnoreCase(v.ssid)));
        }

        if(obj instanceof ScanResult) {
            ScanResult v = (ScanResult) obj;
            String ssid = WifiScanFilter.getSSID(v);
            String bssid = v.BSSID;
            boolean vHasSsid = !Str.isEmpty(ssid);
            boolean vHasBssid = !Str.isEmpty(bssid);

            if(!vHasSsid && vHasBssid && hasBssid()) return bssid.equalsIgnoreCase(this.bssid);
            if(!hasBssid() && hasSsid() && vHasSsid) return ssid.equalsIgnoreCase(this.ssid);
            return (hasSsid() == vHasSsid && hasBssid() == vHasBssid) && (!hasBssid() || this.bssid.equalsIgnoreCase(bssid) && (!hasSsid() || this.ssid.equalsIgnoreCase(ssid)));
        }

        return false;
    }
}
