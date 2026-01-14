package com.lifemanager.app.widget

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.lifemanager.app.R
import com.lifemanager.app.core.database.AppDatabase
import kotlinx.coroutines.runBlocking

/**
 * 待办小组件列表适配器服务
 */
class TodoWidgetRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TodoRemoteViewsFactory(applicationContext)
    }
}

/**
 * 待办列表RemoteViews工厂
 */
class TodoRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var todos: List<TodoWidgetItem> = emptyList()

    override fun onCreate() {
        // 初始化
    }

    override fun onDataSetChanged() {
        // 加载数据
        runBlocking {
            try {
                val db = AppDatabase.getInstance(context)
                val dataProvider = WidgetDataProvider(context, db)
                val todoData = dataProvider.getTodayTodos()
                todos = todoData.pendingItems
            } catch (e: Exception) {
                e.printStackTrace()
                todos = emptyList()
            }
        }
    }

    override fun onDestroy() {
        todos = emptyList()
    }

    override fun getCount(): Int = todos.size

    override fun getViewAt(position: Int): RemoteViews {
        val todo = todos.getOrNull(position) ?: return RemoteViews(context.packageName, R.layout.widget_todo_item)

        val views = RemoteViews(context.packageName, R.layout.widget_todo_item)

        // 设置标题
        views.setTextViewText(R.id.widget_todo_item_title, todo.title)

        // 设置时间
        if (!todo.dueTime.isNullOrBlank()) {
            views.setTextViewText(R.id.widget_todo_item_time, todo.dueTime)
            views.setViewVisibility(R.id.widget_todo_item_time, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_todo_item_time, View.GONE)
        }

        // 设置优先级标签
        val (priorityText, priorityBgColor) = when (todo.priority) {
            "HIGH" -> "重要" to 0xFFF44336.toInt()
            "MEDIUM" -> "中等" to 0xFFFF9800.toInt()
            "LOW" -> "较低" to 0xFF4CAF50.toInt()
            else -> "" to 0
        }
        if (priorityText.isNotBlank()) {
            views.setTextViewText(R.id.widget_todo_item_priority, priorityText)
            views.setViewVisibility(R.id.widget_todo_item_priority, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_todo_item_priority, View.GONE)
        }

        // 设置分类图标（根据待办内容智能判断）
        val categoryIcon = getCategoryIcon(todo.title)
        views.setTextViewText(R.id.widget_todo_item_category_icon, categoryIcon)

        // 设置点击填充Intent（用于切换完成状态）
        val fillInIntent = Intent().apply {
            putExtra(TodoWidgetProvider.EXTRA_TODO_ID, todo.id)
        }
        views.setOnClickFillInIntent(R.id.widget_todo_item_container, fillInIntent)

        return views
    }

    /**
     * 根据待办标题智能判断分类图标
     */
    private fun getCategoryIcon(title: String): String {
        return when {
            title.contains("工作") || title.contains("会议") || title.contains("汇报") -> "💼"
            title.contains("学习") || title.contains("看书") || title.contains("阅读") -> "📚"
            title.contains("运动") || title.contains("健身") || title.contains("跑步") -> "🏃"
            title.contains("购物") || title.contains("买") -> "🛒"
            title.contains("吃饭") || title.contains("餐") || title.contains("饭") -> "🍽️"
            title.contains("电话") || title.contains("联系") || title.contains("call") -> "📞"
            title.contains("医院") || title.contains("看病") || title.contains("体检") -> "🏥"
            title.contains("出行") || title.contains("旅行") || title.contains("飞机") -> "✈️"
            title.contains("家") || title.contains("打扫") || title.contains("收拾") -> "🏠"
            title.contains("付款") || title.contains("缴费") || title.contains("还款") -> "💳"
            else -> "📋"
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = todos.getOrNull(position)?.id ?: position.toLong()

    override fun hasStableIds(): Boolean = true
}
