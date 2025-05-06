package eu.faircode.xlua.x.data.utils.random.region;

public class RegionCountry {
    public static RegionCountry create(String name, String iso, String id) { return new RegionCountry(name, iso, id); }

    public final String name;
    public final String iso;
    public final String id;

    public RegionCountry(String name, String iso, String id) {
        this.name = name;
        this.iso = iso;
        this.id = id;
    }
}
