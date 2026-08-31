package eu.faircode.xlua.xposed.api101;

import java.lang.reflect.Member;

/**
 * Compatibility-shaped callback used by the existing XPL-EX Lua engine.
 *
 * <p>This is an internal XPL-EX type, not the legacy Xposed API. It preserves the small
 * before/after parameter surface consumed by the Lua hooks while the actual interception is
 * performed by libxposed API 101 in {@link ModernXposedBridge}.</p>
 */
public abstract class ModernMethodHook {
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    }

    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    }

    public static final class MethodHookParam {
        public final Member method;
        public final Object thisObject;
        public Object[] args;

        private Object result;
        private Throwable throwable;
        private boolean returnEarly;

        MethodHookParam(Member method, Object thisObject, Object[] args) {
            this.method = method;
            this.thisObject = thisObject;
            this.args = args;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
            this.throwable = null;
            this.returnEarly = true;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public boolean hasThrowable() {
            return throwable != null;
        }

        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
            this.result = null;
            this.returnEarly = true;
        }

        public Object getResultOrThrowable() throws Throwable {
            if (throwable != null)
                throw throwable;
            return result;
        }

        boolean isReturnEarly() {
            return returnEarly;
        }

        void setInvocationResult(Object result) {
            this.result = result;
            this.throwable = null;
        }

        void setInvocationThrowable(Throwable throwable) {
            this.throwable = throwable;
            this.result = null;
        }

        void restore(Object result, Throwable throwable, boolean returnEarly) {
            this.result = result;
            this.throwable = throwable;
            this.returnEarly = returnEarly;
        }
    }
}
