package eu.faircode.xlua.utilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AppLanguageTest {
    @Test
    public void supportsOnlySystemEnglishAndRussianSelections() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.normalizeSelection(null));
        assertEquals(AppLanguage.SYSTEM, AppLanguage.normalizeSelection("de"));
        assertEquals(AppLanguage.ENGLISH, AppLanguage.normalizeSelection("en"));
        assertEquals(AppLanguage.RUSSIAN, AppLanguage.normalizeSelection("ru"));
    }

    @Test
    public void systemRussianUsesRussianAndOtherLanguagesUseEnglish() {
        assertEquals(AppLanguage.RUSSIAN, AppLanguage.languageForSystem("ru"));
        assertEquals(AppLanguage.RUSSIAN, AppLanguage.languageForSystem("RU"));
        assertEquals(AppLanguage.ENGLISH, AppLanguage.languageForSystem("en"));
        assertEquals(AppLanguage.ENGLISH, AppLanguage.languageForSystem("de"));
    }
}
