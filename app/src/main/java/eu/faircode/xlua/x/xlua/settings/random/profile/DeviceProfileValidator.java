package eu.faircode.xlua.x.xlua.settings.random.profile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class DeviceProfileValidator {
    private DeviceProfileValidator() { }

    public static List<String> validate(List<DeviceProfile> profiles) {
        List<String> errors = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Set<String> identities = new HashSet<>();
        Set<String> fingerprints = new HashSet<>();

        if (profiles == null || profiles.isEmpty()) {
            errors.add("Device profile catalog is empty");
            return errors;
        }

        for (DeviceProfile profile : profiles) {
            String prefix = profile == null ? "<null>" : profile.id;
            if (profile == null) {
                errors.add("Null device profile");
                continue;
            }

            require(errors, prefix, "id", profile.id);
            require(errors, prefix, "manufacturer", profile.manufacturer);
            require(errors, prefix, "brand", profile.brand);
            require(errors, prefix, "marketingName", profile.marketingName);
            require(errors, prefix, "model", profile.model);
            require(errors, prefix, "device", profile.device);
            require(errors, prefix, "product", profile.product);
            require(errors, prefix, "hardware", profile.hardware);
            require(errors, prefix, "board", profile.board);
            require(errors, prefix, "socManufacturer", profile.socManufacturer);
            require(errors, prefix, "socModel", profile.socModel);
            require(errors, prefix, "cpuArchitecture", profile.cpuArchitecture);
            require(errors, prefix, "abi", profile.abi);
            require(errors, prefix, "abiList", profile.abiList);
            require(errors, prefix, "abiList64", profile.abiList64);
            require(errors, prefix, "launchRelease", profile.launchRelease);
            require(errors, prefix, "source", profile.source);

            if (profile.cpuCount < 1)
                errors.add(prefix + ": invalid cpuCount");
            if (!AndroidRelease.matches(profile.launchRelease, profile.launchApiLevel))
                errors.add(prefix + ": launch release/API mismatch");
            if (!ids.add(lower(profile.id)))
                errors.add(prefix + ": duplicate id");

            String identity = lower(profile.manufacturer + "|" + profile.brand + "|" + profile.model
                    + "|" + profile.device + "|" + profile.product);
            if (!identities.add(identity))
                errors.add(prefix + ": duplicate device identity");
            if (profile.builds.isEmpty())
                errors.add(prefix + ": no verified builds");

            boolean hasLaunchBuild = false;
            for (DeviceBuildProfile build : profile.builds) {
                String buildPrefix = prefix + "/" + (build == null ? "<null>" : build.release);
                if (build == null) {
                    errors.add(prefix + ": null build");
                    continue;
                }

                if (!AndroidRelease.matches(build.release, build.apiLevel))
                    errors.add(buildPrefix + ": release/API mismatch");
                if (build.apiLevel < profile.launchApiLevel)
                    errors.add(buildPrefix + ": Android is below launch version");
                if (build.release.equals(profile.launchRelease))
                    hasLaunchBuild = true;
                if (!fingerprints.add(build.fingerprint))
                    errors.add(buildPrefix + ": duplicate fingerprint");

                try {
                    DeviceBuildProfile.FingerprintParts parts = DeviceBuildProfile.FingerprintParts.parse(build.fingerprint);
                    match(errors, buildPrefix, "brand", profile.brand, parts.brand);
                    match(errors, buildPrefix, "product", profile.product, parts.product);
                    match(errors, buildPrefix, "device", profile.device, parts.device);
                    match(errors, buildPrefix, "release", build.release, parts.release);
                    match(errors, buildPrefix, "buildId", build.buildId, parts.buildId);
                    match(errors, buildPrefix, "incremental", build.incremental, parts.incremental);
                } catch (IllegalArgumentException e) {
                    errors.add(buildPrefix + ": " + e.getMessage());
                }
            }

            if (!hasLaunchBuild)
                errors.add(prefix + ": launch Android build is missing");
        }
        return errors;
    }

    public static void validateOrThrow(List<DeviceProfile> profiles) {
        List<String> errors = validate(profiles);
        if (!errors.isEmpty())
            throw new IllegalArgumentException(join(errors));
    }

    private static void require(List<String> errors, String prefix, String field, String value) {
        if (value == null || value.trim().isEmpty())
            errors.add(prefix + ": empty " + field);
    }

    private static void match(List<String> errors, String prefix, String field, String expected, String actual) {
        if (expected == null || actual == null || !expected.equalsIgnoreCase(actual))
            errors.add(prefix + ": fingerprint " + field + " mismatch (" + expected + " != " + actual + ")");
    }

    private static String lower(String value) {
        return value.toLowerCase(Locale.ROOT);
    }

    private static String join(List<String> errors) {
        StringBuilder result = new StringBuilder();
        for (String error : errors) {
            if (result.length() > 0)
                result.append("; ");
            result.append(error);
        }
        return result.toString();
    }
}
