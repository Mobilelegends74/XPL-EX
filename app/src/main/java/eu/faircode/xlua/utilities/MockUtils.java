package eu.faircode.xlua.utilities;

public class MockUtils {
    public static final String NOT_BLACKLISTED = "NotBlacklisted";
    public static final String HIDE_PROPERTY = "<<*hide*>>";

    public static boolean isPropVxpOrLua(String propName) {
        boolean r = propName.equalsIgnoreCase("exp") ||
                        propName.equalsIgnoreCase("vxp") ||
                        propName.equalsIgnoreCase(".lua");
        return r;
    }
}
