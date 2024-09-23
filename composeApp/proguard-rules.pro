# =========================
# General ProGuard Settings
# =========================

# Preserve annotations and inner classes
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Preserve source file and line number information for debugging
-keepattributes SourceFile,LineNumberTable

# =========================
# Kotlin Coroutines
# =========================

# Keep coroutine internal classes
-keep class kotlinx.coroutines.internal.** { *; }
-keep class kotlinx.coroutines.android.** { *; }

# =========================
# Kotlinx Serialization
# =========================

# Keep serialization classes and members
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }

# =========================
# Ktor Client
# =========================

# Keep Ktor client classes
-keep class io.ktor.client.** { *; }
-dontwarn io.ktor.client.**

# Keep Ktor logging classes
-keepattributes SourceFile,LineNumberTable

# =========================
# Koin Dependency Injection
# =========================

# Keep Koin annotations and related classes
-keepclassmembers class * {
    @org.koin.core.annotation.KoinReflectAPI *;
    @org.koin.core.annotation.KoinExperimentalAPI *;
    @org.koin.core.annotation.KoinApiExtension *;
}

# =========================
# Firebase SDKs
# =========================

# Firebase Analytics
-keep class com.google.firebase.analytics.** { *; }
-keep class com.google.android.gms.measurement.** { *; }

# Firebase Crashlytics
-keep class com.google.firebase.crashlytics.** { *; }

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }

# Firebase Functions
-keep class com.google.firebase.functions.** { *; }

# =========================
# Coil (Image Loading Library)
# =========================

# Keep Coil classes
-keep class coil.** { *; }
# Keep OkHttp classes used by Coil
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# =========================
# Kermit (Logging Library)
# =========================

# Keep Kermit logging classes
-keep class co.touchlab.kermit.** { *; }

# =========================
# Logback (Logging Framework)
# =========================

# Keep Logback classes
-keep class ch.qos.logback.** { *; }
-dontwarn ch.qos.logback.**

# =========================
# Image Loader Library
# =========================

# Keep Image Loader classes
-keep class io.github.qdsfdhvh.** { *; }

# =========================
# Cupertino Library
# =========================

# Keep Cupertino library classes
-keep class io.github.alexzhirkevich.cupertino.** { *; }

# =========================
# DeepLink Library
# =========================

# Keep DeepLink classes
-keep class com.motorro.keeplink.** { *; }

# =========================
# MaterialKolor Library
# =========================

# Keep MaterialKolor classes
-keep class com.materialkolor.** { *; }

# =========================
# Kotlinx IO and Datetime
# =========================

# Keep kotlinx IO classes
-keep class kotlinx.io.** { *; }
# Keep kotlinx Datetime classes
-keep class kotlinx.datetime.** { *; }

# =========================
# AndroidX and General Android Libraries
# =========================

# Generally, AndroidX libraries are already handled by the default ProGuard files.
# No additional rules are required unless specified by the library documentation.

# =========================
# Miscellaneous Settings
# =========================

# Keep all native method names and parameters
-keepclasseswithmembernames class * {
    native <methods>;
}

# Don't warn about missing classes (adjust as necessary)
-dontwarn org.codehaus.mojo.**

# =========================
# End of ProGuard Rules
# =========================
