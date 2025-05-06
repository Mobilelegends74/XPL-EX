package eu.faircode.xlua.x.xlua.settings.random.randomizers.cell;

import android.telephony.TelephonyManager;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionInt;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomSimCount extends RandomElement {
    public RandomSimCount() {
        super("CELL SIM Count");
        putSettings(RandomizersCache.SETTING_CELL_SIM_COUNT);
        putOptions(RandomOptionInt.generateOptions(0, 1, 2));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        randomOption(true).randomize(context);
    }
}