package eu.faircode.xlua;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Binder;
import android.os.Parcel;
import android.util.Log;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XposedBridge;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.xstandard.database.SqlQuerySnake;
import eu.faircode.xlua.api.xlua.provider.XLuaHookProvider;
import eu.faircode.xlua.loggers.LogHelper;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.utilities.DatabasePathUtil;
import eu.faircode.xlua.utilities.DateTimeUtil;
import eu.faircode.xlua.utilities.JSONUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHookBase;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHookIO;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHookJsonUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.XposedUtility;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.DatabaseUtils;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.identity.UserIdentityUtils;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApi;

@SuppressWarnings("all")
public class XLegacyCore {
    private static final String TAG = LibUtil.generateTag(XLegacyCore.class);

    private static int totalCycles = 0;
    private static final Object lock = new Object();
    private static final HashMap<String, XHook> hooks = new HashMap<>();

    private static final HashMap<String, XHook> builtIn = new HashMap<>();
    private static final HashMap<String, XHook> databaseDefs = new HashMap<>();

    //public static int version = -1;

    public static final String DB_NAME_LUA = "xlua";
    public static final String DB_NAME_MOCK = "mock";
    final static String cChannelName = "xlua";

    public static List<String> getCollections(SQLDatabase database) { return database == null ? ListUtil.emptyList() : database.executeWithWriteLock(() -> SettingsApi.getCollectionsValue(database, UserIdentityUtils.getUserId(Binder.getCallingUid()))); }

    public static void clear(boolean flag) { if(flag) clear(); }
    public static void clear() { synchronized (lock) { internalClear(true); } }

    public static Collection<String> getGroups(Collection<String> collections) { synchronized (lock) { return internalGetGroups(collections); } }

    public static Collection<XHook> getHooks(Collection<String> collections, boolean takeAllHooks) { return getHooks(collections, takeAllHooks, true); }
    public static Collection<XHook> getHooks(Collection<String> collections, boolean takeAllHooks, boolean organize) {
        synchronized (lock) {
            List<XHook> results = internalGetHooks(collections, takeAllHooks);
            if(organize) organizeHooks(results);
            return results;
        }
    }

    //ToDO: SINCE we re-vamped the Hook System now, we no longer have to juggle, we can just make what our heart desires. IN that case we need to make a Global "Assignemtns" thingy the "10/10" Hooks asigned thing
    //  Lets ensure it is shared and synced attached to events to and when needing to update

    public static String getHookAsJsonString(String hookId) { return  XHookIO.toJsonString(getHook(hookId)); }

    public static void organizeHooks(List<XHook> hooks) {  if(ListUtil.isValid(hooks)) Collections.sort(hooks, (h1, h2) -> h1.getObjectId().compareTo(h2.getObjectId())); }

    public static XHook getHook(String hookId) { synchronized (lock) { return internalGetHook(hookId);  } }
    public static XHook getHook(String hookId, String packageName, Collection<String> collections) { XHook res = getHook(hookId); return res == null || !res.isAvailable(packageName, collections) ? null : res; }

    public static boolean updateHookCache(Context context, XHook hook, String extraId, boolean flag) { return flag && updateHookCache(context, hook, extraId); }
    public static boolean updateHookCache(Context context, XHook hook, String extraId) { synchronized (lock) { return internalUpdateHookCache(context, hook, extraId); } }

    public static void initializeFromJsons(Context context, SQLDatabase database, boolean clearCache) {
        if(DatabaseUtils.isReady(database)) {
            logI(Str.fm("Initializing Hooks from JSONs, Cache [%s::%s] Total Count=(%s) Cycles=(%s) Clear Cache=%s Database=%s",
                    hooks.size(),
                    builtIn.size(),
                    String.valueOf(hooks.size() + builtIn.size()),
                    totalCycles,
                    clearCache,
                    Str.toStringOrNull(database)));

            synchronized (lock) {
                TryRun.silent(() -> {
                    internalClear(clearCache);
                    totalCycles++;
                    PackageManager pm = context.getPackageManager();
                    if(pm == null) {
                        logE("Failed to connect to Package Manager Service! Error Initializing Hooks from JSONs! Stack=" + RuntimeUtils.getStackTraceSafeString(new Exception()));
                        return;
                    }

                    //INCREMENT CYCLES
                    //Consider my OBC Scripts Built In ?
                    ApplicationInfo ai = pm.getApplicationInfo(BuildConfig.APPLICATION_ID, 0);
                    List<XHook> hooksParsed = XHookJsonUtils.readHooks(context, ai.publicSourceDir);
                    if(DebugUtil.isDebug())
                        logD(Str.fm("Finished Reading Apk Assets JSON Hooks! Parsed Hooks Count=(%s) Database=%s Clear Cache=%s",
                                ListUtil.size(hooksParsed),
                                Str.toStringOrNull(database),
                                clearCache));

                    for (XHook hook : hooksParsed) {
                        if(hook.isValid()) {
                            hook.builtin = true;
                            hooks.put(hook.getObjectId(), hook);
                            builtIn.put(hook.getObjectId(), hook);  //Auto Assume if it is From the JSON Files, it is a built in Hook for now later we can expand on this
                        }
                    }

                    //ToDo: Please for one use Database Help Util Class, BUT make a check IF it exists in DB BUT it is the SAME just Ignore it then
                    //  Besides that Everything works as intended 100%, Good job on this Sexy Revision
                    Cursor cursor = null;
                    int parsedFromDatabase = 0;
                    try {
                        cursor = database.getDatabase().query(XHook.TABLE_NAME, null, null, null, null, null, null);
                        if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                            do {
                                XHook hook = XHook.create(cursor).ensureValidLuaScript(ai.publicSourceDir).resolveClass(context);
                                if(!hook.isValid()) {
                                    logE(TAG, "Bad Hook=" + hook.methodName);
                                } else {
                                    hook.builtin = false;
                                    hooks.put(hook.getObjectId(), hook);
                                    databaseDefs.put(hook.getObjectId(), hook);
                                    parsedFromDatabase++;
                                }
                            }
                            while (cursor.moveToNext());
                        }
                    }catch (Exception e) {
                        logE(Str.fm("Error Loading in Database Hooks! Error=" + e));
                    } finally {
                        CursorUtil.closeCursor(cursor);
                    }

                    if(DebugUtil.isDebug())
                        logD(Str.fm("Total Cached Hooks Count=%s Built In Cached Count=%s Parsed From Database Count=%s",
                                hooks.size(),
                                builtIn.size(),
                                parsedFromDatabase));
                });
            }
        }
    }


    private static List<String> internalGetHookIds() { return new ArrayList<>(hooks.keySet()); }

    private static void internalClear(boolean flag) {
        if(flag) {
            if(!hooks.isEmpty() || !builtIn.isEmpty()) {
                if(DebugUtil.isDebug())
                    logI(Str.fm("Clearing Internal Cache, from [%s::%s] Total Count=(%s) Cycles=(%s)",
                            hooks.size(),
                            builtIn.size(),
                            String.valueOf(hooks.size() + builtIn.size()),
                            totalCycles));

                hooks.clear();
                builtIn.clear();
            }
        }
    }

    private static XHook internalGetHook(String hookId) {
        if(Str.isEmpty(hookId))
            return null;

        XHook result =  hooks.get(hookId);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Searching for Hook (%s) within Cached [%s] Hooks and [%s] Built In Cached Hooks! Result=(%s) JSON=%s",
                    hookId,
                    hooks.size(),
                    builtIn.size(),
                    Str.toStringOrNull(result),
                    Str.ensureNoDoubleNewLines(XHookIO.toJsonString(result))));

        return result;
    }

    private static List<XHook> internalGetHooks(Collection<String> collections, boolean takeAllHooks) {
        List<XHook> results = new ArrayList<>();
        if(!hooks.isEmpty())
            for(XHook hook : hooks.values())
                if(takeAllHooks || hook.isAvailable(null, collections))
                    results.add(hook);

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Returning (%s) Hooks, All ? (%s) Collections=(%s) [%s] Cached Hooks Count=%s / %s",
                    results.size(),
                    takeAllHooks,
                    Str.joinList(collections),
                    ListUtil.size(collections),
                    hooks.size(),
                    builtIn.size()));

        return results;
    }

    private static List<String> internalGetGroups(Collection<String> collections) {
        List<String> groups = new ArrayList<>();
        if(ListUtil.isValid(collections) && !hooks.isEmpty()) {
            for(XHook hook : hooks.values()) {
                if(!Str.isEmpty(hook.group) && hook.isAvailable(null, collections) && !groups.contains(hook.group))
                    groups.add(hook.group);
            }
        }

        if(DebugUtil.isDebug())
            logD(Str.fm("Returning (%s) Groups from Collections=(%s) [%s] Cache=[%s::%s] Cycles=%s",
                    groups.size(),
                    Str.joinList(collections),
                    ListUtil.size(collections),
                    hooks.size(),
                    builtIn.size(),
                    totalCycles));

        return groups;
    }

    private static boolean internalUpdateHookCache(Context context, XHook hook, String extraId) {
        if(DebugUtil.isDebug())
            logD(Str.fm("Updating Hook Cache for Hook [%s] Is Delete [%s] ID [%s] Internal Cache Size=%s::%s::%s JSON=%s",
                    extraId,
                    hook == null,
                    Str.toObjectId(hook),
                    hooks.size(),
                    databaseDefs.size(),
                    builtIn.size(),
                    Str.ensureNoDoubleNewLines(XHookIO.toJsonString(hook))), false);

        if(hook == null) {
            databaseDefs.remove(extraId);
            hooks.remove(extraId);
            XHook original = builtIn.get(extraId);
            if(original != null) {
                hooks.put(extraId, original);
                if(DebugUtil.isDebug())
                    logD(Str.fm("Deleted [%s] from Cache and Found Original [%s] Internal Cache Size=%s::%s::%s JSON=%s",
                            extraId,
                            Str.toObjectId(original),
                            hooks.size(),
                            databaseDefs.size(),
                            builtIn.size(),
                            Str.ensureNoDoubleNewLines(XHookIO.toJsonString(original))), false);
            }
        } else {
            hook.resolveClass(context);
            hook.builtin = false;
            hooks.put(extraId, hook);
            databaseDefs.put(extraId, hook);
        }

        return true;
    }

    private static String tag_the_tag(String tag, String t) { return "[" + t.toLowerCase() + "][" + tag + "]"; }
    //ToDO: make our own Logger Class (Not XLog, that class is aids IMO a new XLog)
    public static void logD(String msg) { logD(msg, true); }
    public static void logI(String msg) { logI(msg, true); }
    public static void logE(String msg) { logE(msg, true); }
    public static void logW(String msg) { logW(msg, true); }

    public static void logD(String msg, boolean xp) { if(xp) XposedBridge.log(tag_the_tag(TAG, "D" ) +  msg); Log.d(TAG, msg); }
    public static void logI(String msg, boolean xp) { if(xp) XposedBridge.log(tag_the_tag(TAG, "I") + msg); Log.i(TAG, msg); }
    public static void logE(String msg, boolean xp) { if(xp) XposedBridge.log(tag_the_tag(TAG, "E") + msg); Log.e(TAG, msg); }
    public static void logW(String msg, boolean xp) { if(xp) XposedBridge.log(tag_the_tag(TAG, "W") + msg); Log.w(TAG, msg); }

    public static void logD(String tag, String msg) { logD(tag, msg, true); }
    public static void logI(String tag, String msg) { logI(tag, msg, true); }
    public static void logE(String tag, String msg) { logE(tag, msg, true); }
    public static void logW(String tag, String msg) { logW(tag, msg, true); }

    public static void logD(String tag, String msg, boolean xp) { if(xp) XposedBridge.log(tag_the_tag(tag, "D" ) +  msg); Log.d(TAG, msg); }
    public static void logI(String tag, String msg, boolean xp) { if(xp) XposedBridge.log(tag_the_tag(tag, "I") + msg); Log.i(TAG, msg); }
    public static void logE(String tag, String msg, boolean xp) { if(xp) XposedBridge.log(tag_the_tag(tag, "E") + msg); Log.e(TAG, msg); }
    public static void logW(String tag, String msg, boolean xp) { if(xp) XposedBridge.log(tag_the_tag(tag, "W") + msg); Log.w(TAG, msg); }

    @SuppressLint("WrongConstant")
    public static void writeHookFromCache(
            MatrixCursor writeTo,
            String hookId,
            String used,
            String packageName,
            List<String> collections,
            boolean marshall) {
        synchronized (lock) {
            XHook hook = hooks.get(hookId);
            if(hook == null)
                return;

            try {
                if(hook.isAvailable(packageName, collections)) {
                    if(marshall) {
                        Parcel parcel = Parcel.obtain();
                        hook.writeToParcel(parcel, XHookIO.FLAG_WITH_LUA);
                        writeTo.newRow()
                                .add(parcel.marshall())
                                .add(used);
                        parcel.recycle();
                    } else {
                        writeTo.newRow()
                                .add(hook.toJSON())
                                .add(used);
                    }
                }
            }catch (Exception e) {
                Log.e(TAG, Str.fm("Error Writing Hook From Cache! HookId=(%s) Error=%s",
                        hookId,
                        e));
            }
        }
    }
}
