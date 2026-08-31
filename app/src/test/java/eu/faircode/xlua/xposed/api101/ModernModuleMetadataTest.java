package eu.faircode.xlua.xposed.api101;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ModernModuleMetadataTest {
    @Test
    public void declaresOnlyModernApi101EntryPoint() throws Exception {
        assertEquals("eu.faircode.xlua.xposed.api101.ModernEntryPoint",
                text("src/main/resources/META-INF/xposed/java_init.list"));

        String properties = text("src/main/resources/META-INF/xposed/module.prop");
        assertTrue(properties.contains("minApiVersion=101"));
        assertTrue(properties.contains("targetApiVersion=101"));
        assertTrue(properties.contains("staticScope=false"));
        assertTrue(properties.contains("exceptionMode=protective"));

        File legacyEntry = file("src/main/assets/xposed_init");
        assertFalse("Legacy Xposed entry point must not be packaged", legacyEntry.exists());
    }

    @Test
    public void recommendsModernSystemScopeButAllowsUserApps() throws Exception {
        List<String> scopes = Arrays.asList(
                text("src/main/resources/META-INF/xposed/scope.list").split("\\R"));
        assertEquals(Arrays.asList("system"), scopes);
    }

    private static String text(String relativePath) throws Exception {
        return new String(Files.readAllBytes(file(relativePath).toPath()),
                StandardCharsets.UTF_8).trim();
    }

    private static File file(String relativePath) {
        File result = new File(relativePath);
        if (!result.exists())
            result = new File("app/" + relativePath);
        return result;
    }
}
