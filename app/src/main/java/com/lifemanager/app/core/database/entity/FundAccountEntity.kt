package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 资金账户实体类
 *
 * 用于管理用户的各类资金账户，包括：
 * - 现金账户
 * - 银行卡（储蓄卡）
 * - 信用卡/花呗等信贷账户
 * - 支付宝/微信等电子钱包
 * - 投资账户
 */
@Entity(
    tableName = "fund_accounts",
    indices = [
        Index(value = ["accountType"]),
        Index(value = ["parentId"]),
        Index(value = ["isEnabled"])
    ]
)
data class FundAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 父账户ID（用于账户分组，如"银行卡"下有多个银行）
    val parentId: Long? = null,

    // 账户名称（如"中国银行储蓄卡"、"支付宝余额"）
    val name: String,

    // 账户类型
    val accountType: String,

    // 账户图标
    val iconName: String = "account_balance_wallet",

    // 账户颜色
    val color: String = "#4CAF50",

    // 当前余额（对于信贷账户为负债金额）
    val balance: Double = 0.0,

    // 信用额度（仅信贷账户使用）
    val creditLimit: Double? = null,

    // 账单日（仅信贷账户使用，1-31）
    val billDay: Int? = null,

    // 还款日（仅信贷账户使用，1-31）
    val repaymentDay: Int? = null,

    // 备注
    val note: String = "",

    // 是否计入总资产/总负债统计
    val includeInTotal: Boolean = true,

    // 是否启用
    val isEnabled: Boolean = true,

    // 是否为系统预设账户
    val isPreset: Boolean = false,

    // 排序顺序
    val sortOrder: Int = 0,

    // 创建时间
    val createdAt: Long = System.currentTimeMillis(),

    // 最后更新时间
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 账户类型枚举
 */
object AccountType {
    const val CASH = "CASH"                     // 现金
    const val BANK_CARD = "BANK_CARD"           // 银行卡（储蓄卡）
    const val CREDIT_CARD = "CREDIT_CARD"       // 信用卡
    const val ALIPAY = "ALIPAY"                 // 支付宝
    const val WECHAT = "WECHAT"                 // 微信支付
    const val CREDIT_LOAN = "CREDIT_LOAN"       // 信贷账户（花呗、借呗等）
    const val INVESTMENT = "INVESTMENT"         // 投资账户
    const val OTHER = "OTHER"                   // 其他

    fun getDisplayName(type: String): String = when (type) {
        CASH -> "现金"
        BANK_CARD -> "银行卡"
        CREDIT_CARD -> "信用卡"
        ALIPAY -> "支付宝"
        WECHAT -> "微信支付"
        CREDIT_LOAN -> "信贷账户"
        INVESTMENT -> "投资账户"
        OTHER -> "其他"
        else -> "未知"
    }

    fun getIcon(type: String): String = when (type) {
        CASH -> "💵"
        BANK_CARD -> "💳"
        CREDIT_CARD -> "💳"
        ALIPAY -> "🅰️"
        WECHAT -> "💚"
        CREDIT_LOAN -> "🏦"
        INVESTMENT -> "📈"
        OTHER -> "💰"
        else -> "💰"
    }

    fun isDebtAccount(type: String): Boolean = when (type) {
        CREDIT_CARD, CREDIT_LOAN -> true
        else -> false
    }
}
