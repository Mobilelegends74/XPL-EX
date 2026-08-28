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
            "network.information.spoof",
            "network.spoof.wifi.list",
            "network.wifi.information.spoof",
            "settings.spoof",
            "spoof.accounts",
            "spoof.battery",
            "spoof.display",
            "spoof.features",
            "spoof.language",
            "spoof.soc",
            "spoof.status.cell",
            "spoof.telephony",
            "spoof.timezone",
            "spoof.useragent",
            "storage.spoof.size",
            "user.creation.time"
    ));

    private UniversalGamingSpoof() { }

    public static boolean isVirtualGroup(String groupName) {
        return GROUP_NAME.equalsIgnoreCase(groupName);
    }

    public static boolean includesGroup(String groupName) {
        if (groupName == null)
            return false;

        String normalized = groupName.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("apps.spoof.")
                || normalized.startsWith("device.id.")
                || normalized.startsWith("hardware.spoof.")
                || INCLUDED_GROUPS.contains(normalized);
    }
}
