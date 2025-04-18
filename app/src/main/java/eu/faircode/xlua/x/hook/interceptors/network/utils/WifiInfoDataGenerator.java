package eu.faircode.xlua.x.hook.interceptors.network.utils;

public class WifiInfoDataGenerator {

    // Common 2.4 GHz WiFi channels (in MHz)
    private static final int[] CHANNELS_24GHZ = {
            2412, // Channel 1
            2417, // Channel 2
            2422, // Channel 3
            2427, // Channel 4
            2432, // Channel 5
            2437, // Channel 6
            2442, // Channel 7
            2447, // Channel 8
            2452, // Channel 9
            2457, // Channel 10
            2462, // Channel 11
            2467, // Channel 12
            2472  // Channel 13
    };

    // Common 5 GHz WiFi channels (in MHz)
    private static final int[] CHANNELS_5GHZ = {
            5180, // Channel 36
            5200, // Channel 40
            5220, // Channel 44
            5240, // Channel 48
            5260, // Channel 52
            5280, // Channel 56
            5300, // Channel 60
            5320, // Channel 64
            5500, // Channel 100
            5520, // Channel 104
            5540, // Channel 108
            5560, // Channel 112
            5580, // Channel 116
            5600, // Channel 120
            5620, // Channel 124
            5640, // Channel 128
            5660, // Channel 132
            5680, // Channel 136
            5700, // Channel 140
            5745, // Channel 149
            5765, // Channel 153
            5785, // Channel 157
            5805, // Channel 161
            5825  // Channel 165
    };

    // Common 6 GHz WiFi channels (in MHz) - For WiFi 6E
    private static final int[] CHANNELS_6GHZ = {
            5945, // Channel 1
            5965, // Channel 5
            5985, // Channel 9
            6005, // Channel 13
            6025, // Channel 17
            6045, // Channel 21
            6065, // Channel 25
            6085, // Channel 29
            6105, // Channel 33
            6125, // Channel 37
            6145, // Channel 41
            6165, // Channel 45
            6185, // Channel 49
            6205, // Channel 53
            6225, // Channel 57
            6245, // Channel 61
            6265, // Channel 65
            6285, // Channel 69
            6305, // Channel 73
            6325, // Channel 77
            6345, // Channel 81
            6365, // Channel 85
            6385, // Channel 89
            6405, // Channel 93
            6425, // Channel 97
    };

    /**
     * Generates a random WiFi frequency in MHz
     * @return A random WiFi frequency value in MHz
     */
    public static int generateRandomFrequency() {
        // Randomly choose a band (2.4 GHz, 5 GHz or 6 GHz)
        int band = (int)(Math.random() * 3);

        switch (band) {
            case 0:
                return getRandomElement(CHANNELS_24GHZ);
            case 1:
                return getRandomElement(CHANNELS_5GHZ);
            case 2:
                return getRandomElement(CHANNELS_6GHZ);
            default:
                return getRandomElement(CHANNELS_5GHZ);
        }
    }

    /**
     * Generates a random WiFi frequency in a specific band
     * @param band The frequency band (2.4GHz, 5GHz, or 6GHz)
     * @return A random WiFi frequency value in MHz for the specified band
     */
    public static int generateRandomFrequency(FrequencyBand band) {
        switch (band) {
            case BAND_24GHZ:
                return getRandomElement(CHANNELS_24GHZ);
            case BAND_5GHZ:
                return getRandomElement(CHANNELS_5GHZ);
            case BAND_6GHZ:
                return getRandomElement(CHANNELS_6GHZ);
            default:
                return generateRandomFrequency();
        }
    }

    /**
     * Helper method to get a random element from an array
     */
    private static int getRandomElement(int[] array) {
        int index = (int)(Math.random() * array.length);
        return array[index];
    }

    /**
     * Enum representing different WiFi frequency bands
     */
    public enum FrequencyBand {
        BAND_24GHZ,
        BAND_5GHZ,
        BAND_6GHZ
    }

    // Constants from WifiInfo
    private static final int INVALID_RSSI = -127;
    private static final int MAX_RSSI = 200;

    // Realistic RSSI ranges for WiFi
    private static final int EXCELLENT_MIN = -50;
    private static final int GOOD_MIN = -60;
    private static final int FAIR_MIN = -70;
    private static final int WEAK_MIN = -80;
    private static final int MIN_USABLE_RSSI = -90;

    /**
     * Generates a random RSSI value within valid bounds
     * @return a random RSSI value in dBm
     */
    public static int generateRandomRssi() {
        // Generate within realistic range of -90 to -30 dBm
        return generateRandomRssi(MIN_USABLE_RSSI, EXCELLENT_MIN + 20);
    }

    /**
     * Generates a random RSSI value within specified bounds
     * @param min Minimum RSSI value (will be clamped to INVALID_RSSI if too low)
     * @param max Maximum RSSI value (will be clamped to MAX_RSSI if too high)
     * @return a random RSSI value in dBm
     */
    public static int generateRandomRssi(int min, int max) {
        // Clamp to valid RSSI ranges
        min = Math.max(min, INVALID_RSSI);
        max = Math.min(max, MAX_RSSI);

        // Generate random value in range
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    /**
     * Generates a random RSSI value with a specified signal quality
     * @param quality The desired signal quality (EXCELLENT, GOOD, FAIR, WEAK, or POOR)
     * @return a random RSSI value in dBm corresponding to the requested quality
     */
    public static int generateRssiByQuality(SignalQuality quality) {
        switch (quality) {
            case EXCELLENT:
                return generateRandomRssi(EXCELLENT_MIN, EXCELLENT_MIN + 20);
            case GOOD:
                return generateRandomRssi(GOOD_MIN, EXCELLENT_MIN - 1);
            case FAIR:
                return generateRandomRssi(FAIR_MIN, GOOD_MIN - 1);
            case WEAK:
                return generateRandomRssi(WEAK_MIN, FAIR_MIN - 1);
            case POOR:
                return generateRandomRssi(INVALID_RSSI + 1, WEAK_MIN - 1);
            default:
                return generateRandomRssi();
        }
    }

    /**
     * Enum representing different WiFi signal quality levels
     */
    public enum SignalQuality {
        EXCELLENT,
        GOOD,
        FAIR,
        WEAK,
        POOR
    }
}
