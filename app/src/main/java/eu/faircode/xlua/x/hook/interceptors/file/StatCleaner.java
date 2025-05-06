package eu.faircode.xlua.x.hook.interceptors.file;

import android.os.Build;
import android.util.Log;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.interceptors.shell.ShellInterception;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.hook.interceptors.file.cleaners.StatCommandCleaner;
import eu.faircode.xlua.x.hook.interceptors.file.cleaners.StatStructCleaner;
import eu.faircode.xlua.x.hook.interceptors.file.cleaners.StatUtils;
import eu.faircode.xlua.x.xlua.LibUtil;

public class StatCleaner {
    private static final String TAG = LibUtil.generateTag(StatCleaner.class);

    public static final String NIO_TAG_LAST_ACCESS = "lastAccessTime";
    public static final String NIO_TAG_LAST_MODIFIED = "lastModifiedTime";
    public static final String NIO_TAG_CREATED = "creationTime";

    public static boolean isTimeNioFileTag(String tag) {
        return
            tag.equalsIgnoreCase(NIO_TAG_LAST_ACCESS) ||
            tag.equalsIgnoreCase(NIO_TAG_LAST_MODIFIED) ||
            tag.equalsIgnoreCase(NIO_TAG_CREATED); }


    public static boolean cleanStatCommand(String file, String output, ShellInterception result) {
        try {
            if(DebugUtil.isDebug())
                Log.d(TAG, "Cleaning STAT Command Output for File=" + file + " Output=" + output);

            TimeInterceptor interceptor = TimeInterceptor.create(file, result.param);
            String outputRes = result.getCommandOutput();
            if(outputRes.equals("---") || outputRes.equals("--") || outputRes.equals("-"))
                return false;

            result.param.setLogOld(output);
            result.param.setLogExtra("stat >> " + file);
            if(result.hasCommand("%Y", false)) {
               long original =  interceptor.getOriginalModify(StatStructCleaner.secondsToMillis(Str.tryParseLong(outputRes)));
               //long offset = interceptor.getModifiedOffset();
               //long fake = interceptor.getFinalValue(offset, original);
                long fake = interceptor.getModify(original);
               result.setIsMalicious(true);

               String fakeValue = String.valueOf(StatStructCleaner.millisToSeconds(fake));
               result.setNewValue(fakeValue);
               result.param.setLogNew(fakeValue);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Is Last Modified (epoch) File=[%s] Arg for Stat, Original MS=%s Fake MS=%s Original Hex [%s](%s) Fake Hex [%s](%s) Actual Output=%s",
                            file,
                            original,
                            fake,
                            outputRes.length(),
                            Str.toHex(output),
                            fakeValue.length(),
                            Str.toHex(fakeValue),
                            outputRes));
               return true;
            }
            else if(result.hasCommand("%y", false)) {
                long toMilliseconds = StatUtils.toEpochMillis(outputRes);
                long original = interceptor.getOriginalModify(toMilliseconds);
                //long fake = interceptor.getFinalValue(interceptor.getModifiedOffset(), original);
                long fake = interceptor.getModify(original);
                String fakeToHuman = StatUtils.fromEpochMillis(fake, StatUtils.detectFormat(output));
                result.setIsMalicious(true);
                result.setNewValue(fakeToHuman);
                result.param.setLogNew(fakeToHuman);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Is Last Modified (Human Readable) Arg for Stat! Human Date Original=" + outputRes +
                            " Original MS=" + output +
                            " Fake MS=" + fake +
                            " Fake Human Date=" + fakeToHuman);
                return true;
            }


            if(result.hasCommand("%Z", false)) {
                long original =  interceptor.getOriginalChange(StatStructCleaner.secondsToMillis(Str.tryParseLong(outputRes)));
                //long offset = interceptor.getChangeOffset();
                //long fake = interceptor.getFinalValue(offset, original);
                long fake = interceptor.getChange(original);
                result.setIsMalicious(true);

                String fakeValue = String.valueOf(StatStructCleaner.millisToSeconds(fake));
                result.setNewValue(fakeValue);
                result.param.setLogNew(fakeValue);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Is Last Changes (epoch) File=[%s] Arg for Stat, Original MS=%s Fake MS=%s Original Hex [%s](%s) Fake Hex [%s](%s) Actual Output=%s",
                            file,
                            original,
                            fake,
                            outputRes.length(),
                            Str.toHex(output),
                            fakeValue.length(),
                            Str.toHex(fakeValue),
                            outputRes));

                return true;
            }
            else if(result.hasCommand("%z", false)) {
                //To make even more sharp, perhaps replace the old with new in the string ?
                long toMilliseconds = StatUtils.toEpochMillis(outputRes);
                long original = interceptor.getOriginalChange(toMilliseconds);
                //long fake = interceptor.getFinalValue(interceptor.getChangeOffset(), original);
                long fake = interceptor.getChange(original);
                String fakeToHuman = StatUtils.fromEpochMillis(fake, StatUtils.detectFormat(output));
                //ToDo: Ensure uppercase formats etc are supported , you lack uppercase or lowercase one of the two
                result.setIsMalicious(true);
                result.setNewValue(fakeToHuman);
                result.param.setLogNew(fakeToHuman);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Is Last Change (Human Readable) Arg for Stat! Human Date Original=" + outputRes +
                            " Original MS=" + output +
                            " Fake MS=" + fake +
                            " Fake Human Date=" + fakeToHuman);
                return true;
            }

            if(result.hasCommand("%X", false)) {
                long original =  interceptor.getOriginalAccess(StatStructCleaner.secondsToMillis(Str.tryParseLong(outputRes)));
                //long offset = interceptor.getAccessOffset();
                //long fake = interceptor.getFinalValue(offset, original);
                long fake = interceptor.getAccess(original);
                result.setIsMalicious(true);

                String fakeValue = String.valueOf(StatStructCleaner.millisToSeconds(fake));
                result.setNewValue(fakeValue);
                result.param.setLogNew(fakeValue);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Is Last Access (epoch) File=[%s] Arg for Stat, Original MS=%s Fake MS=%s Original Hex [%s](%s) Fake Hex [%s](%s) Actual Output=%s",
                            file,
                            original,
                            fake,
                            outputRes.length(),
                            Str.toHex(output),
                            fakeValue.length(),
                            Str.toHex(fakeValue),
                            outputRes));

                return true;
            }
            else if(result.hasCommand("%x", false)) {
                long toMilliseconds = StatUtils.toEpochMillis(outputRes);
                long original = interceptor.getOriginalAccess(toMilliseconds);
                //long fake = interceptor.getFinalValue(interceptor.getAccessOffset(), original);
                long fake = interceptor.getAccess(original);
                String fakeToHuman = StatUtils.fromEpochMillis(fake, StatUtils.detectFormat(output));
                result.setIsMalicious(true);
                result.setNewValue(fakeToHuman);
                result.param.setLogNew(fakeToHuman);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Is Last Access (Human Readable) Arg for Stat! Human Date Original=" + outputRes +
                            " Original MS=" + output +
                            " Fake MS=" + fake +
                            " Fake Human Date=" + fakeToHuman);
                return true;
            }


            /*if(result.hasCommand("%W", false)) {
                long original =  interceptor.getC(StatStructCleaner.secondsToMillis(Str.tryParseLong(outputRes)));
                long offset = interceptor.getAccessOffset();
                long fake = original + offset;
                result.setIsMalicious(true);

                String fakeValue = String.valueOf(StatStructCleaner.millisToSeconds(fake));
                result.setNewValue(fakeValue);
                result.param.setNewResult(fakeValue);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Is Last Access (epoch) File=[%s] Arg for Stat, Offset=%s Original MS=%s Fake MS=%s Original Hex [%s](%s) Fake Hex [%s](%s) Actual Output=%s",
                            file,
                            offset,
                            original,
                            fake,
                            outputRes.length(),
                            Str.toHex(output),
                            fakeValue.length(),
                            Str.toHex(fakeValue),
                            outputRes));

                return true;
            }
            else if(result.hasCommand("%x", false)) {
                long toMilliseconds = StatUtils.toEpochMillis(outputRes);
                long original = interceptor.getOriginalAccess(toMilliseconds);
                long fake = original + interceptor.getAccessOffset();
                String fakeToHuman = StatUtils.fromEpochMillis(fake, StatUtils.detectFormat(output));
                result.setIsMalicious(true);
                result.setNewValue(fakeToHuman);
                result.param.setNewResult(fakeToHuman);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Is Last Access (Human Readable) Arg for Stat! Human Date Original=" + outputRes +
                            " Original MS=" + output +
                            " Fake MS=" + fake +
                            " Fake Human Date=" + fakeToHuman);
                return true;
            }*/

            //Change/Modify may be the same no ?
            //Also try to make system so if one is same value then it shares the nearest cache etc
            //Do note it can also use "grep" and can fuck with the algorithm
            String fake = StatCommandCleaner.parseFake(output, interceptor);
            if(DebugUtil.isDebug())
                Log.d(TAG, "Cleaned out Stat Command for File:[" + file + "] Old=" + Str.ensureNoDoubleNewLines(output) + " New=" + Str.ensureNoDoubleNewLines(fake));

            result.setIsMalicious(true);
            result.setNewValue(fake);
            result.param.setLogNew(fake);
            return true;
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting STAT Command, File=" + file + " Output=" + output + " Error=" + e);
            return false;
        }
    }

    //Make a version where the arguments are "*" so it gets any and all members ?
    public static boolean cleanNioAttributes(XParam param) {
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                Path path = param.tryGetArgument(0, null);
                String tag = param.tryGetArgument(1, Str.EMPTY);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Intercepting NIO File: Path=" + Str.toStringOrNull(path) + " Tag=" + tag);

                if(!isTimeNioFileTag(tag))
                    return false;

                if(DebugUtil.isDebug())
                    Log.d(TAG, "Intercepting NIO File Stamps for:" + path + " Tag=" + tag);

                FileTime res = param.tryGetResult(null);
                if(res == null)
                    return false;

                String pathName = path.toFile().getAbsolutePath();
                TimeInterceptor interceptor = TimeInterceptor.create(pathName, param);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "Intercepting NIO File Time Stamp: " + pathName + " Original:" + res.toMillis());

                long original = 0;
                long modified = 0;
                switch (tag) {
                    case NIO_TAG_LAST_ACCESS:
                        original = interceptor.getOriginalAccess(res.toMillis());
                        modified = interceptor.getAccess(original);
                        break;
                    case NIO_TAG_LAST_MODIFIED:
                        original = interceptor.getOriginalModify(res.toMillis());
                        modified = interceptor.getModify(original);
                        break;
                    case NIO_TAG_CREATED:
                        original = interceptor.getOriginalCreated(res.toMillis());
                        modified = interceptor.getCreation(original);
                        break;
                    default:
                        Log.w(TAG, "Is not Attribute! NIO");
                        return false;
                }

                FileTime fake = FileTime.fromMillis(modified);
                param.setLogOld(String.valueOf(original));
                param.setLogNew(String.valueOf(fake.toMillis()));
                param.setResult(fake);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "NIO File:[" + pathName + "] Tag=(" + tag + ")" +
                            " Time Stamps!" +
                            " Old=" + original +
                            " New=" + fake.toMillis() +
                            " Interceptor File=" + interceptor.fileOrApp);

                return true;
            }

            return false;
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting Java Nio Apis for Stat APK! Error=" + e);
            return false;
        }
    }

    public static boolean cleanFileLastModified(XParam param) {
        try {
            File ths = (File) param.getThis();
            String file = ths.getAbsolutePath();
            if(Str.isEmpty(file))
                return false;

            if(DebugUtil.isDebug())
                Log.d(TAG, "Intercepting File.lastModified() File Stamps for:" + file);

            TimeInterceptor interceptor = TimeInterceptor.create(file, param);
            long def = 0L;
            long res = param.tryGetResult(def);
            //long org = interceptor.getOriginalModify(param.tryGetResult(def));
            //long offset = interceptor.getModifiedOffset();
            //long fake = interceptor.getFinalValue(offset, org);
            long fake = interceptor.getModify(res);
            param.setResult(fake);
            param.setLogOld(String.valueOf(res));
            param.setLogNew(String.valueOf(fake));
            if(DebugUtil.isDebug())
                Log.d(TAG, "File.lastModified(" + file + ") Interceptor File=(" + interceptor.fileOrApp + ") Original MS=" + res + " Fake MS=" + fake);

            return true;
        }catch (Throwable e) {
            Log.e(TAG, "Error Intercepting File.lastModified! Error: " + e);
            return false;
        }
    }

    public static boolean cleanOsStatStructure(XParam param) {
        String file = param.tryGetArgument(0, null);
        if(Str.isEmpty(file))
            return false;

        if(DebugUtil.isDebug())
            Log.d(TAG, "Intercepting Os.stat() File Stamps for:" + file);

        TimeInterceptor interceptor = TimeInterceptor.create(file, param);
        return StatStructCleaner.cleanStructure(interceptor, param.tryGetResult(null));
    }
}
