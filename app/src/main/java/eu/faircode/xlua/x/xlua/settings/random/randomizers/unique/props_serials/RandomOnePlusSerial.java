package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.props_serials;


//IGFBDIAFCJIFIEJFG


import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomOnePlusSerial extends RandomElement {
    public RandomOnePlusSerial() {
        super("Unique OnePlus Serial NO");
        putSettings(RandomizersCache.SETTING_PROP_ONE_PLUS_UNIQUE_SERIAL);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), RandomGenerator.nextStringAlpha(17).toUpperCase());
    }
}