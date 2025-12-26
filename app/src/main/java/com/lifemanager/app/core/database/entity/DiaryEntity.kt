package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 日记实体类
 *
 * 用于记录日常生活点滴
 * 支持AI情绪分析和主题标签
 * 可附加图片、语音等多媒体
 */
@Entity(
    tableName = "diaries",
    indices = [Index(value = ["date"], unique = true)]
)
data class DiaryEntity(
    // 主键ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 日期，epochDay格式
    // 每天只能有一篇日记
    val date: Int,

    // 日记内容
    val content: String,

    // 心情评分 (1-5)
    // 1: 很差, 2: 较差, 3: 一般, 4: 较好, 5: 很好
    val moodScore: Int? = null,

    // AI分析的情绪标签，JSON数组格式
    // 如: ["开心", "感恩", "期待"]
    val moodTags: String = "[]",

    // AI分类的主题标签，JSON数组格式
    // 如: ["工作", "学习", "社交"]
    val topicTags: String = "[]",

    // 附件路径（图片/语音/视频），JSON数组格式
    val attachments: String = "[]",

    // 天气
    val weather: String? = null,

    // 位置
    val location: String? = null,

    // 创建时间
    val createdAt: Long = System.currentTimeMillis(),

    // 更新时间
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 心情评分枚举
 */
object MoodScore {
    const val VERY_BAD = 1      // 很差
    const val BAD = 2           // 较差
    const val NORMAL = 3        // 一般
    const val GOOD = 4          // 较好
    const val VERY_GOOD = 5     // 很好
}

/**
 * 天气选项
 */
object Weather {
    const val SUNNY = "SUNNY"       // 晴天
    const val CLOUDY = "CLOUDY"     // 多云
    const val OVERCAST = "OVERCAST" // 阴天
    const val RAINY = "RAINY"       // 雨天
    const val SNOWY = "SNOWY"       // 雪天
    const val WINDY = "WINDY"       // 大风
    const val FOGGY = "FOGGY"       // 雾天
}
