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

# ==================== Apache POI 优化 ====================
# 只保留必要的POI类，积极移除未使用的代码
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.apache.commons.**
-dontwarn org.openxmlformats.**
-dontwarn com.microsoft.**
-dontwarn org.w3c.**
-dontwarn javax.xml.**
-dontwarn org.etsi.**
-dontwarn schemaorg_apache_xmlbeans.**

# 保留POI核心类
-keep class org.apache.poi.ss.usermodel.** { *; }
-keep class org.apache.poi.xssf.usermodel.XSSFWorkbook { *; }
-keep class org.apache.poi.hssf.usermodel.HSSFWorkbook { *; }
-keep class org.apache.poi.xwpf.usermodel.XWPFDocument { *; }
-keep class org.apache.poi.xwpf.usermodel.XWPFTable { *; }
-keep class org.apache.poi.xwpf.usermodel.XWPFTableRow { *; }
-keep class org.apache.poi.xwpf.usermodel.XWPFTableCell { *; }
-keep class org.apache.poi.xwpf.usermodel.XWPFParagraph { *; }

# 移除POI的Log4j依赖（Android不需要）
-assumenosideeffects class org.apache.logging.log4j.** { *; }
-dontwarn org.apache.logging.log4j.**

# ==================== 性能优化 ====================
# 移除调试日志
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

# 积极内联
-allowaccessmodification
-repackageclasses
