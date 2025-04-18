package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;

import java.util.List;

import eu.faircode.xlua.XLegacyCore;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.utilities.BundleUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;

public class GetGroupsCommand extends CallCommandHandlerEx {
    public static final String FIELD_GROUP = "groups";
    public static final String COMMAND_NAME = "getGroups";
    public GetGroupsCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
        this.requiresSingleThread = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        return BundleUtil.
                createFromStringList(
                        FIELD_GROUP,
                        XLegacyCore.getGroups(XLegacyCore.getCollections(commandData.getDatabase())));
    }

    public static List<String> get(Context context) {
        return BundleUtil
                .getStringArrayList(
                        XProxyContent.luaCall(context, COMMAND_NAME), FIELD_GROUP);
    }
}