package eu.faircode.xlua.x.hook.interceptors.file;

import android.system.Os;
import android.system.StructStatVfs;
import android.util.Log;

import java.io.File;
import java.util.UUID;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.runtime.reflect.ReflectUtil;
import eu.faircode.xlua.x.xlua.LibUtil;

public class StorageAvailInterceptor {
    private static final String TAG = LibUtil.generateTag(StorageAvailInterceptor.class);

    public static final String MAP_CATEGORY = "storage";
    public static final String MAP_GROUP_SUB_OFF = "available:subtract";
    public static final String MAP_GROUP_SPOOFED_LAST = "available:last_spoofed";


    /*
        The size, in bytes, of a block on the file system. This corresponds to the Unix statvfs. f_frsize field.
        public long getBlockSizeLong()
            return mStat.f_frsize;

        //This is our target
        //We want to trick it into a different number of available blocks

        The number of blocks that are free on the file system and available to applications. This corresponds to the Unix statvfs. f_bavail field.
        public long getAvailableBlocksLong()
            return mStat.f_bavail;

     */

    public static final DynamicField FIELD_F_B_AVAIL = new DynamicField(StructStatVfs.class, "f_bavail")
            .setAccessible(true);

    public static final DynamicField FIELD_F_B_FREE = new DynamicField(StructStatVfs.class, "f_bfree")
            .setAccessible(true);

    public static boolean interceptStructVfs(XParam param) {
        try {
            StructStatVfs res = param.tryGetResult(null);
            if(res == null || res.f_bavail < 1000)
                return false;

            if(DebugUtil.isDebug())
                Log.d(TAG, "Intercepting [STAT VFS] Structure! Stack=" + RuntimeUtils.getStackTraceSafeString(new Exception()));

            //We use (f_blocks & f_files) Since it best represents the underlying System
            //Data Dir and External Mostly will return Same Size Data, But different IDs etc BUT Same f_blocks and f_files
            //Keep in mind this is to get Storage Avail Size, it looks to the System Storage Volume (most likely) not the actual Path
            String id = String.valueOf(String.format("%x-%x", res.f_blocks, res.f_files).hashCode());

            //Just like app times , generate a static offset not too big not too small
            //IN theory this Command / Struct is Only Used for SIZE related things so it wont be used for something else like perhaps regular Stat
            //So We can Make a Constructor Hook and Function Hooks!
            //Do note for the Future lets say the update after this, perhaps add a controlling setting ? After all if we want to mock Devices we need to mock total size etc
            //Since this is for AVAIL Size (small incremental offsets) this function is fine and Final. For Next update Control Total Size etc
            long sizeInMb = StorageUtils.bytesToMB(res.f_bavail * res.f_frsize);
            GroupedMap map = param.getGroupedMap(MAP_CATEGORY);
            if(!map.hasValue(MAP_GROUP_SUB_OFF, id)) {
                long randomSubtractionMb = RandomGenerator.nextLong(50, Math.min(sizeInMb - 5, 350));
                long newPotentialValueMb = sizeInMb - randomSubtractionMb;
                map.pushValue(MAP_GROUP_SUB_OFF, id, randomSubtractionMb);
                map.pushValue(MAP_GROUP_SPOOFED_LAST, id, newPotentialValueMb);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("File System with ID (%s) was Not Cached (Now Is) for its Fake Blocks Available Value. Calc(MB):%s Fake Subtraction Offset(MB):%s with New Potential Value Of (MB):%s Blocks Available=%s Free=%s",
                            id,
                            sizeInMb,
                            randomSubtractionMb,
                            newPotentialValueMb,
                            res.f_bavail,
                            res.f_frsize));
            }

            //As far as the "STORAGE_STATS_SERVICE" Method goes, just Call to the StatFs Version (let our Hook get Invoked) and Return that
            //StorageStatsManager.getFreeBytes(uuid)
            //All through we need some linking UUID to StatFs Crap...
            long lastSpoofed = map.getValueLong(MAP_GROUP_SPOOFED_LAST, id);
            if(lastSpoofed == sizeInMb) {
                //Then it was already Spoofed, No need to re spoof it, most likely a recursed Hook
                //This function is Fine does what it needs, despite it only updating once, that is fine
                //Also be careful if size changes in general ? then we need to update cache ? eh
                //We can just keep updating last spoof no matter what ?, specifically whenever the Code invokes to Set the Field ?
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("File System with ID (%s) is being Skipped For Available Size Spoofing as it is Already the Spoofed Valued, Spoofed (MB)=%s",
                            id,
                            lastSpoofed));

                return false;
            }

            long subtractMb = map.getValueLong(MAP_GROUP_SUB_OFF, id);
            long newTotalMb = sizeInMb - subtractMb;

            // Calculate how many blocks to subtract based on desired MB reduction
            long blocksToSubtract = StorageUtils.mbToBytes(subtractMb) / res.f_bsize;
            // Make sure we don't subtract more blocks than available
            blocksToSubtract = Math.min(blocksToSubtract, res.f_bavail - 1);
            // Calculate new number of available blocks
            long newAvailBlocks = res.f_bavail - blocksToSubtract;

            // Update both free block fields
            boolean bavailSuccess = FIELD_F_B_AVAIL.trySetValueInstanceEx(res, newAvailBlocks);
            boolean bfreeSuccess = FIELD_F_B_FREE.trySetValueInstanceEx(res, Math.min(res.f_bfree, newAvailBlocks));

            // Both fields must be updated successfully
            boolean wasSetSuccessfully = bavailSuccess && bfreeSuccess;

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("File System with ID (%s) (Blocks:%s Files:%s) was Intercepted with Old Available (MB):%s to Subtract (MB):%s new Result in (MB):%s , Blocks to Subtract=(%s) New Available Blocks=(%s) was Successful:%s (1)=%s (2)=%s",
                        id,
                        res.f_blocks,
                        res.f_files,
                        sizeInMb,
                        subtractMb,
                        newTotalMb,
                        blocksToSubtract,
                        newAvailBlocks,
                        wasSetSuccessfully,
                        bavailSuccess,
                        bfreeSuccess));

            if(wasSetSuccessfully) {
                param.setResult(res);
                param.setLogOld(String.valueOf(subtractMb) + " MB");
                param.setLogNew(String.valueOf(newTotalMb) + " MB");
                return true;
            }

            return false;
        }catch (Throwable e) {
            Log.e(TAG, "Failed to Intercept STATVFS Function! Error=" + e);
            return false;
        }
    }

    public static final int SPACE_FREE = ReflectUtil.useFieldValueOrDefaultInt("java.io.FileSystem", "SPACE_FREE", 1);

    /*
         java.io.File:: public long getFreeSpace()
         java.io.FileSystem:: public abstract long getSpace(File f, int t);
         java.io.UnixFileSystem:: public long getSpace(File f, int t)
         java.io.UnixFileSystem:: private native long getSpace0(File f, int t);
     */
    public static boolean interceptFileFreeSpace(XParam param) {
        try {
            Object p = param.isArgumentType(0, File.class) ?
                    param.getArgument(0) :
                    param.getThis();

            if(p != null) {
                if(!(p instanceof File)) {
                    Log.w(TAG, "Failed to Get File Object for Interception Free Space! P=" + p.getClass().getName() + " This=" + param.getThis().getClass().getName() + " Is Arg Type=" + param.isArgumentType(0, File.class));
                    return false;
                }

                if(param.isArgumentType(0, File.class)) {
                    int flag = param.tryGetArgument(1, -1);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Intercepting a getSpace(File,int) Method! Looking for FREE_SPACE(%s) Flag, Got (%s)",
                                SPACE_FREE,
                                flag));

                    if(flag != SPACE_FREE)
                        return false;
                }

                File path = (File) p;
                long oldSize = param.tryGetResult(0L);
                long oldSizeMb = StorageUtils.bytesToMB(oldSize);
                if(oldSize < 1 || oldSizeMb < 5) {
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Not Intercepting File (%s) Free Space (Results do not meet Size Requirements for Spoofing) Size Bytes=%s Size MB=%s",
                                path.getPath(),
                                oldSize,
                                oldSizeMb));

                    return false;
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Spoofing Free Space for Path (%s) Now Spoofing the Available Bytes! Original Size in Bytes (%s) and in MB (%s)",
                            path.getPath(),
                            oldSize,
                            oldSizeMb));

                StructStatVfs statStruct = Os.statvfs(path.getAbsolutePath());
                long newSizeMB = StorageUtils.bytesToMB(statStruct.f_bavail * statStruct.f_frsize);
                long newSizeBy = StorageUtils.mbToBytes(newSizeMB);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Spoofed Free Space for Path (%s) Original Free Bytes (%s) and in MB (%s) new Free Bytes (%s) and in MB (%s)",
                            path.getPath(),
                            oldSize,
                            oldSizeMb,
                            newSizeBy,
                            newSizeMB));

                if(newSizeBy != oldSize) {
                    param.setResult(newSizeBy);
                    param.setLogNew(String.valueOf(newSizeMB) + " MB");
                    param.setLogOld(String.valueOf(oldSizeMb) + " MB");
                    return true;
                }
            } else {
                Log.w(TAG, "Failed to Get File Object for Interception Free Space (NULL) ! This=" + param.getThis().getClass().getName() + " Is Arg Type=" + param.isArgumentType(0, File.class));
                return false;
            }
        }catch (Throwable e) {
            Log.e(TAG, "Failed to Intercept File.getFreeSpace() Function! Error=" + e);
            return false;
        }

        return false;
    }


    public static boolean interceptStorageStatsManager(XParam param) {
        try {
            UUID uuid = param.tryGetArgument(0, null);
            if(uuid != null) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Intercepting Storage UUID:" + uuid.toString());

                long oldSize = param.tryGetResult(0L);
                long oldSizeMb = StorageUtils.bytesToMB(oldSize);
                if(oldSize < 1 || oldSizeMb < 5)
                    return false;

                File path = StorageUtils.getStorageVolumeUuidFile(uuid, param.getApplicationContext());
                if(path == null) {
                    long randomMbs = RandomGenerator.nextLong(1, Math.min(250, oldSizeMb - 3));
                    long newResult = oldSize - StorageUtils.mbToBytes(randomMbs);
                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Failed to Find Storage Volume from UUID (%s) using Random MB Generator, Generated (%s) MB original MB (%s) and Bytes (%s) New Result (%s) MB and Bytes (%s)",
                                uuid.toString(),
                                randomMbs,
                                oldSizeMb,
                                oldSize,
                                StorageUtils.bytesToMB(newResult),
                                newResult));

                    param.setResult(newResult);
                    param.setLogNew(String.valueOf(StorageUtils.bytesToMB(newResult)) + " MB");
                    param.setLogOld(String.valueOf(oldSizeMb) + " MB");
                    return true;
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Resolved Storage UUID (%s) to Path (%s) Now Spoofing the Available Bytes! Original Size in Bytes (%s) and in MB (%s)",
                            uuid.toString(),
                            path.getPath(),
                            oldSize,
                            oldSizeMb));

                StructStatVfs statStruct = Os.statvfs(path.getAbsolutePath());
                long newSizeMB = StorageUtils.bytesToMB(statStruct.f_bavail * statStruct.f_frsize);
                long newSizeBy = StorageUtils.mbToBytes(newSizeMB);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Storage UUID (%s) Path (%s) Original Free Bytes (%s) and in MB (%s) new Free Bytes (%s) and in MB (%s)",
                            uuid.toString(),
                            path.getPath(),
                            oldSize,
                            oldSizeMb,
                            newSizeBy,
                            newSizeMB));

                if(newSizeBy != oldSize) {
                    param.setResult(newSizeBy);
                    param.setLogNew(String.valueOf(newSizeMB) + " MB");
                    param.setLogOld(String.valueOf(oldSizeMb) + " MB");
                    return true;
                }
            }
        }catch (Throwable e) {
            Log.e(TAG, "Failed to Intercept StorageStatsManager (getFreeBytes) ! Error=" + e);
            return false;
        }

        return false;
    }
}
