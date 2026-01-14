package com.lifemanager.app.core.ai.model

import java.time.LocalDate

/**
 * AI智能建议
 */
data class AISuggestion(
    val id: String,
    val type: SuggestionType,
    val title: String,
    val description: String,
    val action: SuggestionAction? = null,
    val priority: Int = 0,                // 优先级 0-100
    val expiresAt: Long? = null,          // 过期时间
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * 建议类型
 */
enum class SuggestionType {
    SPENDING_ALERT,           // 消费提醒
    BUDGET_WARNING,           // 预算警告
    HABIT_REMINDER,           // 习惯提醒
    GOAL_MILESTONE,           // 目标里程碑
    SAVINGS_TIP,              // 存钱建议
    HEALTH_TIP,               // 健康建议
    WEEKLY_INSIGHT,           // 周度洞察
    QUICK_ACTION,             // 快捷操作
    PERSONALIZED,             // 个性化建议
    WEATHER_REMINDER,         // 天气相关提醒
    TIME_SENSITIVE            // 时间敏感提醒
}

/**
 * 建议动作
 */
sealed class SuggestionAction {
    data class Navigate(val screen: String) : SuggestionAction()
    data class CreateTransaction(val type: String, val category: String? = null) : SuggestionAction()
    data class CreateTodo(val title: String, val dueDate: Int? = null) : SuggestionAction()
    data class HabitCheckin(val habitId: Long) : SuggestionAction()
    data class GoalUpdate(val goalId: Long) : SuggestionAction()
    data class ViewReport(val reportType: String) : SuggestionAction()
    data class Dismiss(val suggestionId: String) : SuggestionAction()
}

/**
 * 对话消息
 */
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: ChatRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val intent: CommandIntent? = null,           // 如果消息触发了意图
    val suggestions: List<QuickReply> = emptyList(),  // 快捷回复建议
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * 对话角色
 */
enum class ChatRole {
    USER,
    ASSISTANT,
    SYSTEM
}

/**
 * 快捷回复
 */
data class QuickReply(
    val text: String,
    val action: String? = null    // 可选的动作标识
)

/**
 * 对话上下文
 */
data class ConversationContext(
    val messages: List<ChatMessage> = emptyList(),
    val sessionId: String = java.util.UUID.randomUUID().toString(),
    val startedAt: Long = System.currentTimeMillis(),
    val userPreferences: Map<String, Any> = emptyMap(),
    val recentTopics: List<String> = emptyList()
) {
    fun addMessage(message: ChatMessage): ConversationContext {
        return copy(messages = messages + message)
    }

    fun getRecentMessages(count: Int = 5): List<ChatMessage> {
        return messages.takeLast(count)
    }
}

/**
 * 快捷操作
 */
data class QuickAction(
    val id: String,
    val icon: String,           // Material icon name
    val title: String,
    val subtitle: String? = null,
    val command: String,        // 对应的命令文本
    val category: QuickActionCategory
)

/**
 * 快捷操作分类
 */
enum class QuickActionCategory {
    FINANCE,        // 财务相关
    TODO,           // 待办相关
    HABIT,          // 习惯相关
    GOAL,           // 目标相关
    QUERY,          // 查询相关
    NAVIGATION      // 导航相关
}

/**
 * 数据查询结果
 */
data class DataQueryResult(
    val success: Boolean,
    val queryType: String,
    val summary: String,
    val details: List<QueryDetail> = emptyList(),
    val chart: ChartData? = null,
    val suggestions: List<String> = emptyList()
)

/**
 * 查询详情
 */
data class QueryDetail(
    val label: String,
    val value: String,
    val change: Double? = null,     // 变化百分比
    val trend: TrendDirection? = null
)

/**
 * 趋势方向
 */
enum class TrendDirection {
    UP, DOWN, STABLE
}

/**
 * 图表数据
 */
data class ChartData(
    val type: ChartType,
    val labels: List<String>,
    val datasets: List<ChartDataset>
)

enum class ChartType {
    LINE, BAR, PIE, DOUGHNUT
}

data class ChartDataset(
    val label: String,
    val data: List<Double>,
    val color: String? = null
)

/**
 * AI对话模式
 */
enum class AIMode {
    COMMAND,        // 命令模式 - 单次命令执行
    CHAT,           // 对话模式 - 多轮对话
    ASSISTANT       // 助手模式 - 智能建议
}

/**
 * 用户行为分析
 */
data class UserBehaviorInsight(
    val spendingPatterns: List<SpendingPattern> = emptyList(),
    val habitPatterns: List<HabitPattern> = emptyList(),
    val peakActivityTimes: List<Int> = emptyList(),     // 活跃时段 (0-23)
    val preferredCategories: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

data class SpendingPattern(
    val dayOfWeek: Int,         // 1-7
    val averageAmount: Double,
    val topCategory: String?
)

data class HabitPattern(
    val habitId: Long,
    val habitName: String,
    val bestTime: Int,          // 最佳打卡时间 (0-23)
    val successRate: Double     // 成功率 0-1
)
