package eu.faircode.xlua.x.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;

@SuppressWarnings("all")
public class TypeMap {
    public static TypeMap create() { return new TypeMap(); }

    private final HashMap<String, List<String>> mTypeMap = new HashMap<>();

    public boolean hasDefinition(XHook hook) { return hook != null && (hasDefinition(hook.className, hook.methodName) || hasDefinition(hook.resolvedClassName, hook.methodName)); }

    public boolean hasDefinition(String className, String methodName) {
        synchronized (mTypeMap) {
            List<String> methods = mTypeMap.get(className);
            if(!ListUtil.isValid(methods))
                return false;

            String t = Str.trimOriginal(methodName);
            for(String m : methods)
                if(m.equalsIgnoreCase(t))
                    return true;

            return false;
        }
    }

    public TypeMap add(Class<?> clazz, String... methods) { return add(Str.toObjectClassNameNonNull(clazz), methods); }

    public TypeMap add(String className, String... methods) {
        if(!Str.isEmpty(className) && ArrayUtils.isValid(methods)) {
            synchronized (mTypeMap) {
                List<String> list = mTypeMap.get(className);
                if(list == null) {
                    list = new ArrayList<>();
                    mTypeMap.put(className, list);
                }

                for(String m : methods) {
                    if(!Str.isValidNotWhitespaces(m))
                        continue;

                    String cleaned = Str.trimOriginal(m);
                    if(!list.contains(cleaned))
                        list.add(cleaned);
                }
            }
        }

        return this;
    }
}
