package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;

import eu.faircode.xlua.ModuleIdentity;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;

/**
 * ToDo: Make use of this, using this can tell if the Bridge has been Updated aka Device ReBoot
 */
public class GetVersionExCommand extends CallCommandHandlerEx {
    public static final String COMMAND_NAME = "getVersion";
    public GetVersionExCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
        requiresSingleThread = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        return BundleUtil.createSingleInt("version", ModuleIdentity.apkVersionCode());
    }

    public static Bundle invoke(Context context) {
        return XProxyContent.luaCall(
                context,
                "getVersion");
    }

    public static int get(Context context) {
        return BundleUtil.readInteger(XProxyContent.luaCall(
                context,
                "getVersion"), "version", -1);
    }
}
