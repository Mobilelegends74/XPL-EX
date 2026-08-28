package eu.faircode.xlua.x.ui.dialogs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SettingValueInfoTest {
    @Test
    public void normalizesSingleSettingName() {
        assertEquals(
                "android_build_fingerprint",
                SettingValueInfo.normalizeSettingName("Android.Build.Fingerprint"));
    }

    @Test
    public void normalizesIndexedContainerName() {
        assertEquals(
                "cell_operator_mcc",
                SettingValueInfo.normalizeSettingName("cell.operator.mcc.[1,2]"));
    }

    @Test
    public void keepsMeaningfulNumericSuffix() {
        assertEquals(
                "soc_cpu_abi_list_64",
                SettingValueInfo.normalizeSettingName("soc.cpu.abi.list.64"));
    }

    @Test
    public void normalizesMissingNameToEmptyName() {
        assertEquals("", SettingValueInfo.normalizeSettingName(null));
        assertEquals("", SettingValueInfo.normalizeSettingName(""));
    }
}
