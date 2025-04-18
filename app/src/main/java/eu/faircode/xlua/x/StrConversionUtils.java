package eu.faircode.xlua.x;

/**
 * Utility class for converting strings to various numeric types.
 * Provides methods that attempt to extract valid numeric values from strings
 * by any means possible, even from mixed content or values that would exceed type limits.
 */
public class StrConversionUtils {

    // Constants for maximum sizes and values
    private static final int BYTE_SIZE = 3; // Max 127
    private static final int SHORT_SIZE = 5; // Max 32767
    private static final int INT_SIZE = 10; // Max 2147483647
    private static final int LONG_SIZE = 19; // Max 9223372036854775807

    private static final String MAX_BYTE = String.valueOf(Byte.MAX_VALUE);
    private static final String MAX_NEGATIVE_BYTE = String.valueOf(Math.abs((int)Byte.MIN_VALUE));
    private static final String MAX_UNSIGNED_BYTE = String.valueOf((short)(Byte.MAX_VALUE * 2 + 1));

    private static final String MAX_SHORT = String.valueOf(Short.MAX_VALUE);
    private static final String MAX_NEGATIVE_SHORT = String.valueOf(Math.abs((int)Short.MIN_VALUE));
    private static final String MAX_UNSIGNED_SHORT = String.valueOf(Short.MAX_VALUE * 2 + 1);

    private static final String MAX_INT = String.valueOf(Integer.MAX_VALUE);
    private static final String MAX_NEGATIVE_INT = String.valueOf(Math.abs((long)Integer.MIN_VALUE));
    private static final String MAX_UNSIGNED_INT = String.valueOf((long)Integer.MAX_VALUE * 2L + 1L);

    private static final String MAX_LONG = String.valueOf(Long.MAX_VALUE);
    private static final String MAX_NEGATIVE_LONG = String.valueOf(Long.MIN_VALUE).substring(1); // Remove the negative sign

    // ============================
    // Byte Conversion Methods
    // ============================

    /**
     * Attempts to parse a string to a Byte, returning 0 if parsing fails.
     */
    public static Byte tryParseByte(String v) {
        return tryParseByte(v, false);
    }

    /**
     * Attempts to parse a string to a Byte, with option to return null on failure.
     *
     * @param v The string to parse
     * @param returnNullIfFailureElseZero Whether to return null (true) or 0 (false) on failure
     * @return The parsed Byte value, or null/0 on failure
     */
    public static Byte tryParseByte(String v, boolean returnNullIfFailureElseZero) {
        if (v == null || v.isEmpty()) return returnNullIfFailureElseZero ? null : (byte)0;

        try {
            // First try direct parsing
            try {
                return Byte.parseByte(v);
            } catch (NumberFormatException ignored) {
                // Continue with custom parsing
            }

            boolean isNegative = false;
            StringBuilder chs = new StringBuilder();
            char[] chars = v.toCharArray();
            boolean foundFirstNonZeroDigit = false;

            // Check for negative sign
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '-' && !foundFirstNonZeroDigit && chs.length() == 0) {
                    // Look ahead for digits after the negative sign
                    for (int j = i + 1; j < chars.length; j++) {
                        if (Character.isDigit(chars[j]) && chars[j] != '0') {
                            isNegative = true;
                            break;
                        }
                        if (chars[j] == '0' && j == chars.length - 1) {
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

                    // Break if we've reached the maximum length
                    if (chs.length() >= BYTE_SIZE) {
                        break;
                    }
                }
            }

            String parsed = chs.toString();

            // Handle empty result
            if (parsed.isEmpty()) {
                for (char c : chars) {
                    if (c == '0') {
                        return isNegative ? (byte)-0 : (byte)0;
                    }
                }
                return returnNullIfFailureElseZero ? null : (byte)0;
            }

            // Check if value exceeds byte limits
            if (parsed.length() == BYTE_SIZE) {
                String maxValue = isNegative ? MAX_NEGATIVE_BYTE : MAX_BYTE;

                for (int i = 0; i < parsed.length(); i++) {
                    char pChar = parsed.charAt(i);
                    char mChar = maxValue.charAt(i);

                    if (pChar > mChar) {
                        if (i == 0) {
                            return isNegative ? Byte.MIN_VALUE : Byte.MAX_VALUE;
                        }

                        try {
                            byte result = Byte.parseByte(parsed.substring(0, i));
                            return isNegative ? (byte)-result : result;
                        } catch (NumberFormatException e) {
                            return isNegative ? Byte.MIN_VALUE : Byte.MAX_VALUE;
                        }
                    } else if (pChar < mChar) {
                        break;
                    }
                }
            }

            // Parse final result
            byte result = Byte.parseByte(parsed);
            return isNegative ? (byte)-result : result;

        } catch (Exception ignored) {
            return returnNullIfFailureElseZero ? null : (byte)0;
        }
    }

    /**
     * Attempts to parse a string to an unsigned byte (0-255), returning 0 if parsing fails.
     */
    public static Short tryParseUnsignedByte(String v) {
        return tryParseUnsignedByte(v, false);
    }

    /**
     * Attempts to parse a string to an unsigned byte (0-255), with option to return null on failure.
     *
     * @param v The string to parse
     * @param returnNullIfFailureElseZero Whether to return null (true) or 0 (false) on failure
     * @return The parsed unsigned byte value as a Short, or null/0 on failure
     */
    public static Short tryParseUnsignedByte(String v, boolean returnNullIfFailureElseZero) {
        if (v == null || v.isEmpty()) return returnNullIfFailureElseZero ? null : (short)0;

        try {
            // First try direct parsing (unsigned byte stored in a short)
            try {
                int value = Integer.parseInt(v);
                if (value >= 0 && value <= 255) {
                    return (short)value;
                }
            } catch (NumberFormatException ignored) {
                // Continue with custom parsing
            }

            StringBuilder chs = new StringBuilder();
            char[] chars = v.toCharArray();
            boolean foundFirstNonZeroDigit = false;

            // Extract digits
            for (char c : chars) {
                if (Character.isDigit(c)) {
                    // Skip leading zeros
                    if (c == '0' && !foundFirstNonZeroDigit && chs.length() == 0) {
                        continue;
                    }

                    if (c != '0' || chs.length() > 0) {
                        foundFirstNonZeroDigit = true;
                    }

                    chs.append(c);

                    // Break if max length reached
                    if (chs.length() >= MAX_UNSIGNED_BYTE.length()) {
                        break;
                    }
                }
            }

            String parsed = chs.toString();

            // Handle empty result
            if (parsed.isEmpty()) {
                for (char c : chars) {
                    if (c == '0') {
                        return 0;
                    }
                }
                return returnNullIfFailureElseZero ? null : (short)0;
            }

            // Check if value exceeds unsigned byte limit
            if (parsed.length() == MAX_UNSIGNED_BYTE.length()) {
                for (int i = 0; i < parsed.length(); i++) {
                    char pChar = parsed.charAt(i);
                    char mChar = MAX_UNSIGNED_BYTE.charAt(i);

                    if (pChar > mChar) {
                        if (i == 0) {
                            return 255;
                        }

                        try {
                            int result = Integer.parseInt(parsed.substring(0, i));
                            return result > 255 ? (short)255 : (short)result;
                        } catch (NumberFormatException e) {
                            return 255;
                        }
                    } else if (pChar < mChar) {
                        break;
                    }
                }
            }

            // Parse final result
            int result = Integer.parseInt(parsed);
            return result > 255 ? (short)255 : (short)result;

        } catch (Exception ignored) {
            return returnNullIfFailureElseZero ? null : (short)0;
        }
    }

    // ============================
    // Short Conversion Methods
    // ============================

    /**
     * Attempts to parse a string to a Short, returning 0 if parsing fails.
     */
    public static Short tryParseShort(String v) {
        return tryParseShort(v, false);
    }

    /**
     * Attempts to parse a string to a Short, with option to return null on failure.
     *
     * @param v The string to parse
     * @param returnNullIfFailureElseZero Whether to return null (true) or 0 (false) on failure
     * @return The parsed Short value, or null/0 on failure
     */
    public static Short tryParseShort(String v, boolean returnNullIfFailureElseZero) {
        if (v == null || v.isEmpty()) return returnNullIfFailureElseZero ? null : (short)0;

        try {
            // First try direct parsing
            try {
                return Short.parseShort(v);
            } catch (NumberFormatException ignored) {
                // Continue with custom parsing
            }

            boolean isNegative = false;
            StringBuilder chs = new StringBuilder();
            char[] chars = v.toCharArray();
            boolean foundFirstNonZeroDigit = false;

            // Check for negative sign
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '-' && !foundFirstNonZeroDigit && chs.length() == 0) {
                    // Look ahead for digits after the negative sign
                    for (int j = i + 1; j < chars.length; j++) {
                        if (Character.isDigit(chars[j]) && chars[j] != '0') {
                            isNegative = true;
                            break;
                        }
                        if (chars[j] == '0' && j == chars.length - 1) {
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

                    // Break if we've reached the maximum length
                    if (chs.length() >= SHORT_SIZE) {
                        break;
                    }
                }
            }

            String parsed = chs.toString();

            // Handle empty result
            if (parsed.isEmpty()) {
                for (char c : chars) {
                    if (c == '0') {
                        return isNegative ? (short)-0 : (short)0;
                    }
                }
                return returnNullIfFailureElseZero ? null : (short)0;
            }

            // Check if value exceeds short limits
            if (parsed.length() == SHORT_SIZE) {
                String maxValue = isNegative ? MAX_NEGATIVE_SHORT : MAX_SHORT;

                for (int i = 0; i < parsed.length(); i++) {
                    char pChar = parsed.charAt(i);
                    char mChar = maxValue.charAt(i);

                    if (pChar > mChar) {
                        if (i == 0) {
                            return isNegative ? Short.MIN_VALUE : Short.MAX_VALUE;
                        }

                        try {
                            short result = Short.parseShort(parsed.substring(0, i));
                            return isNegative ? (short)-result : result;
                        } catch (NumberFormatException e) {
                            return isNegative ? Short.MIN_VALUE : Short.MAX_VALUE;
                        }
                    } else if (pChar < mChar) {
                        break;
                    }
                }
            }

            // Parse final result
            short result = Short.parseShort(parsed);
            return isNegative ? (short)-result : result;

        } catch (Exception ignored) {
            return returnNullIfFailureElseZero ? null : (short)0;
        }
    }

    /**
     * Attempts to parse a string to an unsigned short (0-65535), returning 0 if parsing fails.
     */
    public static Integer tryParseUnsignedShort(String v) {
        return tryParseUnsignedShort(v, false);
    }

    /**
     * Attempts to parse a string to an unsigned short (0-65535), with option to return null on failure.
     *
     * @param v The string to parse
     * @param returnNullIfFailureElseZero Whether to return null (true) or 0 (false) on failure
     * @return The parsed unsigned short value as an Integer, or null/0 on failure
     */
    public static Integer tryParseUnsignedShort(String v, boolean returnNullIfFailureElseZero) {
        if (v == null || v.isEmpty()) return returnNullIfFailureElseZero ? null : 0;

        try {
            // First try direct parsing
            try {
                int value = Integer.parseInt(v);
                if (value >= 0 && value <= 65535) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Continue with custom parsing
            }

            StringBuilder chs = new StringBuilder();
            char[] chars = v.toCharArray();
            boolean foundFirstNonZeroDigit = false;

            // Extract digits
            for (char c : chars) {
                if (Character.isDigit(c)) {
                    // Skip leading zeros
                    if (c == '0' && !foundFirstNonZeroDigit && chs.length() == 0) {
                        continue;
                    }

                    if (c != '0' || chs.length() > 0) {
                        foundFirstNonZeroDigit = true;
                    }

                    chs.append(c);

                    // Break if max length reached
                    if (chs.length() >= MAX_UNSIGNED_SHORT.length()) {
                        break;
                    }
                }
            }

            String parsed = chs.toString();

            // Handle empty result
            if (parsed.isEmpty()) {
                for (char c : chars) {
                    if (c == '0') {
                        return 0;
                    }
                }
                return returnNullIfFailureElseZero ? null : 0;
            }

            // Check if value exceeds unsigned short limit
            if (parsed.length() == MAX_UNSIGNED_SHORT.length()) {
                for (int i = 0; i < parsed.length(); i++) {
                    char pChar = parsed.charAt(i);
                    char mChar = MAX_UNSIGNED_SHORT.charAt(i);

                    if (pChar > mChar) {
                        if (i == 0) {
                            return 65535;
                        }

                        try {
                            int result = Integer.parseInt(parsed.substring(0, i));
                            return Math.min(result, 65535);
                        } catch (NumberFormatException e) {
                            return 65535;
                        }
                    } else if (pChar < mChar) {
                        break;
                    }
                }
            }

            // Parse final result
            int result = Integer.parseInt(parsed);
            return Math.min(result, 65535);

        } catch (Exception ignored) {
            return returnNullIfFailureElseZero ? null : 0;
        }
    }

    // ============================
    // Integer Conversion Methods
    // ============================

    /**
     * Attempts to parse a string to an Integer, returning 0 if parsing fails.
     */
    public static Integer tryParseInt(String v) {
        return tryParseInt(v, false);
    }

    /**
     * Attempts to parse a string to an Integer, with option to return null on failure.
     *
     * @param v The string to parse
     * @param returnNullIfFailureElseZero Whether to return null (true) or 0 (false) on failure
     * @return The parsed Integer value, or null/0 on failure
     */
    public static Integer tryParseInt(String v, boolean returnNullIfFailureElseZero) {
        if (v == null || v.isEmpty()) return returnNullIfFailureElseZero ? null : 0;

        try {
            // First try direct parsing
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException ignored) {
                // Continue with custom parsing
            }

            boolean isNegative = false;
            StringBuilder chs = new StringBuilder();
            char[] chars = v.toCharArray();
            boolean foundFirstNonZeroDigit = false;

            // Check for negative sign
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '-' && !foundFirstNonZeroDigit && chs.length() == 0) {
                    // Look ahead for digits after the negative sign
                    for (int j = i + 1; j < chars.length; j++) {
                        if (Character.isDigit(chars[j]) && chars[j] != '0') {
                            isNegative = true;
                            break;
                        }
                        if (chars[j] == '0' && j == chars.length - 1) {
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

                    // Break if we've reached the maximum length
                    if (chs.length() >= INT_SIZE) {
                        break;
                    }
                }
            }

            String parsed = chs.toString();

            // Handle empty result
            if (parsed.isEmpty()) {
                for (char c : chars) {
                    if (c == '0') {
                        return isNegative ? -0 : 0;
                    }
                }
                return returnNullIfFailureElseZero ? null : 0;
            }

            // Check if value exceeds int limits
            if (parsed.length() == INT_SIZE) {
                String maxValue = isNegative ? MAX_NEGATIVE_INT : MAX_INT;

                for (int i = 0; i < parsed.length(); i++) {
                    char pChar = parsed.charAt(i);
                    char mChar = maxValue.charAt(i);

                    if (pChar > mChar) {
                        if (i == 0) {
                            return isNegative ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                        }

                        try {
                            int result = Integer.parseInt(parsed.substring(0, i));
                            return isNegative ? -result : result;
                        } catch (NumberFormatException e) {
                            return isNegative ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                        }
                    } else if (pChar < mChar) {
                        break;
                    }
                }
            }

            // Parse final result
            int result = Integer.parseInt(parsed);
            return isNegative ? -result : result;

        } catch (Exception ignored) {
            return returnNullIfFailureElseZero ? null : 0;
        }
    }

    /**
     * Attempts to parse a string to an Integer, with option to return a default value on failure.
     *
     * @param v The string to parse
     * @param defaultValue The value to return if parsing fails (can be null)
     * @return The parsed Integer value, or the defaultValue on failure
     */
    public static Integer tryParseInt(String v, Integer defaultValue) {
        if (v == null || v.isEmpty()) return defaultValue;

        try {
            // First try direct parsing
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException ignored) {
                // Continue with custom parsing
            }

            boolean isNegative = false;
            StringBuilder chs = new StringBuilder();
            char[] chars = v.toCharArray();
            boolean foundFirstNonZeroDigit = false;

            // Check for negative sign
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '-' && !foundFirstNonZeroDigit && chs.length() == 0) {
                    // Look ahead for digits after the negative sign
                    for (int j = i + 1; j < chars.length; j++) {
                        if (Character.isDigit(chars[j]) && chars[j] != '0') {
                            isNegative = true;
                            break;
                        }
                        if (chars[j] == '0' && j == chars.length - 1) {
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

                    // Break if we've reached the maximum length
                    if (chs.length() >= INT_SIZE) {
                        break;
                    }
                }
            }

            String parsed = chs.toString();

            // Handle empty result
            if (parsed.isEmpty()) {
                for (char c : chars) {
                    if (c == '0') {
                        return isNegative ? -0 : 0;
                    }
                }
                return defaultValue;
            }

            // Check if value exceeds int limits
            if (parsed.length() == INT_SIZE) {
                String maxValue = isNegative ? MAX_NEGATIVE_INT : MAX_INT;

                for (int i = 0; i < parsed.length(); i++) {
                    char pChar = parsed.charAt(i);
                    char mChar = maxValue.charAt(i);

                    if (pChar > mChar) {
                        if (i == 0) {
                            return isNegative ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                        }

                        try {
                            int result = Integer.parseInt(parsed.substring(0, i));
                            return isNegative ? -result : result;
                        } catch (NumberFormatException e) {
                            return isNegative ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                        }
                    } else if (pChar < mChar) {
                        break;
                    }
                }
            }

            // Parse final result
            int result = Integer.parseInt(parsed);
            return isNegative ? -result : result;

        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    /**
     * Attempts to parse a string to an unsigned int (0-4294967295), returning 0 if parsing fails.
     */
    public static Long tryParseUnsignedInt(String v) {
        return tryParseUnsignedInt(v, false);
    }

    /**
     * Attempts to parse a string to an unsigned int (0-4294967295), with option to return null on failure.
     *
     * @param v The string to parse
     * @param returnNullIfFailureElseZero Whether to return null (true) or 0 (false) on failure
     * @return The parsed unsigned int value as a Long, or null/0 on failure
     */
    public static Long tryParseUnsignedInt(String v, boolean returnNullIfFailureElseZero) {
        if (v == null || v.isEmpty()) return returnNullIfFailureElseZero ? null : 0L;

        try {
            // First try direct parsing
            try {
                long value = Long.parseLong(v);
                if (value >= 0 && value <= 4294967295L) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Continue with custom parsing
            }

            StringBuilder chs = new StringBuilder();
            char[] chars = v.toCharArray();
            boolean foundFirstNonZeroDigit = false;

            // Extract digits
            for (char c : chars) {
                if (Character.isDigit(c)) {
                    // Skip leading zeros
                    if (c == '0' && !foundFirstNonZeroDigit && chs.length() == 0) {
                        continue;
                    }

                    if (c != '0' || chs.length() > 0) {
                        foundFirstNonZeroDigit = true;
                    }

                    chs.append(c);

                    // Break if max length reached
                    if (chs.length() >= MAX_UNSIGNED_INT.length()) {
                        break;
                    }
                }
            }

            String parsed = chs.toString();

            // Handle empty result
            if (parsed.isEmpty()) {
                for (char c : chars) {
                    if (c == '0') {
                        return 0L;
                    }
                }
                return returnNullIfFailureElseZero ? null : 0L;
            }

            // Check if value exceeds unsigned int limit
            if (parsed.length() == MAX_UNSIGNED_INT.length()) {
                for (int i = 0; i < parsed.length(); i++) {
                    char pChar = parsed.charAt(i);
                    char mChar = MAX_UNSIGNED_INT.charAt(i);

                    if (pChar > mChar) {
                        if (i == 0) {
                            return 4294967295L;
                        }

                        try {
                            long result = Long.parseLong(parsed.substring(0, i));
                            return Math.min(result, 4294967295L);
                        } catch (NumberFormatException e) {
                            return 4294967295L;
                        }
                    } else if (pChar < mChar) {
                        break;
                    }
                }
            }

            // Parse final result
            long result = Long.parseLong(parsed);
            return Math.min(result, 4294967295L);

        } catch (Exception ignored) {
            return returnNullIfFailureElseZero ? null : 0L;
        }
    }

    // ============================
    // Long Conversion Methods
    // ============================

    /**
     * Attempts to parse a string to a Long, returning 0 if parsing fails.
     */
    public static Long tryParseLong(String v) {
        return tryParseLong(v, false);
    }


    /**
     * Attempts to parse a string to a Long, with option to return null on failure.
     *
     * @param v The string to parse
     * @param returnNullIfFailureElseZero Whether to return null (true) or 0 (false) on failure
     * @return The parsed Long value, or null/0 on failure
     */
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

                        // Try to parse the substring up to this point to ensure it's within range
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

    /**
     * Attempts to parse a string to an unsigned long (up to 2^64-1),
     * returning BigInteger.ZERO if parsing fails.
     */
    public static java.math.BigInteger tryParseUnsignedLong(String v) {
        return tryParseUnsignedLong(v, false);
    }

    /**
     * Attempts to parse a string to an unsigned long (up to 2^64-1),
     * with option to return null on failure.
     *
     * @param v The string to parse
     * @param returnNullIfFailureElseZero Whether to return null (true) or BigInteger.ZERO (false) on failure
     * @return The parsed unsigned long value as a BigInteger, or null/BigInteger.ZERO on failure
     */
    public static java.math.BigInteger tryParseUnsignedLong(String v, boolean returnNullIfFailureElseZero) {
        java.math.BigInteger UNSIGNED_LONG_MAX = new java.math.BigInteger("18446744073709551615"); // 2^64 - 1

        if (v == null || v.isEmpty()) {
            return returnNullIfFailureElseZero ? null : java.math.BigInteger.ZERO;
        }

        try {
            // First try direct parsing
            try {
                java.math.BigInteger value = new java.math.BigInteger(v);
                if (value.compareTo(java.math.BigInteger.ZERO) >= 0 && value.compareTo(UNSIGNED_LONG_MAX) <= 0) {
                    return value;
                } else if (value.compareTo(UNSIGNED_LONG_MAX) > 0) {
                    return UNSIGNED_LONG_MAX;
                }
            } catch (NumberFormatException ignored) {
                // Continue with custom parsing
            }

            StringBuilder chs = new StringBuilder();
            char[] chars = v.toCharArray();
            boolean foundFirstNonZeroDigit = false;

            // Extract digits
            for (char c : chars) {
                if (Character.isDigit(c)) {
                    // Skip leading zeros
                    if (c == '0' && !foundFirstNonZeroDigit && chs.length() == 0) {
                        continue;
                    }

                    if (c != '0' || chs.length() > 0) {
                        foundFirstNonZeroDigit = true;
                    }

                    chs.append(c);

                    // Check if we've definitely exceeded the max value
                    if (chs.length() > 20) { // More digits than max unsigned long
                        return UNSIGNED_LONG_MAX;
                    }
                }
            }

            String parsed = chs.toString();

            // Handle empty result
            if (parsed.isEmpty()) {
                for (char c : chars) {
                    if (c == '0') {
                        return java.math.BigInteger.ZERO;
                    }
                }
                return returnNullIfFailureElseZero ? null : java.math.BigInteger.ZERO;
            }

            // Parse the result and check limits
            java.math.BigInteger result = new java.math.BigInteger(parsed);
            if (result.compareTo(UNSIGNED_LONG_MAX) > 0) {
                return UNSIGNED_LONG_MAX;
            }
            return result;

        } catch (Exception ignored) {
            return returnNullIfFailureElseZero ? null : java.math.BigInteger.ZERO;
        }
    }

    // ============================
    // Float and Double Conversion Methods
    // ============================

    /**
     * Attempts to parse a string to a Float, returning 0.0f if parsing fails.
     */
    public static Float tryParseFloat(String v) {
        return tryParseFloat(v, false);
    }

    /**
     * Attempts to parse a string to a Float, with option to return null on failure.
     *
     * @param v The string to parse
     * @param returnNullIfFailureElseZero Whether to return null (true) or 0.0f (false) on failure
     * @return The parsed Float value, or null/0.0f on failure
     */
    public static Float tryParseFloat(String v, boolean returnNullIfFailureElseZero) {
        if (v == null || v.isEmpty()) {
            return returnNullIfFailureElseZero ? null : 0.0f;
        }

        try {
            // Try to find a valid floating-point number pattern in the string
            StringBuilder builder = new StringBuilder();
            boolean foundDecimal = false;
            boolean foundDigit = false;
            boolean isNegative = false;
            boolean foundE = false;
            boolean foundExpSign = false;

            for (int i = 0; i < v.length(); i++) {
                char c = v.charAt(i);

                // Handle negative sign (only at the beginning)
                if (c == '-' && builder.length() == 0) {
                    isNegative = true;
                    builder.append(c);
                    continue;
                }

                // Handle decimal point (only one allowed)
                if (c == '.' && !foundDecimal) {
                    foundDecimal = true;
                    builder.append(c);
                    continue;
                }

                // Handle 'e' or 'E' for scientific notation (only one allowed)
                if ((c == 'e' || c == 'E') && foundDigit && !foundE) {
                    foundE = true;
                    builder.append(c);
                    continue;
                }

                // Handle + or - after 'e' or 'E'
                if ((c == '+' || c == '-') && foundE && !foundExpSign) {
                    foundExpSign = true;
                    builder.append(c);
                    continue;
                }

                // Handle digits
                if (Character.isDigit(c)) {
                    foundDigit = true;
                    builder.append(c);
                    continue;
                }

                // Skip other characters
            }

            if (foundDigit) {
                try {
                    float result = Float.parseFloat(builder.toString());
                    if (Float.isInfinite(result)) {
                        return isNegative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
                    }
                    return result;
                } catch (NumberFormatException e) {
                    // If parsing fails, try the original string
                    return Float.parseFloat(v);
                }
            }

            // Direct parsing of the original string
            return Float.parseFloat(v);
        } catch (Exception ignored) {
            return returnNullIfFailureElseZero ? null : 0.0f;
        }
    }

    /**
     * Attempts to parse a string to a Double, returning 0.0 if parsing fails.
     */
    public static Double tryParseDouble(String v) {
        return tryParseDouble(v, false);
    }

    /**
     * Attempts to parse a string to a Double, with option to return null on failure.
     *
     * @param v The string to parse
     * @param returnNullIfFailureElseZero Whether to return null (true) or 0.0 (false) on failure
     * @return The parsed Double value, or null/0.0 on failure
     */
    public static Double tryParseDouble(String v, boolean returnNullIfFailureElseZero) {
        if (v == null || v.isEmpty()) {
            return returnNullIfFailureElseZero ? null : 0.0;
        }

        try {
            // Try to find a valid floating-point number pattern in the string
            StringBuilder builder = new StringBuilder();
            boolean foundDecimal = false;
            boolean foundDigit = false;
            boolean isNegative = false;
            boolean foundE = false;
            boolean foundExpSign = false;

            for (int i = 0; i < v.length(); i++) {
                char c = v.charAt(i);

                // Handle negative sign (only at the beginning)
                if (c == '-' && builder.length() == 0) {
                    isNegative = true;
                    builder.append(c);
                    continue;
                }

                // Handle decimal point (only one allowed)
                if (c == '.' && !foundDecimal) {
                    foundDecimal = true;
                    builder.append(c);
                    continue;
                }

                // Handle 'e' or 'E' for scientific notation (only one allowed)
                if ((c == 'e' || c == 'E') && foundDigit && !foundE) {
                    foundE = true;
                    builder.append(c);
                    continue;
                }

                // Handle + or - after 'e' or 'E'
                if ((c == '+' || c == '-') && foundE && !foundExpSign) {
                    foundExpSign = true;
                    builder.append(c);
                    continue;
                }

                // Handle digits
                if (Character.isDigit(c)) {
                    foundDigit = true;
                    builder.append(c);
                    continue;
                }

                // Skip other characters
            }

            if (foundDigit) {
                try {
                    double result = Double.parseDouble(builder.toString());
                    if (Double.isInfinite(result)) {
                        return isNegative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                    }
                    return result;
                } catch (NumberFormatException e) {
                    // If parsing fails, try the original string
                    return Double.parseDouble(v);
                }
            }

            // Direct parsing of the original string
            return Double.parseDouble(v);
        } catch (Exception ignored) {
            return returnNullIfFailureElseZero ? null : 0.0;
        }
    }

    // ============================
    // Boolean Conversion Method
    // ============================

    /**
     * Attempts to parse a string to a Boolean.
     * Returns true for strings like "true", "yes", "1", "on", etc.
     * Returns false for everything else.
     */
    public static Boolean tryParseBoolean(String v) {
        return tryParseBoolean(v, false);
    }

    /**
     * Attempts to parse a string to a Boolean, with option to return null on failure.
     *
     * @param v The string to parse
     * @param returnNullIfNotExplicit Whether to return null (true) or false (false) if not explicitly true/false
     * @return The parsed Boolean value, or null/false on failure
     */
    public static Boolean tryParseBoolean(String v, boolean returnNullIfNotExplicit) {
        String trimmed = Str.toLowerCase(Str.trimOriginal(v));
        if(!Str.isEmpty(trimmed)) {
            if (trimmed.equals("true") || trimmed.equals("yes") || trimmed.equals("1") ||
                    trimmed.equals("on") || trimmed.equals("y") || trimmed.equals("t") ||
                    trimmed.equals("enable") || trimmed.equals("enabled") || trimmed.equals("succeed") || trimmed.equals("succeeded") ||
                    trimmed.equals("check") || trimmed.equals("checked") || trimmed.equals("positive") || trimmed.equals("up") ||
                    trimmed.equals("plus") || trimmed.equals("+") || trimmed.equals("good")) {
                return true;
            }

            // False values
            if (trimmed.equals("false") || trimmed.equals("no") || trimmed.equals("0") ||
                    trimmed.equals("off") || trimmed.equals("n") || trimmed.equals("f") || trimmed.equals("disable") ||
                    trimmed.equals("disabled") || trimmed.equals("fail") || trimmed.equals("failed") || trimmed.equals("failure") ||
                    trimmed.equals("uncheck") || trimmed.equals("unchecked") || trimmed.equals("negative") || trimmed.equals("down") ||
                    trimmed.equals("error") || trimmed.equals("exception") || trimmed.equals("minus") || trimmed.equals("-") || trimmed.equals("bad") ||
                    trimmed.equals("not")) {
                return false;
            }
        }

        // Not explicit
        return returnNullIfNotExplicit ? null : false;
    }
}