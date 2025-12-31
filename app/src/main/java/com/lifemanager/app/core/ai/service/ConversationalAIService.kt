package com.lifemanager.app.core.ai.service

import com.google.gson.Gson
import com.lifemanager.app.core.ai.model.*
import com.lifemanager.app.core.ai.service.api.ChatRequest
import com.lifemanager.app.core.ai.service.api.DeepSeekApi
import com.lifemanager.app.core.database.dao.*
import com.lifemanager.app.core.database.entity.CustomFieldEntity
import com.lifemanager.app.data.repository.AIConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import com.lifemanager.app.core.ai.service.api.ChatMessage as ApiChatMessage

/**
 * 对话式AI服务
 *
 * 支持多轮对话，保持上下文，智能理解用户意图
 */
@Singleton
class ConversationalAIService @Inject constructor(
    private val api: DeepSeekApi,
    private val configRepository: AIConfigRepository,
    private val transactionDao: DailyTransactionDao,
    private val todoDao: TodoDao,
    private val habitDao: HabitDao,
    private val habitRecordDao: HabitRecordDao,
    private val goalDao: GoalDao,
    private val budgetDao: BudgetDao,
    private val customFieldDao: CustomFieldDao,
    private val gson: Gson
) {
    private val _conversationContext = MutableStateFlow(ConversationContext())
    val conversationContext: StateFlow<ConversationContext> = _conversationContext.asStateFlow()

    private val _aiMode = MutableStateFlow(AIMode.ASSISTANT)
    val aiMode: StateFlow<AIMode> = _aiMode.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    /**
     * 发送消息并获取AI回复
     */
    suspend fun sendMessage(userMessage: String): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            _isProcessing.value = true

            val config = configRepository.getConfig()
            if (!config.isConfigured) {
                return@withContext Result.failure(Exception("AI未配置"))
            }

            // 添加用户消息到上下文
            val userChatMessage = ChatMessage(
                role = ChatRole.USER,
                content = userMessage
            )
            _conversationContext.value = _conversationContext.value.addMessage(userChatMessage)

            // 构建对话历史
            val messages = buildConversationMessages(userMessage)

            val request = ChatRequest(
                model = config.model,
                messages = messages,
                temperature = 0.5,
                maxTokens = 1000
            )

            val response = api.chatCompletion(
                authorization = "Bearer ${config.apiKey}",
                request = request
            )

            val content = response.choices?.firstOrNull()?.message?.content as? String
                ?: return@withContext Result.failure(Exception("AI响应为空"))

            // 解析响应
            val (responseText, intent, suggestions) = parseConversationalResponse(content)

            val assistantMessage = ChatMessage(
                role = ChatRole.ASSISTANT,
                content = responseText,
                intent = intent,
                suggestions = suggestions
            )

            _conversationContext.value = _conversationContext.value.addMessage(assistantMessage)
            _isProcessing.value = false

            Result.success(assistantMessage)
        } catch (e: Exception) {
            _isProcessing.value = false
            Result.failure(e)
        }
    }

    /**
     * 构建对话消息列表
     */
    private suspend fun buildConversationMessages(currentMessage: String): List<ApiChatMessage> {
        val messages = mutableListOf<ApiChatMessage>()

        // 系统提示
        val systemPrompt = buildSystemPrompt()
        messages.add(ApiChatMessage("system", systemPrompt))

        // 历史消息（最多保留5轮对话）
        val recentMessages = _conversationContext.value.getRecentMessages(10)
        for (msg in recentMessages.dropLast(1)) { // 排除刚添加的用户消息
            val role = when (msg.role) {
                ChatRole.USER -> "user"
                ChatRole.ASSISTANT -> "assistant"
                ChatRole.SYSTEM -> "system"
            }
            messages.add(ApiChatMessage(role, msg.content))
        }

        // 当前用户消息
        messages.add(ApiChatMessage("user", currentMessage))

        return messages
    }

    /**
     * 构建系统提示
     */
    private suspend fun buildSystemPrompt(): String {
        val today = LocalDate.now()
        val todayEpochDay = today.toEpochDay()

        // 获取用户数据上下文
        val dataContext = buildDataContext()

        return """
你是"小管家"，一个智能生活助理。你的特点是：
- 亲切友好，像朋友一样聊天
- 专业高效，能准确理解用户意图
- 主动贴心，适时给出建议

今天是$today（星期${today.dayOfWeek.value}），epochDay=$todayEpochDay

$dataContext

你可以帮助用户：
1. 记账 - 记录收入支出
2. 待办 - 管理任务和日程
3. 习惯 - 追踪习惯打卡
4. 目标 - 管理个人目标
5. 查询 - 查询财务、习惯等数据
6. 建议 - 给出个性化建议

对话规则：
1. 如果用户要执行操作（记账、添加待办等），提取关键信息并确认
2. 如果信息不完整，友好地询问补充
3. 对于查询请求，直接回答并可附带建议
4. 保持对话流畅自然，适当使用表情符号

响应格式（JSON）：
{
  "text": "回复给用户的文字",
  "intent": {
    "type": "transaction|todo|habit|goal|query|chat|none",
    "data": {...}  // 意图相关数据
  },
  "suggestions": ["建议回复1", "建议回复2"]  // 可选的快捷回复建议
}

只返回JSON，不要其他内容。
""".trimIndent()
    }

    /**
     * 构建用户数据上下文
     */
    private suspend fun buildDataContext(): String {
        val today = LocalDate.now()
        val todayEpoch = today.toEpochDay().toInt()
        val monthStart = YearMonth.now().atDay(1).toEpochDay().toInt()

        // 本月财务数据
        val transactions = transactionDao.getTransactionsBetweenDatesSync(monthStart, todayEpoch)
        val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val expense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

        // 今日待办
        val todayTodos = todoDao.getByDateSync(todayEpoch).filter { it.status == "PENDING" }

        // 习惯
        val habits = habitDao.getEnabledSync()
        val todayRecords = habitRecordDao.getByDateSync(todayEpoch)
        val uncheckedCount = habits.size - todayRecords.size

        // 活跃目标
        val goals = goalDao.getActiveGoalsSync()

        // 预算
        val budgets = budgetDao.getAllSync()
        val budgetUsage = budgets.map { budget ->
            val spent = transactions.filter {
                it.type == "EXPENSE" && (budget.categoryId == null || it.categoryId == budget.categoryId)
            }.sumOf { it.amount }
            "${budget.name}: ${String.format("%.0f", spent / budget.totalBudget * 100)}%"
        }.joinToString(", ")

        // 分类
        val categories = customFieldDao.getAllFieldsSync()
        val categoryNames = categories.take(10).map { it.name }.joinToString("、")

        return """
【用户数据概览】
- 本月收入: ¥${String.format("%.2f", income)}
- 本月支出: ¥${String.format("%.2f", expense)}
- 今日待办: ${todayTodos.size}项
- 习惯未打卡: ${uncheckedCount}/${habits.size}个
- 活跃目标: ${goals.size}个
- 预算使用: $budgetUsage
- 可用分类: $categoryNames
""".trimIndent()
    }

    /**
     * 解析对话响应
     */
    private fun parseConversationalResponse(json: String): Triple<String, CommandIntent?, List<QuickReply>> {
        return try {
            val jsonStr = extractJson(json)
            val map = gson.fromJson(jsonStr, Map::class.java) as? Map<String, Any>
                ?: return Triple(json, null, emptyList())

            val text = map["text"] as? String ?: json
            val intentData = map["intent"] as? Map<String, Any>
            val suggestionsData = map["suggestions"] as? List<String>

            val intent = intentData?.let { parseIntent(it) }
            val suggestions = suggestionsData?.map { QuickReply(it) } ?: emptyList()

            Triple(text, intent, suggestions)
        } catch (e: Exception) {
            Triple(json, null, emptyList())
        }
    }

    /**
     * 解析意图
     */
    private fun parseIntent(data: Map<String, Any>): CommandIntent? {
        val type = data["type"] as? String ?: return null
        val intentData = data["data"] as? Map<String, Any> ?: emptyMap()

        return when (type) {
            "transaction" -> parseTransactionIntent(intentData)
            "todo" -> parseTodoIntent(intentData)
            "habit" -> parseHabitIntent(intentData)
            "goal" -> parseGoalIntent(intentData)
            "query" -> parseQueryIntent(intentData)
            else -> null
        }
    }

    private fun parseTransactionIntent(data: Map<String, Any>): CommandIntent.Transaction {
        val typeStr = data["transactionType"] as? String ?: "expense"
        val type = if (typeStr.equals("income", ignoreCase = true))
            TransactionType.INCOME else TransactionType.EXPENSE

        return CommandIntent.Transaction(
            type = type,
            amount = (data["amount"] as? Number)?.toDouble(),
            categoryName = data["category"] as? String,
            date = (data["date"] as? Number)?.toInt(),
            note = data["note"] as? String
        )
    }

    private fun parseTodoIntent(data: Map<String, Any>): CommandIntent.Todo {
        return CommandIntent.Todo(
            title = data["title"] as? String ?: "",
            description = data["description"] as? String,
            dueDate = (data["dueDate"] as? Number)?.toInt(),
            startTime = data["startTime"] as? String,
            endTime = data["endTime"] as? String,
            priority = data["priority"] as? String,
            quadrant = data["quadrant"] as? String
        )
    }

    private fun parseHabitIntent(data: Map<String, Any>): CommandIntent.HabitCheckin {
        return CommandIntent.HabitCheckin(
            habitName = data["habitName"] as? String ?: "",
            value = (data["value"] as? Number)?.toDouble()
        )
    }

    private fun parseGoalIntent(data: Map<String, Any>): CommandIntent.Goal {
        return CommandIntent.Goal(
            action = GoalAction.CHECK,
            goalName = data["goalName"] as? String
        )
    }

    private fun parseQueryIntent(data: Map<String, Any>): CommandIntent.Query {
        val typeStr = data["queryType"] as? String ?: "today_expense"
        val queryType = when (typeStr.lowercase()) {
            "month_expense" -> QueryType.MONTH_EXPENSE
            "month_income" -> QueryType.MONTH_INCOME
            "category_expense" -> QueryType.CATEGORY_EXPENSE
            "habit_streak" -> QueryType.HABIT_STREAK
            "goal_progress" -> QueryType.GOAL_PROGRESS
            "savings_progress" -> QueryType.SAVINGS_PROGRESS
            else -> QueryType.TODAY_EXPENSE
        }
        return CommandIntent.Query(type = queryType)
    }

    /**
     * 执行数据查询
     */
    suspend fun executeQuery(query: String): Result<DataQueryResult> = withContext(Dispatchers.IO) {
        try {
            val config = configRepository.getConfig()
            if (!config.isConfigured) {
                return@withContext Result.failure(Exception("AI未配置"))
            }

            val dataContext = buildDetailedDataForQuery()

            val prompt = """
根据用户查询和数据，生成查询结果。

用户查询：$query

$dataContext

请按以下JSON格式返回：
{
  "success": true,
  "queryType": "expense|income|budget|habit|goal",
  "summary": "一句话总结查询结果",
  "details": [
    {"label": "项目名", "value": "数值/描述", "change": 变化百分比, "trend": "UP|DOWN|STABLE"}
  ],
  "suggestions": ["基于数据的建议1", "建议2"]
}

只返回JSON。
""".trimIndent()

            val request = ChatRequest(
                model = config.model,
                messages = listOf(ApiChatMessage("user", prompt)),
                temperature = 0.3,
                maxTokens = 500
            )

            val response = api.chatCompletion(
                authorization = "Bearer ${config.apiKey}",
                request = request
            )

            val content = response.choices?.firstOrNull()?.message?.content as? String
                ?: return@withContext Result.failure(Exception("AI响应为空"))

            val result = parseQueryResult(content)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun buildDetailedDataForQuery(): String {
        val today = LocalDate.now()
        val todayEpoch = today.toEpochDay().toInt()
        val monthStart = YearMonth.now().atDay(1).toEpochDay().toInt()
        val lastMonthStart = YearMonth.now().minusMonths(1).atDay(1).toEpochDay().toInt()
        val lastMonthEnd = YearMonth.now().minusMonths(1).atEndOfMonth().toEpochDay().toInt()

        // 本月数据
        val thisMonthTransactions = transactionDao.getTransactionsBetweenDatesSync(monthStart, todayEpoch)
        val thisMonthIncome = thisMonthTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val thisMonthExpense = thisMonthTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

        // 上月数据
        val lastMonthTransactions = transactionDao.getTransactionsBetweenDatesSync(lastMonthStart, lastMonthEnd)
        val lastMonthIncome = lastMonthTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val lastMonthExpense = lastMonthTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

        // 今日数据
        val todayTransactions = transactionDao.getByDateSync(todayEpoch)
        val todayIncome = todayTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val todayExpense = todayTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

        // 分类统计
        val categories = customFieldDao.getAllFieldsSync()
        val categoryMap = categories.associate { it.id to it.name }
        val categoryExpenses = thisMonthTransactions
            .filter { it.type == "EXPENSE" }
            .groupBy { it.categoryId }
            .mapValues { it.value.sumOf { t -> t.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { (catId, amount) ->
                val name = catId?.let { categoryMap[it] } ?: "未分类"
                "$name: ¥${String.format("%.2f", amount)}"
            }

        // 预算
        val budgets = budgetDao.getAllSync()
        val budgetInfo = budgets.map { budget ->
            val spent = thisMonthTransactions.filter {
                it.type == "EXPENSE" && (budget.categoryId == null || it.categoryId == budget.categoryId)
            }.sumOf { it.amount }
            "${budget.name}: 已用¥${String.format("%.2f", spent)}/${String.format("%.2f", budget.totalBudget)}"
        }

        // 习惯
        val habits = habitDao.getEnabledSync()
        val weekStart = today.minusDays(6).toEpochDay().toInt()
        val weekRecords = habitRecordDao.getRecordsInRangeSync(weekStart, todayEpoch)
        val habitStats = habits.map { habit ->
            val count = weekRecords.count { it.habitId == habit.id }
            "${habit.name}: 本周$count/7天"
        }

        // 目标
        val goals = goalDao.getActiveGoalsSync()
        val goalStats = goals.map { goal ->
            val progress = if ((goal.targetValue ?: 0.0) > 0) {
                (goal.currentValue / goal.targetValue!! * 100).toInt()
            } else 0
            "${goal.title}: ${progress}%"
        }

        return """
【详细数据】
今日: 收入¥${String.format("%.2f", todayIncome)}, 支出¥${String.format("%.2f", todayExpense)}
本月: 收入¥${String.format("%.2f", thisMonthIncome)}, 支出¥${String.format("%.2f", thisMonthExpense)}, 结余¥${String.format("%.2f", thisMonthIncome - thisMonthExpense)}
上月: 收入¥${String.format("%.2f", lastMonthIncome)}, 支出¥${String.format("%.2f", lastMonthExpense)}
分类消费TOP5: ${categoryExpenses.joinToString("; ")}
预算情况: ${budgetInfo.joinToString("; ")}
习惯打卡: ${habitStats.joinToString("; ")}
目标进度: ${goalStats.joinToString("; ")}
""".trimIndent()
    }

    private fun parseQueryResult(json: String): DataQueryResult {
        return try {
            val jsonStr = extractJson(json)
            val map = gson.fromJson(jsonStr, Map::class.java) as? Map<String, Any>
                ?: return DataQueryResult(false, "", "解析失败")

            val details = (map["details"] as? List<Map<String, Any>>)?.map { item ->
                QueryDetail(
                    label = item["label"] as? String ?: "",
                    value = item["value"]?.toString() ?: "",
                    change = (item["change"] as? Number)?.toDouble(),
                    trend = (item["trend"] as? String)?.let {
                        try { TrendDirection.valueOf(it) } catch (e: Exception) { null }
                    }
                )
            } ?: emptyList()

            DataQueryResult(
                success = map["success"] as? Boolean ?: true,
                queryType = map["queryType"] as? String ?: "",
                summary = map["summary"] as? String ?: "",
                details = details,
                suggestions = (map["suggestions"] as? List<String>) ?: emptyList()
            )
        } catch (e: Exception) {
            DataQueryResult(false, "", "解析失败: ${e.message}")
        }
    }

    /**
     * 切换AI模式
     */
    fun setAIMode(mode: AIMode) {
        _aiMode.value = mode
    }

    /**
     * 清除对话历史
     */
    fun clearConversation() {
        _conversationContext.value = ConversationContext()
    }

    /**
     * 获取欢迎消息
     */
    suspend fun getWelcomeMessage(): ChatMessage {
        val today = LocalDate.now()
        val hour = java.time.LocalTime.now().hour

        val greeting = when (hour) {
            in 5..11 -> "早上好"
            in 12..13 -> "中午好"
            in 14..17 -> "下午好"
            in 18..22 -> "晚上好"
            else -> "夜深了"
        }

        // 获取今日简要数据
        val todayEpoch = today.toEpochDay().toInt()
        val todayTransactions = transactionDao.getByDateSync(todayEpoch)
        val todayExpense = todayTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val todayTodos = todoDao.getByDateSync(todayEpoch).filter { it.status == "PENDING" }

        val statusText = buildString {
            if (todayExpense > 0) {
                append("今日已消费¥${String.format("%.2f", todayExpense)}")
            }
            if (todayTodos.isNotEmpty()) {
                if (isNotEmpty()) append("，")
                append("有${todayTodos.size}项待办")
            }
        }

        val content = "$greeting！我是小管家，很高兴为你服务。\n" +
                (if (statusText.isNotEmpty()) "$statusText。\n" else "") +
                "你可以告诉我要记账、添加待办，或者问我任何问题~"

        val suggestions = listOf(
            QuickReply("今天花了多少"),
            QuickReply("本月收支情况"),
            QuickReply("记一笔支出"),
            QuickReply("添加待办")
        )

        return ChatMessage(
            role = ChatRole.ASSISTANT,
            content = content,
            suggestions = suggestions
        )
    }

    private fun extractJson(text: String): String {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        return if (start >= 0 && end > start) {
            text.substring(start, end + 1)
        } else {
            text
        }
    }
}
