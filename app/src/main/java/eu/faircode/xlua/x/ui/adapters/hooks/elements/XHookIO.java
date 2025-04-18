package eu.faircode.xlua.x.ui.adapters.hooks.elements;

import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

@SuppressWarnings("all")
public class XHookIO {
    private static final String TAG = LibUtil.generateTag(XHookIO.class);

    private static boolean supportLegacy = false;
    
    public final static int FLAG_WITH_LUA = 2; // =PARCELABLE_ELIDE_DUPLICATES

    public static int getInteger(JSONObject j, String f, int defaultValue) { return j.has(f) ? j.optInt(f, defaultValue) : defaultValue; }
    public static Integer getInteger(JSONObject j, String f) { return j.has(f) ? j.optInt(f, 0) : null; }
    public static boolean getBoolean(JSONObject j, String f, boolean defaultValue) { return j.has(f) ? j.optBoolean(f, defaultValue) : defaultValue; }
    public static Boolean getBoolean(JSONObject j, String f) { return j.has(f) ? j.optBoolean(f, false) : null; }

    //public static List<XHookBase> fromJsonArrayString(String jsonArrayString) { return ListUtil.emptyList(); }
    //public static XHookBase fromJsonString(XHookBase to, String jsonString) { return TryRun.getOrDefault(() -> XHook.create(new JSONObject(jsonString)), to); }

    public static String toJsonString(XHookBase from) { return toJsonString(from, 2); }
    public static String toJsonString(XHookBase from, int indentSpaces) {
        return TryRun.getOrDefault(() -> {
            JSONObject jRoot = new JSONObject();
            toJson(from, jRoot);
            return jRoot.toString(indentSpaces);
        }, Str.EMPTY);
    }

    public static void fromJson(XHookBase to, String jsonData) {
        if(to != null && !Str.isEmpty(jsonData)) {
            TryRun.silent(() -> {
                JSONObject jRoot = new JSONObject(jsonData);
                fromJson(to, jRoot);
            });
        }
    }

    public static void fromJson(XHookBase to, JSONObject json) {
        if(to != null && json != null) {
            try {
                to.builtin = getBoolean(json, "builtin", false);
                to.collection = json.optString("collection");
                to.group = json.optString("group");
                to.name = json.optString("name");
                to.author = json.optString("author");
                to.version = getInteger(json, "version", 0);
                to.description = json.optString("description");
                to.className = json.optString("className");
                to.resolvedClassName = json.optString("resolvedClassName");
                to.methodName = json.optString("methodName");

                JSONArray jParam = json.optJSONArray("parameterTypes");
                if(jParam != null) {
                    for(int i = 0; i < jParam.length(); i++) {
                        String s = jParam.getString(i);
                        if(s != null)
                            to.parameterTypes.add(s);
                    }
                }

                to.returnType = json.optString("returnType");

                to.minSdk = getInteger(json, "minSdk", 0);
                to.maxSdk = getInteger(json, "maxSdk", 999);

                to.minApk = getInteger(json, "minApk", 0);
                to.maxApk = getInteger(json, "maxApk", Integer.MAX_VALUE);

                ListUtil.addAll(to.excludePackages, Str.split(json.optString("excludePackages"), Str.COMMA, true, true));

                to.enabled = getBoolean (json, "enabled", true);
                to.optional = getBoolean(json, "optional", false);
                to.usage = getBoolean(json, "usage", true);
                to.notify = getBoolean(json, "notify", false);

                to.luaScript = json.optString("luaScript");

                JSONArray jSettings = json.optJSONArray("settings");
                if(jSettings != null) {
                    for(int i = 0; i < jSettings.length(); i++) {
                        String s = jSettings.getString(i);
                        if(s != null)
                            to.settings.add(s);
                    }
                }

                JSONArray jTargets = json.optJSONArray("targetPackages");
                if(jTargets != null) {
                    for(int i = 0; i < jTargets.length(); i++) {
                        String s = jTargets.getString(i);
                        if(s != null)
                            to.targetPackages.add(s);
                    }
                }
            }catch (Exception e) {
                Log.e(TAG, "Error Converting from JSON! Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            }
        }
    }

    public static void toJson(XHookBase from, JSONObject json) {
        if(from != null && json != null) {
            try {
                json.put("builtin", Boolean.TRUE.equals(from.builtin));
                json.put("collection", from.collection);
                json.put("group", from.group);
                json.put("name", from.name);
                json.put("author", from.author);
                json.put("version", Math.max(0, from.version));
                if (from.description != null)
                    json.put("description", from.description);

                json.put("className", from.className);
                if (from.resolvedClassName != null)
                    json.put("resolvedClassName", from.resolvedClassName);

                if (from.methodName != null)
                    json.put("methodName", from.methodName);

                if(!from.parameterTypes.isEmpty()) {
                    JSONArray jParam = new JSONArray();
                    ListUtil.forEachVoidNonNull(from.parameterTypes, (o, i) -> jParam.put(o));
                    json.put("parameterTypes", jParam);
                }

                if (from.returnType != null)
                    json.put("returnType", from.returnType);

                json.put("minSdk", from.minSdk);
                json.put("maxSdk", from.maxSdk);
                json.put("minApk", from.minApk);
                json.put("maxApk", from.maxApk);

                if(!from.excludePackages.isEmpty())
                    json.put("excludePackages", TextUtils.join(Str.COMMA, from.excludePackages)); //Ew

                json.put("enabled", from.enabled);
                json.put("optional", from.optional);
                json.put("usage", from.usage);
                json.put("notify", from.notify);

                json.put("luaScript", from.luaScript);

                if (!from.settings.isEmpty()) {
                    JSONArray jSettings = new JSONArray();
                    ListUtil.forEachVoidNonNull(from.settings, (o, i) -> jSettings.put(o));
                    json.put("settings", jSettings);
                }

                if (!from.targetPackages.isEmpty()) {
                    JSONArray jTargets = new JSONArray();
                    ListUtil.forEachVoidNonNull(from.targetPackages, (o, i) -> jTargets.put(o));
                    json.put("targetPackages", jTargets);
                }
            }catch (Exception e) {
                Log.e(TAG, "Error Converting to JSON! Error=" + e + " Stack=" + RuntimeUtils.getStackTraceSafeString(e));
            }
        }
    }


    public static void toParcel(XHookBase from, Parcel dest, int flags) {
        if(from != null && dest != null) {
            dest.writeByte(Boolean.TRUE.equals(from.builtin) ? (byte) 1 : (byte) 0);
            dest.writeString(from.collection);
            dest.writeString(from.group);
            dest.writeString(from.name);
            dest.writeString(from.author);
            dest.writeInt(Math.max(from.version, 0));
            dest.writeString(from.description);
            dest.writeString(from.className);
            dest.writeString(from.resolvedClassName);
            dest.writeString(from.methodName);
            dest.writeStringArray(ArrayUtils.toArrayNullIfEmpty(from.parameterTypes, String.class));
            dest.writeString(from.returnType);
            dest.writeInt(from.minSdk);
            dest.writeInt(from.maxSdk);
            dest.writeInt(from.minApk);
            dest.writeInt(from.maxApk);
            dest.writeStringArray(ArrayUtils.toArrayNullIfEmpty(from.excludePackages, String.class));
            dest.writeByte(from.enabled ? (byte) 1 : (byte) 0);
            dest.writeByte(from.optional ? (byte) 1 : (byte) 0);
            dest.writeByte(from.usage ? (byte) 1 : (byte) 0);
            dest.writeByte(from.notify ? (byte) 1 : (byte) 0);

            //Check this ?
            if ((flags & FLAG_WITH_LUA) == 0) dest.writeString(null);
            else dest.writeString(from.luaScript);
            dest.writeStringArray(ArrayUtils.toArrayNullIfEmpty(from.settings, String.class));
            if(!supportLegacy) dest.writeStringArray(ArrayUtils.toArrayNullIfEmpty(from.targetPackages, String.class));
        }
    }

    public static void fromParcel(XHookBase to, Parcel in) {
        if(to != null && in != null) {
            to.builtin = (in.readByte() != 0);
            to.collection = in.readString();
            to.group = in.readString();
            to.name = in.readString();
            to.author = in.readString();
            to.version = in.readInt();
            to.description = in.readString();
            to.className = in.readString();
            to.resolvedClassName = in.readString();
            to.methodName = in.readString();
            ListUtil.addAll(to.parameterTypes, in.createStringArray());
            to.returnType = in.readString();
            to.minSdk = in.readInt();
            to.maxSdk = in.readInt();
            to.minApk = in.readInt();
            to.maxApk = in.readInt();
            ListUtil.addAll(to.excludePackages, in.createStringArray());
            to.enabled = (in.readByte() != 0);
            to.optional = (in.readByte() != 0);
            to.usage = (in.readByte() != 0);
            to.notify = (in.readByte() != 0);
            to.luaScript = in.readString();
            ListUtil.addAll(to.settings, in.createStringArray());
            if(!supportLegacy) ListUtil.addAll(to.targetPackages, in.createStringArray());
        }
    }

    public static void copy(XHookBase from, XHookBase to) {
        if(from != null && to != null) {
            to.internalId = from.internalId;
            to.builtin = from.builtin;
            to.collection = from.collection;
            to.group = from.group;
            to.name = from.name;
            to.author = from.author;
            to.version = from.version;
            to.description = from.description;

            to.className = from.className;
            to.resolvedClassName = from.resolvedClassName;
            to.methodName = from.methodName;
            to.parameterTypes.addAll(from.parameterTypes);
            to.returnType = from.returnType;

            to.minSdk = from.minSdk;
            to.maxSdk = from.maxSdk;
            to.minApk = from.minApk;
            to.maxApk = from.maxApk;
            ListUtil.addAll(to.excludePackages, from.excludePackages);
            to.enabled = from.enabled;
            to.optional = from.optional;
            to.usage = from.usage;
            to.notify = from.notify;

            to.luaScript = from.luaScript;
            ListUtil.addAll(to.settings, from.settings);
            ListUtil.addAll(to.targetPackages, from.targetPackages);
        }
    }
}
