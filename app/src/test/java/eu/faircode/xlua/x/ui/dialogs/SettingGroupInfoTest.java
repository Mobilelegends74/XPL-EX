package eu.faircode.xlua.x.ui.dialogs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SettingGroupInfoTest {
    @Test
    public void normalizesVisibleSettingGroupNames() {
        assertEquals("android", SettingGroupInfo.normalizeGroupName("Android"));
        assertEquals("xplex", SettingGroupInfo.normalizeGroupName("xplex"));
        assertEquals("setting_values", SettingGroupInfo.normalizeGroupName("Setting.Values"));
    }

    @Test
    public void normalizesMissingGroupToEmptyName() {
        assertEquals("", SettingGroupInfo.normalizeGroupName(null));
        assertEquals("", SettingGroupInfo.normalizeGroupName(""));
    }
}
