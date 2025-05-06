package eu.faircode.xlua.x.xlua.settings.random.randomizers.cell;

import java.util.Arrays;
import java.util.List;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

public class RandomOperatorName extends RandomElement {
    public static List<String> CARRIER_NAMES = Arrays.asList(
            "T-Mobile",
            "Verizon",
            "AT&T",
            "Sprint",
            "US Cellular",
            "Metro by T-Mobile",
            "Cricket Wireless",
            "Boost Mobile",
            "Xfinity Mobile",
            "Spectrum Mobile",
            "Google Fi",
            "Visible",
            "Mint Mobile",
            "Consumer Cellular",
            "Straight Talk",
            "TracFone",
            "Simple Mobile",
            "Total Wireless",
            "Telcel",
            "Vodafone",
            "Orange",
            "O2",
            "EE",
            "Three",
            "Rogers",
            "Bell",
            "Telus",
            "Optus",
            "Telstra",
            "DoCoMo",
            "SoftBank",
            "China Mobile",
            "China Telecom",
            "Airtel",
            "Jio",
            "Movistar",
            "Claro"
    );

    public RandomOperatorName() {
        super("Cell Operator Name (Carrier Name)");
        putIndexSettings(RandomizersCache.SETTING_CELL_OPERATOR_NAME, 1, 2);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        context.pushSpecial(context.stack.pop(), RandomGenerator.nextElement(CARRIER_NAMES));
    }
}
