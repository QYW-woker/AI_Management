package com.lifemanager.app.core.database.dao

import androidx.room.*
import com.lifemanager.app.core.database.entity.DiaryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 日记DAO接口
 */
@Dao
interface DiaryDao {

    // ==================== 基础查询 ====================

    /**
     * 获取指定日期范围的日记（列表视图）
     */
    @Query("""
        SELECT * FROM diaries
        WHERE date BETWEEN :startDate AND :endDate AND isDeleted = 0
        ORDER BY date DESC
    """)
    fun getByDateRange(startDate: Int, endDate: Int): Flow<List<DiaryEntity>>

    /**
     * 获取指定日期的日记
     */
    @Query("SELECT * FROM diaries WHERE date = :date AND isDeleted = 0")
    suspend fun getByDate(date: Int): DiaryEntity?

    /**
     * 获取指定日期的日记（Flow版本）
     */
    @Query("SELECT * FROM diaries WHERE date = :date AND isDeleted = 0")
    fun getByDateFlow(date: Int): Flow<DiaryEntity?>

    /**
     * 根据ID获取日记
     */
    @Query("SELECT * FROM diaries WHERE id = :id")
    suspend fun getById(id: Long): DiaryEntity?

    /**
     * 根据ID获取日记（Flow版本）
     */
    @Query("SELECT * FROM diaries WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<DiaryEntity?>

    /**
     * 获取最近的日记
     */
    @Query("""
        SELECT * FROM diaries
        WHERE isDeleted = 0
        ORDER BY date DESC
        LIMIT :limit
    """)
    fun getRecentDiaries(limit: Int = 30): Flow<List<DiaryEntity>>

    /**
     * 获取所有日记（分页）
     */
    @Query("""
        SELECT * FROM diaries
        WHERE isDeleted = 0
        ORDER BY date DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getAllPaged(limit: Int, offset: Int): List<DiaryEntity>

    // ==================== 日历视图查询 ====================

    /**
     * 获取有日记的日期列表（用于日历标记）
     */
    @Query("""
        SELECT date FROM diaries
        WHERE date BETWEEN :startDate AND :endDate AND isDeleted = 0
    """)
    fun getDiaryDates(startDate: Int, endDate: Int): Flow<List<Int>>

    /**
     * 获取日历月份的日记摘要
     */
    @Query("""
        SELECT date, moodScore, SUBSTR(content, 1, 50) as preview,
               (locationName IS NOT NULL) as hasLocation,
               (attachments != '[]') as hasAttachments
        FROM diaries
        WHERE date BETWEEN :startDate AND :endDate AND isDeleted = 0
        ORDER BY date ASC
    """)
    suspend fun getMonthDiarySummary(startDate: Int, endDate: Int): List<DiaryCalendarItem>

    /**
     * 获取指定年份有日记的月份
     */
    @Query("""
        SELECT DISTINCT (date / 31 + 1) as month
        FROM diaries
        WHERE date >= :yearStart AND date < :yearEnd AND isDeleted = 0
        ORDER BY month
    """)
    suspend fun getDiaryMonths(yearStart: Int, yearEnd: Int): List<Int>

    // ==================== 搜索和筛选 ====================

    /**
     * 搜索日记内容
     */
    @Query("""
        SELECT * FROM diaries
        WHERE (content LIKE '%' || :keyword || '%' OR title LIKE '%' || :keyword || '%')
        AND isDeleted = 0
        ORDER BY date DESC
    """)
    fun searchByContent(keyword: String): Flow<List<DiaryEntity>>

    /**
     * 高级搜索
     */
    @Query("""
        SELECT * FROM diaries
        WHERE isDeleted = 0
        AND (:keyword IS NULL OR content LIKE '%' || :keyword || '%' OR title LIKE '%' || :keyword || '%')
        AND (:moodScore IS NULL OR moodScore = :moodScore)
        AND (:weather IS NULL OR weather = :weather)
        AND (:hasLocation IS NULL OR ((:hasLocation = 1 AND locationName IS NOT NULL) OR (:hasLocation = 0 AND locationName IS NULL)))
        AND (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        AND (:isFavorite IS NULL OR isFavorite = :isFavorite)
        ORDER BY date DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun advancedSearch(
        keyword: String? = null,
        moodScore: Int? = null,
        weather: String? = null,
        hasLocation: Boolean? = null,
        startDate: Int? = null,
        endDate: Int? = null,
        isFavorite: Boolean? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<DiaryEntity>

    /**
     * 根据情绪评分筛选
     */
    @Query("""
        SELECT * FROM diaries
        WHERE moodScore = :moodScore AND isDeleted = 0
        ORDER BY date DESC
    """)
    fun getByMoodScore(moodScore: Int): Flow<List<DiaryEntity>>

    /**
     * 获取收藏的日记
     */
    @Query("""
        SELECT * FROM diaries
        WHERE isFavorite = 1 AND isDeleted = 0
        ORDER BY date DESC
    """)
    fun getFavoriteDiaries(): Flow<List<DiaryEntity>>

    /**
     * 获取有位置的日记
     */
    @Query("""
        SELECT * FROM diaries
        WHERE locationName IS NOT NULL AND isDeleted = 0
        ORDER BY date DESC
    """)
    fun getDiariesWithLocation(): Flow<List<DiaryEntity>>

    /**
     * 根据位置搜索
     */
    @Query("""
        SELECT * FROM diaries
        WHERE (locationName LIKE '%' || :keyword || '%'
               OR locationAddress LIKE '%' || :keyword || '%'
               OR poiName LIKE '%' || :keyword || '%')
        AND isDeleted = 0
        ORDER BY date DESC
    """)
    fun searchByLocation(keyword: String): Flow<List<DiaryEntity>>

    // ==================== 统计 ====================

    /**
     * 获取情绪统计
     */
    @Query("""
        SELECT moodScore, COUNT(*) as count
        FROM diaries
        WHERE date BETWEEN :startDate AND :endDate AND moodScore IS NOT NULL AND isDeleted = 0
        GROUP BY moodScore
    """)
    fun getMoodStats(startDate: Int, endDate: Int): Flow<List<MoodStat>>

    /**
     * 获取天气统计
     */
    @Query("""
        SELECT weather, COUNT(*) as count
        FROM diaries
        WHERE date BETWEEN :startDate AND :endDate AND weather IS NOT NULL AND isDeleted = 0
        GROUP BY weather
    """)
    suspend fun getWeatherStats(startDate: Int, endDate: Int): List<WeatherStat>

    /**
     * 获取写作统计
     */
    @Query("""
        SELECT
            COUNT(*) as totalDiaries,
            SUM(wordCount) as totalWords,
            AVG(wordCount) as avgWords,
            SUM(CASE WHEN moodScore >= 4 THEN 1 ELSE 0 END) as happyDays,
            SUM(CASE WHEN locationName IS NOT NULL THEN 1 ELSE 0 END) as locationDiaries
        FROM diaries
        WHERE date BETWEEN :startDate AND :endDate AND isDeleted = 0
    """)
    suspend fun getWritingStats(startDate: Int, endDate: Int): WritingStats

    /**
     * 统计日记总数
     */
    @Query("SELECT COUNT(*) FROM diaries WHERE isDeleted = 0")
    suspend fun countAll(): Int

    /**
     * 获取连续写日记天数
     */
    @Query("""
        WITH RECURSIVE streak AS (
            SELECT date, 1 as streak_count
            FROM diaries
            WHERE date = :today AND isDeleted = 0
            UNION ALL
            SELECT d.date, s.streak_count + 1
            FROM diaries d
            INNER JOIN streak s ON d.date = s.date - 1
            WHERE d.isDeleted = 0
        )
        SELECT COALESCE(MAX(streak_count), 0) FROM streak
    """)
    suspend fun getStreak(today: Int): Int

    /**
     * 获取最长连续天数
     */
    @Query("""
        SELECT COUNT(*) FROM diaries WHERE isDeleted = 0
    """)
    suspend fun getLongestStreak(): Int

    // ==================== 增删改 ====================

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
     * 软删除日记
     */
    @Query("UPDATE diaries SET isDeleted = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long = System.currentTimeMillis())

    /**
     * 恢复日记
     */
    @Query("UPDATE diaries SET isDeleted = 0, updatedAt = :now WHERE id = :id")
    suspend fun restore(id: Long, now: Long = System.currentTimeMillis())

    /**
     * 永久删除日记
     */
    @Query("DELETE FROM diaries WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 清空回收站
     */
    @Query("DELETE FROM diaries WHERE isDeleted = 1")
    suspend fun emptyTrash()

    /**
     * 获取回收站日记
     */
    @Query("""
        SELECT * FROM diaries
        WHERE isDeleted = 1
        ORDER BY updatedAt DESC
    """)
    fun getDeletedDiaries(): Flow<List<DiaryEntity>>

    /**
     * 收藏/取消收藏
     */
    @Query("UPDATE diaries SET isFavorite = :isFavorite, updatedAt = :now WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean, now: Long = System.currentTimeMillis())

    /**
     * 更新位置信息
     */
    @Query("""
        UPDATE diaries
        SET locationName = :locationName,
            locationAddress = :locationAddress,
            latitude = :latitude,
            longitude = :longitude,
            poiName = :poiName,
            updatedAt = :now
        WHERE id = :id
    """)
    suspend fun updateLocation(
        id: Long,
        locationName: String?,
        locationAddress: String?,
        latitude: Double?,
        longitude: Double?,
        poiName: String?,
        now: Long = System.currentTimeMillis()
    )

    /**
     * 更新心情
     */
    @Query("UPDATE diaries SET moodScore = :moodScore, updatedAt = :now WHERE id = :id")
    suspend fun updateMood(id: Long, moodScore: Int, now: Long = System.currentTimeMillis())

    /**
     * 更新天气
     */
    @Query("UPDATE diaries SET weather = :weather, updatedAt = :now WHERE id = :id")
    suspend fun updateWeather(id: Long, weather: String, now: Long = System.currentTimeMillis())
}

// ==================== 数据类 ====================

/**
 * 情绪统计数据类
 */
data class MoodStat(
    val moodScore: Int?,
    val count: Int
)

/**
 * 天气统计数据类
 */
data class WeatherStat(
    val weather: String?,
    val count: Int
)

/**
 * 写作统计数据类
 */
data class WritingStats(
    val totalDiaries: Int,
    val totalWords: Int?,
    val avgWords: Double?,
    val happyDays: Int,
    val locationDiaries: Int
)

/**
 * 日历项数据类
 */
data class DiaryCalendarItem(
    val date: Int,
    val moodScore: Int?,
    val preview: String?,
    val hasLocation: Boolean,
    val hasAttachments: Boolean
)
