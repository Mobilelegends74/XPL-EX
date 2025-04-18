package eu.faircode.xlua.x.hook.interceptors.network;

import android.net.NetworkInfo;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.xlua.LibUtil;

public class NetworkInfoInterceptor {
    private static final String TAG = LibUtil.generateTag(NetworkInfoInterceptor.class);

    private static final int TYPE_VPN = 0x11;

    public static final DynamicField FIELD_STATE = new DynamicField(NetworkInfo.class, "mState")
            .setAccessible(true);

    public static boolean intercept(XParam param, boolean getResult) {
        if(param == null) return false;
        try {
            Object obj = getResult ? param.getResult() : param.getThis();
            if(!(obj instanceof NetworkInfo))
                return false;

            NetworkInfo instance = (NetworkInfo) obj;
            if(instance.getType() != TYPE_VPN)
                return false;

            //using "getState" will invoke our Spoof Hook, so use Direct Reflection
            NetworkInfo.State state = FIELD_STATE.tryGetValueInstanceEx(instance, instance.getState());
            if(DebugUtil.isDebug())
                Log.d(TAG, "Found VPN Interface: State=" + state + " String=" + Str.toStringOrNull(instance));


            //Have ability to say its connected to main, for the future
            /*
                Wi-Fi: wlan0
                Mobile Data (Cellular): rmnet0, rmnet_data0, ccmni0, usb0, rndis0
                Ethernet: eth0
                VPN: tun0, ppp0
             */
            param.setLogOld(String.valueOf(instance.getState()));
            if(!FIELD_STATE.trySetValueInstanceEx(instance, NetworkInfo.State.DISCONNECTED)) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Failed to Forcefully Set the NetworkInfo State for a VPN Interface to :" + String.valueOf(NetworkInfo.State.DISCONNECTED) + " ...");

                return false;
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, "Successfully Set the VPN NetworkInfo State to Disconnected!");

            param.setLogNew(String.valueOf(NetworkInfo.State.DISCONNECTED));
            if(getResult) param.setResult(instance);
            return true;
        }catch (Throwable e) {
            Log.e(TAG, "Failed to Intercept NetworkInfo for VPN Connections! Error=" + e);
            return false;
        }
    }
}
