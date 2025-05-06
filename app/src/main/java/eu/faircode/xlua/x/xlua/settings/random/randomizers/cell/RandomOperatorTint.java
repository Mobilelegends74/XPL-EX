package eu.faircode.xlua.x.xlua.settings.random.randomizers.cell;

import android.telephony.SubscriptionManager;

import eu.faircode.xlua.x.xlua.settings.random.RandomColorIntOption;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomIntOption;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionInt;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomOperatorTint  extends RandomElement {
    public RandomOperatorTint() {
        super("Cell Operator ICON Tint");
        putIndexSettings(RandomizersCache.SETTING_OPERATOR_ICON_TINT, 1, 2);
        putOptions(
                RandomOptionNullElement.create(),
                RandomOptionInt.create("Deep Blue", 0xFF3949AB),
                RandomOptionInt.create("Material Blue", 0xFF1E88E5),
                RandomOptionInt.create("Teal", 0xFF00897B),
                RandomOptionInt.create("Green", 0xFF43A047),
                RandomOptionInt.create("Light Green", 0xFF7CB342),
                RandomOptionInt.create("Lime", 0xFFC0CA33),
                RandomOptionInt.create("Yellow", 0xFFFDD835),
                RandomOptionInt.create("Amber", 0xFFFFB300),
                RandomOptionInt.create("Orange", 0xFFFB8C00),
                RandomOptionInt.create("Deep Orange", 0xFFF4511E),
                RandomOptionInt.create("Brown", 0xFF6D4C41),
                RandomOptionInt.create("Deep Purple", 0xFF5E35B1),
                RandomOptionInt.create("Pink", 0xFFD81B60),
                RandomOptionInt.create("Red", 0xFFE53935),
                RandomOptionInt.create("Purple", 0xFF8E24AA),
                RandomOptionInt.create("Light Green Alt", 0xFF99CC00),
                RandomOptionInt.create("Midnight Blue", 0xFF191970),
                RandomOptionInt.create("Steel Blue", 0xFF759CBB),
                RandomOptionInt.create("Mint Green", 0xFFB7F0B1),
                RandomOptionInt.create("Forest Green", 0xFF137F3B),
                RandomColorIntOption.create());
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        randomOption(true, true).randomize(context);
    }
}