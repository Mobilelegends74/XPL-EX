package eu.faircode.xlua.x.hook.interceptors.network.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class WifiRandom {

    private static final Random random = new Random();

    // Common flag values used in ScanResult
    // Based on real WiFi flags used in Android's ScanResult
    private static final long FLAG_PASSPOINT_NETWORK = 0x1;
    private static final long FLAG_80211MC_RESPONDER = 0x2;
    private static final long FLAG_INTERNET_ACCESS = 0x4;
    private static final long FLAG_CARRIER_MANAGED_NETWORK = 0x8;
    private static final long FLAG_THROTTLING_APPLIED = 0x10;
    private static final long FLAG_METERED = 0x20;
    private static final long FLAG_HIGH_USAGE_NETWORK = 0x40;
    private static final long FLAG_EMERGENCY_SERVICE = 0x80;
    private static final long FLAG_RESTRICTED_NETWORK = 0x100;
    private static final long FLAG_OEMPAID_NETWORK = 0x200;
    private static final long FLAG_CARRIER_MERGED_NETWORK = 0x400;


    // Common WiFi frequencies (in MHz) for 2.4GHz and 5GHz bands
    private static final int[] FREQUENCIES_2_4GHZ = {2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447, 2452, 2457, 2462, 2467, 2472};
    private static final int[] FREQUENCIES_5GHZ = {5180, 5200, 5220, 5240, 5260, 5280, 5300, 5320, 5500, 5520, 5540, 5560, 5580, 5600,
            5620, 5640, 5660, 5680, 5700, 5720, 5745, 5765, 5785, 5805, 5825};
    private static final int[] FREQUENCIES_6GHZ = {5955, 5975, 5995, 6015, 6035, 6055, 6075, 6095, 6115, 6135, 6155, 6175, 6195, 6215};

    // WiFi channel width options (in MHz)
    private static final int[] CHANNEL_WIDTHS = {20, 40, 80, 160};

    // Security protocols
    private static final String[] SECURITY_PROTOCOLS = {
            "WEP",
            "WPA-PSK", "WPA2-PSK", "WPA-PSK-TKIP", "WPA2-PSK-CCMP",
            "WPA3-SAE", "WPA3-SAE-TRANSITION",
            "WPA-EAP", "WPA2-EAP", "WPA-EAP-SUITE-B", "WPA2-EAP-SUITE-B-192",
            "OWE", "OWE-TRANSITION"
    };

    // Service types
    private static final String[] SERVICE_TYPES = {
            "ESS", "IBSS"
    };

    // Encryption types
    private static final String[] ENCRYPTION_TYPES = {
            "TKIP", "CCMP", "GCMP", "GCMP-256", "SMS4", "BIP-CMAC-256"
    };

    // Additional features
    private static final String[] ADDITIONAL_FEATURES = {
            "RSN", "PSK", "FT", "EAP", "PREAUTH", "MFP", "MFPR", "MFPC",
            "PMKID", "SAE", "BIP", "SPP", "FILS", "HE", "MU-MIMO"
    };


    // Common HESSID prefixes for well-known providers
    // First 3 bytes often represent the Organization Unique Identifier (OUI)
    private static final String[] KNOWN_OUI = {
            "00:1A:11", // Google
            "00:17:9A", // D-Link
            "00:10:18", // Broadcom
            "00:0E:8F", // Sercomm
            "00:23:3E", // Toshiba
            "00:25:9C", // Cisco-Linksys
            "5C:96:9D", // Apple
            "B8:27:EB", // Raspberry Pi
            "00:13:A9", // Sony
            "00:26:86", // Quantenna
            "DC:A6:32", // Raspberry Pi
            "00:18:0A", // Cisco-Meraki
            "00:13:10", // Cisco-Linksys
            "00:1E:E5", // Cisco-Linksys
            "58:6D:8F", // Cisco-Meraki
            "00:22:75", // Belkin
            "00:25:00", // Apple
            "00:1F:90", // Actiontec
            "00:15:E9", // D-Link
            "00:21:29", // Cisco-Linksys
    };


    // Common ANQP domain ranges
    private static final Map<String, int[]> ANQP_DOMAIN_RANGES = new HashMap<String, int[]>() {{
        // Format: provider name -> [min, max] range for domain IDs
        put("General", new int[]{1, 255});
        put("Boingo", new int[]{1024, 1124});
        put("AT&T", new int[]{2048, 2148});
        put("T-Mobile", new int[]{3072, 3172});
        put("Comcast", new int[]{4096, 4196});
        put("Charter", new int[]{5120, 5220});
        put("Google", new int[]{8192, 8292});
        put("Public", new int[]{16384, 16484});
        put("Experimental", new int[]{49152, 49352});
        put("Reserved", new int[]{65280, 65535});
    }};

    /**
     * Generates a random WiFi capability string.
     * @return A formatted capability string like "[WPA2-PSK][ESS][CCMP]"
     */
    public static String generateRandomCapability() {
        List<String> capabilities = new ArrayList<>();

        // Choose network type (modern or legacy)
        int networkType = random.nextInt(10);

        if (networkType < 1) {
            // 10% chance of legacy network (WEP)
            capabilities.add("WEP");

            // Add service type
            capabilities.add(getRandomElement(SERVICE_TYPES));

        } else if (networkType < 3) {
            // 20% chance of WPA1 network
            capabilities.add("WPA-PSK");
            capabilities.add(getRandomElement(SERVICE_TYPES));
            capabilities.add("TKIP");

            // Maybe add RSN
            if (random.nextBoolean()) {
                capabilities.add("RSN");
            }

        } else if (networkType < 7) {
            // 40% chance of WPA2 network
            capabilities.add("WPA2-PSK");
            capabilities.add(getRandomElement(SERVICE_TYPES));
            capabilities.add("CCMP");
            capabilities.add("RSN");

            // Maybe add MFP
            if (random.nextBoolean()) {
                capabilities.add("MFP");
            }

        } else {
            // 30% chance of WPA3 network
            if (random.nextBoolean()) {
                // Pure WPA3
                capabilities.add("WPA3-SAE");
            } else {
                // Transition mode
                capabilities.add("WPA3-SAE-TRANSITION");
                capabilities.add("WPA2-PSK");
            }

            capabilities.add("ESS");  // Modern networks are almost always ESS
            capabilities.add("CCMP");
            capabilities.add("RSN");
            capabilities.add("MFP");

            // Maybe add HE (WiFi 6)
            if (random.nextBoolean()) {
                capabilities.add("HE");
            }
        }

        // Add 0-2 random additional features if this is a modern network
        if (networkType >= 3) {
            int additionalFeatureCount = random.nextInt(3);
            for (int i = 0; i < additionalFeatureCount; i++) {
                String feature = getRandomElement(ADDITIONAL_FEATURES);
                if (!capabilities.contains(feature)) {
                    capabilities.add(feature);
                }
            }
        }

        // Format the result with brackets
        StringBuilder result = new StringBuilder();
        for (String capability : capabilities) {
            result.append("[").append(capability).append("]");
        }

        return result.toString();
    }

    /**
     * Utility method to get a random element from an array
     */
    private static String getRandomElement(String[] array) {
        return array[random.nextInt(array.length)];
    }



    /**
     * Generates a random WiFi signal strength level (RSSI value)
     * @param distance Optional parameter to simulate distance from access point (1-5)
     *                 where 1 is very close and 5 is far away
     * @return A realistic RSSI value in dBm (negative integer)
     */
    public static int generateRandomLevel(int distance) {
        // Validate distance parameter
        if (distance < 1 || distance > 5) {
            distance = random.nextInt(5) + 1;
        }

        // Signal strength ranges based on approximate distance
        HashMap<Integer, int[]> distanceRanges = new HashMap<>();
        distanceRanges.put(1, new int[]{-30, -55});  // Very close
        distanceRanges.put(2, new int[]{-55, -65});  // Close
        distanceRanges.put(3, new int[]{-65, -75});  // Medium distance
        distanceRanges.put(4, new int[]{-75, -85});  // Far
        distanceRanges.put(5, new int[]{-85, -100}); // Very far



        // Get range for the specified distance
        int[] range = distanceRanges.get(distance);

        try {
            int min = Math.min(range[0], range[1]);
            int max = Math.max(range[0], range[1]);
            // Generate random value within range
            return RandomGenerator.nextInt(max - min) + min;
        }catch (Exception e) {
            return RandomGenerator.nextInt(range[0], range[1]);
        }
    }

    /**
     * Overloaded method that generates a random signal level without specifying distance
     * @return A realistic RSSI value in dBm (negative integer)
     */
    public static int generateRandomLevel() {
        return generateRandomLevel(RandomGenerator.nextInt(1, 5));
    }

    /**
     * Helper method to interpret signal strength
     * @param level The RSSI level in dBm
     * @return A human-readable description of signal quality
     */
    public static String getSignalQuality(int level) {
        if (level >= -50) return "Excellent";
        if (level >= -60) return "Good";
        if (level >= -70) return "Fair";
        if (level >= -80) return "Weak";
        if (level >= -90) return "Poor";
        return "Unusable";
    }


    /**
     * Generates a random WiFi frequency
     * @return A frequency in MHz (2.4GHz, 5GHz or 6GHz band)
     */
    public static int generateRandomFrequency() {
        // Decide which band to use
        int band = random.nextInt(100);

        if (band < 45) {
            // 45% chance of 2.4GHz
            return FREQUENCIES_2_4GHZ[random.nextInt(FREQUENCIES_2_4GHZ.length)];
        } else if (band < 90) {
            // 45% chance of 5GHz
            return FREQUENCIES_5GHZ[random.nextInt(FREQUENCIES_5GHZ.length)];
        } else {
            // 10% chance of 6GHz (WiFi 6E)
            return FREQUENCIES_6GHZ[random.nextInt(FREQUENCIES_6GHZ.length)];
        }
    }

    /**
     * Generates a random scan timestamp
     * @return Timestamp in microseconds since boot
     */
    public static long generateRandomTimestamp() {
        // Generate a random timestamp between 0 and 12 hours ago (in microseconds)
        return (long) (random.nextDouble() * TimeUnit.HOURS.toMicros(12));
    }

    /**
     * Generates a random distance in centimeters
     * @return Distance to AP in cm
     */
    public static int generateRandomDistanceCm() {
        // Most WiFi APs have a range of 0-50 meters
        return random.nextInt(5000);
    }

    /**
     * Generates a random standard deviation for distance measurement
     * @param distanceCm The distance value to base this on
     * @return Standard deviation in cm
     */
    public static int generateRandomDistanceSdCm(int distanceCm) {
        // Standard deviation is typically 10-30% of the distance
        return (int) (distanceCm * (0.1 + 0.2 * random.nextDouble()));
    }

    /**
     * Generates a random channel width
     * @return Channel width in MHz (20, 40, 80, or 160)
     */
    public static int generateRandomChannelWidth() {
        return CHANNEL_WIDTHS[random.nextInt(CHANNEL_WIDTHS.length)];
    }

    /**
     * Generates random center frequencies based on primary frequency and channel width
     * @param primaryFreq The primary frequency
     * @param channelWidth The channel width
     * @return Array of [centerFreq0, centerFreq1] where centerFreq1 may be 0
     */
    public static int[] generateRandomCenterFrequencies(int primaryFreq, int channelWidth) {
        int centerFreq0;
        int centerFreq1 = 0;

        // For simplicity, we'll approximate center frequencies
        if (channelWidth == 20) {
            centerFreq0 = primaryFreq;
        } else if (channelWidth == 40) {
            centerFreq0 = primaryFreq + 10;
        } else if (channelWidth == 80) {
            centerFreq0 = primaryFreq + 30;
        } else { // 160 MHz
            centerFreq0 = primaryFreq + 50;
            // For 160MHz channels, sometimes we have two 80MHz segments
            if (random.nextBoolean()) {
                centerFreq1 = primaryFreq + 130;
            }
        }

        return new int[]{centerFreq0, centerFreq1};
    }

    /**
     * Generates a random "last seen" timestamp
     * @return Timestamp in milliseconds
     */
    public static long generateRandomSeen() {
        // Network was seen between 0 and 24 hours ago
        return System.currentTimeMillis() - TimeUnit.HOURS.toMillis(random.nextInt(24));
    }

    /**
     * Generates a random auto join status
     * @return Auto join status (integer values typically 0-10)
     */
    public static int generateRandomAutoJoinStatus() {
        return random.nextInt(11);
    }

    /**
     * Generates a random untrusted value
     * @return 0 (trusted) or 1 (untrusted)
     */
    public static int generateRandomUntrusted() {
        // 80% of networks are trusted, 20% untrusted
        return random.nextInt(5) == 0 ? 1 : 0;
    }

    /**
     * Generates a random number of connections
     * @return Number of connections to this network
     */
    public static int generateRandomNumConnection() {
        // Most networks have been connected to <30 times
        // Use exponential distribution to favor lower numbers
        return (int) (-Math.log(1 - random.nextDouble()) * 10);
    }

    /**
     * Generates a random number of usages
     * @param numConnections The number of connections to base this on
     * @return Number of times this network was used
     */
    public static int generateRandomNumUsage(int numConnections) {
        // Usage count is usually a bit higher than connection count
        // as a connection can be used multiple times
        return numConnections + random.nextInt(Math.max(1, numConnections / 2));
    }

    /**
     * Generates a random number of IP config failures
     * @return Number of IP configuration failures
     */
    public static int generateRandomNumIpConfigFailures() {
        // Most networks have few IP config failures, if any
        return random.nextInt(3);
    }

    /**
     * Generates a random auto join candidate value
     * @param autoJoinStatus The auto join status to use as reference
     * @return 0 (not a candidate) or 1 (is a candidate)
     */
    public static int generateRandomIsAutoJoinCandidate(int autoJoinStatus) {
        // A network with autoJoinStatus of 0 is more likely to be a candidate
        if (autoJoinStatus == 0 && random.nextInt(10) < 8) {
            return 1;
        }
        return random.nextInt(2);
    }

    /**
     * Generates random flags for a WiFi network
     * @return A long value containing combined flags
     */
    public static long generateRandomFlags() {
        long flags = 0;

        // Randomly decide which flags to set

        // Passpoint network (aka Hotspot 2.0) - 20% chance
        if (random.nextInt(5) == 0) {
            flags |= FLAG_PASSPOINT_NETWORK;
        }

        // IEEE 802.11mc responder (for RTT/indoor positioning) - 15% chance
        if (random.nextInt(20) < 3) {
            flags |= FLAG_80211MC_RESPONDER;
        }

        // Internet access - 90% chance
        if (random.nextInt(10) < 9) {
            flags |= FLAG_INTERNET_ACCESS;
        }

        // Carrier-managed network - 25% chance
        if (random.nextInt(4) == 0) {
            flags |= FLAG_CARRIER_MANAGED_NETWORK;
        }

        // Throttling applied - 10% chance
        if (random.nextInt(10) == 0) {
            flags |= FLAG_THROTTLING_APPLIED;
        }

        // Metered network - 30% chance
        if (random.nextInt(10) < 3) {
            flags |= FLAG_METERED;
        }

        // High usage network - 5% chance
        if (random.nextInt(20) == 0) {
            flags |= FLAG_HIGH_USAGE_NETWORK;
        }

        // Emergency service network - 1% chance
        if (random.nextInt(100) == 0) {
            flags |= FLAG_EMERGENCY_SERVICE;
        }

        // Restricted network - 15% chance
        if (random.nextInt(20) < 3) {
            flags |= FLAG_RESTRICTED_NETWORK;
        }

        // OEM paid network - 5% chance
        if (random.nextInt(20) == 0) {
            flags |= FLAG_OEMPAID_NETWORK;
        }

        // Carrier merged network - 10% chance
        if (random.nextInt(10) == 0) {
            flags |= FLAG_CARRIER_MERGED_NETWORK;
        }

        return flags;
    }


    /**
     * Generate a random HESSID value (as a long)
     * @param useKnownProvider Whether to use a known provider OUI (true) or fully random MAC (false)
     * @return HESSID as a 64-bit long value
     */
    public static long generateRandomHESSID(boolean useKnownProvider) {
        StringBuilder mac = new StringBuilder();

        if (useKnownProvider) {
            // Use a known OUI (first 3 bytes) for a realistic provider
            String oui = KNOWN_OUI[random.nextInt(KNOWN_OUI.length)];
            mac.append(oui);

            // Generate the last 3 bytes randomly
            for (int i = 0; i < 3; i++) {
                mac.append(":");
                mac.append(String.format("%02X", random.nextInt(256)));
            }
        } else {
            // Generate a completely random MAC address
            for (int i = 0; i < 6; i++) {
                if (i > 0) mac.append(":");
                mac.append(String.format("%02X", random.nextInt(256)));
            }
        }

        // Convert the MAC string to a long value
        return macAddressToLong(mac.toString());
    }

    /**
     * Overloaded method for generating a random HESSID
     * 80% chance of using a known provider OUI
     * @return HESSID as a 64-bit long value
     */
    public static long generateRandomHESSID() {
        // 80% chance of using a known provider OUI
        boolean useKnownProvider = random.nextInt(5) != 0;
        return generateRandomHESSID(useKnownProvider);
    }

    /**
     * Get a string representation of a HESSID
     * @param hessid The HESSID value as a long
     * @return MAC address format string (e.g., "00:11:22:33:44:55")
     */
    public static String formatHESSID(long hessid) {
        StringBuilder mac = new StringBuilder();
        for (int i = 5; i >= 0; i--) {
            int shift = i * 8;
            int value = (int)((hessid >> shift) & 0xFF);
            mac.append(String.format("%02X", value));
            if (i > 0) mac.append(":");
        }
        return mac.toString();
    }

    /**
     * Generate a random ANQP Domain ID
     * @param useRealisticRange Whether to use realistic ranges for providers
     * @return ANQP Domain ID as an int (16-bit value)
     */
    public static int generateRandomANQPDomainId(boolean useRealisticRange) {
        if (useRealisticRange) {
            // Choose a random provider range
            String[] providers = ANQP_DOMAIN_RANGES.keySet().toArray(new String[0]);
            String provider = providers[random.nextInt(providers.length)];
            int[] range = ANQP_DOMAIN_RANGES.get(provider);

            // Generate a value within that range
            return range[0] + random.nextInt(range[1] - range[0] + 1);
        } else {
            // Generate a completely random 16-bit value
            return random.nextInt(65536);
        }
    }

    /**
     * Overloaded method for generating a random ANQP Domain ID
     * 90% chance of using a realistic range
     * @return ANQP Domain ID as an int (16-bit value)
     */
    public static int generateRandomANQPDomainId() {
        // 90% chance of using a realistic range
        boolean useRealisticRange = random.nextInt(10) != 0;
        return generateRandomANQPDomainId(useRealisticRange);
    }

    /**
     * Get a description of an ANQP Domain ID
     * @param anqpDomainId The ANQP Domain ID
     * @return Description of the likely provider based on the range
     */
    public static String describeANQPDomainId(int anqpDomainId) {
        for (Map.Entry<String, int[]> entry : ANQP_DOMAIN_RANGES.entrySet()) {
            int min = entry.getValue()[0];
            int max = entry.getValue()[1];

            if (anqpDomainId >= min && anqpDomainId <= max) {
                return entry.getKey() + " range";
            }
        }

        return "Unknown range";
    }

    /**
     * Utility method to convert a MAC address string to a long value
     * @param macAddress MAC address in format "00:11:22:33:44:55"
     * @return The MAC address as a long value
     */
    private static long macAddressToLong(String macAddress) {
        String[] hexBytes = macAddress.split(":");
        long result = 0;

        for (int i = 0; i < 6; i++) {
            int value = Integer.parseInt(hexBytes[i], 16);
            result = (result << 8) | value;
        }

        return result;
    }


}