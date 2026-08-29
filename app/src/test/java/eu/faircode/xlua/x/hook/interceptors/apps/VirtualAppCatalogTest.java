package eu.faircode.xlua.x.hook.interceptors.apps;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class VirtualAppCatalogTest {
    @Test
    public void everyDeviceProfileMapsToItsOwnOemCatalog() throws Exception {
        File file = new File("src/main/assets/device_profiles.json");
        if (!file.isFile()) file = new File("app/src/main/assets/device_profiles.json");
        JSONObject root = new JSONObject(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
        JSONArray profiles = root.getJSONArray("profiles");

        for (int index = 0; index < profiles.length(); index++) {
            JSONObject profile = profiles.getJSONObject(index);
            String brand = profile.getString("brand").toLowerCase(Locale.US);
            String identity = brand + " " + profile.getString("manufacturer") + " "
                    + profile.getString("model") + " " + profile.getString("device");
            String expected = brand;
            if (expected.equals("poco")) expected = "xiaomi";
            if (expected.equals("redmagic")) expected = "nubia";
            assertEquals(profile.getString("id"), expected,
                    VirtualAppCatalog.familyForIdentity(identity.toLowerCase(Locale.US)));
        }
    }
}
