package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique.props_serials;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.SerialNumberGenerator;

public class RandomSerialGeneric extends RandomElement {
    public RandomSerialGeneric() {
        super("Unique Serial NO Generic");
        putSettings(
                "props.unique.ril.serialnumber",
                "props.unique.ap.serial",
                "props.unique.em.did",
                "props.unique.lite.uid",
                "props.unique.persist.radio.serialno");
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), SerialNumberGenerator.generateSerial());
    }
}