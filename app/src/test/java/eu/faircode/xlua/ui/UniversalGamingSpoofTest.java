package eu.faircode.xlua.ui;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UniversalGamingSpoofTest {
    @Test
    public void includesDeviceIdentityGroups() {
        assertTrue(UniversalGamingSpoof.includesGroup("ID.Build"));
        assertTrue(UniversalGamingSpoof.includesGroup("ID.Unique"));
        assertTrue(UniversalGamingSpoof.includesGroup("Device.ID.OnePlus"));
        assertTrue(UniversalGamingSpoof.includesGroup("Hardware.Spoof.GPU"));
        assertTrue(UniversalGamingSpoof.includesGroup("Network.Wifi.Information.Spoof"));
        assertTrue(UniversalGamingSpoof.includesGroup("Apps.Spoof.List.Ex"));
        assertTrue(UniversalGamingSpoof.includesGroup("Spoof.SOC"));
    }

    @Test
    public void excludesUnrelatedPrivacyAndLocationGroups() {
        assertFalse(UniversalGamingSpoof.includesGroup("Get.Contacts"));
        assertFalse(UniversalGamingSpoof.includesGroup("Record.Audio"));
        assertFalse(UniversalGamingSpoof.includesGroup("Spoof.Location"));
        assertFalse(UniversalGamingSpoof.includesGroup("Hide.VPN.State"));
        assertFalse(UniversalGamingSpoof.includesGroup("Use.Camera"));
    }

    @Test
    public void recognizesOnlyVirtualMasterGroup() {
        assertTrue(UniversalGamingSpoof.isVirtualGroup("UniversalGaming.Spoof.Device"));
        assertFalse(UniversalGamingSpoof.isVirtualGroup("Spoof.Device"));
    }
}
