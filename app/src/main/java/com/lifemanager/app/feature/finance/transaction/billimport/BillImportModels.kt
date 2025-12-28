package com.lifemanager.app.feature.finance.transaction.billimport

import com.lifemanager.app.core.database.entity.CustomFieldEntity

/**
 * 账单来源类型
 */
enum class BillSource(val displayName: String) {
    WECHAT("微信"),
    ALIPAY("支付宝"),
    UNKNOWN("未知")
}

/**
 * 解析后的账单记录
 */
data class ParsedBillRecord(
    val datetime: String,           // 交易时间 (yyyy-MM-dd HH:mm:ss)
    val type: String,               // 收/支
    val counterparty: String,       // 交易对方
    val goods: String,              // 商品说明
    val amount: Double,             // 金额
    val paymentMethod: String,      // 支付方式
    val status: String,             // 交易状态
    val orderNo: String,            // 交易单号
    val merchantNo: String = "",    // 商户单号
    val note: String = "",          // 备注
    val source: BillSource = BillSource.UNKNOWN,  // 账单来源
    // 用于UI编辑
    var suggestedCategoryId: Long? = null,  // 建议分类ID
    var isSelected: Boolean = true          // 是否选中导入
)

/**
 * 账单解析结果
 */
sealed class BillParseResult {
    data class Success(
        val records: List<ParsedBillRecord>,
        val source: BillSource,
        val totalIncome: Double,
        val totalExpense: Double,
        val skippedCount: Int = 0       // 跳过的记录数（如退款、红包等）
    ) : BillParseResult()

    data class Error(val message: String) : BillParseResult()
}

/**
 * 导入状态
 */
sealed class ImportState {
    object Idle : ImportState()
    object SelectingFile : ImportState()
    object Parsing : ImportState()
    data class Preview(
        val records: List<ParsedBillRecord>,
        val source: BillSource,
        val categories: List<CustomFieldEntity>
    ) : ImportState()
    object Importing : ImportState()
    data class Success(val importedCount: Int, val totalAmount: Double) : ImportState()
    data class Error(val message: String) : ImportState()
}

/**
 * 导入统计
 */
data class ImportStats(
    val totalRecords: Int,
    val selectedRecords: Int,
    val totalIncome: Double,
    val totalExpense: Double,
    val duplicateCount: Int = 0
)

/**
 * 分类匹配规则
 */
data class CategoryMatchRule(
    val keywords: List<String>,     // 关键词列表
    val categoryName: String        // 对应分类名称
)
