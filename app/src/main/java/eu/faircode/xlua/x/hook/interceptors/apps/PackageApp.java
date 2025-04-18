package eu.faircode.xlua.x.hook.interceptors.apps;

public class PackageApp {
    public String packageName;
    public long installTime;
    public long updateTime;


    //spoof UID
    //spoof Installer App ?
    //spoof sourceDir is its the main app ?

    //frameworks/base/services/core/java/com/android/server/pm/IPackageManagerBase.java
    //frameworks/base/services/core/java/com/android/server/am/ActivityManagerService.java
    //frameworks/base/core/java/android/app/ApplicationPackageManager.java
    //frameworks/base/services/core/java/com/android/server/pm/Computer.java
    //com.android.server.pm.PackageManagerService (getArchivedPackageInternal)
    //com.android.server.pm.AppsFilterImpl
    //public boolean hide;
    //Disable intents ?
    //Super hook kind like (*)
    //Make a multiple list option hook ? for ones using same lua script

}
