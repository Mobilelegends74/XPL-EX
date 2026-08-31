package eu.faircode.xlua.xposed.api101;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ModernMethodHookParamTest {
    @Test
    public void resultAndThrowableFollowLegacyEarlyReturnSemantics() throws Exception {
        Method method = String.class.getDeclaredMethod("length");
        ModernMethodHook.MethodHookParam param = new ModernMethodHook.MethodHookParam(
                method, "value", new Object[0]);

        assertFalse(param.isReturnEarly());
        param.setResult(5);
        assertTrue(param.isReturnEarly());
        assertEquals(5, param.getResult());
        assertNull(param.getThrowable());

        RuntimeException error = new RuntimeException("expected");
        param.setThrowable(error);
        assertTrue(param.isReturnEarly());
        assertNull(param.getResult());
        assertSame(error, param.getThrowable());

        param.restore(7, null, false);
        assertFalse(param.isReturnEarly());
        assertEquals(7, param.getResult());
        assertNull(param.getThrowable());
    }
}
