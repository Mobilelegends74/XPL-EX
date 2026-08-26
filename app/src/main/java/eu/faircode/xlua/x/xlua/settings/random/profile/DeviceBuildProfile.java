package eu.faircode.xlua.x.xlua.settings.random.profile;

import org.json.JSONException;
import org.json.JSONObject;

public final class DeviceBuildProfile {
    public final String release;
    public final int apiLevel;
    public final String fingerprint;
    public final String buildId;
    public final String incremental;
    public final String buildType;
    public final String tags;

    public DeviceBuildProfile(String release, int apiLevel, String fingerprint) {
        this.release = release;
        this.apiLevel = apiLevel;
        this.fingerprint = fingerprint;

        FingerprintParts parts = FingerprintParts.parse(fingerprint);
        this.buildId = parts.buildId;
        this.incremental = parts.incremental;
        this.buildType = parts.buildType;
        this.tags = parts.tags;
    }

    public static DeviceBuildProfile fromJson(JSONObject object) throws JSONException {
        return new DeviceBuildProfile(
                object.getString("release"),
                object.getInt("apiLevel"),
                object.getString("fingerprint"));
    }

    public String description(DeviceProfile device) {
        return device.product + "-" + buildType + " " + release + " " + buildId + " " + incremental + " " + tags;
    }

    public String flavor(DeviceProfile device) {
        return device.product + "-" + buildType;
    }

    static final class FingerprintParts {
        final String brand;
        final String product;
        final String device;
        final String release;
        final String buildId;
        final String incremental;
        final String buildType;
        final String tags;

        private FingerprintParts(String brand, String product, String device, String release,
                                 String buildId, String incremental, String buildType, String tags) {
            this.brand = brand;
            this.product = product;
            this.device = device;
            this.release = release;
            this.buildId = buildId;
            this.incremental = incremental;
            this.buildType = buildType;
            this.tags = tags;
        }

        static FingerprintParts parse(String fingerprint) {
            if (fingerprint == null)
                throw new IllegalArgumentException("Fingerprint is null");

            int identityEnd = fingerprint.indexOf(':');
            int buildEnd = identityEnd < 0 ? -1 : fingerprint.indexOf(':', identityEnd + 1);
            if (identityEnd < 0 || buildEnd < 0)
                throw new IllegalArgumentException("Invalid fingerprint: " + fingerprint);

            String[] identity = fingerprint.substring(0, identityEnd).split("/", -1);
            String[] build = fingerprint.substring(identityEnd + 1, buildEnd).split("/", -1);
            String[] typeAndTags = fingerprint.substring(buildEnd + 1).split("/", -1);
            if (identity.length != 3 || build.length != 3 || typeAndTags.length != 2)
                throw new IllegalArgumentException("Invalid fingerprint: " + fingerprint);

            return new FingerprintParts(identity[0], identity[1], identity[2], build[0],
                    build[1], build[2], typeAndTags[0], typeAndTags[1]);
        }
    }
}
