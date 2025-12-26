// 项目级 build.gradle.kts
// AI智能生活管理APP - 顶层构建配置

plugins {
    // Android应用插件
    id("com.android.application") version "8.2.0" apply false
    // Kotlin Android插件
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    // KSP注解处理器（用于Room等）
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
    // Hilt依赖注入
    id("com.google.dagger.hilt.android") version "2.48" apply false
    // Kotlin序列化
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20" apply false
}

// 清理任务
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
