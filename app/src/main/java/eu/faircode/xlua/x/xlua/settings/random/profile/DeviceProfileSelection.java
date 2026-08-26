package eu.faircode.xlua.x.xlua.settings.random.profile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public final class DeviceProfileSelection {
    public final DeviceProfile device;
    public final DeviceBuildProfile build;
    private final Map<String, String> settings;

    public DeviceProfileSelection(DeviceProfile device, DeviceBuildProfile build) {
        if (device == null || build == null)
            throw new IllegalArgumentException("Device and build are required");
        boolean supportedBuild = false;
        for (DeviceBuildProfile candidate : device.builds) {
            if (candidate.fingerprint.equals(build.fingerprint)) {
                supportedBuild = true;
                break;
            }
        }
        if (!supportedBuild)
            throw new IllegalArgumentException("Build does not belong to device profile " + device.id);

        this.device = device;
        this.build = build;

        Map<String, String> values = new LinkedHashMap<>();
        values.put(RandomizersCache.SETTING_DEVICE_BRAND, device.brand);
        values.put(RandomizersCache.SETTING_DEVICE_MANUFACTURER, device.manufacturer);
        values.put(RandomizersCache.SETTING_DEVICE_NICKNAME, device.device);
        values.put(RandomizersCache.SETTING_DEVICE_MODEL, device.model);
        values.put(RandomizersCache.SETTING_DEVICE_CODENAME, device.device);

        values.put(RandomizersCache.SETTING_ANDROID_BUILD_VERSION, build.release);
        values.put(RandomizersCache.SETTING_ANDROID_BUILD_VERSION_SDK, String.valueOf(build.apiLevel));
        values.put(RandomizersCache.SETTING_ANDROID_BUILD_VERSION_MIN_SDK, String.valueOf(device.launchApiLevel));
        values.put(RandomizersCache.SETTING_ANDROID_BUILD_TAGS, build.tags);
        values.put(RandomizersCache.SETTING_ANDROID_BUILD_INCREMENTAL, build.incremental);
        values.put(RandomizersCache.SETTING_ANDROID_BUILD_DESCRIPTION, build.description(device));
        values.put(RandomizersCache.SETTING_ANDROID_BUILD_ID, build.buildId);
        values.put(RandomizersCache.SETTING_ANDROID_BUILD_FLAVOR, build.flavor(device));
        values.put(RandomizersCache.SETTING_ANDROID_BUILD_CODENAME, device.product);
        values.put(RandomizersCache.SETTING_ANDROID_BUILD_FINGERPRINT, build.fingerprint);
        values.put(RandomizersCache.SETTING_ANDROID_ETC_BUILD_ROM_VERSION_CODENAME, "REL");
        values.put(RandomizersCache.SETTING_ANDROID_ETC_BUILD_ROM_VARIANT, build.buildType);

        values.put(RandomizersCache.SETTING_SOC_BOARD_MODEL, device.socModel);
        values.put(RandomizersCache.SETTING_SOC_BOARD_CONFIG_CODE_NAME, device.board);
        values.put(RandomizersCache.SETTING_SOC_BOARD_MANUFACTURER, device.socManufacturer);
        values.put(RandomizersCache.SETTING_SOC_BOARD_MANUFACTURER_ID, device.hardware);
        values.put(RandomizersCache.SETTING_SOC_CPU_PROCESSOR_COUNT, String.valueOf(device.cpuCount));
        values.put(RandomizersCache.SETTING_SOC_CPU_ARCHITECTURE, device.cpuArchitecture);
        values.put(RandomizersCache.SETTING_SOC_CPU_ABI, device.abi);
        values.put(RandomizersCache.SETTING_SOC_CPU_ABI_LIST, device.abiList);
        values.put(RandomizersCache.SETTING_SOC_CPU_ABI_LIST_32, device.abiList32);
        values.put(RandomizersCache.SETTING_SOC_CPU_ABI_LIST_64, device.abiList64);
        settings = Collections.unmodifiableMap(values);
    }

    public String get(String settingName) {
        return settings.get(settingName);
    }

    public Map<String, String> asSettingMap() {
        return settings;
    }

    public String summary() {
        return device.manufacturer + " — " + device.marketingName + " — Android " + build.release;
    }
}
