package eu.faircode.xlua.x.xlua.settings.random.randomizers.battery;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

/** Randomizes all battery values from one coherent device-profile selection. */
public final class RandomBatteryProfile extends RandomElement {
    private static final String[] SETTINGS = new String[] {
            RandomizersCache.SETTING_BATTERY_CAPACITY_MAH,
            RandomizersCache.SETTING_BATTERY_PERCENT,
            RandomizersCache.SETTING_BATTERY_STATUS,
            RandomizersCache.SETTING_BATTERY_IS_CHARGING,
            RandomizersCache.SETTING_BATTERY_IS_PLUGGED,
            RandomizersCache.SETTING_BATTERY_CHARGING_CYCLES,
            RandomizersCache.SETTING_BATTERY_VOLTAGE_MV,
            RandomizersCache.SETTING_BATTERY_TEMPERATURE_TENTHS_C
    };

    public RandomBatteryProfile() {
        super("Coherent Battery Profile");
        putSettings(SETTINGS);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        String settingName = context.stack.pop();
        String value = context.getDeviceProfileValue(settingName);
        if (value != null)
            context.pushValue(settingName, value);
    }
}
