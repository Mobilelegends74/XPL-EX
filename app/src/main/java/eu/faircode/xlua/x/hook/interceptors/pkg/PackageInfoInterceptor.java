package eu.faircode.xlua.x.hook.interceptors.pkg;

import android.content.pm.PackageInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.utilities.DateTimeUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.hook.interceptors.file.FileTimeInterceptor;
import eu.faircode.xlua.x.hook.interceptors.zone.RandomDateHelper;
import eu.faircode.xlua.x.data.GroupedMap;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class PackageInfoInterceptor {
    private static final String TAG = LibUtil.generateTag(PackageInfoInterceptor.class);

    public static final String RAND_ONCE = "%random.once%";
    public static final String RAND_ALWAYS = "%random.always%";

    public static final String NOW_ONCE = "%now.once%";
    public static final String NOW_ALWAYS = "%now.always%";




    //public static final String ONLY_CURRENT_APP = RandomizersCache.SETTING_APP_TIME_CURRENT_ONLY;


    public static final String INSTALL_CURRENT_OFFSET_SETTING = "apps.current.install.time.offset";
    public static final String UPDATE_CURRENT_OFFSET_SETTING = "apps.current.update.time.offset";

    public static final String INSTALL_OFFSET_SETTING = "apps.install.time.offset";
    public static final String UPDATE_OFFSET_SETTING = "apps.update.time.offset";

    public static final String INSTALL_GROUP = "installTime";
    public static final String UPDATE_GROUP = "updateTime";

    /*

        /data/app/~~NoGhw4jVe309LEdm67pEGQ==/com.obbedcocde.mydevids-W8OniJ6cMW_wtmevk_Sk4A==/base.apk

         Path apkPath = Paths.get(apkFile.getAbsolutePath());
         Object creationTimeObj = Files.getAttribute(apkPath, "creationTime", LinkOption.NOFOLLOW_LINKS);
         Object accessTimeObj = Files.getAttribute(apkPath, "lastAccessTime", LinkOption.NOFOLLOW_LINKS);
         Object modifiedTimeObj = Files.getAttribute(apkPath, "lastModifiedTime", LinkOption.NOFOLLOW_LINKS);

        if (accessTimeObj instanceof FileTime)
            timestamp = ((FileTime) accessTimeObj).toMillis();


         java.io.File
         java.nio.file.Files
         java.nio.file.Path
         java.nio.file.Paths
         java.nio.file.attribute.FileTime

         Improve on filter system ? filters like hooks and rules, easier so like example
         Want to stat spoof they can enable stat filters, it will look for settings
     */


    //lets invoke this and others no matter what ?
    //Simply put maybe we can copy paste lua defs ?
    //Have a dep system, when they assign hooks or hooks aigner looks up list if that hooks requires another hook ?
    //In this case these hooks require file stat ? or ?

    //amll object handling sets etc
    //redirect fioole oprns to points to real location so we can spoof to fake location
    //as result wrapper class ?
    //Make a cool hook like (*) but for types like


    public static boolean interceptTimeStamps(XParam param, boolean isReturn) {
        try {
            Object res = param.tryGetResult(null);
            Object ths = param.getThis();

            Object obj = null;
            if(isReturn) {
                if(!(res instanceof PackageInfo)) {
                    isReturn = false;
                    obj = ths;
                } else {
                    obj = res;
                }
            } else {
                if(!(ths instanceof PackageInfo)) {
                    isReturn = true;
                    obj = res;
                } else {
                    obj = ths;
                }
            }

            if(!(obj instanceof PackageInfo)) {
                Log.e(TAG, "Object is Null or Not Instance of PackageInfo, IsReturn=" + isReturn + " This Clazz=" + Str.toStringOrNull(ths) + " Res Clazz=" + Str.toStringOrNull(res) + " Stack=" + RuntimeUtils.getStackTraceSafeString(new Exception()));
                return false;
            }

            PackageInfo pkgInfo = (PackageInfo) obj;
            boolean isCurrent = pkgInfo.packageName.equalsIgnoreCase(param.getPackageName());
            if(isCurrent) {
                FileTimeInterceptor interceptor = FileTimeInterceptor.create(param.getPackageName(), param);
                long installOffset = interceptor.getCreatedOffset();
                long updateOffset = interceptor.getModifiedOffset();

                long originalInstall = interceptor.getOriginalCreated(pkgInfo.firstInstallTime);
                long originalUpdate = interceptor.getOriginalModified(pkgInfo.lastUpdateTime);

                long installFake = interceptor.getFinalValue(installOffset, originalInstall);
                long updateFake = interceptor.getFinalValue(updateOffset, originalUpdate);
                if(DebugUtil.isDebug())
                    Log.d(TAG, "PKG Time Spoof Self App [" + param.getPackageName() + "]" +
                            "\n Install Original: " + originalInstall +
                            "\n Install Offset: " + installOffset +
                            "\n Install Fake: " + installFake +
                            "\n Update Original: " + originalUpdate +
                            "\n Update Offset: " + updateOffset +
                            "\n Update Fake: " + updateFake);

                pkgInfo.firstInstallTime = installFake;
                pkgInfo.lastUpdateTime = updateFake;
                if(isReturn)
                    param.setResult(pkgInfo);

                return true;
            } else {
                GroupedMap map = param.getGroupedMap(GroupedMap.MAP_APP_TIMES);
                long[] times = RandomDateHelper.generateEpochTimeStamps(2, true);
                long install = PkgHookUtils.getTime(INSTALL_GROUP, pkgInfo.packageName,
                        param.getSetting(INSTALL_OFFSET_SETTING), map, times[0], pkgInfo.firstInstallTime);
                long update = PkgHookUtils.getTime(UPDATE_GROUP, pkgInfo.packageName,
                        param.getSetting(UPDATE_OFFSET_SETTING), map, times[1], pkgInfo.lastUpdateTime);

                if(DebugUtil.isDebug())
                    Log.d(TAG, "PKG Time Spoof [" + param.getPackageName() + "] p=[" + pkgInfo.packageName + "]" +
                            "\n Install Original: " + pkgInfo.firstInstallTime +
                            "\n Install Fake: " + install +
                            "\n Update Original: " + pkgInfo.lastUpdateTime +
                            "\n Update Fake: " + update);

                pkgInfo.firstInstallTime = install;
                pkgInfo.lastUpdateTime = update;
                if(isReturn)
                    param.setResult(pkgInfo);

                return true;
            }
            /*if(obj instanceof PackageInfo) {

            } else {
                Log.e(TAG, "This is NOT a package Info Class Type Weird... " + obj + " Type=" + (obj == null ? "null" : obj.getClass().getName()) + " Clazz=" + Str.toStringOrNull(param.getThis()) + " IsReturn=" + isReturn +  " Stack=" + Str.ensureNoDoubleNewLines(RuntimeUtils.getStackTraceSafeString(new Exception())));
                return false;
            }*/
        }catch (Throwable e) {
            Log.e(TAG, "Failed to Spoof Package Install & Update Times ! " + e);
            return false;
        }
    }


    public static boolean isPackageAllowed(XParam param, String packageName) {
        if(TextUtils.isEmpty(packageName) || param.getPackageName().equalsIgnoreCase(packageName)) return true;
        try {
            boolean blacklist = param.getSettingBool("apps.blacklist.mode.bool", false);
            Object map = param.getValue("OBC.AppList", param.getApplicationContext());
            if(map == null) {
                //Create it
                HashMap<String, Boolean> list = new HashMap<>();

            }

            return true;
        }catch (Throwable e) {
            Log.e(TAG, "Failed to Determine if Package is Allowed... p=" + packageName + " Error=" + e + " Stack=" + Log.getStackTraceString(e));
            return false;
        }
    }

    /*@SuppressWarnings("unused")
    public boolean isPackageAllowed(String str) {
        if(str == null || str.isEmpty() || str.equalsIgnoreCase(this.packageName)) return true;
        str = str.toLowerCase().trim();
        String blackSetting = getSetting("apps.blacklist.mode.bool");
        if(blackSetting != null) {
            boolean isBlacklist = StringUtil.toBoolean(blackSetting, false);
            if(isBlacklist) {
                String block = getSetting("apps.block.list");
                if(Str.isValidNotWhitespaces(block)) {
                    block = block.trim();
                    if(block.contains(",")) {
                        String[] blocked = block.split(",");
                        for(String b : blocked)
                            if(b.trim().equalsIgnoreCase(str))
                                return false;
                    }else {
                        if(block.equalsIgnoreCase("*"))
                            return false;
                        else if(block.equalsIgnoreCase(str))
                            return false;
                    }
                }
            } else {
                String allow = getSetting("apps.allow.list");
                if(Str.isValidNotWhitespaces(allow)) {
                    allow = allow.trim();
                    if(allow.contains(",")) {
                        String[] allowed = allow.split(",");
                        for(String a : allowed)
                            if(a.trim().equalsIgnoreCase(str))
                                return true;
                    }else {
                        if(allow.equalsIgnoreCase("*"))
                            return true;
                        else if(allow.equalsIgnoreCase(str))
                            return true;
                    }
                }
                if(Evidence.packageName(str, 3))
                    return false;

                //Revert this
                if(getSettingBool("apps.blacklist.allow.vital.apps.bool", true)) {
                    for(String p : ALLOWED_PACKAGES)
                        if(str.contains(p))
                            return true;
                }

                return false;
            }
        } return !Evidence.packageName(str, 3);
    }*/
}
