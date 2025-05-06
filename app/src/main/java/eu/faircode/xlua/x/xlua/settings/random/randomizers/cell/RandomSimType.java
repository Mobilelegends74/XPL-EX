package eu.faircode.xlua.x.xlua.settings.random.randomizers.cell;

import android.os.Build;
import android.telephony.SubscriptionManager;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionInt;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomSimType extends RandomElement {
    public RandomSimType() {
        super("SIM Type");
        putIndexSettings(RandomizersCache.SETTING_SIM_KIND, 1, 2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            putOptions(
                    RandomOptionNullElement.create(),
                    RandomOptionInt.create("Local SIM", SubscriptionManager.SUBSCRIPTION_TYPE_LOCAL_SIM),
                    RandomOptionInt.create("Remote SIM (E-SIM) ", SubscriptionManager.SUBSCRIPTION_TYPE_REMOTE_SIM));
        } else {
            putOptions(
                    RandomOptionNullElement.create(),
                    RandomOptionInt.create("Local SIM", 0),
                    RandomOptionInt.create("Remote SIM (E-SIM)", 1));
        }
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        randomOption(true).randomize(context);
    }
}