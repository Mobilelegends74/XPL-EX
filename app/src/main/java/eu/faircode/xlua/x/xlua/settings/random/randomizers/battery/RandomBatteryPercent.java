package eu.faircode.xlua.x.xlua.settings.random.randomizers.battery;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomBatteryPercent extends RandomElement {
    public RandomBatteryPercent() {
        super("Battery Charge (%)");
        putSettings(RandomizersCache.SETTING_BATTERY_PERCENT);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushSpecial(context.stack.pop(), String.valueOf(RandomGenerator.nextInt(1, 100)));
    }
}