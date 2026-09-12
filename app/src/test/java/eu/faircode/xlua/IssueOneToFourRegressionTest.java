package eu.faircode.xlua;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Guards the fixes selected from Mobilelegends74/XPL-EX issue #1. */
public class IssueOneToFourRegressionTest {
    @Test
    public void appListUidLookupIsFilteredWithoutReintroducingBatterySpoof() throws Exception {
        String appHooks = asset("unique/appliction/list/hooks.json");
        String batteryHooks = asset("battery/hooks.json");
        String catalog = source("src/main/java/eu/faircode/xlua/x/hook/interceptors/apps/VirtualAppCatalog.java");

        assertTrue(appHooks.contains("PackageManager.getPackagesForUid"));
        assertTrue(appHooks.contains("@pm_packages_for_uid"));
        assertTrue(catalog.contains("packagesForUid(XParam param, int uid, Object original)"));
        assertFalse(appHooks.contains("spoof_battery_intent_createfromparcel"));
        assertFalse(batteryHooks.contains("spoof_battery_intent_createfromparcel"));
        assertTrue(batteryHooks.contains("@spoof_battery_intent_parcel_one"));
    }

    @Test
    public void highImpactHooksUseDedicatedOptionalCollections() throws Exception {
        String appList = asset("unique/appliction/list/hooks.json");
        assertTrue(appList.contains("\"collection\": \"Applist\""));
        assertFalse(appList.contains("\"collection\": \"PrivacyEx\""));

        String appTimes = asset("unique/appliction/hooks.json");
        assertTrue(pair(appTimes, "Spoof", "Apps.Spoof.TimeStamps"));

        String features = asset("features/hooks.json");
        assertTrue(pair(features, "Advanced", "Spoof.Features"));

        String storage = asset("unique/storage/hooks.json");
        assertTrue(pair(storage, "Advanced", "Storage.Isolate"));
        assertTrue(pair(storage, "Spoof", "Storage.Spoof.Size"));

        String[] hardwareFiles = {"cpu", "gpu", "camera", "location", "memory"};
        for (String directory : hardwareFiles) {
            String hooks = asset("unique/hardware/" + directory + "/hooks.json");
            assertTrue(hooks.contains("\"collection\": \"Hardware\""));
            assertFalse(hooks.contains("\"collection\": \"PrivacyEx\""));
        }
    }

    @Test
    public void directLocalAddressApisAreCovered() throws Exception {
        String hooks = asset("unique/network/parcels/hooks.json");
        assertTrue(hooks.contains("WifiManager.getConnectionInfo/Spoof"));
        assertTrue(hooks.contains("WifiManager.getDhcpInfo/Spoof"));
        assertTrue(hooks.contains("WifiInfo.getIpAddress/Spoof"));
        assertTrue(hooks.contains("ConnectivityManager.getLinkProperties/Spoof"));
        assertTrue(hooks.contains("NetworkInterface.getInetAddresses/Spoof"));
        assertTrue(hooks.contains("InterfaceAddress.getAddress/Spoof"));

        String network = source("src/main/java/eu/faircode/xlua/x/hook/interceptors/network/NetworkInterfaceInterceptor.java");
        assertTrue(network.contains("interceptInetAddresses(XParam param)"));
        assertTrue(network.contains("interceptAddressResult(XParam param, String interfaceName)"));
    }

    @Test
    public void batteryCyclesHaveImmediateDefaultAndBoundedRandomizers() throws Exception {
        String battery = source("src/main/java/eu/faircode/xlua/x/hook/interceptors/battery/BatteryInterceptor.java");
        String cycles = source("src/main/java/eu/faircode/xlua/x/xlua/settings/random/randomizers/battery/RandomChargingCycles.java");
        String boots = source("src/main/java/eu/faircode/xlua/x/xlua/settings/random/randomizers/settings/RandomBootCount.java");
        String defaults = asset("settingdefaults.json");

        assertTrue(battery.contains("DEFAULT_CHARGING_CYCLES = 120"));
        assertTrue(battery.contains("configuredCycleCount == null ? DEFAULT_CHARGING_CYCLES"));
        assertTrue(cycles.contains("nextInt(100, 501)"));
        assertTrue(boots.contains("nextInt(100, 501)"));
        assertTrue(defaults.contains("\"name\": \"battery.charging.cycles\""));
        assertTrue(defaults.contains("\"defaultValue\": \"120\""));
    }

    private static boolean pair(String contents, String collection, String group) {
        return contents.contains("\"collection\": \"" + collection + "\",\n    \"group\": \"" + group + "\"");
    }

    private static String asset(String relativePath) throws Exception {
        return source("src/main/assets/" + relativePath);
    }

    private static String source(String relativePath) throws Exception {
        File file = new File(relativePath);
        if (!file.exists())
            file = new File("app/" + relativePath);
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
