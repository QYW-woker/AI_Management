package com.lifemanager.app.domain.repository

import com.lifemanager.app.core.database.entity.HabitEntity
import com.lifemanager.app.core.database.entity.HabitRecordEntity
import com.lifemanager.app.core.database.dao.HabitCheckStatus
import kotlinx.coroutines.flow.Flow

/**
 * 习惯打卡仓库接口
 */
interface HabitRepository {

    /**
     * 获取所有活跃习惯
     */
    fun getActiveHabits(): Flow<List<HabitEntity>>

    /**
     * 获取所有习惯（包括暂停的）
     */
    fun getAllHabits(): Flow<List<HabitEntity>>

    /**
     * 获取归档的习惯
     */
    fun getArchivedHabits(): Flow<List<HabitEntity>>

    /**
     * 根据ID获取习惯
     */
    suspend fun getHabitById(id: Long): HabitEntity?

    /**
     * 根据ID获取习惯（Flow版本）
     */
    fun getHabitByIdFlow(id: Long): Flow<HabitEntity?>

    /**
     * 保存习惯
     */
    suspend fun saveHabit(habit: HabitEntity): Long

    /**
     * 更新习惯
     */
    suspend fun updateHabit(habit: HabitEntity)

    /**
     * 更新习惯状态
     */
    suspend fun updateHabitStatus(id: Long, status: String)

    /**
     * 删除习惯
     */
    suspend fun deleteHabit(id: Long)

    /**
     * 获取今日各习惯的打卡状态
     */
    fun getTodayStatus(today: Int): Flow<List<HabitCheckStatus>>

    /**
     * 获取指定日期的所有打卡记录
     */
    fun getRecordsByDate(date: Int): Flow<List<HabitRecordEntity>>

    /**
     * 获取指定习惯指定日期的打卡记录
     */
    suspend fun getRecordByHabitAndDate(habitId: Long, date: Int): HabitRecordEntity?

    /**
     * 检查指定习惯指定日期是否已打卡
     */
    suspend fun isCheckedIn(habitId: Long, date: Int): Boolean

    /**
     * 获取习惯的总打卡次数
     */
    suspend fun getTotalCheckins(habitId: Long, today: Int): Int

    /**
     * 获取习惯在日期范围内的完成次数
     */
    suspend fun getCompletedCount(habitId: Long, startDate: Int, endDate: Int): Int

    /**
     * 保存打卡记录
     */
    suspend fun saveRecord(record: HabitRecordEntity): Long

    /**
     * 删除指定习惯指定日期的打卡记录
     */
    suspend fun deleteRecord(habitId: Long, date: Int)

    /**
     * 获取打卡日历数据
     */
    fun getCheckedDates(habitId: Long, startDate: Int, endDate: Int): Flow<List<Int>>

    /**
     * 获取指定习惯指定日期范围的打卡记录
     */
    fun getRecordsByHabitAndDateRange(habitId: Long, startDate: Int, endDate: Int): Flow<List<HabitRecordEntity>>

    /**
     * 统计今日已打卡习惯数
     */
    suspend fun countTodayCheckins(today: Int): Int

    /**
     * 统计活跃习惯数量
     */
    suspend fun countActiveHabits(): Int
}
