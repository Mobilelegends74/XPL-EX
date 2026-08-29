package eu.faircode.xlua.x.hook.interceptors.apps;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import eu.faircode.xlua.XParam;

/** Creates a small, coherent package inventory matching the selected device OEM. */
public final class VirtualAppCatalog {
    private static final String[] COMMON = {
            "android", "com.android.systemui", "com.android.settings",
            "com.android.providers.settings", "com.android.providers.media",
            "com.android.providers.downloads", "com.android.documentsui",
            "com.android.permissioncontroller", "com.android.phone",
            "com.android.contacts", "com.android.mms", "com.android.dialer",
            "com.android.camera2", "com.android.gallery3d", "com.android.calculator2",
            "com.android.calendar", "com.android.deskclock"
    };

    private static final String[] GOOGLE = {
            "com.google.android.gms", "com.android.vending",
            "com.google.android.gsf", "com.google.android.googlequicksearchbox",
            "com.google.android.apps.nexuslauncher", "com.google.android.dialer",
            "com.google.android.contacts", "com.google.android.apps.messaging",
            "com.google.android.GoogleCamera", "com.google.android.apps.photos",
            "com.google.android.apps.maps", "com.google.android.youtube",
            "com.android.chrome", "com.google.android.gm"
    };

    private static final Map<String, String[]> OEM;

    static {
        Map<String, String[]> packages = new LinkedHashMap<>();
        packages.put("google", GOOGLE);
        packages.put("samsung", new String[]{
                "com.sec.android.app.launcher", "com.samsung.android.dialer",
                "com.samsung.android.messaging", "com.samsung.android.contacts",
                "com.sec.android.app.camera", "com.sec.android.gallery3d",
                "com.sec.android.app.clockpackage", "com.sec.android.app.myfiles",
                "com.samsung.android.calendar", "com.samsung.android.game.gamehome",
                "com.samsung.android.game.gametools", "com.samsung.android.oneconnect"
        });
        packages.put("xiaomi", new String[]{
                "com.miui.home", "com.android.contacts", "com.android.mms",
                "com.android.camera", "com.miui.gallery", "com.miui.securitycenter",
                "com.miui.player", "com.miui.videoplayer", "com.miui.notes",
                "com.mi.android.globalFileexplorer", "com.xiaomi.mipicks",
                "com.xiaomi.gamecenter"
        });
        packages.put("oneplus", new String[]{
                "net.oneplus.launcher", "com.oneplus.dialer", "com.oneplus.mms",
                "com.oneplus.camera", "com.oneplus.gallery", "com.oneplus.filemanager",
                "com.oneplus.calculator", "com.oneplus.deskclock",
                "com.oneplus.gamespace", "com.oplus.games"
        });
        packages.put("asus", new String[]{
                "com.asus.launcher", "com.asus.dialer", "com.asus.contacts",
                "com.asus.camera", "com.asus.gallery", "com.asus.filemanager",
                "com.asus.calculator", "com.asus.deskclock", "com.asus.gamecenter"
        });
        packages.put("lenovo", new String[]{
                "com.zui.launcher", "com.android.dialer", "com.android.contacts",
                "com.zui.camera", "com.zui.gallery", "com.zui.filemanager",
                "com.zui.calculator", "com.zui.deskclock", "com.lenovo.gameassistant"
        });
        packages.put("motorola", new String[]{
                "com.motorola.launcher3", "com.google.android.dialer",
                "com.google.android.contacts", "com.motorola.camera3",
                "com.motorola.motogallery", "com.motorola.actions",
                "com.motorola.moto", "com.motorola.gamemode"
        });
        packages.put("meizu", new String[]{
                "com.meizu.flyme.launcher", "com.android.dialer", "com.android.contacts",
                "com.meizu.media.camera", "com.meizu.media.gallery",
                "com.meizu.filemanager", "com.meizu.safe", "com.meizu.flyme.gamecenter"
        });
        packages.put("vivo", new String[]{
                "com.bbk.launcher2", "com.android.dialer", "com.android.contacts",
                "com.android.bbkmusic", "com.android.VideoPlayer",
                "com.vivo.camera", "com.vivo.gallery", "com.vivo.filemanager",
                "com.vivo.game", "com.vivo.gamewatch"
        });
        packages.put("nubia", new String[]{
                "cn.nubia.launcher", "cn.nubia.contacts", "cn.nubia.mms",
                "cn.nubia.camera", "cn.nubia.gallery3d", "cn.nubia.filebrowser",
                "cn.nubia.calculator2", "cn.nubia.deskclock.preset",
                "cn.nubia.security2", "cn.nubia.gamecenter", "cn.nubia.gamelauncher"
        });
        OEM = Collections.unmodifiableMap(packages);
    }

    private VirtualAppCatalog() { }

    public static List<ApplicationInfo> mergeApplications(XParam param, Object original) {
        LinkedHashMap<String, ApplicationInfo> result = new LinkedHashMap<>();
        if (original instanceof List<?>) {
            for (Object item : (List<?>) original)
                if (item instanceof ApplicationInfo && ((ApplicationInfo) item).uid == param.getUid())
                    result.put(((ApplicationInfo) item).packageName, (ApplicationInfo) item);
        }
        for (String packageName : packagesFor(param))
            if (!result.containsKey(packageName))
                result.put(packageName, applicationInfo(param, packageName));
        return new ArrayList<>(result.values());
    }

    public static List<PackageInfo> mergePackages(XParam param, Object original) {
        LinkedHashMap<String, PackageInfo> result = new LinkedHashMap<>();
        if (original instanceof List<?>) {
            for (Object item : (List<?>) original)
                if (item instanceof PackageInfo && ((PackageInfo) item).applicationInfo != null
                        && ((PackageInfo) item).applicationInfo.uid == param.getUid())
                    result.put(((PackageInfo) item).packageName, (PackageInfo) item);
        }
        for (String packageName : packagesFor(param))
            if (!result.containsKey(packageName))
                result.put(packageName, packageInfo(param, packageName));
        return new ArrayList<>(result.values());
    }

    public static ApplicationInfo findApplication(XParam param, String packageName) {
        return isVirtual(param, packageName) ? applicationInfo(param, packageName) : null;
    }

    public static PackageInfo findPackage(XParam param, String packageName) {
        return isVirtual(param, packageName) ? packageInfo(param, packageName) : null;
    }

    public static Integer findUid(XParam param, String packageName) {
        return isVirtual(param, packageName) ? stableUid(packageName) : null;
    }

    private static boolean isVirtual(XParam param, String packageName) {
        if (packageName == null) return false;
        for (String candidate : packagesFor(param))
            if (candidate.equals(packageName)) return true;
        return false;
    }

    private static List<String> packagesFor(XParam param) {
        ArrayList<String> result = new ArrayList<>(Arrays.asList(COMMON));
        // Play services and standard Google applications are present on all global profiles.
        result.addAll(Arrays.asList(GOOGLE));
        String family = family(param.getSetting("device.brand", ""),
                param.getSetting("device.manufacturer", ""));
        String[] oem = OEM.get(family);
        if (oem != null && !"google".equals(family)) result.addAll(Arrays.asList(oem));
        return result;
    }

    private static String family(String brand, String manufacturer) {
        String value = (brand + " " + manufacturer).toLowerCase(Locale.US);
        if (value.contains("redmagic") || value.contains("nubia")) return "nubia";
        if (value.contains("poco") || value.contains("xiaomi") || value.contains("redmi")) return "xiaomi";
        if (value.contains("iqoo") || value.contains("vivo")) return "vivo";
        for (String family : OEM.keySet())
            if (value.contains(family)) return family;
        return "google";
    }

    private static ApplicationInfo applicationInfo(XParam param, String packageName) {
        ApplicationInfo info = new ApplicationInfo();
        info.packageName = packageName;
        info.name = packageName;
        info.processName = packageName;
        info.uid = stableUid(packageName);
        info.enabled = true;
        info.flags = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_INSTALLED;
        info.targetSdkVersion = selectedSdk(param);
        info.sourceDir = "/system/priv-app/" + pathName(packageName) + "/" + pathName(packageName) + ".apk";
        info.publicSourceDir = info.sourceDir;
        info.dataDir = "/data/user/0/" + packageName;
        info.nonLocalizedLabel = label(packageName);
        return info;
    }

    private static PackageInfo packageInfo(XParam param, String packageName) {
        PackageInfo info = new PackageInfo();
        info.packageName = packageName;
        info.applicationInfo = applicationInfo(param, packageName);
        info.versionName = "1.0." + Math.abs(packageName.hashCode() % 1000);
        info.versionCode = 1 + Math.abs(packageName.hashCode() % 100000);
        info.firstInstallTime = 1704067200000L;
        info.lastUpdateTime = 1735689600000L;
        return info;
    }

    private static int stableUid(String packageName) {
        return 10000 + Math.abs(packageName.hashCode() % 40000);
    }

    private static int selectedSdk(XParam param) {
        String value = param.getSetting("android.build.version.sdk");
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException ignored) { }
        }
        return Build.VERSION.SDK_INT;
    }

    private static String pathName(String packageName) {
        int dot = packageName.lastIndexOf('.');
        String part = dot < 0 ? packageName : packageName.substring(dot + 1);
        if (part.isEmpty()) return "SystemApp";
        return Character.toUpperCase(part.charAt(0)) + part.substring(1);
    }

    private static String label(String packageName) {
        String value = pathName(packageName).replace('_', ' ');
        return value.isEmpty() ? packageName : value;
    }
}
