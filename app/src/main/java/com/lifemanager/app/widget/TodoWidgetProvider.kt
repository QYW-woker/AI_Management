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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * 待办事项小组件Provider
 *
 * 在桌面展示今日待办，支持以下功能：
 * - 显示完成进度（已完成/总数）
 * - 圆形进度指示
 * - 显示待完成事项列表
 * - 快速完成待办（点击勾选）
 * - 快速添加新待办
 */
@AndroidEntryPoint
class TodoWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var database: AppDatabase

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        const val ACTION_REFRESH = "com.lifemanager.app.widget.TODO_REFRESH"
        const val ACTION_ADD_TODO = "com.lifemanager.app.widget.ADD_TODO"
        const val ACTION_TOGGLE_TODO = "com.lifemanager.app.widget.TOGGLE_TODO"
        const val ACTION_VIEW_TODO = "com.lifemanager.app.widget.VIEW_TODO"
        const val ACTION_VIEW_ALL = "com.lifemanager.app.widget.VIEW_ALL_TODO"
        const val EXTRA_TODO_ID = "todo_id"

        /**
         * 更新所有待办小组件
         */
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, TodoWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, TodoWidgetProvider::class.java)
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
                    ComponentName(context, TodoWidgetProvider::class.java)
                )
                onUpdate(context, appWidgetManager, widgetIds)
            }
            ACTION_ADD_TODO -> {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigate_to", "todo/add")
                }
                context.startActivity(launchIntent)
            }
            ACTION_TOGGLE_TODO -> {
                val todoId = intent.getLongExtra(EXTRA_TODO_ID, -1)
                if (todoId > 0) {
                    scope.launch {
                        try {
                            val db = AppDatabase.getInstance(context)
                            val todo = db.todoDao().getByIdSync(todoId)
                            if (todo != null) {
                                if (todo.status == "COMPLETED") {
                                    db.todoDao().markPending(todoId)
                                } else {
                                    db.todoDao().markCompleted(todoId)
                                }
                                // 刷新widget
                                updateAllWidgets(context)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            ACTION_VIEW_TODO -> {
                val todoId = intent.getLongExtra(EXTRA_TODO_ID, -1)
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigate_to", if (todoId > 0) "todo/detail/$todoId" else "todo")
                }
                context.startActivity(launchIntent)
            }
            ACTION_VIEW_ALL -> {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigate_to", "todo")
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
                val todoData = dataProvider.getTodayTodos()

                val views = RemoteViews(context.packageName, R.layout.widget_todo)

                // 设置日期
                val today = LocalDate.now()
                val dateFormatter = DateTimeFormatter.ofPattern("M月d日", Locale.CHINA)
                views.setTextViewText(R.id.widget_todo_date, today.format(dateFormatter))

                // 设置完成统计
                val progressPercent = if (todoData.totalCount > 0) {
                    (todoData.completedCount * 100 / todoData.totalCount)
                } else 100

                views.setTextViewText(R.id.widget_todo_completed, todoData.completedCount.toString())
                views.setTextViewText(R.id.widget_todo_total, todoData.totalCount.toString())
                views.setProgressBar(R.id.widget_todo_progress_ring, 100, progressPercent, false)
                views.setTextViewText(R.id.widget_todo_progress_text, "$progressPercent%")

                // 设置鼓励语
                val motivation = when {
                    todoData.totalCount == 0 -> "今天没有待办事项"
                    todoData.completedCount >= todoData.totalCount -> "太棒了，全部完成！🎉"
                    progressPercent >= 80 -> "马上就完成了，加油！💪"
                    progressPercent >= 50 -> "已完成过半，继续努力！"
                    else -> "还有${todoData.totalCount - todoData.completedCount}项待完成"
                }
                views.setTextViewText(R.id.widget_todo_motivation, motivation)

                // 设置列表数据
                if (todoData.pendingItems.isEmpty() && todoData.completedCount >= todoData.totalCount) {
                    views.setViewVisibility(R.id.widget_todo_list, View.GONE)
                    views.setViewVisibility(R.id.widget_todo_empty, View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_todo_list, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_todo_empty, View.GONE)

                    // 设置RemoteViewsService用于列表
                    val serviceIntent = Intent(context, TodoWidgetRemoteViewsService::class.java).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                    }
                    views.setRemoteAdapter(R.id.widget_todo_list, serviceIntent)

                    // 设置列表项点击模板
                    val itemClickIntent = Intent(context, TodoWidgetProvider::class.java).apply {
                        action = ACTION_TOGGLE_TODO
                    }
                    val itemClickPendingIntent = PendingIntent.getBroadcast(
                        context, 0, itemClickIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    views.setPendingIntentTemplate(R.id.widget_todo_list, itemClickPendingIntent)
                }

                // 设置刷新按钮点击
                val refreshIntent = Intent(context, TodoWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context, 1, refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_todo_refresh, refreshPendingIntent)

                // 设置添加按钮点击
                val addIntent = Intent(context, TodoWidgetProvider::class.java).apply {
                    action = ACTION_ADD_TODO
                }
                val addPendingIntent = PendingIntent.getBroadcast(
                    context, 2, addIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_todo_add_btn, addPendingIntent)

                // 设置查看全部点击
                val viewAllIntent = Intent(context, TodoWidgetProvider::class.java).apply {
                    action = ACTION_VIEW_ALL
                }
                val viewAllPendingIntent = PendingIntent.getBroadcast(
                    context, 3, viewAllIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_todo_view_all_btn, viewAllPendingIntent)

                // 设置标题区域点击打开APP
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigate_to", "todo")
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context, 4, openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_todo_title, openAppPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_todo_icon, openAppPendingIntent)

                // 通知列表更新
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_todo_list)

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
