package com.lifemanager.app.domain.usecase

import com.lifemanager.app.core.database.entity.HabitEntity
import com.lifemanager.app.core.database.entity.HabitRecordEntity
import com.lifemanager.app.core.database.entity.HabitStatus
import com.lifemanager.app.domain.model.HabitStats
import com.lifemanager.app.domain.model.HabitWithStatus
import com.lifemanager.app.domain.repository.HabitRepository
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
}
