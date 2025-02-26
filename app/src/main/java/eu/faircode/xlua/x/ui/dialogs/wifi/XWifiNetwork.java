package eu.faircode.xlua.x.ui.dialogs.wifi;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import eu.faircode.xlua.x.xlua.LibUtil;

public class XWifiNetwork {
    private static final String TAG = LibUtil.generateTag(XWifiNetwork.class);

    public String ssid;
    public String bssid;
    public int signal = -3; // -3 indicates random
    public int frequency = -3; // -3 indicates random

    public XWifiNetwork() {
        // Default constructor
    }

    public XWifiNetwork(String ssid, String bssid, int signal, int frequency) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.signal = signal;
        this.frequency = frequency;
    }

    // Create from JSON
    public static XWifiNetwork fromJson(JSONObject json) {
        XWifiNetwork network = new XWifiNetwork();
        try {
            if (json.has("ssid")) network.ssid = json.getString("ssid");
            if (json.has("bssid")) network.bssid = json.getString("bssid");
            if (json.has("signal")) network.signal = json.getInt("signal");
            if (json.has("frequency")) network.frequency = json.getInt("frequency");
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
}
