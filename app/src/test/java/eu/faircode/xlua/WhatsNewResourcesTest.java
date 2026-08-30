package eu.faircode.xlua;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WhatsNewResourcesTest {
    private static final String REPOSITORY = "https://github.com/Mobilelegends74/XPL-EX";

    @Test
    public void englishAndRussianChangelogsContainOnlyOurRepositoryLink() throws Exception {
        String english = stringValue(file("src/main/res/values/strings.xml"), "whats_new");
        String russian = stringValue(file("src/main/res/values-ru/legacy_ui_strings.xml"), "whats_new");

        assertChangelog(english);
        assertChangelog(russian);
        assertEquals("What's new", stringValue(
                file("src/main/res/values/strings.xml"), "menu_whats_new_button")
                .replace("\\'", "'"));
        assertEquals("Что нового", stringValue(
                file("src/main/res/values-ru/strings.xml"), "menu_whats_new_button"));
    }

    private static void assertChangelog(String changelog) {
        assertNotNull(changelog);
        assertTrue(changelog.contains("XPL-EX-NEXT v1.5.6"));
        assertTrue(changelog.contains(REPOSITORY));
        assertEquals(1, occurrences(changelog, "https://"));

        String normalized = changelog.toLowerCase();
        assertFalse(normalized.contains("obbedcode"));
        assertFalse(normalized.contains("t.me/"));
        assertFalse(normalized.contains("xdaforums"));
        assertFalse(normalized.contains("instagram"));
        assertFalse(normalized.contains("nulled"));
    }

    private static File file(String relativePath) {
        File file = new File(relativePath);
        if (!file.isFile())
            file = new File("app/" + relativePath);
        return file;
    }

    private static String stringValue(File file, String name) throws Exception {
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(file);
        NodeList strings = document.getElementsByTagName("string");
        for (int index = 0; index < strings.getLength(); index++) {
            Element element = (Element) strings.item(index);
            if (name.equals(element.getAttribute("name")))
                return element.getTextContent().trim();
        }
        return null;
    }

    private static int occurrences(String value, String target) {
        int count = 0;
        int offset = 0;
        while ((offset = value.indexOf(target, offset)) >= 0) {
            count++;
            offset += target.length();
        }
        return count;
    }
}
