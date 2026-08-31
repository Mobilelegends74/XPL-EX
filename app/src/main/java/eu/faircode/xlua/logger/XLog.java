package eu.faircode.xlua.logger;

import android.util.Log;

import org.luaj.vm2.Varargs;

import eu.faircode.xlua.xposed.api101.ModernXposedBridge;
import eu.faircode.xlua.api.hook.XLuaHook;

public class XLog {
    //Can be set for future
    private static boolean ENFORCE_CAT_LOG = true;

    private XLuaHook hook;
    private Varargs result;
    private long startTime;
    private String functionName;
    private String exception;





    //public XLog(XLuaHook hook, )

    //public static XLog create(XLuaHook hook, String packageName, int uid, long time, String data) {
    //    return null;
    //}

    //
    //hook
    //packageName
    //uid
    //event => (use .....)
    //time
    //data  =>  [function]
    //          [restricted]
    //          [duration]
    //              [old]
    //              [new]
    //
    //


    ///XLuaHook hook, Varargs result, long startTime, String funName
    //public static XLog create() { return new XLog(); }

    //public static XLog setHook()

    //public static void e(boolean xposedLog, String tag, String msg) { e(xposedLog, tag, msg, null, null, null, false, true); }

    public static void e(boolean xposedLog, String tag, String msg) { e(xposedLog, tag, msg, null, null, null, false, true); }

    public static void e(boolean xposedLog, String tag, String msg, Throwable e, boolean dumpStack) { e(xposedLog, tag, msg, e, null, null, dumpStack, true); }
    public static void e(boolean xposedLog, String tag, String msg, Throwable e) { e(xposedLog, tag, msg, e, null, null, false, true); }
    public static void e(boolean xposedLog, String tag, String msg, Throwable e, String className, String methodName) { e(xposedLog, tag, msg, e, className, methodName, false, false); }
    public static void e(boolean xposedLog, String tag, String msg, Throwable e, String className, String methodName, boolean dumpStack) { e(xposedLog, tag, msg, e, className, methodName, dumpStack, false); }


    public static void e(String msg) { e(false, null, msg, null, null, null, false, true); }

    public static void e(Throwable e) { e(false, null, null, e, null, null, false, true); }
    public static void e(Throwable e, boolean dumpStack) { e(false, null, null, e, null, null, dumpStack, true); }
    public static void e(String msg, Throwable e) { e(false, null, msg, e, null, null, false, true); }
    public static void e(String msg, Throwable e, boolean dumpStack) { e(false, null, msg, e, null, null, dumpStack, true); }


    public static void e(String tag, String msg) { e(false, tag, msg, null, null, null, false, true); }
    public static void e(String tag, String msg, boolean dumpStack) { e(false, tag, msg, null, null, null, dumpStack, true); }
    public static void e(String tag, String msg, Throwable e, boolean dumpStack) { e(false, tag, msg, e, null, null, dumpStack, true); }
    public static void e(String tag, String msg, Throwable e, String className, String methodName) { e(false, tag, msg, e, className, methodName, true, false); }
    public static void e(String tag, String msg, Throwable e, String className, String methodName, boolean dumpStack) { e(false, tag, msg, e, className, methodName, dumpStack, false); }
    public static void e(String tag, String msg, Throwable e, String className, String methodName, boolean dumpStack, boolean getLastCall) { e(false, tag, msg, e, className, methodName, dumpStack, getLastCall); }

    public static void e(boolean xposedLog, String tag, String msg, Throwable e, String className, String methodName, boolean dumpStack, boolean getLastCall) {
        if(ENFORCE_CAT_LOG) {
            XBasicLog log = XBasicLog.create(tag, msg, e, className, methodName, dumpStack, getLastCall);
            Log.e(log.tag, log.message);
            if(xposedLog) ModernXposedBridge.log(log.toString());
        }
    }

    public static void i(boolean xposedLog, String tag, String msg, Class<?> clazz, String methodName) { i(xposedLog, tag, msg, clazz.getName(), methodName, false, false); }
    public static void i(boolean xposedLog, String tag, String msg, Class<?> clazz, String methodName, boolean dumpStack) { i(xposedLog, tag, msg, clazz.getName(), methodName, dumpStack, false); }

    public static void i(String msg) { i(false, null, msg, null, null, false, true); }
    public static void i(String tag, String msg) { i(false, tag, msg, null, null, false, true); }

    public static void i(boolean xposedLog, String msg) { i(xposedLog, null, msg, null, null, false, true); }
    public static void i(boolean xposedLog, String tag, String msg) { i(xposedLog, tag, msg, null, null, false, true); }

    public static void i(String tag, String msg, String className, String methodName) { i(false, tag, msg, className, methodName, false, false); }
    public static void i(String tag, String msg, String className, String methodName, boolean dumpStack) { i(false, tag, msg, className, methodName, dumpStack, false); }

    public static void i(boolean exposedLog, String msg, String className, String methodName) { i(exposedLog, null, msg, className, methodName, false, true); }
    public static void i(boolean xposedLog, String tag, String msg, String className, String methodName, boolean dumpStack) { i(xposedLog, tag, msg, className, methodName, dumpStack, false); }

    public static void i(String tag, String msg, boolean dumpStack, boolean getLastCall) { i(false, tag, msg, null, null, dumpStack, getLastCall); }
    public static void i(boolean xposedLog, String tag, String msg, boolean dumpStack, boolean getLastCall) { i(xposedLog, tag, msg, null, null, dumpStack, getLastCall); }
    public static void i(boolean xposedLog, String tag, String msg, String className, String methodName, boolean dumpStack, boolean getLastCall) {
        if(ENFORCE_CAT_LOG) {
            XBasicLog log = XBasicLog.create(tag, msg, null, className, methodName, dumpStack, getLastCall);
            Log.i(log.tag, log.message);
            if(xposedLog) ModernXposedBridge.log(log.toString());
        }
    }

    public static void w(boolean xposedLog, String tag, String msg, Class<?> clazz, String methodName) { w(xposedLog, tag, msg, clazz.getName(), methodName, false, false); }
    public static void w(boolean xposedLog, String tag, String msg, Class<?> clazz, String methodName, boolean dumpStack) { w(xposedLog, tag, msg, clazz.getName(), methodName, dumpStack, false); }

    public static void w(String msg) { w(false, null, msg, null, null, false, true); }
    public static void w(String tag, String msg) { w(false, tag, msg, null, null, false, true); }

    public static void w(boolean xposedLog, String msg) { w(xposedLog, null, msg, null, null, false, true); }
    public static void w(boolean xposedLog, String tag, String msg) { w(xposedLog, tag, msg, null, null, false, true); }

    public static void w(String tag, String msg, String className, String methodName) { w(false, tag, msg, className, methodName, false, false); }
    public static void w(String tag, String msg, String className, String methodName, boolean dumpStack) { w(false, tag, msg, className, methodName, dumpStack, false); }

    public static void w(boolean exposedLog, String msg, String className, String methodName) { w(exposedLog, null, msg, className, methodName, false, true); }
    public static void w(boolean xposedLog, String tag, String msg, String className, String methodName, boolean dumpStack) { w(xposedLog, tag, msg, className, methodName, dumpStack, false); }

    public static void w(String tag, String msg, boolean dumpStack, boolean getLastCall) { w(false, tag, msg, null, null, dumpStack, getLastCall); }
    public static void w(boolean xposedLog, String tag, String msg, boolean dumpStack, boolean getLastCall) { w(xposedLog, tag, msg, null, null, dumpStack, getLastCall); }
    public static void w(boolean xposedLog, String tag, String msg, String className, String methodName, boolean dumpStack, boolean getLastCall) {
        if(ENFORCE_CAT_LOG) {
            XBasicLog log = XBasicLog.create(tag, msg, null, className, methodName, dumpStack, getLastCall);
            Log.w(log.tag, log.message);
            if(xposedLog) ModernXposedBridge.log(log.toString());
        }
    }
}
