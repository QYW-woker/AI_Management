package com.lifemanager.app.feature.diary

import com.lifemanager.app.core.database.dao.*
import com.lifemanager.app.core.database.entity.DiaryEntity
import com.lifemanager.app.core.database.entity.DiaryLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 日记服务
 *
 * 提供日记的完整CRUD操作、位置管理、日历视图支持
 */
@Singleton
class DiaryService @Inject constructor(
    private val diaryDao: DiaryDao
) {

    // ==================== 创建日记 ====================

    /**
     * 创建新日记
     */
    suspend fun createDiary(
        content: String,
        title: String = "",
        moodScore: Int? = null,
        weather: String? = null,
        location: DiaryLocation? = null,
        attachments: List<String> = emptyList(),
        date: LocalDate = LocalDate.now()
    ): Long {
        val wordCount = calculateWordCount(content)
        val epochDay = date.toEpochDay().toInt()

        val diary = DiaryEntity(
            date = epochDay,
            title = title,
            content = content,
            moodScore = moodScore,
            weather = weather,
            locationName = location?.name,
            locationAddress = location?.address,
            latitude = location?.latitude,
            longitude = location?.longitude,
            poiName = location?.poiName,
            attachments = attachments.joinToString(",", "[", "]") { "\"$it\"" },
            wordCount = wordCount
        )

        return diaryDao.insert(diary)
    }

    /**
     * 快速记录（仅内容）
     */
    suspend fun quickRecord(content: String, date: LocalDate = LocalDate.now()): Long {
        return createDiary(content = content, date = date)
    }

    // ==================== 更新日记 ====================

    /**
     * 更新日记内容
     */
    suspend fun updateDiary(diary: DiaryEntity): Boolean {
        val wordCount = calculateWordCount(diary.content)
        diaryDao.update(
            diary.copy(
                wordCount = wordCount,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    /**
     * 更新日记内容
     */
    suspend fun updateContent(id: Long, content: String, title: String? = null): Boolean {
        val diary = diaryDao.getById(id) ?: return false
        val wordCount = calculateWordCount(content)

        diaryDao.update(
            diary.copy(
                content = content,
                title = title ?: diary.title,
                wordCount = wordCount,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    /**
     * 更新位置
     */
    suspend fun updateLocation(id: Long, location: DiaryLocation?) {
        diaryDao.updateLocation(
            id = id,
            locationName = location?.name,
            locationAddress = location?.address,
            latitude = location?.latitude,
            longitude = location?.longitude,
            poiName = location?.poiName
        )
    }

    /**
     * 更新心情
     */
    suspend fun updateMood(id: Long, moodScore: Int) {
        diaryDao.updateMood(id, moodScore)
    }

    /**
     * 更新天气
     */
    suspend fun updateWeather(id: Long, weather: String) {
        diaryDao.updateWeather(id, weather)
    }

    /**
     * 收藏/取消收藏
     */
    suspend fun toggleFavorite(id: Long): Boolean {
        val diary = diaryDao.getById(id) ?: return false
        diaryDao.setFavorite(id, !diary.isFavorite)
        return !diary.isFavorite
    }

    // ==================== 删除日记 ====================

    /**
     * 软删除日记
     */
    suspend fun deleteDiary(id: Long) {
        diaryDao.softDelete(id)
    }

    /**
     * 恢复日记
     */
    suspend fun restoreDiary(id: Long) {
        diaryDao.restore(id)
    }

    /**
     * 永久删除日记
     */
    suspend fun permanentlyDelete(id: Long) {
        diaryDao.deleteById(id)
    }

    /**
     * 清空回收站
     */
    suspend fun emptyTrash() {
        diaryDao.emptyTrash()
    }

    // ==================== 查询日记 ====================

    /**
     * 获取日记详情
     */
    suspend fun getDiary(id: Long): DiaryEntity? = diaryDao.getById(id)

    /**
     * 获取日记详情（Flow）
     */
    fun getDiaryFlow(id: Long): Flow<DiaryEntity?> = diaryDao.getByIdFlow(id)

    /**
     * 获取指定日期的日记
     */
    suspend fun getDiaryByDate(date: LocalDate): DiaryEntity? {
        return diaryDao.getByDate(date.toEpochDay().toInt())
    }

    /**
     * 获取指定日期的日记（Flow）
     */
    fun getDiaryByDateFlow(date: LocalDate): Flow<DiaryEntity?> {
        return diaryDao.getByDateFlow(date.toEpochDay().toInt())
    }

    /**
     * 获取最近日记
     */
    fun getRecentDiaries(limit: Int = 30): Flow<List<DiaryEntity>> {
        return diaryDao.getRecentDiaries(limit)
    }

    /**
     * 获取收藏日记
     */
    fun getFavoriteDiaries(): Flow<List<DiaryEntity>> {
        return diaryDao.getFavoriteDiaries()
    }

    /**
     * 获取回收站日记
     */
    fun getDeletedDiaries(): Flow<List<DiaryEntity>> {
        return diaryDao.getDeletedDiaries()
    }

    /**
     * 分页获取日记
     */
    suspend fun getDiariesPaged(page: Int, pageSize: Int = 20): List<DiaryEntity> {
        return diaryDao.getAllPaged(pageSize, page * pageSize)
    }

    // ==================== 日历视图 ====================

    /**
     * 获取月份日记日期（用于日历标记）
     */
    fun getMonthDiaryDates(yearMonth: YearMonth): Flow<List<Int>> {
        val startDate = yearMonth.atDay(1).toEpochDay().toInt()
        val endDate = yearMonth.atEndOfMonth().toEpochDay().toInt()
        return diaryDao.getDiaryDates(startDate, endDate)
    }

    /**
     * 获取月份日记摘要
     */
    suspend fun getMonthDiarySummary(yearMonth: YearMonth): List<DiaryCalendarItem> {
        val startDate = yearMonth.atDay(1).toEpochDay().toInt()
        val endDate = yearMonth.atEndOfMonth().toEpochDay().toInt()
        return diaryDao.getMonthDiarySummary(startDate, endDate)
    }

    /**
     * 获取日期范围内的日记
     */
    fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DiaryEntity>> {
        return diaryDao.getByDateRange(
            startDate.toEpochDay().toInt(),
            endDate.toEpochDay().toInt()
        )
    }

    /**
     * 构建日历数据
     */
    suspend fun buildCalendarData(yearMonth: YearMonth): DiaryCalendarData {
        val summaries = getMonthDiarySummary(yearMonth)
        val diaryMap = summaries.associateBy { it.date }

        val days = mutableListOf<DiaryCalendarDay>()
        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()

        // 添加上月填充
        val firstDayOfWeek = firstDay.dayOfWeek.value
        if (firstDayOfWeek > 1) {
            val prevMonth = yearMonth.minusMonths(1)
            val prevLastDay = prevMonth.atEndOfMonth()
            for (i in (firstDayOfWeek - 1) downTo 1) {
                val date = prevLastDay.minusDays(i.toLong() - 1)
                days.add(DiaryCalendarDay(
                    date = date.toEpochDay().toInt(),
                    isCurrentMonth = false,
                    hasDiary = false
                ))
            }
        }

        // 添加当月日期
        var currentDate = firstDay
        while (!currentDate.isAfter(lastDay)) {
            val epochDay = currentDate.toEpochDay().toInt()
            val summary = diaryMap[epochDay]
            days.add(DiaryCalendarDay(
                date = epochDay,
                isCurrentMonth = true,
                hasDiary = summary != null,
                moodScore = summary?.moodScore,
                preview = summary?.preview,
                hasLocation = summary?.hasLocation ?: false,
                hasAttachments = summary?.hasAttachments ?: false
            ))
            currentDate = currentDate.plusDays(1)
        }

        // 添加下月填充
        val lastDayOfWeek = lastDay.dayOfWeek.value
        if (lastDayOfWeek < 7) {
            val nextMonth = yearMonth.plusMonths(1)
            val nextFirstDay = nextMonth.atDay(1)
            for (i in 0 until (7 - lastDayOfWeek)) {
                val date = nextFirstDay.plusDays(i.toLong())
                days.add(DiaryCalendarDay(
                    date = date.toEpochDay().toInt(),
                    isCurrentMonth = false,
                    hasDiary = false
                ))
            }
        }

        return DiaryCalendarData(
            yearMonth = yearMonth,
            days = days,
            diaryCount = summaries.size
        )
    }

    // ==================== 搜索 ====================

    /**
     * 搜索日记
     */
    fun searchDiaries(keyword: String): Flow<List<DiaryEntity>> {
        return diaryDao.searchByContent(keyword)
    }

    /**
     * 高级搜索
     */
    suspend fun advancedSearch(filter: DiarySearchFilter): List<DiaryEntity> {
        return diaryDao.advancedSearch(
            keyword = filter.keyword,
            moodScore = filter.moodScore,
            weather = filter.weather,
            hasLocation = filter.hasLocation,
            startDate = filter.startDate?.toEpochDay()?.toInt(),
            endDate = filter.endDate?.toEpochDay()?.toInt(),
            isFavorite = filter.isFavorite,
            limit = filter.limit,
            offset = filter.offset
        )
    }

    /**
     * 按位置搜索
     */
    fun searchByLocation(keyword: String): Flow<List<DiaryEntity>> {
        return diaryDao.searchByLocation(keyword)
    }

    /**
     * 按心情筛选
     */
    fun filterByMood(moodScore: Int): Flow<List<DiaryEntity>> {
        return diaryDao.getByMoodScore(moodScore)
    }

    /**
     * 获取有位置的日记
     */
    fun getDiariesWithLocation(): Flow<List<DiaryEntity>> {
        return diaryDao.getDiariesWithLocation()
    }

    // ==================== 统计 ====================

    /**
     * 获取写作统计
     */
    suspend fun getWritingStats(yearMonth: YearMonth): WritingStats {
        val startDate = yearMonth.atDay(1).toEpochDay().toInt()
        val endDate = yearMonth.atEndOfMonth().toEpochDay().toInt()
        return diaryDao.getWritingStats(startDate, endDate)
    }

    /**
     * 获取心情统计
     */
    fun getMoodStats(yearMonth: YearMonth): Flow<List<MoodStat>> {
        val startDate = yearMonth.atDay(1).toEpochDay().toInt()
        val endDate = yearMonth.atEndOfMonth().toEpochDay().toInt()
        return diaryDao.getMoodStats(startDate, endDate)
    }

    /**
     * 获取天气统计
     */
    suspend fun getWeatherStats(yearMonth: YearMonth): List<WeatherStat> {
        val startDate = yearMonth.atDay(1).toEpochDay().toInt()
        val endDate = yearMonth.atEndOfMonth().toEpochDay().toInt()
        return diaryDao.getWeatherStats(startDate, endDate)
    }

    /**
     * 获取日记总数
     */
    suspend fun getTotalCount(): Int = diaryDao.countAll()

    /**
     * 获取连续写日记天数
     */
    suspend fun getCurrentStreak(): Int {
        val today = LocalDate.now().toEpochDay().toInt()
        return diaryDao.getStreak(today)
    }

    /**
     * 获取综合统计
     */
    suspend fun getOverallStats(): DiaryOverallStats {
        val today = LocalDate.now()
        val thisMonth = YearMonth.from(today)
        val thisYear = today.year

        val yearStart = LocalDate.of(thisYear, 1, 1).toEpochDay().toInt()
        val yearEnd = LocalDate.of(thisYear, 12, 31).toEpochDay().toInt()

        val totalCount = diaryDao.countAll()
        val currentStreak = getCurrentStreak()
        val yearlyStats = diaryDao.getWritingStats(yearStart, yearEnd)
        val monthlyStats = getWritingStats(thisMonth)

        return DiaryOverallStats(
            totalDiaries = totalCount,
            currentStreak = currentStreak,
            longestStreak = diaryDao.getLongestStreak(),
            totalWords = yearlyStats.totalWords ?: 0,
            thisMonthDiaries = monthlyStats.totalDiaries,
            thisMonthWords = monthlyStats.totalWords ?: 0,
            happyDaysRatio = if (yearlyStats.totalDiaries > 0)
                (yearlyStats.happyDays.toFloat() / yearlyStats.totalDiaries * 100).toInt()
            else 0
        )
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算字数
     */
    private fun calculateWordCount(content: String): Int {
        // 中文字符算1个字，英文单词算1个字
        val chineseCount = content.count { it.code in 0x4E00..0x9FFF }
        val englishWords = content.replace(Regex("[\\u4E00-\\u9FFF]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .size
        return chineseCount + englishWords
    }

    /**
     * 检查今日是否已写日记
     */
    suspend fun hasTodayDiary(): Boolean {
        return getDiaryByDate(LocalDate.now()) != null
    }

    /**
     * 获取今日日记或创建新日记
     */
    suspend fun getTodayDiaryOrCreate(): DiaryEntity {
        val today = LocalDate.now()
        val existing = getDiaryByDate(today)
        if (existing != null) return existing

        val id = createDiary(content = "", date = today)
        return diaryDao.getById(id)!!
    }
}

// ==================== 数据模型 ====================

/**
 * 日记搜索过滤器
 */
data class DiarySearchFilter(
    val keyword: String? = null,
    val moodScore: Int? = null,
    val weather: String? = null,
    val hasLocation: Boolean? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isFavorite: Boolean? = null,
    val limit: Int = 50,
    val offset: Int = 0
)

/**
 * 日历天数据
 */
data class DiaryCalendarDay(
    val date: Int,
    val isCurrentMonth: Boolean,
    val hasDiary: Boolean,
    val moodScore: Int? = null,
    val preview: String? = null,
    val hasLocation: Boolean = false,
    val hasAttachments: Boolean = false
)

/**
 * 日历数据
 */
data class DiaryCalendarData(
    val yearMonth: YearMonth,
    val days: List<DiaryCalendarDay>,
    val diaryCount: Int
)

/**
 * 日记综合统计
 */
data class DiaryOverallStats(
    val totalDiaries: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalWords: Int,
    val thisMonthDiaries: Int,
    val thisMonthWords: Int,
    val happyDaysRatio: Int
)
