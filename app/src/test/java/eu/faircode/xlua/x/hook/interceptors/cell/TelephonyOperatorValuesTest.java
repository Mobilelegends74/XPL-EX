package eu.faircode.xlua.x.hook.interceptors.cell;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TelephonyOperatorValuesTest {
    @Test
    public void resolvesOperatorNameForRequestedSimSlot() {
        Map<String, String> settings = new HashMap<>();
        settings.put("cell.operator.name.1", "Operator One");
        settings.put("cell.operator.name.2", "Operator Two");

        assertEquals("Operator One", TelephonyOperatorValues.resolve(settings, true, 0));
        assertEquals("Operator Two", TelephonyOperatorValues.resolve(settings, true, 1));
    }

    @Test
    public void prefersNumericIdAndFallsBackToMccPlusMnc() {
        Map<String, String> settings = new HashMap<>();
        settings.put("cell.operator.numeric.id.1", "25001");
        settings.put("cell.operator.mcc.1", "999");
        settings.put("cell.operator.mnc.1", "99");
        settings.put("cell.operator.mcc.2", "310");
        settings.put("cell.operator.mnc.2", "260");

        assertEquals("25001", TelephonyOperatorValues.resolve(settings, false, 0));
        assertEquals("310260", TelephonyOperatorValues.resolve(settings, false, 1));
    }

    @Test
    public void unknownSlotUsesFirstConfiguredSimAndEmptyValuesDoNotSpoof() {
        Map<String, String> settings = new HashMap<>();
        settings.put("cell.operator.name.1", "  ");
        settings.put("cell.operator.name.2", "Fallback Operator");

        assertEquals("Fallback Operator", TelephonyOperatorValues.resolve(settings, true, -1));
        assertNull(TelephonyOperatorValues.resolve(new HashMap<>(), false, -1));
    }
}
