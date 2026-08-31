package eu.faircode.xlua.xposed.api101;

import android.util.Log;

import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.libxposed.api.XposedInterface;

/**
 * The single compiled bridge from XPL-EX's hook engine to libxposed API 101.
 */
public final class ModernXposedBridge {
    private static final String TAG = "XPL-EX/API101";
    private static final AtomicBoolean firstHookExecutionLogged = new AtomicBoolean();
    private static volatile XposedInterface framework;

    private ModernXposedBridge() {
    }

    public static void attach(XposedInterface value) {
        if (value == null)
            throw new IllegalArgumentException("framework == null");
        XposedInterface current = framework;
        if (current != null && current != value)
            throw new IllegalStateException("A different libxposed interface is already attached");
        framework = value;
    }

    public static boolean isAttached() {
        return framework != null;
    }

    public static XposedInterface.HookHandle hookMethod(Member member, ModernMethodHook callback) {
        if (!(member instanceof Executable))
            throw new IllegalArgumentException("Only methods and constructors can be hooked: " + member);
        if (callback == null)
            throw new IllegalArgumentException("callback == null");

        XposedInterface api = requireFramework();
        Executable executable = (Executable) member;
        return api.hook(executable)
                .setExceptionMode(XposedInterface.ExceptionMode.PASSTHROUGH)
                .intercept(chain -> dispatch(chain, callback));
    }

    public static Set<XposedInterface.HookHandle> hookAllMethods(
            Class<?> type, String methodName, ModernMethodHook callback) {
        if (type == null)
            throw new IllegalArgumentException("type == null");
        if (methodName == null)
            throw new IllegalArgumentException("methodName == null");

        Set<XposedInterface.HookHandle> handles = new LinkedHashSet<>();
        for (Method method : type.getDeclaredMethods()) {
            if (methodName.equals(method.getName()))
                handles.add(hookMethod(method, callback));
        }
        return Collections.unmodifiableSet(handles);
    }

    private static Object dispatch(XposedInterface.Chain chain, ModernMethodHook callback)
            throws Throwable {
        if (firstHookExecutionLogged.compareAndSet(false, true))
            log("First hook executed: " + chain.getExecutable());

        Object[] args = chain.getArgs().toArray(new Object[0]);
        ModernMethodHook.MethodHookParam param = new ModernMethodHook.MethodHookParam(
                chain.getExecutable(), chain.getThisObject(), args);

        try {
            callback.beforeHookedMethod(param);
        } catch (Throwable callbackError) {
            // Match legacy XC_MethodHook behavior: a failing before callback must
            // not accidentally keep an early result/throwable and skip the method.
            param.restore(null, null, false);
            log(callbackError);
        }

        if (!param.isReturnEarly()) {
            try {
                param.setInvocationResult(chain.proceed(param.args));
            } catch (Throwable invocationError) {
                param.setInvocationThrowable(invocationError);
            }
        }

        Object resultBeforeAfter = param.getResult();
        Throwable throwableBeforeAfter = param.getThrowable();
        boolean returnEarlyBeforeAfter = param.isReturnEarly();
        try {
            callback.afterHookedMethod(param);
        } catch (Throwable callbackError) {
            param.restore(resultBeforeAfter, throwableBeforeAfter, returnEarlyBeforeAfter);
            log(callbackError);
        }

        if (param.hasThrowable())
            throw param.getThrowable();
        return param.getResult();
    }

    private static XposedInterface requireFramework() {
        XposedInterface value = framework;
        if (value == null)
            throw new IllegalStateException("libxposed API 101 framework is not attached");
        return value;
    }

    public static void log(String message) {
        XposedInterface value = framework;
        if (value == null)
            Log.i(TAG, message == null ? "null" : message);
        else
            value.log(Log.INFO, TAG, message == null ? "null" : message);
    }

    public static void log(Throwable throwable) {
        XposedInterface value = framework;
        if (value == null)
            Log.e(TAG, throwable == null ? "null" : Log.getStackTraceString(throwable));
        else
            value.log(Log.ERROR, TAG,
                    throwable == null ? "null" : throwable.toString(), throwable);
    }
}
