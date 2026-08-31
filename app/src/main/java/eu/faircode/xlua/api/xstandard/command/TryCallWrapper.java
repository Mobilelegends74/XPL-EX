package eu.faircode.xlua.api.xstandard.command;

import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.Callable;

import eu.faircode.xlua.xposed.api101.ModernXposedBridge;
import eu.faircode.xlua.XPolicy;
import eu.faircode.xlua.api.xstandard.CallCommandHandler;

public class TryCallWrapper implements Callable<Bundle> {
    private static final String TAG = "XLua.TryCallWrapper";

    private final CallPacket_old packet;
    private final CallCommandHandler handle;

    private boolean isRunning = false;
    private Throwable exception;

    public static TryCallWrapper create(CallPacket_old packet, CallCommandHandler handler) { return new TryCallWrapper(packet, handler); }

    public TryCallWrapper(CallPacket_old packet, CallCommandHandler handle) {
        this.packet = packet;
        this.handle = handle;
    }

    @Override
    public Bundle call() {
        XPolicy policy = XPolicy.policyAllowRW();
        try {
            isRunning = true;
            return handle.handle(packet);
        }catch (Throwable e) {
            exception = e;
            Log.e(TAG, "Call Error: packet=" + packet + " handler=" + handle.getName() + " \n" + e + "\n" + Log.getStackTraceString(e));
            ModernXposedBridge.log("Call Error");
            return null;
        }finally {
            policy.revert();
            isRunning = false;
        }
    }

    public boolean isRunning() { return isRunning; }
    public boolean hasException() { return exception != null; }
    public Throwable getException() { return this.exception; }
}
