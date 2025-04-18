package eu.faircode.xlua.x.hook;

import java.lang.reflect.Field;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;

public class HookDefinitionField extends HookDefinition {
    public Field field;

    @Override
    protected String getName() { return field != null ? field.getName() : ""; }

    @Override
    protected void setAccessible(boolean a) { try { field.setAccessible(a); }catch (Exception ignored) { } }

    public HookDefinitionField(Field field, Class<?> resolvedClazz, XHook blob) {
        this.blob = blob;
        this.field = field;
        this.resolvedClazz = resolvedClazz;
    }
}
