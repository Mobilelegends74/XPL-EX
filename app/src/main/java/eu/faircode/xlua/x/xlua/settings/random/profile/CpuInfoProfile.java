package eu.faircode.xlua.x.xlua.settings.random.profile;

import java.util.Locale;

/** Builds a per-app /proc/cpuinfo identity from the selected real device profile. */
public final class CpuInfoProfile {
    private static final String ARM64_FEATURES =
            "fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics fphp asimdhp "
                    + "cpuid asimdrdm lrcpc dcpop asimddp";

    private CpuInfoProfile() { }

    public static String generate(DeviceProfile device) {
        if (device == null)
            throw new IllegalArgumentException("Device profile is required");

        String chipset = chipsetName(device.socModel);
        String implementer = isOryon(device.socModel) ? "0x51" : "0x41";
        String bogoMips = "Qualcomm".equalsIgnoreCase(device.socManufacturer) ? "38.40" : "26.00";
        StringBuilder cpuInfo = new StringBuilder(device.cpuCount * 260);

        for (int processor = 0; processor < device.cpuCount; processor++) {
            if (processor > 0)
                cpuInfo.append('\n');
            cpuInfo.append("processor\t: ").append(processor).append('\n')
                    .append("model name\t: ").append(chipset).append('\n')
                    .append("BogoMIPS\t: ").append(bogoMips).append('\n')
                    .append("Features\t: ").append(ARM64_FEATURES).append('\n')
                    .append("CPU implementer\t: ").append(implementer).append('\n')
                    .append("CPU architecture: 8\n");
        }

        String hardwareVendor = "Qualcomm".equalsIgnoreCase(device.socManufacturer)
                ? "Qualcomm Technologies, Inc"
                : device.socManufacturer;
        cpuInfo.append('\n')
                .append("Hardware\t: ").append(hardwareVendor).append(' ').append(device.socModel).append('\n')
                .append("Processor\t: ").append(chipset).append('\n');
        return cpuInfo.toString();
    }

    public static String chipsetName(String socModel) {
        String model = socModel == null ? "" : socModel.trim().toUpperCase(Locale.ROOT);
        if (model.startsWith("TENSOR G1")) return "Google Tensor G1";
        if (model.startsWith("TENSOR G2")) return "Google Tensor G2";
        if (model.startsWith("TENSOR G3")) return "Google Tensor G3";
        if (model.startsWith("TENSOR G4")) return "Google Tensor G4";
        if (model.startsWith("SM8150")) return "Snapdragon 855";
        if (model.startsWith("SM8250")) return "Snapdragon 865/870";
        if (model.startsWith("SM8350")) return "Snapdragon 888";
        if (model.startsWith("SM8450")) return "Snapdragon 8 Gen 1";
        if (model.startsWith("SM8475")) return "Snapdragon 8+ Gen 1";
        if (model.startsWith("SM8550")) return "Snapdragon 8 Gen 2";
        if (model.startsWith("SM8650")) return "Snapdragon 8 Gen 3";
        if (model.startsWith("SM8735")) return "Snapdragon 8s Gen 4";
        if (model.startsWith("SM8750")) return "Snapdragon 8 Elite";
        if (model.startsWith("SM8845")) return "Snapdragon 8s Gen 5";
        if (model.startsWith("SM8850")) return "Snapdragon 8 Elite Gen 5";
        throw new IllegalArgumentException("Unsupported SoC model: " + socModel);
    }

    private static boolean isOryon(String socModel) {
        String model = socModel == null ? "" : socModel.trim().toUpperCase(Locale.ROOT);
        return model.startsWith("SM8750") || model.startsWith("SM8850");
    }
}
