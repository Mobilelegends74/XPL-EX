package eu.faircode.xlua.x.xlua.hook;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.JsonHelperEx;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.call.GetHookCommand;
import eu.faircode.xlua.x.xlua.commands.call.GetSettingExCommand;
import eu.faircode.xlua.x.xlua.commands.query.GetHooksCommand;
import eu.faircode.xlua.x.xlua.database.updaters.UpdateMapEntry;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.SettingReMappedItem;
import eu.faircode.xlua.x.xlua.settings.SettingsContainer;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.interfaces.NameInformationTypeBase;

public class HooksSettingsGlobal {
    private static final String TAG = LibUtil.generateTag(HooksSettingsGlobal.class);

    public static final Map<String, Integer> GET_SET_PATTERNS = Map.ofEntries(
            Map.entry("getSettingInt(", 1),
            Map.entry("getSetting(", 1),
            Map.entry("getSettingBool(", 1),
            Map.entry("getSettingReMap(", 2));

    private static final Object lock = new Object();

    private static final Map<String, String> groups = new HashMap<>();
    private static final Map<String, String> collections = new HashMap<>();
    private static final Map<String, List<String>> settingsMap = new HashMap<>();
    private static final Map<String, String> remappedSettings = new HashMap<>();


    public static List<XHook> getHooksEx(Context context) { return GetHooksCommand.getHooks(context, true, false); }

    //public static XLuaHook getHook(Context context, String hookId) { return GetHookCommand.get(context, hookId); }
    //public static List<XLuaHook> getHooks(Context context) { return GetHooksCommand.getHooks(context, true, false); }
    //public static List<XLuaHook> getAllHooks(Context context) { return GetHooksCommand.getHooks(context, true, true); }
    public static List<String> getCollections(Context context) { return GetSettingExCommand.getCollections(context, Process.myUid()); }

    public static List<String> settingHoldersToNames(SettingsContainer container) { return settingHoldersToNames(container, false); }


    public static List<String> settingHoldersToNames(SettingsContainer container, boolean includeIndexZero) {
        return container != null ? settingHoldersToNames(container.getSettings(includeIndexZero)) : ListUtil.emptyList();
    }


    public static List<String> settingPacketsToNames(List<SettingPacket> settings) { return ListUtil.forEachTo(settings, (o) -> o.name); }
    public static List<String> settingHoldersToNames(List<SettingHolder> settings) { return ListUtil.forEachTo(settings, NameInformationTypeBase::getName); }

    public static List<String> keepCollectionHooks(Context context, List<String> hookIds) {
        init(context);
        return keepCollectionHooks(getCollections(context), hookIds);
    }

    public static List<String> keepCollectionHooks(List<String> collections, List<String> hookIds) {
        if(!ListUtil.isValid(hookIds) || !ListUtil.isValid(collections))
            return hookIds;

        List<String> out = new ArrayList<>();
        for(String hookId : hookIds) {
            if(!Str.isEmpty(hookId)) {
                String collection = HooksSettingsGlobal.collections.get(hookId);
                if(Str.isEmpty(collection))
                    continue;

                if(collections.contains(collection)) {
                    out.add(collection);
                }
            }
        }

        return out;
    }

    public static String resolveSettingName(Context context, String settingName) {
        init(context);
        return resolveSettingNameInternal(settingName);
    }

    public static List<String> getHookIdsForSetting(Context context, String settingName) {
        return getHookIdsForSettings(context, settingName);
    }

    public static List<String> getHookIdsFromCollections(Context context) {
        init(context);
        return getHookIdsFromCollections(getCollections(context));
    }

    public static List<String> getHookIdsFromCollections(List<String> collections) {
        List<String> all = new ArrayList<>();
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Getting Hook Ids from Collections, Collections=[%s] Count=%s", Str.joinList(collections), ListUtil.size(collections)));

        if(ListUtil.isValid(collections) && !HooksSettingsGlobal.collections.isEmpty()) {
            for(Map.Entry<String, String> entry : HooksSettingsGlobal.collections.entrySet()) {
                String hookId = entry.getKey();
                String collection = entry.getValue();
                if(Str.isEmpty(hookId) || Str.isEmpty(collection))
                    continue;

                if(!all.contains(hookId) && collections.contains(collection))
                    all.add(hookId);
            }
        }

        return all;
    }

    public static List<String> getHookIdsForSettings(Context context, String... settingNames) { return ArrayUtils.isValid(settingNames, 1) ? getHookIdsForSettings(context, Arrays.asList(settingNames)) : ListUtil.emptyList(); }
    public static List<String> getHookIdsForSettings(Context context, List<String> settingNames) {
        init(context);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Getting Hook Ids for Settings=[%s] Count=%s Has Setting First [%s]",
                    Str.joinList(settingNames),
                    ListUtil.size(settingNames),
                    ListUtil.size(settingNames) > 0 && settingsMap.containsKey(settingNames.get(0))));

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Getting Hook Ids for [%s] Stack=%s",
                    Str.joinList(settingNames),
                    Str.ensureNoDoubleNewLines(RuntimeUtils.getStackTraceSafeString(new Exception()))));

        List<String> all = getHookIdsFromIndex(settingNames);

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Finished Getting Hook Ids for Settings=[%s] Count=%s All=[%s] All Count=%s",
                    Str.joinList(settingNames),
                    ListUtil.size(settingNames),
                    Str.joinList(all),
                    ListUtil.size(all)));

        return all;
    }

    static List<String> getHookIdsFromIndex(List<String> settingNames) {
        List<String> all = new ArrayList<>();
        if(ListUtil.isValid(settingNames)) {
            for(String setting : settingNames) {
                if(!Str.isEmpty(setting)) {
                    for(String lookupName : getLookupNames(setting))
                        ListUtil.addAllIfValidEx(all, settingsMap.get(lookupName));
                }
            }
        }
        return all;
    }

    public static void clear() {
        synchronized (lock) {
            groups.clear();
            collections.clear();
            settingsMap.clear();
            remappedSettings.clear();
        }
    }

    public static void init(Context context) {
        if(context == null)
            return;

        //We should make this instance base all of this ?
        synchronized (lock) {
            if(settingsMap.isEmpty()) {
                internalInitReMappedSettings(context);
                List<XHook> hooks = GetHooksCommand.getHooks(context, true, true);
                if(DebugUtil.isDebug()) {
                    Log.d(TAG, "Got Hooks Count=" + ListUtil.size(hooks));
                }

                List<String> collections = GetSettingExCommand.getCollections(context, Process.myUid());
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Collections Size=" + ListUtil.size(collections) + " Collections(" + Str.joinList(collections) + ")");

                if(ListUtil.isValid(hooks)) {
                    for(XHook hook : hooks) {

                        if(!ListUtil.isValid(collections) || hook == null || Str.isEmpty(hook.collection) || !collections.contains(hook.collection))
                            continue;


                        try {
                            String hookId = hook.getObjectId();
                            List<String> settings = hook.settings;
                            if(DebugUtil.isDebug())
                                Log.d(TAG, Str.fm("HookId (%s) Settings (%s)(%s)",
                                        hook,
                                        ListUtil.size(settings),
                                        Str.joinList(settings)));

                            if(ListUtil.isValid(settings)) {
                               for(String setting : settings) {
                                   if(Str.isEmpty(setting))
                                       continue;

                                   String lowered = setting.toLowerCase();
                                   internalAdd(lowered, hookId);
                                   internalAddHook(hook);
                                   /*if(isXi) {
                                       if(DebugUtil.isDebug())
                                           Log.d(TAG, Str.fm("Adding Setting [%s] Hook Id [%s] Lowered [%s] Contains Setting=%s Hook Id Count for Setting=%s",
                                                   setting,
                                                   hookId,
                                                   lowered,
                                                   settingsMap.containsKey(lowered),
                                                   ListUtil.size(settingsMap.get(lowered))));
                                   }*/
                               }
                            }

                            try {
                                //This will cause issues, as some are not direct link any more
                                String luaScript = hook.luaScript;
                                if(!Str.isEmpty(luaScript) && luaScript.length() > 5 && luaScript.contains("function") && (luaScript.contains("before") || luaScript.contains("after"))) {
                                    for(Map.Entry<String, Integer> entry : GET_SET_PATTERNS.entrySet()) {
                                        String search = entry.getKey();
                                        int count = entry.getValue();
                                        List<String> parsed = SettingsParseUtil.findStringOccurrences(luaScript, search, count);
                                        if (ListUtil.isValid(parsed)) {
                                            for(String found : parsed) {
                                                if(!Str.isEmpty(found)) {
                                                    //Consider mapping ?
                                                    String lowered = found.toLowerCase();
                                                    internalAdd(lowered, hookId);
                                                    internalAddHook(hook);
                                                    if(DebugUtil.isDebug())
                                                        Log.d(TAG, Str.fm("Adding Setting [%s] Hook Id [%s] Lowered [%s] from Lua Script!",
                                                                found,
                                                                hookId,
                                                                lowered));
                                                }
                                            }
                                        }
                                    }
                                }
                            }catch (Exception e) {
                                Log.e(TAG, Str.fm("Failed to Parse Lua Script Hook Id [%s] Error=%s", hookId, e));
                            }
                        }catch (Exception e) {
                            Log.e(TAG, "Failed to Push / Parse Hook, ID=" + hook.getObjectId());
                        }
                    }
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Finished Initializing Hooks and their Settings etc... Groups Count=%s Collections Count=%s Settings Map Count=%s ReMapped Settings Count=%s",
                            HooksSettingsGlobal.groups.size(),
                            HooksSettingsGlobal.collections.size(),
                            HooksSettingsGlobal.settingsMap.size(),
                            HooksSettingsGlobal.remappedSettings.size()));
            }
        }
    }

    private static void internalInitReMappedSettings(Context context) {
        try {
            for(UpdateMapEntry entry: JsonHelperEx.findJsonElementsFromAssets(XUtil.getApk(context), SettingReMappedItem.Setting_legacy.JSON, true, UpdateMapEntry.class)) {
                if(!Str.isEmpty(entry.id)) {
                    for(String oldId : entry.oldIds) {
                        if(!Str.isEmpty(oldId))
                            remappedSettings.put(normalizeSettingName(oldId), normalizeSettingName(entry.id));
                    }
                }
            }
        }catch (Exception e) {
            Log.e(TAG, "Failed to Init Settings Re Map, Error=" + e);
        }
    }

    private static void internalAdd(String settingName, String hookId) {
        if(!Str.isEmpty(settingName) && !Str.isEmpty(hookId)) {
            for(String lookupName : getLookupNames(settingName))
                internalAddExact(lookupName, hookId);
        }
    }

    private static void internalAddExact(String settingName, String hookId) {
        if(!Str.isEmpty(settingName) && !Str.isEmpty(hookId)) {
            List<String> hooks = settingsMap.get(settingName);
            if(hooks == null) {
                hooks = new ArrayList<>();
                hooks.add(hookId);
                settingsMap.put(settingName, hooks);
            } else {
                if(!hooks.contains(hookId)) {
                    hooks.add(hookId);
                }
            }
        }
    }

    static List<String> getLookupNames(String settingName) {
        Set<String> names = new LinkedHashSet<>();
        String normalized = normalizeSettingName(settingName);
        String resolved = resolveSettingNameInternal(normalized);
        if("__delete".equals(resolved))
            return new ArrayList<>();

        addLookupName(names, normalized);
        addLookupName(names, resolved);

        return new ArrayList<>(names);
    }

    private static void addLookupName(Set<String> names, String settingName) {
        if(Str.isEmpty(settingName) || "__delete".equals(settingName))
            return;

        names.add(settingName);
        String baseName = getIndexedBaseName(settingName);
        if(!Str.isEmpty(baseName))
            names.add(baseName);
    }

    private static String resolveSettingNameInternal(String settingName) {
        String normalized = normalizeSettingName(settingName);
        if(Str.isEmpty(normalized))
            return normalized;

        String resolved = remappedSettings.get(normalized);
        return Str.isEmpty(resolved) ? normalized : resolved;
    }

    private static String normalizeSettingName(String settingName) {
        return Str.isEmpty(settingName) ? settingName : settingName.trim().toLowerCase(Locale.ROOT);
    }

    private static String getIndexedBaseName(String settingName) {
        if(Str.isEmpty(settingName))
            return settingName;

        int arrayIndex = settingName.lastIndexOf(".[");
        if(arrayIndex > 0 && settingName.endsWith("]"))
            return settingName.substring(0, arrayIndex);

        int separator = settingName.lastIndexOf('.');
        if(separator < 1 || separator == settingName.length() - 1)
            return settingName;

        for(int i = separator + 1; i < settingName.length(); i++)
            if(!Character.isDigit(settingName.charAt(i)))
                return settingName;

        return settingName.substring(0, separator);
    }

    private static void internalAddHook(Context context, String hookId) { internalAddHook(GetHookCommand.getEx(context, hookId)); }
    private static void internalAddHook(XHook hook) {
        if(hook != null) {
            String id = hook.getObjectId();
            String group = hook.group;
            String collection = hook.collection;
            if(!Str.isEmpty(id)) {
                if(!Str.isEmpty(group))
                    groups.put(id, group);
                if(!Str.isEmpty(collection))
                    collections.put(id, collection);
            }
        }
    }
}
