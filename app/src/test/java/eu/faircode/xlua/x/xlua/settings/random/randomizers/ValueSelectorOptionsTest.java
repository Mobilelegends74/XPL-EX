package eu.faircode.xlua.x.xlua.settings.random.randomizers;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.faircode.xlua.x.xlua.settings.random.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.battery.RandomBatteryProfile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ValueSelectorOptionsTest {
    @Test
    public void booleanSettingsExposeTrueFalseChoices() {
        Set<String> values = rawValues(RandomizersCache.getValueSelectorOptions(
                RandomizersCache.SETTING_BATTERY_IS_CHARGING));

        assertTrue(values.contains("True"));
        assertTrue(values.contains("False"));

        Set<String> conventionOnlyValues = rawValues(RandomizersCache.getValueSelectorOptions(
                "bluetooth.allow.discovery.bool"));
        assertTrue(conventionOnlyValues.contains("True"));
        assertTrue(conventionOnlyValues.contains("False"));
    }

    @Test
    public void batteryStatusOffersEveryAndroidStatus() {
        Set<String> values = rawValues(RandomizersCache.getValueSelectorOptions(
                RandomizersCache.SETTING_BATTERY_STATUS));

        assertEquals(5, values.size());
        assertTrue(values.contains("1"));
        assertTrue(values.contains("2"));
        assertTrue(values.contains("3"));
        assertTrue(values.contains("4"));
        assertTrue(values.contains("5"));
    }

    @Test
    public void selectorDoesNotReplaceCoherentProfileType() {
        assertEquals(RandomBatteryProfile.class,
                RandomizersCache.SETTING_BATTERY_STATUS_TYPE);
    }

    private static Set<String> rawValues(List<IRandomizer> options) {
        Set<String> values = new HashSet<>();
        for(IRandomizer option : options) {
            String value = option.getRawValue();
            if(value != null)
                values.add(value);
        }
        return values;
    }
}
