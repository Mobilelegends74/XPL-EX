package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import eu.faircode.xlua.XLegacyCore;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.utilities.JSONUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.commands.query.GetHooksCommand;

public class GetHookCommand extends CallCommandHandlerEx {

    public static final String FIELD = "hook";
    public static final String COMMAND_NAME = "getHook";

    public GetHookCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
        this.requiresSingleThread = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        return BundleUtil.createSingleString(
                FIELD,
                XLegacyCore.getHookAsJsonString(commandData.getExtraString(FIELD)));
    }

    public static XHook getEx(Context context, String hookId) {
        if(Str.isEmpty(hookId))
            return null;

        XHook hook = XHook.create();
        Bundle res = XProxyContent.luaCall(context, COMMAND_NAME, BundleUtil.createSingleString(FIELD, hookId));
        if(res == null)
            return null;

        String jsonData = res.getString(FIELD);
        if(Str.isEmpty(jsonData))
            return null;

        hook.fromJSONString(jsonData);
        return hook;
    }
}