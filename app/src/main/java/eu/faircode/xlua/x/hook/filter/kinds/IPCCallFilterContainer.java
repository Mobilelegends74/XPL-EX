package eu.faircode.xlua.x.hook.filter.kinds;

import android.content.ContentResolver;
import android.provider.Settings;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.TypeMap;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.hook.filter.FilterContainerElement;
import eu.faircode.xlua.x.hook.filter.IFilterContainer;
import eu.faircode.xlua.x.hook.filter.SettingPair;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.xlua.LibUtil;

public class IPCCallFilterContainer extends FilterContainerElement implements IFilterContainer {
    private static final String TAG = LibUtil.generateTag(IPCCallFilterContainer.class);

    public static IFilterContainer create() { return new IPCCallFilterContainer(); }

    public static final String GROUP_NAME = "Intercept.Settings.Call";
    public static final TypeMap DEFINITIONS =
            TypeMap.create()
            .add(ContentResolver.class, "call")
            .add(Settings.Secure.class, "getString")
            .add(Settings.Global.class, "getString")
            .add(Settings.System.class, "getString");

    public static final String CARDS = "__reserved.c.w.cards";
    public static final String SETS_OR_VALUE = "__reserved.c.w.sets";
    public static final String DELIMITER = "<_x__>";



    public IPCCallFilterContainer() { super(GROUP_NAME, DEFINITIONS); }

    /*
        ToDO: Add Support for Authority
     */

    @Override
    public int appendSettings(Map<String, String> settings) {
        return super.appendSettings(settings);
        /*if(settings != null) {
            StrBuilder sets = StrBuilder.create().ensureDelimiter(DELIMITER);
            StrBuilder vals = StrBuilder.create().ensureDelimiter(DELIMITER);
            if(!WILD_CARD_SETTINGS.isEmpty()) {
                for(Map.Entry<String, String> entry : WILD_CARD_SETTINGS.entrySet()) {
                    String name = entry.getKey();
                    String value = entry.getValue();
                    if(!Str.isEmpty())
                }
            }
        }*/
    }

    public static final String FILLER_SYMBOL = "__c:s:d";

    @Override
    public boolean hasSwallowedAsRule(XHook hook) {
        boolean isRule = super.hasSwallowedAsRule(hook);
        if(isRule) {
            if(!hasSettings(hook)) {
                factory.removeRule(hook);
            } else {
                //Support wild cards and wild card values >:)
                List<String> authorities = parseMethod(hook, true);
                List<String> params = parseParams(hook, true);
                List<String> settings = hook.settings;
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Hook Call [%s] Rule for Group [%s] was parsed, Filter=[%s] Settings=[%s] Authorities=[%s]",
                            hook.getObjectId(),
                            groupName,
                            Str.joinList(params),
                            Str.joinList(settings),
                            Str.joinList(authorities)));

                if(!params.isEmpty()) {
                    if(authorities.isEmpty())
                        authorities.add("*");

                    for(String auth : authorities) {
                        for(int i = 0; i < params.size(); i++) {
                            String item = Str.toLowerCase(params.get(i));
                            if(Str.isEmpty(item))
                                continue;

                            boolean isWild = item.length() > 3 && item.startsWith(Str.ASTERISK) && item.endsWith(Str.ASTERISK);
                            SettingPair pair = new SettingPair(isWild ? item.substring(1, item.length() - 1) : item, i, settings, auth);
                            if(SettingPair.isValid(pair)) {
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, Str.fm("Setting Item [%s] Pair Name [%s] Pair Setting Name [%s] [%s][%s] Pair Authority [%s] Is Wild:%s Is Direct Value:%s",
                                            item,
                                            pair.name,
                                            pair.createCallSetting(),
                                            pair.createCallDirectValue(),
                                            pair.settingName,
                                            pair.authority,
                                            isWild,
                                            pair.isDirectValue()));

                                //ToDo: Clean this up more
                                TryRun.silent(() -> {
                                    //We can parse the wild as is, but since right now it is "contains" wilds, we will ignore auth or...
                                    if(isWild) {
                                        HashMap<String, String> internalMap = wildCardPatterns.get(auth);
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
                                        }
                                    }
                                    else if(pair.isDirectValue()) {
                                        String actualValue = pair.settingName.substring(DIRECT_VAL_SYMBOL.length());
                                        String settingNameReDirect = pair.createCallDirectValue();
                                        putSettingPair(pair.createCallSetting(), settingNameReDirect);
                                        putSettingPair(settingNameReDirect, actualValue);
                                        //Same as below but with one more step, last step to dynamically here add thr setting to the list of settings
                                    } else {
                                        putSettingPair(pair.createCallSetting(), pair.settingName);
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }

        return isRule;
    }
}