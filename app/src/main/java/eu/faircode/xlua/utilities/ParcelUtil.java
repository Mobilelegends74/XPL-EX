package eu.faircode.xlua.utilities;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.List;

public class ParcelUtil {
    private static final String TAG = "XLua.ParcelUtil";
    public static final String IGNORE_VALUE = "<<!>>";


    /**
     * Converts a Parcel to a hex string representation
     * @param parcel The parcel to convert
     * @return Hex string representation of the parcel's bytes
     */
    public static String parcelToHexStringEx(Parcel parcel) {
        if (parcel == null) return "";

        try {
            // Get the current position to restore later
            int originalPosition = parcel.dataPosition();

            // Reset to beginning
            parcel.setDataPosition(0);

            // Get the bytes from the parcel
            byte[] bytes = new byte[parcel.dataSize()];
            parcel.unmarshall(bytes, 0, parcel.dataSize());

            // Restore original position
            parcel.setDataPosition(originalPosition);

            // Convert to hex string
            return bytesToHex(bytes);
        }catch (Exception e) {
            Log.e(TAG, "Error=" + e);
            return "";
        }
    }

    /**
     * Creates a hex dump of a byte array with offset, hex values, and ASCII representation
     * @param bytes The byte array to dump
     * @return Formatted hex dump string
     */
    public static String createHexDump(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";

        StringBuilder result = new StringBuilder();
        int bytesPerLine = 16;

        for (int i = 0; i < bytes.length; i += bytesPerLine) {
            // Print offset
            result.append(String.format("%08X  ", i));

            // Print hex values
            StringBuilder hexPart = new StringBuilder();
            StringBuilder asciiPart = new StringBuilder();

            for (int j = 0; j < bytesPerLine; j++) {
                if (i + j < bytes.length) {
                    byte b = bytes[i + j];
                    hexPart.append(String.format("%02X ", b));
                    // Add ASCII representation (printable characters only)
                    if (b >= 32 && b < 127) {
                        asciiPart.append((char)b);
                    } else {
                        asciiPart.append('.');
                    }
                } else {
                    hexPart.append("   ");
                    asciiPart.append(" ");
                }

                // Add extra space between 8-byte groups
                if (j == 7) {
                    hexPart.append(" ");
                }
            }

            result.append(hexPart).append(" |").append(asciiPart).append("|\n");
        }

        return result.toString();
    }

    /**
     * Converts a byte array to a hex string
     * @param bytes The byte array to convert
     * @return Hex string representation
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }



    public static String parcelToHexString(Parcel parcel) {
        Log.w(TAG, "AD ID (phs) pos=" + parcel.dataPosition() + " size=" + parcel.dataSize());
        byte[] parcelBytes = parcel.marshall();
        parcel.setDataPosition(0);  // Reset parcel for future use

        StringBuilder hexString = new StringBuilder();
        for (byte b : parcelBytes) {
            hexString.append(String.format("%02X ", b));
        }

        return hexString.toString();
    }

    //public static void writeInt(Parcel in, int value, String defaultIgnore) {}


    public static void writeString(Parcel in, String value, String ignoreValue) {
        writeString(in, value, ignoreValue, true);
    }

    public static void writeString(Parcel in, String value, String ignoreValue, boolean checkEmpty) {
        if(in == null)
            return;

        String vw = value;
        if((!checkEmpty && value == null) || (checkEmpty && !StringUtil.isValidString(value)))
            vw = ignoreValue;

        in.writeString(vw);
    }

    public static String readString(Parcel in, String defaultValue, String ignoreValue) {
        String val = null;
        try {
            val = in.readString();
        }catch (Exception e) {
            Log.e(TAG, "Failed to read Parcel String: [" + e + "]");
        }

        if(val == null)
            return defaultValue;

        if(val.equals(ignoreValue))
            return defaultValue;

        return val;
    }

    public static void writeBool(Parcel in, Boolean bResult) {
        if(bResult == null) {
            //?
        }else {
            in.writeByte(bResult ? (byte)1 : (byte)0);
        }
    }

    public static Boolean readBool(Parcel in) {
        return in.readByte() == 1;
    }
    /*public final <T extends Parcelable> List<T> readParcelableList(List<T> list, Parcel p) {
        final int N = p.readInt();// readInt();
        if (N == -1) {
            list.clear();
            return list;
        }

        final int M = list.size();
        int i = 0;
        for (; i < M && i < N; i++) {
            //list.set(i, (T) readParcelable(cl));
            list.size(i, (T)p.readParcelable())
        }
        for (; i<N; i++) {
            list.add((T) readParcelable(cl));
        }
        for (; i<M; i++) {
            list.remove(N);
        }
        return list;
    }*/
}
