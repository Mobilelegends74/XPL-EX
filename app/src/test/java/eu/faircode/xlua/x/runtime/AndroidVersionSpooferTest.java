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

    @Test
    public void normalizesStaleCurrentVersionFromSelectedFirmware() {
        Map<String, String> settings = new HashMap<>();
        settings.put("android.build.fingerprint",
                "google/raven/raven:14/AP1A.240305.019.A1/11445699:user/release-keys");
        settings.put("android.build.version", "15");
        settings.put("android.build.version.sdk", "35");
        settings.put("android.build.version.min.sdk", "31");

        AndroidVersionSpoofer.VersionValues values =
                AndroidVersionSpoofer.VersionValues.from(settings);
        values.normalize(settings);

        assertEquals("14", settings.get("android.build.version"));
        assertEquals("34", settings.get("android.build.version.sdk"));
        assertEquals("14", values.currentRelease);
        assertEquals(34, values.currentApi);
        assertEquals("12", values.firstRelease);
        assertEquals(31, values.firstApi);
    }

    @Test
    public void derivesEverySupportedCurrentReleaseFromFirmwareFingerprint() {
        String[] releases = {"9", "10", "11", "12", "13", "14", "15", "16"};
        int[] apis = {28, 29, 30, 31, 33, 34, 35, 36};

        for (int index = 0; index < releases.length; index++) {
            Map<String, String> settings = new HashMap<>();
            settings.put("android.build.fingerprint",
                    "vendor/product/device:" + releases[index]
                            + "/BUILD/123456:user/release-keys");
            settings.put("android.build.version", "15");
            settings.put("android.build.version.sdk", "35");
            settings.put("android.build.version.min.sdk", "28");

            AndroidVersionSpoofer.VersionValues values =
                    AndroidVersionSpoofer.VersionValues.from(settings);

            assertEquals(releases[index], values.currentRelease);
            assertEquals(apis[index], values.currentApi);
        }
    }
}
