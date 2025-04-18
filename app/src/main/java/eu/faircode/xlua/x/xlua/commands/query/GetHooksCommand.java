package eu.faircode.xlua.x.xlua.commands.query;

import android.content.Context;
import android.database.Cursor;

import java.util.List;

import eu.faircode.xlua.XLegacyCore;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.api.hook.XLuaHookConversions;
import eu.faircode.xlua.utilities.CursorUtil;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHookIO;
import eu.faircode.xlua.x.xlua.commands.QueryCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.QueryPacket;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.DatabaseHelpEx;

public class GetHooksCommand extends QueryCommandHandlerEx {
    public static final String COMMAND_NAME = "getHooks";

    public GetHooksCommand() { this.name = COMMAND_NAME; this.requiresSingleThread = true; this.requiresPermissionCheck = false; }

    @Override
    public Cursor handle(QueryPacket commandData) throws Throwable {
        if(commandData.isDump()) {
            return CursorUtil.toMatrixCursor(
                    DatabaseHelpEx.getFromDatabase(commandData.getDatabase(),
                            XHook.TABLE_NAME,
                            XHook.class,
                            true),
                    marshall,
                    XHookIO.FLAG_WITH_LUA);
        } else {
            return CursorUtil.toMatrixCursor(
                    XLegacyCore.getHooks(XLegacyCore.getCollections(commandData.getDatabase()), commandData.isAll()),
                    marshall,
                    XHookIO.FLAG_WITH_LUA);
        }
    }

    public static List<XHook> dump(Context context, boolean marshall) {
        return ListUtil.copyToArrayList(
                XLuaHookConversions.fromCursor(
                        XProxyContent.luaQuery(context, XProxyContent.commandName(COMMAND_NAME, marshall), new String[] { ActionPacket.ACTION_DUMP }),
                        marshall, true));
    }

    public static List<XHook> getHooks(Context context, boolean marshall) { return ListUtil.copyToArrayList(XLuaHookConversions.fromCursor(invoke(context, marshall, false), marshall, true)); }
    public static List<XHook> getHooks(Context context, boolean marshall, boolean all) { return ListUtil.copyToArrayList(XLuaHookConversions.fromCursor(invoke(context, marshall, all), marshall, true)); }
    public static Cursor invoke(Context context, boolean marshall, boolean all) {
        return XProxyContent.luaQuery(
                context,
                XProxyContent.commandName(COMMAND_NAME, marshall), all ? new String[] { ActionPacket.ACTION_ALL } : null);
    }
}
