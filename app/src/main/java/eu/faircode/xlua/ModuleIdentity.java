package eu.faircode.xlua;

/** Separates the installed APK identity from the persistent command-bridge protocol. */
public final class ModuleIdentity {
    private ModuleIdentity() {
    }

    /** Version of the code currently executing in this process. */
    public static int apkVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    /** Compatibility version of commands exchanged with the system-side bridge. */
    public static String bridgeProtocolVersion() {
        return BuildConfig.BRIDGE_VERSION;
    }
}
