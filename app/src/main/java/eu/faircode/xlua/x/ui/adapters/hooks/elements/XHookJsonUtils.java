package eu.faircode.xlua.x.ui.adapters.hooks.elements;

import android.content.Context;

import org.json.JSONArray;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XLegacyCore;
import eu.faircode.xlua.utilities.StreamUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

public class XHookJsonUtils {
    private static final String TAG = LibUtil.generateTag(XHookJsonUtils.class);
    public static final String JSON_NAME_HOOK = "hooks.json";

    public static boolean isAsset(String name, String endCompare) { return name.startsWith("assets/") && name.endsWith(endCompare); }



    public static List<XHook> readHooks(Context context, String apk) {
        long startTime = System.currentTimeMillis();
        List<XHook> hooks = new ArrayList<>();
        if(Str.isEmpty(apk))
            return hooks;

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(apk);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            XLegacyCore.logI(TAG, Str.fm("Reading All Apk (%s) JSON [%s] Scripts from Assets to Serialize as Hooks!",
                    apk,
                    JSON_NAME_HOOK));

            int ix = 0;
            while (entries.hasMoreElements()) {
                ZipEntry entry = null;
                try {
                    entry = entries.nextElement();
                    String entryName = entry.getName();
                    if(Str.isEmpty(entryName) || entryName.toLowerCase().contains("__deprecated")) {
                        XLegacyCore.logW(TAG, Str.fm("Skipping Entry... (Empty Name) from (%s) Apk Assets, Current Hook Count=%s i=%s",
                                apk,
                                hooks.size(),
                                ix), false);

                        ix++;
                        continue;
                    }

                    if(isAsset(entryName, JSON_NAME_HOOK)) {
                        int originalCount = hooks.size();
                        List<XHook> parsed = parseHooksJson(apk, entry, entryName, zipFile, context);
                        ListUtil.addAll(hooks, parsed, false);
                        ix++;
                        if(DebugUtil.isDebug())
                            XLegacyCore.logI(TAG, Str.fm("[%s] Entry [%s] From Apk (%s) Assets appears to be a [%s] JSON File! Original Hook Count=(%s), New Total=(%s) Added Count=(%s)",
                                    ix,
                                    entryName,
                                    apk,
                                    JSON_NAME_HOOK,
                                    originalCount,
                                    hooks.size(),
                                    parsed.size()));
                    }
                }catch (Exception e) {
                    XLegacyCore.logE(TAG, Str.fm("Error Reading Apk (%s) Asset Entry (%s) Current Hook Count=%s i=(%s) Error=%s",
                            apk,
                            Str.toStringOrNull(entry),
                            hooks.size(),
                            ix,
                            e));
                }
            }
        }catch (Exception e) {
            XLegacyCore.logE(TAG, Str.fm("Error Reading Hooks From Apk:%s (JSON) Hooks Count=%s Error=%s Stack=%s",
                    apk,
                    hooks.size(),
                    e,
                    RuntimeUtils.getStackTraceSafeString(e)));
        } finally {
            StreamUtil.close(zipFile);
        }

        long endTime = System.currentTimeMillis();
        long elapsedTimeMs = endTime - startTime;
        long elapsedTimeSec = elapsedTimeMs / 1000;

        if(DebugUtil.isDebug())
            XLegacyCore.logD(TAG, Str.fm("Returning (%s) Hooks from Apk (%s) JSON Assets! Finished in %s:%s (Seconds:Milliseconds)",
                    hooks.size(),
                    apk,
                    elapsedTimeSec,
                    elapsedTimeMs));

        return hooks;
    }


    public static List<XHook> parseHooksJson(String apk, ZipEntry entry, String entryName, ZipFile zipFile, Context context) {
        List<XHook> parsed = new ArrayList<>();
        if(ObjectUtils.areAnyNullLog(apk, entry, entryName, zipFile))
            return parsed;

        String path = entryName.substring(0, JSON_NAME_HOOK.length() - 1);
        //boolean isBuiltIn = entryName.endsWith("assets/hooks.json");
        boolean isBuiltIn = true;//Now always true, they are all considered built in
        InputStream inputStream = null;
        try {
            inputStream = zipFile.getInputStream(entry);
            String jsonData = new Scanner(inputStream).useDelimiter("\\A").next();
            if(Str.length(jsonData) < 5)
                return parsed;

            JSONArray jsonArray = new JSONArray(jsonData);
            int length = ArrayUtils.safeLength(jsonArray);
            if(DebugUtil.isDebug())
                XLegacyCore.logD(TAG, Str.fm("Parsing (%s) JSON Array Elements (Hooks) from Asset Entry [%s] from Apk (%s) Is Built In=%s  JsonData Size=%s",
                        length,
                        entryName,
                        apk,
                        isBuiltIn,
                        Str.length(jsonData)));

            if(length > 0) {
                int successful = 0;
                int failed = 0;
                for(int i = 0; i < length; i++) {
                    try {
                        //ToDo: Possibly make sure no Duplicates ?
                        XHook serializedHook = XHook.create(jsonArray.getJSONObject(i)).ensureValidLuaScript(apk).setIsBuiltIn(isBuiltIn).resolveClass(context);
                        parsed.add(serializedHook);
                        successful++;
                    }catch (Exception e) {
                        failed++;
                        XLegacyCore.logE(TAG, Str.fm("Error Parsing Json Element at (%s) From Apk (%s) Asset Entry [%s] (%s::%s) Error=%s",
                                i,
                                apk,
                                entryName,
                                failed,
                                successful,
                                e));
                    }
                }

                if(DebugUtil.isDebug())
                    XLegacyCore.logD(TAG, Str.fm("Finished Reading (%s) JSON Hooks from Entry [%s][%s] within Apk (%s) Parsed Successful Count=%s Failed=%s ",
                            parsed.size(),
                            entryName,
                            path,
                            apk,
                            successful,
                            failed));
            }
        }catch (Exception e) {
            XLegacyCore.logE(TAG, Str.fm("Error Parsing Entry [%s][%s] File From (%s) Assets! Parsed Count=%s Is Built In=%s Error=%s",
                    entryName,
                    path,
                    apk,
                    parsed.size(),
                    isBuiltIn,
                    e));
        } finally {
            StreamUtil.close(inputStream);
        }


        return parsed;
    }

    public static String getLuaScript(String apk, String scriptName) {
        if(Str.isEmpty(apk) || Str.isEmpty(scriptName) || scriptName.length() < 3 || scriptName.length() > 80)
            return scriptName;

        String trimmed = Str.trimOriginal(scriptName);
        if(!trimmed.startsWith("@") || trimmed.length() < 4)
            return scriptName;

        String targetScript = Str.ensureEndsWith(trimmed.substring(1), ".lua");
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(apk);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            if(DebugUtil.isDebug())
                XLegacyCore.logI(TAG, Str.fm("Starting Lua Script Search for Script [%s][%s] from Apk [%s]",
                        scriptName,
                        targetScript,
                        apk));

            while (entries.hasMoreElements()) {
                InputStream inputStream = null;
                try {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if(Str.isEmpty(name))
                        continue;

                    if(!name.toLowerCase().endsWith(targetScript) || name.toLowerCase().contains("__deprecated"))
                        continue;

                    inputStream = zipFile.getInputStream(entry);
                    return new Scanner(inputStream).useDelimiter("\\A").next(); //Check if its empty ? if so keep going ?
                }catch (Exception ignored) {
                } finally {
                    StreamUtil.close(inputStream);
                }
            }
        }catch (Exception e) {
            XLegacyCore.logE(TAG, Str.fm("Error Finding [%s] Lua Script From (%s) Apk! Error=%e",
                    scriptName,
                    apk,
                    e));
        } finally {
            StreamUtil.close(zipFile);
        }

        return null;
    }
}
