package eu.faircode.xlua.x.xlua.settings.random.profile;

import android.content.Context;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public final class DeviceProfileCatalog {
    public static final String ASSET_NAME = "device_profiles.json";
    private static volatile List<DeviceProfile> cached;
    private static final Object SELECTION_LOCK = new Object();
    private static final List<String> unusedBrands = new ArrayList<>();
    private static String previousBrand;

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
        return selectBalancedForApi(get(context), Build.VERSION.SDK_INT);
    }

    static DeviceProfileSelection selectBalancedForApi(List<DeviceProfile> profiles, int apiLevel) {
        if (profiles == null || profiles.isEmpty())
            throw new IllegalArgumentException("Device profiles are required");

        Map<String, List<CompatibleDevice>> profilesByBrand = new LinkedHashMap<>();
        for (DeviceProfile profile : profiles) {
            List<DeviceBuildProfile> compatibleBuilds = new ArrayList<>();
            for (DeviceBuildProfile build : profile.builds)
                if (build.apiLevel == apiLevel)
                    compatibleBuilds.add(build);
            if (compatibleBuilds.isEmpty())
                continue;

            String brand = selectionBrand(profile);
            List<CompatibleDevice> brandProfiles = profilesByBrand.get(brand);
            if (brandProfiles == null) {
                brandProfiles = new ArrayList<>();
                profilesByBrand.put(brand, brandProfiles);
            }
            brandProfiles.add(new CompatibleDevice(profile, compatibleBuilds));
        }

        if (profilesByBrand.isEmpty())
            throw new IllegalStateException(
                    "No device profile officially supports Android API " + apiLevel);

        synchronized (SELECTION_LOCK) {
            if (unusedBrands.isEmpty() || !profilesByBrand.keySet().containsAll(unusedBrands))
                refillBrandBag(profilesByBrand.keySet());

            String brand = unusedBrands.remove(0);
            List<CompatibleDevice> brandProfiles = profilesByBrand.get(brand);
            CompatibleDevice compatible = brandProfiles.get(
                    RandomGenerator.nextInt(brandProfiles.size()));
            DeviceBuildProfile build = compatible.builds.get(
                    RandomGenerator.nextInt(compatible.builds.size()));
            previousBrand = brand;
            return new DeviceProfileSelection(compatible.device, build);
        }
    }

    static DeviceProfileSelection selectBalanced(List<DeviceProfile> profiles) {
        if (profiles == null || profiles.isEmpty())
            throw new IllegalArgumentException("Device profiles are required");

        Map<String, List<DeviceProfile>> profilesByBrand = new LinkedHashMap<>();
        for (DeviceProfile profile : profiles) {
            String brand = selectionBrand(profile);
            List<DeviceProfile> brandProfiles = profilesByBrand.get(brand);
            if (brandProfiles == null) {
                brandProfiles = new ArrayList<>();
                profilesByBrand.put(brand, brandProfiles);
            }
            brandProfiles.add(profile);
        }

        synchronized (SELECTION_LOCK) {
            if (unusedBrands.isEmpty() || !profilesByBrand.keySet().containsAll(unusedBrands))
                refillBrandBag(profilesByBrand.keySet());

            String brand = unusedBrands.remove(0);
            List<DeviceProfile> brandProfiles = profilesByBrand.get(brand);
            DeviceProfile device = brandProfiles.get(RandomGenerator.nextInt(brandProfiles.size()));
            DeviceBuildProfile build = device.builds.get(RandomGenerator.nextInt(device.builds.size()));
            previousBrand = brand;
            return new DeviceProfileSelection(device, build);
        }
    }

    private static final class CompatibleDevice {
        final DeviceProfile device;
        final List<DeviceBuildProfile> builds;

        CompatibleDevice(DeviceProfile device, List<DeviceBuildProfile> builds) {
            this.device = device;
            this.builds = builds;
        }
    }

    static String selectionBrand(DeviceProfile profile) {
        String brand = profile.brand.toLowerCase(Locale.ROOT);
        if ("redmagic".equals(brand))
            return "nubia";
        return brand;
    }

    static void resetSelectionForTests() {
        synchronized (SELECTION_LOCK) {
            unusedBrands.clear();
            previousBrand = null;
        }
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

    private static void refillBrandBag(Iterable<String> brands) {
        unusedBrands.clear();
        for (String brand : brands)
            unusedBrands.add(brand);

        for (int i = unusedBrands.size() - 1; i > 0; i--) {
            int other = RandomGenerator.nextInt(i + 1);
            Collections.swap(unusedBrands, i, other);
        }

        if (previousBrand != null && unusedBrands.size() > 1
                && previousBrand.equals(unusedBrands.get(0))) {
            for (int i = 1; i < unusedBrands.size(); i++) {
                if (!previousBrand.equals(unusedBrands.get(i))) {
                    Collections.swap(unusedBrands, 0, i);
                    break;
                }
            }
        }
    }
}
