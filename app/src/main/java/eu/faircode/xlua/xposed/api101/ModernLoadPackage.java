package eu.faircode.xlua.xposed.api101;

import android.content.pm.ApplicationInfo;

/** Process/package data consumed by the pre-existing XPL-EX hook installer. */
public final class ModernLoadPackage {
    private ModernLoadPackage() {
    }

    public static final class LoadPackageParam {
        public final String packageName;
        public final String processName;
        public final ClassLoader classLoader;
        public final ApplicationInfo appInfo;
        public final boolean isFirstApplication;

        public LoadPackageParam(String packageName,
                                String processName,
                                ClassLoader classLoader,
                                ApplicationInfo appInfo,
                                boolean isFirstApplication) {
            this.packageName = packageName;
            this.processName = processName;
            this.classLoader = classLoader;
            this.appInfo = appInfo;
            this.isFirstApplication = isFirstApplication;
        }
    }
}
