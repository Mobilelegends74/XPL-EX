package eu.faircode.xlua.x.xlua.settings.random.randomizers.network;

import eu.faircode.xlua.x.xlua.hook.PackageHookContext;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomNetAllowedList extends RandomElement {
    public RandomNetAllowedList() {
        super("Network Show List");
        putSettings(RandomizersCache.SETTING_NET_ALLOWED_LIST);
        putOptions(RandomNullElement.create(),
                RandomOptionString.create("Random", PackageHookContext.RANDOM_VALUE));
        //putOptions(RandomOptionNullElement.create(),
               // RandomOptionString.create("Green Safe", "1"),
               // RandomOptionString.create("Red Blown", "0"));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //context.stack.push(getFirstSettingName());
        randomOption().randomize(context);
    }
}