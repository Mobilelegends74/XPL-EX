package eu.faircode.xlua.x.hook.filter;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.FilterHooksHolder;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.TypeMap;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.hook.filter.kinds.IPCCallFilterContainer;
import eu.faircode.xlua.x.hook.filter.kinds.IPCQueryFilterContainer;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHookIO;
import eu.faircode.xlua.x.xlua.LibUtil;

@SuppressWarnings("all")
public class FilterContainerElement implements IFilterContainer {
    private static final String TAG = LibUtil.generateTag(FilterContainerElement.class);

    protected TypeMap definitions;
    protected String groupName;
    protected final FilterHooksHolder factory = FilterHooksHolder.create();
    protected final HashMap<String, String> createdSettings = new HashMap<>();
    protected final List<String> dependencies = new ArrayList<>();

    public final Map<String, HashMap<String, String>> wildCardPatterns = new HashMap<>();
    public static final String DIRECT_VAL_SYMBOL = "val:";

    public FilterContainerElement() { }
    public FilterContainerElement(String groupName, TypeMap definitions) { this.groupName = groupName; this.definitions = definitions; }

    @Override
    public String getGroupName() { return groupName; }

    public void putSettingPair(String name, String value) {
        if(!Str.isEmpty(name)) {
            createdSettings.put(name, Str.getNonNullString(value, Str.EMPTY));
        } else {
            Log.e(TAG, Str.fm("Failed to Put Setting for Group [%s], name of Setting is NULL or Empty! Stack=%s",
                    groupName,
                    RuntimeUtils.getStackTraceSafeString(new Throwable())));
        }
    }

    @Override
    public boolean hasSwallowedAsDefinition(XHook hook) {
        if(isGroup(hook)) {
            if(definitions.hasDefinition(hook)) {
                factory.addBase(hook);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Swallowed Hook [%s] as a Definition [(%s)::(%s)::(%s)] for Filter Group [%s], Updated Count=%s",
                            hook.getObjectId(),
                            hook.className,
                            hook.resolvedClassName,
                            hook.methodName,
                            groupName,
                            factory.baseCount()));

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasSwallowedAsRule(XHook hook) {
        if(isClassGroup(hook)) {
            factory.addRule(hook);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Swallowed Hook [%s] as a Rule [(%s)::(%s)::(%s)] for Filter Group [%s], Updated Count=%s",
                        hook.getObjectId(),
                        hook.methodName,
                        Str.joinList(hook.parameterTypes),
                        Str.joinList(hook.settings),
                        groupName,
                        factory.ruleCount()));

            List<String> params = hook.parameterTypes;
            if(ListUtil.isValid(params)) {
                //We reserve the settings[] field so the settings UI is not confused
                //if the param has a '=' then we can assume they are trying to set a setting to a value
                //Instead of Invoking a Script or something and waiting we can pre init settings via Param Types of a Hook Definition
                for(String p : params) {
                    if(p.contains("=")) {
                        String[] parts = p.split("=");
                        if(p.length() == 2) {
                            String settingName = parts[0].trim();
                            String settingValue = parts[1];
                            if(!Str.isEmpty(settingValue))
                                createdSettings.put(settingName, settingValue);
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }


    @Override
    public List<String> getDependencies() { return ListUtil.copyToArrayList(dependencies); }

    @Override
    public boolean hasSettings() { return !createdSettings.isEmpty(); }

    @Override
    public int appendSettings(Map<String, String> settings) {
        int count = 0;
        if(settings != null) {
            if(ListUtil.isValid(this.createdSettings)) {
                for(Map.Entry<String, String> entry : new HashMap<>(this.createdSettings).entrySet()) {
                    String name = entry.getKey();
                    String value = entry.getValue();
                    //Removed Value Null Check
                    if(!Str.isEmpty(name)) {
                        settings.put(name, value);
                        count++;
                    }
                }
            }
        }

        return count;
    }

    @Override
    public void initializeDefinitions(List<XHook> hooks, Map<String, String> settings) {
        appendSettings(settings);
        if(hooks != null) {
            if(factory.hasRules() && factory.hasBases())
                ListUtil.addAllNoCopies(hooks, factory.getBaseHooks());
        }
    }

    @Override
    public List<XHook> getRules() { return factory.getRuleHooks();  }

    @Override
    public List<XHook> getFilterBases() { return factory.getBaseHooks(); }

    public boolean isClassGroup(XHook hook) { return isClassGroup(hook, false, false); }
    public boolean isClassGroup(XHook hook, boolean checkHasParams) { return isClassGroup(hook, checkHasParams, false); }
    public boolean isClassGroup(XHook hook, boolean checkHasParams, boolean checkHasSettings) {
        if(!isDefinitionValid(hook, checkHasParams, checkHasSettings))
            return false;

        return  hook.getClassName().equalsIgnoreCase(groupName);
    }

    public boolean isGroup(XHook hook) { return isGroup(hook, false, false); }
    public boolean isGroup(XHook hook, boolean checkHasParams) { return isGroup(hook, checkHasParams, false); }
    public boolean isGroup(XHook hook, boolean checkHasParams, boolean checkHasSettings) {
        if(!isDefinitionValid(hook, checkHasParams, checkHasSettings))
            return false;

        return hook != null && hook.isValid(true, true) && hook.group.equalsIgnoreCase(groupName);
    }

    public boolean isDefinitionValid(XHook hook) { return isDefinitionValid(hook, false, false); }
    public boolean isDefinitionValid(XHook hook, boolean checkHasParams) { return isDefinitionValid(hook, checkHasParams, false); }
    public boolean isDefinitionValid(XHook hook, boolean checkHasParams, boolean checkHasSettings) {
        if(hook == null) {
            Log.e(TAG, "Critical Error, some how the Hook Definition passed is Null ? Check your fucking Code! Stack=" + RuntimeUtils.getStackTraceSafeString(new Throwable()));
            return false;
        }

        if(Str.isEmpty(hook.getClassName()) || Str.isEmpty(hook.methodName) || Str.isEmpty(hook.group)) {
            Log.e(TAG, "Error, Hook Definition is Invalid, is Missing Class Name, Method Name or Group Name! Hook=" + XHookIO.toJsonString(hook));
            return false;
        }

        return !(checkHasParams && !hasParams(hook) || checkHasSettings && !hasSettings(hook));
    }

    public boolean hasParams(XHook hook) {
        if(hook == null) {
            Log.e(TAG, "(hasParams) Error Hook Input is Null! Stack=" + RuntimeUtils.getStackTraceSafeString(new Throwable()));
            return false;
        }

        List<String> types = hook.parameterTypes;
        if(!ListUtil.isValid(types)) {
            Log.e(TAG, "Error, Hook Definition is Invalid, is Missing Parameter Types! Hook=" + XHookIO.toJsonString(hook));
            return false;
        }

        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasSettings(XHook hook) {
        if(hook == null) {
            Log.e(TAG, "(hasSettings) Error Hook Input is Null! Stack=" + RuntimeUtils.getStackTraceSafeString(new Throwable()));
            return false;
        }

        List<String> setting = hook.settings;
        if(!ListUtil.isValid(setting)) {
            Log.e(TAG, "Error, Hook Definition is Invalid, is Missing Settings! Hook=" + XHookIO.toJsonString(hook));
            return false;
        }

        return true;
    }

    public List<String> parseArgsAsAuthorities(XHook hook) {
        List<String> auths = new ArrayList<>();
        if(!hasParams(hook)) {
            Log.w(TAG, "Hook is Some how missing param types or is Null, using [*] (wild card) for Authorities! Hook=" + XHookIO.toJsonString(hook));
            auths.add("*");
            return auths;
        }

        List<String> params = hook.parameterTypes;
        for(String auth : params) {
            if(!Str.isEmpty(auth)) {
                String trimmed = Str.trimEx(auth, true, true);
                if(!Str.isEmpty(trimmed))
                    auths.add(trimmed);
            }
        }

        if(auths.isEmpty()) {
            Log.w(TAG, Str.fm("No Valid Param Types for Hook [%s] under Group [%s], using [*] (wild card) for Authorities!", hook.getObjectId(), groupName));
            auths.add("*");
        }

        return auths;
    }


    public List<String> parseParams(XHook hook, boolean trimWhiteSpace) {
        List<String> pts = new ArrayList<>();
        if(hook == null) {
            pts.add("*");
            return pts;
        }

        if(ListUtil.isValid(hook.parameterTypes)) {
            for(String p : hook.parameterTypes) {
                if(!Str.isEmpty(p)) {
                    if(trimWhiteSpace) {
                        String trimmed = Str.trimEx(p, true, true);
                        if(!Str.isEmpty(trimmed))
                            pts.add(trimmed);
                    } else {
                        pts.add(p);
                    }
                }
            }
        }

        if(pts.isEmpty()) {
            pts.add("*");
        }

        return pts;
    }

    @SuppressWarnings("IndexOfReplaceableByContains")
    public List<String> parseMethod(XHook hook, boolean trimWhiteSpace) {
        //Also Support parse by new line!
        List<String> pts = new ArrayList<>();
        if(hook == null || Str.isEmpty(hook.methodName)) {
            Log.w(TAG, "Hook is Some how missing method name or is Null, using [*] (wild card), Hook=" + XHookIO.toJsonString(hook));
            pts.add("*");
            return pts;
        }

        String method = hook.methodName;
        if(!Str.isEmpty(method)) {
            if(method.indexOf("|") > -1) {
                String[] parts = method.split("\\|");
                for(String p : parts) {
                    if(!Str.isEmpty(p)) {
                        if(trimWhiteSpace) {
                            String trimmed = Str.trimEx(p, true, true);
                            if(!Str.isEmpty(trimmed))
                                pts.add(trimmed);
                        } else {
                            pts.add(p);
                        }
                    }
                }
            } else {
                pts.add(method);
            }
        }

        if(pts.isEmpty()) {
            Log.w(TAG, Str.fm("No Valid Filters for Hook [%s] under Group [%s], using [*] (wild card) for Filters!", hook.getObjectId(), groupName));
            pts.add("*");
        }

        return pts;
    }

    // "query:[" + auth + "]:" + pair.name;

    public static String createAuthoritySetting(String rule) { return !Str.isEmpty(rule) ? Str.combine("query:", rule) : rule; }
    public static String createCallSetting(String rule) { return !Str.isEmpty(rule) ? Str.combine("call:", rule) : rule; }
    public static String createPropertySetting(String property) { return !Str.isEmpty(property) ? Str.combine("prop:", property) : property; }


    public static String createQuerySetting(String auth, String name) { return Str.isEmpty(name) ? null : Str.combineEx("query:[", Str.ensureIsNotNullOrDefault(auth, "*"), "]:", name);  }
    public static String createQueryDirectValueName(String name, String authority) { return !Str.isEmpty(name) ? Str.combineEx(IPCQueryFilterContainer.FILLER_SYMBOL, createQuerySetting(name, authority)) : name; }

    public static String createCallSetting(String arg, String authority) { return !Str.isEmpty(arg) ? Str.combineEx("call:[", Str.ensureIsNotNullOrDefault(authority, "*"), "]:", arg) : arg; }
    public static String createCallDirectValueName(String name, String authority) { return !Str.isEmpty(name) ? Str.combineEx(IPCCallFilterContainer.FILLER_SYMBOL, createCallSetting(name, authority)) : name; }

    @NonNull
    @Override
    public String toString() {
        return StrBuilder.create()
                .ensureOneNewLinePer(true)
                .appendLine("==============================")
                .appendFieldLine("Group Name", groupName)
                .appendFieldLine("Base Hook Count", factory.baseCount())
                .appendFieldLine("Rule Hook Count", factory.ruleCount())
                .appendFieldLine("Settings Count", createdSettings.size())
                .appendLine("==============================")
                .toString(true);
    }
}
