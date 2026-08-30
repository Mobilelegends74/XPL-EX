package eu.faircode.xlua.x.xlua.settings.random;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RandomizerSessionContextTest {
    @Test
    public void masterSpoofLeavesEntireZoneSectionUntouched() {
        assertFalse(RandomizerSessionContext.shouldRandomizeFromMasterSwitch("zone.country.name"));
        assertFalse(RandomizerSessionContext.shouldRandomizeFromMasterSwitch("zone.language.tag"));
        assertFalse(RandomizerSessionContext.shouldRandomizeFromMasterSwitch("zone.timezone.id"));
        assertTrue(RandomizerSessionContext.shouldRandomizeFromMasterSwitch("device.model"));
        assertTrue(RandomizerSessionContext.shouldRandomizeFromMasterSwitch("android.build.version"));
    }
}
