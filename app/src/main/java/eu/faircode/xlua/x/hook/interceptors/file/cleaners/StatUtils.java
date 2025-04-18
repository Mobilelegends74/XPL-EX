package eu.faircode.xlua.x.hook.interceptors.file.cleaners;
import android.util.Log;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.LibUtil;

public class StatUtils {
    private static final String TAG = LibUtil.generateTag(StatUtils.class);

    /**
     * Helper method to pad milliseconds to nanoseconds (9 digits)
     * @param timestamp A timestamp string with milliseconds (3 digits)
     * @return A timestamp string with milliseconds padded to 9 digits
     */
    private static String padMillisecondsToNanoseconds(String timestamp) {
        // Find the position where the milliseconds part ends
        int msEndPos = -1;

        if (timestamp.contains(" +") || timestamp.contains(" -")) {
            // Format with timezone
            msEndPos = timestamp.indexOf(" +");
            if (msEndPos == -1) {
                msEndPos = timestamp.indexOf(" -");
            }
        } else {
            // Format without timezone
            msEndPos = timestamp.length();
        }

        if (msEndPos > 0) {
            // Find the position of the dot that separates seconds and milliseconds
            int dotPos = timestamp.lastIndexOf('.', msEndPos);

            if (dotPos > 0 && msEndPos - dotPos == 4) { // .SSS = 4 chars including the dot
                // Get the parts
                String beforeMs = timestamp.substring(0, dotPos + 4); // Include the dot and 3 ms digits
                String padding = "000000"; // Pad with 6 zeros to get 9 digits total
                String afterMs = timestamp.substring(msEndPos);

                // Combine the parts
                return beforeMs + padding + afterMs;
            }
        }

        // Return the original if we couldn't parse/pad it
        return timestamp;
    }

    public static final String REGEX_FILE = "\\s*File:\\s*";


    public static String extractFilePathEx(String statOutput) {
        if(statOutput == null || statOutput.isEmpty()) {
            return "";
        }

        if(statOutput.contains("File:")) {
            String[] lines = statOutput.split("\\n");
            for(String line : lines) {
                if(line.contains("File:")) {
                    return line.replaceFirst(REGEX_FILE, "").trim();
                }
            }
        }

        return "";
    }

    /**
     * Extracts a file path from a stat command output
     *
     * @param statOutput The complete output string from the stat command
     * @return The extracted file path, or an empty string if not found
     */
    public static String extractFilePath(String statOutput) {
        try {
            if (statOutput == null || statOutput.isEmpty()) {
                return "";
            }

            String posRes = extractFilePathEx(statOutput);
            if(!Str.isEmpty(posRes))
                return posRes;

            // Find the "File:" indicator
            String fileIndicator = "File:";
            int fileIndex = statOutput.indexOf(fileIndicator);

            if (fileIndex == -1) {
                return ""; // "File:" not found in output
            }

            // Move to the character after "File:"
            int startIndex = fileIndex + fileIndicator.length();

            // Skip any whitespace
            while (startIndex < statOutput.length() &&
                    Character.isWhitespace(statOutput.charAt(startIndex))) {
                startIndex++;
            }

            // Find the first "/" character
            while (startIndex < statOutput.length() &&
                    statOutput.charAt(startIndex) != '/') {
                startIndex++;
            }

            if (startIndex >= statOutput.length()) {
                return ""; // No "/" found
            }

            // Extract the path until we hit a control character
            StringBuilder pathBuilder = new StringBuilder();
            int currentIndex = startIndex;

            while (currentIndex < statOutput.length()) {
                char c = statOutput.charAt(currentIndex);

                // Check for control characters that would indicate end of path
                if (c == '\n' || c == '\r' || c == '\t' || c == '\b' || c == '\0' || c == '\'' || c == '\"') {
                    break;
                }

                pathBuilder.append(c);
                currentIndex++;
            }

            return pathBuilder.toString().trim();
        }catch (Exception e) {
            Log.e(TAG, "Error Extracting Path, Error=" + e + " Output=" + statOutput);
            return Str.EMPTY;
        }
    }


    /**
     * Converts various stat command timestamp formats to epoch milliseconds
     *
     * @param statTimestamp The timestamp string from stat command output
     * @return Epoch time in milliseconds, or -1 if parsing fails
     */
    public static long toEpochMillis(String statTimestamp) {
        if (statTimestamp == null || statTimestamp.trim().isEmpty()) {
            return -1;
        }

        // Get the format
        int format = detectFormat(statTimestamp);
        if (format == FORMAT_UNKNOWN) {
            return -1;
        }

        // Try different date formats
        SimpleDateFormat sdf = null;
        Date date = null;

        try {
            switch (format) {
                case FORMAT_ISO_WITH_TIMEZONE:
                    // Trim nanoseconds to milliseconds (3 digits)
                    String adjusted1 = statTimestamp.replaceAll("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\d+ ([+-]\\d{4})", "$1 $2");
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US);
                    date = sdf.parse(adjusted1);
                    return date.getTime();

                case FORMAT_ISO_WITH_MILLIS:
                    // Trim nanoseconds to milliseconds (3 digits)
                    String adjusted2 = statTimestamp.replaceAll("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\d+", "$1");
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
                    date = sdf.parse(adjusted2);
                    return date.getTime();

                case FORMAT_ISO_SECONDS:
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    date = sdf.parse(statTimestamp);
                    return date.getTime();

                case FORMAT_MONTH_DAY_TIME_YEAR:
                    sdf = new SimpleDateFormat("MMM d HH:mm:ss yyyy", Locale.US);
                    date = sdf.parse(statTimestamp);
                    return date.getTime();

                case FORMAT_WEEKDAY_MONTH_DAY_TIME_YEAR:
                    sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US);
                    date = sdf.parse(statTimestamp);
                    return date.getTime();

                case FORMAT_DATE_ONLY:
                    sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    date = sdf.parse(statTimestamp);
                    return date.getTime();

                default:
                    return -1;
            }

        } catch (ParseException e) {
            // Log error or handle differently if needed
            Log.e(TAG, "Failed to parse timestamp: " + statTimestamp);
        }

        return -1; // Return -1 to indicate parsing failure
    }

    /**
     * Converts epoch milliseconds back to a stat timestamp string
     *
     * @param epochMillis Epoch time in milliseconds
     * @param format The desired output format (use the FORMAT_* constants)
     * @return Formatted timestamp string
     */
    public static String fromEpochMillis(long epochMillis, int format) {
        if (epochMillis < 0) {
            return "";
        }

        Date date = new Date(epochMillis);
        SimpleDateFormat sdf;

        switch (format) {
            case FORMAT_ISO_WITH_TIMEZONE:
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.US);
                break;
            case FORMAT_ISO_WITH_MILLIS:
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
                break;
            case FORMAT_ISO_SECONDS:
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                break;
            case FORMAT_MONTH_DAY_TIME_YEAR:
                sdf = new SimpleDateFormat("MMM d HH:mm:ss yyyy", Locale.US);
                break;
            case FORMAT_WEEKDAY_MONTH_DAY_TIME_YEAR:
                sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US);
                break;
            case FORMAT_DATE_ONLY:
                sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                break;
            default:
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        }

        return padMillisecondsToNanoseconds(sdf.format(date));
    }

    // Format constants for fromEpochMillis
    public static final int FORMAT_ISO_WITH_TIMEZONE = 0;
    public static final int FORMAT_ISO_WITH_MILLIS = 1;
    public static final int FORMAT_ISO_SECONDS = 2;
    public static final int FORMAT_MONTH_DAY_TIME_YEAR = 3;
    public static final int FORMAT_WEEKDAY_MONTH_DAY_TIME_YEAR = 4;
    public static final int FORMAT_DATE_ONLY = 5;
    public static final int FORMAT_UNKNOWN = -1;

    // Regex patterns for format detection
    private static final Pattern PATTERN_ISO_WITH_TIMEZONE = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d+ [+-]\\d{4}");
    private static final Pattern PATTERN_ISO_WITH_MILLIS = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d+");
    private static final Pattern PATTERN_ISO_SECONDS = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    private static final Pattern PATTERN_MONTH_DAY_TIME_YEAR = Pattern.compile("[A-Za-z]{3} \\d{1,2} \\d{2}:\\d{2}:\\d{2} \\d{4}");
    private static final Pattern PATTERN_WEEKDAY_MONTH_DAY_TIME_YEAR = Pattern.compile("[A-Za-z]{3} [A-Za-z]{3} \\d{1,2} \\d{2}:\\d{2}:\\d{2} \\d{4}");
    private static final Pattern PATTERN_DATE_ONLY = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    /**
     * Detects the format of a stat timestamp string
     *
     * @param statTimestamp The timestamp string from stat command output
     * @return The format code (one of the FORMAT_* constants) or FORMAT_UNKNOWN if not recognized
     */
    public static int detectFormat(String statTimestamp) {
        if (statTimestamp == null || statTimestamp.trim().isEmpty()) {
            return FORMAT_UNKNOWN;
        }

        if (PATTERN_ISO_WITH_TIMEZONE.matcher(statTimestamp).matches()) {
            return FORMAT_ISO_WITH_TIMEZONE;
        }
        if (PATTERN_ISO_WITH_MILLIS.matcher(statTimestamp).matches()) {
            return FORMAT_ISO_WITH_MILLIS;
        }
        if (PATTERN_ISO_SECONDS.matcher(statTimestamp).matches()) {
            return FORMAT_ISO_SECONDS;
        }
        if (PATTERN_MONTH_DAY_TIME_YEAR.matcher(statTimestamp).matches()) {
            return FORMAT_MONTH_DAY_TIME_YEAR;
        }
        if (PATTERN_WEEKDAY_MONTH_DAY_TIME_YEAR.matcher(statTimestamp).matches()) {
            return FORMAT_WEEKDAY_MONTH_DAY_TIME_YEAR;
        }
        if (PATTERN_DATE_ONLY.matcher(statTimestamp).matches()) {
            return FORMAT_DATE_ONLY;
        }

        return FORMAT_UNKNOWN;
    }

    /**
     * Gets a human-readable name for the timestamp format
     *
     * @param formatCode One of the FORMAT_* constants
     * @return A descriptive name of the format
     */
    public static String getFormatName(int formatCode) {
        switch (formatCode) {
            case FORMAT_ISO_WITH_TIMEZONE:
                return "ISO with timezone";
            case FORMAT_ISO_WITH_MILLIS:
                return "ISO with milliseconds";
            case FORMAT_ISO_SECONDS:
                return "ISO seconds";
            case FORMAT_MONTH_DAY_TIME_YEAR:
                return "Month-day-time-year";
            case FORMAT_WEEKDAY_MONTH_DAY_TIME_YEAR:
                return "Weekday-month-day-time-year";
            case FORMAT_DATE_ONLY:
                return "Date only";
            default:
                return "Unknown format";
        }
    }
}
