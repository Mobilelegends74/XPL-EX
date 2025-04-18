package eu.faircode.xlua.x.xlua.settings.random.randomizers.battery;

import android.os.BatteryManager;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionInt;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomBatteryStatus extends RandomElement {
    public RandomBatteryStatus() {
        super("Battery Status");
        putSettings(RandomizersCache.SETTING_BATTERY_STATUS);
        putOptions(
                RandomOptionNullElement.create(),
                RandomOptionInt.create("Charging", BatteryManager.BATTERY_STATUS_CHARGING),
                RandomOptionInt.create("Discharging", BatteryManager.BATTERY_STATUS_DISCHARGING),
                RandomOptionInt.create("Full", BatteryManager.BATTERY_STATUS_FULL),
                RandomOptionInt.create("Not Charging", BatteryManager.BATTERY_STATUS_NOT_CHARGING),
                RandomOptionInt.create("Unknown", BatteryManager.BATTERY_STATUS_UNKNOWN));

    }

    @Override
    public void randomize(RandomizerSessionContext context) { randomOption().randomize(context); }
}