package eu.faircode.xlua;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Guards the UI semantics requested in Mobilelegends74/XPL-EX issue #2. */
public class FingerprintIndicatorRegressionTest {
    @Test
    public void fingerprintRemainsSelectiveAndHelpHasItsOwnAction() throws Exception {
        String stats = source("src/main/java/eu/faircode/xlua/x/xlua/settings/GroupStats.java");
        String containers = source("src/main/java/eu/faircode/xlua/x/ui/adapters/settings/ContainersListManager.java");
        String groups = source("src/main/java/eu/faircode/xlua/x/ui/adapters/settings/OptimizedSettingGroupAdapter.java");
        String containerLayout = source("src/main/res/layout/settings_ex_item_container.xml");
        String groupLayout = source("src/main/res/layout/settings_ex_group.xml");

        assertTrue(stats.contains("else if(isSettingUnique(name))"));
        assertTrue(stats.contains("ivWarning.setVisibility(View.GONE)"));
        assertFalse(stats.contains("updateInfoIv("));

        assertTrue(containerLayout.contains("@+id/ivSettingInfo"));
        assertTrue(containerLayout.contains("@drawable/ic_question_square18"));
        assertTrue(containers.contains("case R.id.ivSettingInfo:"));
        assertTrue(containers.contains("SettingValueInfo.getMessage("));

        assertTrue(groupLayout.contains("@+id/ivGroupInfo"));
        assertTrue(groups.contains("case R.id.ivGroupInfo:"));
        assertTrue(groups.contains("SettingGroupInfo.getMessage("));
        assertTrue(groups.contains(".updateIv(binding.ivActionNeeded)"));
    }

    private static String source(String relativePath) throws Exception {
        File file = new File(relativePath);
        if (!file.exists())
            file = new File("app/" + relativePath);
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
