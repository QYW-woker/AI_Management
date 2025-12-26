package com.lifemanager.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * AI智能生活管理APP - Application入口类
 *
 * 使用Hilt进行依赖注入，作为整个应用的入口点
 * 负责初始化全局配置和依赖注入框架
 */
@HiltAndroidApp
class LifeManagerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化操作会在这里进行
        // 例如：日志框架、崩溃上报、数据库预热等
    }
}
