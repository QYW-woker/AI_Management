package com.lifemanager.app.feature.diary

import com.lifemanager.app.core.database.entity.DiaryEntity
import com.lifemanager.app.core.database.entity.DiaryLocation
import com.lifemanager.app.core.database.entity.MoodIcon
import com.lifemanager.app.core.database.entity.Weather
import java.time.LocalDate
import java.time.YearMonth

/**
 * 日记列表界面状态
 */
data class DiaryListState(
    // 视图模式
    val viewMode: DiaryViewMode = DiaryViewMode.LIST,

    // 日记列表
    val diaries: List<DiaryListItem> = emptyList(),

    // 日历数据
    val calendarData: DiaryCalendarData? = null,

    // 当前选中的月份
    val currentMonth: YearMonth = YearMonth.now(),

    // 当前选中的日期
    val selectedDate: LocalDate? = null,

    // 搜索关键词
    val searchQuery: String = "",

    // 筛选条件
    val filter: DiaryFilter = DiaryFilter(),

    // 统计数据
    val stats: DiaryStatsData? = null,

    // 加载状态
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 0,

    // 错误信息
    val error: String? = null
)

/**
 * 视图模式
 */
enum class DiaryViewMode {
    LIST,       // 列表视图
    CALENDAR,   // 日历视图
    TIMELINE    // 时间线视图
}

/**
 * 日记列表项
 */
data class DiaryListItem(
    val id: Long,
    val date: LocalDate,
    val title: String,
    val preview: String,
    val moodScore: Int?,
    val moodIcon: String,
    val weather: String?,
    val weatherIcon: String?,
    val hasLocation: Boolean,
    val locationName: String?,
    val hasAttachments: Boolean,
    val attachmentCount: Int,
    val isFavorite: Boolean,
    val wordCount: Int,
    val createdAt: Long
) {
    companion object {
        fun fromEntity(entity: DiaryEntity): DiaryListItem {
            val preview = entity.content.take(100).replace("\n", " ")
            val attachmentList = parseAttachments(entity.attachments)

            return DiaryListItem(
                id = entity.id,
                date = LocalDate.ofEpochDay(entity.date.toLong()),
                title = entity.title.ifBlank { getDefaultTitle(entity) },
                preview = preview,
                moodScore = entity.moodScore,
                moodIcon = entity.moodScore?.let { MoodIcon.getIcon(it) } ?: "",
                weather = entity.weather,
                weatherIcon = entity.weather?.let { Weather.getIcon(it) },
                hasLocation = entity.locationName != null,
                locationName = entity.poiName ?: entity.locationName,
                hasAttachments = attachmentList.isNotEmpty(),
                attachmentCount = attachmentList.size,
                isFavorite = entity.isFavorite,
                wordCount = entity.wordCount,
                createdAt = entity.createdAt
            )
        }

        private fun getDefaultTitle(entity: DiaryEntity): String {
            val date = LocalDate.ofEpochDay(entity.date.toLong())
            return "${date.monthValue}月${date.dayOfMonth}日的日记"
        }

        private fun parseAttachments(attachments: String): List<String> {
            return try {
                attachments.removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim().removeSurrounding("\"") }
                    .filter { it.isNotBlank() }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

/**
 * 日记筛选条件
 */
data class DiaryFilter(
    val moodScore: Int? = null,
    val weather: String? = null,
    val hasLocation: Boolean? = null,
    val isFavorite: Boolean? = null,
    val dateRange: DateRange? = null
) {
    fun isActive(): Boolean {
        return moodScore != null || weather != null ||
                hasLocation != null || isFavorite != null || dateRange != null
    }

    fun activeCount(): Int {
        var count = 0
        if (moodScore != null) count++
        if (weather != null) count++
        if (hasLocation != null) count++
        if (isFavorite != null) count++
        if (dateRange != null) count++
        return count
    }
}

/**
 * 日期范围
 */
data class DateRange(
    val start: LocalDate,
    val end: LocalDate
)

/**
 * 日记统计数据
 */
data class DiaryStatsData(
    val totalDiaries: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalWords: Int,
    val thisMonthDiaries: Int,
    val avgWordsPerDiary: Int,
    val happyDaysRatio: Int,
    val moodDistribution: Map<Int, Int>,
    val weatherDistribution: Map<String, Int>
)

/**
 * 日记编辑界面状态
 */
data class DiaryEditState(
    // 日记数据
    val id: Long = 0,
    val date: LocalDate = LocalDate.now(),
    val title: String = "",
    val content: String = "",
    val moodScore: Int? = null,
    val weather: String? = null,
    val location: DiaryLocation? = null,
    val attachments: List<AttachmentItem> = emptyList(),
    val isFavorite: Boolean = false,
    val isPrivate: Boolean = false,

    // 编辑状态
    val isNewDiary: Boolean = true,
    val hasChanges: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val autoSaveEnabled: Boolean = true,
    val lastSavedAt: Long? = null,

    // 位置选择
    val isLocationPickerVisible: Boolean = false,
    val isLoadingLocation: Boolean = false,

    // 心情/天气选择
    val isMoodPickerVisible: Boolean = false,
    val isWeatherPickerVisible: Boolean = false,

    // 附件
    val isAttachmentPickerVisible: Boolean = false,

    // 字数统计
    val wordCount: Int = 0,

    // 错误信息
    val error: String? = null,
    val saveSuccess: Boolean = false
)

/**
 * 附件项
 */
data class AttachmentItem(
    val id: String,
    val path: String,
    val type: AttachmentType,
    val thumbnail: String? = null,
    val name: String = "",
    val size: Long = 0
)

/**
 * 附件类型
 */
enum class AttachmentType {
    IMAGE,
    VIDEO,
    AUDIO,
    FILE
}

/**
 * 日记详情界面状态
 */
data class DiaryDetailState(
    val diary: DiaryEntity? = null,
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val error: String? = null,

    // 位置信息
    val location: DiaryLocation? = null,

    // 附件列表
    val attachments: List<AttachmentItem> = emptyList(),

    // 相邻日记
    val previousDate: LocalDate? = null,
    val nextDate: LocalDate? = null
)

/**
 * 心情选项
 */
data class MoodOption(
    val score: Int,
    val name: String,
    val icon: String,
    val color: Long
) {
    companion object {
        fun getAll(): List<MoodOption> = (1..5).map { score ->
            MoodOption(
                score = score,
                name = MoodIcon.getName(score),
                icon = MoodIcon.getIcon(score),
                color = MoodIcon.getColor(score)
            )
        }
    }
}

/**
 * 天气选项
 */
data class WeatherOption(
    val type: String,
    val name: String,
    val icon: String
) {
    companion object {
        fun getAll(): List<WeatherOption> = Weather.getAll().map { (type, name) ->
            WeatherOption(
                type = type,
                name = name,
                icon = Weather.getIcon(type)
            )
        }
    }
}

/**
 * 日记事件
 */
sealed class DiaryEvent {
    // 导航事件
    data class NavigateToDetail(val id: Long) : DiaryEvent()
    data class NavigateToEdit(val id: Long? = null, val date: LocalDate? = null) : DiaryEvent()
    object NavigateBack : DiaryEvent()

    // 操作结果
    data class ShowMessage(val message: String) : DiaryEvent()
    data class ShowError(val error: String) : DiaryEvent()
    object SaveSuccess : DiaryEvent()
    object DeleteSuccess : DiaryEvent()

    // 位置事件
    object RequestLocationPermission : DiaryEvent()
    data class LocationSelected(val location: DiaryLocation) : DiaryEvent()
}

/**
 * 快捷日期选项
 */
object QuickDateOptions {
    fun getOptions(): List<Pair<String, LocalDate>> {
        val today = LocalDate.now()
        return listOf(
            "今天" to today,
            "昨天" to today.minusDays(1),
            "前天" to today.minusDays(2),
            "上周同日" to today.minusWeeks(1)
        )
    }
}

/**
 * 日记提示语
 */
object DiaryPrompts {
    private val prompts = listOf(
        "今天发生了什么有趣的事？",
        "记录下此刻的心情...",
        "今天最让你开心的事是什么？",
        "今天学到了什么新东西？",
        "今天有什么值得感恩的事？",
        "今天遇到了什么挑战？如何应对的？",
        "今天有什么想法或灵感？",
        "今天和谁进行了有意义的交流？",
        "今天完成了什么目标？",
        "今天有什么期待明天的事？"
    )

    fun getRandomPrompt(): String = prompts.random()

    fun getPromptForMood(moodScore: Int): String = when (moodScore) {
        1, 2 -> "发生了什么让你不开心的事？把它写下来，也许会好受一些..."
        3 -> "今天过得怎么样？记录下来吧..."
        4, 5 -> "看起来今天心情不错！分享一下你的喜悦吧..."
        else -> getRandomPrompt()
    }
}
