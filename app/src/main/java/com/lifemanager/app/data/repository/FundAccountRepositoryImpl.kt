package com.lifemanager.app.data.repository

import com.lifemanager.app.core.database.dao.FundAccountDao
import com.lifemanager.app.core.database.entity.FundAccountEntity
import com.lifemanager.app.domain.repository.FundAccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 资金账户仓库实现
 */
@Singleton
class FundAccountRepositoryImpl @Inject constructor(
    private val dao: FundAccountDao
) : FundAccountRepository {

    override fun getAllEnabled(): Flow<List<FundAccountEntity>> {
        return dao.getAllEnabled()
    }

    override fun getAll(): Flow<List<FundAccountEntity>> {
        return dao.getAll()
    }

    override fun getByType(type: String): Flow<List<FundAccountEntity>> {
        return dao.getByType(type)
    }

    override fun getTopLevelAccounts(): Flow<List<FundAccountEntity>> {
        return dao.getTopLevelAccounts()
    }

    override fun getChildAccounts(parentId: Long): Flow<List<FundAccountEntity>> {
        return dao.getChildAccounts(parentId)
    }

    override suspend fun getById(id: Long): FundAccountEntity? {
        return dao.getById(id)
    }

    override suspend fun getTotalAssets(): Double {
        return dao.getTotalAssets()
    }

    override suspend fun getTotalLiabilities(): Double {
        return dao.getTotalLiabilities()
    }

    override suspend fun getNetWorth(): Double {
        return dao.getNetWorth()
    }

    override suspend fun insert(account: FundAccountEntity): Long {
        return dao.insert(account)
    }

    override suspend fun insertAll(accounts: List<FundAccountEntity>) {
        dao.insertAll(accounts)
    }

    override suspend fun update(account: FundAccountEntity) {
        dao.update(account)
    }

    override suspend fun updateBalance(id: Long, balance: Double) {
        dao.updateBalance(id, balance)
    }

    override suspend fun addBalance(id: Long, amount: Double) {
        dao.addBalance(id, amount)
    }

    override suspend fun subtractBalance(id: Long, amount: Double) {
        dao.subtractBalance(id, amount)
    }

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun disable(id: Long) {
        dao.disable(id)
    }

    override suspend fun enable(id: Long) {
        dao.enable(id)
    }

    override suspend fun getCount(): Int {
        return dao.getCount()
    }

    override suspend fun isNameExists(name: String, excludeId: Long): Boolean {
        return dao.countByName(name, excludeId) > 0
    }
}
