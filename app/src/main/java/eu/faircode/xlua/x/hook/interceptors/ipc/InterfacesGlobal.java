package eu.faircode.xlua.x.hook.interceptors.ipc;

import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.x.hook.interceptors.ipc.bases.BinderInterceptorBase;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class InterfacesGlobal {
    public static final String SAMSUNG_INTERFACE = "com.samsung.android.deviceidservice.IDeviceIdService";
    public static final String SAMSUNG_INTERFACE_SETTING = "intercept.ipc.interface.samsung";

    public static final String LENOVO_INTERFACE = "com.zui.deviceidservice.IDeviceidInterface";
    public static final String LENOVO_INTERFACE_SETTING = "intercept.ipc.interface.lenovo";

    public static final String ASUS_INTERFACE = "com.asus.msa.SupplementaryDID.IDidAidlInterface";
    public static final String ASUS_INTERFACE_SETTING = "intercept.ipc.interface.asus";

    public static final String GOOGLE_INTERFACE = "com.google.android.gms.ads.identifier.internal.IAdvertisingIdService";
    public static final String GOOGLE_INTERFACE_SETTING = "intercept.ipc.interface.google";


    public static final String APPSET_INTERFACE = "com.google.android.gms.appset.internal.IAppSetIdCallback";
    public static final String APPSET_INTERFACE_SETTING = "intercept.ipc.interface.google.appset";

    public static final List<String> ID_INTERFACES = Arrays.asList(
            SAMSUNG_INTERFACE,
            LENOVO_INTERFACE,
            ASUS_INTERFACE,
            GOOGLE_INTERFACE);

    public static final List<BinderInterceptorBase> INTERCEPTORS = Arrays.asList(
            BinderInterceptorBase.create(APPSET_INTERFACE, APPSET_INTERFACE_SETTING, RandomizersCache.SETTING_UNIQUE_GOOGLE_APP_SET_ID),
            BinderInterceptorBase.create(SAMSUNG_INTERFACE, SAMSUNG_INTERFACE_SETTING, RandomizersCache.SETTING_UNIQUE_UUID),
            BinderInterceptorBase.create(LENOVO_INTERFACE, LENOVO_INTERFACE_SETTING, RandomizersCache.SETTING_UNIQUE_UUID),
            BinderInterceptorBase.create(ASUS_INTERFACE, ASUS_INTERFACE_SETTING, RandomizersCache.SETTING_UNIQUE_UUID),
            BinderInterceptorBase.create(GOOGLE_INTERFACE, GOOGLE_INTERFACE_SETTING, RandomizersCache.SETTING_UNIQUE_GOOGLE_ID)
    );
}
