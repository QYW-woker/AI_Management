package com.lifemanager.app.core.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 提醒广播接收器
 *
 * 用于接收定时提醒的广播，显示通知
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // TODO: 实现提醒通知显示
        // 1. 获取提醒内容
        // 2. 创建通知渠道
        // 3. 显示通知
    }

    companion object {
        const val ACTION_REMINDER = "com.lifemanager.app.REMINDER_ACTION"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CONTENT = "extra_content"
        const val EXTRA_TYPE = "extra_type"
    }
}
