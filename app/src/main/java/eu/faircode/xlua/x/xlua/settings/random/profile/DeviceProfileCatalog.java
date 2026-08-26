package eu.faircode.xlua.x.xlua.settings.random.profile;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public final class DeviceProfileCatalog {
    public static final String ASSET_NAME = "device_profiles.json";
    private static volatile List<DeviceProfile> cached;

    private DeviceProfileCatalog() { }

    public static List<DeviceProfile> get(Context context) {
        List<DeviceProfile> local = cached;
        if (local != null)
            return local;

        synchronized (DeviceProfileCatalog.class) {
            if (cached == null) {
                if (context == null)
                    throw new IllegalArgumentException("Context is required to load device profiles");
                try (InputStream input = context.getAssets().open(ASSET_NAME)) {
                    cached = Collections.unmodifiableList(parse(input));
                } catch (Exception e) {
                    throw new IllegalStateException("Unable to load " + ASSET_NAME, e);
                }
            }
            return cached;
        }
    }

    public static List<DeviceProfile> parse(InputStream input) throws Exception {
        JSONObject root = new JSONObject(read(input));
        if (root.getInt("schemaVersion") != 1)
            throw new IllegalArgumentException("Unsupported device profile schema");

        JSONArray array = root.getJSONArray("profiles");
        List<DeviceProfile> profiles = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++)
            profiles.add(DeviceProfile.fromJson(array.getJSONObject(i)));

        DeviceProfileValidator.validateOrThrow(profiles);
        return profiles;
    }

    public static DeviceProfileSelection select(Context context) {
        List<DeviceProfile> profiles = get(context);
        DeviceProfile device = profiles.get(RandomGenerator.nextInt(profiles.size()));
        DeviceBuildProfile build = device.builds.get(RandomGenerator.nextInt(device.builds.size()));
        return new DeviceProfileSelection(device, build);
    }

    public static DeviceProfileSelection select(List<DeviceProfile> profiles, int profileIndex, int buildIndex) {
        DeviceProfile device = profiles.get(profileIndex);
        return new DeviceProfileSelection(device, device.builds.get(buildIndex));
    }

    private static String read(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) >= 0)
            output.write(buffer, 0, count);
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
}
