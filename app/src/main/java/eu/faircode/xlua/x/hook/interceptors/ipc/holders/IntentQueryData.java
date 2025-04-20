package eu.faircode.xlua.x.hook.interceptors.ipc.holders;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.hook.filter.kinds.IPCQueryFilterContainer;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

public class IntentQueryData {
    private static final String TAG = LibUtil.generateTag(IntentQueryData.class);

    /**
     * Interface for handling key hits in cursor scanning
     */
    public interface IFoundHit {
        /**
         * Called when a key match is found to provide replacement value
         * @param keyName The matched key/target name
         * @return The replacement value as a String (return null to skip replacement)
         */
        String onKeyHit(String keyName);

        /**
         * Called when a null/default value is found for a matched key
         * @param keyName The matched key name
         * @return True to replace the null value, false to keep it as is
         */
        default boolean shouldReplace(String keyName) { return true; }
    }

    public Cursor result;
    public Uri uri;
    public String authorityOrPath;
    public final List<String> uriPathSegments = new ArrayList<>();

    public final List<String> projection = new ArrayList<>();
    public final List<String> selectionArgs = new ArrayList<>();

    public final List<String> allArguments = new ArrayList<>();

    private boolean isValid = false;

    public final StringBuilder oldChanges = new StringBuilder();
    public final StringBuilder newChanges = new StringBuilder();

    public boolean hasArgs() { return !allArguments.isEmpty(); }
    public boolean hasResult() { return result != null; }
    public boolean hasSegments() { return !uriPathSegments.isEmpty(); }
    public boolean hasAuthority() { return !Str.isEmpty(authorityOrPath); }
    public boolean hasSelectionArgs() { return !this.selectionArgs.isEmpty(); }

    public IntentQueryData(XParam param, boolean getResult) {
        TryRun.silent(() -> {
            Object arg = param.tryGetArgument(0, null);
            if(!(arg instanceof Uri)) {
                Log.e(TAG, Str.fm("Error Failed to Parse Content Resolver Query Data! First Argument (0) is Type [%s] not Type of (%s)",
                        Str.toObjectClassNameNonNull(arg),
                        Uri.class.getName()));

                return;
            }

            this.uri = (Uri)arg;
            this.authorityOrPath = Str.getNonNullOrEmptyString(uri.getAuthority(), uri.getPath());
            ListUtil.addAll(this.projection, ListUtil.arrayToList(param.tryGetArgument(1, null)));
            if(!this.projection.isEmpty() && Str.startsWithAny(this.projection.get(0), "xlua", "mock")) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Ignoring parsing of Content Resolver Query Data! Projection Shows it is An Internal XLua Query Action! Authority=%s Projection=%s",
                            authorityOrPath,
                            Str.joinList(this.projection)));

                return;
            }

            ListUtil.addAll(this.selectionArgs, ListUtil.arrayToList(param.extractSelectionArgs()));
            ListUtil.addAll(this.uriPathSegments, uri.getPathSegments());
            ListUtil.addAll(this.allArguments, this.uriPathSegments);
            ListUtil.addAll(this.allArguments, this.selectionArgs);

            if(getResult) this.result = param.tryGetResult(null);
            this.isValid = true;
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Finished Creating ContentResolver Query Data Wrapper, get Result=%s this=%s",
                        getResult,
                        toString(false)));
        });
    }

    public boolean intercept(XParam param) {
        if(!isValid) return false;
        try {
            if(!hasAuthority()) {
                Log.e(TAG, Str.fm("Authority is Invalid or Missing from [query] this[%s]", toString(false)));
                return false;
            }

            if(!hasResult()) {
                Log.e(TAG, Str.fm("Authority [%s] Query Request lacks a Result! Skipping:: this[%s]",
                        authorityOrPath,
                        toString(false)));
                return false;
            }

            //ToDo: Improve on this / add better support for wild carding + close enough authorities / auth with paths etc...
            String targetAuth = authorityOrPath;
            String data = param.getSetting(IPCQueryFilterContainer.createAuthoritySetting(targetAuth));
            boolean isEmpty =
                    param.getSettingBool(IPCQueryFilterContainer.createArgSetting(IPCQueryFilterContainer.EMPTY_PREFIX, targetAuth), false);
            //TODO: make sure createArgSetting is Correct
            if(!isEmpty && Str.isEmpty(data) && this.uri != null && ListUtil.isValid(this.uri.getPathSegments())) {
                List<String> segments = this.uri.getPathSegments();
                int i = 0;
                int len = segments.size();
                while (Str.isEmpty(data) && i < len && !isEmpty) {
                    String seg = segments.get(i);
                    if(!Str.isEmpty(seg)) {
                        targetAuth += "/" + seg;
                        String emptyName = IPCQueryFilterContainer.createArgSetting(IPCQueryFilterContainer.EMPTY_PREFIX, targetAuth);
                        isEmpty = param.getSettingBool(emptyName, false);
                        data = param.getSetting(IPCQueryFilterContainer.createAuthoritySetting(targetAuth));
                        if(DebugUtil.isDebug())
                            Log.d(TAG, Str.fm("Tried Auth [%s] with Data Result [%s] Is Empty=[%s] Empty Name=[%s] Index=%s",
                                    targetAuth,
                                    data,
                                    isEmpty,
                                    emptyName,
                                    i));
                    }

                    i++;
                }
            }

            if(isEmpty) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Authority [%s][%s] was Passed the Empty Flag, The Cursor will return a Empty Cursor! Cursor Column Count=[%s] Count=[%s] Column Names=[%s] Position=[%s]",
                            authorityOrPath,
                            targetAuth,
                            this.result.getColumnCount(),
                            this.result.getCount(),
                            Str.joinArray(this.result.getColumnNames()),
                            this.result.getPosition()));

                try {
                    MatrixCursor emptyCursor = new MatrixCursor(this.result.getColumnNames());
                    param.setResult(emptyCursor);
                    param.setLogOld("Count=" + this.result.getCount());
                    param.setLogNew("Empty Count=0");
                    param.setLogExtra(authorityOrPath);
                    return true;
                }catch (Exception e) {
                    param.setResult(null);
                    param.setLogOld("Count=" + this.result.getCount());
                    param.setLogNew("Null Result");
                    param.setLogExtra(authorityOrPath);
                    Log.e(TAG, Str.fm("Failed to Create Empty Cursor for Auth [%s][%s] Error=%s",
                            authorityOrPath,
                            targetAuth,
                            e));

                    return true;
                }

            }

            if(Str.isEmpty(data)) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Authority [%s][%s] is not a Targeted Authority to Intercept and Clear for ContentResolver Query (cursor). Skipping... this[%s]",
                            authorityOrPath,
                            targetAuth,
                            toString(false)));

                return false;
            }

            //ToDO: make sure this works! Split can be wonky with regex and quoted etc
            String[] targets = Str.split(data, Str.PIPE, true);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Authority [%s][%s] From ContentResolver Query will have it's Cursor Result Intercepted and Cleaned for Any Target Filters! this[%s] Target Names [%s]",
                        authorityOrPath,
                        targetAuth,
                        toString(false),
                        Str.joinArray(targets)));

            int pos = this.result.getPosition();

            //We can have this setup a bit different
            //so the Setting Result can Contain the Targets for this Authority "gsf|aid..."
            final String finalTargetAuth = targetAuth;
            Cursor cleanedResult = scanAndCleanCursor(this.result, new IFoundHit() {
                @Override
                public String onKeyHit(String keyName) { return resolveArgumentValue(keyName, finalTargetAuth, param, true); }

                @Override
                public boolean shouldReplace(String keyName) { return shouldForce(keyName, finalTargetAuth, param); }
            }, targets);

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Finished Replacing & Cleaning Cursor From Authority [%s][%s] using Targets [%s] new Result (%s). this[%s] Old Changes=(%s) New Changes=(%s)",
                        authorityOrPath,
                        targetAuth,
                        Str.joinArray(targets),
                        CursorUtil.toString(cleanedResult, false),
                        toString(false),
                        //Could never be too Sure xD
                        Str.ensureNoDoubleNewLines(Str.replaceAll(oldChanges.toString(), Str.NEW_LINE, Str.WHITE_SPACE)),
                        Str.ensureNoDoubleNewLines(Str.replaceAll(newChanges.toString(), Str.NEW_LINE, Str.WHITE_SPACE))));

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("New Cursor Position [%s] and Column Count [%s][%s] and Column Names [%s] Old Position [%s] and Column Count [%s][%s] and Column Names [%s]. this[%s]",
                        cleanedResult.getPosition(),
                        cleanedResult.getColumnCount(),
                        cleanedResult.getCount(),
                        Str.joinArray(cleanedResult.getColumnNames()),
                        pos,
                        this.result.getColumnCount(),
                        this.result.getCount(),
                        Str.joinArray(this.result.getColumnNames()),
                        toString(false)));


            //Set the position back to what is was, most likely (-1) so when ".moveToNext()" is invoked it goes from (-1) to (0)
            cleanedResult.moveToPosition(pos);
            param.setResult(cleanedResult);
            param.setLogOld(oldChanges.toString());
            param.setLogNew(newChanges.toString());
            param.setLogExtra(authorityOrPath);
            return true;
        }catch (Throwable e) {
            Log.e(TAG, Str.fm("Error Intercepting and Clearing ContentResolver Query From [%s] Authority! this[%s] O::[%s] N::[%s] Error=%s Stack=%s",
                    authorityOrPath,
                    toString(false),
                    Str.toStringOrNull(Str.ensureNoDoubleNewLines(Str.replaceAll(Str.toStringOrNull(oldChanges), Str.NEW_LINE, Str.WHITE_SPACE))),
                    Str.toStringOrNull(Str.ensureNoDoubleNewLines(Str.replaceAll(Str.toStringOrNull(newChanges), Str.NEW_LINE, Str.WHITE_SPACE))),
                    e,
                    RuntimeUtils.getStackTraceSafeString(e)));

            return false;
        }
    }

    public boolean shouldForce(String arg, String authority, XParam param) {
        if(Str.isEmpty(arg) || param == null)
            return false;

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Resolving Arg [%s] from Authority [%s] for its Force Value Flag. this[%s]",
                    arg,
                    authority,
                    toString(false)));

        String resolvedSettingName = Str.getNonNullOrEmptyString(
                param.getSetting(IPCQueryFilterContainer.createArgSetting(arg, authority)),
                param.getSetting(IPCQueryFilterContainer.createArgSetting(arg, Str.ASTERISK)));

        if(Str.isEmpty(resolvedSettingName))
            return false;

        boolean isForce = param.isForceSetting(resolvedSettingName);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Resolved Arg [%s] from Authority [%s] had a Null or Default Value. The Resolved Setting [%s] returned [%s] for the Force Flag Check. this[%s]",
                    arg,
                    authority,
                    resolvedSettingName,
                    isForce,
                    toString(false)));

        return isForce;
    }

    public String resolveArgumentValue(String arg, String authority, XParam param, boolean setLastSetting) {
        if(Str.isEmpty(arg) || param == null)
            return null;

        //Haha we do not need to pass authority ? its a instance function ?
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Resolving Arg [%s] from Authority [%s] SetLastSetting=[%s] for Its Value. this[%s]",
                    arg,
                    authority,
                    setLastSetting,
                    toString(false)));

        String resolvedSettingName = Str.getNonNullOrEmptyString(
                param.getSetting(IPCQueryFilterContainer.createArgSetting(arg, authority)),
                param.getSetting(IPCQueryFilterContainer.createArgSetting(arg, Str.ASTERISK)));

        if(Str.isEmpty(resolvedSettingName))
            return null;

        String value = param.getSetting(resolvedSettingName);
        param.setLastSetting(resolvedSettingName, setLastSetting);
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Resolved Arg [%s] from Authority [%s] SetLastSetting=[%s], Resolved to Name [%s] with Value [%s]. this[%s]",
                    arg,
                    authority,
                    setLastSetting,
                    resolvedSettingName,
                    value,
                    toString(false)));
        return value;
    }

    /**
     * Thoroughly scans a cursor for target values and replaces them with values from the callback.
     * Handles various cursor structures, multiple selection args, and different ordering.
     * Preserves original data types for non-target columns.
     * Enhanced to properly handle multiple target columns in the same cursor.
     *
     * @param c The original cursor to scan and process
     * @param onHit Callback to provide replacement values when targets are found
     * @param targetNames Array of target names/keys to look for
     * @return A new MatrixCursor with the appropriate values replaced
     */
    public MatrixCursor scanAndCleanCursor(final Cursor c, IFoundHit onHit, final String... targetNames) {
        try {
            if(c == null || !ArrayUtils.isValid(targetNames) || onHit == null) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Not Scanning Cursor, Cursor, onHit callback or Target Items is Null or Invalid! this[%s]",
                            toString(false)));
                return null;
            }

            // Ensure cursor is valid and has data
            if (c.getCount() <= 0) {
                if(DebugUtil.isDebug()) Log.d(TAG, "Cursor has no rows, nothing to scan");
                return new MatrixCursor(c.getColumnNames());
            }

            if(DebugUtil.isDebug()) {
                Log.d(TAG, Str.fm("Scanning Cursor for Targets: [%s] Columns [%s] Rows [%s]",
                        Str.joinArray(targetNames),
                        Str.joinArray(c.getColumnNames()),
                        c.getCount()));
            }

            // Create a new cursor with same column structure
            MatrixCursor newCursor = new MatrixCursor(c.getColumnNames());

            // Check if we have column names to work with
            if (c.getColumnCount() == 0) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Cursor has no columns, nothing to scan");
                return newCursor;
            }

            // Check for key-value structure (common in settings providers)
            int nameIndex = -1;
            int valueIndex = -1;

            // Try to find name/key and value columns with various common names
            String[] nameColumnOptions = {"name", "key", "setting", "id"};
            String[] valueColumnOptions = {"value", "data", "setting_value"};

            for (String nameOption : nameColumnOptions) {
                nameIndex = c.getColumnIndex(nameOption);
                if (nameIndex != -1) break;
            }

            for (String valueOption : valueColumnOptions) {
                valueIndex = c.getColumnIndex(valueOption);
                if (valueIndex != -1) break;
            }

            // If we have both name and value columns, treat as key-value structure
            if (nameIndex != -1 && valueIndex != -1) {
                if(DebugUtil.isDebug()) {
                    Log.d(TAG, "Found key-value structure. Name column: " + c.getColumnName(nameIndex) + ", Value column: " + c.getColumnName(valueIndex));
                    Log.d(TAG, Str.fm("Key-value structure detected with nameIndex=%s, valueIndex=%s", nameIndex, valueIndex));
                }

                // Make sure cursor is positioned properly
                if (c.moveToFirst()) {
                    do {
                        String name = c.getString(nameIndex);
                        String value = null;

                        // Get the column type safely
                        int valueColumnType;
                        try {
                            valueColumnType = c.getType(valueIndex);
                        } catch (Exception e) {
                            if(DebugUtil.isDebug())
                                Log.d(TAG, "Error getting column type: " + e.getMessage());

                            valueColumnType = -1; // Invalid type as fallback
                        }

                        // Get value as string if the column is a string type
                        if (valueColumnType == Cursor.FIELD_TYPE_STRING) {
                            value = c.getString(valueIndex);
                        } else {
                            // For non-string columns, we'll get a string representation but won't replace it
                            try {
                                value = c.getString(valueIndex);
                            } catch (Exception e) {
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, "Non-string value in column, can't convert to string: " + e.getMessage());
                            }
                        }

                        // Handle null value check
                        boolean isNullOrDefault = value == null || Str.isNullOrDefaultValue(value);

                        // Check if this name/key matches any of our target names
                        boolean isTarget = false;
                        String matchedTargetName = null;

                        for (String targetName : targetNames) {
                            // Handle cases where settings have prefixes with colon (e.g., "secure:android_id")
                            // Also handle case where selection arg matches part of the name/key
                            if ((name != null && name.contains(":") && name.endsWith(targetName)) ||
                                    (name != null && name.equalsIgnoreCase(targetName)) ||
                                    (hasSelectionArgs() && containsIgnoreCase(selectionArgs, targetName))) {

                                isTarget = true;
                                matchedTargetName = targetName;
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, Str.fm("Found target key: %s with value: %s - Matched with target: %s",
                                            name, value, targetName));

                                break;
                            }
                        }

                        // If it's a target, get the replacement value and add the modified row
                        if (isTarget && !Str.isEmpty(matchedTargetName) && valueColumnType == Cursor.FIELD_TYPE_STRING) {
                            // For null values, check if we should replace them
                            if (isNullOrDefault && !onHit.shouldReplace(matchedTargetName)) {
                                if(DebugUtil.isDebug()) {
                                    Log.d(TAG, Str.fm("Found null/default value for target [%s] but shouldReplace returned false - keeping original",
                                            matchedTargetName));
                                }

                                // If not replacing null, copy row as is
                                copyRowWithOriginalTypes(c, newCursor);
                                continue;
                            }

                            // Get replacement value from callback
                            String newValue = onHit.onKeyHit(matchedTargetName);

                            // Skip if replacement value is null
                            if (newValue == null) {
                                if(DebugUtil.isDebug()) {
                                    Log.d(TAG, Str.fm("Replacement value for [%s] is null, skipping replacement", matchedTargetName));
                                }

                                // Copy row as is if replacement value is null
                                copyRowWithOriginalTypes(c, newCursor);
                                continue;
                            }

                            // Add row with replacement value
                            MatrixCursor.RowBuilder row = newCursor.newRow();
                            for (int i = 0; i < c.getColumnCount(); i++) {
                                if (i == valueIndex) {
                                    row.add(newValue);

                                    // Log the changes
                                    oldChanges.append("[" + name + "]:" + value).append("\n");
                                    newChanges.append("[" + name + "]:" + newValue).append("\n");

                                    if(DebugUtil.isDebug()) {
                                        Log.d(TAG, Str.fm("Replacing value in cursor row %s: [%s] = '%s' -> '%s'",
                                                c.getPosition(), name, value, newValue));
                                    }
                                } else {
                                    addColumnValueWithCorrectType(c, row, i);
                                }
                            }
                        } else {
                            // If not a target or not a string column, copy the row as is
                            copyRowWithOriginalTypes(c, newCursor);
                        }
                    } while (c.moveToNext());
                }
            }
            // For non key-value structure, handle target columns directly
            else {
                // Create a map to store all target columns (can be multiple)
                java.util.Map<Integer, String> targetColumnMap = new java.util.HashMap<>();

                // First pass: identify ALL target columns by name
                for (int i = 0; i < c.getColumnCount(); i++) {
                    String columnName = c.getColumnName(i);
                    for (String targetName : targetNames) {
                        if (columnName.equalsIgnoreCase(targetName) ||
                                (columnName.contains(":") && columnName.endsWith(targetName))) {
                            targetColumnMap.put(i, targetName);
                            if(DebugUtil.isDebug())
                                Log.d(TAG, Str.fm("Found target column name: %s at index %s Target: %s",
                                        columnName,
                                        i,
                                        targetName));
                            // Don't break - continue to find all target columns
                        }
                    }
                }

                // If no direct column name matches, check other sources (selection args, URI segments, etc.)
                if (targetColumnMap.isEmpty()) {
                    // Try to find target in selection args
                    if (c.getColumnCount() > 0 && hasSelectionArgs()) {
                        for (String targetName : targetNames) {
                            for (String arg : selectionArgs) {
                                if (arg.equalsIgnoreCase(targetName)) {
                                    // Assume first column is the target since we don't have better info
                                    targetColumnMap.put(0, targetName);
                                    if(DebugUtil.isDebug())
                                        Log.d(TAG, Str.fm("Found target in selection arg: %s (assuming first column)", targetName));
                                    break;
                                }
                            }
                        }
                    }

                    // Check URI path segments
                    if (targetColumnMap.isEmpty() && hasSegments()) {
                        for (String segment : uriPathSegments) {
                            for (String targetName : targetNames) {
                                if (segment.equalsIgnoreCase(targetName)) {
                                    // Assume first column is the target
                                    targetColumnMap.put(0, targetName);
                                    if(DebugUtil.isDebug())
                                        Log.d(TAG, Str.fm("Found target in URI path segment: %s (assuming first column)", targetName));
                                    break;
                                }
                            }
                            if (!targetColumnMap.isEmpty()) break;
                        }
                    }

                    // Check projection
                    if (targetColumnMap.isEmpty() && !projection.isEmpty()) {
                        for (String proj : projection) {
                            for (String targetName : targetNames) {
                                if (proj.equalsIgnoreCase(targetName)) {
                                    // Assume first column is the target
                                    targetColumnMap.put(0, targetName);
                                    if(DebugUtil.isDebug())
                                        Log.d(TAG, Str.fm("Found target in projection: %s (assuming first column)", targetName));
                                    break;
                                }
                            }
                            if (!targetColumnMap.isEmpty()) break;
                        }
                    }
                }

                if(DebugUtil.isDebug()) {
                    if (!targetColumnMap.isEmpty()) {
                        StringBuilder targetInfo = new StringBuilder("Found target columns: ");
                        for (java.util.Map.Entry<Integer, String> entry : targetColumnMap.entrySet()) {
                            targetInfo.append(entry.getValue()).append(" (index ").append(entry.getKey()).append("), ");
                        }
                        Log.d(TAG, targetInfo.toString());
                    } else {
                        Log.d(TAG, "No target columns found in cursor");
                    }
                }

                // Make sure cursor is positioned to verify column types
                boolean cursorHasRows = c.getCount() > 0;
                if (cursorHasRows && !targetColumnMap.isEmpty() && c.moveToFirst()) {
                    // Verify which target columns are strings (we can only replace strings)
                    java.util.Map<Integer, String> verifiedTargets = new java.util.HashMap<>();

                    for (java.util.Map.Entry<Integer, String> entry : targetColumnMap.entrySet()) {
                        int colIndex = entry.getKey();
                        String targetName = entry.getValue();

                        try {
                            boolean isString = c.getType(colIndex) == Cursor.FIELD_TYPE_STRING;
                            if (isString) {
                                verifiedTargets.put(colIndex, targetName);
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, Str.fm("Verified column %s (%s) is a string type",
                                            colIndex,
                                            targetName));
                            } else {
                                if(DebugUtil.isDebug())
                                    Log.d(TAG, Str.fm("Column %s (%s) is not a string type, cannot replace",
                                            colIndex,
                                            targetName));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error checking column type for " + targetName + ": " + e.getMessage());
                        }
                    }

                    // Reset cursor position
                    c.moveToPosition(-1);

                    // Now process the cursor if we have valid string targets
                    if (!verifiedTargets.isEmpty() && c.moveToFirst()) {
                        do {
                            MatrixCursor.RowBuilder row = newCursor.newRow();

                            for (int i = 0; i < c.getColumnCount(); i++) {
                                if (verifiedTargets.containsKey(i)) {
                                    // This is a target column to replace
                                    String targetName = verifiedTargets.get(i);
                                    String oldValue = c.getString(i);
                                    boolean isNullOrDefault = oldValue == null || Str.isNullOrDefaultValue(oldValue);

                                    if (isNullOrDefault && !onHit.shouldReplace(targetName)) {
                                        // Don't replace null/default values if not required
                                        addColumnValueWithCorrectType(c, row, i);
                                        continue;
                                    }

                                    // Get replacement value
                                    String newValue = onHit.onKeyHit(targetName);

                                    if (newValue != null) {
                                        // Add replacement value
                                        row.add(newValue);

                                        // Log the change
                                        oldChanges.append("[" + targetName + "]:" + oldValue).append("\n");
                                        newChanges.append("[" + targetName + "]:" + newValue).append("\n");

                                        if(DebugUtil.isDebug()) {
                                            Log.d(TAG, Str.fm("Replacing value in column %s: [%s] = '%s' -> '%s'",
                                                    i,
                                                    targetName,
                                                    oldValue,
                                                    newValue));
                                        }
                                    } else {
                                        // No replacement value available, keep original
                                        addColumnValueWithCorrectType(c, row, i);
                                    }
                                } else {
                                    // Not a target column, copy as is
                                    addColumnValueWithCorrectType(c, row, i);
                                }
                            }
                        } while (c.moveToNext());
                    } else {
                        // No valid string targets found, copy cursor as is
                        if(DebugUtil.isDebug())
                            Log.d(TAG, "No valid string target columns found, copying cursor as is");

                        if (c.moveToFirst()) {
                            do {
                                copyRowWithOriginalTypes(c, newCursor);
                            } while (c.moveToNext());
                        }
                    }
                } else {
                    // No targets or empty cursor, copy as is if there are rows
                    if(DebugUtil.isDebug())
                        Log.d(TAG, "No targets found or cursor is empty, copying cursor as is");

                    if (cursorHasRows && c.moveToFirst()) {
                        do {
                            copyRowWithOriginalTypes(c, newCursor);
                        } while (c.moveToNext());
                    }
                }
            }

            // Ensure cursor is positioned at the beginning if it has rows
            if (newCursor.getCount() > 0) {
                newCursor.moveToFirst();
            }

            if(DebugUtil.isDebug()) {
                Log.d(TAG, Str.fm("Scan complete - Created new cursor with %s rows", newCursor.getCount()));
                if (!oldChanges.toString().isEmpty()) {
                    Log.d(TAG, Str.fm("Changes summary: %s replacements", oldChanges.toString().split("\n").length));
                    Log.d(TAG, Str.fm("Original values: %s", oldChanges.toString().trim()));
                    Log.d(TAG, Str.fm("New values: %s", newChanges.toString().trim()));
                } else {
                    Log.d(TAG, "No changes were made to cursor values");
                }
            }

            return newCursor;
        } catch (Exception e) {
            Log.e(TAG, "Error Cleaning Query! Error=" + e + " this:" + toString(false));
            try {
                // Create a direct copy of the original cursor in case of error
                MatrixCursor fallbackCursor = new MatrixCursor(c.getColumnNames());
                if (c.getCount() > 0 && c.moveToFirst()) {
                    do {
                        copyRowWithOriginalTypes(c, fallbackCursor);
                    } while (c.moveToNext());
                    // Reset position to beginning
                    fallbackCursor.moveToFirst();
                }
                return fallbackCursor;
            } catch (Exception fallbackEx) {
                Log.e(TAG, "Even fallback cursor creation failed: " + fallbackEx);
                // Last resort: empty cursor with same schema
                return new MatrixCursor(c.getColumnNames());
            }
        }
    }

    /**
     * Helper method to add a column value to a row builder with the correct type
     */
    private void addColumnValueWithCorrectType(Cursor c, MatrixCursor.RowBuilder row, int columnIndex) {
        if (c.isNull(columnIndex)) {
            row.add(null);
            return;
        }

        try {
            switch (c.getType(columnIndex)) {
                case Cursor.FIELD_TYPE_INTEGER:
                    row.add(c.getLong(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    row.add(c.getDouble(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    row.add(c.getString(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    row.add(c.getBlob(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    row.add(null);
                    break;
                default:
                    // Fallback to string if type is unknown
                    row.add(c.getString(columnIndex));
                    break;
            }
        } catch (Exception e) {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Error getting column value: " + e.getMessage() + " - using null");
            row.add(null);
        }
    }

    /**
     * Helper method to copy an entire row with original types
     */
    private void copyRowWithOriginalTypes(Cursor c, MatrixCursor newCursor) {
        MatrixCursor.RowBuilder row = newCursor.newRow();
        for (int i = 0; i < c.getColumnCount(); i++) {
            addColumnValueWithCorrectType(c, row, i);
        }
    }

    /**
     * Helper method to check if a list contains a string (case-insensitive)
     */
    private boolean containsIgnoreCase(List<String> list, String target) {
        if (list == null || target == null) return false;
        for (String item : list) {
            if (item != null && item.equalsIgnoreCase(target)) return true;
        }
        return false;
    }

    //content://com.vivo.vms.IdProvider/IdentifierId/OAID
    //Target Service  like "com.google.gsf.service"
    //Then column Arg like "android_id"
    //Then use setting to replace

    public String toString(boolean useNewLine) {
        StrBuilder sb = StrBuilder.create().ensureDelimiter(useNewLine ? Str.NEW_LINE : Str.WHITE_SPACE);
        sb.append(Str.fm("Projection: [%s]", Str.joinList(this.projection)));
        sb.append(Str.fm("Authority: [%s]", Str.toStringOrNull(this.authorityOrPath)));
        sb.append(Str.fm("All Arguments: [%s]", Str.joinList(this.allArguments)));
        sb.append(Str.fm("Selection Arguments: [%s]", Str.joinList(this.selectionArgs)));
        sb.append(Str.fm("Result: [%s]", Str.toStringOrNull(this.result)));
        sb.append(Str.fm("Is Valid: [%s], Is Cursor Result Null: [%s]", this.isValid, this.result == null));
        //CursorUtils.toString(result, ... ?


        if(this.uri == null)
            sb.append("Uri: null");
        else {
            sb.append(Str.fm("Uri Authority: [%s]", this.uri.getAuthority()));
            sb.append(Str.fm("Uri Host: [%s]", this.uri.getHost()));
            sb.append(Str.fm("Uri Path: [%s]", this.uri.getPath()));
            sb.append(Str.fm("Uri Encoded Path: [%s]", this.uri.getEncodedPath()));
            sb.append(Str.fm("Uri Query: [%s]", this.uri.getQuery()));
            sb.append(Str.fm("Uri Path Segments: [%s]", Str.joinList(this.uri.getPathSegments())));

        }

        return sb.toString(useNewLine);
    }

    @NonNull
    @Override
    public String toString() { return toString(true); }
}
