package eu.faircode.xlua.utilities;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class AppLanguageTest {
    @Test
    public void supportsOnlySystemEnglishRussianAndChineseSelections() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.normalizeSelection(null));
        assertEquals(AppLanguage.SYSTEM, AppLanguage.normalizeSelection("de"));
        assertEquals(AppLanguage.ENGLISH, AppLanguage.normalizeSelection("en"));
        assertEquals(AppLanguage.RUSSIAN, AppLanguage.normalizeSelection("ru"));
        assertEquals(AppLanguage.CHINESE, AppLanguage.normalizeSelection("zh"));
    }

    @Test
    public void systemRussianUsesRussianAndOtherLanguagesUseEnglish() {
        assertEquals(AppLanguage.RUSSIAN, AppLanguage.languageForSystem("ru"));
        assertEquals(AppLanguage.RUSSIAN, AppLanguage.languageForSystem("RU"));
        assertEquals(AppLanguage.ENGLISH, AppLanguage.languageForSystem("en"));
        assertEquals(AppLanguage.ENGLISH, AppLanguage.languageForSystem("de"));
    }

    @Test
    public void systemChineseUsesSimplifiedChinese() {
        assertEquals(AppLanguage.CHINESE, AppLanguage.languageForSystem("zh"));
        assertEquals(AppLanguage.CHINESE, AppLanguage.languageForSystem("ZH"));
        assertEquals(AppLanguage.CHINESE, AppLanguage.languageForSystem("zh_CN"));
    }

    @Test
    public void chineseLocaleCarriesRegionForResourceSelection() {
        Locale locale = AppLanguage.toLocale(AppLanguage.CHINESE);
        assertEquals("zh", locale.getLanguage());
        assertEquals("CN", locale.getCountry());
        assertEquals(new Locale(AppLanguage.RUSSIAN), AppLanguage.toLocale(AppLanguage.RUSSIAN));
        assertEquals(Locale.ENGLISH, AppLanguage.toLocale(AppLanguage.ENGLISH));
    }
}
