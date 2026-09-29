# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep data models
-keep class com.example.model.** { *; }

# Keep Room database entities & daos
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Keep ViewModel classes
-keep class com.example.ui.viewmodel.** { *; }

# Keep Coil image loader
-keep class coil.** { *; }

# Keep Google Play Services and AdMob
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Keep Amazon Appstore components
-keep class com.amazon.** { *; }
-dontwarn com.amazon.**
