package eu.faircode.xlua.x.ui.adapters.hooks.elements;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.api.xstandard.interfaces.IDBSerial;
import eu.faircode.xlua.api.xstandard.interfaces.IJsonSerial;
import eu.faircode.xlua.hooks.XHookUtil;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.interfaces.IValidator;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.IBundleData;
import eu.faircode.xlua.x.xlua.database.IDatabaseEntry;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLQueryBuilder;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;
import eu.faircode.xlua.x.xlua.interfaces.ICursorType;

@SuppressWarnings("unused")
public abstract class XHookBase implements IValidator, IBundleData, IIdentifiableObject, Parcelable, IDatabaseEntry, ICursorType, IJsonSerial, IDBSerial {
    public static final String FIELD_ID = "id";
    public static final String FIELD_DEFINITION = "definition";
    public static final String TABLE_NAME = "hook";
    public static final TableInfo TABLE_INFO = TableInfo.create(TABLE_NAME)
            .putText(FIELD_ID)
            .putText(FIELD_DEFINITION)
            .putPrimaryKey(false, FIELD_ID, FIELD_DEFINITION);

    public XHook asHook() { return (XHook)this; }

    public Boolean builtin = false;
    public String collection;
    public String group;
    public String name;
    public String author;
    public Integer version = 0;
    public String description;

    public String className;
    public String resolvedClassName = null;
    public String methodName;
    public final List<String> parameterTypes = new ArrayList<>();
    public String returnType;

    public int minSdk = 0;
    public int maxSdk = 999;

    public int minApk = 0;
    public int maxApk = Integer.MAX_VALUE;

    public final List<String> excludePackages = new ArrayList<>();
    public Boolean enabled;
    public Boolean optional;
    public Boolean usage;
    public Boolean notify;

    public String luaScript;
    public final List<String> settings = new ArrayList<>();
    public final List<String> targetPackages = new ArrayList<>();

    public String internalId = null;

    public boolean hasSettings() { return ListUtil.isValid(this.settings); }
    public boolean hasParameters() { return ListUtil.isValid(this.parameterTypes); }
    public boolean hasCollection(Collection<String> collections) { return ListUtil.isValid(collections) && !Str.isEmpty(this.collection) && collections.contains(this.collection); }

    public String getResolvedClassName() { return getResolvedClassName(null); }
    public String getResolvedClassName(Context context) {
        if(Str.isEmpty(this.resolvedClassName) && context != null) this.resolvedClassName =  Str.getNonNullOrEmptyString(XHookUtil.resolveClassName(context, this.className), this.className);
        return Str.getNonNullOrEmptyString(this.resolvedClassName, this.className);
    }

    @Override
    public boolean isValid() { return !Str.isEmpty(name) && !Str.isEmpty(collection); }
    public boolean isValid(boolean checkClass, boolean checkGroup) { return isValid() &&  (!checkClass || (!Str.isEmpty(this.className) || !Str.isEmpty(this.resolvedClassName)) && (!checkGroup || !Str.isEmpty(this.group))); }

    @Override
    public String getObjectId() { return Str.isEmpty(this.name) || Str.isEmpty(this.collection)  ? this.internalId : Str.combineEx(this.collection, Str.PERIOD, this.name);  }
    public String getObjectId(boolean tryInternalIdFirst) { return tryInternalIdFirst ? Str.isEmpty(this.internalId) ? getObjectId() : this.internalId : getObjectId(); }

    public void fromJSONString(String s) { TryRun.silent(() -> fromJSONObject(new JSONObject(s))); }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jRoot = new JSONObject();
        XHookIO.toJson(this, jRoot);
        return jRoot;
    }

    @Override
    public String toJSON() throws JSONException { return XHookIO.toJsonString(this); }

    @Override
    public void fromJSONObject(JSONObject obj) throws JSONException { XHookIO.fromJson(this, obj); }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) { XHookIO.toParcel(this, parcel, i); }

    @Override
    public void populateFromBundle(Bundle b) {
        if(b != null) {
            this.internalId = b.getString(FIELD_ID, null);
            fromJSONString(b.getString(FIELD_DEFINITION));
        }
    }

    @Override
    public void populateBundle(Bundle b) {
        if(b != null) {
            b.putString(FIELD_ID, this.getObjectId());
            b.putString(FIELD_DEFINITION, XHookIO.toJsonString(this));
        }
    }

    @Override
    public Bundle toBundle() {
        Bundle b = new Bundle();
        populateBundle(b);
        return b;
    }

    @Override
    public void populateContentValues(ContentValues cv) {
        if(cv != null) {
            cv.put(FIELD_ID, this.getObjectId());
            cv.put(FIELD_DEFINITION, XHookIO.toJsonString(this));
        }
    }

    @Override
    public void populateFromContentValues(ContentValues cv) {
        if(cv != null) {
            this.internalId = cv.getAsString(FIELD_ID);
            XHookIO.fromJson(this, cv.getAsString(FIELD_DEFINITION));
        }
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        populateContentValues(cv);
        return cv;
    }

    @Override
    public void fromCursor(Cursor c) {
        if(c != null) {
            this.internalId = CursorUtil.getString(c, FIELD_ID);
            fromJSONString(CursorUtil.getString(c, FIELD_DEFINITION));
        }
    }

    @Override
    public SQLSnake createSnake() {
        return SQLSnake.create()
                .whereColumn(FIELD_ID, this.getObjectId()).asSnake();
    }

    @Override
    public void populateSnake(SQLQueryBuilder snake) {
        if(snake != null) {
            snake.whereColumn(FIELD_ID, this.getObjectId());
        }
    }

    @Override
    public ContentValues createContentValues() { return toContentValues(); }

    @Override
    public List<ContentValues> createContentValuesList() { return Collections.emptyList(); }

    @Override
    public void fromContentValuesList(List<ContentValues> contentValues) { }

    @Override
    public void fromContentValues(ContentValues contentValue) { populateFromContentValues(contentValue); }

    @Override
    public void fromBundle(Bundle bundle) { populateFromBundle(bundle); }

    @Override
    public void fromParcel(Parcel in) { XHookIO.fromParcel(this, in); }

    @Override
    public int describeContents() { return 0; }

    @Override
    public int hashCode() { return this.getObjectId().hashCode(); }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        String thisId = Str.getNonNullOrEmptyString(this.getObjectId(), this.internalId);
        if(Str.isEmpty(thisId))
            return false;

        if (obj instanceof String) {
            String other = (String) obj;
            return thisId.equalsIgnoreCase(other) || Str.areEqual(this.name, other, true, false);
        }

        if (getClass() != obj.getClass())
            return false;

        XHookBase other = (XHookBase) obj;
        return thisId.equalsIgnoreCase(other.getObjectId()) || Str.areEqual(this.name, other.name, true, false);
    }

    @NonNull
    @Override
    public String toString() { return toString(true, false); }
    public String toString(boolean useNewLine, boolean jsonDump) {
        return !jsonDump ?
                Str.combineEx(this.getObjectId(), "@", this.className, ":", this.methodName) :
                StrBuilder.create().ensureOneNewLinePer(true).ensureDelimiter(useNewLine ? Str.NEW_LINE : Str.WHITE_SPACE)
                    .append(FIELD_ID).append(this.getObjectId())
                    .append(FIELD_DEFINITION).append(XHookIO.toJsonString(this, useNewLine ? 1 : 0))
                    .toString(true);
    }
}
