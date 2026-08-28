package eu.faircode.xlua.x.xlua.settings.random.profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DeviceProfile {
    public final String id;
    public final String manufacturer;
    public final String brand;
    public final String marketingName;
    public final String model;
    public final String device;
    public final String product;
    public final String hardware;
    public final String board;
    public final String socManufacturer;
    public final String socModel;
    public final int cpuCount;
    public final String cpuArchitecture;
    public final String abi;
    public final String abiList;
    public final String abiList32;
    public final String abiList64;
    public final String launchRelease;
    public final int launchApiLevel;
    public final String source;
    public final DeviceCharacteristics characteristics;
    public final List<DeviceBuildProfile> builds;

    public DeviceProfile(String id, String manufacturer, String brand, String marketingName,
                         String model, String device, String product, String hardware, String board,
                         String socManufacturer, String socModel, int cpuCount, String cpuArchitecture,
                         String abi, String abiList, String abiList32, String abiList64,
                         String launchRelease, int launchApiLevel, String source,
                         List<DeviceBuildProfile> builds) {
        this.id = id;
        this.manufacturer = manufacturer;
        this.brand = brand;
        this.marketingName = marketingName;
        this.model = model;
        this.device = device;
        this.product = product;
        this.hardware = hardware;
        this.board = board;
        this.socManufacturer = socManufacturer;
        this.socModel = socModel;
        this.cpuCount = cpuCount;
        this.cpuArchitecture = cpuArchitecture;
        this.abi = abi;
        this.abiList = abiList;
        this.abiList32 = abiList32;
        this.abiList64 = abiList64;
        this.launchRelease = launchRelease;
        this.launchApiLevel = launchApiLevel;
        this.source = source;
        this.characteristics = DeviceCharacteristics.forProfile(id);
        this.builds = Collections.unmodifiableList(new ArrayList<>(builds));
    }

    public static DeviceProfile fromJson(JSONObject object) throws JSONException {
        JSONArray buildArray = object.getJSONArray("builds");
        List<DeviceBuildProfile> builds = new ArrayList<>(buildArray.length());
        for (int i = 0; i < buildArray.length(); i++)
            builds.add(DeviceBuildProfile.fromJson(buildArray.getJSONObject(i)));

        return new DeviceProfile(
                object.getString("id"), object.getString("manufacturer"), object.getString("brand"),
                object.getString("marketingName"), object.getString("model"), object.getString("device"),
                object.getString("product"), object.getString("hardware"), object.getString("board"),
                object.getString("socManufacturer"), object.getString("socModel"), object.getInt("cpuCount"),
                object.getString("cpuArchitecture"), object.getString("abi"), object.getString("abiList"),
                object.getString("abiList32"), object.getString("abiList64"),
                object.getString("launchRelease"), object.getInt("launchApiLevel"),
                object.getString("source"), builds);
    }
}
