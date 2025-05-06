package eu.faircode.xlua.x.hook.interceptors.cell;

import android.telecom.PhoneAccountHandle;

import java.util.ArrayList;

import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.xlua.LibUtil;

public class PhoneAccountInterceptor {
    private static final String TAG = LibUtil.generateTag(PhoneAccountInterceptor.class);

    public static boolean interceptObject(XParam param, boolean isResult) {
        try {
            PhoneIdMap.init(param);
            Object res = isResult ?
                    param.getResult() :
                    param.getThis();

            //if(!(res instanceof PhoneAccountHandle))
            //    return

            //for(String id : new ArrayList<>(PhoneIdMap.ID_MAP.keySet())) {
            //    if(id.endsWith())
            //}

        }catch (Throwable e) {
            return false;
        }

        return false;
    }
}
