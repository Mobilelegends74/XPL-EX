package eu.faircode.xlua.x.hook.interceptors.ipc.bases;

import android.os.Parcel;
import android.util.Log;

import android.os.Parcel;
import android.util.Log;

/**
 * Utility class for dumping the contents of Android Parcel objects.
 * Provides a hex-style dump similar to Frida's hexdump functionality.
 */
public class ParcelDumpUtil {

    private static final String TAG = "ParcelDumpUtil";
    private static final int BYTES_PER_LINE = 16;

    /**
     * Dumps all data from a Parcel as a hex dump string.
     *
     * @param parcel The parcel to dump
     * @return String representation of the parcel contents in hex format
     */
    public static String dumpParcel(Parcel parcel) {
        if (parcel == null) {
            return "Parcel is null";
        }

        StringBuilder result = new StringBuilder();

        // Save the original position
        int originalPosition = parcel.dataPosition();

        try {
            // Create a copy of the parcel to avoid modifying the original
            Parcel copy = Parcel.obtain();
            copy.appendFrom(parcel, 0, parcel.dataSize());
            copy.setDataPosition(0);

            // Dump basic information
            result.append("Parcel@").append(Integer.toHexString(System.identityHashCode(parcel)))
                    .append(" size=").append(copy.dataSize())
                    .append(" position=").append(originalPosition)
                    .append("\n");

            // Get the raw data from the parcel
            byte[] rawData = getRawDataFromParcel(copy);
            if (rawData != null) {
                result.append(createHexDump(rawData));
            } else {
                result.append("Unable to access raw data from parcel");
            }

            // Clean up
            copy.recycle();

        } catch (Exception e) {
            result.append("Error during parcel dump: ").append(e.getMessage());
            Log.e(TAG, "Error dumping parcel", e);
        } finally {
            // Restore the original position
            parcel.setDataPosition(originalPosition);
        }

        // Log the result and return it
        String resultString = result.toString();
        Log.d(TAG, resultString);
        return resultString;
    }

    /**
     * Attempts to extract raw bytes from a Parcel.
     * Note: Uses reflection to access internal data, may not work on all Android versions.
     */
    private static byte[] getRawDataFromParcel(Parcel parcel) {
        try {
            // Marshall the parcel to a byte array
            byte[] data = parcel.marshall();
            return data;
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract raw data from parcel", e);
            return null;
        }
    }

    /**
     * Creates a hex dump string from a byte array, similar to Frida's hexdump output.
     */
    private static String createHexDump(byte[] data) {
        StringBuilder result = new StringBuilder();
        StringBuilder asciiResult = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            // Print offset at the beginning of each line
            if (i % BYTES_PER_LINE == 0) {
                if (i > 0) {
                    // Add ASCII representation at the end of the previous line
                    result.append("  ").append(asciiResult.toString()).append("\n");
                    asciiResult.setLength(0); // Clear ASCII buffer
                }
                result.append(String.format("%08x  ", i));
            }

            // Add the hex value
            result.append(String.format("%02x ", data[i] & 0xFF));

            // Add to ASCII representation if printable, otherwise add a dot
            if (data[i] >= 32 && data[i] <= 126) {
                asciiResult.append((char) data[i]);
            } else {
                asciiResult.append('.');
            }

            // Add an extra space in the middle of each line
            if (i % BYTES_PER_LINE == 7) {
                result.append(" ");
            }
        }

        // Pad the last line to align the ASCII representation
        int remaining = BYTES_PER_LINE - (data.length % BYTES_PER_LINE);
        if (data.length % BYTES_PER_LINE != 0) {
            for (int i = 0; i < remaining; i++) {
                result.append("   ");
            }
            // Add an extra space if we would've crossed the middle point
            if (data.length % BYTES_PER_LINE <= 7) {
                result.append(" ");
            }
        }

        // Add the ASCII representation for the last line
        result.append("  ").append(asciiResult.toString());

        return result.toString();
    }

    /**
     * Alternative dump method that attempts to interpret the parcel data with more semantic meaning.
     * Can be useful in addition to the hex dump.
     */
    public static String interpretParcelContents(Parcel parcel) {
        if (parcel == null) {
            return "Parcel is null";
        }

        StringBuilder result = new StringBuilder();

        // Save the original position
        int originalPosition = parcel.dataPosition();

        try {
            // Create a copy of the parcel to avoid modifying the original
            Parcel copy = Parcel.obtain();
            copy.appendFrom(parcel, 0, parcel.dataSize());
            copy.setDataPosition(0);

            result.append("Trying to interpret parcel data:\n");

            int index = 0;
            while (copy.dataPosition() < copy.dataSize()) {
                int startPos = copy.dataPosition();
                try {
                    result.append(String.format("[%d] Offset: 0x%08x - ", index, startPos));

                    // Try to read as string first (since it's often the most useful)
                    copy.setDataPosition(startPos);
                    String strValue = copy.readString();
                    if (strValue != null) {
                        result.append(String.format("String: \"%s\"\n", strValue));
                        index++;
                        continue;
                    }

                    // Read as int
                    copy.setDataPosition(startPos);
                    int intValue = copy.readInt();
                    result.append(String.format("Int: %d (0x%08x)\n", intValue, intValue));

                    copy.setDataPosition(startPos + 4); // Move to next int-sized chunk
                } catch (Exception e) {
                    result.append("Error reading: ").append(e.getMessage()).append("\n");
                    // Try to advance by 4 bytes (typical alignment)
                    try {
                        copy.setDataPosition(startPos + 4);
                    } catch (Exception e2) {
                        // If we can't advance anymore, break out
                        break;
                    }
                }
                index++;

                // Prevent potentially infinite loops
                if (index > 1000) {
                    result.append("... too many items, truncating output ...\n");
                    break;
                }
            }

            // Clean up
            copy.recycle();

        } catch (Exception e) {
            result.append("Error during parcel interpretation: ").append(e.getMessage());
            Log.e(TAG, "Error interpreting parcel", e);
        } finally {
            // Restore the original position
            parcel.setDataPosition(originalPosition);
        }

        return result.toString();
    }
}
