package eu.faircode.xlua.x.systemlab;

/** An intentionally closed-scope, app-owned Settings API experiment. */
public final class IdentityLabPolicy {
    public static final String PROBE_PACKAGE = "eu.faircode.xlua.identitylab";
    public static final String DEMO_ID = "1122334455667788";

    private IdentityLabPolicy() { }

    public static boolean isAndroidIdRead(String method, String key) {
        return "GET_secure".equals(method) && "android_id".equals(key);
    }

    public static boolean isLabCaller(int uid, String[] packages) {
        return uid >= 10000 && packages != null && packages.length == 1
                && PROBE_PACKAGE.equals(packages[0]);
    }

    public static boolean validId(String id) {
        if (id == null || id.length() != 16) return false;
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if (!(c >= '0' && c <= '9') && !(c >= 'a' && c <= 'f')
                    && !(c >= 'A' && c <= 'F')) return false;
        }
        return true;
    }
}
