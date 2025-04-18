package eu.faircode.xlua.x.ui.adapters.hooks.elements;

import android.os.Bundle;

import org.json.JSONObject;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.commands.call.PutHookExCommand;

public class ResultRequest {
    public XHook hook;
    public String exception;
    public boolean flag;

    public boolean successful() { return flag; }
    public boolean isValid() { return hook != null && hook.isValid(); }

    public ResultRequest() { }
    public ResultRequest(XHook hook, boolean flag) { this.hook = hook; this.flag = flag; }
    public ResultRequest(XHook hook, boolean flag, String exception) { this.hook = hook; this.flag = flag; this.exception = exception; }
    public static ResultRequest create(XHook hook, boolean res) { return new ResultRequest(hook, res); }
    public static ResultRequest create(XHook hook, String exception) { return new ResultRequest(hook, false, exception); }

    public static ResultRequest create(Bundle r, boolean fromResult) {
        ResultRequest result = new ResultRequest();
        result.fromBundle(r, fromResult);
        return result;
    }

    public ResultRequest fromBundle(Bundle b, boolean fromResultOrIgnoreNullData) {
        if(b == null) {
            flag = false;
            exception = "Null Bundle Result";
        } else {
            if(!b.containsKey(PutHookExCommand.FIELD_FLAG)) {
                flag = false;
                exception = "Missing Flag!";
                return this;
            } else {
                flag = b.getBoolean(PutHookExCommand.FIELD_FLAG);
                if(!flag) {
                    exception = b.getString(PutHookExCommand.FIELD_EXCEPTION);
                    if(!Str.isEmpty(exception))
                        return this;
                }
            }

            if(b.containsKey(PutHookExCommand.FIELD_DATA)) {
                try {
                    hook = XHook.create();
                    hook.fromJSONObject(new JSONObject(b.getString(PutHookExCommand.FIELD_DATA)));
                }catch (Exception e) {
                    if(!fromResultOrIgnoreNullData) {
                        exception = "Failed to Serialize Hook Object! Error=" + e.toString();
                        flag = false;
                    }
                }
            } else {
                flag = false;
                exception = "Missing Hook Data!";
            }
        }

        return this;
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        try {
            b.putBoolean(PutHookExCommand.FIELD_FLAG, flag);
            b.putString(PutHookExCommand.FIELD_EXCEPTION, exception);
            if(hook == null || !hook.isValid()) b.putString(PutHookExCommand.FIELD_DATA, null);
            else b.putString(PutHookExCommand.FIELD_DATA, hook.toJSON());
        }catch (Exception ignored) { }
        return b;
    }
}