package eu.faircode.xlua.x.xlua.settings.random.utils;

import java.util.Random;
import java.util.UUID;

/**
 * Utility class to generate valid-looking Android device serial numbers
 * for testing purposes when mocking Build.getSerial()
 */
public class SerialNumberGenerator {

    private static final Random random = new Random();

    /**
     * Generates a random device serial number that mimics real Android device serials
     * @param style The style of serial to generate (SAMSUNG, GOOGLE, HUAWEI, XIAOMI, or GENERIC)
     * @return A valid-looking device serial number
     */
    public static String generateSerial(SerialStyle style) {
        switch (style) {
            case SAMSUNG:
                return generateSamsungSerial();
            case GOOGLE:
                return generateGoogleSerial();
            case HUAWEI:
                return generateHuaweiSerial();
            case XIAOMI:
                return generateXiaomiSerial();
            case GENERIC:
            default:
                return generateGenericSerial();
        }
    }

    /**
     * Generates a random device serial without specifying a manufacturer style
     * @return A valid-looking device serial number
     */
    public static String generateSerial() {
        // Choose a random style
        SerialStyle[] styles = SerialStyle.values();
        SerialStyle randomStyle = styles[random.nextInt(styles.length)];
        return generateSerial(randomStyle);
    }

    /**
     * Generates a Samsung-style serial number
     * Format examples: R9AM50XXXXXX, R58M20XXXXXX
     */
    private static String generateSamsungSerial() {
        StringBuilder sb = new StringBuilder();

        // First character is typically R
        sb.append("R");

        // 1-2 digits
        sb.append(random.nextInt(90) + 10);

        // A letter (typically A, B, M, J)
        char[] commonLetters = {'A', 'B', 'M', 'J'};
        sb.append(commonLetters[random.nextInt(commonLetters.length)]);

        // 2 more digits
        sb.append(random.nextInt(90) + 10);

        // 6 alphanumeric characters
        for (int i = 0; i < 6; i++) {
            if (random.nextBoolean()) {
                // Add a letter
                sb.append((char) (random.nextInt(26) + 'A'));
            } else {
                // Add a digit
                sb.append(random.nextInt(10));
            }
        }

        return sb.toString();
    }

    /**
     * Generates a Google-style serial number
     * Format examples: HT82G1AXXXXX, FA8151AXXXXX
     */
    private static String generateGoogleSerial() {
        StringBuilder sb = new StringBuilder();

        // First two characters are typically letters
        for (int i = 0; i < 2; i++) {
            sb.append((char) (random.nextInt(26) + 'A'));
        }

        // 2-4 digits
        sb.append(random.nextInt(9000) + 1000);

        // A letter (typically G or A)
        char[] commonLetters = {'G', 'A'};
        sb.append(commonLetters[random.nextInt(commonLetters.length)]);

        // 1 digit
        sb.append(random.nextInt(10));

        // A letter
        sb.append((char) (random.nextInt(26) + 'A'));

        // 5 alphanumeric characters
        for (int i = 0; i < 5; i++) {
            if (random.nextBoolean()) {
                // Add a letter
                sb.append((char) (random.nextInt(26) + 'A'));
            } else {
                // Add a digit
                sb.append(random.nextInt(10));
            }
        }

        return sb.toString();
    }

    /**
     * Generates a Huawei-style serial number
     * Format examples: KWG7N19xxxxxx, TAS3N20xxxxxx
     */
    private static String generateHuaweiSerial() {
        StringBuilder sb = new StringBuilder();

        // First three characters are typically capital letters
        for (int i = 0; i < 3; i++) {
            sb.append((char) (random.nextInt(26) + 'A'));
        }

        // One digit
        sb.append(random.nextInt(10));

        // Another letter (typically N)
        sb.append('N');

        // 2 digits (typically 19, 20, 21, etc. - year)
        sb.append(random.nextInt(5) + 19);

        // 6 lowercase alphanumeric characters
        for (int i = 0; i < 6; i++) {
            if (random.nextBoolean()) {
                // Add a lowercase letter
                sb.append((char) (random.nextInt(26) + 'a'));
            } else {
                // Add a digit
                sb.append(random.nextInt(10));
            }
        }

        return sb.toString();
    }

    /**
     * Generates a Xiaomi-style serial number
     * Format examples: 23948/XXXXXXXX
     */
    private static String generateXiaomiSerial() {
        StringBuilder sb = new StringBuilder();

        // 5 digits
        for (int i = 0; i < 5; i++) {
            sb.append(random.nextInt(10));
        }

        sb.append('/');

        // 8 alphanumeric characters (mostly uppercase)
        for (int i = 0; i < 8; i++) {
            if (random.nextInt(4) > 0) { // 75% chance for uppercase letter
                sb.append((char) (random.nextInt(26) + 'A'));
            } else {
                sb.append(random.nextInt(10));
            }
        }

        return sb.toString();
    }

    /**
     * Generates a generic Android serial number based on common patterns
     */
    private static String generateGenericSerial() {
        // Several common formats exist:
        // 1. 16-character alphanumeric
        // 2. UUID style
        // 3. Simple alphanumeric with dashes

        int type = random.nextInt(3);

        switch (type) {
            case 0:
                return generateAlphanumeric(16).toUpperCase();
            case 1:
                return UUID.randomUUID().toString().toUpperCase().replace("-", "");
            case 2:
                return String.format("%s-%s-%s-%s",
                        generateAlphanumeric(4),
                        generateAlphanumeric(4),
                        generateAlphanumeric(4),
                        generateAlphanumeric(4)).toUpperCase();
            default:
                return generateAlphanumeric(16).toUpperCase();
        }
    }

    /**
     * Helper method to generate random alphanumeric strings
     */
    private static String generateAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }

    /**
     * Enum for different serial number styles
     */
    public enum SerialStyle {
        SAMSUNG,
        GOOGLE,
        HUAWEI,
        XIAOMI,
        GENERIC
    }

    /**
     * Example of how to use this with Build.getSerial() mocking
     */
    public static void mockBuildSerial() {
        // Example of mocking with Mockito or similar framework:
        // When using Mockito to mock android.os.Build:
        //
        // String mockSerial = SerialNumberGenerator.generateSerial();
        // when(Build.getSerial()).thenReturn(mockSerial);
        //
        // Or with Robolectric:
        //
        // String mockSerial = SerialNumberGenerator.generateSerial();
        // shadowOf(Build.class).setSerial(mockSerial);
    }
}