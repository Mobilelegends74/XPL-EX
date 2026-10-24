package eu.faircode.xlua.x.xlua.commands.call;

import android.os.Bundle;
import eu.faircode.xlua.x.xlua.commands.CallCommandHandlerEx;
import eu.faircode.xlua.x.xlua.commands.packet.CallPacket;
import eu.faircode.xlua.xposed.api101.ModernXposedBridge;

/** Signed module-app only; copy bounded scope metadata into the LSPosed export. */
public final class ScopeDiagnosticCommand extends CallCommandHandlerEx {
    public ScopeDiagnosticCommand() {
        name = "scopeDiagnostic";
        requiresPermissionCheck = true;
    }

    @Override
    public Bundle handle(CallPacket commandData) {
        String message = commandData.getExtraString("message");
        if (message != null) {
            message = message.replace('\n', ' ').replace('\r', ' ');
            ModernXposedBridge.log("[XLua.Scope] " + message.substring(0, Math.min(message.length(), 1024)));
        }
        return new Bundle();
    }
}
