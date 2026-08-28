package eu.faircode.xlua.x.xlua.settings.random.profile;

import android.os.BatteryManager;

import java.util.Collections;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
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
        values.put(RandomizersCache.SETTING_SOC_CPU_INFO_DUMP, CpuInfoProfile.generate(device));

        String gpuRenderer = gpuRendererFor(device.socModel);
        boolean maliGpu = gpuRenderer.startsWith("Mali") || gpuRenderer.startsWith("Immortalis");
        String gpuVendor = maliGpu ? "ARM" : "Qualcomm";
        values.put(RandomizersCache.SETTING_SOC_GPU_EGL_IMPLEMENTOR, gpuVendor);
        values.put(RandomizersCache.SETTING_SOC_GPU_VULKAN_IMPLEMENTOR, gpuVendor);
        values.put(RandomizersCache.SETTING_SOC_GPU_OPEN_GLES_VERSION_ENCODED, "196610");
        values.put(RandomizersCache.SETTING_SOC_GPU_OPEN_GLES_VENDOR, gpuVendor);
        values.put(RandomizersCache.SETTING_SOC_GPU_OPEN_GLES_RENDERER, gpuRenderer);
        values.put(RandomizersCache.SETTING_SOC_GPU_OPEN_GLES_VERSION, "OpenGL ES 3.2");

        DeviceCharacteristics characteristics = device.characteristics;
        if (characteristics == null)
            throw new IllegalArgumentException("Model characteristics are missing for " + device.id);

        values.put(RandomizersCache.SETTING_DISPLAY_WIDTH, String.valueOf(characteristics.displayWidthPx));
        values.put(RandomizersCache.SETTING_DISPLAY_HEIGHT, String.valueOf(characteristics.displayHeightPx));
        values.put(RandomizersCache.SETTING_DISPLAY_DENSITY_DPI, String.valueOf(characteristics.displayDensityDpi));
        values.put(RandomizersCache.SETTING_DISPLAY_REFRESH_RATE_HZ, String.valueOf(characteristics.displayRefreshRateHz));

        int batteryPercent = RandomGenerator.nextInt(25, 95);
        boolean batteryCharging = RandomGenerator.nextInt(4) == 0;
        int batteryStatus = batteryCharging
                ? BatteryManager.BATTERY_STATUS_CHARGING
                : BatteryManager.BATTERY_STATUS_DISCHARGING;
        int batteryVoltage = Math.min(4350,
                3300 + Math.round(batteryPercent * 10.5f) + (batteryCharging ? 40 : 0));
        int deviceAge = Math.max(0,
                Calendar.getInstance().get(Calendar.YEAR) - characteristics.launchYear);
        int maxCycles = Math.min(1000, 180 + deviceAge * 140);
        int batteryCycles = RandomGenerator.nextInt(20, maxCycles + 1);
        values.put(RandomizersCache.SETTING_BATTERY_CAPACITY_MAH, String.valueOf(characteristics.batteryCapacityMah));
        values.put(RandomizersCache.SETTING_BATTERY_PERCENT, String.valueOf(batteryPercent));
        values.put(RandomizersCache.SETTING_BATTERY_STATUS, String.valueOf(batteryStatus));
        values.put(RandomizersCache.SETTING_BATTERY_IS_CHARGING, String.valueOf(batteryCharging));
        values.put(RandomizersCache.SETTING_BATTERY_IS_PLUGGED, String.valueOf(batteryCharging));
        values.put(RandomizersCache.SETTING_BATTERY_CHARGING_CYCLES, String.valueOf(batteryCycles));
        values.put(RandomizersCache.SETTING_BATTERY_VOLTAGE_MV, String.valueOf(batteryVoltage));
        values.put(RandomizersCache.SETTING_BATTERY_TEMPERATURE_TENTHS_C,
                batteryCharging ? "310" : "270");

        values.put(RandomizersCache.SETTING_HARDWARE_MEMORY_TOTAL, String.valueOf(characteristics.ramGb));
        values.put(RandomizersCache.SETTING_HARDWARE_MEMORY_AVAILABLE,
                String.valueOf(Math.max(2, characteristics.ramGb * 3 / 5)));
        values.put(RandomizersCache.SETTING_HARDWARE_CAMERA_COUNT, String.valueOf(characteristics.cameraCount));
        values.put(RandomizersCache.SETTING_HARDWARE_CAMERA_APP,
                cameraPackageFor(device.brand, device.manufacturer));
        values.put(RandomizersCache.SETTING_HARDWARE_GPS_MODEL_YEAR, String.valueOf(characteristics.launchYear));
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

    private static String gpuRendererFor(String socModel) {
        String model = socModel == null ? "" : socModel.toUpperCase();
        if (model.startsWith("TENSOR G1"))
            return "Mali-G78 MP20";
        if (model.startsWith("TENSOR G2"))
            return "Mali-G710 MP7";
        if (model.startsWith("TENSOR G3"))
            return "Immortalis-G715s MC10";
        if (model.startsWith("TENSOR G4"))
            return "Mali-G715 MP7";
        if (model.startsWith("SM8150"))
            return "Adreno 640";
        if (model.startsWith("SM8250"))
            return "Adreno 650";
        if (model.startsWith("SM8350"))
            return "Adreno 660";
        if (model.startsWith("SM8450") || model.startsWith("SM8475"))
            return "Adreno 730";
        if (model.startsWith("SM8550"))
            return "Adreno 740";
        if (model.startsWith("SM8650"))
            return "Adreno 750";
        if (model.startsWith("SM8735"))
            return "Adreno 825";
        if (model.startsWith("SM8750"))
            return "Adreno 830";
        if (model.startsWith("SM8845") || model.startsWith("SM8850"))
            return "Adreno 840";
        throw new IllegalArgumentException("Unsupported GPU mapping for " + socModel);
    }

    private static String cameraPackageFor(String brand, String manufacturer) {
        String identity = ((brand == null ? "" : brand) + " "
                + (manufacturer == null ? "" : manufacturer)).toLowerCase(Locale.ROOT);
        if (identity.contains("google"))
            return "com.google.android.GoogleCamera";
        if (identity.contains("samsung"))
            return "com.sec.android.app.camera";
        if (identity.contains("oneplus"))
            return "com.oplus.camera";
        if (identity.contains("asus"))
            return "com.asus.camera";
        if (identity.contains("meizu"))
            return "com.meizu.media.camera";
        if (identity.contains("nubia") || identity.contains("redmagic"))
            return "cn.nubia.camera";
        if (identity.contains("motorola"))
            return "com.motorola.camera3";
        if (identity.contains("lenovo"))
            return "com.zui.camera";
        return "com.android.camera";
    }
}
