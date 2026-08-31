package eu.faircode.xlua.xposed.api101;

import android.util.Log;

import androidx.annotation.NonNull;

import eu.faircode.xlua.XLua;
import io.github.libxposed.api.XposedModule;

/** libxposed API 101 entry point instantiated once in every scoped process. */
public final class ModernEntryPoint extends XposedModule {
    private static final String TAG = "XPL-EX/API101";

    private XLua hookRuntime;
    private String processName = "unknown";
    private boolean systemServer;

    @Override
    public void onModuleLoaded(@NonNull ModuleLoadedParam param) {
        ModernXposedBridge.attach(this);
        hookRuntime = new XLua();
        processName = param.getProcessName();
        systemServer = param.isSystemServer();
        log(Log.INFO, TAG, "Module loaded: process=" + processName
                + " framework=" + getFrameworkName()
                + " version=" + getFrameworkVersion()
                + " api=" + getApiVersion());
    }

    @Override
    public void onPackageReady(@NonNull PackageReadyParam param) {
        // API 101 loads Settings Provider code into system_server. Its callback
        // is deliberately not the first package callback in that process.
        if (systemServer) {
            if (!"com.android.providers.settings".equals(param.getPackageName()))
                return;
        } else if (!param.isFirstPackage()) {
            // XPL-EX installs process-wide application hooks only for the main
            // scoped package, not packages loaded later via createPackageContext.
            return;
        }
        dispatch(new ModernLoadPackage.LoadPackageParam(
                param.getPackageName(), processName, param.getClassLoader(),
                param.getApplicationInfo(), param.isFirstPackage()));
    }

    @Override
    public void onSystemServerStarting(@NonNull SystemServerStartingParam param) {
        dispatch(new ModernLoadPackage.LoadPackageParam(
                "android", processName, param.getClassLoader(), null, true));
    }

    private void dispatch(ModernLoadPackage.LoadPackageParam param) {
        try {
            if (hookRuntime == null)
                throw new IllegalStateException("Module lifecycle is not initialized");
            hookRuntime.handleLoadPackage(param);
            log(Log.INFO, TAG, "Hook installation completed: package=" + param.packageName
                    + " process=" + param.processName);
        } catch (Throwable error) {
            log(Log.ERROR, TAG, "Hook installation failed: package=" + param.packageName
                    + " process=" + param.processName, error);
        }
    }
}
