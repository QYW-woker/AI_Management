package com.lifemanager.app.core.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.lifemanager.app.MainActivity
import com.lifemanager.app.R

/**
 * 提醒广播接收器
 *
 * 用于接收定时提醒的广播，显示通知
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ReminderReceiver"

        const val ACTION_REMINDER = "com.lifemanager.app.REMINDER_ACTION"
        const val EXTRA_TODO_ID = "extra_todo_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CONTENT = "extra_content"
        const val EXTRA_TYPE = "extra_type"

        const val TYPE_TODO = "todo"
        const val TYPE_HABIT = "habit"
        const val TYPE_BUDGET = "budget"

        private const val CHANNEL_ID = "reminder_channel"
        private const val CHANNEL_NAME = "任务提醒"
        private const val NOTIFICATION_ID_BASE = 20000
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMINDER) {
            return
        }

        Log.d(TAG, "Received reminder broadcast")

        val todoId = intent.getLongExtra(EXTRA_TODO_ID, -1)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "待办提醒"
        val content = intent.getStringExtra(EXTRA_CONTENT) ?: ""
        val type = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_TODO

        // 创建通知渠道
        createNotificationChannel(context)

        // 显示通知
        showNotification(context, todoId, title, content, type)
    }

    /**
     * 创建通知渠道（Android 8.0+）
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "待办事项和习惯打卡提醒"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 显示通知
     */
    private fun showNotification(
        context: Context,
        todoId: Long,
        title: String,
        content: String,
        type: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 创建点击通知时打开应用的Intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "todo")
            putExtra("todo_id", todoId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            getNotificationId(todoId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 根据类型选择图标
        val icon = when (type) {
            TYPE_TODO -> R.drawable.ic_todo
            TYPE_HABIT -> R.drawable.ic_habit
            TYPE_BUDGET -> R.drawable.ic_budget
            else -> R.drawable.ic_notification
        }

        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(content.ifEmpty { "点击查看详情" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .apply {
                if (content.isNotEmpty()) {
                    setStyle(NotificationCompat.BigTextStyle().bigText(content))
                }
            }
            .build()

        try {
            notificationManager.notify(getNotificationId(todoId), notification)
            Log.d(TAG, "Notification shown for todo: $todoId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification", e)
        }
    }

    /**
     * 生成唯一的通知ID
     */
    private fun getNotificationId(todoId: Long): Int {
        return (NOTIFICATION_ID_BASE + todoId).toInt()
    }
}
