package eu.faircode.xlua.x.xlua.database.updaters;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XLegacyCore;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.JsonHelperEx;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.XposedUtility;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;
import eu.faircode.xlua.x.xlua.hook.AssignmentPacket;
import eu.faircode.xlua.x.xlua.interfaces.ICursorType;
import eu.faircode.xlua.x.xlua.interfaces.IJsonType;
import eu.faircode.xlua.x.xlua.settings.SettingReMappedItem;
import eu.faircode.xlua.x.xlua.settings.data.SettingInfoPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;

//Builder then use factory for caller ?
public class DatabaseUpdater<T extends IIdentifiableObject & ICursorType & IJsonType> {

    private static final String TAG = "XLua.DatabaseUpdater";
    private static final String TAG_PUSH = "XLua.DatabaseUpdater(push)";
    private static final String TAG_ENSURE_TABLE = "XLua.DatabaseUpdater(ensureTableIsUpdated)";

    public static <T extends IIdentifiableObject & ICursorType & IJsonType> DatabaseUpdater<T> create() { return new DatabaseUpdater<>(); }


    public static interface IContentValues<T extends IIdentifiableObject & ICursorType> {
        ContentValues getContentValues(T o);
    }

    public static final String DELETE_PREFIX = "__delete";

    private final WeakHashMap<String, T> all = new WeakHashMap<>();
    private final WeakHashMap<String, Pair<T, String>> push = new WeakHashMap<>();

    private TableInfo table;
    private Class<T> clazz;
    private SQLDatabase database;
    private boolean shouldContinue = true;

    public DatabaseUpdater<T> clearMaps(boolean resetContinue) {
        if(resetContinue)
            this.shouldContinue = true;

        this.all.clear();
        this.push.clear();
        return this;
    }

    public DatabaseUpdater<T> setTable(TableInfo table) {
        this.table = table;
        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, "Set Table=" + Str.toStringOrNull(table));

        return this;
    }

    public DatabaseUpdater<T> clearAll() {
        this.all.clear();
        return this;
    }

    public DatabaseUpdater<T> clearPush() {
        this.push.clear();
        return this;
    }

    public DatabaseUpdater<T> setDatabase(SQLDatabase database) {
        this.database = database;
        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, "Set Database=" + Str.toStringOrNull(database));

        return this;
    }

    public DatabaseUpdater<T> setContinue(boolean shouldContinue) {
        this.shouldContinue = shouldContinue;
        return this;
    }

    public DatabaseUpdater<T> continueIfItemsNotEmpty() {
        this.shouldContinue = ListUtil.size(this.all) > 0;
        return this;
    }

    public DatabaseUpdater<T> setClazz(Class<T> clazz) {
        this.clazz = clazz;
        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, "Set Clazz=" + Str.toStringOrNull(clazz));
        return this;
    }

    public DatabaseUpdater<T> closeDatabase() {
        if(this.database != null)
            this.database.close();

        return this;
    }

    public DatabaseUpdater<T> setDatabaseIfExists(Context context, String databaseName) {
        if(!ObjectUtils.anyNull(context, databaseName)) {
            SQLDatabase db = new SQLDatabase(databaseName, context, false);
            if(db.exists()) {
                this.database = db;
            }
        }

        this.shouldContinue = this.database != null;
        return this;
    }

    public String getInfoPrefix() { return getInfoPrefix(table); }
    public String getInfoPrefix(TableInfo tableInfo) {
        tableInfo = tableInfo == null ? table : tableInfo;
        return Str.fm("All Count=%s  Push Count=%s  Database=%s  TableInfo=%s", this.all.size(), this.push.size(), this.database.file.getAbsolutePath(), tableInfo == null ? "null" : tableInfo.name);
    }

    public DatabaseUpdater<T> ensureTableExists() { return ensureTableExists(table); }
    public DatabaseUpdater<T> ensureTableExists(TableInfo tableInfo) {
        if(shouldContinue && !ObjectUtils.anyNull(tableInfo, database) && database.isOpen(true))
            this.shouldContinue = DatabaseHelpEx.prepareDatabase(database, tableInfo);

        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, "Ensure if Table Exists, Should Continue=" + this.shouldContinue + " " + getInfoPrefix(tableInfo));

        return this;
    }

    public DatabaseUpdater<T> ensureTableIsUpdated(boolean dropOldTableIfOutdated) { return ensureTableIsUpdated(table, dropOldTableIfOutdated); }
    public DatabaseUpdater<T> ensureTableIsUpdated(TableInfo tableInfo, boolean dropOldTableIfOutdated) {
        if(shouldContinue && !ObjectUtils.anyNull(database, tableInfo) && database.isOpen(true)) {
            int old = this.push.size();
            if(!database.doColumnNamesMatch(tableInfo.name, tableInfo.columns.keySet())) {
                sendRemainingToPush();
                XposedUtility.logI_xposed(TAG_ENSURE_TABLE, "Table Columns do not align!\n" + tableInfo);
                if(dropOldTableIfOutdated) {
                    database.beginTransaction(true);
                    database.endTransaction(true, database.dropTable(tableInfo.name));
                }
            }

            if(DebugUtil.isDebug())
                XposedUtility.logD_xposed(TAG_ENSURE_TABLE, Str.fm("Finished Ensuring Table matches columns, old push=%s  %s", old, getInfoPrefix(tableInfo)));
        }

        return this;
    }

    public DatabaseUpdater<T> ensureMatchesJson(String defaultsJson, Context context) { return ensureMatchesJson(table, defaultsJson, context, clazz); }
    public DatabaseUpdater<T> ensureMatchesJson(String defaultsJson, Context context, Class<T> clazz) { return ensureMatchesJson(table, defaultsJson, context, clazz); }
    public DatabaseUpdater<T> ensureMatchesJson(TableInfo tableInfo, String defaultsJson, Context context, Class<T> clazz) {
        if(shouldContinue && !ObjectUtils.anyNull(database, tableInfo) && database.isOpen(true)) {
            if(!DatabaseHelpEx.prepareDatabaseLocked(database, tableInfo))
                return this;

            //How Much of this are we using given we have a "IDMapper" Class ? Supposedly suppose to do the same thing ?
            if(defaultsJson != null) {
                List<T> jsonElements = JsonHelperEx.findJsonElementsFromAssets(XUtil.getApk(context), defaultsJson, true, clazz);
                if(DebugUtil.isDebug())
                    XposedUtility.logD_xposed(TAG, Str.fm("Got JSON Elements Count=(%s) JSON Name=(%s) Push Count=(%s) Table Info Prefix=(%s)",
                            ListUtil.size(jsonElements),
                            defaultsJson,
                            this.push.size(),
                            Str.ensureNoDoubleNewLines(getInfoPrefix(tableInfo))));

                //[2] Create a Local Map of all the Elements from the DB that Need to be Updated because of the cause (NEW ID) (Needs to Update their IDs)
                //      Map<NEW_ID, OLD_ITEM>
                Map<String, T> toUpdates = new HashMap<>();
                for(Map.Entry<String, Pair<T, String >> entry : this.push.entrySet()) {
                    //Create a MAP of the Ones in Need of An Update for their IDs Map<NEW_ID, OLD_ITEM>
                    Pair<T, String> pair = entry.getValue();
                    T item = pair.first;
                    String newId = pair.second;
                    if(!Str.isEmpty(newId)) toUpdates.put(newId, item);
                }

                //[3] Iterate through all the JSON Elements
                for(T jsonElement : jsonElements) {
                    //[3.1] using the JSON Element ID (Newest since its from JSON) try to Get the DB Object
                    //      Only Gets if Exits in the Local Map of Need to Update due to Outdated ID
                    T up = toUpdates.get(jsonElement.getObjectId());
                    if(up != null) {
                        if(up.consumeObject(jsonElement))
                            if(DebugUtil.isDebug())
                                XLegacyCore.logD(TAG,
                                        Str.fm("Consumed JSON Element (%s) to the DB Element (%s) in the Push (NEW_IDS) QUE of Count (%s) Global (%s)",
                                                jsonElement.getObjectId(),
                                                up.getObjectId(),
                                                toUpdates.size(),
                                                this.push.size()));
                    } else {
                        //[3.2] If not in the Already Push QUE for NEW IDs, then Check if it is in the Push Que At All for Any Reason
                        Pair<T, String> pItem = push.get(jsonElement.getObjectId());
                        if(pItem != null) {
                            if(pItem.first.consumeObject(jsonElement))
                                if(DebugUtil.isDebug())
                                    XLegacyCore.logD(TAG, Str.fm("Consumed JSON Element (%s) to the DB Element (%s) from the Push QUE of Count (%s)",
                                            jsonElement.getObjectId(),
                                            pItem.first.getObjectId(),
                                            this.push.size()));
                        } else {
                            //[3.3] Not in Push QUE for NEW IDs, Not in the Global Push QUE, now Check the (All) Global List
                            //ToDo: Support Deleting, and also should we Remove it from All ? No I dont think so ALL is all
                            T item = all.get(jsonElement.getObjectId());
                            if(item == null)
                                push.put(jsonElement.getObjectId(), Pair.create(jsonElement, null));
                            else if(item.consumeObject(jsonElement)) {
                                push.put(item.getObjectId(), Pair.create(item, null));
                                if(DebugUtil.isDebug())
                                    XLegacyCore.logD(TAG, Str.fm("Consumed JSON Element (%s) to the DB Element (%s) from the (All) list, Pushed Count (%s) All Count (%s)",
                                            jsonElement.getObjectId(),
                                            item.getObjectId(),
                                            this.push.size(),
                                            this.all.size()));
                            } else {
                                try {
                                    String jsonOne = item.toJSONString();
                                    String jsonTwo = jsonElement.toJSONString();
                                    if(jsonTwo != null) {
                                        if(!jsonTwo.equals(jsonOne)) {
                                            push.put(jsonElement.getObjectId(), Pair.create(jsonElement, null));
                                            if(DebugUtil.isDebug())
                                                XLegacyCore.logD(TAG, Str.fm("Consumed JSON Element (%s) to DB Element (%s) from (All) list, Reason (BAD_JSON_COMPARE) Pushed Count (%s) All Count (%s)",
                                                        jsonElement.getObjectId(),
                                                        item.getObjectId(),
                                                        this.push.size(),
                                                        this.all.size()));
                                        }
                                    }
                                }catch (Exception ignored) { }
                            }
                        }
                    }
                }

                if(DebugUtil.isDebug())
                    XLegacyCore.logD(TAG, Str.fm("Finished Ensuring (%s) Elements Match JSON Elements from Assets (%s), to Push Count=%s",
                            this.all.size(),
                            ListUtil.size(jsonElements),
                            this.push.size()));
            }
        }

        return this;
    }

    public DatabaseUpdater<T> pushUpdates(IContentValues<T> ic) { return pushUpdates(table, ic); }
    public DatabaseUpdater<T> pushUpdates(TableInfo toTable, IContentValues<T> ic) {
        if(shouldContinue && !ObjectUtils.anyNull(database, toTable, ic) && database.isOpen(true)) {
            if(!DatabaseHelpEx.prepareDatabaseLocked(database, toTable)) {
                XposedUtility.logE_xposed(TAG_PUSH, "Failed to prepare Database for push!");
                return this;
            }

            if(DebugUtil.isDebug())
                XLegacyCore.logD(TAG,
                        "Starting Database Push Update for (%s)" +
                                getInfoPrefix(toTable));

            if(!push.isEmpty()) {
                int successCount = 0;
                try {
                    if(!database.beginTransaction(true)) {
                        XposedUtility.logE_xposed(TAG_PUSH, "Failed to Begin Database Transaction for push!");
                        return this;
                    }

                    for(String id : new ArrayList<>(push.keySet())) {
                        Pair<T, String> pair = push.remove(id);
                        if(pair == null || (pair.first == null))
                            continue;

                        T item = pair.first;
                        String newId = pair.second;
                        SQLSnake query = item.createSnake();
                        if(query == null)
                            continue;

                        if(!Str.isEmpty(newId)) {
                            if(newId.startsWith(DELETE_PREFIX)) {
                                if(database.delete(toTable.name, query.getWhereClause(), query.getWhereArgs())) {
                                    this.all.remove(id);
                                    successCount++;
                                }
                                else {
                                    this.push.put(id, pair);
                                    if(DebugUtil.isDebug())
                                        XLegacyCore.logD(TAG, Str.fm("Failed to Delete Item (%s) Where Clause (%s) Where Args (%s) Table (%s) Database (%s)",
                                                id,
                                                query.getWhereClause(),
                                                Str.joinArray(query.getWhereArgs()),
                                                toTable.name,
                                                database.toString()));
                                }
                            } else {
                                item.setId(newId);
                                ContentValues contentValues = ic.getContentValues(item);
                                if(!database.update(toTable.name, contentValues, query)) {
                                    if(!database.delete(toTable.name, query.getWhereClause(), query.getWhereArgs())) {
                                        item.setId(id);
                                        this.push.put(id, pair);
                                        if(DebugUtil.isDebug())
                                            XLegacyCore.logD(TAG, Str.fm("Failed to Delete Item (%s) For Reason (NEW_ID) (%s) Content Values (%s) Where Clause (%s) Where Args (%s) Table (%s) Database (%s)",
                                                    id,
                                                    newId,
                                                    Str.toStringOrNull(contentValues),
                                                    query.getWhereClause(),
                                                    Str.joinArray(query.getWhereArgs()),
                                                    toTable.name,
                                                    database.toString()));
                                    } else {
                                        this.all.remove(id);
                                        if(database.insert(toTable.name, contentValues)) {
                                            this.all.put(newId, item);
                                            successCount++;
                                        }
                                        else {
                                            item.setId(id);
                                            this.push.put(id, pair);
                                            if(DebugUtil.isDebug())
                                                XLegacyCore.logD(TAG, Str.fm("Failed to Push Insert Item (%s) For Reason (NEW_ID) (%s) Content Values (%s) Where Clause (%s) Where Args (%s) Table (%s) Database (%s)",
                                                        id,
                                                        newId,
                                                        Str.toStringOrNull(contentValues),
                                                        query.getWhereClause(),
                                                        Str.joinArray(query.getWhereArgs()),
                                                        toTable.name,
                                                        database.toString()));
                                        }
                                    }
                                } else {
                                    this.all.remove(id);
                                    this.all.put(newId, item);
                                    successCount++;
                                }
                            }
                        } else {
                            ContentValues contentValues = ic.getContentValues(item);
                            if(database.insert(toTable.name, contentValues)) {
                                this.all.put(id, item);
                                successCount++;
                                if(DebugUtil.isDebug())
                                    XLegacyCore.logD(TAG, Str.fm("Updated Item (%s) Content Values (%s) Table (%s)",
                                            id,
                                            Str.toStringOrNull(contentValues),
                                            toTable.name));
                            }
                             else {
                                this.push.put(id, pair);
                                if(DebugUtil.isDebug())
                                    XLegacyCore.logD(TAG, Str.fm("Failed to Push Insert Item by Id (%s) Content Values (%s) Table (%s) Database (%s)",
                                            id,
                                            Str.toStringOrNull(contentValues),
                                            toTable.name,
                                            database.toString()));
                            }
                        }
                    }

                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction(true, false);
                    if(DebugUtil.isDebug())
                        XLegacyCore.logD(TAG, Str.fm("Finished Pushing Updates! For Table (%s) Database (%s) All Count (%s) Push Count (%s) Success Count=%s",
                                toTable.name,
                                database.toString(),
                                all.size(),
                                push.size(),
                                successCount));
                }
            }
        }

        return this;
    }

    public DatabaseUpdater<T> sendRemainingToPush() {
        if(shouldContinue) {
            if(!this.all.isEmpty()) {
                for(Map.Entry<String, T> entry : all.entrySet()) {
                    String id = entry.getKey();
                    T item = entry.getValue();
                    if(!push.containsKey(id))
                        push.put(id, Pair.create(item, null));
                }
            }
        }

        return this;
    }

    public DatabaseUpdater<T> continueIfTableExists(String tableName) { return continueIfTableExists(tableName, -1, false); }
    public DatabaseUpdater<T> continueIfTableExists(String tableName, int minimumItemCount, boolean dropIfLessThanMinimum) {
        if(database == null)
            this.shouldContinue = false;
        else {
            if(!database.isOpen(true)) {
                this.shouldContinue = false;
                return this;
            }

            if(!database.hasTable(tableName))
                this.shouldContinue = false;
            else if(minimumItemCount > -1) {
                int count = database.tableEntries(tableName);
                this.shouldContinue = count >= minimumItemCount;
                if(dropIfLessThanMinimum && !this.shouldContinue) {
                    database.beginTransaction(true);
                    database.endTransaction(true, database.dropTable(tableName));
                }
            }
        }

        return this;
    }
    public DatabaseUpdater<T> getItems(boolean compressUIDs, boolean dropTableIfExists) { return getItems(clazz, table.name, compressUIDs, dropTableIfExists); }
    public DatabaseUpdater<T> getItems(String tableName, boolean compressUIDs, boolean dropTableIfExists) { return getItems(clazz, tableName, compressUIDs, dropTableIfExists); }
    public DatabaseUpdater<T> getItems(Class<T> clazz, String tableName, boolean compressUIDs, boolean dropTableIfExists) {
        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG, "Getting Items, do=" + shouldContinue + " " + getInfoPrefix());

        //This can cause user related issues
        //If multiple users, then the ID key will be written
        //ToDo: High Priority
        if(shouldContinue && !ObjectUtils.anyNull(clazz, tableName, database) && database.isOpen(true)) {
            List<T> items = compressUIDs ?
                    UpdateUtils.getUidDatabaseEntries(database, tableName, clazz, dropTableIfExists) :
                    DatabaseHelpEx.getFromDatabase(database, tableName, clazz, true);

            for(T item : items)
                this.all.put(item.getObjectId(), item);

            if(DebugUtil.isDebug())
                XposedUtility.logD_xposed(TAG, "Got All Items, Size=" + this.all.size() + " Table Name=" + tableName);
        }

        return this;
    }

    public DatabaseUpdater<T> ensureItemsMapped(Context context, String jsonIdFile) {
        if(shouldContinue && !ObjectUtils.anyNull(context, jsonIdFile)) {
            if(!this.all.isEmpty()) {
                LinkedHashMap<String, T> mapFull = new LinkedHashMap<>();
                for(Map.Entry<String, T> entry : this.all.entrySet()) {
                    String id = entry.getKey();
                    T item = entry.getValue();

                    //This is to check make sure it is not already being pushed or updated at least its ID
                    Pair<T, String> pair = this.push.get(id);
                    if(pair == null || Str.isEmpty(pair.second))
                        mapFull.put(id, item);
                }

                if(DebugUtil.isDebug())
                    XposedUtility.logD_xposed(TAG, "Ensure Mapping, Full Size=" + mapFull.size() + " " + getInfoPrefix());

                //Got to make sure if something is already getting updated don't push
                //Should not matter actually should be fine
                for(Map.Entry<String, Pair<T, String>> entry : this.push.entrySet()) {
                    Pair<T, String > pair = entry.getValue();
                    T item = pair.first;
                    String newId = pair.second;
                    if(Str.isEmpty(newId) && !mapFull.containsKey(entry.getKey()))
                        mapFull.put(entry.getKey(), item);
                }

                if(DebugUtil.isDebug())
                    XposedUtility.logD_xposed(TAG, "Ensure Mapping done enumerating push, Full Size=" + mapFull.size() + " " + getInfoPrefix());

                if(ListUtil.isValid(mapFull)) {
                    Map<T, String> map = UpdateUtils.remapItems(context, mapFull.values(), jsonIdFile);
                    if(ListUtil.isValid(map)) {
                        for(Map.Entry<T, String> entry : map.entrySet()) {
                            T item = entry.getKey();
                            String newId = entry.getValue();
                            String oldId = item.getObjectId();
                            if(push.containsKey(oldId))
                                if(DebugUtil.isDebug())
                                    XposedUtility.logD_xposed(TAG, "Push already Contains the ID item: " + oldId);

                            if(!Str.isEmpty(newId)) {
                                push.put(oldId, Pair.create(item, newId));
                            }
                        }
                    }
                }

                if(DebugUtil.isDebug())
                    XposedUtility.logD_xposed(TAG, "Ensure Mapping finished, " + getInfoPrefix());

            }
        }

        return this;
    }

    /*

    public DatabaseUpdater<T> ensureItemsMapped(Context context, String jsonIdFile) {
        if(shouldContinue && !ObjectUtils.anyNull(context, jsonIdFile, this.items)) {
            this.map = UpdateUtils.remapItems(context, this.items, jsonIdFile);
            this.same.clear();
            this.needsUpdate.clear();
            if(ListUtil.isValid(this.map)) {
                for(Map.Entry<T, String> entry : this.map.entrySet()) {
                    T item = entry.getKey();
                    String newId = entry.getValue();

                    if(newId != null && !newId.isEmpty())
                        needsUpdate.put(item, newId);
                    else {
                        same.add(item);
                    }
                }
            }
        }

        return this;
    }

    public DatabaseUpdater<T> pushSame(TableInfo toTable, IContentValues<T> ic) {
        if(DatabaseHelpEx.prepareDatabaseLocked(database, toTable)) {
            if(ListUtil.size(this.same) > 0 && ic != null) {
                if(!database.beginTransaction(true)) {
                    XposedUtility.logE_xposed(TAG, "Failed to begin Transaction [pushSame]");
                    return this;
                }

                List<T> sameCopy = new ArrayList<>(this.same);
                ListUtil.clear(this.same);
                try {
                    for(T item : sameCopy) {
                        ContentValues cv = ic.getContentValues(item);
                        if(!database.insert(toTable.name, cv))
                            this.same.add(item);
                    }

                    database.setTransactionSuccessful();
                }finally {
                    database.endTransaction(true, false);
                    if(DebugUtil.isDebug())
                        XposedUtility.logD_xposed(TAG, Str.fm("Same Sized after push=%s  Before push=%s  Items=%s  Map=%s", ListUtil.size(this.same), sameCopy.size(), ListUtil.size(this.items), ListUtil.size(this.map)));

                }
            }
        }

        return this;
    }

    public DatabaseUpdater<T> pushUpdated(TableInfo toTable, IContentValues<T> ic) {
        if(DatabaseHelpEx.prepareDatabaseLocked(database, toTable)) {
            if(ListUtil.size(this.needsUpdate) > 0 && ic != null) {
                if(!database.beginTransaction(true)) {
                    XposedUtility.logE_xposed(TAG, "Failed to begin Transaction [pushUpdated]");
                    return this;
                }

                WeakHashMap<T, String> updateCopy = new WeakHashMap<>(this.needsUpdate);
                ListUtil.clear(this.needsUpdate);
                try {
                    for(Map.Entry<T, String> entry : updateCopy.entrySet()) {
                        T item = entry.getKey();
                        String newId = entry.getValue();
                        if(item == null || TextUtils.isEmpty(newId))
                            continue;

                        String oldId = item.getId();
                        item.setId(newId);

                        ContentValues cv = ic.getContentValues(item);
                        if(!database.insert(toTable.name, cv)) {
                            item.setId(oldId);
                            this.needsUpdate.put(item, newId);
                        }
                    }

                    database.setTransactionSuccessful();
                }finally {
                    database.endTransaction(true, false);
                    if(DebugUtil.isDebug())
                        XposedUtility.logD_xposed(TAG, Str.fm("Updated Sized after push=%s  Before push=%s  Items=%s  Map=%s", ListUtil.size(this.needsUpdate), updateCopy.size(), ListUtil.size(this.items), ListUtil.size(this.map)));
                }
            }
        }

        return this;
    }*/

    public static boolean ensureUpdated(Context context, SQLDatabase database) {
        if(database.isOpen(true)) {
            if(DebugUtil.isDebug())
                XposedUtility.logI_xposed(TAG, "Starting Update Checker, " + database.file.getAbsolutePath() + " name=" + database.name + " IsOpen=" + database.isOpen(true) + " IsXLua=" + database.isXLua());

            if(database.isXLua()) {
                try {
                    //Just have it so the main fucking legacy item has a function like "readFromOld(Cursor c)" or something
                    //And have a function "ensureTableIsMoved(From, To)

                    /* Init Settings, Convert old to New / Re Map Names */
                    /*new DatabaseUpdater<SettingReMappedItem.Setting_legacy>()
                            .setDatabase(database)
                            .setClazz(SettingReMappedItem.Setting_legacy.class)
                            .continueIfTableExists(SettingReMappedItem.Setting_legacy.OLD_TABLE_NAME, 1, true)
                            .getItems(SettingReMappedItem.Setting_legacy.OLD_TABLE_NAME, true, true)
                            .ensureItemsMapped(context, SettingReMappedItem.Setting_legacy.JSON)
                            .sendRemainingToPush()
                            .setTable(SettingPacket.TABLE_INFO)
                            .pushUpdates(SettingReMappedItem.Setting_legacy::toContentValuesFromLegacy)
                            .clearMaps(true)
                            .ensureTableExists()
                            .getItems(false, false)
                            .ensureTableIsUpdated(true)
                            .ensureItemsMapped(context, SettingReMappedItem.Setting_legacy.JSON)
                            .pushUpdates(SettingReMappedItem.Setting_legacy::toContentValuesFromNew);*/


                    //Ensure IDs are Updated
                    new IDMapper<SettingReMappedItem.Setting_legacy>()
                            .setDatabase(database)
                            .setTable(SettingPacket.TABLE_INFO)
                            .getItems(SettingReMappedItem.Setting_legacy.class, false, false)
                            .remapToJson(context, SettingReMappedItem.Setting_legacy.JSON, SettingReMappedItem.Setting_legacy::toContentValuesFromNew);


                    /*new DatabaseUpdater<SettingInfoPacket>()
                            .setDatabase(database)
                            .setClazz(SettingInfoPacket.class)
                            .setTable(SettingInfoPacket.TABLE_INFO)
                            .ensureTableExists()
                            .getItems(SettingInfoPacket.TABLE_NAME, false, true)    //Should we drop ?
                            .ensureItemsMapped(context, SettingReMappedItem.Setting_legacy.JSON)
                            .ensureTableExists()
                            .pushUpdates(SettingInfoPacket::toContentValues);*/

                    /* Assignment => Assignments */
                    //Fix this, we have to use "package"
                    //This is broken it drops new Assignments Table
                    //You need to re do this fucking updater fuck this Updater Class we will use things like "ColumnSimpleReMapper" etc
                    //This class is ACTUALLY banned from being used,
                    /*new DatabaseUpdater<AssignmentLegacy>()
                            .setDatabase(database)
                            .setClazz(AssignmentLegacy.class)
                            .setTable(AssignmentLegacy.TABLE_INFO)
                            .continueIfTableExists(AssignmentLegacy.TABLE_NAME, 1, true)
                            .getItems(true, true)
                            .ensureTableExists(AssignmentPacket.TABLE_INFO)
                            .pushUpdates(AssignmentPacket.TABLE_INFO, (o) -> {
                                AssignmentPacket packet = new AssignmentPacket(o);
                                return packet.toContentValues();
                            });*/

                    ColumnSimpleReMapper.create()
                            .setDatabase(database)
                            .createMap(context)
                            .ensureIsUpdated(AssignmentPacket.TABLE_INFO);

                    ColumnSimpleReMapper.create()
                            .setDatabase(database)
                            .createMap(context)
                            .ensureIsUpdated(SettingInfoPacket.TABLE_INFO);

                    /* Init Settings Defaults */
                    new DatabaseUpdater<SettingInfoPacket>()
                            .setDatabase(database)
                            .setClazz(SettingInfoPacket.class)
                            .setTable(SettingInfoPacket.TABLE_INFO)
                            .ensureTableExists()
                            .getItems(false, false)
                            .ensureMatchesJson(SettingInfoPacket.JSON, context)
                            .ensureTableIsUpdated(true)
                            .pushUpdates(SettingInfoPacket::toContentValues);

                    new IDMapper<SettingInfoPacket>()
                            .setDatabase(database)
                            .setTable(SettingInfoPacket.TABLE_INFO)
                            .getItems(SettingInfoPacket.class, false, false)
                            .remapToJson(context, SettingInfoPacket.REMAP_JSON, SettingInfoPacket::toContentValues);

                }catch (Exception e) {
                    XposedUtility.logE_xposed(TAG, "Error Updating= error:" + e);
                }
            }
        }

        return true;
    }
}
