package eu.faircode.xlua.x.data;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;

public class FilterHooksHolder {
    public static FilterHooksHolder create() { return new FilterHooksHolder(); }

    private final Object mLock = new Object();
    private final List<XHook> baseHooks = new ArrayList<>();
    private final List<XHook> ruleHooks = new ArrayList<>();

    public boolean hasRules() { synchronized (mLock) { return !ruleHooks.isEmpty(); } }
    public boolean hasBases() { synchronized (mLock) { return !baseHooks.isEmpty(); } }

    public List<XHook> getBaseHooks() { synchronized (mLock) { return new ArrayList<>(baseHooks); } }
    public List<XHook> getRuleHooks() { synchronized (mLock) { return new ArrayList<>(ruleHooks); } }

    public int baseCount() { synchronized (mLock) { return baseHooks.size(); } }
    public int ruleCount() { synchronized (mLock) { return ruleHooks.size(); } }

    public void addBase(XHook hook) {
        synchronized (mLock) {
            if(!baseHooks.contains(hook)) {
                hook.enabled = true;
                baseHooks.add(hook);
            }
        }
    }

    public void addRule(XHook hook) {
        synchronized (mLock) {
            if(!ruleHooks.contains(hook))
                ruleHooks.add(hook);
        }
    }

    public void removeRule(XHook hook) {
        synchronized (mLock) {
            if(!ruleHooks.contains(hook))
                ruleHooks.remove(hook);
        }
    }
}
