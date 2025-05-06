package eu.faircode.xlua.x.xlua.settings.random;

import java.util.ArrayList;
import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;

public class RandomIntOption extends RandomElement {
    public static IRandomizer create(String displayName, int origin, int bound) { return new RandomIntOption(displayName, origin, bound); }
    private int origin;
    private int bound;

    @Override
    public String getRawValue() { return String.valueOf(RandomGenerator.nextInt(origin, bound)); }

    public RandomIntOption(int origin, int bound) { this("Random", origin, bound); }
    public RandomIntOption(String displayName, int origin, int bound) {
        super(Str.combineEx(displayName, " (", origin, " - ", bound, ")"));
        this.origin = origin;
        this.bound = bound;
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushValue(context.stack.pop(), String.valueOf(RandomGenerator.nextInt(origin, bound)));
    }
}
