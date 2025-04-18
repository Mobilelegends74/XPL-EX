package eu.faircode.xlua.x.xlua.settings.random.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

/**
 * Class to generate region settings using HashMaps for easy lookup
 */
public class RegionSettingsHashMap {

    // Main HashMap: Display name -> RegionSetting
    private final HashMap<String, RegionSetting> regionSettingsMap = new HashMap<>();

    // Country map for organizing settings hierarchically
    private final HashMap<String, CountryData> countryDataMap = new HashMap<>();

    private final Random random = new Random();

    /**
     * Constructor - initializes all region data
     */
    public RegionSettingsHashMap() {
        initializeRegionData();
        generateRegionSettingsMap();
    }

    /**
     * Initializes the core country data
     */
    private void initializeRegionData() {
        // United States
        CountryData us = new CountryData("US", "USA", "United States");
        us.addLanguage("en", "English", "en-US");
        us.addLanguage("es", "Spanish", "es-US");
        us.addTimezone("America/New_York", "Eastern Time", "GMT-5");
        us.addTimezone("America/Chicago", "Central Time", "GMT-6");
        us.addTimezone("America/Denver", "Mountain Time", "GMT-7");
        us.addTimezone("America/Los_Angeles", "Pacific Time", "GMT-8");
        us.addTimezone("America/Anchorage", "Alaska Time", "GMT-9");
        us.addTimezone("Pacific/Honolulu", "Hawaii Time", "GMT-10");
        countryDataMap.put(us.getIso2(), us);

        // United Kingdom
        CountryData uk = new CountryData("GB", "GBR", "United Kingdom");
        uk.addLanguage("en", "English", "en-GB");
        uk.addLanguage("cy", "Welsh", "cy-GB");
        uk.addLanguage("gd", "Scottish Gaelic", "gd-GB");
        uk.addTimezone("Europe/London", "Greenwich Mean Time", "GMT+0");
        countryDataMap.put(uk.getIso2(), uk);

        // Canada
        CountryData ca = new CountryData("CA", "CAN", "Canada");
        ca.addLanguage("en", "English", "en-CA");
        ca.addLanguage("fr", "French", "fr-CA");
        ca.addTimezone("America/Toronto", "Eastern Time", "GMT-5");
        ca.addTimezone("America/Winnipeg", "Central Time", "GMT-6");
        ca.addTimezone("America/Edmonton", "Mountain Time", "GMT-7");
        ca.addTimezone("America/Vancouver", "Pacific Time", "GMT-8");
        ca.addTimezone("America/St_Johns", "Newfoundland Time", "GMT-3:30");
        countryDataMap.put(ca.getIso2(), ca);

        // Germany
        CountryData de = new CountryData("DE", "DEU", "Germany");
        de.addLanguage("de", "German", "de-DE");
        de.addTimezone("Europe/Berlin", "Central European Time", "GMT+1");
        countryDataMap.put(de.getIso2(), de);

        // France
        CountryData fr = new CountryData("FR", "FRA", "France");
        fr.addLanguage("fr", "French", "fr-FR");
        fr.addTimezone("Europe/Paris", "Central European Time", "GMT+1");
        countryDataMap.put(fr.getIso2(), fr);

        // Japan
        CountryData jp = new CountryData("JP", "JPN", "Japan");
        jp.addLanguage("ja", "Japanese", "ja-JP");
        jp.addTimezone("Asia/Tokyo", "Japan Standard Time", "GMT+9");
        countryDataMap.put(jp.getIso2(), jp);

        // China
        CountryData cn = new CountryData("CN", "CHN", "China");
        cn.addLanguage("zh", "Chinese", "zh-CN");
        cn.addTimezone("Asia/Shanghai", "China Standard Time", "GMT+8");
        countryDataMap.put(cn.getIso2(), cn);

        // India
        CountryData in = new CountryData("IN", "IND", "India");
        in.addLanguage("hi", "Hindi", "hi-IN");
        in.addLanguage("en", "English", "en-IN");
        in.addLanguage("bn", "Bengali", "bn-IN");
        in.addLanguage("te", "Telugu", "te-IN");
        in.addLanguage("mr", "Marathi", "mr-IN");
        in.addLanguage("ta", "Tamil", "ta-IN");
        in.addTimezone("Asia/Kolkata", "India Standard Time", "GMT+5:30");
        countryDataMap.put(in.getIso2(), in);

        // Brazil
        CountryData br = new CountryData("BR", "BRA", "Brazil");
        br.addLanguage("pt", "Portuguese", "pt-BR");
        br.addTimezone("America/Sao_Paulo", "Brasilia Time", "GMT-3");
        countryDataMap.put(br.getIso2(), br);

        // Australia
        CountryData au = new CountryData("AU", "AUS", "Australia");
        au.addLanguage("en", "English", "en-AU");
        au.addTimezone("Australia/Sydney", "Australian Eastern Standard Time", "GMT+10");
        au.addTimezone("Australia/Adelaide", "Australian Central Standard Time", "GMT+9:30");
        au.addTimezone("Australia/Perth", "Australian Western Standard Time", "GMT+8");
        countryDataMap.put(au.getIso2(), au);

        // Russia
        CountryData ru = new CountryData("RU", "RUS", "Russia");
        ru.addLanguage("ru", "Russian", "ru-RU");
        ru.addTimezone("Europe/Moscow", "Moscow Standard Time", "GMT+3");
        ru.addTimezone("Asia/Yekaterinburg", "Yekaterinburg Time", "GMT+5");
        ru.addTimezone("Asia/Omsk", "Omsk Standard Time", "GMT+6");
        ru.addTimezone("Asia/Krasnoyarsk", "Krasnoyarsk Time", "GMT+7");
        ru.addTimezone("Asia/Irkutsk", "Irkutsk Time", "GMT+8");
        ru.addTimezone("Asia/Vladivostok", "Vladivostok Time", "GMT+10");
        countryDataMap.put(ru.getIso2(), ru);

        // Mexico
        CountryData mx = new CountryData("MX", "MEX", "Mexico");
        mx.addLanguage("es", "Spanish", "es-MX");
        mx.addTimezone("America/Mexico_City", "Central Standard Time", "GMT-6");
        countryDataMap.put(mx.getIso2(), mx);

        // Spain
        CountryData es = new CountryData("ES", "ESP", "Spain");
        es.addLanguage("es", "Spanish", "es-ES");
        es.addLanguage("ca", "Catalan", "ca-ES");
        es.addLanguage("eu", "Basque", "eu-ES");
        es.addLanguage("gl", "Galician", "gl-ES");
        es.addTimezone("Europe/Madrid", "Central European Time", "GMT+1");
        countryDataMap.put(es.getIso2(), es);

        // Italy
        CountryData it = new CountryData("IT", "ITA", "Italy");
        it.addLanguage("it", "Italian", "it-IT");
        it.addTimezone("Europe/Rome", "Central European Time", "GMT+1");
        countryDataMap.put(it.getIso2(), it);

        // Iceland
        CountryData is = new CountryData("IS", "ISL", "Iceland");
        is.addLanguage("is", "Icelandic", "is-IS");
        is.addTimezone("Atlantic/Reykjavik", "Greenwich Mean Time", "GMT+0");
        countryDataMap.put(is.getIso2(), is);
    }

    /**
     * Generate the region settings HashMap with display names as keys
     */
    private void generateRegionSettingsMap() {
        for (CountryData country : countryDataMap.values()) {
            // For each country, create entries with all language/timezone combinations
            for (LanguageData language : country.getLanguages()) {
                for (TimezoneData timezone : country.getTimezones()) {
                    // Create a unique identifier string for this combination
                    String displayName = country.getName() + " - " + language.getName() + " (" + timezone.getOffset() + ")";

                    // Create the region setting
                    RegionSetting regionSetting = new RegionSetting(
                            country.getIso2(), country.getCode(), country.getName(),
                            language.getIso(), language.getName(), language.getTag(),
                            timezone.getId(), timezone.getDisplayName(), timezone.getOffset()
                    );

                    // Add to map with display name as key
                    regionSettingsMap.put(displayName, regionSetting);
                }
            }
        }
    }

    /**
     * Get the complete HashMap of display names to region settings
     */
    public HashMap<String, RegionSetting> getRegionSettingsMap() {
        return regionSettingsMap;
    }

    /**
     * Get a list of all display names (for dropdown population)
     */
    public List<String> getDisplayNames() {
        List<String> displayNames = new ArrayList<>(regionSettingsMap.keySet());
        Collections.sort(displayNames);
        return displayNames;
    }

    /**
     * Get a random region setting
     */
    public RegionSetting getRandomRegionSetting() {
        List<String> keys = new ArrayList<>(regionSettingsMap.keySet());
        String randomKey = keys.get(random.nextInt(keys.size()));
        return regionSettingsMap.get(randomKey);
    }

    /**
     * Get a region setting by country ISO2 code (randomly selects language and timezone)
     */
    public RegionSetting getRegionSettingByCountry(String countryIso2) {
        CountryData country = countryDataMap.get(countryIso2);
        if (country == null) {
            return null;
        }

        // Randomly select a language for this country
        List<LanguageData> languages = country.getLanguages();
        LanguageData randomLanguage = languages.get(random.nextInt(languages.size()));

        // Randomly select a timezone for this country
        List<TimezoneData> timezones = country.getTimezones();
        TimezoneData randomTimezone = timezones.get(random.nextInt(timezones.size()));

        // Build display name
        String displayName = country.getName() + " - " + randomLanguage.getName() + " (" + randomTimezone.getOffset() + ")";

        // Return existing entry if found
        if (regionSettingsMap.containsKey(displayName)) {
            return regionSettingsMap.get(displayName);
        }

        // Otherwise create a new one
        RegionSetting setting = new RegionSetting(
                country.getIso2(), country.getCode(), country.getName(),
                randomLanguage.getIso(), randomLanguage.getName(), randomLanguage.getTag(),
                randomTimezone.getId(), randomTimezone.getDisplayName(), randomTimezone.getOffset()
        );

        regionSettingsMap.put(displayName, setting);
        return setting;
    }

    /**
     * Convert region setting to JSON format
     */
    public JSONObject regionSettingToJson(RegionSetting setting) {
        JSONObject json = new JSONObject();
        try {
            // Parent control
            json.put("zone.parent.control.tz", "0");

            // Country settings
            json.put("zone.country.name", setting.getCountryName());
            json.put("zone.country.iso2", setting.getCountryIso2());
            json.put("zone.country.code", setting.getCountryCode());

            // Language settings
            json.put("zone.language.name", setting.getLanguageName());
            json.put("zone.language.iso", setting.getLanguageIso());
            json.put("zone.language.tag", setting.getLanguageTag());

            // Timezone settings
            json.put("zone.timezone.offset", setting.getTimezoneOffset());
            json.put("zone.timezone.id", setting.getTimezoneId());
            json.put("zone.timezone.display.name", setting.getTimezoneDisplayName());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Inner class for country data
     */
    private static class CountryData {
        private final String iso2;
        private final String code;
        private final String name;
        private final List<LanguageData> languages = new ArrayList<>();
        private final List<TimezoneData> timezones = new ArrayList<>();

        public CountryData(String iso2, String code, String name) {
            this.iso2 = iso2;
            this.code = code;
            this.name = name;
        }

        public String getIso2() {
            return iso2;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public void addLanguage(String iso, String name, String tag) {
            languages.add(new LanguageData(iso, name, tag));
        }

        public List<LanguageData> getLanguages() {
            return languages;
        }

        public void addTimezone(String id, String displayName, String offset) {
            timezones.add(new TimezoneData(id, displayName, offset));
        }

        public List<TimezoneData> getTimezones() {
            return timezones;
        }
    }

    /**
     * Inner class for language data
     */
    private static class LanguageData {
        private final String iso;
        private final String name;
        private final String tag;

        public LanguageData(String iso, String name, String tag) {
            this.iso = iso;
            this.name = name;
            this.tag = tag;
        }

        public String getIso() {
            return iso;
        }

        public String getName() {
            return name;
        }

        public String getTag() {
            return tag;
        }
    }

    /**
     * Inner class for timezone data
     */
    private static class TimezoneData {
        private final String id;
        private final String displayName;
        private final String offset;

        public TimezoneData(String id, String displayName, String offset) {
            this.id = id;
            this.displayName = displayName;
            this.offset = offset;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getOffset() {
            return offset;
        }
    }

    /**
     * Class to hold the complete region setting data
     */
    public static class RegionSetting {
        private final String countryIso2;
        private final String countryCode;
        private final String countryName;
        private final String languageIso;
        private final String languageName;
        private final String languageTag;
        private final String timezoneId;
        private final String timezoneDisplayName;
        private final String timezoneOffset;

        public RegionSetting(String countryIso2, String countryCode, String countryName,
                             String languageIso, String languageName, String languageTag,
                             String timezoneId, String timezoneDisplayName, String timezoneOffset) {
            this.countryIso2 = countryIso2;
            this.countryCode = countryCode;
            this.countryName = countryName;
            this.languageIso = languageIso;
            this.languageName = languageName;
            this.languageTag = languageTag;
            this.timezoneId = timezoneId;
            this.timezoneDisplayName = timezoneDisplayName;
            this.timezoneOffset = timezoneOffset;
        }

        public String getCountryIso2() {
            return countryIso2;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public String getCountryName() {
            return countryName;
        }

        public String getLanguageIso() {
            return languageIso;
        }

        public String getLanguageName() {
            return languageName;
        }

        public String getLanguageTag() {
            return languageTag;
        }

        public String getTimezoneId() {
            return timezoneId;
        }

        public String getTimezoneDisplayName() {
            return timezoneDisplayName;
        }

        public String getTimezoneOffset() {
            return timezoneOffset;
        }
    }
}