package eu.faircode.xlua.x.xlua.settings.random.randomizers.cell._deprecated;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomMCC extends RandomElement {
    public static RandomMCC create() { return new RandomMCC(); }
    public RandomMCC() {
        super("Cell MCC");
        bindSetting(RandomizersCache.SETTING_CELL_OPERATOR_MCC);
    }

    @Override
    public String generateString() { return String.valueOf(RandomGenerator.nextInt(100, 999)); }
}
