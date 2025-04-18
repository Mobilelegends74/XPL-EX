package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomGSFID extends RandomElement {
    public RandomGSFID() {
        super("Google Services Framework ID");
        putSettings(RandomizersCache.SETTING_UNIQUE_GSF_ID);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //Was 16 Char Length
        String setting = context.stack.pop();
        if(setting == null)
            return;

        context.pushSpecial(setting, RandomGenerator.nextStringNumeric(19));
    }
}