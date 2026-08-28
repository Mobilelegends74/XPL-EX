package eu.faircode.xlua.x.xlua.settings.random.profile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Model-specific values which must stay coherent with a selected device identity. */
public final class DeviceCharacteristics {
    private static final Map<String, DeviceCharacteristics> VALUES;

    public final int displayWidthPx;
    public final int displayHeightPx;
    public final int displayDensityDpi;
    public final int displayRefreshRateHz;
    public final int batteryCapacityMah;
    public final int ramGb;
    public final int cameraCount;
    public final int launchYear;

    static {
        Map<String, DeviceCharacteristics> values = new LinkedHashMap<>();
        // Google
        put(values, "google_pixel_6", 1080, 2400, 420, 90, 4614, 8, 3, 2021);
        put(values, "google_pixel_6_pro", 1440, 3120, 560, 120, 5003, 12, 4, 2021);
        put(values, "google_pixel_7", 1080, 2400, 420, 90, 4355, 8, 3, 2022);
        put(values, "google_pixel_7_pro", 1440, 3120, 560, 120, 5000, 12, 4, 2022);
        put(values, "google_pixel_8", 1080, 2400, 420, 120, 4575, 8, 3, 2023);
        put(values, "google_pixel_8_pro", 1344, 2992, 560, 120, 5050, 12, 4, 2023);
        put(values, "google_pixel_9", 1080, 2424, 420, 120, 4700, 12, 3, 2024);
        put(values, "google_pixel_9_pro_xl", 1344, 2992, 560, 120, 5060, 16, 4, 2024);
        // Xiaomi and POCO
        put(values, "xiaomi_13_global", 1080, 2400, 440, 120, 4500, 8, 4, 2022);
        put(values, "xiaomi_13_pro_global", 1440, 3200, 560, 120, 4820, 12, 4, 2022);
        put(values, "xiaomi_14_ultra_global", 1440, 3200, 560, 120, 5000, 16, 5, 2024);
        put(values, "xiaomi_15_cn", 1200, 2670, 480, 120, 5400, 12, 4, 2024);
        put(values, "xiaomi_14_cn", 1200, 2670, 480, 120, 4610, 12, 4, 2023);
        put(values, "poco_f6_pro_global", 1440, 3200, 560, 120, 5000, 12, 4, 2024);
        put(values, "poco_f7_global", 1280, 2772, 480, 120, 6500, 12, 3, 2025);
        put(values, "poco_f7_pro_global", 1440, 3200, 560, 120, 6000, 12, 4, 2025);
        put(values, "poco_f7_ultra_eea", 1440, 3200, 560, 120, 5300, 16, 4, 2025);
        put(values, "poco_f8_ultra_global", 1200, 2608, 480, 120, 6500, 16, 4, 2025);
        // Nubia / REDMAGIC
        put(values, "nubia_redmagic_8_pro_global", 1116, 2480, 400, 120, 6000, 12, 3, 2022);
        put(values, "nubia_redmagic_9s_pro_eea", 1116, 2480, 400, 120, 6500, 16, 3, 2024);
        put(values, "nubia_redmagic_10_pro_global", 1216, 2688, 440, 144, 7050, 16, 3, 2024);
        put(values, "nubia_redmagic_11_pro_eea", 1216, 2688, 440, 144, 8000, 16, 3, 2025);
        put(values, "nubia_redmagic_9_pro", 1116, 2480, 400, 120, 6500, 12, 3, 2023);
        // OnePlus
        put(values, "oneplus_11_global", 1440, 3216, 560, 120, 5000, 16, 4, 2023);
        put(values, "oneplus_12_na", 1440, 3168, 560, 120, 5400, 16, 4, 2024);
        put(values, "oneplus_13_na", 1440, 3168, 560, 120, 6000, 16, 4, 2024);
        put(values, "oneplus_13r_global", 1264, 2780, 480, 120, 6000, 12, 4, 2025);
        put(values, "oneplus_open_na", 2268, 2440, 420, 120, 4805, 16, 5, 2023);
        // Samsung
        put(values, "samsung_galaxy_s23_global", 1080, 2340, 420, 120, 3900, 8, 4, 2023);
        put(values, "samsung_galaxy_s23_plus_global", 1080, 2340, 420, 120, 4700, 8, 4, 2023);
        put(values, "samsung_galaxy_s23_ultra_global", 1440, 3088, 560, 120, 5000, 12, 5, 2023);
        put(values, "samsung_galaxy_z_fold5_global", 1812, 2176, 420, 120, 4400, 12, 5, 2023);
        put(values, "samsung_galaxy_s24_ultra_global", 1440, 3120, 560, 120, 5000, 12, 5, 2024);

        // ASUS
        put(values, "asus_zenfone_10", 1080, 2400, 440, 144, 4300, 16, 3, 2023);
        put(values, "asus_rog_phone_7", 1080, 2448, 400, 165, 6000, 16, 4, 2023);
        put(values, "asus_rog_phone_8_pro", 1080, 2400, 400, 165, 5500, 16, 4, 2024);
        put(values, "asus_zenfone_11_ultra", 1080, 2400, 400, 144, 5500, 16, 4, 2024);
        put(values, "asus_rog_phone_9_pro", 1080, 2400, 400, 185, 5800, 16, 4, 2024);
        // Meizu
        put(values, "meizu_20", 1080, 2400, 400, 144, 4700, 12, 4, 2023);
        put(values, "meizu_20_pro", 1440, 3200, 560, 120, 5000, 12, 5, 2023);
        put(values, "meizu_20_infinity", 1368, 3192, 560, 120, 4800, 16, 4, 2023);
        put(values, "meizu_21", 1080, 2340, 400, 120, 4800, 12, 4, 2023);
        put(values, "meizu_21_pro", 1368, 3192, 560, 120, 5050, 16, 5, 2024);
        // vivo / iQOO
        put(values, "vivo_x90_pro_plus", 1440, 3200, 560, 120, 4700, 12, 5, 2022);
        put(values, "vivo_x100_ultra", 1440, 3200, 560, 120, 5500, 16, 5, 2024);
        put(values, "vivo_x200_ultra", 1440, 3168, 560, 120, 6000, 16, 5, 2025);
        put(values, "iqoo_12_global", 1260, 2800, 480, 144, 5000, 16, 4, 2023);
        put(values, "iqoo_13_global", 1440, 3168, 560, 144, 6150, 16, 4, 2024);
        // Lenovo phones (real ZUI / Legion identities, not Motorola aliases)
        put(values, "lenovo_legion_phone_duel", 1080, 2340, 420, 144, 5000, 16, 3, 2020);
        put(values, "lenovo_legion_phone_duel_2", 1080, 2460, 420, 144, 5500, 18, 3, 2021);
        put(values, "lenovo_legion_y90", 1080, 2460, 480, 144, 5600, 18, 3, 2022);
        put(values, "lenovo_legion_y70", 1080, 2400, 420, 144, 5100, 16, 4, 2022);
        put(values, "lenovo_z6_pro", 1080, 2340, 480, 60, 4000, 12, 5, 2019);
        // Lenovo-owned Motorola Mobility flagships
        put(values, "motorola_edge_40_pro", 1080, 2400, 400, 165, 4600, 12, 4, 2023);
        put(values, "motorola_edge_plus_2023", 1080, 2400, 400, 165, 5100, 8, 4, 2023);
        put(values, "motorola_moto_x40", 1080, 2400, 400, 165, 4600, 12, 4, 2022);
        put(values, "motorola_razr_60_ultra", 1224, 2992, 420, 165, 4700, 16, 4, 2025);
        put(values, "motorola_signature", 1264, 2780, 480, 165, 5200, 16, 4, 2026);
        VALUES = Collections.unmodifiableMap(values);
    }

    private DeviceCharacteristics(int displayWidthPx, int displayHeightPx, int displayDensityDpi,
                                  int displayRefreshRateHz, int batteryCapacityMah, int ramGb,
                                  int cameraCount, int launchYear) {
        this.displayWidthPx = displayWidthPx;
        this.displayHeightPx = displayHeightPx;
        this.displayDensityDpi = displayDensityDpi;
        this.displayRefreshRateHz = displayRefreshRateHz;
        this.batteryCapacityMah = batteryCapacityMah;
        this.ramGb = ramGb;
        this.cameraCount = cameraCount;
        this.launchYear = launchYear;
    }

    public static DeviceCharacteristics forProfile(String profileId) {
        return VALUES.get(profileId);
    }

    private static void put(Map<String, DeviceCharacteristics> values, String id,
                            int width, int height, int density, int refreshRate, int battery,
                            int ram, int cameras, int year) {
        values.put(id, new DeviceCharacteristics(
                width, height, density, refreshRate, battery, ram, cameras, year));
    }
}
