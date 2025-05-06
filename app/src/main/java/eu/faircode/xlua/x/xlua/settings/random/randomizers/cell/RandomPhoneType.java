package eu.faircode.xlua.x.xlua.settings.random.randomizers.cell;

import android.os.Build;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionInt;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomPhoneType extends RandomElement {
    public RandomPhoneType() {
        super("CELL Phone Type");
        putIndexSettings(RandomizersCache.SETTING_PHONE_KIND, 1, 2);
        putOptions(
                RandomOptionNullElement.create(),
                RandomOptionInt.create("None", TelephonyManager.PHONE_TYPE_NONE),
                RandomOptionInt.create("GSM", TelephonyManager.PHONE_TYPE_GSM),
                RandomOptionInt.create("CDMA", TelephonyManager.PHONE_TYPE_CDMA),
                RandomOptionInt.create("SIP", TelephonyManager.PHONE_TYPE_SIP));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        randomOption(true).randomize(context);
    }
}