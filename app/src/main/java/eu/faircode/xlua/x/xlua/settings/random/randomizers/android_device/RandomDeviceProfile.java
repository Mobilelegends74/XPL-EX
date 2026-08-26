package eu.faircode.xlua.x.xlua.settings.random.randomizers.android_device;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomDeviceProfile extends RandomElement {
    private static final String[] SETTINGS = new String[] {
            RandomizersCache.SETTING_DEVICE_BRAND,
            RandomizersCache.SETTING_DEVICE_MANUFACTURER,
            RandomizersCache.SETTING_DEVICE_NICKNAME,
            RandomizersCache.SETTING_DEVICE_MODEL,
            RandomizersCache.SETTING_DEVICE_CODENAME,
            RandomizersCache.SETTING_ANDROID_BUILD_VERSION,
            RandomizersCache.SETTING_ANDROID_BUILD_VERSION_SDK,
            RandomizersCache.SETTING_ANDROID_BUILD_VERSION_MIN_SDK,
            RandomizersCache.SETTING_ANDROID_BUILD_TAGS,
            RandomizersCache.SETTING_ANDROID_BUILD_INCREMENTAL,
            RandomizersCache.SETTING_ANDROID_BUILD_DESCRIPTION,
            RandomizersCache.SETTING_ANDROID_BUILD_ID,
            RandomizersCache.SETTING_ANDROID_BUILD_FLAVOR,
            RandomizersCache.SETTING_ANDROID_BUILD_CODENAME,
            RandomizersCache.SETTING_ANDROID_BUILD_FINGERPRINT,
            RandomizersCache.SETTING_ANDROID_ETC_BUILD_ROM_VERSION_CODENAME,
            RandomizersCache.SETTING_ANDROID_ETC_BUILD_ROM_VARIANT,
            RandomizersCache.SETTING_SOC_BOARD_MODEL,
            RandomizersCache.SETTING_SOC_BOARD_CONFIG_CODE_NAME,
            RandomizersCache.SETTING_SOC_BOARD_MANUFACTURER,
            RandomizersCache.SETTING_SOC_BOARD_MANUFACTURER_ID,
            RandomizersCache.SETTING_SOC_CPU_PROCESSOR_COUNT,
            RandomizersCache.SETTING_SOC_CPU_ARCHITECTURE,
            RandomizersCache.SETTING_SOC_CPU_ABI,
            RandomizersCache.SETTING_SOC_CPU_ABI_LIST,
            RandomizersCache.SETTING_SOC_CPU_ABI_LIST_32,
            RandomizersCache.SETTING_SOC_CPU_ABI_LIST_64
    };

    public RandomDeviceProfile() {
        super("Real Android Device Profile");
        putSettings(SETTINGS);
    }

    public static boolean isProfileSetting(String settingName) {
        if (settingName == null)
            return false;
        for (String setting : SETTINGS)
            if (setting.equalsIgnoreCase(settingName))
                return true;
        return false;
    }

    public static int profileSettingCount() {
        return SETTINGS.length;
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        String settingName = context.stack.pop();
        String value = context.getDeviceProfileValue(settingName);
        if (value != null)
            context.pushValue(settingName, value);
    }
}
