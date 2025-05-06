package eu.faircode.xlua.x;

import android.text.Editable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.x.data.string.StrBuilder;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.data.utils.TryRun;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.runtime.reflect.DynClass;
import eu.faircode.xlua.x.ui.core.view_registry.IIdentifiableObject;
import eu.faircode.xlua.x.xlua.LibUtil;

/*
    ToDO: Clean this damn class up after release
 */
public class Str {
    private static final String TAG = LibUtil.generateTag(Str.class);

    public static final String DEFAULT_DEFAULT = "DEFAULT";
    public static final String EMPTY = "";
    public static final String ASTERISK = "*";
    public static final String COLLEN = ":";
    public static final String NEW_LINE = "\n";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String COMMA = ",";
    public static final String PERIOD = ".";
    public static final String WHITE_SPACE = " ";
    public static final String FORWARD_SLASH = "/";
    public static final String PIPE = "|";

    public static final Charset CHAR_SET_UTF_8 = StandardCharsets.UTF_8;
    public static final Charset CHAR_SET_UTF_16 = StandardCharsets.UTF_16;
    public static final Charset CHAR_SET_UTF_16_LE = StandardCharsets.UTF_16LE;

    private static final Pattern DOUBLE_NEWLINE_PATTERN = Pattern.compile("\n\\s*\n");

    public static final int MOBILE_SAFE_LENGTH = 40;
    public static final int MIN_DIVIDER_LENGTH = 4;  // Minimum to have at least "-|-"
    public static final char DEFAULT_DIVIDER = '-';
    public static final String DEFAULT_TEXT = "";

    public static final Character SPACE_CHAR = ' ';

    public static final String[] CONTROL_CHARS = new String[] {
            "\n",   // Line feed (new line)
            "\r",   // Carriage return
            "\t",   // Tab
            "\f",   // Form feed
            "\b",   // Backspace
            "\0"    // Null character
    };

    // If you really need the quote, use it without escape:
    public static final String[] ESCAPE_CHARS = new String[] {
            "\n",   // Line feed (new line)
            "\r",   // Carriage return
            "\t",   // Tab
            "\f",   // Form feed
            "\b",   // Backspace
            "\0",   // Null character
            "\"",   // Double quote
            "'",    // Single quote (no escape needed)
            "\\"    // Backslash
    };

    //formattedString.replaceAll("(?m)\\n{2,}", "\n");


    public static String replaceAll(String s, String regex, String replaceWith) { return isEmpty(s) || isEmpty(regex) ? s : s.replaceAll(regex, getNonNullString(replaceWith, Str.EMPTY)); }

    public static boolean isNullOrDefaultValue(String s) { return isEmpty(s) ||  areEqualAny(s,
            "00:00:00:00:00:00",
            "0000000000",
            "02:00:00:00:00:00",
            "unknown",
            "null",
            "empty",
            "default",
            "private",
            "<unknown ssid>",
            "unavailable"); }



    public static int hashCode(String s) { return s != null ? s.hashCode() : 0; }

    public static String enclose(String s) { return "(" + s + ")"; }

    public static int length(String s) {
        return s == null ? -1 : s.length();
    }

    public static byte[] stringToRawBytes(String str) {
        if(str == null)
            return null;
        // Convert the string back to bytes
        int len = str.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4)
                    + Character.digit(str.charAt(i + 1), 16));
        }

        return data;
    }


    public static boolean hasAlphabeticChars(String s) {
        if(!isValid(s))
            return false;

        char[] chars = s.toCharArray();
        for(char c : chars)
            if(Character.isAlphabetic(c))
                return true;

        return false;
    }

    public static boolean hasNumericChars(String s) {
        if(!isValid(s))
            return false;

        char[] chars = s.toCharArray();
        for(char c : chars)
            if(Character.isDigit(c))
                return true;

        return false;
    }

    //Clean these three
    public static String cleanDoubleNewLines(String input) { return input == null ? null : DOUBLE_NEWLINE_PATTERN.matcher(input).replaceAll("\n"); }


    public static String noNewLineEnding(String input) { return input == null ? null : input.endsWith("\n") ? input.substring(0, input.length() - 1) : input; }

    public static String ensureNoDoubleNewLines(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        try {
            // Replace all occurrences of multiple consecutive newlines with a single newline
            return input.replaceAll("\\n+", "\n");
        } catch (Exception e) {
            Log.e(TAG, "Failed ensuring no double new lines: " + e);
            return input;
        }
    }

    public static boolean equalsObject(Object o, String compareAgainst) { return equalsObject(o, compareAgainst, true); }
    public static boolean equalsObject(Object o, String compareAgainst, boolean caseSensitive) {
        if(!(o instanceof String)) return false;
        return caseSensitive ? ((String)o).equals(compareAgainst) : ((String)o).equalsIgnoreCase(compareAgainst);
    }

    public static String noNL(Object o) { return noNL(o, " "); }
    public static String noNL(Object o, String replaceString) {
        String s = ensureNoDoubleNewLines(toStringOrNull(o));
        return s.replaceAll("\n", replaceString);
    }

    public static String toLowerCase(String s) { return s == null || s.isEmpty() ? s : s.toLowerCase(); }

    public static boolean contains(String s, String c, boolean lowerCase) {
        if(s == null) return c == null;
        if(c == null) return false;
        if(lowerCase) {
            String loweredS = s.toLowerCase();
            String loweredC = c.toLowerCase();
            return loweredS.contains(loweredC);
        } else {
            return s.contains(c);
        }
    }

    public static boolean matches(String s, String pattern) { return matches(s, pattern, true); }
    public static boolean matches(String s, String pattern, boolean ignoreCase) {
        if(s == null)
            return pattern == null;
        if(isEmpty(pattern))
            return false;

        int len = length(pattern);
        boolean start = pattern.charAt(0) == '*';
        boolean end = len > 1 && pattern.charAt(len - 1) == '*';
        if(!start && !end)
            return ignoreCase ?
                    s.equalsIgnoreCase(pattern) : s.equals(pattern);

        String cleanPattern = start ?
                end ? pattern.substring(1, pattern.length() - 1) : pattern.substring(1) :
                pattern.substring(0, pattern.length() - 1);

        if(ignoreCase) {
            String loweredPattern = toLowerCase(cleanPattern);
            String loweredString = toLowerCase(s);
            return end ? start ?
                    loweredString.contains(loweredPattern) : loweredString.endsWith(loweredPattern) :
                    loweredString.startsWith(loweredPattern);
        } else {
            return end ? start ?
                    s.contains(cleanPattern) : s.endsWith(cleanPattern) :
                    s.startsWith(cleanPattern);
        }
    }

    public static String fm(String str, Object... objects) { return fm(true, false, str, objects); }
    public static String fm(boolean noDoubleNewLines, String str, Object... objects) { return fm(noDoubleNewLines, false, str, objects); }
    public static String fm(boolean noDoubleNewLines, boolean newLineBetweenEach, String str, Object... objects) {
        if(!ArrayUtils.isValid(objects) || isEmpty(str))
            return str;

        if(!str.contains("%s"))
            return str;

        try {
            Object[] os = new Object[objects.length];
            for(int i = 0; i < objects.length; i++) {
                Object o = objects[i];
                if(o == null) os[i] = "null";
                else {
                    os[i] = o;
                }
            }

            String formattedString = String.format(str, os);
            if (newLineBetweenEach) {
                String[] placeholders = str.split("%s");
                StringBuilder newLineFormatted = new StringBuilder();
                for (int i = 0; i < placeholders.length; i++) {
                    newLineFormatted.append(placeholders[i]);
                    if (i < objects.length) {
                        newLineFormatted.append(objects[i]);
                        newLineFormatted.append("\n"); // Append a newline after each object
                    }
                }
                formattedString = newLineFormatted.toString().trim(); // Remove any trailing newline
            }

            return noDoubleNewLines ? ensureNoDoubleNewLines(formattedString) : formattedString;
        }catch (Exception e) {
            Log.e(TAG, "Failed to format String! Make sure you have correct number of objects for each insert of %s ! Objects Count=" + ArrayUtils.safeLength(objects) + " String=[" + str + "] >> Error=" + e);
            return Str.combineEx("[FORMAT_EXCEPTION]::[", str, "]::[FORMAT_EXCEPTION]");
        }
    }

    public static String e(Throwable e, boolean stack) {
        StringBuilder sb = new StringBuilder();
        sb.append("Error=").append(e.getMessage());
        if(stack) {
            sb.append("\n")
                    .append("Stack:")
                    .append("\n")
                    .append(RuntimeUtils.getStackTraceSafeString(e));
        }

        return cleanDoubleNewLines(sb.toString());
    }


    public static String isNullAsString(Object v) { return v == null ? "False" : "True"; }

    @SuppressWarnings("StringOperationCanBeSimplified")
    public static String createCopy(String x) { return x == null ? null : new String(x); }

    //Have a T Combine list thing ? T can be like InetAddress (assuming it has a toString)

    public static String getNonNullOrEmptyString(String a, String b) { return TextUtils.isEmpty(a) ? b : a; }


    public static String getNonNullString(String a, String b) { return a == null ? b : a; }

    //Make a ALL version
    public static boolean areEqualsAnyIgnoreCase(String a, String... compareItems) {
        if(ArrayUtils.isValid(compareItems)) {
            if(a == null) {
                for(String c : compareItems) if(c == null) return true;
                return false;
            }

            for(String c : compareItems)
                if(a.equalsIgnoreCase(c))
                    return true;
        }

        return false;
    }

    public static boolean areEqualAny(String a, String... compareItems) {
        if(ArrayUtils.isValid(compareItems)) {
            for(String c : compareItems)
                if(Objects.equals(a, c))
                    return true;
        }

        return false;
    }

    public static boolean areEqual(String a, String b) { return Objects.equals(a, b); }
    public static boolean areEqualIgnoreCase(String a, String b) { return a == null ? b == null : a.equalsIgnoreCase(b); }

    public static boolean areEqual(String a, String b, boolean caseSensitive) { return areEqual(a, b, caseSensitive, false); }
    public static boolean areEqual(String a, String b, boolean caseSensitive, boolean treatNullAndEmptyAsSame) {
        /*return treatNullAndEmptyAsSame ?
                (TextUtils.isEmpty(a) && TextUtils.isEmpty(b)) || caseSensitive ? areEqual(a, b) : areEqualIgnoreCase(a, b) :
                caseSensitive ? areEqual(a, b) : areEqualIgnoreCase(a, b);*/

        if(treatNullAndEmptyAsSame) {
            if(TextUtils.isEmpty(a) && TextUtils.isEmpty(b))
                return true;
        } else {
            if(a == null && b == null)
                return true;
        }

        if(a == null || b == null)
            return false;

        return caseSensitive ? a.equalsIgnoreCase(b) : a.equals(b);
    }

    public static String ensureIsNotNullOrDefault(String s, String defaultValue) { return s == null ? defaultValue : s; }

    public static String ensureIsValidOrDefault(String s, String defaultValue) { return isValidNotWhitespaces(s) ? s : defaultValue; }

    public static String ensureIsValidOrNull(String s) { return isValidNotWhitespaces(s) ? s : null; }

    public static CharSequence convertToUTF8CharSequence(String input) {
        byte[] utf8Bytes = input.getBytes(StandardCharsets.UTF_8);
        return new String(utf8Bytes, StandardCharsets.UTF_8);
    }

    public static String subStringIndexOf(String data, String indexOfData, boolean cutOffAfterIndexData) {
        if (data == null || indexOfData == null || indexOfData.isEmpty()) return data;
        int indexOf = data.indexOf(indexOfData);
        if (indexOf == -1) return data;
        if (cutOffAfterIndexData) {
            int startIndex = indexOf + indexOfData.length();
            return data.substring(startIndex);
        } else {
            return data.substring(indexOf);
        }
    }



    public static String[] splitAdvance(String data, String... delimiters) { return splitAdvance(data, false, false, delimiters); }
    public static String[] splitAdvance(String data, boolean ensureEachLineValid, String... delimiters) { return splitAdvance(data, ensureEachLineValid, false, delimiters); }


    /**
     * Splits the input string using multiple delimiters.
     * @param data The string to split.
     * @param ensureEachLineValid If true, only non-blank segments are included.
     * @param trimEach If true, each segment is trimmed of leading/trailing whitespace.
     * @param delimiters One or more delimiters to split on.
     * @return An array of split segments, never null.
     */
    public static String[] splitAdvance(String data,
                                        boolean ensureEachLineValid,
                                        boolean trimEach,
                                        String... delimiters) {
        if (isEmpty(data))
            return new String[0];

        // If no delimiters provided, return the whole string (post-trim/validation)
        if (!ArrayUtils.isValid(delimiters)) {
            String single = trimEach ? data.trim() : data;
            if (ensureEachLineValid && !isValidNotWhitespaces(single)) return new String[0];
            return new String[]{ single };
        }

        // Build a regex that matches any of the delimiters
        StringBuilder regexBuilder = new StringBuilder();
        for (String delimiter : delimiters) {
            if (!isEmpty(delimiter)) {
                if (regexBuilder.length() > 0)
                    regexBuilder.append("|");

                regexBuilder.append(Pattern.quote(delimiter));
            }
        }

        // If all delimiters were null/empty, fallback
        if (isEmpty(regexBuilder))
            return new String[]{ trimEach ? trimOriginal(data) : data };

        String regex = regexBuilder.toString();

        // Perform the split
        String[] parts = data.split(regex);
        List<String> result = new ArrayList<>(parts.length);
        if(ArrayUtils.isValid(parts)) {
            for (String p : parts) {
                String item = trimEach ? trimOriginal(p) : p;
                if (ensureEachLineValid) {
                    if (!isValidNotWhitespaces(item))
                        continue;

                    result.add(item);
                } else {
                    if (trimEach && isEmpty(item))
                        continue;

                    result.add(item);
                }
            }
        }

        return result.toArray(new String[0]);
    }


    public static String[] split(String data, String delimiter, boolean ensureEachLineValid) { return split(data, delimiter, ensureEachLineValid, false); }
    public static String[] split(String data, String delimiter, boolean ensureEachLineValid, boolean trimEach) {
        if(Str.isEmpty(data))
            return new String[0];

        String[] parts = data.split(Pattern.quote(delimiter));
        if(ensureEachLineValid) {
            List<String> partsCleaned = new ArrayList<>();
            for(String p : parts) {
                if(Str.isEmpty(p))
                    continue;

                partsCleaned.add(trimEach ? p.trim() : p);
            }

            return partsCleaned.toArray(new String[0]);
        } else if(trimEach) {
            List<String> partsCleaned = new ArrayList<>();
            for(String p : parts) {
                String t = p.trim();
                if(t.isEmpty()) continue;
                partsCleaned.add(t);
            }

            return partsCleaned.toArray(new String[0]);
        }

        return parts;
    }

    public static String createFilledCopy(String str, String fillChar) {
        if(str == null) return fillChar;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < str.length(); i++)
            sb.append(fillChar);
        return sb.toString();
    }

    //NOTE WHEN USING THIS ALSO TRIMS WHITE SPACE!
    public static String combineWithDelimiter(String str1, String str2, String delimiter, boolean allowDup) {
        StringBuilder sb = new StringBuilder();
        if(!TextUtils.isEmpty(str1))
            sb.append(trim(str1, delimiter, true, true));
        if(!TextUtils.isEmpty(str2)) {
            sb.append(delimiter);
            sb.append(trim(str2, delimiter, true, true));

            if(allowDup) {
                String[] parts = sb.toString().split(delimiter);
                List<String> parsed = new ArrayList<>();
                for(String p : parts) {
                    if(!parsed.contains(p))
                        parsed.add(p);
                }

                return Str.joinList(parsed, delimiter);
            }
        }



        return sb.toString();
    }

    /**
     * Capitalizes the first letter of the input string.
     *
     * @param input The string to capitalize
     * @return The input string with the first letter capitalized, or the original string
     *         if it's null, empty, or already capitalized
     */
    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // If the string is just one character, uppercase it and return
        if (input.length() == 1) {
            return input.toUpperCase();
        }

        // Otherwise, uppercase the first character and append the rest of the string
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    /**
     * Counts the number of occurrences of a substring within a string using regex.
     *
     * @param text The string to search within
     * @param substring The substring to search for
     * @return The number of times the substring appears in the text
     */
    public static int countOccurrencesRegex(String text, String substring) {
        // Handle edge cases
        if (text == null || substring == null || text.isEmpty() || substring.isEmpty()) {
            return 0;
        }

        // Escape special regex characters in the substring to treat it as literal text
        String escapedSubstring = Pattern.quote(substring);

        // Create a pattern and matcher
        Pattern pattern = Pattern.compile(escapedSubstring);
        Matcher matcher = pattern.matcher(text);

        // Count occurrences
        int count = 0;
        while (matcher.find()) {
            count++;
        }

        return count;
    }


    public static boolean isEmpty(StrBuilder sb) { return sb == null || sb.isEmpty(); }
    public static boolean isEmpty(StringBuilder sb) { return sb == null || sb.length() <= 0; }
    public static boolean isEmpty(String s) { return s == null || s.isEmpty(); }
    public static boolean isAnyEmpty(String... strings) {
        if(strings != null) {
            for(String s : strings) {
                if(s == null || s.isEmpty())
                    return true;
            }
        }

        return false;
    }

    /**
     * Converts a byte array to a hexadecimal string representation.
     *
     * @param bytes The byte array to convert
     * @param addSpaces Whether to add spaces between bytes in the output
     * @return A hexadecimal string representation of the byte array
     */
    public static String bytesToHexString(byte[] bytes, boolean addSpaces) {
        if (bytes == null || bytes.length == 0)
            return "";

        StringBuilder hexString = new StringBuilder(bytes.length * (addSpaces ? 3 : 2));
        String separator = addSpaces ? " " : "";

        for (int i = 0; i < bytes.length; i++) {
            // Convert each byte to a 2-digit hex value with leading zeros if needed
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1)
                hexString.append('0');

            hexString.append(hex);

            // Add space if not the last byte and spaces are requested
            if (addSpaces && i < bytes.length - 1) {
                hexString.append(separator);
            }
        }

        return hexString.toString();
    }

    public static String combineEx(Object... objects) { return combineEx(false, objects); }
    public static String combineEx(boolean appendNullIfNull, Object... objects) {
        StringBuilder sb = new StringBuilder();
        if(ArrayUtils.isValid(objects)) {
            for(Object o : objects) {
                if(o instanceof String) {
                    sb.append((String) o);
                    continue;
                }

                //This is all kinda silly :P
                if(o == null) {
                    if(appendNullIfNull) {
                       sb.append("null");
                    }
                } else {
                    sb.append(String.valueOf(o));
                }
            }
        }

        return sb.toString();
    }

    public static String combine(String str1, String str2) { return combine(str1, str2, false); }
    public static String combine(String str1, String str2, boolean useNewLine) {
        StringBuilder sb = new StringBuilder();
        sb.append(str1);
        if(useNewLine) sb.append(NEW_LINE);
        sb.append(str2);
        return sb.toString();
    }

    //Move this
    public static String hookToJsonString(XLuaHook hook) {
        try {
             return hook.toJSON();
        }catch (Exception ignored) { }
        return "Name=" + hook.getName() + " Id=" + hook.getObjectId() + " Class=" + hook.getClassName();
    }

    /*public static String repeatChar(char c, int repeatTimes) {
        if(repeatTimes < 2) return String.valueOf(c);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < repeatTimes; i++)
            sb.append(c);

        return sb.toString();
    }*/

    public static String toString(Object o) { return toString(o, null); }
    public static String toString(Object o, String defaultValueIfNull) {
        if(o == null)
            return defaultValueIfNull;

        if(o instanceof String)
            return (String) o;

        try {
            return String.valueOf(o);
        }catch (Exception ignored) {
            return defaultValueIfNull;
        }
    }

    public static String toString(EditText ti) { return toString(ti, null); }
    public static String toString(EditText ti, String defaultIfNull) {
        if(ti == null)
            return defaultIfNull;
        try {
            Editable et = ti.getText();
            if(et == null)
                return defaultIfNull;

            return et.toString();
        }catch (Exception e) {
            return defaultIfNull;
        }
    }

    public static String toObjectId(Object o) {
        if(o != null) {
            try {
                if(o instanceof IIdentifiableObject) {
                    IIdentifiableObject io = (IIdentifiableObject)o;
                    return io.getObjectId();
                }

                return String.valueOf(o.hashCode());
            }catch (Exception ignored) { }
        }
        return "null";
    }

    public static String toObjectClassSimpleNameNonNull(Object o) { return Str.getNonNullOrEmptyString(toObjectClassSimpleName(o), "null"); }
    public static String toObjectClassSimpleName(Object o) {
        if(o != null) {
            return TryRun.get(() -> {
                if(o instanceof DynClass) return toObjectClassSimpleNameNonNull(((DynClass)o).getRawClass());
                else if(o instanceof Class<?>) return ((Class<?>)o).getSimpleName();
                 else return o.getClass().getSimpleName();
            });
        }
        return null;
    }

    public static String toObjectClassNameNonNull(Object o) { return Str.getNonNullOrEmptyString(toObjectClassName(o), "null"); }
    public static String toObjectClassName(Object o) {
        if(o != null) {
            return TryRun.get(() -> {
                if(o instanceof DynClass) return ((DynClass)o).getName();
                else if(o instanceof Class<?>) return ((Class<?>)o).getName();
                else return o.getClass().getName();
            });
        }
        return null;
    }


    public static Character charAt(String s, int index) { return charAt(s, index, '\0'); }
    public static Character charAt(String s, int index, Character defaultValue) {
        if(!isValid(s))
            return defaultValue;
        if(s.length() <= index || index < 0)
            return defaultValue;

        return s.charAt(index);
    }

    public static String toStringOrNull(final Object o) {
        if(o != null) {
            return TryRun.get(() -> {
                if(o instanceof byte[] || o.getClass().equals(Byte[].class))
                    return bytesToHexString((byte[]) o, true);
                else if(o instanceof ZipEntry)
                    return ((ZipEntry)o).getName();
                else if(o instanceof Class<?>[]) {
                    StrBuilder sb = StrBuilder.create().ensureDelimiter(Str.COMMA);
                    Class<?>[] cTypes = (Class<?>[]) o;
                    if(!ArrayUtils.isValid(cTypes)) {
                        sb.append(Str.EMPTY);
                    } else {
                        for(Class<?> c : cTypes)
                            sb.append(c == null ? "null" : c.getName());
                    }

                    return sb.toString();
                }
                else if(o instanceof Method) {
                    Method m = (Method) o;
                    return m.getName();
                }
                else if(o instanceof Field) {
                    Field f = (Field) o;
                    return f.getName();
                }
                else {
                    return String.valueOf(o);
                }
            });
        }
        return "null";
    }

    public static boolean isValid(CharSequence s) { return s != null && isValid(s.toString()); }
    public static boolean isValid(String s) { return s != null && !TextUtils.isEmpty(s); }

    public static boolean isValidNotWhitespaces(CharSequence s) { return s != null && isValidNotWhitespaces(s.toString()); }
    public static boolean isValidNotWhitespaces(String s) {
        if(s == null || s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if(!(c == '\n' || c == '\t' || c == '\0' || c == ' ' || c == '\b' || c == '\r' || c == '\f')) return true;
        } return false;
    }

    public static boolean containsAny(String s, String... values) {
        if(TextUtils.isEmpty(s)) return false;
        String low = s.toLowerCase();
        for(String v : values) {
            if(v != null) {
                if(low.contains(v.toLowerCase()))
                    return true;
            }
        }

        return false;
    }

    public static boolean hasChars(String s, char... cs) {
        if(s == null) return false;
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            for (char cr : cs)
                if (c == cr) return true;
        } return false;
    }

    public static int tryParseInt(String v, int defaultValue) {
        try {
            return Integer.parseInt(v);
        }catch (Exception ignored) {
            return defaultValue;
        }
    }

    public static int tryParseInt(String v) {
        try { return Integer.parseInt(v);
        }catch (Exception e) { return 0; }
    }

    public static Double tryParseDouble(String v) {
        try { return Double.parseDouble(v);
        }catch (Exception e) { return 0.0; }
    }

    public static Float tryParseFloat(String v) {
        try { return Float.parseFloat(v);
        }catch (Exception ignored) { return 0.1F; }
    }


    //9223372036854775807
    public static final int LONG_SIZE = 19; // Maximum number of digits in Long.MAX_VALUE/MIN_VALUE
    public static final String MAX_LONG = String.valueOf(Long.MAX_VALUE);
    public static final String MAX_NEGATIVE_LONG = String.valueOf(Long.MIN_VALUE).substring(1); // Remove the negative sign


    public static byte toNumericByte(char c) {
        switch (c) {
            case '0': return 0;
            case '1': return 1;
            case '2': return 2;
            case '3': return 3;
            case '4': return 4;
            case '5': return 5;
            case '6': return 6;
            case '7': return 7;
            case '8': return 8;
            case '9': return 9;
        }

        return -1;
    }

    public static Long tryParseLong(String v) { return tryParseLong(v, false); }
    public static Long tryParseLong(String v, boolean returnNullIfFailureElseZero) {
        if (v == null || v.isEmpty()) return returnNullIfFailureElseZero ? null : 0L;
        try {
            // First attempt: Try simple parsing if it's a valid number
            try {
                return Long.parseLong(v);
            } catch (NumberFormatException ignored) {
                // Continue with custom parsing
            }

            boolean isNegative = false;
            StringBuilder chs = new StringBuilder();
            char[] chars = v.toCharArray();
            boolean foundFirstNonZeroDigit = false;

            // Check for negative sign at the beginning or after non-digit characters
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '-' && !foundFirstNonZeroDigit && chs.length() == 0) {
                    // Only consider the negative sign if it's followed by digits
                    for (int j = i + 1; j < chars.length; j++) {
                        if (Character.isDigit(chars[j]) && chars[j] != '0') {
                            isNegative = true;
                            break;
                        }
                        if (chars[j] == '0' && j == chars.length - 1) {
                            // If there's only zeros after the negative sign
                            isNegative = true;
                            break;
                        }
                    }
                    continue;
                }

                if (Character.isDigit(chars[i])) {
                    // Skip leading zeros
                    if (chars[i] == '0' && !foundFirstNonZeroDigit && chs.length() == 0) {
                        continue;
                    }

                    if (chars[i] != '0' || chs.length() > 0) {
                        foundFirstNonZeroDigit = true;
                    }

                    chs.append(chars[i]);

                    // Break if we've reached the maximum length for a long
                    if (chs.length() >= LONG_SIZE) {
                        break;
                    }
                }
            }

            String parsed = chs.toString();

            // Handle empty result (only zeros or no digits)
            if (parsed.isEmpty()) {
                // Check if there were any zeros in the input
                for (char c : chars) {
                    if (c == '0') {
                        return isNegative ? -0L : 0L;
                    }
                }
                return returnNullIfFailureElseZero ? null : 0L;
            }

            // Check if the parsed value exceeds max long
            if (parsed.length() == LONG_SIZE) {
                String maxLongValue = isNegative ? MAX_NEGATIVE_LONG : MAX_LONG;

                // Compare each character to determine if we need to truncate
                for (int i = 0; i < parsed.length(); i++) {
                    char pChar = parsed.charAt(i);
                    char mChar = maxLongValue.charAt(i);

                    if (pChar > mChar) {
                        // If this digit exceeds the max, truncate to the maximum valid value
                        if (i == 0) {
                            // If the first digit is already too large, return max/min long
                            return isNegative ? Long.MIN_VALUE : Long.MAX_VALUE;
                        }

                        // Try to parse the substring up to this point minus 1 to ensure it's within range
                        try {
                            long result = Long.parseLong(parsed.substring(0, i));
                            return isNegative ? -result : result;
                        } catch (NumberFormatException e) {
                            // Fallback to max/min if there's a parsing issue
                            return isNegative ? Long.MIN_VALUE : Long.MAX_VALUE;
                        }
                    } else if (pChar < mChar) {
                        // If this digit is less than max, the whole number is valid
                        break;
                    }
                    // If equal, continue checking next digit
                }
            }

            // Parse the final result
            long result = Long.parseLong(parsed);
            return isNegative ? -result : result;
        } catch (Exception ignored) {
            return returnNullIfFailureElseZero ? null : 0L;
        }
    }

    public static String bytesToHex(byte[] bys) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bys) hexString.append(String.format("%02X ", b));
        return hexString.toString();
    }

    public static String toHex(String input) {
        if (input == null) return null;
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            int charValue = input.charAt(i);
            hexString.append(String.format("%04X ", charValue)); // Uses 4 digits to account for Unicode values
        }

        return hexString.toString().trim();
    }

    public static String fromBase64String(String s, Charset characterSet) { return TryRun.getOrDefault(() -> new String(Base64.decode(s, Base64.DEFAULT), characterSet), s); }


    public static String toBase64String(String s, Charset characterSet) {
        if(isEmpty(s)) return s;
        byte[] bytes = s.getBytes(characterSet);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * Safely extracts a substring with extensive error checking.
     *
     * Example:
     *   safeSubstring("Hello World", 6, 11) returns "World"
     *   safeSubstring("Hello World", 6, 999) returns "World"
     *   safeSubstring("Hello World", -5, 5) returns "Hello"
     *   safeSubstring("Hello World", 11, 6) returns "World" (swaps automatically)
     *   safeSubstring(null, 0, 5) returns null
     *
     * @param inputString The input string
     * @param startIndexInclusive The starting index (inclusive, 0-based) - character at this position is included
     * @param endIndexExclusive The ending index (exclusive, 0-based) - character at this position is NOT included
     * @return The substring or the original string if invalid parameters
     */
    public static String subString(String inputString, int startIndexInclusive, int endIndexExclusive) {
        // Check for null or empty string
        if (inputString == null || inputString.isEmpty()) {
            return inputString;
        }

        // Ensure start and end are in correct order (swap if needed)
        if (startIndexInclusive > endIndexExclusive) {
            int temp = startIndexInclusive;
            startIndexInclusive = endIndexExclusive;
            endIndexExclusive = temp;
        }

        // Ensure values are within valid range
        int stringLength = inputString.length();
        startIndexInclusive = Math.max(0, startIndexInclusive);
        endIndexExclusive = Math.min(stringLength, endIndexExclusive);

        // If after adjustments, start is still less than end, return substring
        if (startIndexInclusive < endIndexExclusive) {
            return inputString.substring(startIndexInclusive, endIndexExclusive);
        }

        // Return original string if parameters would result in empty string
        return inputString;
    }

    /**
     * Safely extracts a substring from start index to the end of the string.
     *
     * Example:
     *   safeSubstring("Hello World", 6) returns "World"
     *   safeSubstring("Hello World", -5) returns "Hello World"
     *   safeSubstring("Hello World", 20) returns ""
     *   safeSubstring(null, 5) returns null
     *
     * @param inputString The input string
     * @param startIndexInclusive The starting position (inclusive, 0-based) - extracts from this index to the end
     * @return The substring or the original string if invalid parameters
     */
    public static String subString(String inputString, int startIndexInclusive) {
        // Check for null or empty string
        if (inputString == null || inputString.isEmpty()) {
            return inputString;
        }

        // Ensure start is within valid range
        int stringLength = inputString.length();
        startIndexInclusive = Math.max(0, Math.min(stringLength, startIndexInclusive));

        // If after adjustments, start is still less than length, return substring
        if (startIndexInclusive < stringLength) {
            return inputString.substring(startIndexInclusive);
        }

        // Return empty string if start is at or beyond end of string
        return "";
    }

    public static String getFirstString(String str, String delimiter) { return getFirstString(str, delimiter, null); }
    public static String getFirstString(String str, String delimiter, String defaultValue) {
        if (delimiter == null || delimiter.isEmpty()) return defaultValue != null ? defaultValue : str;
        String trim = trimEx(str, delimiter);
        if (TextUtils.isEmpty(trim)) return defaultValue;       //if default value is Null ?
        if (!trim.contains(delimiter)) return trim;
        String[] split = trim.split(Pattern.quote(delimiter));
        return split.length > 0 ? split[0] : defaultValue;
    }

    public static String getParentPath(String input) {
        if(input == null) return null;
        if(input.contains(File.separator)) {
            int lastSeparatorIndex = input.lastIndexOf(File.separator);
            return lastSeparatorIndex > 0 ? input.substring(0, lastSeparatorIndex) : null;
        }

        return input;
    }

    public static int getEndInteger(String str) { return getEndInteger(str, -1); }
    public static int getEndInteger(String str, int defaultValue) {
        String cleaned = trimControlChars(str);
        if(TextUtils.isEmpty(cleaned) || !Character.isDigit(cleaned.charAt(cleaned.length() - 1)))
            return defaultValue;

        StringBuilder sb = new StringBuilder();
        char[] chars = cleaned.toCharArray();
        for(int i = chars.length - 1; i >= 0; i--) {
            char c = chars[i];
            if(Character.isDigit(c)) {
               sb.append(c);
            } else {
                break;
            }
        }

        return tryGetIntegerFromString(sb.reverse().toString(), defaultValue);
    }

    public static int tryGetIntegerFromString(String s, int defaultValue) {
        if(TextUtils.isEmpty(s)) return defaultValue;
        String t = trimStart(s, "0");
        if(TextUtils.isEmpty(t))  return 0;
        try {
            if(s.length() >= 9) {
                try {
                    return Integer.parseInt(t.substring(t.length() - 9));
                }catch (Exception e) {
                    return Integer.parseInt(t.substring(t.length() - 8));
                }
            } else {
                return Integer.parseInt(t);
            }
        }catch (Exception outerException) {
            return defaultValue;
        }
    }

    public static String repeatString(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }

    public static String getLastStringEx(String str, String delimiter) {
        // Check for null or empty inputs
        if (str == null || str.isEmpty() || delimiter == null || delimiter.isEmpty()) {
            return str;
        }

        //We don't need to use Pattern.quote as none of these functions use RegEx
        str = trimEx(str, true, true, delimiter);

        // Check if the delimiter is in the string
        int lastIndex = str.lastIndexOf(delimiter);
        if (lastIndex == -1) {
            return str; // Return the original string if delimiter is not found
        }

        // Extract and return the last substring after the delimiter
        return str.substring(lastIndex + delimiter.length());
    }


    public static String getLastString(String str, String delimiter) { return getLastString(str, delimiter, null); }
    public static String getLastString(String str, String delimiter, String defaultValue) {
        str = trim(str, delimiter, true, false);
        if(delimiter == null || delimiter.isEmpty()) return defaultValue != null ? defaultValue : str;
        if(str == null || str.isEmpty()) return defaultValue;
        if(!str.contains(delimiter)) return str;
        String[] sp = str.split(Pattern.quote(delimiter));
        return sp.length > 0 ? sp[sp.length - 1] : defaultValue;
    }

    public static String trimOriginal(String s) {
        return Str.isEmpty(s) ? s : s.trim();
    }

    public static String trim(String s, String trimPrefix, boolean ensureTrimmed, boolean trimWhitespace) {
        //Stop using this version ? use the ex version then rename ex to this ?
        if (s == null || s.isEmpty() || trimPrefix == null || trimPrefix.isEmpty()) {
            return s;
        }

        //Make a version so it does multiple trim prefixes "String trimPrefixes..." like "trim("\n\n\t\n\tSomething\n\t\n\t\t\n\n\t\n", "\n", "\n", true, true)

        if(trimWhitespace) {
            s = s.trim();
            if (!s.contains(trimPrefix)) {
                return s;
            }
        }

        int start = 0;
        int end = s.length();
        int prefixLength = trimPrefix.length();

        // Trim from the start
        if (ensureTrimmed) {
            while (start < end && s.startsWith(trimPrefix, start)) {
                start += prefixLength;
            }
        } else if (s.startsWith(trimPrefix)) {
            start = prefixLength;
        }

        // Trim from the end
        if (ensureTrimmed) {
            while (end > start && s.startsWith(trimPrefix, end - prefixLength)) {
                end -= prefixLength;
            }
        } else if (end > prefixLength && s.startsWith(trimPrefix, end - prefixLength)) {
            end -= prefixLength;
        }

        // Only create a new string if we actually trimmed something
        return (start > 0 || end < s.length()) ? trimWhitespace ? s.substring(start, end).trim() : s.substring(start, end) : s;
    }

    /**
     * Trim all Control Chars, \n \t \r \b \0
     */
    public static String trimControlChars(String s) { return trimEx(s, true, true, true, true, CONTROL_CHARS); }


    /**
     * Simplest form - trim both ends with defaults (ensureTrimmed=true, trimWhitespace=true)
     */
    public static String trimEx(String s, String... trimPrefixes) { return trimEx(s, true, true, true, true, trimPrefixes); }

    /**
     * Trims specific end(s) with default options (ensureTrimmed=true, trimWhitespace=true)
     */
    public static String trimEx(String s, boolean trimStart, boolean trimEnd, String... trimPrefixes) { return trimEx(s, true, true, trimStart, trimEnd, trimPrefixes); }

    /**
     * Trims start only with default options (ensureTrimmed=true, trimWhitespace=true)
     */
    public static String trimStart(String s, String... trimPrefixes) { return trimEx(s, true, true, true, false, trimPrefixes); }

    /**
     * Trims end only with default options (ensureTrimmed=true, trimWhitespace=true)
     */
    public static String trimEnd(String s, String... trimPrefixes) { return trimEx(s, true, true, false, true, trimPrefixes); }

    /**
     * Complete trim with all options
     */
    @SuppressWarnings("ConstantConditions")
    public static String trimEx(String s,
                                boolean ensureTrimmed,
                                boolean trimWhitespace,
                                boolean trimStart,
                                boolean trimEnd,
                                String... trimPrefixes) {
        if (s == null || s.isEmpty() || trimPrefixes == null || trimPrefixes.length == 0) {
            return s;
        }

        List<String> validPrefixes = new ArrayList<>();
        if(trimPrefixes != null) {
            for(String p : trimPrefixes) {
                if(!TextUtils.isEmpty(p))
                    validPrefixes.add(p);
            }
        }

        if(trimWhitespace && !validPrefixes.contains(WHITE_SPACE))
            validPrefixes.add(WHITE_SPACE);

        if(validPrefixes.isEmpty())
            return s;

        trimPrefixes = validPrefixes.toArray(new String[0]);

        String previousString;
        do {
            previousString = s;

            for (String prefix : trimPrefixes) {
                if (prefix == null || prefix.isEmpty()) continue;

                int start = 0;
                int end = s.length();
                int prefixLength = prefix.length();

                if (trimStart) {
                    if (ensureTrimmed) {
                        while (start < end && s.startsWith(prefix, start)) {
                            start += prefixLength;
                        }
                    } else if (s.startsWith(prefix)) {
                        start = prefixLength;
                    }
                }

                if (trimEnd) {
                    if (ensureTrimmed) {
                        while (end > start && s.startsWith(prefix, end - prefixLength)) {
                            end -= prefixLength;
                        }
                    } else if (end > prefixLength && s.startsWith(prefix, end - prefixLength)) {
                        end -= prefixLength;
                    }
                }

                if (start > 0 || end < s.length()) {
                    s = s.substring(start, end);
                }
            }
        } while (ensureTrimmed && !s.equals(previousString));

        return s;
    }


    public static List<String> splitToList(String str) { return splitToList(str, ","); }
    public static List<String> splitToList(String str, String delimiter) {
        if(Str.isEmpty(str))
            return ListUtil.emptyList();

        if(!str.contains(delimiter))
            return ListUtil.toSingleList(str);

        String[] split = str.split(Pattern.quote(delimiter));
        if(!ArrayUtils.isValid(split))
            return ListUtil.toSingleList(str);

        List<String> list = new ArrayList<>();
        for(String p : split)
            if(!isEmpty(p) && !list.contains(p))
                list.add(p);

        return list;
    }

    public static String joinArray(String[] arr) { return joinArray(arr, COMMA); }
    public static String joinArray(String[] arr, String delimiter) {
        if(!ArrayUtils.isValid(arr))
            return EMPTY;

        StringBuilder sb = new StringBuilder();
        for (String l : arr) {
            if (isEmpty(l))
                continue;

            if (sb.length() > 0)
                sb.append(delimiter);

            sb.append(l);
        }

        return sb.toString();
    }

    //public static final List<String> STRING_NUMBERS = Arrays.asList("")

    public static boolean isNumeric(String s) {
        if(isEmpty(s))
            return false;

        char[] chars = s.toCharArray();
        for(char c : chars)
            if(!Character.isDigit(c))
                return false;

        return true;
    }

    public static boolean isAlphabet(String s) {
        if(TextUtils.isEmpty(s)) return false;
        char[] chars = s.toCharArray();
        for(char c : chars) {
            if(!Character.isAlphabetic(c)) return false;
        }

        return true;
    }

    public static String getItemAt(String[] array, int index, String defaultValue) {
        if(array == null || array.length <= index)
            return defaultValue;

        String v = array[index];
        return v == null ? defaultValue : v;
    }

    public static boolean isSize(String s, int wantedCount) { return s != null && s.length() == wantedCount;  }
    public static int getSize(String s) { return s == null ? -1 : s.length(); }

    public static boolean isSpecialChar(char c) { return c == '\n' || c == '\t' || c == '\b' || c == ' ' || c == '\r'; }


    public static int firstItemThatEndsWithInListIndex(List<String> list, String compare, boolean ignoreCase, int maxCount) {
        if(!ListUtil.isValid(list) || compare == null)
            return -1;

        compare = ignoreCase ? compare.toLowerCase() : compare;
        int sz = maxCount > 0 ? Math.min(ListUtil.size(list), maxCount) : ListUtil.size(list);
        for(int i = 0; i < sz; i++) {
            String item = list.get(i);
            if (item == null)
                continue;

            if (ignoreCase) {
                if (compare.endsWith(item.toLowerCase()))
                    return i;
            }
            else if(compare.endsWith(item))
                return i;
        }

        return -1;
    }

    public static boolean isAnyEndsWithList(List<String> list, String compare, boolean ignoreCase, int maxCount) { return firstItemThatEndsWithInListIndex(list, compare, ignoreCase, maxCount) > -1; }

    public static boolean isAnyEqualsList(List<String> list, String compare, boolean ignoreCase, int maxCount) {
        if(!ListUtil.isValid(list) || compare == null)
            return false;

        int sz = maxCount > 0 ? Math.min(ListUtil.size(list), maxCount) : ListUtil.size(list);
        for(int i = 0; i < sz; i++) {
            String item = list.get(i);
            if(item == null)
                continue;

            if(ignoreCase) {
                if(compare.equalsIgnoreCase(item))
                    return true;
            }
            else if(compare.equals(item))
                    return true;
        }

        return false;
    }

    public static List<String> breakCommandString(String s) {
        if(Str.isEmpty(s))
            return ListUtil.emptyList();

        boolean isInQuotes = false;
        List<String> parts = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        try {
            char[] chars = s.toCharArray();
            for(char c : chars) {
                if(c == '\"' || c == '\'') {
                    if(isInQuotes) {
                        isInQuotes = false;
                        if(sb.length() > 0)
                            parts.add(sb.toString());

                        sb = new StringBuilder();
                    } else {
                        isInQuotes = true;
                    }
                }
                else if(isInQuotes) {
                    sb.append(c);
                }
                else {
                    if(c == ' ' || c == '\t' || c == '\b' || c == '\r' || c == '\0') {
                        if(sb.length() > 0)
                            parts.add(sb.toString());

                        sb = new StringBuilder();
                        continue;
                    }

                    sb.append(c);
                }
            }
        }catch (Exception e) {
            Log.e(TAG, "Error Breaking Command String: Error=" + e + " S=" + s);
        }

        if(sb.length() > 0)
            parts.add(sb.toString());

        return parts;
    }

    public static String joinList(Collection<String> list) { return joinList(list, COMMA); }
    public static String joinList(Collection<String> list, String delimiter) {
        if(!ListUtil.isValid(list))
            return EMPTY;

        StringBuilder sb = new StringBuilder();
        for(String s : list) {
            if(isEmpty(s))
                continue;

            if(sb.length() > 0) sb.append(delimiter);
            sb.append(s);
        }

        return sb.toString();
    }



    /*
    public static String joinList(List<String> list, String delimiter, int startIndex, int endIndex) {
        if(list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < list.size(); i++) {
            String l = list.get(i);
            if(TextUtils.isEmpty(l)) continue;
            if(sb.length() > 0) sb.append(delimiter);
            sb.append(l);
        }

        return sb.toString();
    }*/


    public static boolean toBool(String str) { return toBool(str, false); }
    public static boolean toBool(String str, Boolean defaultValue) {
        try {
            if(str == null || TextUtils.isEmpty(str)) return defaultValue;
            str = str.trim().toLowerCase();
            if(str.equals("yes") || str.equals("true") || str.equals("1") || str.equals("checked") || str.equals("enabled") || str.equals("succeed") || str.equals("succeeded")) return true;
            if(str.equals("no") || str.equals("false") || str.equals("0") || str.equals("unchecked") || str.equals("disabled") || str.equals("fail") || str.equals("failed") || str.equals("error")) return false;
            return defaultValue;
        }catch (Exception ex) {
            return defaultValue;
        }
    }

    public static boolean endsWithAny(String s, String... stringsToCheckFor) { return endsWithAny(s, false, stringsToCheckFor); }
    public static boolean endsWithAny(String s, boolean ignoreCase, String... stringsToCheckFor) {
        if(!isValid(s) || !ArrayUtils.isValid(stringsToCheckFor))
            return false;

        String str = ignoreCase ?  s.toLowerCase() : s;
        for(String c : stringsToCheckFor) {
            if(!isValid(c))
                continue;

            if(ignoreCase)
                if(str.endsWith(c.toLowerCase()))
                    return true;
                else if(str.endsWith(c))
                    return true;
        }

        return false;
    }

    public static boolean endsWith(String s, String checkFor) { return endsWith(s, checkFor, false); }
    public static boolean endsWith(String s, String checkFor, boolean ignoreCase) {
        if(!isEmpty(s) || checkFor == null)
            return false;

        if(ignoreCase) {
            String loweredOne = s.toLowerCase();
            String loweredTwo = checkFor.toLowerCase();
            return loweredOne.endsWith(loweredTwo);
        }

        return s.endsWith(checkFor);
    }

    public static boolean startsWith(String s, String checkFor) { return startsWith(s, checkFor, false); }
    public static boolean startsWith(String s, String checkFor, boolean ignoreCase) {
        if(isEmpty(s) || checkFor == null)
            return false;

        if(ignoreCase) {
            String loweredOne = s.toLowerCase();
            String loweredTwo = checkFor.toLowerCase();
            return loweredOne.startsWith(loweredTwo);
        }

        return s.startsWith(checkFor);
    }

    public static boolean startsWithAny(String s, String... stringsToCheckFor) { return startsWithAny(s, false, stringsToCheckFor); }
    public static boolean startsWithAny(String s, boolean ignoreCase, String... stringsToCheckFor) {
        if(!isValid(s) || !ArrayUtils.isValid(stringsToCheckFor))
            return false;

        String str = ignoreCase ?  s.toLowerCase() : s;
        for(String c : stringsToCheckFor) {
            if(!isValid(c))
                continue;

            if(ignoreCase)
                if(str.startsWith(c.toLowerCase()))
                    return true;
            else if(str.startsWith(c))
                    return true;
        }

        return false;
    }

    public static String ensureNotStartWith(String s, String start) { return (isEmpty(s) || isEmpty(start)) ? s : (s.startsWith(start) ? s.substring(start.length()) : s); }

    public static String ensureStartsWith(String s, String start) { return s == null ? start : s.startsWith(start) ? s : s + start; }
    public static String ensureEndsWith(String s, String ending) { return s == null ? ending : s.endsWith(ending) ? s : s + ending; }


    public static Boolean toBoolean(String str) { return toBoolean(str, null); }
    public static Boolean toBoolean(String str, Boolean defaultValue) {
        try {
            if(str == null || TextUtils.isEmpty(str)) return defaultValue;
            str = str.trim().toLowerCase();
            if(str.equals("yes") || str.equals("true") || str.equals("1") || str.equals("checked") || str.equals("enabled") || str.equals("succeed") || str.equals("succeeded")) return true;
            if(str.equals("no") || str.equals("false") || str.equals("0") || str.equals("unchecked") || str.equals("disabled") || str.equals("fail") || str.equals("failed") || str.equals("error")) return false;
            return defaultValue;
        }catch (Exception ex) {
            return defaultValue;
        }
    }


    /**
     * Creates a divider with centered text. Handles all edge cases safely.
     *
     * @param dividerChar The character to use for the divider (or default if invalid)
     * @param text The text to center in the divider (or empty if null)
     * @return A string with the text centered between divider characters
     */
    public static String dividerWithTitle(char dividerChar, String text) {
        // Validate/default the divider char (avoid control chars, zero-width chars, etc)
        char safeChar = isSafeDividerChar(dividerChar) ? dividerChar : DEFAULT_DIVIDER;

        // Normalize text input
        String safeText = normalizeDividerText(text);

        // If text is empty, return simple divider
        if (safeText.isEmpty()) {
            return repeatChar(safeChar, MOBILE_SAFE_LENGTH);
        }

        try {
            // Add spaces around text for padding, with bounds checking
            String paddedText = " " + safeText + " ";

            // Safety check for string length overflow
            if (paddedText.length() >= MOBILE_SAFE_LENGTH - MIN_DIVIDER_LENGTH) {
                // Text too long - truncate and add ellipsis
                int maxLength = MOBILE_SAFE_LENGTH - MIN_DIVIDER_LENGTH - 3; // space for "..."
                if (maxLength < 1) {
                    return repeatChar(safeChar, MOBILE_SAFE_LENGTH); // fallback to simple divider
                }
                paddedText = " " + safeText.substring(0, maxLength) + "... ";
            }

            // Calculate remaining space for dividers
            int remainingSpace = Math.max(MIN_DIVIDER_LENGTH,
                    MOBILE_SAFE_LENGTH - paddedText.length());

            // Calculate divider lengths, ensuring left side is smaller when uneven
            int rightDivLength = Math.max(MIN_DIVIDER_LENGTH/2, (remainingSpace + 1) / 2);
            int leftDivLength = Math.max(MIN_DIVIDER_LENGTH/2, remainingSpace - rightDivLength);

            // Final safety check for total length
            String result = repeatChar(safeChar, leftDivLength) +
                    paddedText +
                    repeatChar(safeChar, rightDivLength);

            // Ensure we don't exceed MOBILE_SAFE_LENGTH
            if (result.length() > MOBILE_SAFE_LENGTH) {
                return result.substring(0, MOBILE_SAFE_LENGTH);
            }

            return result;

        } catch (Exception e) {
            // Absolute fallback - return simple divider
            return repeatChar(safeChar, MOBILE_SAFE_LENGTH);
        }
    }

    /**
     * Safe character repeater that handles negative numbers and other edge cases
     */
    public static String repeatChar(char c, int length) {
        // Handle invalid lengths
        if (length <= 0) return "";
        if (length == 1) return String.valueOf(c);

        try {
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length && i < MOBILE_SAFE_LENGTH; i++) {
                sb.append(c);
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(DEFAULT_DIVIDER);
        }
    }

    /**
     * Validates and normalizes input text
     */
    public static String normalizeDividerText(@Nullable String text) {
        if (text == null) return DEFAULT_TEXT;
        // Remove control characters and trim
        String cleaned = text.replaceAll("[\\p{Cntrl}]", "").trim();
        // If empty after cleaning, return default
        if (cleaned.isEmpty()) return DEFAULT_TEXT;
        return cleaned;
    }

    /**
     * Checks if a character is safe to use as a divider
     */
    public static boolean isSafeDividerChar(char c) {
        // Avoid control characters, zero-width characters, and other problematic chars
        return c > 32 && c < 127 && // Basic ASCII printable
                c != 127 && // DEL
                Character.isValidCodePoint(c) &&
                !Character.isSpaceChar(c) &&
                !Character.isISOControl(c);
    }

    public static String hexDump(ByteBuffer buffer) {
        // Store original position
        int originalPosition = buffer.position();

        // Reset to beginning for reading
        buffer.position(0);

        int width = 16; // bytes per line
        StringBuilder builder = new StringBuilder();
        StringBuilder asciiBuilder = new StringBuilder();

        int count = 0;
        while (buffer.hasRemaining()) {
            if (count % width == 0) {
                // Print the offset at the beginning of the line
                if (count > 0) {
                    builder.append("  ");
                    builder.append(asciiBuilder.toString());
                    builder.append("\n");
                    asciiBuilder.setLength(0);
                }
                builder.append(String.format("%08X: ", count));
            }

            byte b = buffer.get();
            builder.append(String.format("%02X ", b & 0xFF));

            // Add to ASCII representation (if printable)
            if (b >= 32 && b < 127) {
                asciiBuilder.append((char) b);
            } else {
                asciiBuilder.append('.');
            }

            count++;
        }

        // Handle the last line
        int remaining = width - (count % width);
        if (remaining < width) {
            for (int i = 0; i < remaining; i++) {
                builder.append("   ");
            }
            builder.append("  ");
            builder.append(asciiBuilder.toString());
        }

        //System.out.println(builder.toString());

        // Restore original position
        buffer.position(originalPosition);
        return builder.toString();
    }

}
