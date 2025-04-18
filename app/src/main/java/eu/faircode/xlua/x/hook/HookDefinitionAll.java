package eu.faircode.xlua.x.hook;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;

public class HookDefinitionAll extends HookDefinition {
    private String methodName = null;
    @Override
    protected String getName() { return
            !Str.isEmpty(methodName) ? methodName : blob != null ? blob.methodName : ""; }

    public HookDefinitionAll(Class<?> resolvedClazz, XHook blob) {
        this.blob = blob;
        this.resolvedClazz = resolvedClazz;
    }

    public HookDefinitionAll(Class<?> resolvedClazz, XHook blob, String methodName) {
        this.blob = blob;
        this.resolvedClazz = resolvedClazz;
        this.methodName = methodName;
    }
}
