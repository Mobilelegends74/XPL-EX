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
import java.util.Random;

import eu.faircode.xlua.XParam;

/** A coherent system/OEM inventory plus a stable random user-app inventory. */
public final class VirtualAppCatalog {
    private static final String[] ANDROID_CORE = {
            "android", "com.android.systemui", "com.android.settings",
            "com.android.permissioncontroller", "com.android.packageinstaller",
            "com.android.networkstack", "com.android.captiveportallogin",
            "com.android.providers.settings", "com.android.providers.media",
            "com.android.providers.downloads", "com.android.providers.contacts",
            "com.android.providers.calendar", "com.android.documentsui",
            "com.android.externalstorage", "com.android.bluetooth",
            "com.android.nfc", "com.android.phone", "com.android.shell"
    };

    private static final String[] GOOGLE_CORE = {
            "com.google.android.gms", "com.android.vending", "com.google.android.gsf",
            "com.google.android.configupdater", "com.google.android.modulemetadata",
            "com.google.android.webview", "com.google.android.ext.shared",
            "com.google.android.ext.services", "com.google.android.permissioncontroller",
            "com.google.android.networkstack", "com.google.android.captiveportallogin"
    };

    private static final Map<String, String[]> OEM_SYSTEM;
    private static final Map<String, String[]> MODEL_SYSTEM;

    private static final String[][] USER_APPS = {
            {"org.telegram.messenger", "Telegram"}, {"com.whatsapp", "WhatsApp"},
            {"com.instagram.android", "Instagram"}, {"com.facebook.katana", "Facebook"},
            {"com.facebook.orca", "Messenger"}, {"com.zhiliaoapp.musically", "TikTok"},
            {"com.twitter.android", "X"}, {"com.reddit.frontpage", "Reddit"},
            {"com.discord", "Discord"}, {"com.snapchat.android", "Snapchat"},
            {"com.spotify.music", "Spotify"}, {"com.netflix.mediaclient", "Netflix"},
            {"com.amazon.mShop.android.shopping", "Amazon Shopping"},
            {"com.ebay.mobile", "eBay"}, {"com.alibaba.aliexpresshd", "AliExpress"},
            {"com.booking", "Booking.com"}, {"com.ubercab", "Uber"},
            {"com.airbnb.android", "Airbnb"}, {"com.microsoft.office.outlook", "Outlook"},
            {"com.microsoft.teams", "Microsoft Teams"},
            {"com.microsoft.office.officehubrow", "Microsoft 365"},
            {"com.dropbox.android", "Dropbox"}, {"com.google.android.apps.docs", "Google Drive"},
            {"com.google.android.apps.translate", "Google Translate"},
            {"com.google.android.apps.authenticator2", "Authenticator"},
            {"com.valvesoftware.android.steam.community", "Steam"},
            {"com.supercell.clashofclans", "Clash of Clans"},
            {"com.supercell.brawlstars", "Brawl Stars"},
            {"com.miHoYo.GenshinImpact", "Genshin Impact"},
            {"com.tencent.ig", "PUBG MOBILE"},
            {"com.activision.callofduty.shooter", "Call of Duty"},
            {"com.roblox.client", "Roblox"}, {"com.mojang.minecraftpe", "Minecraft"},
            {"com.king.candycrushsaga", "Candy Crush Saga"},
            {"com.adobe.reader", "Adobe Acrobat"}, {"com.canva.editor", "Canva"},
            {"com.shazam.android", "Shazam"}, {"com.duolingo", "Duolingo"},
            {"com.anydesk.anydeskandroid", "AnyDesk"},
            {"com.speedsoftware.rootexplorer", "Root Explorer"}
    };

    static {
        Map<String, String[]> oem = new LinkedHashMap<>();
        oem.put("google", new String[]{
                "com.google.android.apps.nexuslauncher", "com.google.android.dialer",
                "com.google.android.contacts", "com.google.android.apps.messaging",
                "com.google.android.GoogleCamera", "com.google.android.apps.photos",
                "com.google.android.apps.nbu.files", "com.google.android.deskclock",
                "com.google.android.calculator", "com.google.android.calendar",
                "com.google.android.googlequicksearchbox", "com.google.android.apps.tips",
                "com.google.android.apps.wellbeing", "com.google.android.safetycenter.resources",
                "com.google.android.apps.maps", "com.google.android.youtube",
                "com.android.chrome", "com.google.android.gm"
        });
        oem.put("samsung", new String[]{
                "com.sec.android.app.launcher", "com.samsung.android.dialer",
                "com.samsung.android.incallui", "com.samsung.android.messaging",
                "com.samsung.android.contacts", "com.sec.android.app.camera",
                "com.sec.android.gallery3d", "com.sec.android.app.clockpackage",
                "com.sec.android.app.myfiles", "com.samsung.android.calendar",
                "com.sec.android.app.popupcalculator", "com.samsung.android.lool",
                "com.samsung.android.app.notes", "com.samsung.android.bixby.agent",
                "com.samsung.android.app.spage", "com.sec.android.app.samsungapps",
                "com.samsung.android.game.gamehome", "com.samsung.android.game.gametools",
                "com.samsung.android.oneconnect", "com.samsung.android.knox.containercore"
        });
        oem.put("xiaomi", new String[]{
                "com.miui.home", "com.android.contacts", "com.android.mms",
                "com.android.camera", "com.miui.gallery", "com.miui.securitycenter",
                "com.miui.cleanmaster", "com.miui.packageinstaller", "com.android.updater",
                "com.mi.android.globalFileexplorer", "com.miui.calculator", "com.miui.notes",
                "com.miui.compass", "com.miui.weather2", "com.miui.screenrecorder",
                "com.xiaomi.scanner", "com.miui.player", "com.miui.videoplayer",
                "com.xiaomi.joyose", "com.miui.powerkeeper", "com.xiaomi.gamecenter"
        });
        oem.put("oneplus", new String[]{
                "com.android.launcher", "com.android.dialer", "com.android.contacts",
                "com.android.mms", "com.oplus.camera", "com.coloros.gallery3d",
                "com.coloros.filemanager", "com.coloros.calculator", "com.coloros.alarmclock",
                "com.coloros.weather2", "com.coloros.phonemanager", "com.oplus.safecenter",
                "com.oplus.games", "com.oneplus.membership", "com.oneplus.account"
        });
        oem.put("asus", new String[]{
                "com.asus.launcher", "com.asus.dialer", "com.asus.contacts",
                "com.asus.camera", "com.asus.gallery", "com.asus.filemanager",
                "com.asus.calculator", "com.asus.deskclock", "com.asus.mobilemanager",
                "com.asus.weather", "com.asus.soundrecorder"
        });
        oem.put("lenovo", new String[]{
                "com.zui.launcher", "com.android.dialer", "com.android.contacts",
                "com.zui.camera", "com.zui.gallery", "com.zui.filemanager",
                "com.zui.calculator", "com.zui.deskclock", "com.zui.safecenter",
                "com.zui.weather", "com.zui.notes"
        });
        oem.put("motorola", new String[]{
                "com.motorola.launcher3", "com.google.android.dialer",
                "com.google.android.contacts", "com.google.android.apps.messaging",
                "com.motorola.camera3", "com.google.android.apps.photos",
                "com.google.android.apps.nbu.files", "com.motorola.actions",
                "com.motorola.moto", "com.motorola.help", "com.motorola.gamemode",
                "com.motorola.timeweatherwidget", "com.motorola.motosignature.app"
        });
        oem.put("meizu", new String[]{
                "com.meizu.flyme.launcher", "com.android.dialer", "com.android.contacts",
                "com.android.mms", "com.meizu.media.camera", "com.meizu.media.gallery",
                "com.meizu.filemanager", "com.meizu.safe", "com.meizu.flyme.update",
                "com.meizu.flyme.weather", "com.meizu.notepaper", "com.meizu.flyme.gamecenter"
        });
        oem.put("vivo", new String[]{
                "com.bbk.launcher2", "com.android.dialer", "com.android.contacts",
                "com.android.mms", "com.vivo.camera", "com.vivo.gallery",
                "com.vivo.filemanager", "com.iqoo.secure", "com.bbk.updater",
                "com.vivo.weather", "com.android.bbkmusic", "com.android.VideoPlayer",
                "com.vivo.notes", "com.vivo.game", "com.vivo.gamewatch"
        });
        oem.put("nubia", new String[]{
                "cn.nubia.launcher", "cn.nubia.contacts", "cn.nubia.mms",
                "cn.nubia.camera", "cn.nubia.gallery3d", "cn.nubia.filebrowser",
                "cn.nubia.calculator2", "cn.nubia.deskclock.preset", "cn.nubia.weather",
                "cn.nubia.security2", "cn.nubia.updatesystem", "cn.nubia.neoshare",
                "cn.nubia.gamecenter", "cn.nubia.gamelauncher", "cn.nubia.gameassist"
        });
        OEM_SYSTEM = Collections.unmodifiableMap(oem);

        Map<String, String[]> models = new LinkedHashMap<>();
        models.put("pixel", new String[]{"com.google.android.pixel.setupwizard", "com.google.android.apps.restore"});
        models.put("sm-s918", new String[]{"com.samsung.android.service.aircommand", "com.samsung.android.app.notes.addons"});
        models.put("sm-s928", new String[]{"com.samsung.android.service.aircommand", "com.samsung.android.visionintelligence"});
        models.put("sm-f946", new String[]{"com.samsung.android.appcontinuity", "com.samsung.android.honeyboard"});
        models.put("asus_ai22", new String[]{"com.asus.gamecenter", "com.asus.rog"});
        models.put("asus_ai24", new String[]{"com.asus.gamecenter", "com.asus.rog"});
        models.put("asus_ai25", new String[]{"com.asus.gamecenter", "com.asus.rog"});
        models.put("nx7", new String[]{"cn.nubia.gamehelper", "cn.nubia.redmagiclight"});
        models.put("nx8", new String[]{"cn.nubia.gamehelper", "cn.nubia.redmagiclight"});
        models.put("i2220", new String[]{"com.vivo.gamecube", "com.iqoo.gameturbo"});
        models.put("i2401", new String[]{"com.vivo.gamecube", "com.iqoo.gameturbo"});
        models.put("lenovo l7", new String[]{"com.lenovo.gameassistant", "com.zui.game.service"});
        models.put("xt2551", new String[]{"com.motorola.readyfor", "com.motorola.flexui"});
        MODEL_SYSTEM = Collections.unmodifiableMap(models);
    }

    private VirtualAppCatalog() { }

    public static List<ApplicationInfo> mergeApplications(XParam param, Object original) {
        LinkedHashMap<String, ApplicationInfo> result = new LinkedHashMap<>();
        if (original instanceof List<?>) {
            for (Object item : (List<?>) original)
                if (item instanceof ApplicationInfo && ((ApplicationInfo) item).uid == param.getUid())
                    result.put(((ApplicationInfo) item).packageName, (ApplicationInfo) item);
        }
        for (CatalogEntry entry : catalogFor(param).values())
            if (!result.containsKey(entry.packageName))
                result.put(entry.packageName, applicationInfo(param, entry));
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
        for (CatalogEntry entry : catalogFor(param).values())
            if (!result.containsKey(entry.packageName))
                result.put(entry.packageName, packageInfo(param, entry));
        return new ArrayList<>(result.values());
    }

    public static ApplicationInfo findApplication(XParam param, String packageName) {
        CatalogEntry entry = catalogFor(param).get(packageName);
        return entry == null ? null : applicationInfo(param, entry);
    }

    public static PackageInfo findPackage(XParam param, String packageName) {
        CatalogEntry entry = catalogFor(param).get(packageName);
        return entry == null ? null : packageInfo(param, entry);
    }

    public static Integer findUid(XParam param, String packageName) {
        return catalogFor(param).containsKey(packageName) ? stableUid(packageName) : null;
    }

    private static LinkedHashMap<String, CatalogEntry> catalogFor(XParam param) {
        LinkedHashMap<String, CatalogEntry> result = new LinkedHashMap<>();
        addSystem(result, ANDROID_CORE);
        addSystem(result, GOOGLE_CORE);

        String identity = identity(param);
        String family = familyForIdentity(identity);
        String[] oem = OEM_SYSTEM.get(family);
        if (oem != null) addSystem(result, oem);
        for (Map.Entry<String, String[]> model : MODEL_SYSTEM.entrySet())
            if (identity.contains(model.getKey())) addSystem(result, model.getValue());

        List<String[]> userApps = new ArrayList<>(Arrays.asList(USER_APPS));
        long seed = seed(param, identity);
        Collections.shuffle(userApps, new Random(seed));
        int count = 10 + (int) Math.floorMod(seed, 9);
        for (int i = 0; i < count; i++) {
            String[] app = userApps.get(i);
            result.put(app[0], new CatalogEntry(app[0], app[1], false));
        }
        return result;
    }

    private static void addSystem(Map<String, CatalogEntry> result, String[] packages) {
        for (String packageName : packages)
            result.put(packageName, new CatalogEntry(packageName, label(packageName), true));
    }

    private static String identity(XParam param) {
        String value = join(param.getSetting("device.brand"), param.getSetting("device.manufacturer"),
                param.getSetting("device.model"), param.getSetting("device.nick.name"));
        if (value.trim().isEmpty())
            value = join(Build.BRAND, Build.MANUFACTURER, Build.MODEL, Build.DEVICE);
        return value.toLowerCase(Locale.US);
    }

    private static String join(String... values) {
        StringBuilder result = new StringBuilder();
        for (String value : values)
            if (value != null && !value.trim().isEmpty()) result.append(' ').append(value.trim());
        return result.toString();
    }

    static String familyForIdentity(String identity) {
        if (identity.contains("redmagic") || identity.contains("nubia")) return "nubia";
        if (identity.contains("poco") || identity.contains("xiaomi") || identity.contains("redmi")) return "xiaomi";
        if (identity.contains("samsung") || identity.contains("sm-s") || identity.contains("sm-f")) return "samsung";
        if (identity.contains("oneplus") || identity.contains("cph")) return "oneplus";
        if (identity.contains("asus")) return "asus";
        if (identity.contains("lenovo")) return "lenovo";
        if (identity.contains("motorola") || identity.contains("xt2")) return "motorola";
        if (identity.contains("meizu")) return "meizu";
        if (identity.contains("iqoo") || identity.contains("vivo")) return "vivo";
        if (identity.contains("nx7") || identity.contains("nx8")) return "nubia";
        if (identity.matches(".* 2[0-9]{6,}[a-z].*")) return "xiaomi";
        if (identity.contains(" i2220") || identity.contains(" i2401")) return "vivo";
        if (identity.matches(".* m[0-9]{3}q.*")) return "meizu";
        return "google";
    }

    private static long seed(XParam param, String identity) {
        String fingerprint = param.getSetting("android.build.fingerprint");
        String source = identity + '|' + (fingerprint == null ? "" : fingerprint);
        long result = 1125899906842597L;
        for (int i = 0; i < source.length(); i++) result = 31L * result + source.charAt(i);
        return result;
    }

    private static ApplicationInfo applicationInfo(XParam param, CatalogEntry entry) {
        ApplicationInfo info = new ApplicationInfo();
        info.packageName = entry.packageName;
        info.name = entry.packageName;
        info.processName = entry.packageName;
        info.uid = stableUid(entry.packageName);
        info.enabled = true;
        info.flags = ApplicationInfo.FLAG_INSTALLED | (entry.system ? ApplicationInfo.FLAG_SYSTEM : 0);
        info.targetSdkVersion = selectedSdk(param);
        String path = pathName(entry.packageName);
        info.sourceDir = entry.system
                ? "/system/priv-app/" + path + "/" + path + ".apk"
                : "/data/app/~~" + Integer.toHexString(entry.packageName.hashCode()) + "/" + entry.packageName + "-1/base.apk";
        info.publicSourceDir = info.sourceDir;
        info.dataDir = "/data/user/0/" + entry.packageName;
        info.nonLocalizedLabel = entry.label;
        return info;
    }

    private static PackageInfo packageInfo(XParam param, CatalogEntry entry) {
        PackageInfo info = new PackageInfo();
        info.packageName = entry.packageName;
        info.applicationInfo = applicationInfo(param, entry);
        info.versionName = entry.system ? "1.0." : "10." + Math.abs(entry.packageName.hashCode() % 100) + ".";
        info.versionName += Math.abs(entry.packageName.hashCode() % 1000);
        info.versionCode = 1 + Math.abs(entry.packageName.hashCode() % 900000);
        info.firstInstallTime = 1704067200000L + Math.abs(entry.packageName.hashCode() % 20000000000L);
        info.lastUpdateTime = info.firstInstallTime + 86400000L * (30 + Math.abs(entry.packageName.hashCode() % 300));
        return info;
    }

    private static int stableUid(String packageName) {
        if ("android".equals(packageName) || "com.android.systemui".equals(packageName)) return 1000;
        return 10000 + Math.abs(packageName.hashCode() % 40000);
    }

    private static int selectedSdk(XParam param) {
        String value = param.getSetting("android.build.version.sdk");
        if (value != null) try { return Integer.parseInt(value.trim()); } catch (NumberFormatException ignored) { }
        return Build.VERSION.SDK_INT;
    }

    private static String pathName(String packageName) {
        int dot = packageName.lastIndexOf('.');
        String part = dot < 0 ? packageName : packageName.substring(dot + 1);
        if (part.isEmpty()) return "SystemApp";
        return Character.toUpperCase(part.charAt(0)) + part.substring(1);
    }

    private static String label(String packageName) {
        return pathName(packageName).replace('_', ' ');
    }

    private static final class CatalogEntry {
        final String packageName;
        final String label;
        final boolean system;

        CatalogEntry(String packageName, String label, boolean system) {
            this.packageName = packageName;
            this.label = label;
            this.system = system;
        }
    }
}
