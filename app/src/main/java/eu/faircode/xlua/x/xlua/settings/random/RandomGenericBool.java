package eu.faircode.xlua.x.xlua.settings.random;

import android.os.BatteryManager;

import java.util.UUID;

import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.x.runtime.BuildInfo;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomGenericBool extends RandomElement {
    public RandomGenericBool() {
        super("Random Boolean (True/False)");
        putSettings(
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
        //String setting = context.stack.pop();
        //if(setting == null)
        //    return;

        randomOption().randomize(context);
        //context.pushValue(setting, );
    }
}