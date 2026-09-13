package eu.faircode.xlua.x.hook.interceptors.cell;

import java.util.Map;

/** Resolves coherent operator values for TelephonyManager's SIM-aware APIs. */
public final class TelephonyOperatorValues {
    public static final String OPERATOR_NAME = "cell.operator.name";
    public static final String OPERATOR_NUMERIC = "cell.operator.numeric.id";
    public static final String OPERATOR_MCC = "cell.operator.mcc";
    public static final String OPERATOR_MNC = "cell.operator.mnc";

    private TelephonyOperatorValues() { }

    public static String resolve(Map<String, String> settings, boolean operatorName, int slot) {
        if(settings == null)
            return null;

        String[] suffixes = slot == 0
                ? new String[] { ".1", "" }
                : slot == 1
                ? new String[] { ".2", "" }
                : new String[] { ".1", ".2", "" };

        if(operatorName) {
            for(String suffix : suffixes) {
                String value = clean(settings.get(OPERATOR_NAME + suffix));
                if(value != null)
                    return value;
            }
            String legacy = clean(settings.get("gsm.network.carrier"));
            return legacy != null ? legacy : clean(settings.get("phone.isp"));
        }

        for(String suffix : suffixes) {
            String numeric = clean(settings.get(OPERATOR_NUMERIC + suffix));
            if(numeric != null)
                return numeric;

            String mcc = clean(settings.get(OPERATOR_MCC + suffix));
            String mnc = clean(settings.get(OPERATOR_MNC + suffix));
            if(mcc != null && mnc != null)
                return mcc + mnc;
        }

        String legacyNumeric = clean(settings.get("gsm.operator.id"));
        if(legacyNumeric != null)
            return legacyNumeric;
        String legacyMcc = first(settings, "gsm.operator.mcc", "phone.mmc");
        String legacyMnc = first(settings, "gsm.operator.mnc", "phone.mnc");
        return legacyMcc != null && legacyMnc != null ? legacyMcc + legacyMnc : null;
    }

    private static String first(Map<String, String> settings, String first, String second) {
        String value = clean(settings.get(first));
        return value != null ? value : clean(settings.get(second));
    }

    private static String clean(String value) {
        if(value == null)
            return null;
        value = value.trim();
        return value.isEmpty() ? null : value;
    }
}
