package eu.faircode.xlua.x.xlua.settings.random.randomizers.settings;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomBootCount extends RandomElement {
    public RandomBootCount() {
        super("Boot Count");
        putSettings(RandomizersCache.SETTING_SETTING_BOOT_COUNT);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushSpecial(context.stack.pop(), String.valueOf(RandomGenerator.nextInt(3, 9999)));
    }
}