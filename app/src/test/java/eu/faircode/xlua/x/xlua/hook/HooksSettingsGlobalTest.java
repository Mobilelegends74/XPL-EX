package eu.faircode.xlua.x.xlua.hook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Test;

public class HooksSettingsGlobalTest {
    @After
    public void clearIndex() {
        HooksSettingsGlobal.clear();
    }

    @Test
    public void normalizesSettingNames() {
        assertEquals(
                Arrays.asList("unique.android.id"),
                HooksSettingsGlobal.getLookupNames("  UNIQUE.Android.ID  "));
    }

    @Test
    public void includesBaseNameForNumericIndex() {
        assertEquals(
                Arrays.asList("cell.operator.name.1", "cell.operator.name"),
                HooksSettingsGlobal.getLookupNames("cell.operator.name.1"));
    }

    @Test
    public void includesBaseNameForArrayIndex() {
        assertEquals(
                Arrays.asList("network.wifi.bssid.[1,2]", "network.wifi.bssid"),
                HooksSettingsGlobal.getLookupNames("network.wifi.bssid.[1,2]"));
    }

    @Test
    public void preservesNonNumericSuffix() {
        assertEquals(
                Arrays.asList("android.build.version.sdk"),
                HooksSettingsGlobal.getLookupNames("android.build.version.sdk"));
    }

    @Test
    public void resolvesIndexedSettingToActiveHook() throws Exception {
        indexSetting("cell.operator.name", "PrivacyEx.Telephony");

        AppAssignmentsMap assignments = new AppAssignmentsMap(12345, "com.example.app");
        assignments.setAssigned("PrivacyEx.Telephony", true);

        assertTrue(assignments.isAssigned(HooksSettingsGlobal
                .getHookIdsFromIndex(Arrays.asList("cell.operator.name.2"))
                .get(0)));

        assignments.setAssigned("PrivacyEx.Telephony", false);
        assertFalse(assignments.isAssigned(HooksSettingsGlobal
                .getHookIdsFromIndex(Arrays.asList("cell.operator.name.2"))
                .get(0)));
    }

    @Test
    public void resolvesLegacyNameToCurrentSetting() throws Exception {
        remaps().put("legacy.android.id", "unique.android.id");
        indexSetting("legacy.android.id", "PrivacyEx.AndroidId");

        assertEquals(
                Arrays.asList("PrivacyEx.AndroidId"),
                HooksSettingsGlobal.getHookIdsFromIndex(Arrays.asList("unique.android.id")));
    }

    @Test
    public void mapsOneSettingToEveryRequiredHookWithoutDuplicates() throws Exception {
        indexSetting("hardware.cpu", "PrivacyEx.HardwareCpu");
        indexSetting("hardware.cpu", "PrivacyEx.HardwareInfo");
        indexSetting("hardware.cpu", "PrivacyEx.HardwareCpu");

        assertEquals(
                Arrays.asList("PrivacyEx.HardwareCpu", "PrivacyEx.HardwareInfo"),
                HooksSettingsGlobal.getHookIdsFromIndex(Arrays.asList("hardware.cpu")));
    }

    @Test
    public void resolvesMatchedHooksToTheirVisibleGroups() throws Exception {
        indexSetting("device.model", "PrivacyEx.ID.Build.MODEL");
        indexSetting("device.model", "PrivacyEx.Intercept.MODEL");
        groups().put("PrivacyEx.ID.Build.MODEL", "ID.Build");
        groups().put("PrivacyEx.ID.Build.BRAND", "ID.Build");
        groups().put("PrivacyEx.Intercept.MODEL", "Intercept.Properties");

        assertEquals(
                Arrays.asList("ID.Build"),
                visibleGroupsForSettings(Arrays.asList("device.model")));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> remaps() throws Exception {
        Field field = HooksSettingsGlobal.class.getDeclaredField("remappedSettings");
        field.setAccessible(true);
        return (Map<String, String>) field.get(null);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> groups() throws Exception {
        Field field = HooksSettingsGlobal.class.getDeclaredField("groups");
        field.setAccessible(true);
        return (Map<String, String>) field.get(null);
    }

    @SuppressWarnings("unchecked")
    private List<String> visibleGroupsForSettings(List<String> settingNames) throws Exception {
        Method method = HooksSettingsGlobal.class.getDeclaredMethod(
                "getVisibleGroupsForSettings",
                List.class);
        method.setAccessible(true);
        return (List<String>) method.invoke(null, settingNames);
    }

    private void indexSetting(String settingName, String hookId) throws Exception {
        Method method = HooksSettingsGlobal.class.getDeclaredMethod(
                "internalAdd",
                String.class,
                String.class);
        method.setAccessible(true);
        method.invoke(null, settingName, hookId);
    }
}
