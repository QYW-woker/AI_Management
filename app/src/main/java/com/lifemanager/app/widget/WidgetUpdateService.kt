package com.lifemanager.app.widget

import android.content.Context
import android.content.Intent
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Widget更新服务
 *
 * 负责管理和调度Widget更新任务
 */
@Singleton
class WidgetUpdateService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val WIDGET_UPDATE_WORK_NAME = "widget_periodic_update"
        private const val UPDATE_INTERVAL_MINUTES = 15L
    }

    /**
     * 立即刷新所有Widget
     */
    fun refreshAllWidgets() {
        GoalWidgetProvider.updateAllWidgets(context)
        TodoWidgetProvider.updateAllWidgets(context)
    }

    /**
     * 刷新目标Widget
     */
    fun refreshGoalWidget() {
        GoalWidgetProvider.updateAllWidgets(context)
    }

    /**
     * 刷新待办Widget
     */
    fun refreshTodoWidget() {
        TodoWidgetProvider.updateAllWidgets(context)
    }

    /**
     * 启动周期性更新任务
     */
    fun startPeriodicUpdate() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            UPDATE_INTERVAL_MINUTES, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WIDGET_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            updateRequest
        )
    }

    /**
     * 停止周期性更新任务
     */
    fun stopPeriodicUpdate() {
        WorkManager.getInstance(context).cancelUniqueWork(WIDGET_UPDATE_WORK_NAME)
    }
}

/**
 * Widget更新Worker
 */
class WidgetUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        return try {
            GoalWidgetProvider.updateAllWidgets(applicationContext)
            TodoWidgetProvider.updateAllWidgets(applicationContext)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}

/**
 * Widget更新触发器
 *
 * 可在数据变化时调用来刷新Widget
 */
object WidgetUpdateTrigger {

    /**
     * 通知目标数据已变化
     */
    fun notifyGoalChanged(context: Context) {
        GoalWidgetProvider.updateAllWidgets(context)
    }

    /**
     * 通知待办数据已变化
     */
    fun notifyTodoChanged(context: Context) {
        TodoWidgetProvider.updateAllWidgets(context)
    }

    /**
     * 通知所有数据已变化
     */
    fun notifyDataChanged(context: Context) {
        GoalWidgetProvider.updateAllWidgets(context)
        TodoWidgetProvider.updateAllWidgets(context)
    }
}
