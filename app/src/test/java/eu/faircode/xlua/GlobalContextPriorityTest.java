package eu.faircode.xlua;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import eu.faircode.xlua.x.ui.fragments.GlobalContextSelectionPolicy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Guards the Global Context priority requested in Mobilelegends74/XPL-EX issue #6. */
public class GlobalContextPriorityTest {
    @Test
    public void savedGlobalTemplateBlocksHookDerivedSelections() {
        boolean hasTemplate = GlobalContextSelectionPolicy.hasUserDefinedTemplate(
                Arrays.asList("device.name", "android.id"));

        assertTrue(hasTemplate);
        assertFalse(GlobalContextSelectionPolicy.shouldAutoSelectAssignedSettings(
                false,
                hasTemplate));
    }

    @Test
    public void hookDerivedSelectionsRemainFallbackWhenNoTemplateExists() {
        boolean hasTemplate = GlobalContextSelectionPolicy.hasUserDefinedTemplate(
                Collections.emptyList());

        assertFalse(hasTemplate);
        assertTrue(GlobalContextSelectionPolicy.shouldAutoSelectAssignedSettings(
                false,
                hasTemplate));
    }

    @Test
    public void explicitlySavedEmptyTemplateStillBlocksHookDerivedSelections() {
        boolean hasTemplate = GlobalContextSelectionPolicy.hasUserDefinedTemplate(
                true,
                Collections.emptyList());

        assertTrue(hasTemplate);
        assertFalse(GlobalContextSelectionPolicy.shouldAutoSelectAssignedSettings(
                false,
                hasTemplate));
    }

    @Test
    public void globalEditorNeverUsesPerAppHookPattern() {
        assertFalse(GlobalContextSelectionPolicy.shouldAutoSelectAssignedSettings(
                true,
                false));
    }

    @Test
    public void perAppScreenConsultsGlobalTemplateBeforeHookPattern() throws Exception {
        String fragment = source("src/main/java/eu/faircode/xlua/x/ui/fragments/SettingExFragment.java");
        String utils = source("src/main/java/eu/faircode/xlua/x/ui/fragments/SettingFragmentUtils.java");
        String prefs = source("src/main/java/eu/faircode/xlua/x/data/PrefManager.java");

        int policyCheck = fragment.indexOf(
                "GlobalContextSelectionPolicy.shouldAutoSelectAssignedSettings(");
        int hookPattern = fragment.indexOf("sharedRegistry.selectAssignedHookSettings(");

        assertTrue(fragment.contains(
                "hasUserDefinedGlobalTemplate = SettingFragmentUtils.initializeFragment("));
        assertTrue(policyCheck >= 0);
        assertTrue(hookPattern > policyCheck);
        assertTrue(utils.contains(
                "GlobalContextSelectionPolicy.hasUserDefinedTemplate(globalChecked)"));
        assertTrue(utils.contains(
                "PrefManager.SETTING_SETTINGS_GLOBAL_TEMPLATE_DEFINED"));
        assertTrue(fragment.contains("clearGlobalCheckedTemplate()"));
        assertTrue(prefs.contains("SETTING_SETTINGS_GLOBAL_TEMPLATE_DEFINED"));
    }

    private static String source(String relativePath) throws Exception {
        File file = new File(relativePath);
        if (!file.exists())
            file = new File("app/" + relativePath);
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
