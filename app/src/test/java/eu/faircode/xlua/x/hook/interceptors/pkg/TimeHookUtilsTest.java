package eu.faircode.xlua.x.hook.interceptors.pkg;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimeHookUtilsTest {
    @Test
    public void recognizesGooglePlayServicesFilesWhoseTimestampsMustBePreserved() {
        assertTrue(TimeHookUtils.isGMSFile(
                "/data/user_de/0/com.google.android.gms/app_chimera/m/module.apk"));
        assertTrue(TimeHookUtils.isGMSFile(
                "/data/app/~~token/com.google.android.gms-token/base.apk"));
        assertTrue(TimeHookUtils.isGMSFile(
                "/data/misc/profiles/cur/0/com.google.android.gms/primary.prof"));
        assertTrue(TimeHookUtils.isGMSFile(
                "/data/dalvik-cache/arm64/data@app@com.google.android.gms-token@base.apk@classes.dex"));
        assertTrue(TimeHookUtils.isGMSFile(
                "/system/framework/com.android.location.provider.jar"));
    }

    @Test
    public void doesNotDisableTimestampSpoofingForUnrelatedFiles() {
        assertFalse(TimeHookUtils.isGMSFile(null));
        assertFalse(TimeHookUtils.isGMSFile(""));
        assertFalse(TimeHookUtils.isGMSFile(
                "/data/app/~~token/com.example.app-token/base.apk"));
        assertFalse(TimeHookUtils.isGMSFile(
                "/data/user_de/0/com.example.app/files/cache"));
    }
}
