package eu.faircode.xlua.x.hook;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.utilities.ReflectUtilEx;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;

public class HookDefinitionMember extends HookDefinition {
    //Make constructor version
    public Method method;
    public Constructor<?> constructor;

    public boolean isMethod() { return method != null; }
    public boolean isConstructor() { return constructor != null; }

    public static HookDefinitionMember fromMethod(Member m, XHook blob, Class<?> resolvedClazz) {
        HookDefinitionMember def = new HookDefinitionMember();
        def.member = m;
        def.blob = blob;
        def.resolvedClazz = resolvedClazz;
        def.method = (Method) m;
        return def;
    }

    public static HookDefinitionMember fromConstructor(Member m, XHook blob, Class<?> resolvedClazz) {
        HookDefinitionMember def = new HookDefinitionMember();
        def.member = m;
        def.blob = blob;
        def.resolvedClazz = resolvedClazz;
        def.constructor = (Constructor<?>) m;
        return def;
    }

    //? or
    @Override
    protected String getName() {
        if(method != null) return method.getName();
        if(constructor != null) return constructor.getName();
        if(member != null) return member.getName();
        return Str.EMPTY;
    }

    @Override
    protected void setAccessible(boolean a) {
        try {
            if(method != null) method.setAccessible(a);
            if(constructor != null) constructor.setAccessible(a);
        }catch (Exception ignored) { }
    }

    public HookDefinitionMember() { }
    public HookDefinitionMember(Method method, XHook blob, Class<?> resolvedClazz) throws NoSuchMethodException {
        this.blob = blob;
        this.method = method;
        this.resolvedClazz = resolvedClazz;
        this.member = ReflectUtilEx.resolveMember(resolvedClazz, method.getName(), method.getParameterTypes());
    }

    public HookDefinitionMember(Constructor<?> constructor, XHook blob, Class<?> resolvedClazz) throws NoSuchMethodException {
        this.blob = blob;
        this.constructor = constructor;
        this.resolvedClazz = resolvedClazz;
        this.member = ReflectUtilEx.resolveMember(resolvedClazz, null, constructor.getParameterTypes());
    }
}
