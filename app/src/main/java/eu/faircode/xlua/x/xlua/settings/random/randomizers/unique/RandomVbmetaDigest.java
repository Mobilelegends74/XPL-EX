package eu.faircode.xlua.x.xlua.settings.random.randomizers.unique;

import java.util.Random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.runtime.BuildInfo;
import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.SerialNumberGenerator;

//
public class RandomVbmetaDigest extends RandomElement {
    public RandomVbmetaDigest() {
        super("VBMeta Digest");
        putSettings(RandomizersCache.SETTING_UNIQUE_VB_META_DIGEST);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        String setting = context.stack.pop();
        if(setting == null)
            return;

        context.pushSpecial(setting, generateVbmetaDigest());
    }

    /**
     * Generates a random vbmeta digest string similar to the format used in Android boot properties
     * Format matches: f5341246f4b2b08d815788d86dbd6ff2597c4da1aff42062021a7aa8a46aab81e
     *
     * @return A random vbmeta digest string
     */
    public static String generateVbmetaDigest() {
        try {

            // Create a byte array of appropriate length (your example is 64 hex chars = 32 bytes)
            byte[] bytes = RandomGenerator.nextBytes(32);

            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (Exception e) {
            // Simple fallback method if there's any issue
            StringBuilder sb = new StringBuilder();
            Random fallbackRandom = new Random(System.currentTimeMillis());

            // Generate 64 hex characters
            for (int i = 0; i < 64; i++) {
                sb.append("0123456789abcdef".charAt(fallbackRandom.nextInt(16)));
            }

            return sb.toString();
        }
    }
}
