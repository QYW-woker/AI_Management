package com.lifemanager.app.core.undo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 撤销操作管理器
 *
 * 用于管理删除操作的撤销功能，提供统一的撤销体验
 */
@Singleton
class UndoManager @Inject constructor() {

    companion object {
        /** 撤销超时时间（毫秒） */
        const val UNDO_TIMEOUT_MS = 5000L
    }

    private val _undoState = MutableStateFlow<UndoState>(UndoState.Idle)
    val undoState: StateFlow<UndoState> = _undoState.asStateFlow()

    private val _undoEvents = MutableSharedFlow<UndoEvent>()
    val undoEvents: SharedFlow<UndoEvent> = _undoEvents.asSharedFlow()

    private var currentAction: UndoAction? = null
    private var timeoutJob: Job? = null

    /**
     * 注册一个可撤销的删除操作
     *
     * @param scope 协程作用域（用于超时计时）
     * @param action 撤销操作
     */
    fun registerUndoAction(scope: CoroutineScope, action: UndoAction) {
        // 取消之前的操作（如果有）
        cancelPendingUndo()

        currentAction = action
        _undoState.value = UndoState.Pending(action)

        // 发送显示Snackbar事件
        scope.launch {
            _undoEvents.emit(UndoEvent.ShowSnackbar(action.message))
        }

        // 设置超时自动执行删除
        timeoutJob = scope.launch {
            delay(UNDO_TIMEOUT_MS)
            executePendingDelete()
        }
    }

    /**
     * 执行撤销操作
     */
    suspend fun undo(): Boolean {
        val action = currentAction ?: return false

        // 取消超时
        timeoutJob?.cancel()
        timeoutJob = null

        return try {
            action.onUndo()
            _undoState.value = UndoState.Undone
            _undoEvents.emit(UndoEvent.UndoSuccess(action.undoMessage))
            currentAction = null
            true
        } catch (e: Exception) {
            _undoEvents.emit(UndoEvent.UndoFailed(e.message ?: "撤销失败"))
            false
        }
    }

    /**
     * 取消待处理的撤销操作（不执行删除）
     */
    fun cancelPendingUndo() {
        timeoutJob?.cancel()
        timeoutJob = null
        currentAction = null
        _undoState.value = UndoState.Idle
    }

    /**
     * 立即执行待处理的删除（跳过撤销机会）
     */
    suspend fun executePendingDelete() {
        val action = currentAction ?: return

        timeoutJob?.cancel()
        timeoutJob = null

        try {
            action.onDelete()
            _undoState.value = UndoState.Deleted
        } catch (e: Exception) {
            _undoEvents.emit(UndoEvent.DeleteFailed(e.message ?: "删除失败"))
        } finally {
            currentAction = null
            _undoState.value = UndoState.Idle
        }
    }

    /**
     * Snackbar被关闭时调用
     */
    suspend fun onSnackbarDismissed() {
        // Snackbar关闭时执行删除
        executePendingDelete()
    }
}

/**
 * 撤销操作定义
 */
data class UndoAction(
    /** 操作类型 */
    val type: UndoType,
    /** 提示消息 */
    val message: String,
    /** 撤销成功消息 */
    val undoMessage: String = "已撤销",
    /** 执行删除操作 */
    val onDelete: suspend () -> Unit,
    /** 执行撤销操作 */
    val onUndo: suspend () -> Unit
)

/**
 * 撤销操作类型
 */
enum class UndoType {
    DELETE_TRANSACTION,
    DELETE_TODO,
    DELETE_HABIT,
    DELETE_DIARY,
    DELETE_GOAL,
    BATCH_DELETE
}

/**
 * 撤销状态
 */
sealed class UndoState {
    /** 空闲状态 */
    object Idle : UndoState()

    /** 待处理状态（可撤销） */
    data class Pending(val action: UndoAction) : UndoState()

    /** 已撤销 */
    object Undone : UndoState()

    /** 已删除 */
    object Deleted : UndoState()
}

/**
 * 撤销事件
 */
sealed class UndoEvent {
    data class ShowSnackbar(val message: String) : UndoEvent()
    data class UndoSuccess(val message: String) : UndoEvent()
    data class UndoFailed(val message: String) : UndoEvent()
    data class DeleteFailed(val message: String) : UndoEvent()
}
