package eu.faircode.xlua.x.hook.interceptors.ipc.holders;

//    android.net.Uri uri,
//    String method,
//    String arg,
//    android.os.Bundle extras

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.StrConversionUtils;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.hook.filter.kinds.IPCCallFilterContainer;
import eu.faircode.xlua.x.xlua.LibUtil;

public class IntentCallData {
    public static IntentCallData create(XParam param, boolean getResult) { return new IntentCallData(param, getResult); }

    private static final String TAG = LibUtil.generateTag(IntentCallData.class);

    public static final String SETTINGS_AUTH = "settings";
    public static final String GET_SECURE_METHOD = "GET_secure";
    public static final String BUNDLE_ARG = "value";

    private boolean forceIfNull = false;

    public Uri uri;
    public String authority;
    public String method;
    public String arg;
    public Bundle extras;
    public Bundle result;
    public boolean isDirectReturnNoBundle = false;

    private boolean isValid = false;

    public boolean hasAuthority() { return !TextUtils.isEmpty(authority); }
    public boolean hasMethod() { return !TextUtils.isEmpty(method); }
    public boolean hasArg() { return !TextUtils.isEmpty(arg); }
    public boolean hasResult() { return result != null; }

    public boolean isSettingsAuthority() { return hasAuthority() && authority.contains(SETTINGS_AUTH); }
    public boolean isGetSetting() { return hasMethod() && method.toLowerCase().startsWith("get_"); }

    public void setForceIfNull(boolean forceIfNull) { this.forceIfNull = forceIfNull; }
    public boolean getForceIfNull() { return this.forceIfNull; }

    public IntentCallData(XParam param, boolean getResult) {
        try {
            Object pOne = param.tryGetArgument(0, null);
            if(pOne instanceof ContentResolver) {
                //ASSUME this is part of the Setting.getString function
                this.arg = param.tryGetArgument(1, null);
                if(this.arg != null) {
                    this.isDirectReturnNoBundle = true;
                    this.authority = SETTINGS_AUTH;
                    this.uri = Uri.parse(this.authority);
                    this.method = GET_SECURE_METHOD;
                    this.extras = null;
                    if(getResult) {
                        Object res = param.tryGetResult(null);
                        if(res != null) {
                            this.isValid = true;
                            this.result = new Bundle();
                            TryRun.handle(() -> {
                                if(res instanceof Integer) result.putInt(BUNDLE_ARG, (int)res);
                                else if(res instanceof Long) result.putLong(BUNDLE_ARG, (long)res);
                                else if(res instanceof Short) result.putShort(BUNDLE_ARG, (short)res);
                                else if(res instanceof Byte) result.putByte(BUNDLE_ARG, (byte) res);
                                else if(res instanceof Character) result.putChar(BUNDLE_ARG, (char) res);
                                else if(res instanceof Boolean) result.putBoolean(BUNDLE_ARG, (boolean)res);
                                else if(res instanceof String) result.putString(BUNDLE_ARG, (String)res);
                            }, (e) -> {
                                Log.e(TAG, Str.fm("Error Putting Bundle Result! POne Type [%s] POneVal [%s] Res [%s] Res Type [%s] this[%s] error=%s",
                                        Str.toObjectClassNameNonNull(pOne),
                                        Str.toStringOrNull(pOne),
                                        Str.toObjectClassNameNonNull(res),
                                        Str.toStringOrNull(res),
                                        toString(false),
                                        e)); });
                        }
                    } else {
                        this.isValid = true;
                    }
                }
            } else {
                if(pOne instanceof Uri)  {
                    this.uri = (Uri) pOne;
                    this.authority = this.uri.getAuthority();
                    if(Str.isEmpty(this.authority)) this.authority = this.uri.getPath();
                }
                else if(pOne instanceof String) {
                    this.authority = (String) pOne;
                    this.uri = Uri.parse(this.authority);
                }

                if(pOne != null && !Str.isEmpty(this.authority)) {
                    this.method = param.tryGetArgument(1, null);

                    //ToDo: Move this else where perhaps the XProxyContent ?
                    if(Str.isEmpty(this.method) || Str.areEqualAny(this.method, "xlua", "mock")) {
                        if(DebugUtil.isDebug())
                            Log.d(TAG, Str.fm("Call Is Empty Method (%s) or Contains XLUA or MOCK, Authority=%s",
                                    Str.toStringOrNull(this.method),
                                    this.authority));
                        return;
                    }

                    this.arg = param.tryGetArgument(2, null);
                    this.extras = param.tryGetArgument(3, null);
                    if(Str.isEmpty(this.arg)) {
                        //We can take query possibly or extras ?
                        if(DebugUtil.isDebug())
                            Log.d(TAG, Str.fm("Argument for Call Is Empty or Null [%s] Authority (%s)",
                                    Str.toStringOrNull(this.arg),
                                    Str.toStringOrNull(this.authority)));
                        return;
                    }

                    this.isValid = true;
                    if(getResult)
                        this.result = param.tryGetResult(null);

                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Finished Parsing Content Resolver Call, Get Result=%s this[%s]",
                                getResult,
                                toString(false)));
                } else {
                    Log.e(TAG, Str.fm("Intent Call, Param at Index (0) or the First is not a [URI] or [STRING] or NULL, Error Type=%s Value=%s This[%s]",
                            Str.toObjectClassNameNonNull(pOne),
                            Str.toStringOrNull(pOne),
                            Str.toStringOrNull(toString(true))));
                }
            }
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Error in Constructor<IntentCallData> This[%s] Error=%s",
                    toString(false),
                    e));
        }
    }

    public boolean replaceSettingStringResult(XParam param) {
        if(!this.isValid || this.result == null)
            return false;

        try {
            if(isSettingsAuthority()) {
                if(!isGetSetting()) {
                    if (DebugUtil.isDebug()) Log.d(TAG, "Ignoring Settings as we are looking for [GET_secure] " + this);
                    return false;
                }
            }

            //Should we continue as well if there is no args ? we can see
            String ourAuth = Str.toLowerCase(Str.getNonNullOrEmptyString(this.authority, Str.ASTERISK));
            String newValue = param.resolveIpcSetting(this.arg, ourAuth, true, true);
            if(!forceIfNull) {
                if(newValue == null) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Failed to Find Arg [%s] New Value [NULL] From Auth [%s] This[%s]",
                                this.arg,
                                ourAuth,
                                toString(false)));
                    return false;
                }
            }

            String oldValue = replace(newValue); //Set this Flag to be Dynamic with the System
            if(!forceIfNull) {
                if(oldValue == null) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Failed to Find Arg [%s] New Value [%s] Old Value [NULL] From Auth [%s] This[%s]",
                                this.arg,
                                newValue,
                                ourAuth,
                                toString(false)));

                    return false;
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Replaced Arg [%s] old Value of [%s] with the new Value [%s] from Auth [%s] This[%s]",
                        this.arg,
                        oldValue,
                        newValue,
                        ourAuth,
                        toString(false)));

            if(this.result != null && !Str.areEqual(newValue, oldValue, false, false)) {
                param.setLogOld(Str.toStringOrNull(oldValue));
                param.setLogNew(Str.toStringOrNull(newValue));
                param.setLogExtra(Str.combineEx(IPCCallFilterContainer.createCallSetting(param.getLastSetting(), authority)));
                param.setResult(this.isDirectReturnNoBundle ? this.result.get(BUNDLE_ARG) : this.result);
                return true;
            }

            return false;
        }catch (Throwable e) {
            Log.e(TAG, "Error Replacing Result: [" + toString(false) + "] Error: " + e);
            return false;
        }
    }

    public String replace(String newValue) {
        return TryRun.getOrDefault(() -> {
            if(this.result == null || !this.isValid) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Not Forcing Call (bad result or is not valid) New Value [%s] Force [%s] This[%s]",
                            newValue,
                            forceIfNull,
                            toString(true)));
                return null;
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("New Value [%s] Force [%s] This [%s]",
                        newValue,
                        forceIfNull,
                        toString(false)));

            //returns old
            String newValueSafe = Str.getNonNullString(newValue, Str.EMPTY);
            //Finds the "value" field or the field that reflects its name
            boolean hasValueField = result.containsKey(BUNDLE_ARG);
            String key = hasValueField ? BUNDLE_ARG : this.arg;
            Object value = result.containsKey(key) ? result.get(key) : null;
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Starting Setting for Fake Values [%s] Force [%s] New Value Safe [%s] HasValueField=[%s] Key: [%s] Value: [%s] This[%s]",
                        newValue,
                        forceIfNull,
                        newValueSafe,
                        hasValueField,
                        key,
                        Str.toStringOrNull(value),
                        toString(false)));

            TryRun.handle(() -> {
                if(value instanceof Integer) result.putInt(key, StrConversionUtils.tryParseInt(newValueSafe));
                else if(value instanceof Long) result.putLong(key, StrConversionUtils.tryParseLong(newValueSafe));
                else if(value instanceof Short) result.putShort(key, StrConversionUtils.tryParseShort(newValueSafe));
                else if(value instanceof Byte) result.putByte(key, StrConversionUtils.tryParseByte(newValueSafe));
                else if(value instanceof Character) result.putChar(key, Str.isEmpty(newValueSafe) ? '\0' : newValueSafe.charAt(0));
                else if(value instanceof Boolean) result.putBoolean(key, StrConversionUtils.tryParseBoolean(newValueSafe));
                else if(value instanceof String) {
                    result.putString(key, newValueSafe);
                } else {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Forcing [%s] Type [%s] to String, Cant Find its Handler Type to place Fake Result [%s] Key [%s]",
                                forceIfNull,
                                Str.toObjectClassNameNonNull(value),
                                newValueSafe,
                                key));

                    if(forceIfNull) {
                        result.putString(key, newValueSafe);
                    }
                }
            }, (e) -> Log.e(TAG, Str.fm("Error Putting Fake Result [%s] This[%s] Error=$s",
                    newValueSafe,
                    toString(false),
                    e)));

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Updated Key:%s From Call [%s] is a Type:%s value%s New Value:%s New Value Safe:%s",
                        key,
                        toString(false),
                        Str.toObjectClassNameNonNull(value),
                        Str.toStringOrNull(value),
                        Str.toStringOrNull(newValue),
                        Str.toStringOrNull(newValueSafe)));

            return String.valueOf(value);
        }, null);
    }

    public String toString(boolean useNewLine) {
        StrBuilder sb = StrBuilder.create().ensureDelimiter(useNewLine ? Str.NEW_LINE : Str.WHITE_SPACE);
        sb.append(Str.fm("Method: [%s]", Str.toStringOrNull(this.method)));
        sb.append(Str.fm("Authority: [%s]", Str.toStringOrNull(this.authority)));
        sb.append(Str.fm("Arg: [%s]", Str.toStringOrNull(this.arg)));
        sb.append(Str.fm("Result: [%s]", Str.toStringOrNull(this.result)));
        sb.append(Str.fm("Is Direct Return: [%s] Is Valid [%s]", this.isDirectReturnNoBundle, this.isValid));

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
