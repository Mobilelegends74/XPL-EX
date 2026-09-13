package eu.faircode.xlua;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Guards the Hooks editor navigation fix requested in Mobilelegends74/XPL-EX issue #4. */
public class HookEditorNavigationRegressionTest {
    @Test
    public void rowDelegatesEditingToHooksFragmentLifecycle() throws Exception {
        String adapter = source("src/main/java/eu/faircode/xlua/x/ui/adapters/hooks/HookAdapter.java");
        String fragment = source("src/main/java/eu/faircode/xlua/x/ui/fragments/HooksExFragment.java");
        String dialog = source("src/main/java/eu/faircode/xlua/x/ui/dialogs/HookEditDialog.java");
        String styles = source("src/main/res/values/styles.xml");

        assertTrue(adapter.contains("hookEditRequested.onHookEditRequested(source)"));
        assertFalse(adapter.contains("HookEditDialog.create()"));

        assertTrue(fragment.contains("this::showHookEditor"));
        assertTrue(fragment.contains("getChildFragmentManager().isStateSaved()"));
        assertTrue(fragment.contains("findFragmentByTag(HOOK_EDIT_DIALOG_TAG)"));
        assertTrue(fragment.contains(".setHook(XHook.copy(source))"));
        assertTrue(fragment.contains(".show(getChildFragmentManager(), HOOK_EDIT_DIALOG_TAG)"));

        assertTrue(dialog.contains("new ContextThemeWrapper(requireContext(), resolveEditorTheme())"));
        assertTrue(dialog.contains("LayoutInflater.from(themedContext).inflate(R.layout.define, null)"));
        assertTrue(styles.contains("Theme.MaterialComponents.Light.Dialog.Alert"));
        assertTrue(styles.contains("Theme.MaterialComponents.Dialog.Alert"));
    }

    private static String source(String relativePath) throws Exception {
        File file = new File(relativePath);
        if (!file.exists())
            file = new File("app/" + relativePath);
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
