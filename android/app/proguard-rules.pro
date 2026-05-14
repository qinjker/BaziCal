# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Moshi adapters
-keep class com.bazical.app.data.remote.dto.** { *; }
-keepclassmembers class com.bazical.app.data.remote.dto.** { *; }

# Keep Retrofit interfaces
-keep,allowobfuscation interface com.bazical.app.data.remote.api.BaziApi

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}