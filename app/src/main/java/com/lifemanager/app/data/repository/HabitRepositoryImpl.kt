package com.lifemanager.app.data.repository

import com.lifemanager.app.core.database.dao.HabitDao
import com.lifemanager.app.core.database.dao.HabitRecordDao
import com.lifemanager.app.core.database.dao.HabitCheckStatus
import com.lifemanager.app.core.database.entity.HabitEntity
import com.lifemanager.app.core.database.entity.HabitRecordEntity
import com.lifemanager.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 习惯打卡仓库实现类
 */
@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val habitRecordDao: HabitRecordDao
) : HabitRepository {

    override fun getActiveHabits(): Flow<List<HabitEntity>> {
        return habitDao.getActiveHabits()
    }

    override fun getAllHabits(): Flow<List<HabitEntity>> {
        return habitDao.getAllHabits()
    }

    override fun getArchivedHabits(): Flow<List<HabitEntity>> {
        return habitDao.getArchivedHabits()
    }

    override suspend fun getHabitById(id: Long): HabitEntity? {
        return habitDao.getById(id)
    }

    override fun getHabitByIdFlow(id: Long): Flow<HabitEntity?> {
        return habitDao.getByIdFlow(id)
    }

    override suspend fun saveHabit(habit: HabitEntity): Long {
        return habitDao.insert(habit)
    }

    override suspend fun updateHabit(habit: HabitEntity) {
        habitDao.update(habit)
    }

    override suspend fun updateHabitStatus(id: Long, status: String) {
        habitDao.updateStatus(id, status)
    }

    override suspend fun deleteHabit(id: Long) {
        habitDao.deleteById(id)
    }

    override fun getTodayStatus(today: Int): Flow<List<HabitCheckStatus>> {
        return habitRecordDao.getTodayStatus(today)
    }

    override fun getRecordsByDate(date: Int): Flow<List<HabitRecordEntity>> {
        return habitRecordDao.getByDate(date)
    }

    override suspend fun getRecordByHabitAndDate(habitId: Long, date: Int): HabitRecordEntity? {
        return habitRecordDao.getByHabitAndDate(habitId, date)
    }

    override suspend fun isCheckedIn(habitId: Long, date: Int): Boolean {
        return habitRecordDao.isCheckedIn(habitId, date) > 0
    }

    override suspend fun getTotalCheckins(habitId: Long, today: Int): Int {
        return habitRecordDao.getTotalCheckins(habitId, today)
    }

    override suspend fun getCompletedCount(habitId: Long, startDate: Int, endDate: Int): Int {
        return habitRecordDao.getCompletedCount(habitId, startDate, endDate)
    }

    override suspend fun saveRecord(record: HabitRecordEntity): Long {
        return habitRecordDao.insert(record)
    }

    override suspend fun deleteRecord(habitId: Long, date: Int) {
        habitRecordDao.deleteByHabitAndDate(habitId, date)
    }

    override fun getCheckedDates(habitId: Long, startDate: Int, endDate: Int): Flow<List<Int>> {
        return habitRecordDao.getCheckedDates(habitId, startDate, endDate)
    }

    override fun getRecordsByHabitAndDateRange(
        habitId: Long,
        startDate: Int,
        endDate: Int
    ): Flow<List<HabitRecordEntity>> {
        return habitRecordDao.getByHabitAndDateRange(habitId, startDate, endDate)
    }

    override suspend fun countTodayCheckins(today: Int): Int {
        return habitRecordDao.countTodayCheckins(today)
    }

    override suspend fun countActiveHabits(): Int {
        return habitDao.countActive()
    }
}
