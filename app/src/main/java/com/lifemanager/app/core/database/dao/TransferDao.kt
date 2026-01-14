package com.lifemanager.app.core.database.dao

import androidx.room.*
import com.lifemanager.app.core.database.entity.TransferEntity
import kotlinx.coroutines.flow.Flow

/**
 * 转账记录DAO接口
 */
@Dao
interface TransferDao {

    /**
     * 获取指定日期范围的转账记录
     */
    @Query("""
        SELECT * FROM account_transfers
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, createdAt DESC
    """)
    fun getByDateRange(startDate: Int, endDate: Int): Flow<List<TransferEntity>>

    /**
     * 获取指定账户的转账记录（转出或转入）
     */
    @Query("""
        SELECT * FROM account_transfers
        WHERE fromAccountId = :accountId OR toAccountId = :accountId
        ORDER BY date DESC, createdAt DESC
    """)
    fun getByAccount(accountId: Long): Flow<List<TransferEntity>>

    /**
     * 获取最近的转账记录
     */
    @Query("""
        SELECT * FROM account_transfers
        ORDER BY date DESC, createdAt DESC
        LIMIT :limit
    """)
    fun getRecent(limit: Int = 20): Flow<List<TransferEntity>>

    /**
     * 根据ID获取转账记录
     */
    @Query("SELECT * FROM account_transfers WHERE id = :id")
    suspend fun getById(id: Long): TransferEntity?

    /**
     * 插入转账记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transfer: TransferEntity): Long

    /**
     * 更新转账记录
     */
    @Update
    suspend fun update(transfer: TransferEntity)

    /**
     * 删除转账记录
     */
    @Query("DELETE FROM account_transfers WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 获取指定日期范围内的总转账金额
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM account_transfers
        WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalInRange(startDate: Int, endDate: Int): Double

    /**
     * 获取指定日期范围内的总手续费
     */
    @Query("""
        SELECT COALESCE(SUM(fee), 0.0) FROM account_transfers
        WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalFeeInRange(startDate: Int, endDate: Int): Double
}
