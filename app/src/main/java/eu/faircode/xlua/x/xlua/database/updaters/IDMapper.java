package eu.faircode.xlua.x.xlua.database.updaters;

import android.content.ContentValues;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XLegacyCore;
import eu.faircode.xlua.XUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.JsonHelperEx;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.ObjectUtils;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.XposedUtility;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;
import eu.faircode.xlua.x.xlua.database.TableInfo;
import eu.faircode.xlua.x.xlua.database.sql.SQLDatabase;
import eu.faircode.xlua.x.xlua.database.sql.SQLSnake;
import eu.faircode.xlua.x.xlua.interfaces.ICursorType;

/** @noinspection UnusedReturnValue*/
public class IDMapper<T extends IIdentifiableObject & ICursorType> {
    private static final String TAG = LibUtil.generateTag(IDMapper.class);


    public interface IContentValues<T extends IIdentifiableObject & ICursorType> {  ContentValues getContentValues(T o); }

    public static final String DELETE_PREFIX = "__delete";

    private SQLDatabase database;
    private TableInfo tableInfo;
    private int total = 0;
    private final Map<String, String> idetifiersMap = new HashMap<>();
    private final Map<String, List<T>> everything = new HashMap<>();

    public boolean hasOutdatedId(String id) { return idetifiersMap.containsKey(id); }

    public void put(T item) {
        if(item != null) {
            String id = item.getObjectId();
            List<T> items = everything.get(id);
            if(items == null) {
                items = new ArrayList<>(1);
                items.add(item);
                total++;
                everything.put(id, items);
            } else {
                if(!items.contains(item)) {
                    items.add(item);
                    total++;
                }
            }
        }
    }

    public IDMapper<T> setTable(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
        return this;
    }

    public IDMapper<T> setDatabase(SQLDatabase database) {
        this.database = database;
        return this;
    }

    public IDMapper<T> getItems(Class<T> clazz, boolean compressUIDs, boolean dropTableIfExists) {
        if(DebugUtil.isDebug())
            XposedUtility.logD_xposed(TAG,
                    Str.fm("Getting Items in Database [%s] from Table [%s] Compress UIDs=%s Drop If Table Exists=%s",
                    Str.noNL(database.toString()),
                    tableInfo.name,
                    compressUIDs,
                    dropTableIfExists));

        if(!ObjectUtils.anyNull(clazz, tableInfo.name, database) && database.isOpen(true)) {
            List<T> items = compressUIDs ?
                    UpdateUtils.getUidDatabaseEntries(database, tableInfo.name, clazz, dropTableIfExists) :
                    DatabaseHelpEx.getFromDatabase(database, tableInfo.name, clazz, true);

            for(T item : items)
                put(item);

            if(DebugUtil.isDebug())
                XposedUtility.logD_xposed(TAG,
                        Str.fm("Got Items, Total=%s Groups Count=%s",
                        this.total,
                        this.everything.size()));
        }

        return this;
    }

    public IDMapper<T> initIdentifiersMap(Context context, String json) {
        idetifiersMap.clear();
        List<UpdateMapEntry> jsonEntries = JsonHelperEx.findJsonElementsFromAssets(
                XUtil.getApk(context),
                json,
                true,
                UpdateMapEntry.class);

        if(DebugUtil.isDebug())
            XLegacyCore.logD(TAG, Str.fm("Parsing (%s) Json Elements for a Identifiers Map! from Json (%s)",
                    ListUtil.size(jsonEntries),
                    json));

        if(ListUtil.isValid(jsonEntries)) {
            for(UpdateMapEntry entry : jsonEntries)
                for(String old : entry.oldIds)
                    idetifiersMap.put(old, entry.id);
        }

        if (DebugUtil.isDebug())
            XLegacyCore.logD(TAG, Str.fm("Finished Parsing Json (%s) to Identifiers Map of Count (%s) from Json Count (%s)",
                    json,
                    idetifiersMap.size(),
                    ListUtil.size(jsonEntries)));
        return this;
    }

    public IDMapper<T> remapToJson(Context context, String json, IContentValues<T> ic) {
        if(ObjectUtils.anyNull(context, json, tableInfo, ic, database)) {
            XposedUtility.logE_xposed(TAG, "(2) Something is null find out fucker....");
            return this;
        }

        if(!database.isOpen(true)) {
            XposedUtility.logE_xposed(TAG, "(2) Failed to open Database...");
            return this;
        }

        initIdentifiersMap(context, json);
        int failed = 0;
        int skipped = 0;
        int updated = 0;
        try {
            if(!DatabaseHelpEx.prepareDatabase(database, tableInfo)) {
                XposedUtility.logE_xposed(TAG, Str.fm("Failed to Prepare Table [%s] JSON [%s]", tableInfo.name, json));
                return this;
            }

            database.beginTransaction(true);
            for(String id : new ArrayList<>(this.everything.keySet())) {
                if(hasOutdatedId(id)) {
                    List<T> items = this.everything.remove(id);
                    String newId = idetifiersMap.get(id);
                    if(!ListUtil.isValid(items) || Str.isEmpty(newId) || Str.areEqual(id, newId, false))
                        continue;

                    if(DebugUtil.isDebug())
                        XLegacyCore.logD(TAG, Str.fm("Re-Naming (%s) Items from Old Id (%s) to New Id (%s)",
                                items.size(),
                                id,
                                newId));

                    for(T item : items) {
                        SQLSnake snake = item.createSnake();
                        if(snake == null)
                            continue;

                        if(newId.startsWith(DELETE_PREFIX)) {
                            if(database.delete(tableInfo.name, snake.getWhereClause(), snake.getWhereArgs())) {
                                updated++;
                                if(DebugUtil.isDebug())
                                    XLegacyCore.logD(TAG, Str.fm("Successfully Deleted Item (%s) From Table (%s)",
                                            id,
                                            tableInfo.name));
                            } else {
                                put(item);
                                failed++;
                                if(DebugUtil.isDebug())
                                    XLegacyCore.logD(TAG, Str.fm("Failed to Delete Item (%s) Where Clause (%s) Where Args (%s) Table (%s) Database (%s)",
                                            id,
                                            snake.getWhereClause(),
                                            Str.joinArray(snake.getWhereArgs()),
                                            tableInfo.name,
                                            database.toString()));
                            }
                        } else {
                            item.setId(newId);
                            ContentValues contentValues = ic.getContentValues(item);
                            if(database.update(tableInfo.name, contentValues, snake)) {
                                updated++;
                                put(item);
                                if(DebugUtil.isDebug())
                                    XLegacyCore.logD(TAG, Str.fm("Successfully Updated Item from Id (%s) to new Id (%s) Table (%s)",
                                            id,
                                            newId,
                                            tableInfo.name));
                            } else {
                                if(!database.delete(tableInfo.name, snake.getWhereClause(), snake.getWhereArgs())) {
                                    item.setId(id);
                                    failed++;
                                    put(item);
                                    if(DebugUtil.isDebug())
                                        XLegacyCore.logD(TAG, Str.fm("Failed to Delete Item (%s) to Replace with new Id (%s) Where Clause (%s) Where Args (%s) Table (%s) Database (%s)",
                                                id,
                                                newId,
                                                snake.getWhereClause(),
                                                Str.joinArray(snake.getWhereArgs()),
                                                tableInfo.name,
                                                database.toString()));
                                } else {
                                    if(database.insert(tableInfo.name, contentValues)) {
                                        updated++;
                                        put(item);
                                        if(DebugUtil.isDebug())
                                            XLegacyCore.logD(TAG, Str.fm("Successfully Updated Item from Id (%s) to new Id (%s) Table (%s) using DELETE First then Insert Method!",
                                                    id,
                                                    newId,
                                                    tableInfo.name));
                                    } else {
                                        //item.setId(id);
                                        failed++;
                                        //Is In Limbo the original was Delete...
                                        if(DebugUtil.isDebug())
                                            XLegacyCore.logD(TAG, Str.fm("Failed to Update Item from Id (%s) to new Id (%s) Where Clause (%s) Where Args (%s) Content Values (%s) Table (%s) Database (%s)",
                                                    id,
                                                    newId,
                                                    snake.getWhereClause(),
                                                    Str.joinArray(snake.getWhereArgs()),
                                                    Str.toStringOrNull(contentValues),
                                                    tableInfo.name,
                                                    database.toString()));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            database.setTransactionSuccessful();
        }catch (Exception e) {
            XposedUtility.logE_xposed(TAG, Str.fm("Failed to Re Map via JSON [%s] Table [%s] Updated Count=%s Error=%s",
                    json,
                    tableInfo.name,
                    updated,
                    e));
        }finally {
            database.endTransaction(true, false);
            if(DebugUtil.isDebug())
                XposedUtility.logD_xposed(TAG, Str.fm("Returning from attempt update from JSON [%s] Table [%s] Updated Count=%s Failed Count=%s Skipped Count=%s",
                        json,
                        tableInfo.name,
                        updated,
                        failed,
                        skipped));
        }


        return this;
    }
}
