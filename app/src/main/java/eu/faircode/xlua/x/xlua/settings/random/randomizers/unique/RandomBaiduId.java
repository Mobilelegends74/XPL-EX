package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RanUnqUtils;
import eu.faircode.xlua.x.xlua.settings.random.utils.SerialNumberGenerator;

//43851AA1525622247E2C7CA2049961B4
public class RandomBaiduId extends RandomElement {
    public RandomBaiduId() {
        super("Unique Baidu ID All Versions");
        putIndexSettings(RandomizersCache.SETTING_BAIDU_DEVICE_ID, 1, 2, 3, 4, 5);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        String name = context.stack.pop();
        String trimmed = Str.trimOriginal(name);
        if(Str.isEmpty(trimmed))
            return;

        char endChar = trimmed.charAt(trimmed.length() - 1);
        String newValue;
        switch (endChar) {
            case '3':
                //com.baidu.uuid (Apps Package Name and Serial or a Random UUID, MD5 Hash)
                newValue = generateBaiduUuid(RandomGenerator.nextStringAlpha(8, 28), SerialNumberGenerator.generateSerial());
                break;
            case '4':
            case '5':
                //dxCRMxhQkdGePGnp (IMEI/MEID, serial, or Android ID, AES encrypted, base64 encoded)
                //mqBRboGZkQPcAkyk (IMEI/MEID, serial, or Android ID, AES encrypted, base64 encoded)
                int rnd = RandomGenerator.nextInt(1, 4);
                switch (rnd) {
                    case 1:
                        newValue = generateAlicloudDeviceId(RandomGenerator.nextStringNumeric(15));
                        break;
                    case 2:
                        newValue = generateAlicloudDeviceId(RandomGenerator.nextStringNumeric(14));
                        break;
                    case 3:
                        newValue = generateAlicloudDeviceId(SerialNumberGenerator.generateSerial());
                        break;
                    default:
                        newValue = generateAlicloudDeviceId(RandomGenerator.nextStringHex(16).toLowerCase());
                        break;
                }
                break;
            default:    //1, 2, etc
                newValue = generateBaiduDeviceIdV2(RandomGenerator.nextStringAlpha(8, 28), RandomGenerator.nextStringHex(16).toLowerCase());
                break;
        }

        context.pushValue(name, newValue);
    }

    /**
     * Generates a Baidu device ID (v2) based on package name and Android ID
     * Format: MD5 hash of package name + Android ID with some modifications
     *
     * @param packageName The application package name
     * @param androidId The Android ID of the device
     * @return A 32-character Baidu device ID hash
     */
    public static String generateBaiduDeviceIdV2(String packageName, String androidId) {
        try {
            // Combine inputs with fixed salt
            String baseInput = packageName + "_" + androidId + "_baidu_v2";

            // Create MD5 hash
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(baseInput.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return RandomGenerator.nextStringHex(16);
        }
    }

    /**
     * Generates a Baidu UUID based on package name and device serial number
     * Format: MD5 hash of package name + serial (or random UUID if serial is unavailable)
     *
     * @param packageName The application package name
     * @param serial The device serial number (or null if unavailable)
     * @return A 32-character Baidu UUID hash
     */
    public static String generateBaiduUuid(String packageName, String serial) {
        try {
            String baseInput;

            // Use serial if available, otherwise generate a random UUID
            if (serial != null && !serial.isEmpty()) {
                baseInput = packageName + "_" + serial + "_baidu_uuid";
            } else {
                baseInput = packageName + "_" + UUID.randomUUID().toString() + "_baidu_uuid";
            }

            // Create MD5 hash
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(baseInput.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return RandomGenerator.nextStringHex(16);
        }
    }

    /**
     * Generates an Alicloud Push device ID (dxCRMxhQkdGePGnp format)
     * Uses IMEI/MEID/Serial/AndroidID with AES encryption and base64 encoding
     *
     * @param deviceIdentifier The device identifier (IMEI, MEID, serial, or Android ID)
     * @param packageName Optional package name to add uniqueness
     * @return Base64-encoded AES-encrypted identifier in Alicloud Push format
     */
    public static String generateAlicloudPushId(String deviceIdentifier, String packageName) {
        try {
            // Add salt and package info to make it unique but deterministic
            String input = deviceIdentifier + "_alicloud_" + (packageName != null ? packageName : "");

            // AES encryption key and initialization vector (fixed for deterministic results)
            byte[] keyBytes = "AliCloudP0shKey!".getBytes(StandardCharsets.UTF_8); // 16 bytes for AES-128
            byte[] ivBytes = "AliCloud!InitVec".getBytes(StandardCharsets.UTF_8);  // 16 bytes IV

            // Setup AES encryption
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            // Encrypt the input
            byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));

            // Encode with Base64 using Android's implementation
            String base64Encoded = android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT);

            // Take first 16 chars to match the expected format (dxCRMxhQkdGePGnp)
            return base64Encoded.substring(0, Math.min(16, base64Encoded.length()));

        } catch (Exception e) {
            // Fallback method if encryption fails
            int seed = (deviceIdentifier + packageName).hashCode();
            Random random = new Random(seed);

            // Generate a 16-character identifier with similar pattern
            StringBuilder sb = new StringBuilder("dx");
            for (int i = 2; i < 16; i++) {
                // Mix uppercase, lowercase and numbers like the example
                int type = random.nextInt(3);
                if (type == 0) {
                    sb.append((char)('a' + random.nextInt(26)));
                } else if (type == 1) {
                    sb.append((char)('A' + random.nextInt(26)));
                } else {
                    sb.append(random.nextInt(10));
                }
            }
            return sb.toString();
        }
    }

    /**
     * Generates an Alicloud device ID (mqBRboGZkQPcAkyk format)
     * Uses IMEI/MEID/Serial/AndroidID with AES encryption and base64 encoding
     *
     * @param deviceIdentifier The device identifier (IMEI, MEID, serial, or Android ID)
     * @return Base64-encoded AES-encrypted identifier in Alicloud format
     */
    public static String generateAlicloudDeviceId(String deviceIdentifier) {
        try {
            // Add salt to make it unique but deterministic
            String input = deviceIdentifier + "_alicloudid_";

            // AES encryption key and initialization vector (fixed for deterministic results)
            byte[] keyBytes = "A1iDeviceIdK3y!!".getBytes(StandardCharsets.UTF_8); // 16 bytes for AES-128
            byte[] ivBytes = "AliDeviceIdIV012".getBytes(StandardCharsets.UTF_8);  // 16 bytes IV

            // Setup AES encryption
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            // Encrypt the input
            byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));

            // Encode with Base64 using Android's implementation
            String base64Encoded = android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT);

            // Take first 16 chars to match the expected format (mqBRboGZkQPcAkyk)
            return base64Encoded.substring(0, Math.min(16, base64Encoded.length()));

        } catch (Exception e) {
            // Fallback method if encryption fails
            int seed = deviceIdentifier.hashCode();
            Random random = new Random(seed);

            // Generate a 16-character identifier with similar pattern to the example
            StringBuilder sb = new StringBuilder("mq");
            for (int i = 2; i < 16; i++) {
                // Mix uppercase, lowercase letters like the example
                if (random.nextBoolean()) {
                    sb.append((char)('a' + random.nextInt(26)));
                } else {
                    sb.append((char)('A' + random.nextInt(26)));
                }
            }
            return sb.toString();
        }
    }
}