package com.lifemanager.app.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room数据库类型转换器
 *
 * 用于在Room不支持的类型和支持的类型之间进行转换
 * 主要处理List<String>和JSON字符串之间的转换
 */
class Converters {

    // JSON解析器，配置为宽松模式以处理各种格式
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * 将JSON字符串转换为String列表
     */
    @TypeConverter
    fun fromStringList(value: String): List<String> {
        return try {
            if (value.isBlank() || value == "[]") {
                emptyList()
            } else {
                json.decodeFromString<List<String>>(value)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 将String列表转换为JSON字符串
     */
    @TypeConverter
    fun toStringList(list: List<String>): String {
        return try {
            json.encodeToString(list)
        } catch (e: Exception) {
            "[]"
        }
    }

    /**
     * 将Long列表的JSON字符串转换为列表
     */
    @TypeConverter
    fun fromLongList(value: String): List<Long> {
        return try {
            if (value.isBlank() || value == "[]") {
                emptyList()
            } else {
                json.decodeFromString<List<Long>>(value)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 将Long列表转换为JSON字符串
     */
    @TypeConverter
    fun toLongList(list: List<Long>): String {
        return try {
            json.encodeToString(list)
        } catch (e: Exception) {
            "[]"
        }
    }

    /**
     * 将Int列表的JSON字符串转换为列表
     */
    @TypeConverter
    fun fromIntList(value: String): List<Int> {
        return try {
            if (value.isBlank() || value == "[]") {
                emptyList()
            } else {
                json.decodeFromString<List<Int>>(value)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 将Int列表转换为JSON字符串
     */
    @TypeConverter
    fun toIntList(list: List<Int>): String {
        return try {
            json.encodeToString(list)
        } catch (e: Exception) {
            "[]"
        }
    }
}
