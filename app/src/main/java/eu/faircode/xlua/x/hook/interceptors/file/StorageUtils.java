package eu.faircode.xlua.x.hook.interceptors.file;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.runtime.reflect.DynamicMethod;
import eu.faircode.xlua.x.xlua.LibUtil;

public class StorageUtils {
    private static final String TAG = LibUtil.generateTag(StorageUtils.class);


    /**
     * Converts bytes to megabytes
     *
     * @param bytes The number of bytes to convert
     * @return The equivalent value in megabytes (MB)
     */
    public static long bytesToMB(long bytes) {
        return bytes / (1024 * 1024);
    }

    /**
     * Converts megabytes to bytes
     *
     * @param mb The number of megabytes to convert
     * @return The equivalent value in bytes
     */
    public static long mbToBytes(long mb) {
        return mb * 1024 * 1024;
    }

    /**
     * Generates a random amount of megabytes between 1 and 1000
     *
     * @return Random MB value between 1 and 1000
     */
    public static long randomMB() {
        return RandomGenerator.nextInt(1000) + 1; // 1 to 1000 MB
    }

    /**
     * Generates a random amount of megabytes within the specified range
     *
     * @param min Minimum MB value (inclusive)
     * @param max Maximum MB value (inclusive)
     * @return Random MB value between min and max
     */
    public static long randomMB(long min, long max) {
        if (min >= max) {
            throw new IllegalArgumentException("Min must be less than max");
        }
        return min + (long)(RandomGenerator.nextDouble() * (max - min + 1));
    }

    /**
     * Generates a random amount of megabytes within a range around the original value
     *
     * @param originalMB The original size in megabytes
     * @param percentVariation The percentage variation allowed (e.g., 10 for ±10%)
     * @return Random MB value within the specified percentage of the original
     */
    public static long randomMB(long originalMB, int percentVariation) {
        if (originalMB <= 0) {
            return randomMB();
        }

        long variation = (originalMB * percentVariation) / 100;
        long min = Math.max(1, originalMB - variation);
        long max = originalMB + variation;

        return min + (long)(RandomGenerator.nextDouble() * (max - min + 1));
    }

    /**
     * Generates a random amount of megabytes to subtract from the original value
     *
     * @param originalMB The original size in megabytes
     * @param minSubtractMB Minimum MB to subtract
     * @param maxSubtractMB Maximum MB to subtract
     * @return Original MB minus a random value between minSubtract and maxSubtract
     */
    public static long randomSubtractMB(long originalMB, long minSubtractMB, long maxSubtractMB) {
        if (originalMB <= 0) {
            return 0;
        }

        long subtractAmount = randomMB(minSubtractMB, maxSubtractMB);
        return Math.max(1, originalMB - subtractAmount);
    }

    /**
     * Generates a random amount of megabytes to add to the original value
     *
     * @param originalMB The original size in megabytes
     * @param minAddMB Minimum MB to add
     * @param maxAddMB Maximum MB to add
     * @return Original MB plus a random value between minAdd and maxAdd
     */
    public static long randomAddMB(long originalMB, long minAddMB, long maxAddMB) {
        if (originalMB < 0) {
            return randomMB(minAddMB, maxAddMB);
        }

        long addAmount = randomMB(minAddMB, maxAddMB);
        return originalMB + addAmount;
    }


    /*
        Returns the mount path for the volume.
        @UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.Q, publicAlternatives = "{@link StorageVolume#getDirectory()}")
        @TestApi
        public String getPath() {
            return mPath.toString();
        }
        Returns the path of the underlying filesystem.
        public String getInternalPath() {
            return mInternalPath.toString();
        }

        @UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.Q, publicAlternatives = "{@link StorageVolume#getDirectory()}")
        public File getPathFile() {
            return mPath;
        }
    */


    public static final String STORAGE_VOLUME_CLASS = "android.os.storage.StorageVolume";

    public static final DynamicField FIELD_PATH = new DynamicField(STORAGE_VOLUME_CLASS, "mPath")
            .setAccessible(true);

    public static final DynamicMethod METHOD_GET_PATH_FILE = new DynamicMethod(STORAGE_VOLUME_CLASS, "getPathFile")
            .setAccessible(true);

    public static final DynamicMethod METHOD_GET_PATH = new DynamicMethod(STORAGE_VOLUME_CLASS, "getPath")
            .setAccessible(true);

    public static final DynamicMethod METHOD_GET_PATH_INTERNAL = new DynamicMethod(STORAGE_VOLUME_CLASS, "getInternalPath")
            .setAccessible(true);

    public static StorageVolume getStorageVolumeFromUuid(UUID uuid, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(uuid == null)
                return null;

            try {
                StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                List<StorageVolume> volumes = storageManager.getStorageVolumes();
                for (StorageVolume volume : volumes) {
                    String vUuid = volume.getUuid();
                    if (vUuid != null && vUuid.equals(uuid.toString()))
                        return volume;
                }
            }catch (Exception e) {
                Log.e(TAG, Str.fm("Error Getting Storage Volume UUID (%s) Object Error=%s",
                        uuid.toString(),
                        e));
            }
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, "Failed to Get Path for Storage UUID: " + uuid.toString());

        return null;
    }

    public static String getStorageVolumeUuidPath(UUID uuid, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(uuid == null)
                return null;

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (uuid.toString().equals(StorageManager.UUID_DEFAULT.toString())) {
                        return Environment.getDataDirectory().getPath();
                    }
                }

                File fileObject = getStorageVolumeUuidFile(uuid, context);
                if(fileObject != null)
                    return fileObject.getPath();

                StorageVolume sv = getStorageVolumeFromUuid(uuid, context);
                if(sv != null) {
                    String path = METHOD_GET_PATH.tryInstanceInvokeEx(sv);
                    return Str.isEmpty(path) ? METHOD_GET_PATH_INTERNAL.tryInstanceInvokeEx(sv) : path;
                }
            }catch (Exception e) {
                Log.e(TAG, Str.fm("Error Getting Storage Volume UUID (%s) Path Error=%s",
                        uuid.toString(),
                        e));
            }
        }

        return null;
    }

    public static File getStorageVolumeUuidFile(UUID uuid, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (uuid.toString().equals(StorageManager.UUID_DEFAULT.toString())) {
                        return Environment.getDataDirectory();
                    }
                }

                StorageVolume sv = getStorageVolumeFromUuid(uuid, context);
                if(sv != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        return sv.getDirectory();
                    } else {
                        File path = FIELD_PATH.tryGetValueInstanceEx(sv);
                        return path == null ? METHOD_GET_PATH_FILE.tryInstanceInvokeEx(sv) : path;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, Str.fm("Error Getting Storage Volume UUID (%s) Path File Error=%s",
                        uuid.toString(),
                        e));
            }
        }

        return null;
    }


    /*java.io.UnixFileSystem
     public long getSpace(File f, int t) {
        BlockGuard.getThreadPolicy().onReadFromDisk();
        BlockGuard.getVmPolicy().onPathAccess(f.getPath());
        return getSpace0(f, t); }
     private native long getSpace0(File f, int t);


     java.io.File:: public long getFreeSpace()
     java.io.FileSystem:: public abstract long getSpace(File f, int t);
     java.io.UnixFileSystem:: public long getSpace(File f, int t)
     java.io.UnixFileSystem:: private native long getSpace0(File f, int t);
    *
    */
}
