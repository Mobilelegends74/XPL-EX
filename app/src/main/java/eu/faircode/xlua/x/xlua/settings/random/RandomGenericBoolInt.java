package eu.faircode.xlua.x.xlua.settings.random;

import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomGenericBoolInt extends RandomElement {
    public RandomGenericBoolInt() {
        super("Random Int Boolean (1=True/0=False) ");
        putSettings(
                RandomizersCache.SETTING_SETTING_MOCK_LOCATION,
                RandomizersCache.SETTING_SETTING_MASS_STORAGE,
                RandomizersCache.SETTING_SETTING_DEVICE_PROVISIONED,
                RandomizersCache.SETTING_SETTING_STAY_ON_WHILE_PLUGGED,
                RandomizersCache.SETTING_SETTING_ADB_ENABLED,
                RandomizersCache.SETTING_SETTING_DEV_SETTINGS_ENABLED);

        putOptions(
                RandomOptionNullElement.create(),
                RandomOptionInt.create("True", 1),
                RandomOptionInt.create("False", 0));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        //String setting = context.stack.pop();
        //if(setting == null)
        //    return;

        randomOption().randomize(context);
        //context.pushValue(setting, );
    }
}