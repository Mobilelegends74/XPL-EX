package eu.faircode.xlua.x.xlua.settings.random.profile;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device.RandomDeviceProfile;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.battery.RandomBatteryProfile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DeviceProfileCatalogTest {
    private static List<DeviceProfile> profiles;

    @BeforeClass
    public static void loadCatalog() throws Exception {
        File catalog = new File("src/main/assets/" + DeviceProfileCatalog.ASSET_NAME);
        if (!catalog.isFile())
            catalog = new File("app/src/main/assets/" + DeviceProfileCatalog.ASSET_NAME);
        assertTrue("Device profile catalog is missing", catalog.isFile());
        try (FileInputStream input = new FileInputStream(catalog)) {
            profiles = DeviceProfileCatalog.parse(input);
        }
    }

    @Test
    public void catalogContainsOnlyValidProfiles() {
        assertEquals(58, profiles.size());
        assertTrue(DeviceProfileValidator.validate(profiles).isEmpty());
    }

    @Test
    public void requestedFlagshipBrandsContainFiveRealModelsEach() {
        Map<String, Integer> counts = new HashMap<>();
        for (DeviceProfile profile : profiles) {
            String brand = DeviceProfileCatalog.selectionBrand(profile);
            Integer count = counts.get(brand);
            counts.put(brand, count == null ? 1 : count + 1);
        }

        assertEquals(Integer.valueOf(5), counts.get("nubia"));
        assertEquals(Integer.valueOf(5), counts.get("samsung"));
        assertEquals(Integer.valueOf(5), counts.get("oneplus"));
        assertEquals(Integer.valueOf(5), counts.get("xiaomi"));
        assertEquals(Integer.valueOf(5), counts.get("poco"));
        assertEquals(Integer.valueOf(5), counts.get("asus"));
        assertEquals(Integer.valueOf(5), counts.get("meizu"));
        assertEquals(Integer.valueOf(5), counts.get("vivo"));
        assertEquals(Integer.valueOf(5), counts.get("lenovo"));
        assertEquals(Integer.valueOf(5), counts.get("motorola"));
    }

    @Test
    public void requestedProfilesUseFlagshipClassQualcommPlatforms() {
        List<String> supportedPlatforms = Arrays.asList(
                "SM8550", "SM8550-AB", "SM8550-AC", "SM8650", "SM8650-AB",
                "SM8735", "SM8750-AB", "SM8845", "SM8850-AC",
                "SM8150", "SM8250-AB", "SM8350", "SM8450", "SM8475");

        for (DeviceProfile profile : profiles) {
            String brand = DeviceProfileCatalog.selectionBrand(profile);
            if ("google".equals(brand))
                continue;
            assertEquals(profile.id, "Qualcomm", profile.socManufacturer);
            assertTrue(profile.id + ": " + profile.socModel,
                    supportedPlatforms.contains(profile.socModel));
        }
    }

    @Test
    public void randomSelectionIsBalancedByBrandWithoutConsecutiveRepeats() {
        DeviceProfileCatalog.resetSelectionForTests();
        Map<String, Integer> counts = new HashMap<>();
        String previous = null;

        for (int i = 0; i < 66; i++) {
            DeviceProfileSelection selection = DeviceProfileCatalog.selectBalanced(profiles);
            String brand = DeviceProfileCatalog.selectionBrand(selection.device);
            assertNotEquals("Brand repeated at selection " + i, previous, brand);
            Integer count = counts.get(brand);
            counts.put(brand, count == null ? 1 : count + 1);
            previous = brand;
        }

        assertEquals(11, counts.size());
        for (Integer count : counts.values())
            assertEquals(Integer.valueOf(6), count);
    }

    @Test
    public void androidReleaseAlwaysMatchesSdkAndLaunchVersion() {
        for (DeviceProfile profile : profiles) {
            assertTrue(AndroidRelease.matches(profile.launchRelease, profile.launchApiLevel));
            assertFalse(profile.builds.isEmpty());
            for (DeviceBuildProfile build : profile.builds) {
                assertTrue(profile.id + "/" + build.release,
                        AndroidRelease.matches(build.release, build.apiLevel));
                assertTrue(profile.id + "/" + build.release,
                        build.apiLevel >= profile.launchApiLevel);
            }
        }
    }

    @Test
    public void xiaomiAndNubiaIdentitiesCannotBeMixed() {
        DeviceProfile xiaomi = find("xiaomi_14_cn");
        DeviceProfile nubia = find("nubia_redmagic_9_pro");

        assertEquals("Xiaomi", xiaomi.manufacturer);
        assertEquals("23127PN0CC", xiaomi.model);
        assertEquals("houji", xiaomi.device);
        assertNotEquals(nubia.manufacturer, xiaomi.manufacturer);
        assertNotEquals(nubia.model, xiaomi.model);
        assertFalse(xiaomi.marketingName.toLowerCase().contains("redmagic"));
    }

    @Test
    public void samsungNeverUsesXiaomiProductOrDevice() {
        DeviceProfile samsung = find("samsung_galaxy_s24_ultra_global");
        DeviceProfile xiaomi = find("xiaomi_14_cn");

        assertEquals("samsung", samsung.brand);
        assertNotEquals(xiaomi.product, samsung.product);
        assertNotEquals(xiaomi.device, samsung.device);
        assertEquals("e3qxxx", samsung.product);
        assertEquals("e3q", samsung.device);
    }

    @Test
    public void oneSelectionPopulatesOneCoherentProfile() {
        for (DeviceProfile profile : profiles) {
            for (DeviceBuildProfile build : profile.builds) {
                DeviceProfileSelection selection = new DeviceProfileSelection(profile, build);
                Map<String, String> values = selection.asSettingMap();

                assertEquals(RandomDeviceProfile.profileSettingCount(), values.size());
                for (String settingName : values.keySet())
                    assertTrue("Unregistered profile setting: " + settingName,
                            RandomDeviceProfile.isProfileSetting(settingName));

                assertEquals(profile.manufacturer, values.get(RandomizersCache.SETTING_DEVICE_MANUFACTURER));
                assertEquals(profile.brand, values.get(RandomizersCache.SETTING_DEVICE_BRAND));
                assertEquals(profile.model, values.get(RandomizersCache.SETTING_DEVICE_MODEL));
                String displayId = values.get(RandomizersCache.SETTING_ANDROID_BUILD_DISPLAY_ID);
                assertNotNull(profile.id, displayId);
                assertFalse(profile.id, displayId.contains("REDMAGICOS10.07.MR_EU1"));
                assertTrue(profile.id, displayId.equals(build.buildId)
                        || displayId.contains(build.incremental));
                assertEquals(profile.device, values.get(RandomizersCache.SETTING_DEVICE_CODENAME));
                assertEquals(profile.product, values.get(RandomizersCache.SETTING_ANDROID_BUILD_CODENAME));
                assertEquals(profile.hardware, values.get(RandomizersCache.SETTING_SOC_BOARD_MANUFACTURER_ID));
                assertEquals(profile.board, values.get(RandomizersCache.SETTING_SOC_BOARD_CONFIG_CODE_NAME));
                assertEquals(profile.socModel, values.get(RandomizersCache.SETTING_SOC_BOARD_MODEL));
                String cpuInfo = values.get(RandomizersCache.SETTING_SOC_CPU_INFO_DUMP);
                assertNotNull(cpuInfo);
                assertTrue(cpuInfo.contains("Hardware\t:"));
                assertTrue(cpuInfo.contains(profile.socModel));
                assertTrue(cpuInfo.contains(CpuInfoProfile.chipsetName(profile.socModel)));
                assertEquals(profile.cpuCount, count(cpuInfo, "processor\t:"));
                assertEquals(build.release, values.get(RandomizersCache.SETTING_ANDROID_BUILD_VERSION));
                assertEquals(String.valueOf(build.apiLevel), values.get(RandomizersCache.SETTING_ANDROID_BUILD_VERSION_SDK));
                assertEquals(build.fingerprint, values.get(RandomizersCache.SETTING_ANDROID_BUILD_FINGERPRINT));
                long buildEpoch = Long.parseLong(values.get(RandomizersCache.SETTING_ANDROID_BUILD_DATE_EPOC));
                assertTrue(profile.id, buildEpoch > 1546300800L);
                assertTrue(profile.id, buildEpoch * 1000L < System.currentTimeMillis());
                assertFalse(profile.id, values.get(RandomizersCache.SETTING_ANDROID_BUILD_DATE).contains("1970"));
                assertTrue(profile.id, values.get(RandomizersCache.SETTING_ANDROID_BUILD_PATCH).matches("20\\d{2}-\\d{2}-01"));
                assertEquals("Linux", values.get(RandomizersCache.SETTING_ANDROID_KERNEL_SYS_NAME));
                assertEquals("localhost", values.get(RandomizersCache.SETTING_ANDROID_KERNEL_NODE_NAME));
                assertTrue(profile.id, values.get(RandomizersCache.SETTING_ANDROID_KERNEL_RELEASE)
                        .matches("(4\\.(14|19)|5\\.(10|15)|6\\.(1|6|12))\\..*"));
                assertTrue(profile.id, values.get(RandomizersCache.SETTING_ANDROID_KERNEL_VERSION)
                        .startsWith("#1 SMP PREEMPT_DYNAMIC "));
                assertEquals(build.buildType, values.get(RandomizersCache.SETTING_ANDROID_ETC_BUILD_ROM_VARIANT));
                assertEquals("REL", values.get(RandomizersCache.SETTING_ANDROID_ETC_BUILD_ROM_VERSION_CODENAME));
                assertEquals(String.valueOf(profile.characteristics.displayWidthPx),
                        values.get(RandomizersCache.SETTING_DISPLAY_WIDTH));
                assertEquals(String.valueOf(profile.characteristics.displayHeightPx),
                        values.get(RandomizersCache.SETTING_DISPLAY_HEIGHT));
                assertEquals(String.valueOf(profile.characteristics.displayRefreshRateHz),
                        values.get(RandomizersCache.SETTING_DISPLAY_REFRESH_RATE_HZ));
                assertEquals(String.valueOf(profile.characteristics.batteryCapacityMah),
                        values.get(RandomizersCache.SETTING_BATTERY_CAPACITY_MAH));
                assertEquals(String.valueOf(profile.characteristics.ramGb),
                        values.get(RandomizersCache.SETTING_HARDWARE_MEMORY_TOTAL));
                assertTrue(values.get(RandomizersCache.SETTING_SOC_GPU_OPEN_GLES_RENDERER)
                        .startsWith("Adreno")
                        || values.get(RandomizersCache.SETTING_SOC_GPU_OPEN_GLES_RENDERER)
                        .startsWith("Mali")
                        || values.get(RandomizersCache.SETTING_SOC_GPU_OPEN_GLES_RENDERER)
                        .startsWith("Immortalis"));
                assertEquals("OpenGL ES 3.2",
                        values.get(RandomizersCache.SETTING_SOC_GPU_OPEN_GLES_VERSION));
                assertFalse(values.get(RandomizersCache.SETTING_HARDWARE_CAMERA_APP).isEmpty());
                assertTrue(build.fingerprint.contains("/" + profile.product + "/" + profile.device + ":"));

                int percent = Integer.parseInt(values.get(RandomizersCache.SETTING_BATTERY_PERCENT));
                int voltage = Integer.parseInt(values.get(RandomizersCache.SETTING_BATTERY_VOLTAGE_MV));
                int totalRam = Integer.parseInt(values.get(RandomizersCache.SETTING_HARDWARE_MEMORY_TOTAL));
                int availableRam = Integer.parseInt(values.get(RandomizersCache.SETTING_HARDWARE_MEMORY_AVAILABLE));
                boolean charging = Boolean.parseBoolean(values.get(RandomizersCache.SETTING_BATTERY_IS_CHARGING));
                boolean plugged = Boolean.parseBoolean(values.get(RandomizersCache.SETTING_BATTERY_IS_PLUGGED));
                assertTrue(percent >= 25 && percent < 95);
                assertTrue(voltage >= 3300 && voltage <= 4350);
                assertEquals(charging, plugged);
                assertTrue(availableRam > 0 && availableRam < totalRam);
            }
        }
    }

    @Test
    public void newRequestedBrandsUseRealModelCharacteristics() {
        for (String id : Arrays.asList(
                "asus_zenfone_10", "meizu_21_pro", "vivo_x200_ultra",
                "lenovo_legion_y90", "motorola_signature")) {
            DeviceProfile profile = find(id);
            assertNotNull(profile.characteristics);
            assertTrue(profile.characteristics.displayWidthPx >= 1080);
            assertTrue(profile.characteristics.displayHeightPx > profile.characteristics.displayWidthPx);
            assertTrue(profile.characteristics.batteryCapacityMah >= 4300);
            assertTrue(profile.characteristics.ramGb >= 8);
        }
    }

    @Test
    public void batteryRandomizationUsesOneCoherentProfile() {
        assertEquals(RandomBatteryProfile.class, RandomizersCache.SETTING_BATTERY_PERCENT_TYPE);
        assertEquals(RandomBatteryProfile.class, RandomizersCache.SETTING_BATTERY_STATUS_TYPE);
        assertEquals(RandomBatteryProfile.class, RandomizersCache.SETTING_BATTERY_IS_CHARGING_TYPE);
        assertEquals(RandomBatteryProfile.class, RandomizersCache.SETTING_BATTERY_CAPACITY_MAH_TYPE);
    }

    @Test
    public void validatorRejectsDuplicateProfilesAndFingerprints() {
        DeviceProfile first = profiles.get(0);
        List<String> errors = DeviceProfileValidator.validate(Arrays.asList(first, first));

        assertFalse(errors.isEmpty());
        assertTrue(contains(errors, "duplicate id"));
        assertTrue(contains(errors, "duplicate device identity"));
        assertTrue(contains(errors, "duplicate fingerprint"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void selectionRejectsBuildFromAnotherDevice() {
        DeviceProfile xiaomi = find("xiaomi_14_cn");
        DeviceProfile nubia = find("nubia_redmagic_9_pro");
        new DeviceProfileSelection(xiaomi, nubia.builds.get(0));
    }

    private static DeviceProfile find(String id) {
        for (DeviceProfile profile : profiles)
            if (profile.id.equals(id))
                return profile;
        assertNotNull("Missing profile: " + id, null);
        return null;
    }

    private static boolean contains(List<String> values, String expected) {
        for (String value : values)
            if (value.contains(expected))
                return true;
        return false;
    }

    private static int count(String value, String token) {
        int count = 0;
        int offset = 0;
        while ((offset = value.indexOf(token, offset)) >= 0) {
            count++;
            offset += token.length();
        }
        return count;
    }
}
