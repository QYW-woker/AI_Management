# ProGuard规则文件
# AI智能生活管理APP

# 保留Kotlin序列化
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# 保留Room Entity
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# 保留Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# 保留Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }

# 保留Hilt ViewModel
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class dagger.hilt.android.internal.lifecycle.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# 保留Compose Navigation Hilt
-keep class androidx.hilt.navigation.** { *; }
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel *;
}

# 保留OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# 保留Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.stream.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# 保留数据类
-keep class com.lifemanager.app.core.database.entity.** { *; }
-keep class com.lifemanager.app.core.network.** { *; }
-keep class com.lifemanager.app.domain.model.** { *; }

# 保留UseCase
-keep class com.lifemanager.app.domain.usecase.** { *; }
-keepclassmembers class com.lifemanager.app.domain.usecase.** {
    <init>(...);
}

# 保留Repository实现
-keep class com.lifemanager.app.data.repository.** { *; }
-keepclassmembers class com.lifemanager.app.data.repository.** {
    <init>(...);
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Java 8 Time API
-keep class java.time.** { *; }
-keep class java.time.chrono.** { *; }

# Room DAO
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
