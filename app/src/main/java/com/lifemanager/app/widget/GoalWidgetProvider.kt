package com.lifemanager.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.lifemanager.app.MainActivity
import com.lifemanager.app.R
import com.lifemanager.app.core.database.AppDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 目标追踪小组件Provider
 *
 * 在桌面展示用户的目标进度，支持以下功能：
 * - 显示活跃/已完成目标数量
 * - 显示总体进度百分比
 * - 显示最近活跃的目标列表
 * - 点击进入APP目标模块
 * - 快速添加新目标
 */
@AndroidEntryPoint
class GoalWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var database: AppDatabase

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        const val ACTION_REFRESH = "com.lifemanager.app.widget.GOAL_REFRESH"
        const val ACTION_ADD_GOAL = "com.lifemanager.app.widget.ADD_GOAL"
        const val ACTION_VIEW_GOAL = "com.lifemanager.app.widget.VIEW_GOAL"
        const val EXTRA_GOAL_ID = "goal_id"

        /**
         * 更新所有目标小组件
         */
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, GoalWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, GoalWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_REFRESH -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, GoalWidgetProvider::class.java)
                )
                onUpdate(context, appWidgetManager, widgetIds)
            }
            ACTION_ADD_GOAL -> {
                // 打开APP的添加目标页面
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigate_to", "goal/add")
                }
                context.startActivity(launchIntent)
            }
            ACTION_VIEW_GOAL -> {
                val goalId = intent.getLongExtra(EXTRA_GOAL_ID, -1)
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigate_to", if (goalId > 0) "goal/detail/$goalId" else "goal")
                }
                context.startActivity(launchIntent)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        scope.launch {
            try {
                val db = AppDatabase.getInstance(context)
                val dataProvider = WidgetDataProvider(context, db)
                val goalData = dataProvider.getGoalWidgetData()

                val views = RemoteViews(context.packageName, R.layout.widget_goal)

                // 设置统计数据
                views.setTextViewText(R.id.widget_goal_completed_count, goalData.completedCount.toString())
                views.setTextViewText(R.id.widget_goal_active_count, goalData.activeCount.toString())
                views.setTextViewText(R.id.widget_goal_progress_percent, "${goalData.totalProgress}%")

                // 设置列表数据
                if (goalData.displayGoals.isEmpty()) {
                    views.setViewVisibility(R.id.widget_goal_list, View.GONE)
                    views.setViewVisibility(R.id.widget_goal_empty, View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_goal_list, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_goal_empty, View.GONE)

                    // 设置RemoteViewsService用于列表
                    val serviceIntent = Intent(context, GoalWidgetRemoteViewsService::class.java).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                    }
                    views.setRemoteAdapter(R.id.widget_goal_list, serviceIntent)

                    // 设置列表项点击模板
                    val itemClickIntent = Intent(context, GoalWidgetProvider::class.java).apply {
                        action = ACTION_VIEW_GOAL
                    }
                    val itemClickPendingIntent = PendingIntent.getBroadcast(
                        context, 0, itemClickIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    views.setPendingIntentTemplate(R.id.widget_goal_list, itemClickPendingIntent)
                }

                // 设置刷新按钮点击
                val refreshIntent = Intent(context, GoalWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context, 1, refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_goal_refresh, refreshPendingIntent)

                // 设置添加按钮点击
                val addIntent = Intent(context, GoalWidgetProvider::class.java).apply {
                    action = ACTION_ADD_GOAL
                }
                val addPendingIntent = PendingIntent.getBroadcast(
                    context, 2, addIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_goal_add_btn, addPendingIntent)

                // 设置标题区域点击打开APP
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigate_to", "goal")
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context, 3, openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_goal_title, openAppPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_goal_icon, openAppPendingIntent)

                // 通知列表更新
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_goal_list)

                // 更新Widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        job.cancel()
    }
}
