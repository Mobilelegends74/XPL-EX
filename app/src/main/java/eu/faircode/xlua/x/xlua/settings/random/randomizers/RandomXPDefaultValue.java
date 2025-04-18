package eu.faircode.xlua.x.xlua.settings.random.randomizers;
//xplex.ignore.default.values

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.xlua.settings.SettingHolder;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionBoolean;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionString;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;

public class RandomXPDefaultValue extends RandomElement {
    public RandomXPDefaultValue() {
        super("XP Reserved (Defaults)");
        putSettings(RandomizersCache.SETTING_XP_DEFAULTS);
        putOptions(RandomOptionBoolean.generate());

        //Str.bytesToHex()
        //new SettingHolder().getNa
    }

    //ListView of checkable items
    //Save to Settings
    //When intializing take that list put "ingore." in front of the name

    @Override
    public void randomize(RandomizerSessionContext context) {
        randomOption().randomize(context);
    }
}