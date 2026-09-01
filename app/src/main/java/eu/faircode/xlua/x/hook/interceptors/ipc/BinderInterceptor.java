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
        //data.enforceInterface(this.getInterfaceDescriptor());
        InterfaceBinderData helper = InterfaceBinderData.create(param, getResult);
        if(!helper.hasInterfaceName()) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "IPC call has no interface descriptor; skipping");

            return false;
        }

        if(!getResult) {
            if(!helper.hasData()) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "IPC data parcel is unavailable; skipping " + helper.interfaceName);
                return false;
            }

            if(!InterfacesGlobal.APPSET_INTERFACE.equalsIgnoreCase(helper.interfaceName)) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "IPC interface is not handled by the before interceptor: " + helper.interfaceName);
                return false;
            }
        } else {
            if(InterfacesGlobal.APPSET_INTERFACE.equalsIgnoreCase(helper.interfaceName)) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "IPC interface is not handled by the after interceptor: " + helper.interfaceName);
                return false;
            }

            if(!helper.hasReply()) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "IPC reply parcel is unavailable; skipping " + helper.interfaceName);
                return false;
            }
        }

        //
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
