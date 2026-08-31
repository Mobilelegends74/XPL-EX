package eu.faircode.xlua.utilities;

import android.content.Context;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.util.UUID;

import eu.faircode.xlua.xposed.api101.ModernXposedBridge;
import eu.faircode.xlua.XposedUtil;
import eu.faircode.xlua.rootbox.XFileUtils;

public class DatabasePathUtil {
    private static final String TAG = "XLua.DatabasePathUtil";
    //private static String lastKnownPath = null;
    //private static final String NEW_PATH = "/da"
    //make a function to ensure not alot of copies ? find what has all the files

    public static void log(String data, boolean error) {
        ModernXposedBridge.log(data);
        if(error)
            Log.e(TAG, data);
        else
            Log.i(TAG, data);
    }


    public static boolean ensureDirectoryChange(Context context) {
        if(context != null && XposedUtil.isVirtualXposed())
            return true;

        //First get the Original Directory then the "new" one by finding it
        //If finding it does not exist then create it
        //If old Directory exists then Copy files to new Directory
        //If Copied work then delete old directory
        //else if Old Dir does not exist just ensure new is created return true

        //Dont fuck this up again
        //Fuck this up cut your (I) hands off and rip out eyes as you (I) is not worthy of programming
        //STOP MAKING POSH FUCKING MISTAKES
        String oldDirectoryPath = getOriginalDataLocationString(null);
        File oldDirectory = new File(oldDirectoryPath);

        File newDirectory = getDatabaseDirectory(context);
        if(newDirectory == null) {
            //It does not exist the new Directory so lets at least create it
            String newDirectoryPath = getDefaultLocationString(null) + "-" + UUID.randomUUID().toString();
            newDirectory = new File(newDirectoryPath);
            log("XPL-EX Created new Directory: " + newDirectoryPath, false);
            XFileUtils.mkdirs(newDirectory);

            if(oldDirectory.exists() && oldDirectory.isDirectory()) {
                log("Original Directory Exists now copying over Files: " + oldDirectoryPath + " to " + newDirectoryPath, false);

                if (!XFileUtils.copyDirectories(oldDirectory, newDirectory, true, Process.SYSTEM_UID)) {
                    log("Failed to Copy Files (manually do it please) from: " + oldDirectoryPath + " to " + newDirectory, true);
                    //XFileUtils.forceDelete(newDirectory, true, Process.SYSTEM_UID);
                    //New directory is still created so we might as well use it then still
                    //return true;
                    return newDirectory.exists();
                }else {
                    if(!XFileUtils.forceDelete(oldDirectory, true, Process.SYSTEM_UID)) {
                        log("Failed to Delete Old Directory=" + oldDirectoryPath, true);
                    }else {
                        log("Deleted old Directory=" + oldDirectoryPath, false);
                    }
                }
            }
        }

        return true;
    }

    /*public static boolean ensureDirectoryChange(Context context) {
        if(context != null && XposedUtil.isVirtualXposed())
            return true;

        Log.i(TAG, "ensureDirectoryChange");
        ModernXposedBridge.log("ensureDirectoryChange");
        String originalDirectory = getOriginalDataLocationString(null);
        File oDir = new File(originalDirectory);
        Log.i(TAG, "Ensuring Directory does not exist: " + originalDirectory);
        ModernXposedBridge.log("Ensuring Directory does not exist: " + originalDirectory);

        File nDir = getDatabaseDirectory(context);
        if (nDir == null) {
            String newDir = getDefaultLocationString(null) + "-" + UUID.randomUUID().toString();
            Log.i(TAG, "New Directory! =" + newDir);
            nDir = new File(newDir);
            ModernXposedBridge.log("New Directory: " + nDir.getAbsolutePath());
            XFileUtils.mkdirs(nDir);
        }

        if(oDir.exists() && oDir.isDirectory()) {
            if (!XFileUtils.copyDirectories(oDir, nDir, true, Process.SYSTEM_UID)) {
                Log.e(TAG, "Failed to Copy over the Database Files to the new Directory! new dir=" + nDir.getAbsolutePath());
                ModernXposedBridge.log("Failed to Copy over the Database Files to the new Directory! new dir=" + nDir.getAbsolutePath());
                XFileUtils.forceDelete(nDir, true, Process.SYSTEM_UID);
                return false;
            }

            if(!XFileUtils.forceDelete(oDir, true, Process.SYSTEM_UID)) {
                ModernXposedBridge.log("Failed to Delete Old Directory=" + oDir.getAbsolutePath());
                Log.e(TAG, "Failed to Delete Old Directory=" + oDir.getAbsolutePath());
            }
        }

        return true;
    }*/

    public static File getDatabaseDirectory(Context context) {
        if (context != null && XposedUtil.isVirtualXposed())
            return context.getFilesDir();

        return XFileUtils.getFirstDirectoryNameLike(Environment.getDataDirectory() + File.separator + "system", "xplex-");
    }

    public static String getOriginalDataLocationString(Context context) {
        if (context != null && XposedUtil.isVirtualXposed()) {
            return context.getFilesDir().getPath();
        } else {
            return Environment.getDataDirectory() + File.separator +
                    "system" + File.separator +
                    "xlua";
        }
    }

    public static String getDefaultLocationString(Context context) {
        if (context != null && XposedUtil.isVirtualXposed()) {
            return context.getFilesDir().getPath();
        } else {
            return Environment.getDataDirectory() + File.separator +
                    "system" + File.separator +
                    "xplex";
        }
    }
}
