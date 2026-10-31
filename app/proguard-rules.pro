# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   https://developer.android.com/studio/build/shrink-code.html

# Preserve ViewModel and Navigation classes
-keep class androidx.lifecycle.ViewModel { *; }
-keep class androidx.navigation.** { *; }

# Preserve Android component entry points
-keep class com.kevinsrm.ide.MainActivity { *; }
-keep class com.kevinsrm.ide.termux.TermuxService { *; }

# Keep native method names
-keepclasseswithmembers class * {
    native <methods>;
}

# Preserve enumeration values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}