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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Fix for missing classes in jsoup and rhino/mozilla javascript
-dontwarn com.google.re2j.**
-dontwarn java.beans.**

# Keep the specific entry point class exactly as it is
-keep class com.veena.saavn.Plugin { *; }
-keep class com.veena.ytmusic.Plugin { *; }
-keep class com.veena.newpipe.Plugin { *; }

# Also keep the contract (since the plugin uses it to implement)
-keep class com.indus.veena.contract.** { *; }

# Keep Kotlinx Serialization generated classes if used
-keep class **$$serializer { *; }
-keepclassmembers class * {
    *** Companion;
}
