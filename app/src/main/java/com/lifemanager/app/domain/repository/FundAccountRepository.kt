package com.lifemanager.app.domain.repository

import com.lifemanager.app.core.database.entity.FundAccountEntity
import kotlinx.coroutines.flow.Flow

/**
 * 资金账户仓库接口
 */
interface FundAccountRepository {

    /**
     * 获取所有启用的账户
     */
    fun getAllEnabled(): Flow<List<FundAccountEntity>>

    /**
     * 获取所有账户（包括禁用）
     */
    fun getAll(): Flow<List<FundAccountEntity>>

    /**
     * 按类型获取账户
     */
    fun getByType(type: String): Flow<List<FundAccountEntity>>

    /**
     * 获取顶级账户
     */
    fun getTopLevelAccounts(): Flow<List<FundAccountEntity>>

    /**
     * 获取子账户
     */
    fun getChildAccounts(parentId: Long): Flow<List<FundAccountEntity>>

    /**
     * 根据ID获取账户
     */
    suspend fun getById(id: Long): FundAccountEntity?

    /**
     * 获取总资产
     */
    suspend fun getTotalAssets(): Double

    /**
     * 获取总负债
     */
    suspend fun getTotalLiabilities(): Double

    /**
     * 获取净资产
     */
    suspend fun getNetWorth(): Double

    /**
     * 插入账户
     */
    suspend fun insert(account: FundAccountEntity): Long

    /**
     * 批量插入账户
     */
    suspend fun insertAll(accounts: List<FundAccountEntity>)

    /**
     * 更新账户
     */
    suspend fun update(account: FundAccountEntity)

    /**
     * 更新账户余额
     */
    suspend fun updateBalance(id: Long, balance: Double)

    /**
     * 增加账户余额
     */
    suspend fun addBalance(id: Long, amount: Double)

    /**
     * 减少账户余额
     */
    suspend fun subtractBalance(id: Long, amount: Double)

    /**
     * 删除账户
     */
    suspend fun deleteById(id: Long)

    /**
     * 禁用账户
     */
    suspend fun disable(id: Long)

    /**
     * 启用账户
     */
    suspend fun enable(id: Long)

    /**
     * 获取账户数量
     */
    suspend fun getCount(): Int

    /**
     * 检查账户名是否存在
     */
    suspend fun isNameExists(name: String, excludeId: Long = 0): Boolean
}
