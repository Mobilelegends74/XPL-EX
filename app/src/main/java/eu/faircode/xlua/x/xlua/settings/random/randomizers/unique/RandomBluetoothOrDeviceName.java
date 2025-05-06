package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomBluetoothOrDeviceName extends RandomElement {
    public static List<String> NAMES = Arrays.asList(
            "Android",
            "William",
            "Bobby",
            "Jonny",
            "Uber",
            "Dad",
            "Mom",
            "Amanda",
            "Lucy",
            "John",
            "Burt",
            "random"
    );

    //net.bt.name
    public RandomBluetoothOrDeviceName() {
        super("Bluetooth Name or Device Name");
        putSettings(RandomizersCache.SETTING_BLUETOOTH_NAME, RandomizersCache.SETTING_DEVICE_NAME);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        String val = RandomGenerator.nextElement(NAMES);
        if(RandomGenerator.nextBoolean() || val.equals("random"))
            val = RandomGenerator.generateRandomAlphanumericString(RandomGenerator.nextInt(6, 23));
        context.pushValue(context.stack.pop(), val);
    }
}