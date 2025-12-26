package com.lifemanager.app.core.database

import com.lifemanager.app.core.database.entity.*

/**
 * 预设数据定义
 *
 * 包含应用初始化时需要插入的所有预设类别
 * 这些预设项不可删除，但可以禁用
 */
object PresetData {

    /**
     * 收入类别预设
     */
    val incomeFields = listOf(
        CustomFieldEntity(
            moduleType = ModuleType.INCOME,
            name = "工资收入",
            iconName = "work",
            color = "#4CAF50",
            tagType = TagType.OTHER,
            sortOrder = 1,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.INCOME,
            name = "副业收入",
            iconName = "business_center",
            color = "#8BC34A",
            tagType = TagType.OTHER,
            sortOrder = 2,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.INCOME,
            name = "投资收益",
            iconName = "trending_up",
            color = "#FF9800",
            tagType = TagType.INVESTMENT,
            sortOrder = 3,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.INCOME,
            name = "奖金",
            iconName = "emoji_events",
            color = "#FFC107",
            tagType = TagType.OTHER,
            sortOrder = 4,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.INCOME,
            name = "红包礼金",
            iconName = "card_giftcard",
            color = "#E91E63",
            tagType = TagType.OTHER,
            sortOrder = 5,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.INCOME,
            name = "其他收入",
            iconName = "more_horiz",
            color = "#9E9E9E",
            tagType = TagType.OTHER,
            sortOrder = 99,
            isPreset = true
        )
    )

    /**
     * 支出类别预设（收入的去向）
     */
    val expenseFields = listOf(
        CustomFieldEntity(
            moduleType = ModuleType.EXPENSE,
            name = "存款",
            iconName = "savings",
            color = "#2196F3",
            tagType = TagType.SAVINGS,
            sortOrder = 1,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.EXPENSE,
            name = "养老金",
            iconName = "elderly",
            color = "#3F51B5",
            tagType = TagType.SAVINGS,
            sortOrder = 2,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.EXPENSE,
            name = "股票",
            iconName = "show_chart",
            color = "#E91E63",
            tagType = TagType.INVESTMENT,
            sortOrder = 3,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.EXPENSE,
            name = "基金",
            iconName = "pie_chart",
            color = "#9C27B0",
            tagType = TagType.INVESTMENT,
            sortOrder = 4,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.EXPENSE,
            name = "日常支出",
            iconName = "shopping_cart",
            color = "#FF5722",
            tagType = TagType.CONSUMPTION,
            sortOrder = 5,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.EXPENSE,
            name = "大额支出",
            iconName = "credit_card",
            color = "#795548",
            tagType = TagType.CONSUMPTION,
            sortOrder = 6,
            isPreset = true
        )
    )

    /**
     * 资产类别预设
     */
    val assetFields = listOf(
        CustomFieldEntity(
            moduleType = ModuleType.ASSET,
            name = "活期存款",
            iconName = "account_balance",
            color = "#2196F3",
            tagType = TagType.SAVINGS,
            sortOrder = 1,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.ASSET,
            name = "定期存款",
            iconName = "lock",
            color = "#1976D2",
            tagType = TagType.SAVINGS,
            sortOrder = 2,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.ASSET,
            name = "货币基金",
            iconName = "monetization_on",
            color = "#00BCD4",
            tagType = TagType.SAVINGS,
            sortOrder = 3,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.ASSET,
            name = "股票",
            iconName = "show_chart",
            color = "#E91E63",
            tagType = TagType.INVESTMENT,
            sortOrder = 4,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.ASSET,
            name = "基金",
            iconName = "pie_chart",
            color = "#9C27B0",
            tagType = TagType.INVESTMENT,
            sortOrder = 5,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.ASSET,
            name = "养老金账户",
            iconName = "elderly",
            color = "#3F51B5",
            tagType = TagType.SAVINGS,
            sortOrder = 6,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.ASSET,
            name = "房产",
            iconName = "home",
            color = "#FF9800",
            tagType = TagType.OTHER,
            sortOrder = 7,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.ASSET,
            name = "车辆",
            iconName = "directions_car",
            color = "#607D8B",
            tagType = TagType.OTHER,
            sortOrder = 8,
            isPreset = true
        )
    )

    /**
     * 负债类别预设
     */
    val liabilityFields = listOf(
        CustomFieldEntity(
            moduleType = ModuleType.LIABILITY,
            name = "房贷",
            iconName = "home",
            color = "#F44336",
            tagType = TagType.FIXED,
            sortOrder = 1,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.LIABILITY,
            name = "车贷",
            iconName = "directions_car",
            color = "#E91E63",
            tagType = TagType.FIXED,
            sortOrder = 2,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.LIABILITY,
            name = "信用卡",
            iconName = "credit_card",
            color = "#9C27B0",
            tagType = TagType.OTHER,
            sortOrder = 3,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.LIABILITY,
            name = "借款",
            iconName = "handshake",
            color = "#795548",
            tagType = TagType.OTHER,
            sortOrder = 4,
            isPreset = true
        )
    )

    /**
     * 月度开销类别预设
     */
    val monthlyExpenseFields = listOf(
        CustomFieldEntity(
            moduleType = ModuleType.MONTHLY_EXPENSE,
            name = "房租/房贷",
            iconName = "home",
            color = "#F44336",
            tagType = TagType.FIXED,
            sortOrder = 1,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.MONTHLY_EXPENSE,
            name = "水电燃气",
            iconName = "bolt",
            color = "#FF9800",
            tagType = TagType.FIXED,
            sortOrder = 2,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.MONTHLY_EXPENSE,
            name = "物业费",
            iconName = "apartment",
            color = "#FF5722",
            tagType = TagType.FIXED,
            sortOrder = 3,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.MONTHLY_EXPENSE,
            name = "交通出行",
            iconName = "directions_car",
            color = "#2196F3",
            tagType = TagType.OTHER,
            sortOrder = 4,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.MONTHLY_EXPENSE,
            name = "餐饮伙食",
            iconName = "restaurant",
            color = "#4CAF50",
            tagType = TagType.OTHER,
            sortOrder = 5,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.MONTHLY_EXPENSE,
            name = "日用品",
            iconName = "shopping_basket",
            color = "#9C27B0",
            tagType = TagType.OTHER,
            sortOrder = 6,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.MONTHLY_EXPENSE,
            name = "通讯网络",
            iconName = "wifi",
            color = "#00BCD4",
            tagType = TagType.FIXED,
            sortOrder = 7,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.MONTHLY_EXPENSE,
            name = "医疗保健",
            iconName = "local_hospital",
            color = "#E91E63",
            tagType = TagType.OTHER,
            sortOrder = 8,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.MONTHLY_EXPENSE,
            name = "娱乐休闲",
            iconName = "sports_esports",
            color = "#673AB7",
            tagType = TagType.OTHER,
            sortOrder = 9,
            isPreset = true
        ),
        CustomFieldEntity(
            moduleType = ModuleType.MONTHLY_EXPENSE,
            name = "教育学习",
            iconName = "school",
            color = "#3F51B5",
            tagType = TagType.OTHER,
            sortOrder = 10,
            isPreset = true
        )
    )

    /**
     * 时间统计分类预设
     */
    val timeCategories = listOf(
        TimeCategoryEntity(
            name = "工作",
            iconName = "work",
            color = "#2196F3",
            sortOrder = 1
        ),
        TimeCategoryEntity(
            name = "学习",
            iconName = "school",
            color = "#4CAF50",
            sortOrder = 2
        ),
        TimeCategoryEntity(
            name = "运动",
            iconName = "fitness_center",
            color = "#FF9800",
            sortOrder = 3
        ),
        TimeCategoryEntity(
            name = "娱乐",
            iconName = "sports_esports",
            color = "#9C27B0",
            sortOrder = 4
        ),
        TimeCategoryEntity(
            name = "休息",
            iconName = "hotel",
            color = "#607D8B",
            sortOrder = 5
        ),
        TimeCategoryEntity(
            name = "社交",
            iconName = "people",
            color = "#E91E63",
            sortOrder = 6
        ),
        TimeCategoryEntity(
            name = "其他",
            iconName = "more_horiz",
            color = "#9E9E9E",
            sortOrder = 99
        )
    )

    /**
     * 获取所有自定义字段预设
     */
    fun getAllCustomFields(): List<CustomFieldEntity> {
        return incomeFields + expenseFields + assetFields + liabilityFields + monthlyExpenseFields
    }
}
