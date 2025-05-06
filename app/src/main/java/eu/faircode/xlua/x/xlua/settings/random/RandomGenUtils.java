package eu.faircode.xlua.x.xlua.settings.random;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomGenUtils {
    public static int generateTrulyRandomColor() {
        // Generate random RGB values
        int red = RandomGenerator.nextInt(256);
        int green = RandomGenerator.nextInt(256);
        int blue = RandomGenerator.nextInt(256);

        // Ensure the color isn't too dark by enforcing a minimum brightness
        // If the color is too dark, boost one of the channels
        int brightness = (red + green + blue) / 3;
        if (brightness < 80) {
            int boostChannel = RandomGenerator.nextInt(3);
            if (boostChannel == 0) {
                red = Math.min(255, red + 100);
            } else if (boostChannel == 1) {
                green = Math.min(255, green + 100);
            } else {
                blue = Math.min(255, blue + 100);
            }
        }

        // Always set alpha to fully opaque (FF)
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
}
