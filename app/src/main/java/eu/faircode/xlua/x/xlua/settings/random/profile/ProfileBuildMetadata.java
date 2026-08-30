package eu.faircode.xlua.x.xlua.settings.random.profile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Values that must stay coherent with the selected stock fingerprint. */
public final class ProfileBuildMetadata {
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final Pattern DATE_EIGHT = Pattern.compile("(?:^|\\D)(20\\d{6})(?:\\D|$)");
    private static final Pattern DATE_SIX = Pattern.compile("(?:^|\\D)(2\\d{5})(?:\\D|$)");

    final long epochSeconds;
    final String buildDate;
    final String compactDate;
    final String dottedDate;
    final String securityPatch;
    final String kernelSystemName;
    final String kernelNodeName;
    final String kernelRelease;
    final String kernelVersion;

    private ProfileBuildMetadata(DeviceProfile device, DeviceBuildProfile build) {
        Calendar date = dateFromValues(build.fingerprint, build.buildId, build.incremental, build.release);
        this.epochSeconds = date.getTimeInMillis() / 1000L;
        this.buildDate = format(date, "EEE MMM d HH:mm:ss 'UTC' yyyy");
        this.compactDate = format(date, "yyyyMMdd");
        this.dottedDate = format(date, "yyyy.MM.dd");
        this.securityPatch = format(date, "yyyy-MM-01");
        this.kernelSystemName = "Linux";
        this.kernelNodeName = "localhost";
        this.kernelRelease = kernelRelease(device, build);
        this.kernelVersion = "#1 SMP PREEMPT_DYNAMIC "
                + format(date, "EEE MMM d HH:mm:ss 'UTC' yyyy");
    }

    static ProfileBuildMetadata from(DeviceProfile device, DeviceBuildProfile build) {
        return new ProfileBuildMetadata(device, build);
    }

    public static long deriveEpochSeconds(String fingerprint, String buildId,
                                          String incremental, String release) {
        if (fingerprint == null || fingerprint.trim().isEmpty())
            return 0L;
        return dateFromValues(fingerprint, buildId, incremental, release).getTimeInMillis() / 1000L;
    }

    private static Calendar dateFromValues(String fingerprint, String buildId,
                                           String incremental, String release) {
        Calendar parsed = parseDate(incremental, DATE_EIGHT, true);
        if (parsed == null) parsed = parseDate(buildId, DATE_EIGHT, true);
        if (parsed == null) parsed = parseDate(incremental, DATE_SIX, false);
        if (parsed == null) parsed = parseDate(buildId, DATE_SIX, false);
        if (parsed == null) parsed = fallbackDate(release, fingerprint.hashCode());

        int seconds = Math.floorMod(fingerprint.hashCode(), 12 * 60 * 60);
        parsed.set(Calendar.HOUR_OF_DAY, 8 + seconds / 3600);
        parsed.set(Calendar.MINUTE, seconds / 60 % 60);
        parsed.set(Calendar.SECOND, seconds % 60);
        parsed.set(Calendar.MILLISECOND, 0);

        Calendar latest = Calendar.getInstance(UTC, Locale.US);
        latest.add(Calendar.DAY_OF_MONTH, -7);
        if (parsed.after(latest)) parsed = latest;
        return parsed;
    }

    private static Calendar parseDate(String value, Pattern pattern, boolean fourDigitYear) {
        if (value == null) return null;
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) {
            String digits = matcher.group(1);
            int year = fourDigitYear
                    ? Integer.parseInt(digits.substring(0, 4))
                    : 2000 + Integer.parseInt(digits.substring(0, 2));
            int offset = fourDigitYear ? 4 : 2;
            int month = Integer.parseInt(digits.substring(offset, offset + 2));
            int day = Integer.parseInt(digits.substring(offset + 2, offset + 4));
            Calendar calendar = newCalendar(year, month, day);
            if (calendar != null) return calendar;
        }
        return null;
    }

    private static Calendar fallbackDate(String release, int seed) {
        int android = 13;
        try { android = Integer.parseInt(release); } catch (NumberFormatException ignored) { }
        int year = 2009 + android;
        int month = 3 + Math.floorMod(seed, 8);
        int day = 1 + Math.floorMod(seed / 31, 27);
        Calendar result = newCalendar(year, month, day);
        return result == null ? Calendar.getInstance(UTC, Locale.US) : result;
    }

    private static Calendar newCalendar(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance(UTC, Locale.US);
        calendar.clear();
        calendar.setLenient(false);
        calendar.set(year, month - 1, day, 12, 0, 0);
        try {
            calendar.getTimeInMillis();
            return calendar;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String kernelRelease(DeviceProfile device, DeviceBuildProfile build) {
        String base;
        if (device.launchApiLevel <= 29) base = "4.14.190";
        else if (device.launchApiLevel == 30) base = "4.19.157";
        else if (device.launchApiLevel <= 32) base = "5.10.198";
        else if (device.launchApiLevel == 33) base = "5.15.148";
        else if (device.launchApiLevel == 34) base = "6.1.75";
        else if (device.launchApiLevel == 35) base = "6.6.56";
        else base = "6.12.23";
        return base + "-android" + device.launchRelease + "-"
                + String.format(Locale.US, "%05d", Math.floorMod(build.fingerprint.hashCode(), 100000));
    }

    private static String format(Calendar date, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.US);
        formatter.setTimeZone(UTC);
        return formatter.format(date.getTime());
    }
}
