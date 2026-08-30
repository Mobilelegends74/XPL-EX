package eu.faircode.xlua.x.runtime;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AndroidVersionSpooferTest {
    @Test
    public void usesExactFirmwareAndLaunchVersions() {
        Map<String, String> settings = new HashMap<>();
        settings.put("android.build.fingerprint",
                "Lenovo/zippo/zippo:11/RKQ1.200928.002/12.5.365_210831:user/release-keys");
        settings.put("android.build.version", "15");
        settings.put("android.build.version.sdk", "35");
        settings.put("android.build.version.min.sdk", "30");

        AndroidVersionSpoofer.VersionValues values =
                AndroidVersionSpoofer.VersionValues.from(settings);

        assertEquals("11", values.currentRelease);
        assertEquals(30, values.currentApi);
        assertEquals("11", values.firstRelease);
        assertEquals(30, values.firstApi);
        assertEquals("30", values.forProperty("ro.build.version.sdk"));
        assertEquals("30", values.forProperty("ro.product.first_api_level"));
        assertEquals("11", values.forProperty("ro.build.version.release"));
        assertNull(values.forProperty("ro.build.version.min_supported_target_sdk"));
    }
}
