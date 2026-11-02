# Project specific R8 / ProGuard rules for APK size optimization

# Keep data models
-keep class com.example.model.** { *; }

# Keep Room database entities & daos
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Keep ViewModel classes
-keep class com.example.ui.viewmodel.** { *; }

# Keep Coil image loader
-keep class coil.** { *; }
-dontwarn coil.**

# Keep Amazon Appstore components
-keep class com.amazon.** { *; }
-dontwarn com.amazon.**

# Keep Start.io (StartApp) SDK components
-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile, LineNumberTable, *Annotation*, EnclosingMethod
-dontwarn com.startapp.**
-keep class com.startapp.** { *; }
-dontwarn android.webkit.JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# General optimization settings
-dontwarn kotlinx.coroutines.**
-dontwarn kotlin.coroutines.**
-dontwarn java.lang.invoke.**
-keep class kotlin.coroutines.jvm.internal.SpillingKt { *; }


