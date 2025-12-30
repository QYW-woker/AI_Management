package com.lifemanager.app.core.floatingball

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.lifemanager.app.core.ai.model.CommandIntent
import com.lifemanager.app.core.ai.model.TransactionType

/**
 * 悬浮确认弹窗视图
 * 用于在应用外显示语音识别结果确认
 */
class FloatingConfirmationView(context: Context) : FrameLayout(context) {

    private var onConfirm: (() -> Unit)? = null
    private var onCancel: (() -> Unit)? = null

    private val titleText: TextView
    private val contentText: TextView
    private val detailText: TextView
    private val confirmButton: Button
    private val cancelButton: Button

    init {
        val density = context.resources.displayMetrics.density

        // 半透明背景
        setBackgroundColor(Color.parseColor("#80000000"))
        setOnClickListener { onCancel?.invoke() }

        // 卡片容器
        val cardView = CardView(context).apply {
            radius = 16 * density
            cardElevation = 8 * density
            setCardBackgroundColor(Color.WHITE)
            setOnClickListener { /* 阻止点击穿透 */ }
        }

        val cardParams = LayoutParams(
            (context.resources.displayMetrics.widthPixels * 0.85).toInt(),
            LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }

        // 内容布局
        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                (20 * density).toInt(),
                (16 * density).toInt(),
                (20 * density).toInt(),
                (16 * density).toInt()
            )
        }

        // 标题
        titleText = TextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTextColor(Color.parseColor("#1976D2"))
            text = "语音识别结果"
            gravity = Gravity.CENTER
        }

        // 原始内容
        contentText = TextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(Color.parseColor("#333333"))
            setPadding(0, (12 * density).toInt(), 0, (8 * density).toInt())
        }

        // 详情
        detailText = TextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(Color.parseColor("#666666"))
            setPadding(0, 0, 0, (16 * density).toInt())
        }

        // 按钮容器
        val buttonLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        // 取消按钮
        cancelButton = Button(context).apply {
            text = "取消"
            setTextColor(Color.parseColor("#666666"))
            background = createRoundedDrawable(Color.parseColor("#E0E0E0"), 8 * density)
            setPadding(
                (24 * density).toInt(),
                (8 * density).toInt(),
                (24 * density).toInt(),
                (8 * density).toInt()
            )
            setOnClickListener { onCancel?.invoke() }
        }

        // 确认按钮
        confirmButton = Button(context).apply {
            text = "确认记录"
            setTextColor(Color.WHITE)
            background = createRoundedDrawable(Color.parseColor("#1976D2"), 8 * density)
            setPadding(
                (24 * density).toInt(),
                (8 * density).toInt(),
                (24 * density).toInt(),
                (8 * density).toInt()
            )
            setOnClickListener { onConfirm?.invoke() }
        }

        val buttonParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = (8 * density).toInt()
            marginEnd = (8 * density).toInt()
        }

        buttonLayout.addView(cancelButton, buttonParams)
        buttonLayout.addView(confirmButton, buttonParams)

        contentLayout.addView(titleText)
        contentLayout.addView(contentText)
        contentLayout.addView(detailText)
        contentLayout.addView(buttonLayout)

        cardView.addView(contentLayout)
        addView(cardView, cardParams)
    }

    /**
     * 设置确认回调
     */
    fun setOnConfirmListener(listener: () -> Unit) {
        onConfirm = listener
    }

    /**
     * 设置取消回调
     */
    fun setOnCancelListener(listener: () -> Unit) {
        onCancel = listener
    }

    /**
     * 更新显示内容
     */
    fun updateContent(originalText: String, intent: CommandIntent) {
        contentText.text = "\"$originalText\""

        val (title, detail) = when (intent) {
            is CommandIntent.Transaction -> {
                val typeStr = if (intent.type == TransactionType.EXPENSE) "支出" else "收入"
                val emoji = if (intent.type == TransactionType.EXPENSE) "💸" else "💰"
                "记账 $emoji" to "类型：$typeStr\n金额：¥${String.format("%.2f", intent.amount ?: 0.0)}\n备注：${intent.note ?: intent.categoryName ?: "-"}"
            }
            is CommandIntent.Todo -> {
                "待办事项 📝" to "内容：${intent.title}\n${if (intent.dueDate != null) "截止日期：已设置" else ""}"
            }
            is CommandIntent.Goal -> {
                "目标 🎯" to "目标：${intent.goalName ?: "新目标"}\n${if (intent.targetAmount != null) "目标金额：¥${intent.targetAmount}" else ""}"
            }
            is CommandIntent.Diary -> {
                "日记 📔" to "内容：${intent.content.take(50)}${if (intent.content.length > 50) "..." else ""}"
            }
            is CommandIntent.HabitCheckin -> {
                "习惯打卡 ✅" to "习惯：${intent.habitName ?: "打卡"}"
            }
            is CommandIntent.Query -> {
                "查询 🔍" to "正在查询..."
            }
            is CommandIntent.Navigate -> {
                "导航 📱" to "打开：${intent.screen}"
            }
            is CommandIntent.TimeTrack -> {
                "时间追踪 ⏱️" to "任务：${intent.note ?: intent.categoryName ?: "-"}"
            }
            is CommandIntent.Savings -> {
                "储蓄 💰" to "金额：¥${String.format("%.2f", intent.amount ?: 0.0)}"
            }
            is CommandIntent.Unknown -> {
                "未识别 ❓" to "无法识别该命令，请重试"
            }
        }

        titleText.text = title
        detailText.text = detail

        // Unknown时隐藏确认按钮
        confirmButton.visibility = if (intent is CommandIntent.Unknown) View.GONE else View.VISIBLE
    }

    /**
     * 创建圆角背景
     */
    private fun createRoundedDrawable(color: Int, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(color)
        }
    }
}
