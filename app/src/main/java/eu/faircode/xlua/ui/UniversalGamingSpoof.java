package eu.faircode.xlua.ui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Defines the existing hook groups controlled by the virtual Spoof Device switch. */
public final class UniversalGamingSpoof {
    public static final String GROUP_NAME = "UniversalGaming.Spoof.Device";
    public static final String CATEGORY_TITLE = "Universal Gaming Spoof";

    private static final Set<String> INCLUDED_GROUPS = new HashSet<>(Arrays.asList(
            "ad.analytics.spoof",
            "devices.hide.driver.files",
            "devices.spoof.info",
            "id.boot.id",
            "id.build",
            "id.file.stat",
            "id.keyboard",
            "id.network",
            "id.network.linkproperties",
            "id.telephony",
            "id.unique",
            "id.uptime",
            "intercept.properties",
            "network.information.spoof",
            "network.spoof.wifi.list",
            "network.wifi.information.spoof",
            "settings.spoof",
            "spoof.accounts",
            "spoof.battery",
            "spoof.display",
            "spoof.features",
            "spoof.soc",
            "spoof.status.cell",
            "spoof.telephony",
            "spoof.useragent",
            "storage.spoof.size",
            "user.creation.time"
    ));

    /*
     * Only hardware hooks backed by the selected real-device profile belong in the master
     * switch. Hardware.Spoof.CPU now consumes the /proc/cpuinfo contents generated from the same
     * real-device profile. Hardware.Spoof.GPS.Model still needs a vendor-specific GNSS string
     * which is not published for every model, so it remains excluded.
     */
    private static final Set<String> PROFILE_BACKED_HARDWARE_GROUPS = new HashSet<>(Arrays.asList(
            "hardware.spoof.cpu.ex",
            "hardware.spoof.cpu",
            "hardware.spoof.gpu",
            "hardware.spoof.camera.count",
            "hardware.spoof.memory"
    ));

    private UniversalGamingSpoof() { }

    /** Groups that must remain under explicit user control. */
    public static boolean isEnvironmentGroup(String groupName) {
        if (groupName == null)
            return false;
        String normalized = groupName.trim().toLowerCase(Locale.ROOT);
        return "spoof.language".equals(normalized)
                || "spoof.timezone".equals(normalized)
                || normalized.contains("zoneid");
    }

    public static boolean isVirtualGroup(String groupName) {
        return GROUP_NAME.equalsIgnoreCase(groupName);
    }

    public static boolean includesGroup(String groupName) {
        if (groupName == null)
            return false;

        String normalized = groupName.trim().toLowerCase(Locale.ROOT);
        if (isEnvironmentGroup(normalized))
            return false;
        if ("apps.spoof.timestamps".equals(normalized))
            return false;
        return normalized.startsWith("apps.spoof.")
                || normalized.startsWith("device.id.")
                || PROFILE_BACKED_HARDWARE_GROUPS.contains(normalized)
                || INCLUDED_GROUPS.contains(normalized);
    }

    public static boolean includesGroup(String groupName, String brand, String manufacturer) {
        if (groupName == null)
            return false;

        String normalized = groupName.trim().toLowerCase(Locale.ROOT);
        if (!isManufacturerGroup(normalized))
            return includesGroup(groupName);

        String expected = manufacturerGroupFor(brand, manufacturer);
        return expected != null && expected.equalsIgnoreCase(groupName);
    }

    public static boolean isManufacturerGroup(String groupName) {
        return groupName != null
                && groupName.trim().toLowerCase(Locale.ROOT).startsWith("device.id.");
    }

    public static String manufacturerGroupFor(String brand, String manufacturer) {
        String identity = ((brand == null ? "" : brand) + " "
                + (manufacturer == null ? "" : manufacturer)).toLowerCase(Locale.ROOT);

        if (containsAny(identity, "lenovo"))
            return "Device.ID.Lenovo";
        if (containsAny(identity, "asus"))
            return "Device.ID.Asus";
        if (containsAny(identity, "meizu"))
            return "Device.ID.Meizu";
        if (containsAny(identity, "vivo", "iqoo"))
            return "Device.ID.Vivo";
        if (containsAny(identity, "oneplus"))
            return "Device.ID.OnePlus";
        if (containsAny(identity, "samsung"))
            return "Device.ID.Samsung";
        if (containsAny(identity, "xiaomi", "redmi", "poco"))
            return "Device.ID.Xiaomi";
        return null;
    }

    private static boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates)
            if (value.contains(candidate))
                return true;
        return false;
    }
}
