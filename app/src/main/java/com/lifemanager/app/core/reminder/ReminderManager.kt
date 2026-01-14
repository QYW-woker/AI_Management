package com.lifemanager.app.core.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.lifemanager.app.core.database.entity.TodoEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 提醒管理器
 *
 * 负责调度和取消待办提醒的定时任务
 */
@Singleton
class ReminderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ReminderManager"
        private const val REQUEST_CODE_BASE = 10000
    }

    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    /**
     * 调度单个提醒
     *
     * @param todo 待办事项
     */
    fun scheduleReminder(todo: TodoEntity) {
        val reminderAt = todo.reminderAt ?: return

        // 如果提醒时间已过，不调度
        if (reminderAt <= System.currentTimeMillis()) {
            Log.d(TAG, "Reminder time already passed for todo: ${todo.id}")
            return
        }

        val intent = createReminderIntent(todo)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRequestCode(todo.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderAt,
                        pendingIntent
                    )
                } else {
                    // 无精确闹钟权限，使用非精确闹钟
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderAt,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderAt,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderAt,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled reminder for todo: ${todo.id} at $reminderAt")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule reminder for todo: ${todo.id}", e)
        }
    }

    /**
     * 取消提醒
     *
     * @param todoId 待办事项ID
     */
    fun cancelReminder(todoId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRequestCode(todoId),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d(TAG, "Cancelled reminder for todo: $todoId")
        }
    }

    /**
     * 重新调度所有待办提醒
     *
     * @param todos 待提醒的待办列表
     */
    fun rescheduleAllReminders(todos: List<TodoEntity>) {
        Log.d(TAG, "Rescheduling ${todos.size} reminders")
        todos.forEach { todo ->
            scheduleReminder(todo)
        }
    }

    /**
     * 创建提醒Intent
     */
    private fun createReminderIntent(todo: TodoEntity): Intent {
        return Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
            putExtra(ReminderReceiver.EXTRA_TODO_ID, todo.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, todo.title)
            putExtra(ReminderReceiver.EXTRA_CONTENT, todo.description)
            putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_TODO)
        }
    }

    /**
     * 生成唯一的请求码
     */
    private fun getRequestCode(todoId: Long): Int {
        return (REQUEST_CODE_BASE + todoId).toInt()
    }
}
