package eu.faircode.xlua;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LocalizedResultResourcesTest {
    @Test
    public void savedSettingsSummaryFormatsCountsInBothLanguages() throws Exception {
        String english = stringValue(file("src/main/res/values/strings.xml"));
        String russian = stringValue(file("src/main/res/values-ru/legacy_ui_strings.xml"));

        assertEquals("Updated 113 Settings! Failed Count 0",
                String.format(Locale.ENGLISH, english, 113, 0));
        Locale russianLocale = Locale.forLanguageTag("ru");
        assertEquals("Обновлено настроек: 113; ошибок: 0",
                String.format(russianLocale, russian, 113, 0));
        assertFalse(String.format(russianLocale, russian, 113, 0).contains("%1$"));
    }

    private static File file(String relativePath) {
        File file = new File(relativePath);
        if (!file.isFile())
            file = new File("app/" + relativePath);
        return file;
    }

    private static String stringValue(File file) throws Exception {
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(file);
        NodeList strings = document.getElementsByTagName("string");
        for (int index = 0; index < strings.getLength(); index++) {
            Element element = (Element) strings.item(index);
            if ("result_settings_update".equals(element.getAttribute("name")))
                return element.getTextContent().trim();
        }
        return null;
    }
}
