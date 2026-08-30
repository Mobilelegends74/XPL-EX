package eu.faircode.xlua.x.runtime.reflect;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class StaticFieldWriterTest {
    private static final String ORIGINAL_STRING = new String("original");
    private static final int ORIGINAL_INT = Integer.parseInt("35");

    @Test
    public void writesStaticFinalObjectAndPrimitiveFields() throws Throwable {
        Field stringField = StaticFieldWriterTest.class.getDeclaredField("ORIGINAL_STRING");
        Field intField = StaticFieldWriterTest.class.getDeclaredField("ORIGINAL_INT");

        StaticFieldWriter.set(stringField, "Android 11");
        StaticFieldWriter.set(intField, 30);

        assertEquals("Android 11", stringField.get(null));
        assertEquals(30, intField.getInt(null));
    }
}
