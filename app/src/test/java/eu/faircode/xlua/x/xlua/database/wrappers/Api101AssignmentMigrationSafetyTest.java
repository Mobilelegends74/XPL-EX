package eu.faircode.xlua.x.xlua.database.wrappers;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Api101AssignmentMigrationSafetyTest {
    @Test
    public void migrationTargetsExistingAssignmentTableWithoutRewritingHookSchema()
            throws Exception {
        String source = source("src/main/java/eu/faircode/xlua/x/xlua/database/wrappers/XLuaDatabaseHelp.java");

        assertTrue(source.contains("getAssignmentTable(database)"));
        assertTrue(source.contains("database.hasTable(\"assignments\")"));
        assertTrue(source.contains("database.hasTable(\"assignment\")"));
        assertTrue(source.contains("updateWithOnConflict"));

        // The failed attempt grouped/deleted hook definitions by id, which is
        // invalid for the legacy hook table and emptied the UI.
        assertFalse(source.contains("GROUP BY id"));
        assertFalse(source.contains("repairHookTable"));
    }

    @Test
    public void managerKeepsTheKnownWorkingHookInitializationOrder() throws Exception {
        String source = source("src/main/java/eu/faircode/xlua/x/xlua/database/wrappers/XLuaDatabaseManager.java");
        int legacyCheck = source.indexOf("ensureIsUpdated_legacy(db)");
        int hookLoad = source.indexOf("initializeFromJsons(context, db, true)");
        int currentUpdater = source.indexOf("DatabaseUpdater.ensureUpdated(context, db)");

        assertTrue(legacyCheck >= 0);
        assertTrue(hookLoad > legacyCheck);
        assertTrue(currentUpdater > hookLoad);
        assertFalse(source.contains("GlobalDatabaseResolver.ensureAssignments"));
    }

    private static String source(String relativePath) throws Exception {
        File file = new File(relativePath);
        if (!file.exists())
            file = new File("app/" + relativePath);
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
