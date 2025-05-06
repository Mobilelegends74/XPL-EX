package eu.faircode.xlua.x.xlua.settings.random.randomizers.cell;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionInt;
import eu.faircode.xlua.x.xlua.settings.random.RandomOptionNullElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;

//"carrierIdentification.db" => carrier_id [carrier id] Example (1839)
//We can also try to load DB on Service end ???
public class RandomOperatorCarrierId extends RandomElement {
    private boolean doChance = false;
    public RandomOperatorCarrierId() {
        super("Operator Carrier ID");
        putIndexSettings(RandomizersCache.SETTING_CELL_OPERATOR_ID, 1, 2);
        putOptions(
                RandomOptionNullElement.create(),
                RandomOptionInt.create("T-Mobile - US", 1),
                RandomOptionInt.create("T-Mobile - UA", 12),
                RandomOptionInt.create("Verizon Wireless", 1839),
                RandomOptionInt.create("AT&T", 1187),
                RandomOptionInt.create("AT&T Wireless (Antigua)", 1326),
                RandomOptionInt.create("AT&T Mobility (Wireless Maritime Services)", 1770),
                RandomOptionInt.create("AT&T Mobility Vanguard Services", 1807),
                RandomOptionInt.create("AT&T MX (1)", 1912),
                RandomOptionInt.create("AT&T MX (2)", 1925),
                RandomOptionInt.create("AT&T 5G", 10021),
                RandomOptionInt.create("AT&T 5G SA", 10028),
                RandomOptionInt.create("Tracfone-ATT", 10000),
                RandomOptionInt.create("China Mobile", 1435),
                RandomOptionInt.create("China Unicom", 1436),
                RandomOptionInt.create("Vodafone (1)", 20),
                RandomOptionInt.create("Vodafone (2)", 21),
                RandomOptionInt.create("Vodafone (3)", 22),
                RandomOptionInt.create("Vodafone (4)", 23),
                RandomOptionInt.create("Vodafone (5)", 24),
                RandomOptionInt.create("Vodafone (6)", 25),
                RandomOptionInt.create("Vodafone (7)", 28),
                RandomOptionInt.create("Vodafone (8)", 29),
                RandomOptionInt.create("Vodafone (9)", 1535),
                RandomOptionInt.create("Internet One Ltd", 1496),
                RandomOptionInt.create("British Telecom", 1509),
                RandomOptionInt.create("PSN", 1536),
                RandomOptionInt.create("Jersey Telecom", 1506),
                RandomOptionInt.create("Telecom Samoa Cellular Ltd.", 1302),
                RandomOptionInt.create("Mattel S.A.", 1616),
                RandomOptionInt.create("Consumer Cellular ATT", 10022),
                RandomOptionInt.create("Red Pocket-ATT", 10012),
                RandomOptionInt.create("Telekom", 3),
                RandomOptionInt.create("Magenta Telekom", 4),
                RandomOptionInt.create("Magyar Telekom (1)", 8),
                RandomOptionInt.create("Magyar Telekom (2)", 9));
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        if(!doChance || RandomGenerator.chance()) {
            randomOption(true).randomize(context);
        } else {
            String item = context.stack.pop();
            if(item != null) {
                context.pushSpecial(item, String.valueOf(RandomGenerator.nextInt(1, 99999)));
            }
        }
    }
}