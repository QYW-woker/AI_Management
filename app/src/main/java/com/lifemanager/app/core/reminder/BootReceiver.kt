package com.lifemanager.app.core.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Room
import com.lifemanager.app.core.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 开机广播接收器
 *
 * 用于在设备重启后恢复所有定时提醒
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        Log.d(TAG, "Boot completed, rescheduling reminders")

        // 使用goAsync()确保广播接收器有足够时间完成工作
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                rescheduleAllReminders(context)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule reminders", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * 重新调度所有待提醒的任务
     */
    private suspend fun rescheduleAllReminders(context: Context) {
        // 直接创建数据库实例（因为这不是Hilt管理的组件）
        val database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()

        try {
            val todoDao = database.todoDao()
            val now = System.currentTimeMillis()

            // 获取所有待提醒的任务
            val upcomingReminders = todoDao.getUpcomingRemindersSync(now)

            Log.d(TAG, "Found ${upcomingReminders.size} reminders to reschedule")

            // 创建ReminderManager并重新调度
            val reminderManager = ReminderManager(context.applicationContext)
            reminderManager.rescheduleAllReminders(upcomingReminders)

            Log.d(TAG, "Successfully rescheduled ${upcomingReminders.size} reminders")
        } finally {
            database.close()
        }
    }
}
