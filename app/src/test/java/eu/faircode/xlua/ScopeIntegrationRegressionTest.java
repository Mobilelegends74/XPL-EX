package eu.faircode.xlua;

import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.*;

/** Wiring guards; queue behavior is tested separately, Binder needs a device test. */
public class ScopeIntegrationRegressionTest {
    private String source(String relative) throws Exception {
        Path root = Paths.get("").toAbsolutePath();
        if (!Files.isDirectory(root.resolve("src"))) root = root.resolve("app");
        return new String(Files.readAllBytes(root.resolve("src/main/java/eu/faircode/xlua/" + relative)),
                java.nio.charset.StandardCharsets.UTF_8);
    }

    @Test public void usageReportCanOnlyUpdateAndNeverResurrectAssignments() throws Exception {
        String text = source("x/xlua/commands/call/ReportCommand.java");
        assertFalse(text.contains("execute_one_locked_name"));
        assertFalse(text.contains("new AssignmentPacket()"));
        assertTrue(text.contains("database.update(AssignmentPacket.TABLE_NAME"));
        assertTrue(text.contains(".whereIdentity(userId, pkg)"));
        assertTrue(text.contains(".whereColumn(AssignmentPacket.FIELD_HOOK, hookId)"));
    }

    @Test public void masterOffClearsPersistedRowsEvenForUnavailableCollections() throws Exception {
        String adapter = source("AdapterApp.java");
        assertTrue(adapter.contains("groupName == null && !assign"));
        assertTrue(adapter.contains("AssignmentsPacket.clearAll(app.uid, pkgName"));
        String api = source("x/xlua/hook/AssignmentApi.java");
        assertTrue(api.contains("packet.clearAll"));
        assertTrue(api.contains("packet.isAction(ActionFlag.DELETE)"));
    }

    @Test public void scopeRemovalIsVerifiedAndDiagnosticBridgeIsRestricted() throws Exception {
        String scope = source("xposed/api101/LsposedScope.java");
        assertTrue(scope.contains("stillPresent="));
        assertTrue(scope.contains("callback timeout"));
        assertTrue(scope.contains("\"scopeDiagnostic\""));
        assertTrue(source("x/xlua/commands/call/ScopeDiagnosticCommand.java")
                .contains("requiresPermissionCheck = true"));
    }
}
