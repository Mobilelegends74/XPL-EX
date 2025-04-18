package eu.faircode.xlua.x.xlua.settings.random;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;

public class RandomOptionBoolean extends RandomElement {
    public static final IRandomizer TRUE = new RandomOptionBoolean(true);
    public static final IRandomizer FALSE = new RandomOptionBoolean(false);

    public boolean value;

    @Override
    public String getRawValue() { return String.valueOf(value); }

    public RandomOptionBoolean(boolean flag) {
        super(String.valueOf(flag));
        this.value = flag;
    }

    @Override
    public void randomize(RandomizerSessionContext context) { context.pushValue(context.stack.pop(), String.valueOf(value)); }

    public static IRandomizer[] generate() { return generate(true); }
    public static IRandomizer[] generate(boolean generateNullFirstItem) {
        List<IRandomizer> items = new ArrayList<>();
        if(generateNullFirstItem) items.add(RandomOptionNullElement.create());
        items.add(TRUE);
        items.add(FALSE);
        return ArrayUtils.toArray(items, IRandomizer.class);
    }
}
