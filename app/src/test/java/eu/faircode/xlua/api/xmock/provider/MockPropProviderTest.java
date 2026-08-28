package eu.faircode.xlua.api.xmock.provider;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MockPropProviderTest {
    @Test
    public void legacySocAliasesMapToProfileSettings() {
        assertEquals("soc.board.model",
                MockPropProvider.canonicalSettingName("soc.hardware.model"));
        assertEquals("soc.board.config.code.name",
                MockPropProvider.canonicalSettingName("soc.hardware.config.code.name"));
        assertEquals("soc.board.manufacturer",
                MockPropProvider.canonicalSettingName("soc.hardware.manufacturer"));
        assertEquals("soc.board.manufacturer.id",
                MockPropProvider.canonicalSettingName("soc.hardware.manufacturer.id"));
    }

    @Test
    public void unrelatedSettingsRemainUnchanged() {
        assertEquals("device.model",
                MockPropProvider.canonicalSettingName("device.model"));
    }
}
