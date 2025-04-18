package eu.faircode.xlua.utilities;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.LibUtil;

public class DateTimeUtil {
    private static final String TAG = LibUtil.generateTag(DateTimeUtil.class);

    public static long convertToMilliseconds(int year, int month, int day) {
        return 0;//Add
    }


    public static long toTimeMillis(String setting, long defaultValue) {
        try {
            long[] iTimes = DateTimeUtil.toTimeSpecs(setting);
            long seconds = iTimes[0];
            long off = seconds * 1000; // Convert seconds to milliseconds
            return off;
        }catch (Exception e) {
            Log.e(TAG, "Error Converting X Setting to Time! Error=" + e);
            return defaultValue;
        }
    }

    public static long[] toTimeSpecs(String input) {
        List<String> symbols = Arrays.asList("NANO", "SECOND", "MINUTE", "HOUR", "DAY");
        long nanoSeconds = 0;
        long seconds = 0;

        List<String> parts = Str.splitToList(input);
        for(String part : parts) {
            if(!Str.isEmpty(part)) {
                String cleaned = part.replaceAll(Str.WHITE_SPACE, Str.EMPTY);
                char[] chars = cleaned.toCharArray();
                StringBuilder low = new StringBuilder();
                boolean exitNumeric = false;
                StringBuilder hig = new StringBuilder();
                //Handle "*"
                //Example if "*:DAY" Then generate random amount of days
                for(char c : chars) {
                    if(!exitNumeric && Character.isDigit(c)) {
                        if(c == '0' && low.length() == 0) continue;
                        low.append(c);
                    }
                    else if(Character.isAlphabetic(c)) {
                        exitNumeric = true;
                        hig.append(c);
                    }
                }

                if(low.length() == 0) continue;

                String sym = hig.toString().toUpperCase();
                if(!symbols.contains(sym))
                    sym = symbols.get(0);    //Assume the Symbol is "nano seconds"

                String val = low.toString();
                if(val.length() > 8)
                    val = val.substring(0, 8);

                long value = Str.tryParseLong(val);

                // Convert each unit to seconds and nanoseconds
                switch(sym) {
                    case "NANO":
                        nanoSeconds += value;
                        break;
                    case "SECOND":
                        seconds += value;
                        break;
                    case "MINUTE":
                        seconds += value * 60;
                        break;
                    case "HOUR":
                        seconds += value * 3600;
                        break;
                    case "DAY":
                        seconds += value * 86400; // 24 * 60 * 60
                        break;
                }

                // Handle nanosecond overflow
                if (nanoSeconds >= 1_000_000_000) {
                    seconds += nanoSeconds / 1_000_000_000;
                    nanoSeconds = nanoSeconds % 1_000_000_000;
                }
            }
        }

        return new long[]{seconds, nanoSeconds};
    }

}
