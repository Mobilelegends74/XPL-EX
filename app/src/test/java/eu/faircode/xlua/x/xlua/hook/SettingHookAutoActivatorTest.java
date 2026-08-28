package eu.faircode.xlua.x.xlua.hook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class SettingHookAutoActivatorTest {
    @Test
    public void detectsOnlyDeviceIdentityChangesForManufacturerCleanup() {
        assertTrue(SettingHookAutoActivator.changesDeviceIdentity(
                Arrays.asList("device.model", "device.brand")));
        assertFalse(SettingHookAutoActivator.changesDeviceIdentity(
                Arrays.asList("battery.charge.percent", "display.width")));
        assertFalse(SettingHookAutoActivator.changesDeviceIdentity(Collections.emptyList()));
    }
    @Test
    public void returnsOnlyMissingHooksAndPreservesDependencyOrder() {
        assertEquals(
                Arrays.asList("PrivacyEx.Device", "PrivacyEx.Unique"),
                SettingHookAutoActivator.getMissingHookIds(
                        Arrays.asList(
                                "PrivacyEx.Device",
                                "PrivacyEx.Hardware",
                                "PrivacyEx.Device",
                                "PrivacyEx.Unique"),
                        Collections.singletonList("PrivacyEx.Hardware")));
    }

    @Test
    public void returnsEmptyWhenEveryRequiredHookIsAlreadyAssigned() {
        assertEquals(
                Collections.emptyList(),
                SettingHookAutoActivator.getMissingHookIds(
                        Arrays.asList("PrivacyEx.Device", "PrivacyEx.Hardware"),
                        Arrays.asList("PrivacyEx.Device", "PrivacyEx.Hardware")));
    }
}
