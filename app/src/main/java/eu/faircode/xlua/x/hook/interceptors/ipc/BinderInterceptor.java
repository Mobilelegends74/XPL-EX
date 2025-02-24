package eu.faircode.xlua.x.hook.interceptors.ipc;

import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.utilities.ParcelUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.hook.interceptors.ipc.bases.IBinderInterceptor;
import eu.faircode.xlua.x.hook.interceptors.ipc.holders.InterfaceBinderData;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

public class BinderInterceptor {
    private static final String TAG = LibUtil.generateTag(BinderInterceptor.class);

    //android.adservices.appsetid.AppSetId;
    //com.google.android.gms.appset.service.AppSetIdProviderService
    //com.google.android.gms.appset.internal.IAppSetService
    //com.google.android.gms.appset.internal.IAppSetIdCallback
    //com.google.android.gms.appset.service.START
    //com.google.android.gms.appset.internal.IAppSetIdCallback
    //com.google.android.gms.appset.internal.IAppSetService

    public static boolean intercept(XParam param, boolean getResult) {
        InterfaceBinderData helper = InterfaceBinderData.create(param, getResult);
        if(!helper.hasInterfaceName()) {
            if(DebugUtil.isDebug())
                Log.w(TAG, "Interface Name is NULL for IPC Call Returning...");

            return false;
        }

        if(!helper.hasReply()) {
            //Log.w(TAG, helper.data.)
            if(DebugUtil.isDebug()) {
                Log.w(TAG, "Reply Parcel for IPC Call is Not Valid Size=" + helper.getReplySize() + " Data Size=" + helper.getDataSize() +  " Name=" + helper.interfaceName + " Code=" + helper.code +  " Flags=" + helper.flags);
                //Log.w(TAG, "Reply Parcel for IPC Call is Not Valid Size=" + helper.getReplySize() + " Data Size=" + helper.getDataSize() +  " Name=" + helper.interfaceName + " Code=" + helper.code +  " Flags=" + helper.flags + " Stack=" + Str.ensureNoDoubleNewLines(RuntimeUtils.getStackTraceSafeString(new Exception())));
                //Log.w(TAG, "R[" + helper.interfaceName + "] Reply=" + ParcelUtil.parcelToHexStringEx(helper.reply) + "   DATA=" + ParcelUtil.parcelToHexStringEx(helper.data));
            }
            return false;
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Checking Interface IPC Call => " + helper.interfaceName);

        for(IBinderInterceptor i : InterfacesGlobal.INTERCEPTORS) {
            if(helper.isInterfaceName(i.getInterfaceName())) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Found Interface => " + i.getInterfaceName());

                return i.intercept(param, helper);
            }
        }

        return false;
    }
}
