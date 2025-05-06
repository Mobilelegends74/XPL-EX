package eu.faircode.xlua.x.xlua.settings.random.randomizers.region;

import eu.faircode.xlua.x.xlua.settings.random.RandomElement;
import eu.faircode.xlua.x.xlua.settings.random.RandomizerSessionContext;
import eu.faircode.xlua.x.xlua.settings.random.randomizers.RandomizersCache;
import eu.faircode.xlua.x.xlua.settings.random.utils.RegionSettingsHashMap;

public class RandomRegionParent extends RandomElement {
    private final RegionSettingsHashMap regionSettingsMap = new RegionSettingsHashMap();

    public RandomRegionParent() {
        super("Region Control Parent");
        this.isParent = true;
        putSettings(RandomizersCache.SETTING_ZONE_PARENT);

        putRequirements(
                RandomizersCache.SETTING_ZONE_COUNTRY_NAME,
                //RandomizersCache.SETTING_ZONE_COUNTRY_ISO2,
                RandomizersCache.SETTING_ZONE_COUNTRY_CODE,
                RandomizersCache.SETTING_ZONE_LANGUAGE_NAME,
                RandomizersCache.SETTING_ZONE_LANGUAGE_ISO,
                RandomizersCache.SETTING_ZONE_LANGUAGE_TAG,
                RandomizersCache.SETTING_ZONE_TIMEZONE_OFFSET,
                RandomizersCache.SETTING_ZONE_TIMEZONE_ID,
                RandomizersCache.SETTING_ZONE_TIMEZONE_DISPLAY_NAME);
    }

    @Override
    public void randomize(RandomizerSessionContext context) {
        // Create the region settings generator

        // Get a random region setting
        RegionSettingsHashMap.RegionSetting randomSetting = regionSettingsMap.getRandomRegionSetting();

        // Push all region settings to context
        context.pushValue(RandomizersCache.SETTING_ZONE_COUNTRY_NAME, randomSetting.getCountryName());
        //context.pushValue(RandomizersCache.SETTING_ZONE_COUNTRY_ISO2, randomSetting.getCountryIso2());
        context.pushValue(RandomizersCache.SETTING_ZONE_COUNTRY_CODE, randomSetting.getCountryCode());
        context.pushValue(RandomizersCache.SETTING_ZONE_LANGUAGE_NAME, randomSetting.getLanguageName());
        context.pushValue(RandomizersCache.SETTING_ZONE_LANGUAGE_ISO, randomSetting.getLanguageIso());
        context.pushValue(RandomizersCache.SETTING_ZONE_LANGUAGE_TAG, randomSetting.getLanguageTag());
        context.pushValue(RandomizersCache.SETTING_ZONE_TIMEZONE_OFFSET, randomSetting.getTimezoneOffset());
        context.pushValue(RandomizersCache.SETTING_ZONE_TIMEZONE_ID, randomSetting.getTimezoneId());
        context.pushValue(RandomizersCache.SETTING_ZONE_TIMEZONE_DISPLAY_NAME, randomSetting.getTimezoneDisplayName());

        // Push special value
        context.pushSpecial(context.stack.pop(), randomSetting.getCountryName());
    }

    // Country Name Randomizer
    public static class RandomRegionCountryName extends RandomElement {
        public RandomRegionCountryName() {
            super("Region Country Name");
            putSettings(RandomizersCache.SETTING_ZONE_COUNTRY_NAME);
            putParents(RandomizersCache.SETTING_ZONE_PARENT);
        }

        @Override
        public void randomize(RandomizerSessionContext context) {
            RegionSettingsHashMap regionSettingsMap = new RegionSettingsHashMap();
            RegionSettingsHashMap.RegionSetting randomSetting = regionSettingsMap.getRandomRegionSetting();
            context.pushValue(context.stack.pop(), randomSetting.getCountryName());
        }
    }

    // Country ISO2 Randomizer
    /*public static class RandomRegionCountryIso2 extends RandomElement {
        public RandomRegionCountryIso2() {
            super("Region Country ISO2 Code");
            putSettings(RandomizersCache.SETTING_ZONE_COUNTRY_ISO2);
            putParents(RandomizersCache.SETTING_ZONE_PARENT);
        }

        @Override
        public void randomize(RandomizerSessionContext context) {
            RegionSettingsHashMap regionSettingsMap = new RegionSettingsHashMap();
            RegionSettingsHashMap.RegionSetting randomSetting = regionSettingsMap.getRandomRegionSetting();
            context.pushValue(context.stack.pop(), randomSetting.getCountryIso2());
        }
    }*/

    // Country Code Randomizer
    public static class RandomRegionCountryCode extends RandomElement {
        public RandomRegionCountryCode() {
            super("Region Country Code");
            putSettings(RandomizersCache.SETTING_ZONE_COUNTRY_CODE);
            putParents(RandomizersCache.SETTING_ZONE_PARENT);
        }

        @Override
        public void randomize(RandomizerSessionContext context) {
            RegionSettingsHashMap regionSettingsMap = new RegionSettingsHashMap();
            RegionSettingsHashMap.RegionSetting randomSetting = regionSettingsMap.getRandomRegionSetting();
            context.pushValue(context.stack.pop(), randomSetting.getCountryCode());
        }
    }

    // Language Name Randomizer
    public static class RandomRegionLanguageName extends RandomElement {
        public RandomRegionLanguageName() {
            super("Region Language Name");
            putSettings(RandomizersCache.SETTING_ZONE_LANGUAGE_NAME);
            putParents(RandomizersCache.SETTING_ZONE_PARENT);
        }

        @Override
        public void randomize(RandomizerSessionContext context) {
            RegionSettingsHashMap regionSettingsMap = new RegionSettingsHashMap();
            RegionSettingsHashMap.RegionSetting randomSetting = regionSettingsMap.getRandomRegionSetting();
            context.pushValue(context.stack.pop(), randomSetting.getLanguageName());
        }
    }

    // Language ISO Randomizer
    public static class RandomRegionLanguageIso extends RandomElement {
        public RandomRegionLanguageIso() {
            super("Region Language ISO Code");
            putSettings(RandomizersCache.SETTING_ZONE_LANGUAGE_ISO);
            putParents(RandomizersCache.SETTING_ZONE_PARENT);
        }

        @Override
        public void randomize(RandomizerSessionContext context) {
            RegionSettingsHashMap regionSettingsMap = new RegionSettingsHashMap();
            RegionSettingsHashMap.RegionSetting randomSetting = regionSettingsMap.getRandomRegionSetting();
            context.pushValue(context.stack.pop(), randomSetting.getLanguageIso());
        }
    }

    // Language Tag Randomizer
    public static class RandomRegionLanguageTag extends RandomElement {
        public RandomRegionLanguageTag() {
            super("Region Language Tag");
            putSettings(RandomizersCache.SETTING_ZONE_LANGUAGE_TAG);
            putParents(RandomizersCache.SETTING_ZONE_PARENT);
        }

        @Override
        public void randomize(RandomizerSessionContext context) {
            RegionSettingsHashMap regionSettingsMap = new RegionSettingsHashMap();
            RegionSettingsHashMap.RegionSetting randomSetting = regionSettingsMap.getRandomRegionSetting();
            context.pushValue(context.stack.pop(), randomSetting.getLanguageTag());
        }
    }

    // Timezone Offset Randomizer
    public static class RandomRegionTimezoneOffset extends RandomElement {
        public RandomRegionTimezoneOffset() {
            super("Region Timezone Offset");
            putSettings(RandomizersCache.SETTING_ZONE_TIMEZONE_OFFSET);
            putParents(RandomizersCache.SETTING_ZONE_PARENT);
        }

        @Override
        public void randomize(RandomizerSessionContext context) {
            RegionSettingsHashMap regionSettingsMap = new RegionSettingsHashMap();
            RegionSettingsHashMap.RegionSetting randomSetting = regionSettingsMap.getRandomRegionSetting();
            context.pushValue(context.stack.pop(), randomSetting.getTimezoneOffset());
        }
    }

    // Timezone ID Randomizer
    public static class RandomRegionTimezoneId extends RandomElement {
        public RandomRegionTimezoneId() {
            super("Region Timezone ID");
            putSettings(RandomizersCache.SETTING_ZONE_TIMEZONE_ID);
            putParents(RandomizersCache.SETTING_ZONE_PARENT);
        }

        @Override
        public void randomize(RandomizerSessionContext context) {
            RegionSettingsHashMap regionSettingsMap = new RegionSettingsHashMap();
            RegionSettingsHashMap.RegionSetting randomSetting = regionSettingsMap.getRandomRegionSetting();
            context.pushValue(context.stack.pop(), randomSetting.getTimezoneId());
        }
    }

    // Timezone Display Name Randomizer
    public static class RandomRegionTimezoneDisplayName extends RandomElement {
        public RandomRegionTimezoneDisplayName() {
            super("Region Timezone Display Name");
            putSettings(RandomizersCache.SETTING_ZONE_TIMEZONE_DISPLAY_NAME);
            putParents(RandomizersCache.SETTING_ZONE_PARENT);
        }

        @Override
        public void randomize(RandomizerSessionContext context) {
            RegionSettingsHashMap regionSettingsMap = new RegionSettingsHashMap();
            RegionSettingsHashMap.RegionSetting randomSetting = regionSettingsMap.getRandomRegionSetting();
            context.pushValue(context.stack.pop(), randomSetting.getTimezoneDisplayName());
        }
    }
}