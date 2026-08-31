package eu.faircode.xlua.hooks;

import android.content.Context;
import android.util.Log;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.xposed.api101.ModernMethodHook;
import eu.faircode.xlua.xposed.api101.ModernXposedBridge;
import eu.faircode.xlua.XParam;
import eu.faircode.xlua.logger.XLog;

public class LuaHook extends VarArgFunction {
    private Context context;
    private Map<String, String> settings;
    private Map<String, Integer> propSettings;
    private Map<String, String> propMaps;
    private String key;
    private boolean useDefault;
    private String packageName;

    LuaHook(Context context,
            Map<String, String> settings,
            Map<String, Integer> propSettings,
            Map<String, String> propMaps,
            String key,
            boolean useDefault,
            String packageName) {

        this.context = context;
        this.settings = settings;
        this.propSettings = propSettings;
        this.propMaps = propMaps;
        this.key = key;
        this.useDefault = useDefault;
        this.packageName = packageName;
    }

    @Override
    public Varargs invoke(final Varargs args) {
        try {
            Class<?> cls = args.arg(1).checkuserdata().getClass();      //Get Class from Arg(0) or in Lua (1)
            String m = args.arg(2).checkjstring();                      //Get Method Name from Arg(1) or in Lua (2)
            args.arg(3).checkfunction();                                //Check Call Back arg
            final LuaValue fun = args.arg(3);                           //Get Call Back / Function from Arg(2) or in Lua (3)
            final List<LuaValue> xargs = new ArrayList<>();               //Rest of Arguments are Args for the Invoke
            for (int i = 4; i <= args.narg(); i++) xargs.add(args.arg(i));//Append Args

            ModernXposedBridge.hookAllMethods(cls, m, new ModernMethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) { execute("before", param); }

                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    execute("after", param);
                }

                private void execute(String when, MethodHookParam param) {
                    List<LuaValue> values = new ArrayList<>();
                    values.add(LuaValue.valueOf(when));
                    values.add(CoerceJavaToLua.coerce(new XParam(context, param, settings, propSettings, propMaps, key, useDefault, packageName)));
                    for (int i = 0; i < xargs.size(); i++)
                        values.add(xargs.get(i));
                    fun.invoke(values.toArray(new LuaValue[0]));
                }
            });
        }catch (Exception e) { XLog.e("Failed to Hook and or Invoke Hook for Interfaced Function (Dynamic Hook)", e, true); }
        return LuaValue.NIL;
    }
}