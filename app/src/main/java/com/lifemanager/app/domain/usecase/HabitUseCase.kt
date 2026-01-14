package com.lifemanager.app.domain.usecase

import com.lifemanager.app.core.database.entity.HabitEntity
import com.lifemanager.app.core.database.entity.HabitRecordEntity
import com.lifemanager.app.core.database.entity.HabitStatus
import com.lifemanager.app.domain.model.AchievementType
import com.lifemanager.app.domain.model.DailyHabitData
import com.lifemanager.app.domain.model.HabitAchievement
import com.lifemanager.app.domain.model.HabitCalendarData
import com.lifemanager.app.domain.model.HabitDetailData
import com.lifemanager.app.domain.model.HabitRankItem
import com.lifemanager.app.domain.model.HabitStats
import com.lifemanager.app.domain.model.HabitWithStatus
import com.lifemanager.app.domain.model.MonthlyHabitStats
import com.lifemanager.app.domain.model.WeeklyHabitStats
import com.lifemanager.app.domain.model.getAchievementDefinitions
import com.lifemanager.app.domain.repository.HabitRepository
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 习惯打卡业务逻辑用例
 */
@Singleton
class HabitUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    /**
     * 获取今日日期（epochDay格式）
     */
    fun getToday(): Int {
        return LocalDate.now().toEpochDay().toInt()
    }

    /**
     * 获取当前周的开始日期
     */
    private fun getWeekStart(): Int {
        return LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .toEpochDay().toInt()
    }

    /**
     * 获取当前月的开始日期
     */
    private fun getMonthStart(): Int {
        return LocalDate.now()
            .withDayOfMonth(1)
            .toEpochDay().toInt()
    }

    /**
     * 获取所有活跃习惯及其状态
     */
    fun getHabitsWithStatus(): Flow<List<HabitWithStatus>> {
        val today = getToday()
        return combine(
            habitRepository.getActiveHabits(),
            habitRepository.getTodayStatus(today)
        ) { habits, todayStatus ->
            val statusMap = todayStatus.associate { it.habitId to it }
            habits.map { habit ->
                val status = statusMap[habit.id]
                HabitWithStatus(
                    habit = habit,
                    isCheckedToday = status?.isCompleted == true,
                    todayValue = status?.value,
                    streak = calculateStreak(habit.id, today),
                    totalCheckins = habitRepository.getTotalCheckins(habit.id, today)
                )
            }
        }
    }

    /**
     * 计算连续打卡天数
     */
    private suspend fun calculateStreak(habitId: Long, today: Int): Int {
        var streak = 0
        var checkDate = today

        while (true) {
            val isChecked = habitRepository.isCheckedIn(habitId, checkDate)
            if (isChecked) {
                streak++
                checkDate--
            } else {
                break
            }
        }
        return streak
    }

    /**
     * 获取习惯统计数据
     */
    suspend fun getHabitStats(): HabitStats {
        val today = getToday()
        val activeHabits = habitRepository.countActiveHabits()
        val todayCheckins = habitRepository.countTodayCheckins(today)

        val completionRate = if (activeHabits > 0) {
            todayCheckins.toFloat() / activeHabits
        } else 0f

        return HabitStats(
            totalHabits = activeHabits,
            activeHabits = activeHabits,
            todayCompleted = todayCheckins,
            todayTotal = activeHabits,
            todayCompletionRate = completionRate * 100
        )
    }

    /**
     * 打卡/取消打卡
     */
    suspend fun toggleCheckIn(habitId: Long, value: Double? = null) {
        val today = getToday()
        val existing = habitRepository.getRecordByHabitAndDate(habitId, today)

        if (existing != null) {
            // 已打卡，取消
            habitRepository.deleteRecord(habitId, today)
        } else {
            // 未打卡，打卡
            val record = HabitRecordEntity(
                habitId = habitId,
                date = today,
                isCompleted = true,
                value = value
            )
            habitRepository.saveRecord(record)
        }
    }

    /**
     * 更新数值型习惯的值
     */
    suspend fun updateNumericValue(habitId: Long, value: Double) {
        val today = getToday()
        val existing = habitRepository.getRecordByHabitAndDate(habitId, today)

        val record = existing?.copy(
            value = value,
            isCompleted = true
        ) ?: HabitRecordEntity(
            habitId = habitId,
            date = today,
            isCompleted = true,
            value = value
        )
        habitRepository.saveRecord(record)
    }

    /**
     * 保存习惯
     */
    suspend fun saveHabit(habit: HabitEntity): Long {
        return habitRepository.saveHabit(habit)
    }

    /**
     * 更新习惯
     */
    suspend fun updateHabit(habit: HabitEntity) {
        habitRepository.updateHabit(habit)
    }

    /**
     * 暂停习惯
     */
    suspend fun pauseHabit(habitId: Long) {
        habitRepository.updateHabitStatus(habitId, HabitStatus.PAUSED)
    }

    /**
     * 恢复习惯
     */
    suspend fun resumeHabit(habitId: Long) {
        habitRepository.updateHabitStatus(habitId, HabitStatus.ACTIVE)
    }

    /**
     * 归档习惯
     */
    suspend fun archiveHabit(habitId: Long) {
        habitRepository.updateHabitStatus(habitId, HabitStatus.ARCHIVED)
    }

    /**
     * 删除习惯
     */
    suspend fun deleteHabit(habitId: Long) {
        habitRepository.deleteHabit(habitId)
    }

    /**
     * 根据ID获取习惯
     */
    suspend fun getHabitById(habitId: Long): HabitEntity? {
        return habitRepository.getHabitById(habitId)
    }

    /**
     * 获取习惯的打卡日历数据
     */
    fun getHabitCalendarData(habitId: Long, month: LocalDate): Flow<List<Int>> {
        val start = month.withDayOfMonth(1).toEpochDay().toInt()
        val end = month.withDayOfMonth(month.lengthOfMonth()).toEpochDay().toInt()
        return habitRepository.getCheckedDates(habitId, start, end)
    }

    /**
     * 获取所有习惯（包括暂停的）
     */
    fun getAllHabits(): Flow<List<HabitEntity>> {
        return habitRepository.getAllHabits()
    }

    /**
     * 获取归档的习惯
     */
    fun getArchivedHabits(): Flow<List<HabitEntity>> {
        return habitRepository.getArchivedHabits()
    }

    // ============ 新增打卡增强功能 ============

    /**
     * 获取日历打卡数据
     */
    suspend fun getCalendarData(yearMonth: Int): List<HabitCalendarData> {
        val year = yearMonth / 100
        val month = yearMonth % 100
        val ym = YearMonth.of(year, month)
        val startDate = ym.atDay(1).toEpochDay().toInt()
        val endDate = ym.atEndOfMonth().toEpochDay().toInt()
        val daysInMonth = ym.lengthOfMonth()

        val habits = habitRepository.getActiveHabits().first()

        return habits.map { habit ->
            val checkedDates = habitRepository.getCheckedDates(habit.id, startDate, endDate).first().toSet()
            val completedDays = checkedDates.size

            HabitCalendarData(
                habitId = habit.id,
                habitName = habit.name,
                habitColor = habit.color,
                month = yearMonth,
                checkedDays = checkedDates,
                totalDaysInMonth = daysInMonth,
                completedDays = completedDays,
                completionRate = if (daysInMonth > 0) completedDays.toFloat() / daysInMonth else 0f
            )
        }
    }

    /**
     * 获取成就列表
     */
    suspend fun getAchievements(): List<HabitAchievement> {
        val today = getToday()
        val habits = habitRepository.getActiveHabits().first()
        val definitions = getAchievementDefinitions().toMutableList()

        // 计算各项数据
        var maxStreak = 0
        var totalCheckins = 0

        for (habit in habits) {
            val streak = calculateStreak(habit.id, today)
            if (streak > maxStreak) maxStreak = streak
            totalCheckins += habitRepository.getTotalCheckins(habit.id, today)
        }

        // 更新成就状态
        return definitions.map { achievement ->
            val (progress, isUnlocked) = when (achievement.id) {
                AchievementType.FIRST_CHECKIN -> totalCheckins to (totalCheckins >= 1)
                AchievementType.STREAK_7 -> maxStreak to (maxStreak >= 7)
                AchievementType.STREAK_21 -> maxStreak to (maxStreak >= 21)
                AchievementType.STREAK_30 -> maxStreak to (maxStreak >= 30)
                AchievementType.STREAK_100 -> maxStreak to (maxStreak >= 100)
                AchievementType.TOTAL_100 -> totalCheckins to (totalCheckins >= 100)
                else -> 0 to false
            }
            achievement.copy(
                progress = minOf(progress, achievement.targetProgress),
                isUnlocked = isUnlocked,
                unlockedAt = if (isUnlocked) System.currentTimeMillis() else null
            )
        }
    }

    /**
     * 获取本周统计
     */
    suspend fun getWeeklyStats(): WeeklyHabitStats {
        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(6)

        val habits = habitRepository.getActiveHabits().first()
        val habitCount = habits.size

        val dailyData = mutableListOf<DailyHabitData>()
        var totalCheckins = 0
        var possibleCheckins = 0

        for (dayOffset in 0..6) {
            val currentDate = weekStart.plusDays(dayOffset.toLong())
            val epochDay = currentDate.toEpochDay().toInt()

            if (epochDay <= getToday()) {
                var completed = 0
                for (habit in habits) {
                    if (habitRepository.isCheckedIn(habit.id, epochDay)) {
                        completed++
                        totalCheckins++
                    }
                }
                possibleCheckins += habitCount

                dailyData.add(
                    DailyHabitData(
                        date = epochDay,
                        dayLabel = getDayLabel(currentDate.dayOfWeek),
                        completed = completed,
                        total = habitCount,
                        isToday = epochDay == getToday()
                    )
                )
            }
        }

        val completionRate = if (possibleCheckins > 0) {
            totalCheckins.toFloat() / possibleCheckins
        } else 0f

        return WeeklyHabitStats(
            weekNumber = today.get(java.time.temporal.WeekFields.of(java.util.Locale.getDefault()).weekOfYear()),
            weekLabel = "${weekStart.monthValue}/${weekStart.dayOfMonth} - ${weekEnd.monthValue}/${weekEnd.dayOfMonth}",
            totalCheckins = totalCheckins,
            possibleCheckins = possibleCheckins,
            completionRate = completionRate,
            isCurrentWeek = true,
            dailyData = dailyData
        )
    }

    /**
     * 获取月度统计
     */
    suspend fun getMonthlyStats(yearMonth: Int): MonthlyHabitStats {
        val year = yearMonth / 100
        val month = yearMonth % 100
        val ym = YearMonth.of(year, month)
        val daysInMonth = ym.lengthOfMonth()
        val startDate = ym.atDay(1).toEpochDay().toInt()
        val endDate = ym.atEndOfMonth().toEpochDay().toInt()
        val today = getToday()

        val habits = habitRepository.getActiveHabits().first()
        val habitCount = habits.size

        var totalCheckins = 0
        var perfectDays = 0
        var bestStreak = 0
        var currentStreak = 0
        val habitCheckinCounts = mutableMapOf<String, Int>()

        for (day in startDate..minOf(endDate, today)) {
            var dayCompleted = 0
            for (habit in habits) {
                if (habitRepository.isCheckedIn(habit.id, day)) {
                    dayCompleted++
                    totalCheckins++
                    habitCheckinCounts[habit.name] = (habitCheckinCounts[habit.name] ?: 0) + 1
                }
            }

            if (dayCompleted == habitCount && habitCount > 0) {
                perfectDays++
                currentStreak++
                if (currentStreak > bestStreak) bestStreak = currentStreak
            } else {
                currentStreak = 0
            }
        }

        val possibleDays = minOf(daysInMonth, today - startDate + 1)
        val possibleCheckins = possibleDays * habitCount
        val completionRate = if (possibleCheckins > 0) {
            totalCheckins.toFloat() / possibleCheckins
        } else 0f

        val mostActiveHabit = habitCheckinCounts.maxByOrNull { it.value }?.key

        return MonthlyHabitStats(
            yearMonth = yearMonth,
            monthLabel = "${month}月",
            totalCheckins = totalCheckins,
            possibleCheckins = possibleCheckins,
            completionRate = completionRate,
            perfectDays = perfectDays,
            bestStreak = bestStreak,
            mostActiveHabit = mostActiveHabit
        )
    }

    /**
     * 获取习惯排行榜
     */
    suspend fun getHabitRanking(): List<HabitRankItem> {
        val today = getToday()
        val habits = habitRepository.getActiveHabits().first()

        val rankingData = habits.map { habit ->
            val streak = calculateStreak(habit.id, today)
            val totalCheckins = habitRepository.getTotalCheckins(habit.id, today)

            Triple(habit, streak, totalCheckins)
        }.sortedByDescending { it.second }

        return rankingData.mapIndexed { index, (habit, streak, total) ->
            HabitRankItem(
                habitId = habit.id,
                habitName = habit.name,
                habitColor = habit.color,
                rank = index + 1,
                streak = streak,
                totalCheckins = total,
                completionRate = 0f // 简化处理
            )
        }
    }

    /**
     * 补打卡
     */
    suspend fun retroCheckin(habitId: Long, date: Int, note: String = "") {
        // 不允许补打今天或未来的日期
        if (date >= getToday()) return

        val existing = habitRepository.getRecordByHabitAndDate(habitId, date)
        if (existing == null) {
            val record = HabitRecordEntity(
                habitId = habitId,
                date = date,
                isCompleted = true,
                note = note
            )
            habitRepository.saveRecord(record)
        }
    }

    /**
     * 获取习惯详情
     */
    suspend fun getHabitDetail(habitId: Long): HabitDetailData? {
        val habit = habitRepository.getHabitById(habitId) ?: return null
        val today = getToday()
        val currentStreak = calculateStreak(habitId, today)
        val totalCheckins = habitRepository.getTotalCheckins(habitId, today)

        // 计算最长连续（简化：使用当前连续）
        val longestStreak = maxOf(currentStreak, totalCheckins / 3)

        // 获取最近30天的打卡记录
        val thirtyDaysAgo = today - 30
        val recentCheckins = habitRepository.getCheckedDates(habitId, thirtyDaysAgo, today).first()

        // 本周完成率
        val weekStart = getWeekStart()
        var weekCheckins = 0
        var weekDays = 0
        for (day in weekStart..today) {
            weekDays++
            if (habitRepository.isCheckedIn(habitId, day)) weekCheckins++
        }
        val weekRate = if (weekDays > 0) weekCheckins.toFloat() / weekDays else 0f

        // 本月完成率
        val monthStart = getMonthStart()
        var monthCheckins = 0
        var monthDays = 0
        for (day in monthStart..today) {
            monthDays++
            if (habitRepository.isCheckedIn(habitId, day)) monthCheckins++
        }
        val monthRate = if (monthDays > 0) monthCheckins.toFloat() / monthDays else 0f

        // 成就（简化）
        val achievements = getAchievementDefinitions().map { achievement ->
            val progress = when (achievement.id) {
                AchievementType.STREAK_7, AchievementType.STREAK_21,
                AchievementType.STREAK_30, AchievementType.STREAK_100 -> currentStreak
                AchievementType.TOTAL_100 -> totalCheckins
                else -> if (totalCheckins > 0) 1 else 0
            }
            val isUnlocked = progress >= achievement.targetProgress
            achievement.copy(
                progress = minOf(progress, achievement.targetProgress),
                isUnlocked = isUnlocked
            )
        }

        return HabitDetailData(
            habit = habit,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalCheckins = totalCheckins,
            firstCheckinDate = if (recentCheckins.isNotEmpty()) recentCheckins.minOrNull() else null,
            completionRateThisMonth = monthRate,
            completionRateThisWeek = weekRate,
            recentCheckins = recentCheckins,
            achievements = achievements
        )
    }

    /**
     * 获取星期标签
     */
    private fun getDayLabel(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "周一"
            DayOfWeek.TUESDAY -> "周二"
            DayOfWeek.WEDNESDAY -> "周三"
            DayOfWeek.THURSDAY -> "周四"
            DayOfWeek.FRIDAY -> "周五"
            DayOfWeek.SATURDAY -> "周六"
            DayOfWeek.SUNDAY -> "周日"
        }
    }

    /**
     * 获取最长连续打卡记录
     */
    suspend fun getLongestStreak(): Int {
        val today = getToday()
        val habits = habitRepository.getActiveHabits().first()
        var maxStreak = 0

        for (habit in habits) {
            val streak = calculateStreak(habit.id, today)
            if (streak > maxStreak) maxStreak = streak
        }

        return maxStreak
    }
}
