package eu.faircode.xlua.x.hook.filter.kinds;

import java.util.List;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.data.TypeMap;
import eu.faircode.xlua.x.hook.filter.FilterContainerElement;
import eu.faircode.xlua.x.hook.filter.IFilterContainer;
import eu.faircode.xlua.x.hook.interceptors.ipc.InterfacesGlobal;
import eu.faircode.xlua.x.hook.interceptors.ipc.bases.IBinderInterceptor;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;

public class IPCBinderFilterContainer extends FilterContainerElement implements IFilterContainer {
    public static IFilterContainer create() { return new IPCBinderFilterContainer(); }

    public static final String GROUP_NAME = "Intercept.IPC.Interface";
    public static final TypeMap DEFINITIONS =
            TypeMap.create()
            .add("android.os.Binder", "transact", "onTransact", "execTransact", "execTransactInternal")
            .add("android.os.BinderProxy", "transact", "onTransact", "execTransact", "execTransactInternal");

    public IPCBinderFilterContainer() { super(GROUP_NAME, DEFINITIONS); }

    @Override
    public boolean hasSwallowedAsRule(XHook hook) {
        boolean isRule = super.hasSwallowedAsRule(hook);
        if(isRule) {
            String method = hook.methodName.trim();
            for(IBinderInterceptor interceptor : InterfacesGlobal.INTERCEPTORS) {
                if(interceptor.getInterfaceName().equalsIgnoreCase(method)) {
                    createdSettings.put(interceptor.getSettingName(), "true");
                    break;
                }
            }
        }
        return isRule;
    }
}