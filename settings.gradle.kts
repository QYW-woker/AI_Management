// 项目设置配置文件
// AI智能生活管理APP - Settings Configuration

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// 项目根名称
rootProject.name = "AI_Management"

// 包含app模块
include(":app")
