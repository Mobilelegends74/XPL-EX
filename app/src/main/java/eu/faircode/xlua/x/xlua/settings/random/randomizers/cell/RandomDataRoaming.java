package eu.faircode.xlua.x.xlua.settings.random.randomizers.cell;

import android.telephony.SubscriptionManager;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionInt;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomDataRoaming extends RandomElement {
    public RandomDataRoaming() {
        super("Data Roaming Bit Flag");
        putIndexSettings(RandomizersCache.SETTING_CELL_DATA_ROAMING, 1, 2);
        putOptions(
                RandomOptionNullElement.create(),
                RandomOptionInt.create("Enabled (" + SubscriptionManager.DATA_ROAMING_ENABLE + ")", SubscriptionManager.DATA_ROAMING_ENABLE),
                RandomOptionInt.create("DISABLED (" + SubscriptionManager.DATA_ROAMING_DISABLE + ")", SubscriptionManager.DATA_ROAMING_DISABLE));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        randomOption(true).randomize(context);
    }
}