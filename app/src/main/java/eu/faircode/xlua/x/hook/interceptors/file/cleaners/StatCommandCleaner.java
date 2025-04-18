package eu.faircode.xlua.x.hook.interceptors.file.cleaners;

import android.text.TextUtils;
import android.util.Log;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.hook.interceptors.file.FileInterceptor;
import eu.faircode.xlua.x.hook.interceptors.file.FileTimeInterceptor;
import eu.faircode.xlua.x.hook.interceptors.file.stat.StatMockSettings;
import eu.faircode.xlua.x.xlua.LibUtil;
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

public class StatCommandCleaner {
    private static final String TAG = LibUtil.generateTag(StatCommandCleaner.class);

    public static class FieldValuePointer {
        public boolean lastWasTime = false;
        public boolean lastWasDevice = false;
        public boolean lastWasInode = false;

        private boolean mExpectingValue = false;
        public String field = null;

        public FileTimeInterceptor interceptor;
        private StringBuilder mValueBuilder = new StringBuilder();

        public FieldValuePointer(FileTimeInterceptor interceptor) { this.interceptor = interceptor; }

        public boolean valueIsEmpty() { return mValueBuilder == null || mValueBuilder.length() == 0; }
        public boolean expectingValue() {  return mExpectingValue; }
        public String getValue(boolean resetValue, boolean resetAll) {
            String val = valueIsEmpty() ? "" : mValueBuilder.toString();
            if(resetValue) mValueBuilder = new StringBuilder();
            if(resetAll) reset();
            return val;
        }

        public boolean partOfValue(char c) {
            boolean appended = false;
            if(lastWasTime) {
                //Accepted Time Stamp Value Chars
                appended = !(c == '-' && mValueBuilder.length() == 0) && (c == ' ' || c == '-' || c == ':' || c == '+' || c == '.' || Character.isDigit(c));
            }
            if(lastWasDevice) {
                //Accepted Device Value Chars
                appended = c == '/' || Character.isDigit(c) || Character.isAlphabetic(c);
            }
            //Accepted Generic Value Chars
            if(!appended) appended = mExpectingValue && Character.isDigit(c);
            if(appended) mValueBuilder.append(c);
            return appended;
        }

        public String getDataField(String originalValue) {
            if(field != null) {
                String t = field.trim().toLowerCase();
                //Access:
                if(t.contains(":")) t = t.replaceAll(":", "");
                long inputMilliseconds = StatUtils.toEpochMillis(originalValue);
                long org = 0;
                long offset = 0;
                switch (t) {
                    case "modify":
                        org = interceptor.getOriginalModified(inputMilliseconds);
                        offset = interceptor.getModifiedOffset();
                        break;
                    case "change":
                        org = interceptor.getOriginalChange(inputMilliseconds);
                        offset = interceptor.getChangeOffset();
                        break;
                    case "access":
                        org = interceptor.getOriginalAccess(inputMilliseconds);
                        offset = interceptor.getAccessOffset();
                        break;
                    case "birth":
                    case "create":
                        org = interceptor.getOriginalCreated(inputMilliseconds);
                        offset = interceptor.getCreatedOffset();
                        break;
                    default:
                        if(DebugUtil.isDebug()) Log.d(TAG, "Failed to find Field: " + t + " Returning:" + originalValue);
                        return originalValue;
                }

                String fakeTime = StatUtils.fromEpochMillis(org + offset, StatUtils.detectFormat(originalValue));
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Created Fake Time for Field [%s] Original Value=[%s] Converted MS=%s Original MS=%s Offset=%s Fake Time Value=[%s] File=%s",
                            t,
                            originalValue,
                            inputMilliseconds,
                            org,
                            offset,
                            fakeTime,
                            interceptor.file));

                return fakeTime;
            }

            return originalValue;
        }

        public void ensureField(String fieldName) {
            if(fieldName != null && fieldName.length() > 3) {
                String fld = fieldName.trim().toLowerCase();
                //fld = fld.endsWith(":") && fld.length() > 3 ? fld.substring(0, fld.length() - 1) : fld;
                switch(fld) {
                    case "inode:":
                        lastWasInode = true;
                        mExpectingValue = true;
                        field = "Inode";
                        break;
                    case "device:":
                        lastWasDevice = true;
                        mExpectingValue = true;
                        field = "Device";
                        break;
                    case "access:":
                    case "modify:":
                    case "change:":
                    case "birth:":
                    case "create:":
                        lastWasTime = true;
                        mExpectingValue = true;
                        field = fld.substring(0, 1).toUpperCase() + fld.substring(1).replaceAll(":", "");
                        break;
                    default:
                        reset();
                        break;
                }
            }
        }

        public void reset() {
            lastWasTime = false;
            lastWasDevice = false;
            mExpectingValue = false;
            lastWasInode = false;
            field = null;
            mValueBuilder = new StringBuilder();
        }
    }


    public static String parseFake(String output, FileTimeInterceptor interceptor) {
        if(DebugUtil.isDebug())
            Log.d(TAG, "Cleaning Stat Command output for File: " + interceptor.file + " Output=" + Str.ensureNoDoubleNewLines(output));

        StringBuilder currentChunk = new StringBuilder();
        StringBuilder full = new StringBuilder();

        int lastIndex = output.length() - 1;
        FieldValuePointer ptr = new FieldValuePointer(interceptor);
        char[] chars = output.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if(ptr.expectingValue()) {
                boolean added = ptr.partOfValue(c);
                if(!added || i == lastIndex) {
                    if(!ptr.valueIsEmpty()) {
                        //System.out.println("[1] Field: [" + ptr.field + "] Value: [" + ptr.getValue(false, false) + "]");
                        full.append(cleanValue(ptr, ptr.getValue(true, false)));
                    }

                    if(!added)  full.append(c);
                    ptr.reset();
                }

                continue;
            }

            if(c == '\n') {
                if(ptr.expectingValue() || !ptr.valueIsEmpty()) {
                    //System.out.println("[2] Field: [" + ptr.field + "] Value: [" + value + "]");
                    full.append(cleanValue(ptr, ptr.getValue(true, false)));
                    ptr.reset();
                }

                if(currentChunk.length() > 0) {
                    full.append(currentChunk);
                    currentChunk = new StringBuilder();
                }
            }

            if(c == ' ') {
                if(currentChunk.length() > 0) {
                    String cChunk = currentChunk.toString();
                    ptr.ensureField(cChunk);
                    currentChunk = new StringBuilder();
                    full.append(cChunk);
                } else if(!ptr.valueIsEmpty()) {
                    //End of Value like Inode
                    //System.out.println("[3] Field: [" + ptr.field + "] Value: [" + value + "]");
                    full.append(cleanValue(ptr, ptr.getValue(true, false)));
                    ptr.reset();
                }
            }

            if(Character.isAlphabetic(c) || currentChunk.length() > 0) {
                currentChunk.append(c);
                continue;
            }

            full.append(c);
        }

        String res = full.toString();
        if(DebugUtil.isDebug())
            Log.d(TAG, "File:" + interceptor.file + " Stat Command Output is now: " + Str.ensureNoDoubleNewLines(res));

        return res;
    }

    public static String cleanValue(FieldValuePointer ptr, String value) {
        if(TextUtils.isEmpty(value) || value.equals("-") || value.equals("--"))
            return value;

        return ptr.getDataField(value);
    }
}

