package eu.faircode.xlua.x.xlua.settings.random.randomizers.cell;

import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomCellDisplayName extends RandomElement {
    public static List<String> NAMES = Arrays.asList(
            "billy",
            "xpl",
            "joe",
            "jannet",
            "joey",
            "amanda",
            "random",
            "random-op");

    public RandomCellDisplayName() {
        super("Cell Display Name");
        putIndexSettings(RandomizersCache.SETTING_CELL_DISPLAY_NAME, 1, 2);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        String op = RandomGenerator.nextElement(NAMES);
        if(op.equals("random")) {
            context.pushSpecial(context.stack.pop(), RandomGenerator.nextString(8, 32));
        } else if(op.equals("random-op")) {
            context.pushSpecial(context.stack.pop(), RandomGenerator.nextElement(RandomOperatorName.CARRIER_NAMES));
        } else {
            context.pushSpecial(context.stack.pop(), op);
        }
    }
}
