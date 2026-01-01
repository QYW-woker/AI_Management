package com.lifemanager.app.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.lifemanager.app.R
import com.lifemanager.app.core.database.AppDatabase
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

/**
 * 目标小组件列表适配器服务
 */
class GoalWidgetRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return GoalRemoteViewsFactory(applicationContext)
    }
}

/**
 * 目标列表RemoteViews工厂
 */
class GoalRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var goals: List<GoalWidgetItem> = emptyList()

    override fun onCreate() {
        // 初始化
    }

    override fun onDataSetChanged() {
        // 加载数据
        runBlocking {
            try {
                val db = AppDatabase.getInstance(context)
                val dataProvider = WidgetDataProvider(context, db)
                val goalData = dataProvider.getGoalWidgetData()
                goals = goalData.displayGoals
            } catch (e: Exception) {
                e.printStackTrace()
                goals = emptyList()
            }
        }
    }

    override fun onDestroy() {
        goals = emptyList()
    }

    override fun getCount(): Int = goals.size

    override fun getViewAt(position: Int): RemoteViews {
        val goal = goals.getOrNull(position) ?: return RemoteViews(context.packageName, R.layout.widget_goal_item)

        val views = RemoteViews(context.packageName, R.layout.widget_goal_item)

        // 设置图标
        views.setTextViewText(R.id.widget_goal_item_icon, goal.icon)

        // 设置标题
        views.setTextViewText(R.id.widget_goal_item_title, goal.title)

        // 设置截止日期
        val deadlineText = goal.endDate?.let { epochDay ->
            val today = LocalDate.now().toEpochDay().toInt()
            val remaining = epochDay - today
            when {
                remaining < 0 -> "已逾期${-remaining}天"
                remaining == 0 -> "今天截止"
                remaining == 1 -> "明天截止"
                remaining <= 7 -> "${remaining}天后截止"
                else -> {
                    val date = LocalDate.ofEpochDay(epochDay.toLong())
                    "截止: ${date.monthValue}月${date.dayOfMonth}日"
                }
            }
        } ?: "无截止日期"
        views.setTextViewText(R.id.widget_goal_item_deadline, deadlineText)

        // 设置进度
        views.setProgressBar(R.id.widget_goal_item_progress_bar, 100, goal.progress, false)
        views.setTextViewText(R.id.widget_goal_item_progress, "${goal.progress}%")

        // 设置进度颜色（根据进度调整文字颜色）
        val progressColor = when {
            goal.progress >= 80 -> 0xFF4CAF50.toInt() // 绿色
            goal.progress >= 50 -> 0xFF2196F3.toInt() // 蓝色
            goal.progress >= 25 -> 0xFFFF9800.toInt() // 橙色
            else -> 0xFFF44336.toInt() // 红色
        }
        views.setTextColor(R.id.widget_goal_item_progress, progressColor)

        // 设置点击填充Intent
        val fillInIntent = Intent().apply {
            putExtra(GoalWidgetProvider.EXTRA_GOAL_ID, goal.id)
        }
        views.setOnClickFillInIntent(R.id.widget_goal_item_container, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = goals.getOrNull(position)?.id ?: position.toLong()

    override fun hasStableIds(): Boolean = true
}
