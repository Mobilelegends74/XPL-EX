package eu.faircode.xlua.x.ui.dialogs.wifi;


import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class XWifiUtils {
    private static final String TAG = "XLua.XWifiUtils";

    /**
     * Converts a Base64-encoded string to a list of XWifiNetwork objects.
     * The Base64 string should decode to a JSON array of XWifiNetwork objects.
     *
     * @param base64String The Base64-encoded JSON array string
     * @return A list of XWifiNetwork objects, or an empty list if parsing fails
     */
    public static List<XWifiNetwork> fromBase64String(String base64String) {
        List<XWifiNetwork> networks = new ArrayList<>();

        if (base64String == null || base64String.isEmpty()) {
            Log.w(TAG, "Empty or null Base64 string provided");
            return networks;
        }

        try {
            // Decode Base64 string to byte array
            byte[] bytes = Base64.decode(base64String, Base64.DEFAULT);

            // Convert bytes to string
            String jsonString = new String(bytes, StandardCharsets.UTF_8);

            // Parse JSON array
            JSONArray jsonArray = new JSONArray(jsonString);

            // Convert each JSON object to XWifiNetwork
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                XWifiNetwork network = XWifiNetwork.fromJson(jsonObject);
                networks.add(network);
            }

            Log.d(TAG, "Successfully parsed " + networks.size() + " networks from Base64 string");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid Base64 encoding: " + e.getMessage());
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
        }

        return networks;
    }

    /**
     * Converts a list of XWifiNetwork objects to a Base64-encoded JSON array string.
     *
     * @param networks The list of XWifiNetwork objects to encode
     * @return A Base64-encoded string representation of the networks list
     */
    public static String toBase64String(List<XWifiNetwork> networks) {
        if (networks == null) {
            Log.w(TAG, "Null networks list provided");
            return "";
        }

        try {
            // Create JSON array
            JSONArray jsonArray = new JSONArray();

            // Add each network as a JSON object
            for (XWifiNetwork network : networks) {
                jsonArray.put(network.toJson());
            }

            // Convert JSON array to string
            String jsonString = jsonArray.toString();

            // Encode to Base64
            byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
            String base64String = Base64.encodeToString(bytes, Base64.DEFAULT);

            Log.d(TAG, "Successfully encoded " + networks.size() + " networks to Base64 string");

            return base64String;
        } catch (Exception e) {
            Log.e(TAG, "Error encoding networks to Base64: " + e.getMessage());
            return "";
        }
    }
}