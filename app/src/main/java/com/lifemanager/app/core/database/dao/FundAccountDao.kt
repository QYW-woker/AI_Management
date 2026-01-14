package com.lifemanager.app.core.database.dao

import androidx.room.*
import com.lifemanager.app.core.database.entity.FundAccountEntity
import kotlinx.coroutines.flow.Flow

/**
 * 资金账户DAO接口
 */
@Dao
interface FundAccountDao {

    /**
     * 获取所有账户
     */
    @Query("""
        SELECT * FROM fund_accounts
        WHERE isEnabled = 1
        ORDER BY sortOrder ASC, createdAt DESC
    """)
    fun getAllEnabled(): Flow<List<FundAccountEntity>>

    /**
     * 获取所有账户（包括禁用）
     */
    @Query("""
        SELECT * FROM fund_accounts
        ORDER BY sortOrder ASC, createdAt DESC
    """)
    fun getAll(): Flow<List<FundAccountEntity>>

    /**
     * 按类型获取账户
     */
    @Query("""
        SELECT * FROM fund_accounts
        WHERE accountType = :type AND isEnabled = 1
        ORDER BY sortOrder ASC, createdAt DESC
    """)
    fun getByType(type: String): Flow<List<FundAccountEntity>>

    /**
     * 获取顶级账户（无父账户）
     */
    @Query("""
        SELECT * FROM fund_accounts
        WHERE parentId IS NULL AND isEnabled = 1
        ORDER BY sortOrder ASC, createdAt DESC
    """)
    fun getTopLevelAccounts(): Flow<List<FundAccountEntity>>

    /**
     * 获取子账户
     */
    @Query("""
        SELECT * FROM fund_accounts
        WHERE parentId = :parentId AND isEnabled = 1
        ORDER BY sortOrder ASC, createdAt DESC
    """)
    fun getChildAccounts(parentId: Long): Flow<List<FundAccountEntity>>

    /**
     * 根据ID获取账户
     */
    @Query("SELECT * FROM fund_accounts WHERE id = :id")
    suspend fun getById(id: Long): FundAccountEntity?

    /**
     * 获取总资产（计入统计的非负债账户余额总和）
     */
    @Query("""
        SELECT COALESCE(SUM(balance), 0.0) FROM fund_accounts
        WHERE includeInTotal = 1 AND isEnabled = 1
        AND accountType NOT IN ('CREDIT_CARD', 'CREDIT_LOAN')
    """)
    suspend fun getTotalAssets(): Double

    /**
     * 获取总负债（计入统计的负债账户余额总和）
     */
    @Query("""
        SELECT COALESCE(SUM(balance), 0.0) FROM fund_accounts
        WHERE includeInTotal = 1 AND isEnabled = 1
        AND accountType IN ('CREDIT_CARD', 'CREDIT_LOAN')
    """)
    suspend fun getTotalLiabilities(): Double

    /**
     * 获取净资产
     */
    @Query("""
        SELECT COALESCE(
            (SELECT COALESCE(SUM(balance), 0.0) FROM fund_accounts
             WHERE includeInTotal = 1 AND isEnabled = 1
             AND accountType NOT IN ('CREDIT_CARD', 'CREDIT_LOAN'))
            -
            (SELECT COALESCE(SUM(balance), 0.0) FROM fund_accounts
             WHERE includeInTotal = 1 AND isEnabled = 1
             AND accountType IN ('CREDIT_CARD', 'CREDIT_LOAN'))
        , 0.0)
    """)
    suspend fun getNetWorth(): Double

    /**
     * 插入账户
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: FundAccountEntity): Long

    /**
     * 批量插入账户
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<FundAccountEntity>)

    /**
     * 更新账户
     */
    @Update
    suspend fun update(account: FundAccountEntity)

    /**
     * 更新账户余额
     */
    @Query("UPDATE fund_accounts SET balance = :balance, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBalance(id: Long, balance: Double, updatedAt: Long = System.currentTimeMillis())

    /**
     * 增加账户余额
     */
    @Query("UPDATE fund_accounts SET balance = balance + :amount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun addBalance(id: Long, amount: Double, updatedAt: Long = System.currentTimeMillis())

    /**
     * 减少账户余额
     */
    @Query("UPDATE fund_accounts SET balance = balance - :amount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun subtractBalance(id: Long, amount: Double, updatedAt: Long = System.currentTimeMillis())

    /**
     * 删除账户
     */
    @Query("DELETE FROM fund_accounts WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 禁用账户（软删除）
     */
    @Query("UPDATE fund_accounts SET isEnabled = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun disable(id: Long, updatedAt: Long = System.currentTimeMillis())

    /**
     * 启用账户
     */
    @Query("UPDATE fund_accounts SET isEnabled = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun enable(id: Long, updatedAt: Long = System.currentTimeMillis())

    /**
     * 获取账户数量
     */
    @Query("SELECT COUNT(*) FROM fund_accounts WHERE isEnabled = 1")
    suspend fun getCount(): Int

    /**
     * 检查账户名是否存在
     */
    @Query("SELECT COUNT(*) FROM fund_accounts WHERE name = :name AND id != :excludeId")
    suspend fun countByName(name: String, excludeId: Long = 0): Int
}
