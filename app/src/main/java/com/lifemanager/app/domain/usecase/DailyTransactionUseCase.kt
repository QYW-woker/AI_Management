package com.lifemanager.app.domain.usecase

import com.lifemanager.app.core.database.entity.DailyTransactionEntity
import com.lifemanager.app.domain.model.*
import com.lifemanager.app.domain.repository.CustomFieldRepository
import com.lifemanager.app.domain.repository.DailyTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

/**
 * 日常记账用例
 */
class DailyTransactionUseCase @Inject constructor(
    private val transactionRepository: DailyTransactionRepository,
    private val fieldRepository: CustomFieldRepository
) {

    /**
     * 获取指定日期的交易（带分类信息）
     */
    fun getTransactionsByDate(date: Int): Flow<List<DailyTransactionWithCategory>> {
        return combine(
            transactionRepository.getByDate(date),
            fieldRepository.getFieldsByModule("DAILY_EXPENSE")
        ) { transactions, fields ->
            val fieldMap = fields.associateBy { it.id }
            transactions.map { transaction ->
                DailyTransactionWithCategory(
                    transaction = transaction,
                    category = transaction.categoryId?.let { fieldMap[it] }
                )
            }
        }
    }

    /**
     * 获取指定日期范围的交易（按日期分组）
     */
    fun getTransactionGroupsByDateRange(startDate: Int, endDate: Int): Flow<List<DailyTransactionGroup>> {
        return combine(
            transactionRepository.getByDateRange(startDate, endDate),
            fieldRepository.getFieldsByModule("DAILY_EXPENSE")
        ) { transactions, fields ->
            val fieldMap = fields.associateBy { it.id }

            transactions
                .groupBy { it.date }
                .map { (date, dayTransactions) ->
                    val localDate = LocalDate.ofEpochDay(date.toLong())
                    val withCategory = dayTransactions.map { transaction ->
                        DailyTransactionWithCategory(
                            transaction = transaction,
                            category = transaction.categoryId?.let { fieldMap[it] }
                        )
                    }

                    DailyTransactionGroup(
                        date = date,
                        dateText = formatDate(localDate),
                        dayOfWeek = localDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA),
                        transactions = withCategory,
                        totalIncome = dayTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                        totalExpense = dayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    )
                }
                .sortedByDescending { it.date }
        }
    }

    /**
     * 获取最近交易（按日期分组）
     */
    fun getRecentTransactionGroups(limit: Int = 50): Flow<List<DailyTransactionGroup>> {
        return combine(
            transactionRepository.getRecentTransactions(limit),
            fieldRepository.getFieldsByModule("DAILY_EXPENSE")
        ) { transactions, fields ->
            val fieldMap = fields.associateBy { it.id }

            transactions
                .groupBy { it.date }
                .map { (date, dayTransactions) ->
                    val localDate = LocalDate.ofEpochDay(date.toLong())
                    val withCategory = dayTransactions.map { transaction ->
                        DailyTransactionWithCategory(
                            transaction = transaction,
                            category = transaction.categoryId?.let { fieldMap[it] }
                        )
                    }

                    DailyTransactionGroup(
                        date = date,
                        dateText = formatDate(localDate),
                        dayOfWeek = localDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA),
                        transactions = withCategory,
                        totalIncome = dayTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                        totalExpense = dayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    )
                }
                .sortedByDescending { it.date }
        }
    }

    /**
     * 获取日期范围内的统计
     */
    suspend fun getPeriodStats(startDate: Int, endDate: Int): PeriodStats {
        val income = transactionRepository.getTotalByTypeInRange(startDate, endDate, TransactionType.INCOME)
        val expense = transactionRepository.getTotalByTypeInRange(startDate, endDate, TransactionType.EXPENSE)
        val count = transactionRepository.countInRange(startDate, endDate)
        val days = endDate - startDate + 1

        return PeriodStats(
            startDate = startDate,
            endDate = endDate,
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense,
            transactionCount = count,
            avgDailyExpense = if (days > 0) expense / days else 0.0
        )
    }

    /**
     * 获取本月统计
     */
    suspend fun getCurrentMonthStats(): PeriodStats {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())

        return getPeriodStats(
            startOfMonth.toEpochDay().toInt(),
            endOfMonth.toEpochDay().toInt()
        )
    }

    // ==================== 季度/年度统计 ====================

    /**
     * 获取指定月份的统计
     */
    suspend fun getMonthStats(year: Int, month: Int): MonthlyStats {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())
        val startEpoch = startDate.toEpochDay().toInt()
        val endEpoch = endDate.toEpochDay().toInt()

        val income = transactionRepository.getTotalByTypeInRange(startEpoch, endEpoch, TransactionType.INCOME)
        val expense = transactionRepository.getTotalByTypeInRange(startEpoch, endEpoch, TransactionType.EXPENSE)
        val count = transactionRepository.countInRange(startEpoch, endEpoch)
        val days = endDate.dayOfMonth

        return MonthlyStats(
            year = year,
            month = month,
            startDate = startEpoch,
            endDate = endEpoch,
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense,
            transactionCount = count,
            avgDailyExpense = if (days > 0) expense / days else 0.0
        )
    }

    /**
     * 获取当前季度统计
     */
    suspend fun getCurrentQuarterStats(): QuarterlyStats {
        val today = LocalDate.now()
        val quarter = (today.monthValue - 1) / 3 + 1
        return getQuarterStats(today.year, quarter)
    }

    /**
     * 获取指定季度统计
     */
    suspend fun getQuarterStats(year: Int, quarter: Int): QuarterlyStats {
        val startMonth = (quarter - 1) * 3 + 1
        val endMonth = startMonth + 2

        val startDate = LocalDate.of(year, startMonth, 1)
        val endDate = LocalDate.of(year, endMonth, 1).withDayOfMonth(
            LocalDate.of(year, endMonth, 1).lengthOfMonth()
        )
        val startEpoch = startDate.toEpochDay().toInt()
        val endEpoch = endDate.toEpochDay().toInt()

        val income = transactionRepository.getTotalByTypeInRange(startEpoch, endEpoch, TransactionType.INCOME)
        val expense = transactionRepository.getTotalByTypeInRange(startEpoch, endEpoch, TransactionType.EXPENSE)
        val count = transactionRepository.countInRange(startEpoch, endEpoch)
        val days = endEpoch - startEpoch + 1

        // 获取月度分解
        val monthlyBreakdown = (startMonth..endMonth).map { month ->
            getMonthStats(year, month)
        }

        // 获取分类分解
        val categoryBreakdown = getCategoryExpenseStats(startEpoch, endEpoch).first()

        return QuarterlyStats(
            year = year,
            quarter = quarter,
            startDate = startEpoch,
            endDate = endEpoch,
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense,
            transactionCount = count,
            avgMonthlyExpense = expense / 3,
            avgDailyExpense = if (days > 0) expense / days else 0.0,
            monthlyBreakdown = monthlyBreakdown,
            categoryBreakdown = categoryBreakdown
        )
    }

    /**
     * 获取当前年度统计
     */
    suspend fun getCurrentYearStats(): YearlyStats {
        return getYearStats(LocalDate.now().year)
    }

    /**
     * 获取指定年度统计
     */
    suspend fun getYearStats(year: Int): YearlyStats {
        val startDate = LocalDate.of(year, 1, 1)
        val endDate = LocalDate.of(year, 12, 31)
        val startEpoch = startDate.toEpochDay().toInt()
        val endEpoch = endDate.toEpochDay().toInt()

        val income = transactionRepository.getTotalByTypeInRange(startEpoch, endEpoch, TransactionType.INCOME)
        val expense = transactionRepository.getTotalByTypeInRange(startEpoch, endEpoch, TransactionType.EXPENSE)
        val count = transactionRepository.countInRange(startEpoch, endEpoch)
        val days = endEpoch - startEpoch + 1

        // 获取季度分解
        val quarterlyBreakdown = (1..4).map { quarter ->
            getQuarterStats(year, quarter)
        }

        // 获取月度分解
        val monthlyBreakdown = (1..12).map { month ->
            getMonthStats(year, month)
        }

        // 获取分类分解
        val categoryBreakdown = getCategoryExpenseStats(startEpoch, endEpoch).first()

        return YearlyStats(
            year = year,
            startDate = startEpoch,
            endDate = endEpoch,
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense,
            transactionCount = count,
            avgMonthlyExpense = expense / 12,
            avgDailyExpense = if (days > 0) expense / days else 0.0,
            quarterlyBreakdown = quarterlyBreakdown,
            monthlyBreakdown = monthlyBreakdown,
            categoryBreakdown = categoryBreakdown
        )
    }

    /**
     * 获取月度趋势（最近N个月）
     */
    suspend fun getMonthlyTrend(months: Int = 12): List<TrendDataPoint> {
        val today = LocalDate.now()
        return (0 until months).map { offset ->
            val date = today.minusMonths(offset.toLong())
            val stats = getMonthStats(date.year, date.monthValue)
            TrendDataPoint(
                label = stats.monthLabel,
                income = stats.totalIncome,
                expense = stats.totalExpense,
                balance = stats.balance
            )
        }.reversed()
    }

    /**
     * 获取季度趋势（最近N个季度）
     */
    suspend fun getQuarterlyTrend(quarters: Int = 4): List<TrendDataPoint> {
        val today = LocalDate.now()
        val currentQuarter = (today.monthValue - 1) / 3 + 1
        var year = today.year
        var quarter = currentQuarter

        return (0 until quarters).map {
            val stats = getQuarterStats(year, quarter)
            val point = TrendDataPoint(
                label = "Q$quarter",
                income = stats.totalIncome,
                expense = stats.totalExpense,
                balance = stats.balance
            )

            quarter--
            if (quarter < 1) {
                quarter = 4
                year--
            }
            point
        }.reversed()
    }

    /**
     * 获取月度同比分析（与去年同月比较）
     */
    suspend fun getMonthYearOverYearComparison(year: Int, month: Int): ComparisonStats {
        val currentStats = getMonthStats(year, month)
        val previousStats = getMonthStats(year - 1, month)

        return createComparisonStats(
            PeriodStats(
                startDate = currentStats.startDate,
                endDate = currentStats.endDate,
                totalIncome = currentStats.totalIncome,
                totalExpense = currentStats.totalExpense,
                balance = currentStats.balance,
                transactionCount = currentStats.transactionCount,
                avgDailyExpense = currentStats.avgDailyExpense
            ),
            PeriodStats(
                startDate = previousStats.startDate,
                endDate = previousStats.endDate,
                totalIncome = previousStats.totalIncome,
                totalExpense = previousStats.totalExpense,
                balance = previousStats.balance,
                transactionCount = previousStats.transactionCount,
                avgDailyExpense = previousStats.avgDailyExpense
            )
        )
    }

    /**
     * 获取月度环比分析（与上月比较）
     */
    suspend fun getMonthMonthOverMonthComparison(year: Int, month: Int): ComparisonStats {
        val currentStats = getMonthStats(year, month)

        val prevMonth = if (month == 1) 12 else month - 1
        val prevYear = if (month == 1) year - 1 else year
        val previousStats = getMonthStats(prevYear, prevMonth)

        return createComparisonStats(
            PeriodStats(
                startDate = currentStats.startDate,
                endDate = currentStats.endDate,
                totalIncome = currentStats.totalIncome,
                totalExpense = currentStats.totalExpense,
                balance = currentStats.balance,
                transactionCount = currentStats.transactionCount,
                avgDailyExpense = currentStats.avgDailyExpense
            ),
            PeriodStats(
                startDate = previousStats.startDate,
                endDate = previousStats.endDate,
                totalIncome = previousStats.totalIncome,
                totalExpense = previousStats.totalExpense,
                balance = previousStats.balance,
                transactionCount = previousStats.transactionCount,
                avgDailyExpense = previousStats.avgDailyExpense
            )
        )
    }

    /**
     * 创建比较统计
     */
    private fun createComparisonStats(current: PeriodStats, previous: PeriodStats): ComparisonStats {
        fun calcChange(current: Double, previous: Double): Double {
            return if (previous > 0) (current - previous) / previous * 100 else 0.0
        }

        return ComparisonStats(
            currentPeriod = current,
            previousPeriod = previous,
            incomeChange = calcChange(current.totalIncome, previous.totalIncome),
            expenseChange = calcChange(current.totalExpense, previous.totalExpense),
            balanceChange = calcChange(current.balance, previous.balance)
        )
    }

    /**
     * 获取今日统计
     */
    suspend fun getTodayStats(): DailyStats {
        val today = LocalDate.now().toEpochDay().toInt()
        val income = transactionRepository.getTotalByTypeInRange(today, today, TransactionType.INCOME)
        val expense = transactionRepository.getTotalByTypeInRange(today, today, TransactionType.EXPENSE)
        val count = transactionRepository.countInRange(today, today)

        return DailyStats(
            date = today,
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense,
            transactionCount = count
        )
    }

    /**
     * 获取分类支出统计
     */
    fun getCategoryExpenseStats(startDate: Int, endDate: Int): Flow<List<CategoryExpenseStats>> {
        return combine(
            transactionRepository.getCategoryTotalsInRange(startDate, endDate, TransactionType.EXPENSE),
            fieldRepository.getFieldsByModule("DAILY_EXPENSE")
        ) { totals, fields ->
            val fieldMap = fields.associateBy { it.id }
            val totalExpense = totals.sumOf { it.total }

            totals.map { total ->
                val field = total.fieldId?.let { fieldMap[it] }
                CategoryExpenseStats(
                    categoryId = total.fieldId,
                    categoryName = field?.name ?: "未分类",
                    categoryColor = field?.color ?: "#9E9E9E",
                    totalAmount = total.total,
                    percentage = if (totalExpense > 0) total.total / totalExpense * 100 else 0.0,
                    transactionCount = 0
                )
            }.sortedByDescending { it.totalAmount }
        }
    }

    /**
     * 获取日历视图的每日支出数据
     */
    fun getCalendarExpenseData(yearMonth: Int): Flow<Map<Int, Double>> {
        val year = yearMonth / 100
        val month = yearMonth % 100
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())

        return transactionRepository.getDailyExpenseTotals(
            startDate.toEpochDay().toInt(),
            endDate.toEpochDay().toInt()
        ).map { totals ->
            totals.associate { it.date to it.total }
        }
    }

    /**
     * 添加交易
     */
    suspend fun addTransaction(
        type: String,
        amount: Double,
        categoryId: Long?,
        date: Int,
        time: String = "",
        note: String = "",
        source: String = "MANUAL",
        accountId: Long? = null
    ): Long {
        val transaction = DailyTransactionEntity(
            type = type,
            amount = amount,
            categoryId = categoryId,
            accountId = accountId,
            date = date,
            time = time,
            note = note,
            source = source
        )
        return transactionRepository.insert(transaction)
    }

    /**
     * 添加交易（带重复检测）
     *
     * @return AddTransactionResult 包含是否成功、交易ID和潜在重复交易
     */
    suspend fun addTransactionWithDuplicateCheck(
        type: String,
        amount: Double,
        categoryId: Long?,
        date: Int,
        time: String = "",
        note: String = "",
        source: String = "MANUAL",
        accountId: Long? = null,
        skipDuplicateCheck: Boolean = false
    ): AddTransactionResult {
        // 如果跳过重复检测，直接添加
        if (skipDuplicateCheck) {
            val id = addTransaction(type, amount, categoryId, date, time, note, source, accountId)
            return AddTransactionResult(success = true, transactionId = id)
        }

        // 检查时间窗口内的重复（5分钟内相同金额相同类型）
        val recentDuplicates = transactionRepository.findDuplicatesInTimeWindow(
            date = date,
            type = type,
            amount = amount,
            timeWindowMinutes = 5
        )

        if (recentDuplicates.isNotEmpty()) {
            return AddTransactionResult(
                success = false,
                duplicateType = DuplicateType.RECENT,
                potentialDuplicates = recentDuplicates
            )
        }

        // 检查同一天的潜在重复
        val potentialDuplicates = transactionRepository.findPotentialDuplicates(
            date = date,
            type = type,
            amount = amount,
            categoryId = categoryId
        )

        if (potentialDuplicates.isNotEmpty()) {
            return AddTransactionResult(
                success = false,
                duplicateType = DuplicateType.SAME_DAY,
                potentialDuplicates = potentialDuplicates
            )
        }

        // 无重复，正常添加
        val id = addTransaction(type, amount, categoryId, date, time, note, source, accountId)
        return AddTransactionResult(success = true, transactionId = id)
    }

    /**
     * 强制添加交易（忽略重复检测）
     */
    suspend fun forceAddTransaction(
        type: String,
        amount: Double,
        categoryId: Long?,
        date: Int,
        time: String = "",
        note: String = "",
        source: String = "MANUAL",
        accountId: Long? = null
    ): Long {
        return addTransaction(type, amount, categoryId, date, time, note, source, accountId)
    }

    /**
     * 检查是否存在重复交易
     */
    suspend fun checkForDuplicates(
        type: String,
        amount: Double,
        categoryId: Long?,
        date: Int
    ): List<DailyTransactionEntity> {
        return transactionRepository.findPotentialDuplicates(date, type, amount, categoryId)
    }

    /**
     * 更新交易
     */
    suspend fun updateTransaction(
        id: Long,
        type: String,
        amount: Double,
        categoryId: Long?,
        date: Int,
        time: String = "",
        note: String = "",
        accountId: Long? = null
    ) {
        val existing = transactionRepository.getById(id) ?: return
        val updated = existing.copy(
            type = type,
            amount = amount,
            categoryId = categoryId,
            accountId = accountId,
            date = date,
            time = time,
            note = note
        )
        transactionRepository.update(updated)
    }

    /**
     * 删除交易
     */
    suspend fun deleteTransaction(id: Long) {
        transactionRepository.deleteById(id)
    }

    /**
     * 批量删除交易
     */
    suspend fun deleteTransactions(ids: List<Long>) {
        transactionRepository.deleteByIds(ids)
    }

    /**
     * 获取交易详情
     */
    suspend fun getTransactionById(id: Long): DailyTransactionWithCategory? {
        val transaction = transactionRepository.getById(id) ?: return null
        val fields = fieldRepository.getFieldsByModule("DAILY_EXPENSE").first()
        val category = transaction.categoryId?.let { catId ->
            fields.find { it.id == catId }
        }
        return DailyTransactionWithCategory(transaction, category)
    }

    /**
     * 搜索交易
     */
    fun searchTransactions(keyword: String): Flow<List<DailyTransactionWithCategory>> {
        return combine(
            transactionRepository.searchByNote(keyword),
            fieldRepository.getFieldsByModule("DAILY_EXPENSE")
        ) { transactions, fields ->
            val fieldMap = fields.associateBy { it.id }
            transactions.map { transaction ->
                DailyTransactionWithCategory(
                    transaction = transaction,
                    category = transaction.categoryId?.let { fieldMap[it] }
                )
            }
        }
    }

    /**
     * 格式化日期
     */
    private fun formatDate(date: LocalDate): String {
        val today = LocalDate.now()
        return when {
            date == today -> "今天"
            date == today.minusDays(1) -> "昨天"
            date == today.minusDays(2) -> "前天"
            date.year == today.year -> "${date.monthValue}月${date.dayOfMonth}日"
            else -> "${date.year}年${date.monthValue}月${date.dayOfMonth}日"
        }
    }
}
