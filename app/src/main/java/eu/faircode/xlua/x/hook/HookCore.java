package eu.faircode.xlua.x.hook;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import org.lsposed.hiddenapibypass.HiddenApiBypass;
import org.luaj.vm2.Globals;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.Varargs;

import java.lang.reflect.Member;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import eu.faircode.xlua.xposed.api101.ModernMethodHook;
import eu.faircode.xlua.xposed.api101.ModernXposedBridge;
import eu.faircode.xlua.xposed.api101.ModernLoadPackage;
import eu.faircode.xlua.BuildConfig;
import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.api.hook.XLuaHook;
import eu.faircode.xlua.hooks.LuaHookWrapper;
import eu.faircode.xlua.hooks.LuaScriptHolder;
import eu.faircode.xlua.hooks.XHookUtil;
import eu.faircode.xlua.logger.XReport;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.hook.filter.HookRepository;
import eu.faircode.xlua.x.hook.inlined.HashMapHooks;
import eu.faircode.xlua.x.hook.inlined.UpTimeHooks;
import eu.faircode.xlua.x.runtime.HiddenApi;
import eu.faircode.xlua.x.runtime.AndroidVersionSpoofer;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.runtime.reflect.DynamicField;
import eu.faircode.xlua.x.runtime.reflect.ReflectUtil;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.xlua.LibUtil;
import eu.faircode.xlua.x.xlua.commands.query.GetAssignedHooksExCommand;
import eu.faircode.xlua.x.xlua.hook.PackageHookContext;

public class HookCore {
    private static final String TAG = LibUtil.generateTag(HookCore.class);


    public static void initHooks(final ModernLoadPackage.LoadPackageParam loadParam, int uid, final Context context) {
        try {
            // Capture the host API before any profile data is applied. Hook
            // compatibility must always be decided by the Android runtime
            // that actually owns this process.
            final int runtimeSdk = Build.VERSION.SDK_INT;
            //Ask to Force Bypass
            boolean bypassed = HiddenApi.bypassHiddenApiRestrictionsClassLoader(loadParam.classLoader);
            final PackageHookContext app = PackageHookContext.create(loadParam, uid, context);

            // Normalize legacy/cross-version saved settings back to the host
            // Android generation before resolving any target-app classes.
            AndroidVersionSpoofer.normalizeSettings(app.settings);
            AndroidVersionSpoofer.installSystemPropertyHooks(app.settings);
            AndroidVersionSpoofer.applyFrameworkFields(app.settings);

            final Collection<XHook> hooks = GetAssignedHooksExCommand.get(context, true, uid, loadParam.packageName);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Loading Hooks for Package=%s UID=%s Hooks Count=%s Settings Count=%s Bypassed Hidden Api=%s",
                        loadParam.packageName,
                        uid,
                        hooks.size(),
                        app.settings.size(),
                        bypassed));

            Map<LuaScriptHolder, Prototype> scriptPrototype = new HashMap<>();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            for(XHook hook :  HookRepository.create().initializeHooks(context, hooks, app.settings).getHooks()) {
                // Old assignments can outlive an OS upgrade or an imported backup.
                // Reject incompatible hooks before resolving their classes so an API
                // from another Android generation is never touched.
                if (runtimeSdk < hook.minSdk || runtimeSdk > hook.maxSdk)
                    continue;
                List<HookDefinition> definitions = HookResolver.resolveHook(context, context.getClassLoader(), hook);
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Creating Hook [%s][%s] Class [%s] Method [%s] Definitions Found Count=%s Pkg=%s",
                            hook.getObjectId(),
                            hook.name,
                            hook.getResolvedClassName(),
                            hook.methodName,
                            definitions.size(),
                            loadParam.packageName));

                if(definitions.isEmpty())
                    continue;

                for(HookDefinition definition : definitions) {
                    //get time & Compile Script
                    definition.setAccessible(true);
                    final long install = SystemClock.elapsedRealtime();
                    final Prototype compiledScript = XHookUtil.compileScript(scriptPrototype, hook);
                    if(!hook.isAvailable(pInfo.versionCode))
                        continue;

                    if(DebugUtil.isDebug())
                        Log.d(TAG, Str.fm("Deploying Hook [%s][%s] Class [%s] Method [%s] Pkg=%s Definition:%s ",
                                hook.getObjectId(),
                                hook.name,
                                hook.getResolvedClassName(),
                                hook.methodName,
                                loadParam.packageName,
                                Str.toStringOrNull(definition)));

                    if(definition instanceof HookDefinitionAll) {
                        try {
                            if(DebugUtil.isDebug())
                                Log.d(TAG, "Definition is a (ALL) Hook! " + definition);

                            ModernXposedBridge.hookAllMethods(definition.resolvedClazz, definition.getName(), new ModernMethodHook() {
                                private final WeakHashMap<Thread, Globals> threadGlobals = new WeakHashMap<>();
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param)  { execute(param, "before"); }
                                @Override
                                protected void afterHookedMethod(MethodHookParam param)  { execute(param, "after"); }

                                private void execute(MethodHookParam param, String function) {
                                    long run = SystemClock.elapsedRealtime();
                                    try {
                                        LuaHookWrapper luaMember;
                                        synchronized (threadGlobals) {
                                            Thread thread = Thread.currentThread();
                                            if (!threadGlobals.containsKey(thread))
                                                threadGlobals.put(thread, XHookUtil.getHookGlobals(
                                                        context,
                                                        hook,
                                                        app.settings,
                                                        app.buildPropSettings,
                                                        app.buildPropMaps,
                                                        app.temporaryKey,
                                                        app.useDefault,
                                                        app.packageName));

                                            Globals globals = threadGlobals.get(thread);
                                            luaMember = LuaHookWrapper
                                                    .createMember(
                                                            context,
                                                            hook,
                                                            app.settings,
                                                            app.buildPropSettings,
                                                            app.buildPropMaps,
                                                            compiledScript,
                                                            function,
                                                            param,
                                                            globals,
                                                            app.temporaryKey,
                                                            app.useDefault,
                                                            app.packageName);

                                            if(!luaMember.isValid()) {
                                                if(BuildConfig.DEBUG)
                                                    Log.w(TAG, Str.fm("Lua Member(ALL) is Not Valid [%s] Most likely not a after or before, function=%s", definition.getName(), function));
                                                return;
                                            }
                                        }

                                        Varargs result = luaMember.invoke();
                                        //Log.d(TAG, "Result=" + Str.toStringOrNull(result));
                                        XReport.usage(hook, result, run, function, context);
                                    }catch (Exception ex) {
                                        synchronized (threadGlobals) {
                                            threadGlobals.remove(Thread.currentThread());
                                        }

                                        //Fix in add in
                                        //XReport.memberException(context, ex, hook, member, function, param);
                                    }

                                    if (DebugUtil.isDebug())
                                        XReport.install(hook, install, context);
                                }
                            });
                        }catch (Exception e) {
                            Log.e(TAG, "Failed to Deploy ALL Members Hook: " + Str.toStringOrNull(definition) + " Hook:" + hook.getObjectId() + " Error=" + e);
                        }
                    }
                    else if(definition instanceof HookDefinitionField) {
                        final HookDefinitionField hf = (HookDefinitionField) definition;
                        try {
                            long run = SystemClock.elapsedRealtime();
                            LuaHookWrapper luaField = LuaHookWrapper.createField(
                                    context,
                                    hook,
                                    app.settings,
                                    app.buildPropSettings,
                                    app.buildPropMaps,
                                    compiledScript,
                                    hf.field,
                                    app.temporaryKey,
                                    app.useDefault,
                                    app.packageName);

                            if(!luaField.isValid()) {
                                Log.e(TAG, Str.fm("Skipping Field Hook Field: %s is not a After Hook, Field Hooks can not be before Hooks!", hf.getName()));
                                continue;
                            }

                            Varargs result = luaField.invoke();
                            XReport.usage(hook, result, run, XReport.FUNCTION_AFTER, context);
                        }catch (Exception e) {
                            XReport.fieldException(context, e, hook, hf.field);
                            Log.e(TAG, "Failed to Invoke Field Hook: " + Str.toStringOrNull(definition) + " Hook:" + hook.getObjectId() + " Error=" + e);
                        }
                    }
                    else if(definition instanceof HookDefinitionMember) {
                        try {
                            final Member member = definition.member;
                            if(member == null) {
                                Log.e(TAG, "Critical Error, Member is Some how NULL! Hook:" + hook.getObjectId() + " Class=" + hook.getResolvedClassName() + " Definition:" + definition);
                                continue;
                            }

                            if (HashMapHooks.attach(hook, member, app) || UpTimeHooks.attach(hook, member)) {
                                Log.i(TAG, "Inlined Deployed [" + hook.getObjectId() + "]");
                                continue;
                            }

                            if(DebugUtil.isDebug()) Log.d(TAG, "Deploying Member Hook! Definition:" + definition);

                            ModernXposedBridge.hookMethod(member, new ModernMethodHook() {
                                private final WeakHashMap<Thread, Globals> threadGlobals = new WeakHashMap<>();
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param)  { execute(param, "before"); }
                                @Override
                                protected void afterHookedMethod(MethodHookParam param)  { execute(param, "after"); }

                                private void execute(MethodHookParam param, String function) {
                                    long run = SystemClock.elapsedRealtime();
                                    try {
                                        LuaHookWrapper luaMember;
                                        synchronized (threadGlobals) {
                                            Thread thread = Thread.currentThread();
                                            if (!threadGlobals.containsKey(thread))
                                                threadGlobals.put(thread, XHookUtil.getHookGlobals(
                                                        context,
                                                        hook,
                                                        app.settings,
                                                        app.buildPropSettings,
                                                        app.buildPropMaps,
                                                        app.temporaryKey,
                                                        app.useDefault,
                                                        app.packageName));

                                            Globals globals = threadGlobals.get(thread);
                                            luaMember = LuaHookWrapper
                                                    .createMember(
                                                            context,
                                                            hook,
                                                            app.settings,
                                                            app.buildPropSettings,
                                                            app.buildPropMaps,
                                                            compiledScript,
                                                            function,
                                                            param,
                                                            globals,
                                                            app.temporaryKey,
                                                            app.useDefault,
                                                            app.packageName);

                                            if(!luaMember.isValid()) {
                                                if(BuildConfig.DEBUG)
                                                    Log.w(TAG, Str.fm("Lua Member is Not Valid [%s] Most likely not a after or before, function=%s", definition.getName(), function));
                                                return;
                                            }
                                        }

                                        Varargs result = luaMember.invoke();
                                        XReport.usage(hook, result, run, function, context);
                                    }catch (Exception ex) {
                                        synchronized (threadGlobals) { threadGlobals.remove(Thread.currentThread()); }
                                        XReport.memberException(context, ex, hook, definition.member, function, param);
                                        Log.e(TAG, "Failed Invoking the Hook: " + Str.toStringOrNull(definition) + " Hook:" + hook.getObjectId() + " Error=" + ex + " Stack=" + RuntimeUtils.getStackTraceSafeString(ex));
                                    }
                                }
                            });

                            if (DebugUtil.isDebug()) {
                                Log.d(TAG, "Deployed Member Hook! Definition:" + definition);
                                XReport.install(hook, install, context);
                            }
                        }catch (Exception e) {
                            Log.e(TAG, "Failed To Deploy Hook! Error=" + e + " Hook=" + Str.toStringOrNull(hook));
                        }
                    }
                }
            }

            // Field hooks run above and older saved configurations may contain a
            // stale standalone SDK value. The firmware fingerprint is authoritative,
            // so enforce the coherent current/launch versions one final time.
            AndroidVersionSpoofer.normalizeSettings(app.settings);
            AndroidVersionSpoofer.applyFrameworkFields(app.settings);

        }catch (Exception e) {
            Log.e(TAG, "Failed to InitHooks! UID=" + uid + " Error=" + e);
        }
    }
}
