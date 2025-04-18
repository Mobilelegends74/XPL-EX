package eu.faircode.xlua.x.xlua.commands.call;

import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.XProxyContent;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.ui.core.UserClientAppContext;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.x.xlua.database.ActionFlag;
import eu.faircode.xlua.x.xlua.database.ActionPacket;
import eu.faircode.xlua.x.xlua.database.A_CODE;
import eu.faircode.xlua.x.xlua.identity.UserIdentity;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.data.SettingPacket;
import eu.faircode.xlua.x.xlua.settings.data.SettingsApi;

public class PutSettingExCommand extends CallCommandHandlerEx {
    private static final String TAG = LibUtil.generateTag(PutSettingExCommand.class);
    public static final String COMMAND_NAME = "putExSettingEx";

    public PutSettingExCommand() {
        name = COMMAND_NAME;
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) throws Throwable {
        return SettingsApi.single_locked(
                        commandData.getDatabase(),
                        commandData.readExtraAs(SettingPacket.class),
                        commandData.getContext(),
                        commandData.getCategory(),
                        commandData.getUserId())
                        .toBundle();
    }

    public static A_CODE call(Context context, SettingHolder holder, UserClientAppContext userContext, boolean kill, boolean delete) {
        SettingPacket packet = new SettingPacket(holder, userContext, ActionPacket.create(delete ? ActionFlag.DELETE : ActionFlag.PUSH, kill));
        Bundle b = packet.toBundle();
        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Executing Put Setting Call, Pkg=%s  Setting=%s  Action=%s  Kill=%s",
                    userContext.appPackageName,
                    holder.getName(),
                    packet.getActionFlags().name(),
                    String.valueOf(kill)));

        return A_CODE.fromBundle(
                XProxyContent.luaCall(
                        context,
                        COMMAND_NAME, b));
    }

    public static A_CODE call(Context context, SettingPacket packet) {
        return A_CODE.fromBundle(
                XProxyContent.luaCall(
                        context,
                        "putExSettingEx", packet.toBundle()));
    }

    public static A_CODE putTheme(Context context, String value) { return putGen(context, GetSettingExCommand.SETTING_THEME, value); }

    public static A_CODE putVerboseLogging(Context context, boolean verboseLogging) { return putGen(context, GetSettingExCommand.SETTING_VERBOSE_DEBUG, String.valueOf(verboseLogging)); }
    public static A_CODE putRestrictNewApps(Context context, boolean restrict) { return putGen(context, GetSettingExCommand.SETTING_RESTRICT_NEW_APPS, String.valueOf(restrict)); }
    public static A_CODE putNotifyNewApps(Context context, boolean notify) { return putGen(context, GetSettingExCommand.SETTING_NOTIFY_NEW_APPS, String.valueOf(notify)); }


    public static A_CODE putForceStop(Context context, int uid, String packageName, boolean forceStop) {
        if(!Str.isEmpty(packageName) && !UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName)) {
            SettingPacket packet = new SettingPacket(GetSettingExCommand.SETTING_FORCE_STOP, String.valueOf(forceStop));
            packet.setUserIdentity(UserIdentity.fromUid(uid, packageName));
            packet.setActionPacket(ActionPacket.create(ActionFlag.PUSH, false));
            return call(context, packet);
        }

        return A_CODE.FAILED;
    }



    public static A_CODE putGen(Context context, String settingName, String value) {
        SettingPacket packet = new SettingPacket(settingName, value);
        packet.setUserIdentity(UserIdentity.fromUid(Process.myUid(), UserIdentity.GLOBAL_NAMESPACE));
        packet.setActionPacket(ActionPacket.create(ActionFlag.PUSH, false));
        return call(context, packet);
    }

    public static A_CODE putConfig(Context context, int uid, String packageName, String value) {
        if(!Str.isEmpty(packageName) && !UserIdentity.GLOBAL_NAMESPACE.equalsIgnoreCase(packageName)) {
            SettingPacket packet = new SettingPacket(GetSettingExCommand.SETTING_SELECTED_CONFIG, value);
            packet.setUserIdentity(UserIdentity.fromUid(uid, packageName));
            packet.setActionPacket(ActionPacket.create(ActionFlag.PUSH, false));
            return call(context, packet);
        }

        return A_CODE.FAILED;
    }
}
