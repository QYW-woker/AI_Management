package com.lifemanager.app.core.database.dao

import androidx.room.*
import com.lifemanager.app.core.database.entity.TimeCategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 时间分类DAO接口
 */
@Dao
interface TimeCategoryDao {

    /**
     * 获取所有启用的时间分类
     */
    @Query("""
        SELECT * FROM time_categories
        WHERE isEnabled = 1
        ORDER BY sortOrder ASC
    """)
    fun getEnabledCategories(): Flow<List<TimeCategoryEntity>>

    /**
     * 获取所有时间分类
     */
    @Query("SELECT * FROM time_categories ORDER BY sortOrder ASC")
    fun getAllCategories(): Flow<List<TimeCategoryEntity>>

    /**
     * 获取子分类
     */
    @Query("""
        SELECT * FROM time_categories
        WHERE parentId = :parentId
        ORDER BY sortOrder ASC
    """)
    fun getChildCategories(parentId: Long): Flow<List<TimeCategoryEntity>>

    /**
     * 根据ID获取分类
     */
    @Query("SELECT * FROM time_categories WHERE id = :id")
    suspend fun getById(id: Long): TimeCategoryEntity?

    /**
     * 插入分类
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: TimeCategoryEntity): Long

    /**
     * 批量插入分类
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<TimeCategoryEntity>)

    /**
     * 更新分类
     */
    @Update
    suspend fun update(category: TimeCategoryEntity)

    /**
     * 删除分类
     */
    @Query("DELETE FROM time_categories WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 统计分类数量
     */
    @Query("SELECT COUNT(*) FROM time_categories")
    suspend fun countAll(): Int
}
