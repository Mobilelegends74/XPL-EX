package eu.faircode.xlua;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModuleIdentityTest {
    @Test
    public void appVersionAndBridgeProtocolAreIndependent() {
        assertEquals(166, ModuleIdentity.apkVersionCode());
        assertEquals("1.5.5", ModuleIdentity.bridgeProtocolVersion());
    }
}
