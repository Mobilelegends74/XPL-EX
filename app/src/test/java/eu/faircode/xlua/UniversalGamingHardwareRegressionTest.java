package eu.faircode.xlua;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import eu.faircode.xlua.ui.UniversalGamingSpoof;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Guards the hardware profile activation path reproduced by the 1.6.0 LSPosed log. */
public class UniversalGamingHardwareRegressionTest {
    @Test
    public void hardwareCollectionIsRequiredWithoutEnablingUnrelatedOptionalCollections() {
        assertTrue(UniversalGamingSpoof.requiresCollectionActivation("Hardware"));
        assertTrue(UniversalGamingSpoof.requiresCollectionActivation(" hardware "));
        assertFalse(UniversalGamingSpoof.requiresCollectionActivation("Advanced"));
        assertFalse(UniversalGamingSpoof.requiresCollectionActivation("Applist"));
    }

    @Test
    public void masterSwitchActivatesHardwareBeforeAssigningHooks() throws Exception {
        String adapter = source("src/main/java/eu/faircode/xlua/AdapterApp.java");
        int ensureCall = adapter.indexOf("ensureUniversalGamingSpoofCollections(",
                adapter.indexOf("if (isUniversalGamingSpoof && assign)"));
        int assignmentLoop = adapter.indexOf("for (XHook hook : hooks)", ensureCall);

        assertTrue(ensureCall >= 0);
        assertTrue(assignmentLoop > ensureCall);
        assertTrue(adapter.contains("GetSettingExCommand.SETTING_COLLECTION"));
        assertTrue(adapter.contains("hook.isAvailable(packageName, null, false, true)"));
        assertTrue(adapter.contains("List<XHook> universalGamingSpoofHooks"));
    }

    @Test
    public void cpuAndGpuHooksConsumeSelectedProfileSettings() throws Exception {
        String cpu = asset("unique/hardware/cpu/hooks.json");
        String gpu = asset("unique/hardware/gpu/hooks.json");
        String profile = source("src/main/java/eu/faircode/xlua/x/xlua/settings/random/"
                + "randomizers/android_device/RandomDeviceProfile.java");

        assertTrue(cpu.contains("\"collection\": \"Hardware\""));
        assertTrue(cpu.contains("soc.cpu.info.dump"));
        assertTrue(cpu.contains("soc.cpu.processor.count"));
        assertTrue(gpu.contains("\"collection\": \"Hardware\""));
        assertTrue(gpu.contains("soc.gpu.open.gles.renderer"));
        assertTrue(profile.contains("SETTING_SOC_CPU_INFO_DUMP"));
        assertTrue(profile.contains("SETTING_SOC_GPU_OPEN_GLES_RENDERER"));
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
