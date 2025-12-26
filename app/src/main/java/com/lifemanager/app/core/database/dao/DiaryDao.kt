package com.lifemanager.app.core.database.dao

import androidx.room.*
import com.lifemanager.app.core.database.entity.DiaryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 日记DAO接口
 */
@Dao
interface DiaryDao {

    /**
     * 获取指定日期范围的日记
     */
    @Query("""
        SELECT * FROM diaries
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date DESC
    """)
    fun getByDateRange(startDate: Int, endDate: Int): Flow<List<DiaryEntity>>

    /**
     * 获取指定日期的日记
     */
    @Query("SELECT * FROM diaries WHERE date = :date")
    suspend fun getByDate(date: Int): DiaryEntity?

    /**
     * 获取指定日期的日记（Flow版本）
     */
    @Query("SELECT * FROM diaries WHERE date = :date")
    fun getByDateFlow(date: Int): Flow<DiaryEntity?>

    /**
     * 根据ID获取日记
     */
    @Query("SELECT * FROM diaries WHERE id = :id")
    suspend fun getById(id: Long): DiaryEntity?

    /**
     * 获取最近的日记
     */
    @Query("""
        SELECT * FROM diaries
        ORDER BY date DESC
        LIMIT :limit
    """)
    fun getRecentDiaries(limit: Int = 30): Flow<List<DiaryEntity>>

    /**
     * 搜索日记内容
     */
    @Query("""
        SELECT * FROM diaries
        WHERE content LIKE '%' || :keyword || '%'
        ORDER BY date DESC
    """)
    fun searchByContent(keyword: String): Flow<List<DiaryEntity>>

    /**
     * 根据情绪评分筛选
     */
    @Query("""
        SELECT * FROM diaries
        WHERE moodScore = :moodScore
        ORDER BY date DESC
    """)
    fun getByMoodScore(moodScore: Int): Flow<List<DiaryEntity>>

    /**
     * 获取有日记的日期列表（用于日历标记）
     */
    @Query("""
        SELECT date FROM diaries
        WHERE date BETWEEN :startDate AND :endDate
    """)
    fun getDiaryDates(startDate: Int, endDate: Int): Flow<List<Int>>

    /**
     * 获取情绪统计
     */
    @Query("""
        SELECT moodScore, COUNT(*) as count
        FROM diaries
        WHERE date BETWEEN :startDate AND :endDate AND moodScore IS NOT NULL
        GROUP BY moodScore
    """)
    fun getMoodStats(startDate: Int, endDate: Int): Flow<List<MoodStat>>

    /**
     * 插入或更新日记（每天唯一）
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diary: DiaryEntity): Long

    /**
     * 更新日记
     */
    @Update
    suspend fun update(diary: DiaryEntity)

    /**
     * 删除日记
     */
    @Query("DELETE FROM diaries WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 统计日记总数
     */
    @Query("SELECT COUNT(*) FROM diaries")
    suspend fun countAll(): Int

    /**
     * 获取连续写日记天数
     */
    @Query("""
        SELECT COUNT(*) FROM (
            SELECT date FROM diaries
            WHERE date <= :today
            ORDER BY date DESC
        )
    """)
    suspend fun getStreak(today: Int): Int
}

/**
 * 情绪统计数据类
 */
data class MoodStat(
    val moodScore: Int?,
    val count: Int
)
