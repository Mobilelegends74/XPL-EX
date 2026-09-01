package eu.faircode.xlua;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Guards fixes derived from the LSPosed API101 runtime log. */
public class RuntimeLogRegressionTest {
    @Test
    public void featureScriptsDoNotCallUnregisteredLuaHelpers() throws Exception {
        String hasFeature = asset("features/spoof_packagemanager_hassystemfeature.lua");
        String allFeatures = asset("features/spoof_packagemanager_getsystemavailablefeatures.lua");

        assertFalse(hasFeature.contains("printInetAddress"));
        assertFalse(hasFeature.contains("param:setResult(false)"));
        assertFalse(allFeatures.contains("printInetAddress"));
        assertFalse(allFeatures.contains("param:setResult(fake)"));
        assertFalse(allFeatures.contains("newInstance(featureType, 1)"));
        assertTrue(hasFeature.contains("return false"));
        assertTrue(allFeatures.contains("return false"));
    }

    @Test
    public void staleFeatureHooksAreBypassedBeforeResolution() throws Exception {
        String core = source("src/main/java/eu/faircode/xlua/x/hook/HookCore.java");
        int loop = core.indexOf("for(XHook hook");
        int bypass = core.indexOf("mustPreserveSystemFeatures(hook)", loop);
        int resolve = core.indexOf("HookResolver.resolveHook", loop);

        assertTrue(loop >= 0);
        assertTrue(bypass > loop);
        assertTrue(resolve > bypass);
        assertTrue(core.contains("PackageManager.hasSystemFeature(String)"));
        assertTrue(core.contains("PackageManager.hasSystemFeature(String, int)"));
        assertTrue(core.contains("PackageManager.getSystemAvailableFeatures"));

        String metadata = asset("features/hooks.json");
        assertFalse(metadata.contains("\"version\": 1"));
    }

    @Test
    public void hookMetadataContainsOnlyJavaParameterTypes() throws Exception {
        String networkHooks = asset("unique/network/parcels/hooks.json");
        assertFalse(networkHooks.contains("\"java.lang.String\" , \"network.parent.control.isp\""));

        String memoryHooks = asset("unique/hardware/memory/hooks.json");
        assertFalse(memoryHooks.contains("\".hardware.memory.total\""));
    }

    @Test
    public void inlinedHooksAreAttachedBeforeLuaCompilation() throws Exception {
        String source = source("src/main/java/eu/faircode/xlua/x/hook/HookCore.java");
        int memberBranch = source.indexOf("definition instanceof HookDefinitionMember");
        int inlineAttach = source.indexOf("UpTimeHooks.attach(hook, member)", memberBranch);
        int luaCompile = source.indexOf("XHookUtil.compileScript(scriptPrototype, hook)", inlineAttach);

        assertTrue(memberBranch >= 0);
        assertTrue(inlineAttach > memberBranch);
        assertTrue(luaCompile > inlineAttach);
    }

    @Test
    public void randomizerCacheRejectsMarkerClassesBeforeInstantiation() throws Exception {
        String source = source("src/main/java/eu/faircode/xlua/x/xlua/settings/random/randomizers/RandomizersCache.java");
        assertTrue(source.contains("IRandomizer.class.isAssignableFrom(classType)"));
        assertTrue(source.contains("getDeclaredConstructor().newInstance()"));
    }

    @Test
    public void debugFormatStringsHaveMatchingPlaceholders() throws Exception {
        String source = source("src/main/java/eu/faircode/xlua/x/xlua/settings/SettingsFactory.java");
        assertFalse(source.contains("Children (%)"));
        assertFalse(source.contains("From Parent [%s][%s] Total Container Count"));
    }

    @Test
    public void absentLegacyGroupsTableIsNotQueried() throws Exception {
        String source = source("src/main/java/eu/faircode/xlua/x/xlua/hook/AssignmentApi.java");
        assertTrue(source.contains("!packet.isAction(ActionFlag.DELETE) && database.hasTable(GroupPacket.TABLE_NAME)"));
    }

    @Test
    public void missingOptionalFieldsAreHandledWithoutRuntimeErrors() throws Exception {
        String source = source("src/main/java/eu/faircode/xlua/x/runtime/reflect/DynamicField.java");
        assertTrue(source.contains("if (mField == null)\n            return this;"));
        assertTrue(source.contains("if (mField == null)\n            return false;"));
    }

    private static String asset(String relativePath) throws Exception {
        return source("src/main/assets/" + relativePath);
    }

    private static String source(String relativePath) throws Exception {
        File file = new File(relativePath);
        if (!file.exists())
            file = new File("app/" + relativePath);
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
