package com.lifemanager.app.core.database.dao

import androidx.room.*
import com.lifemanager.app.core.database.entity.SavingsPlanEntity
import kotlinx.coroutines.flow.Flow

/**
 * 存钱计划DAO接口
 */
@Dao
interface SavingsPlanDao {

    /**
     * 获取所有活跃的存钱计划
     */
    @Query("""
        SELECT * FROM savings_plans
        WHERE status = 'ACTIVE'
        ORDER BY createdAt DESC
    """)
    fun getActivePlans(): Flow<List<SavingsPlanEntity>>

    /**
     * 获取所有存钱计划（不包括已取消的）
     */
    @Query("""
        SELECT * FROM savings_plans
        WHERE status != 'CANCELLED'
        ORDER BY status ASC, createdAt DESC
    """)
    fun getAllPlans(): Flow<List<SavingsPlanEntity>>

    /**
     * 根据ID获取计划
     */
    @Query("SELECT * FROM savings_plans WHERE id = :id")
    suspend fun getById(id: Long): SavingsPlanEntity?

    /**
     * 根据ID获取计划（Flow版本）
     */
    @Query("SELECT * FROM savings_plans WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<SavingsPlanEntity?>

    /**
     * 插入计划
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: SavingsPlanEntity): Long

    /**
     * 更新计划
     */
    @Update
    suspend fun update(plan: SavingsPlanEntity)

    /**
     * 更新计划状态
     */
    @Query("""
        UPDATE savings_plans
        SET status = :status, updatedAt = :updatedAt
        WHERE id = :id
    """)
    suspend fun updateStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    /**
     * 更新当前金额
     */
    @Query("""
        UPDATE savings_plans
        SET currentAmount = :amount, updatedAt = :updatedAt
        WHERE id = :id
    """)
    suspend fun updateAmount(id: Long, amount: Double, updatedAt: Long = System.currentTimeMillis())

    /**
     * 增加存款金额
     */
    @Query("""
        UPDATE savings_plans
        SET currentAmount = currentAmount + :amount, updatedAt = :updatedAt
        WHERE id = :id
    """)
    suspend fun addAmount(id: Long, amount: Double, updatedAt: Long = System.currentTimeMillis())

    /**
     * 删除计划
     */
    @Query("DELETE FROM savings_plans WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 统计活跃计划数
     */
    @Query("SELECT COUNT(*) FROM savings_plans WHERE status = 'ACTIVE'")
    suspend fun countActive(): Int

    /**
     * 获取所有计划的总目标金额
     */
    @Query("SELECT COALESCE(SUM(targetAmount), 0.0) FROM savings_plans WHERE status = 'ACTIVE'")
    suspend fun getTotalTarget(): Double

    /**
     * 获取所有计划的当前总金额
     */
    @Query("SELECT COALESCE(SUM(currentAmount), 0.0) FROM savings_plans WHERE status = 'ACTIVE'")
    suspend fun getTotalCurrent(): Double
}
