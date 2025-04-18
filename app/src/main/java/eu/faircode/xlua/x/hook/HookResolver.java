package eu.faircode.xlua.x.hook;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.faircode.xlua.DebugUtil;
import eu.faircode.xlua.hooks.XHookUtil;
import eu.faircode.xlua.x.Str;
import eu.faircode.xlua.x.data.utils.ArrayUtils;
import eu.faircode.xlua.x.data.utils.ListUtil;
import eu.faircode.xlua.x.runtime.RuntimeUtils;
import eu.faircode.xlua.x.ui.adapters.hooks.elements.XHook;
import eu.faircode.xlua.x.xlua.LibUtil;

public class HookResolver {
    private static final String TAG = LibUtil.generateTag(HookResolver.class);

    //Make super typr esolver and converter

    public static final String WILD_CHAR = "*";

    public static boolean isWildCardArgs(String... args) { return ArrayUtils.isValid(args) && args.length == 1 && WILD_CHAR.equals(args[0]); }

    public static boolean isWildCardName(String m) { return WILD_CHAR.equalsIgnoreCase(Str.trimOriginal(m)); }
    public static boolean isField(String methodName) { return !Str.isEmpty(methodName) && methodName.startsWith("#"); }
    public static boolean isWildCardArg(String arg) { return WILD_CHAR.equals(arg); }
    public static boolean isWildCardReturn(String returnType) {  return !Str.isEmpty(returnType) && (returnType.length() > 3 && returnType.startsWith("*") && returnType.endsWith("*")); }
    public static boolean isValidName(String name) { return !Str.isEmpty(name) && Character.isLowerCase(name.charAt(0)); }

    public static boolean isSpecial(String methodName) { return !Str.isEmpty(methodName) && methodName.contains(":"); }

    public static String getMethodNamePrefix(String methodName) {
        if(Str.isEmpty(methodName))
            return methodName;

        methodName = Str.trimOriginal(methodName);
        if(Str.isEmpty(methodName))
            return methodName;

        if(methodName.startsWith("!")) methodName = methodName.substring(1);
        if(methodName.startsWith("#")) methodName = methodName.substring(1);

        if(methodName.contains(":")) {
            String[] parts = methodName.split(":");
            if(ArrayUtils.isValid(parts)) {
                for(String p : parts) {
                    if(!Str.isEmpty(p))
                        return p;
                }
            }
        }

        return methodName;
    }

    public static String getMethodName(String methodName) {
        if(Str.isEmpty(methodName))
            return methodName;

        methodName = Str.trimOriginal(methodName);
        if(Str.isEmpty(methodName))
            return methodName;

        if(methodName.startsWith("!")) methodName = methodName.substring(1);
        if(methodName.startsWith("#")) methodName = methodName.substring(1);

        if(methodName.contains(":")) {
            String[] parts = methodName.split(":");
            if(ArrayUtils.isValid(parts)) {
                for (int i = parts.length - 1; i >= 0; i--) {
                    if(!Str.isEmpty(parts[i]))
                        return parts[i];
                }
            }
        }

        return methodName;
    }

    public static String getReturnTypeName(String returnType) {
        return Str.isEmpty(returnType) ?
                returnType : returnType.contains("*") ?
                Str.trimOriginal(returnType.replaceAll("\\*", Str.EMPTY)) : Str.trimOriginal(returnType); }

    //Make version for multiple targeted classes
    public static List<HookDefinition> resolveHook(Context context, ClassLoader loader, XHook hook) {
        try {
            String className = Str.trimOriginal(hook.getClassName());
            if(Str.isEmpty(className))
                return ListUtil.emptyList();

            List<String> targetClassNames = new ArrayList<>();
            if(className.contains("|")) {
                String[] parts = className.split("\\|");
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Hook (%s) has a Multiple Target Classes (%s) Split Count=%s Items=%s",
                            hook.getObjectId(),
                            className,
                            ArrayUtils.safeLength(parts),
                            Str.joinArray(parts)));

                if(ArrayUtils.isValid(parts)) {
                    for(String p : parts) {
                        String trimmed = Str.trimOriginal(p);
                        if(Str.isEmpty(trimmed))
                            continue;

                        String resolved = XHookUtil.resolveClassName(context, trimmed);
                        if(!Str.isEmpty(resolved) && !targetClassNames.contains(resolved))
                            targetClassNames.add(resolved);
                    }
                }
            }

            if(targetClassNames.isEmpty())
                targetClassNames.add(XHookUtil.resolveClassName(context, className));

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Resolving (%s) Classes (%s) from Hook ID (%s) Method (%s) Class (%s)",
                        ListUtil.size(targetClassNames),
                        Str.joinList(targetClassNames),
                        hook.getObjectId(),
                        hook.methodName,
                        hook.getClassName()));

            String[] classNames = ArrayUtils.toArrayNoDuplicates(targetClassNames, String.class);
            if(!ArrayUtils.isValid(classNames))
                throw new Exception(Str.fm("Failed to (toArray) Target Classes! Target Classes Count=%s Data=(%s) HookId=(%s) Hook Class=(%s)",
                        ListUtil.size(targetClassNames),
                        Str.joinList(targetClassNames),
                        hook.getObjectId(),
                        hook.getClassName()));

            return resolveClasses(loader, hook, classNames);
        }catch (Exception e) {
            Log.e(TAG, "Failed to Resolve Hook: " + Str.ensureNoDoubleNewLines(Str.toStringOrNull(hook)) + " Error=" + e);
            return ListUtil.emptyList();
        }
    }

    public static List<HookDefinition> resolveClasses(
            ClassLoader loader,
            XHook hook,
            String... clazzNames) {

        List<HookDefinition> definitions = new ArrayList<>();
        if(hook == null) {
            Log.e(TAG, "Error Failed to Resolve Any Classes, Hook Definition given is Some how NULL ? Class Name Count=" + ArrayUtils.safeLength(clazzNames) + " Stack=" + RuntimeUtils.getStackTraceSafeString(new Exception()));
            return definitions;
        }

        if(loader == null) {
            Log.e(TAG, "Error Failed to Resolve Any Classes, Class Loader given is Some how NULL ? Class Name Count=" + ArrayUtils.safeLength(clazzNames) + " HookId=" + hook.getObjectId() + " Stack=" + RuntimeUtils.getStackTraceSafeString(new Exception()));
            return definitions;
        }

        if(!ArrayUtils.isValid(clazzNames)) {
            Log.w(TAG, "Empty Class Count to Resolve for Hook:" + hook.getObjectId());
            return definitions;
        }

        try {
            boolean isSpecial = isSpecial(hook.methodName);
            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Resolving (%s) Classes (%s) for Hook ID (%s) with Class (%s) with Method Name (%s) with Return (%s) Is Special ? %s",
                        ArrayUtils.safeLength(clazzNames),
                        Str.joinArray(clazzNames),
                        hook.getObjectId(),
                        hook.getClassName(),
                        hook.methodName,
                        hook.returnType,
                        isSpecial));

            for(String clazzName : clazzNames) {
                String trimmed = Str.trimOriginal(clazzName);
                if(!Str.isEmpty(trimmed)) {
                    try {
                        Class<?> clazz = Class.forName(trimmed, false, loader);
                        if(isSpecial) {
                            Field field = clazz.getField(getMethodNamePrefix(hook.methodName));
                            field.setAccessible(true);
                            Object instance = field.get(null);
                            if(instance == null) {
                                Log.e(TAG, Str.fm("Failed to Resolve Class (%s) For Special Method (%s) HookId(%s) Hook Class (%s)",
                                        clazzName,
                                        hook.methodName,
                                        hook.getObjectId(),
                                        hook.getClassName()));

                                continue;
                            }

                            clazz = instance.getClass();
                            if(DebugUtil.isDebug())
                                Log.d(TAG, Str.fm("Resolved Class (%s) to (%s) For Special Method (%s) HookId (%s) Hook Class (%s)",
                                        clazzName,
                                        clazz.getName(),
                                        hook.methodName,
                                        hook.getObjectId(),
                                        hook.getClassName()));
                        }

                        //Make sure we are using the resolver for Class also on types of all kinds ?
                        List<HookDefinition> resolved = resolveDefinitionsForClazz(clazz, loader, hook);
                        if(!ListUtil.isValid(resolved)) {
                            if(DebugUtil.isDebug())
                                Log.d(TAG, Str.fm("Got 0 Definitions from Class (%s) from Hook (%s) with Class (%s) with Method (%s) with Return (%s) with Args (%s) Given Class Name Count=(%s)",
                                        clazzName,
                                        hook.getObjectId(),
                                        hook.getClassName(),
                                        hook.methodName,
                                        hook.returnType,
                                        Str.joinList(hook.parameterTypes),
                                        ArrayUtils.safeLength(clazzNames)));
                        } else {
                            ListUtil.addAll(definitions, resolved);
                            if(DebugUtil.isDebug())
                                Log.d(TAG, Str.fm("Parsed [%s] Definitions from Clazz: [%s] now Totaling [%s] form Hook (%s)",
                                        resolved.size(),
                                        trimmed,
                                        definitions.size(),
                                        hook.getObjectId()));
                        }
                    }catch (Exception e) {
                        Log.e(TAG, "Error Parsing Clazz: " + clazzName + " For Hook: " + Str.ensureNoDoubleNewLines(Str.toStringOrNull(hook)));
                    }
                }
            }
        }catch (Exception e) {
            Log.e(TAG, Str.fm("Error Resolving Classes For Hook! Hook [%s] Classes [%s] Hook Class [%s] Method [%s] Return [%s] Args [%s] Error=%s",
                    hook.getObjectId(),
                    Str.joinArray(clazzNames),
                    hook.getClassName(),
                    hook.methodName,
                    hook.returnType,
                    Str.joinList(hook.parameterTypes),
                    e));
        }

        if(DebugUtil.isDebug())
            Log.d(TAG, Str.fm("Returning [%s] Hook Definitions", definitions.size()));

        return definitions;
    }

    public static List<HookDefinition> resolveDefinitionsForClazz(Class<?> clazzResolved, ClassLoader loader, XHook hook) {
        List<HookDefinition> definitions = new ArrayList<>();
        try {
            //Also if params is wild card we will use the hookAll ? method ?
            //String clazzName = hook.getClassName();
            boolean wildCardWithReturn = isWildCardReturn(hook.returnType);
            String returnTypeName = getReturnTypeName(hook.returnType);

            boolean isWildCardWithArgs = isWildCardArgs(ArrayUtils.toArray(hook.parameterTypes, String.class));

            boolean isField = isField(hook.methodName);
            boolean isWildCardName = isWildCardName(hook.methodName);
            boolean isConstructor = Str.isEmpty(hook.methodName);

            String methodName = getMethodName(hook.methodName);
            Class<?> resolvedReturn = HookResolverUtils.resolveClass(returnTypeName, loader);
            Class<?>[] arguments = HookResolverUtils.getParameterTypesArray(ArrayUtils.toArray(hook.parameterTypes, String.class), loader);

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Getting Definition for Hook [%s][%s] Hook Class [%s][%s] Hook Method [%s] Target Class [%s] Return [%s] Resolved Return [%s] Target Method [%s] Is Wild Name [%s] Is Wild Args [%s] Is Wild Return [%s] Is Constructor [%s] Is Field [%s] Resolved Args Count [%s]",
                        hook.getObjectId(),
                        hook.name,
                        hook.getResolvedClassName(),
                        hook.getClassName(),
                        hook.methodName,
                        clazzResolved.getName(),
                        returnTypeName,
                        Str.toStringOrNull(resolvedReturn),
                        methodName,
                        isWildCardName,
                        isWildCardWithArgs,
                        wildCardWithReturn,
                        isConstructor,
                        isField,
                        ArrayUtils.safeLength(arguments)));

            List<String> targetMethodNames = new ArrayList<>();
            if(!Str.isEmpty(methodName)) {
                if(methodName.contains("|")) {
                    String[] parts = methodName.split("\\|");
                    if(ArrayUtils.isValid(parts)) {
                        for(String p : parts) {
                            String trimmed = Str.trimOriginal(p);
                            if(!Str.isEmpty(trimmed) && !targetMethodNames.contains(trimmed))
                                targetMethodNames.add(trimmed);
                        }
                    }
                }

                if(targetMethodNames.isEmpty())
                    targetMethodNames.add(methodName);
            }

            if(isWildCardWithArgs && !isWildCardName && !isConstructor) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Hook Appears to be a Hook All Definition for Any Method [%s] Class [%s] Hook:%s",
                            methodName,
                            clazzResolved.getName(),
                            hook.getObjectId()));

                List<HookDefinition> allDefList = new ArrayList<>();
                for(String m : targetMethodNames) allDefList.add(new HookDefinitionAll(clazzResolved, hook, m));
                return allDefList;
            }

            if(isConstructor) {
                List<HookDefinition> constructorDefList = new ArrayList<>();
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Resolving Constructor Definition from Class [%s] Is Wildcard Args:%s Hook:%s",
                            clazzResolved.getName(),
                            isWildCardWithArgs,
                            hook.getObjectId()));

                if(!isWildCardWithArgs) {
                    Member resolved = resolveMember(clazzResolved, null, arguments);
                    if(resolved != null)
                        constructorDefList.add(HookDefinitionMember.fromConstructor(resolved, hook,
                                HookResolverUtils.resolveClass(resolved.getDeclaringClass().getName(), loader)));
                } else {
                    List<Member> alreadyAdded = new ArrayList<>();
                    for(Class<?> clazz : getAllClasses(clazzResolved, false)) {
                        for(Member c : clazz.getDeclaredConstructors()) {
                            if(!alreadyAdded.contains(c)) {
                                alreadyAdded.add(c);
                                constructorDefList.add(HookDefinitionMember.fromConstructor(c, hook,
                                        HookResolverUtils.resolveClass(c.getDeclaringClass().getName(), loader)));
                            }
                        }
                    }
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Returning [%s] Constructor Members for Hook [%s] Is Wild Args:%s",
                            constructorDefList.size(),
                            hook.getObjectId(),
                            isWildCardWithArgs));

                return constructorDefList;
            }

            if(isField) {
                List<HookDefinition> fieldsDefList = new ArrayList<>();
                Field resolved = resolveField(clazzResolved, methodName, resolvedReturn);
                if(resolved != null)
                    fieldsDefList.add(new HookDefinitionField(resolved,
                            HookResolverUtils.resolveClass(resolved.getDeclaringClass().getName(), loader), hook));

                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Returning [%s] Field Members for Hook [%s] Class [%s] Field Name:%s",
                            fieldsDefList.size(),
                            hook.getObjectId(),
                            clazzResolved.getName(),
                            hook.name));

                return fieldsDefList;
            }

            if(!isWildCardWithArgs && !isWildCardName && !wildCardWithReturn) {
                //No Wild Cards just direct resolving
                List<HookDefinition> methodsDefList = new ArrayList<>();
                for(String m : targetMethodNames) {
                    Member resolved = resolveMember(clazzResolved, m, arguments);
                    if(resolved != null) {
                        HookDefinition resolvedMethod = HookDefinitionMember.fromMethod(resolved, hook, HookResolverUtils.resolveClass(resolved.getDeclaringClass().getName(), loader));
                        if(!methodsDefList.contains(resolvedMethod))
                            methodsDefList.add(resolvedMethod);
                    }
                }

                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Returning [%s] Direct Target Method Members for Hook [%s] Class [%s] Method Name:%s",
                            methodsDefList.size(),
                            hook.getObjectId(),
                            clazzResolved.getName(),
                            hook.methodName));

                return methodsDefList;
            }

            //Improve to also bundle in constructors as they return that target type!!!
            //Also add more code to SPECIFICALLY Search and find the PARCEL /CREATOR methods etc
            //Method searching now, this is in support for wild cards etc
            //Target hidden API for the class loader ?
            //Future look into old method of finding classes what not that inherit interfaces so make a system that hooks interfaces / type shit
            //Make a Hook all functions in class hook type ? and / for log ? so hook all log all ? dynamic system that dumps all info every arg etc
            boolean subClasses = hook.getClassName().endsWith("**");
            Map<Class<?>, List<Method>> targets = new HashMap<>();
            ListUtil.forEachVoid(getAllClasses(clazzResolved, subClasses), (e, i)
                    -> targets.put(e, ListUtil.arrayToList(e.getDeclaredMethods())));

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Resolving [%s] Total Class Method Blocks, Class [%s] Hook=%s",
                        targets.size(),
                        clazzResolved.getName(),
                        hook.getObjectId()));

            for(Map.Entry<Class<?>, List<Method>> entry : targets.entrySet()) {
                if(DebugUtil.isDebug())
                    Log.d(TAG, Str.fm("Parsing Class [%s] With total Methods [%s] For Hook [%s]",
                            entry.getKey().getName(),
                            entry.getValue().size(),
                            hook.getObjectId()));

                for(Method m : entry.getValue()) {
                    if(meetsRequirements(m, targetMethodNames, isWildCardName, resolvedReturn, arguments, isWildCardWithArgs))
                        definitions.add(HookDefinitionMember.fromMethod(m, hook,
                                HookResolverUtils.resolveClass(m.getDeclaringClass().getName(), loader)));
                }
            }

            if(DebugUtil.isDebug())
                Log.d(TAG, Str.fm("Finished for Hook [%s] Total Targets: [%s] IsWildName:%s IsWildArgs:%s IsWildReturn:%s Resolved Class=%s Hook=%s",
                        hook.getObjectId(),
                        Str.combineEx(targets.size(), " / ", definitions.size()),
                        isWildCardName,
                        isWildCardWithArgs,
                        wildCardWithReturn,
                        clazzResolved.getName(),
                        Str.ensureNoDoubleNewLines(Str.toStringOrNull(hook))));

            return definitions;
        }catch (Exception e) {
            Log.e(TAG, "Failed to Resolve Hook from XLua Hook! Hook=" + Str.ensureNoDoubleNewLines(Str.toStringOrNull(hook)) + " Error=" + e);
            return definitions;
        }
    }

    public static boolean meetsRequirements(
            Method method,
            List<String> methodNames,
            boolean isWildName,
            Class<?> resolvedReturn,
            Class<?>[] arguments,
            boolean isWildArgs) {

        if(method == null)
            return false;

        if(!isWildName) {
            boolean found = false;
            for(String m : methodNames) {
                if(method.getName().equalsIgnoreCase(m)) {
                    found = true;
                    break;
                }
            }

            if(!found)
                return false;
        }

        if(resolvedReturn != null && !HookResolverUtils.isWildCardType(resolvedReturn)) {
            //If its NULL then assume any ?
            if(!HookResolverUtils.isSameType(method.getReturnType(), resolvedReturn))
                return false;
        }

        if(!isWildArgs) {
            //Ensure we resolve the actual required types if needed
            //Check if it has a wild char param but requires one param
            if(!HookResolverUtils.isSameTypes(method.getParameterTypes(), arguments))
                return false;
        }

        return true;
    }

    public static List<Class<?>> getAllClasses(Class<?> baseClazz, boolean includeSubClasses) {
        List<Class<?>> classes = new ArrayList<>();
        if(baseClazz == null)
            return classes;

        try {
            Class<?> clazz = baseClazz;
            while (clazz != null && !clazz.equals(Object.class)) {
                if(!classes.contains(clazz))
                    classes.add(clazz);

                if(includeSubClasses) {
                    try {
                        for(Class<?> c : clazz.getDeclaredClasses()) {
                            if(!classes.contains(c))
                                classes.add(c);

                            for(Class<?> subClazz : getAllClasses(c, true)) {
                                if(!classes.contains(subClazz))
                                    classes.add(subClazz);
                            }
                        }
                    }catch (Exception ignored) { }
                }

                clazz = clazz.getSuperclass();
                if(clazz == null)
                    break;
            }
        }catch (Exception e) {
            Log.e(TAG, "Error getting ALL Classes, Include Sub=" + includeSubClasses + " Class=" + baseClazz.getName() + " Error=" + e);
        }

        return classes;
    }

    public static Member resolveMember(Class<?> clazz, String name, Class<?>[] params) {
        try {
            //Make sure it is not a interface ? or make sure it has code is implemented ?
            //Make so it can find all methods etc ?
            boolean isConstructor = Str.isEmpty(name);
            int paramSz = ArrayUtils.safeLength(params);
            Class<?> c = clazz;
            while (c != null && !c.equals(Object.class)) {
                try {
                    if (isConstructor) return c.getDeclaredConstructor(params);
                    else return c.getDeclaredMethod(name, params);
                }catch (Exception ignored) {
                    for(Member member : isConstructor ? c.getDeclaredConstructors() : c.getDeclaredMethods()) {
                        if(!isConstructor && !name.equalsIgnoreCase(member.getName()))
                            continue;

                        Class<?>[] memberParams = isConstructor ?
                                ((Constructor)member).getParameterTypes() : ((Method)member).getParameterTypes();

                        if(paramSz != ArrayUtils.safeLength(memberParams))
                            continue;

                        if(paramSz == 0)
                            return member;

                        boolean isSame = true;
                        for(int i = 0; i < paramSz; i++) {
                            if(!memberParams[i].isAssignableFrom(params[i])) {
                                isSame = false;
                                break;
                            }
                        }

                        if(isSame) return member;
                    }

                    c = c.getSuperclass();
                    if(c == null)
                        break;
                }
            }

            return null;
        }catch (Exception e) {
            Log.e(TAG, "Error Resolving Member, Clazz=" + clazz.getName() + " Name=" + name + " Error=" + e);
            return null;
        }
    }

    public static Field resolveField(Class<?> clazz, String name, Class<?> returnType) {
        try {
            name = Str.trimOriginal(name);
            if(Str.isEmpty(name)) return null;
            if(name.startsWith("#")) name = name.substring(1);

            Class<?> c = clazz;
            while (c != null && !c.equals(Object.class)) {
                try {
                    Field field = c.getDeclaredField(name);
                    if (!field.getType().equals(returnType)) throw new NoSuchFieldException();
                    return field;
                }catch (Exception e) {
                    for(Field f : c.getDeclaredFields()) {
                        if(!name.equalsIgnoreCase(f.getName()))
                            continue;

                        if(returnType == null)
                            return f;

                        //or is assignable from ?
                        if(HookResolverUtils.isSameType(f.getType(), returnType))
                            return f;
                    }
                }

                c = c.getSuperclass();
                if(c == null)
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to Resolve Field:" + name + " For Class:" + clazz.getName() + " Error=" + e);
            //if Debug print all fields ?
        }

        return null;
    }
}
