package eu.faircode.xlua.x.ui.adapters.hooks.elements;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.hooks.XHookUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.hook.filter.kinds.FileFilterContainer;
import eu.faircode.xlua.x.hook.filter.kinds.IPCBinderFilterContainer;
import eu.faircode.xlua.x.hook.filter.kinds.IPCCallFilterContainer;
import eu.faircode.xlua.x.hook.filter.kinds.IPCQueryFilterContainer;
import eu.faircode.xlua.x.hook.filter.kinds.ShellFilterContainer;
import eu.faircode.xlua.x.ui.core.interfaces.IDiffFace;

//CopyConstructorMissesField

@SuppressWarnings("unused")
public class XHook extends XHookBase implements IDiffFace {
    public static final String DEFAULT_SCRIPT = "function before(hook, param) end";
    public static final List<String> SPECIAL_FILTERS = Arrays.asList(
            FileFilterContainer.GROUP_NAME,
            IPCCallFilterContainer.GROUP_NAME,
            IPCBinderFilterContainer.GROUP_NAME,
            IPCQueryFilterContainer.GROUP_NAME,
            ShellFilterContainer.GROUP_NAME);

    public static XHook fromId(String id) { XHook hook = new XHook(); hook.internalId = id; return hook; }

    public static XHook copy(XHook from) { return new XHook(from); }
    public static XHook create() { return new XHook(); }
    public static XHook create(Parcel p) { return new XHook(p); }
    public static XHook create(JSONObject j) { return new XHook(j); }
    public static XHook create(Bundle b) { return new XHook(b); }
    public static XHook create(Cursor c) { return new XHook(c); }
    public static XHook create(String s) { return new XHook(s);  }

    public boolean isBuiltIn() { return this.builtin != null && this.builtin; }

    public String getClassName() { return Str.getNonNullOrEmptyString(this.resolvedClassName, this.className); }

    public boolean isDefaultScript() { return Str.areEqualsAnyIgnoreCase(this.luaScript, DEFAULT_SCRIPT) || Str.isEmpty(this.luaScript); }


    public boolean isGroup(String group) { return this.group != null && this.group.equalsIgnoreCase(group);  }

    public String getName() { return name; }
    public String getGroup() { return group; }
    public String getAuthor() { return author; }

    public XHook() { }
    public XHook(XHook from) { XHookIO.copy(from, this); }
    public XHook(Parcel p) { XHookIO.fromParcel(this, p); }
    public XHook(Bundle b) { populateFromBundle(b); }
    public XHook(JSONObject j) { XHookIO.fromJson(this, j); }
    public XHook(Cursor c) { fromCursor(c); }
    public XHook(String s) { fromJSONString(s); }

    public XHook resolveClass(Context context) {
        if(context != null) this.resolvedClassName = getResolvedClassName(context);
        return this;
    }

    public XHook setIsBuiltIn(boolean givenFlag) {
        if(givenFlag && (this.builtin == null || !this.builtin)) this.builtin = true;
        return this;
    }

    public XHook ensureValidLuaScript(String apk) {
        if(Str.isEmpty(this.luaScript)) this.luaScript = DEFAULT_SCRIPT;
        else this.luaScript = Str.getNonNullOrEmptyString(XHookJsonUtils.getLuaScript(apk, this.luaScript), this.luaScript);
        return this;
    }

    public boolean isAvailable(int versionCode) { return (versionCode >= this.minApk && versionCode <= maxApk); }
    public boolean isAvailable(Collection<String> collections) { return isAvailable(null, collections); }
    public boolean isAvailable(String packageName, Collection<String> collections) { return isAvailable(packageName, collections, false); }
    public boolean isAvailable(String packageName, Collection<String> collections, boolean ignoreDisabled) { return isAvailable(packageName, collections, ignoreDisabled, false); }
    public boolean isAvailable(String packageName, Collection<String> collections, boolean ignoreDisabled, boolean skipCollectionCheck) {
        if(!skipCollectionCheck)
            if(!ListUtil.isValid(collections) || !isValid() || !collections.contains(this.collection))
                return false;

        if(!ignoreDisabled && !Boolean.TRUE.equals(this.enabled))
            if(Str.isEmpty(this.group) || !SPECIAL_FILTERS.contains(this.group))
                return false;

        if(Build.VERSION.SDK_INT < this.minSdk || Build.VERSION.SDK_INT > this.maxSdk)
            return false;

        if(!Str.isEmpty(packageName)) {
            for(String target : this.targetPackages)
                if(Pattern.matches(target, packageName))
                    return true;
            for(String exclude : this.excludePackages)
                if(Pattern.matches(exclude, packageName))
                    return false;
        }

        return true;
    }

    public static final Parcelable.Creator<XHook> CREATOR = new Parcelable.Creator<XHook>() {
        @Override
        public XHook createFromParcel(Parcel source) { return new XHook(source); }
        @Override
        public XHook[] newArray(int size) { return new XHook[size]; }
    };

    @Override
    public boolean areItemsTheSame(IDiffFace newItem) {
        if(newItem instanceof XHookBase) return this.getObjectId().equalsIgnoreCase(((XHookBase) newItem).getObjectId());
        return false;
    }

    @Override
    public boolean areContentsTheSame(IDiffFace newItem) {
        if (!(newItem instanceof XHookBase)) return false;
        XHookBase other = (XHookBase)newItem;
        return Str.areEqual(this.name,           other.name,         true, true)
                && Str.areEqual(this.description,    other.description,  true, true)
                && Str.areEqual(this.group,          other.group,        true, true)
                && Str.areEqual(this.collection,     other.collection,   true, true)
                && Str.areEqual(this.luaScript,      other.luaScript,    true, true)
                && Str.areEqual(this.author,         other.author,       true, true)
                && Str.areEqual(this.className,      other.className,    true, true)
                && Str.areEqual(this.methodName,     other.methodName,   true, true)
                && Str.areEqual(this.returnType,     other.returnType,   true, true)
                && Objects.equals(this.version,      other.version)
                && Objects.equals(this.minApk,       other.minApk)
                && Objects.equals(this.maxApk,       other.maxApk)
                && Objects.equals(this.minSdk,       other.minSdk)
                && Objects.equals(this.maxSdk,       other.maxSdk)
                && Objects.equals(this.builtin,      other.builtin)
                && Objects.equals(this.enabled,      other.enabled)
                && Objects.equals(this.notify,       other.notify)
                && Objects.equals(this.optional,     other.optional)
                && this.parameterTypes.equals(other.parameterTypes)
                && this.excludePackages.equals(other.excludePackages)
                && this.targetPackages.equals(other.targetPackages)
                && this.settings.equals(other.settings);
    }
}
