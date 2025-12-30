package com.lifemanager.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 卡通风格圆角形状定义
 *
 * 使用更大的圆角，营造可爱柔和的视觉效果
 */
val CartoonShapes = Shapes(
    // 超小圆角 - 用于小标签
    extraSmall = RoundedCornerShape(8.dp),

    // 小圆角 - 用于按钮、小卡片
    small = RoundedCornerShape(12.dp),

    // 中等圆角 - 用于普通卡片
    medium = RoundedCornerShape(20.dp),

    // 大圆角 - 用于大型卡片、对话框
    large = RoundedCornerShape(28.dp),

    // 超大圆角 - 用于全屏卡片、底部弹窗
    extraLarge = RoundedCornerShape(32.dp)
)

/**
 * 自定义卡通形状
 */
object CartoonShape {
    // 胶囊形状
    val Capsule = RoundedCornerShape(50)

    // 气泡形状（上大下小）
    val BubbleTop = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 8.dp,
        bottomEnd = 8.dp
    )

    // 气泡形状（下大上小）
    val BubbleBottom = RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 8.dp,
        bottomStart = 24.dp,
        bottomEnd = 24.dp
    )

    // 云朵卡片
    val Cloud = RoundedCornerShape(24.dp)

    // 圆形
    val Circle = RoundedCornerShape(50)

    // 可爱按钮
    val CuteButton = RoundedCornerShape(16.dp)

    // 可爱卡片
    val CuteCard = RoundedCornerShape(24.dp)

    // 超级圆润卡片
    val SuperRounded = RoundedCornerShape(32.dp)

    // 顶部圆角底部弹窗
    val BottomSheet = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    // 聊天气泡 - 左侧
    val ChatBubbleLeft = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 20.dp,
        bottomStart = 20.dp,
        bottomEnd = 20.dp
    )

    // 聊天气泡 - 右侧
    val ChatBubbleRight = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 4.dp,
        bottomStart = 20.dp,
        bottomEnd = 20.dp
    )

    // 快捷入口图标
    val QuickIcon = RoundedCornerShape(20.dp)

    // FAB形状
    val Fab = RoundedCornerShape(20.dp)
}
