package eu.faircode.xlua.x.xlua.settings.random;

import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomGenericBool extends RandomElement {
    public RandomGenericBool() {
        super("Random Boolean (True/False)");
        putIndexSettings(RandomizersCache.SETTING_CELL_OPERATOR_E_SIM, 1, 2);
        putIndexSettings(RandomizersCache.SETTING_CELL_DATA_IS_OPPORTUNISTIC, 1, 2);

        putSettings(
                RandomizersCache.SETTING_FILE_SYNC_TIME,
                RandomizersCache.SETTING_FILES_OFFSET_SUBTRACT,

                RandomizersCache.SETTING_APP_SYNC_TIME,
                RandomizersCache.SETTING_APP_OFFSET_SUBTRACT,

                RandomizersCache.SETTING_BATTERY_IS_PLUGGED,
                RandomizersCache.SETTING_BATTERY_IS_CHARGING,
                RandomizersCache.SETTING_BATTERY_IS_POWER_SAVE_MODE);
        putOptions(
                RandomOptionNullElement.create(),
                RandomOptionString.create("True"),
                RandomOptionString.create("False"));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        randomOption(true).randomize(context);
    }
}