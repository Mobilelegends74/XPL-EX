package eu.faircode.xlua.x.xlua.settings.random.randomizers.battery;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

//battery.charging.cycles
public class RandomChargingCycles extends RandomElement {
    public RandomChargingCycles() {
        super("Battery Charging Cycles");
        putSettings(RandomizersCache.SETTING_BATTERY_CHARGING_CYCLES);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushSpecial(context.stack.pop(), String.valueOf(RandomGenerator.nextInt(10, 999)));
    }
}