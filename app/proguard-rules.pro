# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

#XPrivacyLua
-keep class eu.faircode.xlua.XLua {*; }
-keep class eu.faircode.xlua.XHook {*; }
-keep class eu.faircode.xlua.XParam {*; }
# XPL-EX exposes classes and members through Xposed, Lua, JSON and reflection.
# Preserve the application side exactly; R8 still removes unused dependency code,
# which provides most of the safe size/method-count reduction.
-keep class eu.faircode.xlua.** { *; }

# libxposed API 101 entry point and metadata.
-dontwarn io.github.libxposed.annotation.**
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keep public class eu.faircode.xlua.xposed.api101.ModernEntryPoint {
    public <init>();
}

#LuaJ
-dontwarn org.luaj.vm2.**
-keepnames class org.luaj.vm2.** {*; }

#Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
-keep enum com.bumptech.glide.** {*; }

#Support library
-keep class android.support.v7.widget.** { *; }
