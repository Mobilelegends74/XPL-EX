package eu.faircode.xlua;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ModuleUpdateCheckerTest {
    @Test
    public void comparesSemanticReleaseVersions() {
        assertTrue(ModuleUpdateChecker.compareVersions("1.6.1", "1.6.0") > 0);
        assertTrue(ModuleUpdateChecker.compareVersions("1.10.0", "1.6.9") > 0);
        assertEquals(0, ModuleUpdateChecker.compareVersions("1.6", "1.6.0"));
    }

    @Test
    public void selectsApkFromProjectRelease() throws Exception {
        ModuleUpdateChecker.RemoteRelease release = ModuleUpdateChecker.parseRelease(
                "{\"tag_name\":\"XPL-EX-NEXT-v1.6.1\"," +
                        "\"html_url\":\"https://github.com/release\"," +
                        "\"assets\":[{\"name\":\"checksum.sha256\"," +
                        "\"browser_download_url\":\"https://github.com/checksum\"}," +
                        "{\"name\":\"XPL-EX-NEXT-1.6.1.apk\"," +
                        "\"browser_download_url\":\"https://github.com/module.apk\"}]}"
        );

        assertNotNull(release);
        assertEquals("1.6.1", release.versionName);
        assertEquals("https://github.com/module.apk", release.updateUrl);
        assertNull(ModuleUpdateChecker.parseRelease(
                "{\"tag_name\":\"unrelated-v1\",\"html_url\":\"https://github.com\"}"));
    }
}
