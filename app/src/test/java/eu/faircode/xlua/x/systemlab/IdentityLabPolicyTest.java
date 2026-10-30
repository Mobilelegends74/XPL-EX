package eu.faircode.xlua.x.systemlab;

import org.junit.Test;
import static org.junit.Assert.*;

public final class IdentityLabPolicyTest {
    @Test public void matchesOnlySecureAndroidIdReads() {
        assertTrue(IdentityLabPolicy.isAndroidIdRead("GET_secure", "android_id"));
        assertFalse(IdentityLabPolicy.isAndroidIdRead("GET_secure", "bluetooth_name"));
        assertFalse(IdentityLabPolicy.isAndroidIdRead("PUT_secure", "android_id"));
        assertFalse(IdentityLabPolicy.isAndroidIdRead("GET_global", "android_id"));
        assertFalse(IdentityLabPolicy.isAndroidIdRead(null, "android_id"));
    }

    @Test public void refusesSystemOtherAndSharedUids() {
        assertTrue(IdentityLabPolicy.isLabCaller(10345,
                new String[]{"eu.faircode.xlua.identitylab"}));
        assertFalse(IdentityLabPolicy.isLabCaller(1000,
                new String[]{"eu.faircode.xlua.identitylab"}));
        assertFalse(IdentityLabPolicy.isLabCaller(10345,
                new String[]{"eu.faircode.xlua.identitylab", "other.package"}));
        assertFalse(IdentityLabPolicy.isLabCaller(10345,
                new String[]{"other.package"}));
        assertFalse(IdentityLabPolicy.isLabCaller(10345, null));
    }

    @Test public void validatesLabIdentifier() {
        assertTrue(IdentityLabPolicy.validId(IdentityLabPolicy.DEMO_ID));
        assertFalse(IdentityLabPolicy.validId("not-an-id"));
        assertFalse(IdentityLabPolicy.validId("1234"));
        assertFalse(IdentityLabPolicy.validId(null));
    }
}
