# XPL-EX NEXT 1.5.8: libxposed API 101 migration

This branch is a modern libxposed module for LSPosed 2.1.1 (7790). It targets
libxposed API 101 and Android 8.0+, with device validation intended for Android
14+ on arm64-v8a.

## Architecture mapping

| Legacy implementation | API 101 implementation |
| --- | --- |
| `assets/xposed_init` | `META-INF/xposed/java_init.list` |
| Manifest Xposed metadata | `META-INF/xposed/module.prop` and `scope.list` |
| `IXposedHookLoadPackage` | `XposedModule.onPackageReady` |
| Zygote callback | `XposedModule.onModuleLoaded`; no Zygote injection |
| Package `android` callback | `XposedModule.onSystemServerStarting` and scope `system` |
| `XposedBridge.hookMethod` | `XposedInterface.hook(...).intercept(...)` |
| `XC_MethodHook.MethodHookParam` | Internal `ModernMethodHook.MethodHookParam` adapter |
| `XposedHelpers` static-field fallback | Reflection followed by the existing Unsafe fallback |

`staticScope=false` is intentional. The `system` entry in `scope.list` replaces
both the old Android framework and Settings Provider scopes: under API 101,
`com.android.providers.settings` is loaded into `system_server` and is not a
valid independent scope target. Users can continue to select arbitrary
applications in LSPosed, which is required by XPL-EX.

## Hook inventory

| Hook group | Class / member | Process | Callback behavior | Device test |
| --- | --- | --- | --- | --- |
| System bootstrap | `ActivityManagerService.systemReady` overloads | `system_server` | Before: open the bridge/database and install package services. After: store the module version and register package receivers. | Reboot; confirm all three API101 log stages and no System Server crash. |
| Settings bridge | `SettingsProvider.call` and `query` | Settings Provider package loaded in `system_server` | Before: route XPL-EX command calls and queries; may return a bridge response early. | Open XPL-EX and read/save assignments; confirm the Settings Provider package log. |
| Application bootstrap | `Instrumentation.newApplication` on Android 13+, `LoadedApk.makeApplication` below Android 13 | Every selected application | Before/after: obtain the application context once and load settings, spoofers, filters, and assigned hooks. | Force-stop/start a selected app and confirm its package log plus spoofed value. |
| Dynamic exact hooks | Class, method, or constructor resolved by `HookCore` | Selected application | Existing Lua before/after callbacks can read/change arguments, result, or throwable through the API101 adapter. | Exercise one assigned exact hook and verify `First hook executed`. |
| Dynamic all-overload hooks | All declared overloads resolved by `HookCore` / `LuaHook` | Selected application | Same Lua before/after behavior for each overload. | Exercise an assigned all-overloads hook. |
| Android version properties | `SystemProperties.get`, `getInt`, and `getLong` overloads | Selected application | After: replace only profile-backed Android/build properties. | Check Android version/build fields in DevCheck Pro. |
| Android ID cache | Assigned `HashMap.put` / `ArrayMap.put` member | Selected application | Before: replace the cached `android_id` argument when configured. | Read Android ID twice and confirm the assigned stable value. |
| Uptime | Assigned `SystemClock` uptime member | Selected application | After: apply the existing bounded offset. | Invoke the assigned uptime hook and inspect output/logs. |
| Telephony/subscription | Subscription service/controller members when deployed by the existing runtime | Selected phone/provider process | After: preserve the existing subscription filtering/spoofing behavior. | Select the phone/provider process and inspect telephony data plus logs. |

The diagnostic `hookSubscriptionManagerService` block in `XLua` remains disabled,
as it was in 1.5.7; the migration does not activate experimental hooks.

## Runtime verification

After installing the APK, enable it in LSPosed, select **System Framework** and
the applications to be protected, then reboot. In the
LSPosed module log verify these distinct stages:

1. `Module loaded` proves that LSPosed instantiated the API 101 entry point.
2. `Hook installation completed` proves that XPL-EX registered hooks in a scoped
   process.
3. `First hook executed` proves that an intercepted framework/application method
   actually ran. The first two messages alone do not prove hook execution.

Open XPL-EX, assign a harmless hook to a test application, force-stop that
application, start it again, and verify both the expected spoofed value and the
third log message. Also verify database access through Settings Storage and boot
without system-server crashes.

## Known limits

- There is no C/C++ or JNI entry point in this project, so no
  `native_init.list` is needed.
- A desktop Gradle build can verify compilation, resources, metadata, and unit
  tests, but cannot execute LSPosed hooks inside Android processes. Final
  module/system-server behavior must be confirmed on the target device.
- API 101 requires a compatible modern LSPosed build; this APK intentionally
  contains no legacy Xposed entry point.
