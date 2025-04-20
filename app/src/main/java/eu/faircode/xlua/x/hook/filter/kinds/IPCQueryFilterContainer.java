package eu.faircode.xlua.x.hook.filter.kinds;

import android.content.ContentResolver;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.TypeMap;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.hook.filter.FilterContainerElement;
import eu.faircode.xlua.x.hook.filter.IFilterContainer;
import eu.faircode.xlua.x.hook.filter.SettingPair;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.xlua.LibUtil;

public class IPCQueryFilterContainer extends FilterContainerElement implements IFilterContainer {
    private static final String TAG = LibUtil.generateTag(IPCQueryFilterContainer.class);

    public static IFilterContainer create() { return new IPCQueryFilterContainer(); }

    public static final String GROUP_NAME = "Intercept.Intent.Query";
    public static final TypeMap DEFINITIONS = TypeMap.create().add(ContentResolver.class, "query");

    public IPCQueryFilterContainer() { super(GROUP_NAME, DEFINITIONS); }


    public static String createArgSetting(String arg, String authority) { return arg != null ? Str.combineEx("query:arg:[", Str.toLowerCase(arg), "]:[", Str.toLowerCase(authority), "]") : null; }
    public static String createAuthoritySetting(String authority) { return authority != null ? Str.combineEx(QUERY_SETTING_PREFIX, "[", Str.toLowerCase(authority), "]") : null; }

    public static final String QUERY_SETTING_PREFIX = "query:auth:";
    public static final String FILLER_SYMBOL = "__q:s:d:";
    public static final String EMPTY_PREFIX = "__empty__";
    public static final String AUTH_PREFIX = "content://";

    @Override
    public int appendSettings(Map<String, String> settings) {
        int count = 0;
        for(Map.Entry<String, String> setting : this.createdSettings.entrySet()) {
            String name = setting.getKey();
            String value = setting.getValue();
            if(Str.startsWith(name, QUERY_SETTING_PREFIX) && settings.containsKey(name) && !Str.isEmpty(value)) {
                String oldValue = settings.get(name);
                String trimmedStart = Str.trimEx(oldValue, true, true, true, true, Str.PIPE);
                String trimmedEnd = Str.trimEx(value, true, true, true, true, Str.PIPE);
                String newValue = StrBuilder.create()
                        .setDoAppendFlag(!Str.isEmpty(trimmedStart))
                        .append(trimmedStart)
                        .append(Str.PIPE)
                        .setDoAppendFlag(!Str.isEmpty(trimmedEnd))
                        .toString();

                settings.put(name, newValue);
                count++;
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Modified Existing Query Auth Setting [%s] with Original Value of (%s) requested to Append (%s) with the Final new Value of (%s)",
                            name,
                            oldValue,
                            value,
                            newValue));
            } else {
                settings.put(name, value);
                count++;
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Appended Query Setting [%s] with the value Of (%s)",
                            name,
                            value));
            }
        }

        return count;
    }

    @Override
    public void putSettingPair(String name, String value) {
        if(Str.startsWith(name, QUERY_SETTING_PREFIX) && this.createdSettings.containsKey(name)) {
            String oldValue = this.createdSettings.get(name);
            String trimmedStart = Str.trimEx(oldValue, true, true, true, true, Str.PIPE);
            String trimmedEnd = Str.trimEx(value, true, true, true, true, Str.PIPE);
            String newValue = StrBuilder.create()
                    .setDoAppendFlag(!Str.isEmpty(trimmedStart))
                    .append(trimmedStart)
                    .append(Str.PIPE)
                    .setDoAppendFlag(!Str.isEmpty(trimmedEnd))
                    .append(trimmedEnd)
                    .toString();

            createdSettings.put(name, newValue);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Setting Exists Query Auth Setting [%s] with Original Value of (%s) requested to Append (%s) with the Final new Value of (%s) Created Settings Count=%s",
                        name,
                        oldValue,
                        value,
                        newValue,
                        createdSettings.size()));
        } else {
            createdSettings.put(name, value);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Appended Query Setting [%s] with Value of [%s] Created Settings Count=%s",
                        name,
                        value,
                        createdSettings.size()));
        }
    }

    @Override
    public boolean hasSwallowedAsRule(XHook hook) {
        boolean isRule = super.hasSwallowedAsRule(hook);
        if(isRule) {
            List<String> authorities = parseMethod(hook, true);
            List<String> params = parseParams(hook, true);
            List<String> settings = hook.settings;
            boolean hasSettings = ListUtil.isValid(settings);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Hook Query [%s] Rule for Group [%s] was parsed, Filter=[%s] Settings=[%s] Authorities=[%s]",
                        hook.getObjectId(),
                        groupName,
                        Str.joinList(params),
                        Str.joinList(settings),
                        Str.joinList(authorities)));

            if(authorities.isEmpty() && hasSettings && !params.isEmpty())
                authorities.add(Str.ASTERISK);

            if(DebugUtil.isDebug())
                Log.d(TAG, "Query Hook=" + Str.ensureNoDoubleNewLines(hook.toString(true, true)));

            if(!authorities.isEmpty()) {
                for(String ath : authorities) {
                    final String auth = Str.ensureNotStartWith(ath, AUTH_PREFIX);
                    if(Str.isEmpty(auth))
                        continue;

                    if(!hasSettings) {
                        String emptyName = IPCQueryFilterContainer.createArgSetting(EMPTY_PREFIX, auth);
                        putSettingPair(emptyName, "true");
                        if(DebugUtil.isDebug())
                            Log.d(TAG, Str.fm("Pushing Setting [%s] as a Rule, the Cursor should Return Empty! Auth [%s] HookId=%s",
                                    emptyName,
                                    auth,
                                    hook.getObjectId()));
                    } else {
                        if(!params.isEmpty()) {
                            List<String> addedTargets = new ArrayList<>();
                            for(int i = 0; i < params.size(); i++) {
                                String item = Str.toLowerCase(params.get(i));
                                if(Str.isEmpty(item))
                                    continue;

                                boolean isWild = item.length() > 3 && item.startsWith(Str.ASTERISK) && item.endsWith(Str.ASTERISK);
                                SettingPair pair = new SettingPair(isWild ?
                                        item.substring(1, item.length() - 1) : item, i, settings, auth);

                                if(SettingPair.isValid(pair)) {
                                    if(DebugUtil.isDebug())
                                        Log.d(TAG, Str.fm("Setting Item [%s] Pair Name [%s] Pair Setting Name [%s] [%s][%s] Pair Authority [%s] Is Wild:%s Is Direct Value:%s",
                                                item,
                                                pair.name,
                                                pair.createQuerySetting(),
                                                pair.createQueryDirectValue(),
                                                pair.settingName,
                                                pair.authority,
                                                isWild,
                                                pair.isDirectValue()));

                                    //Fix this up more as we need to push if the target auth is a target auth we should be cleaning

                                    //ToDo: Clean this up more
                                    TryRun.silent(() -> {
                                        //We can parse the wild as is, but since right now it is "contains" wilds, we will ignore auth or...
                                        if(isWild) {
                                        /*HashMap<String, String> internalMap = wildCardPatterns.get(auth);
                                        if(internalMap == null) {
                                            internalMap = new HashMap<>();
                                            wildCardPatterns.put(auth, internalMap);
                                        }

                                        if(pair.isDirectValue()) {
                                            String actualValue = pair.settingName.substring(DIRECT_VAL_SYMBOL.length());
                                            String settingNameReDirect = pair.createCallDirectValue();
                                            internalMap.put(pair.name, settingNameReDirect);
                                            putSettingPair(settingNameReDirect, actualValue);
                                        } else {
                                            internalMap.put(pair.name, pair.settingName);
                                        }*/
                                        }
                                        else if(pair.isDirectValue()) {
                                            String actualValue = pair.settingName.substring(DIRECT_VAL_SYMBOL.length());
                                            String settingNameReDirect = pair.createQueryDirectValue();
                                            putSettingPair(pair.createQuerySetting(), settingNameReDirect);
                                            putSettingPair(settingNameReDirect, actualValue);
                                            //Same as below but with one more step, last step to dynamically here add thr setting to the list of settings
                                            addedTargets.add(pair.name);
                                            if(DebugUtil.isDebug())
                                                Log.d(TAG, Str.fm("Query Filter added a Rule (direct value) for Authority [%s] Hook [%s] Total Added Targets (%s). Pair Name [%s][%s] Redirect Name [%s] Setting Name [%s][%s] Authority=[%s] Actual Value [%s]",
                                                        auth,
                                                        hook.getObjectId(),
                                                        ListUtil.size(addedTargets),
                                                        pair.name,
                                                        item,
                                                        settingNameReDirect,
                                                        pair.createQuerySetting(),
                                                        pair.settingName,
                                                        pair.authority,
                                                        actualValue));
                                        } else {
                                            putSettingPair(pair.createQuerySetting(), pair.settingName);
                                            addedTargets.add(pair.name);
                                            if(DebugUtil.isDebug())
                                                Log.d(TAG, Str.fm("Query Filter added a Rule for Authority [%s] Hook [%s] Total Added Targets (%s). Pair Name [%s][%s] Setting Name [%s], Authority=[%s]",
                                                        auth,
                                                        hook.getObjectId(),
                                                        ListUtil.size(addedTargets),
                                                        pair.name,
                                                        item,
                                                        pair.settingName,
                                                        pair.authority));
                                        }
                                    });
                                }
                            }

                            if(!addedTargets.isEmpty()) {
                                //ToDo: Handle wild card authorities, just add to our global list or something
                                //  This is overriding old ! ? Maybe, I mean this is Authority based ? yes, because the "boot_count" is  part of the authority !
                                //  How the system currently works is it creates for that "auth" a Setting that list its targets, we need to use some shared cache to join others that have tasks on the Same Auth
                                //  Since, the value can be "joined" we should try that ?
                                //  Wait, we control the Appending Settings Stage... hehe eZ
                                putSettingPair(createAuthoritySetting(auth), Str.joinList(addedTargets, Str.PIPE));
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, Str.fm("Created Target Authority Auth Setting [%s] for Authority [%s] for filter Rules. Targets [%s] Hook [%s]",
                                            createAuthoritySetting(auth),
                                            auth,
                                            Str.joinList(addedTargets),
                                            hook.getObjectId()));
                            }
                        }
                    }
                }
            } else {
                return false;
            }
        }

        return isRule;
    }
}