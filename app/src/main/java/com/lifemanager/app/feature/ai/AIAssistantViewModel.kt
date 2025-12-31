package com.lifemanager.app.feature.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifemanager.app.core.ai.model.*
import com.lifemanager.app.core.ai.service.ConversationalAIService
import com.lifemanager.app.core.ai.service.SmartSuggestionService
import com.lifemanager.app.core.database.entity.CustomFieldEntity
import com.lifemanager.app.domain.repository.CustomFieldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AI助手ViewModel
 *
 * 整合对话式AI、智能建议、快捷操作等功能
 */
@HiltViewModel
class AIAssistantViewModel @Inject constructor(
    private val conversationalAIService: ConversationalAIService,
    private val suggestionService: SmartSuggestionService,
    private val customFieldRepository: CustomFieldRepository
) : ViewModel() {

    // 对话上下文
    val conversationContext = conversationalAIService.conversationContext

    // AI模式
    val aiMode = conversationalAIService.aiMode

    // 处理状态
    val isProcessing = conversationalAIService.isProcessing

    // 智能建议
    val suggestions = suggestionService.suggestions

    // 快捷操作
    val quickActions = suggestionService.quickActions

    // UI状态
    private val _uiState = MutableStateFlow(AIAssistantUiState())
    val uiState: StateFlow<AIAssistantUiState> = _uiState.asStateFlow()

    // 分类列表
    private val _categories = MutableStateFlow<List<CustomFieldEntity>>(emptyList())
    val categories: StateFlow<List<CustomFieldEntity>> = _categories.asStateFlow()

    // 待执行的意图
    private val _pendingIntent = MutableStateFlow<CommandIntent?>(null)
    val pendingIntent: StateFlow<CommandIntent?> = _pendingIntent.asStateFlow()

    // 查询结果
    private val _queryResult = MutableStateFlow<DataQueryResult?>(null)
    val queryResult: StateFlow<DataQueryResult?> = _queryResult.asStateFlow()

    init {
        loadCategories()
        loadSuggestions()
        loadWelcomeMessage()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            customFieldRepository.getFieldsByModule("EXPENSE").collect { fields ->
                _categories.value = fields
            }
        }
    }

    private fun loadSuggestions() {
        viewModelScope.launch {
            suggestionService.generateDailySuggestions()
        }
    }

    private fun loadWelcomeMessage() {
        viewModelScope.launch {
            val welcomeMessage = conversationalAIService.getWelcomeMessage()
            _uiState.update { it.copy(welcomeMessage = welcomeMessage) }
        }
    }

    /**
     * 发送消息
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }

            conversationalAIService.sendMessage(text).fold(
                onSuccess = { response ->
                    // 检查是否有意图需要确认
                    response.intent?.let { intent ->
                        _pendingIntent.value = intent
                        _uiState.update { it.copy(showConfirmDialog = true) }
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }

    /**
     * 执行快捷回复
     */
    fun sendQuickReply(reply: QuickReply) {
        sendMessage(reply.text)
    }

    /**
     * 执行快捷操作
     */
    fun executeQuickAction(action: QuickAction) {
        sendMessage(action.command)
    }

    /**
     * 执行数据查询
     */
    fun executeQuery(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isQuerying = true) }

            conversationalAIService.executeQuery(query).fold(
                onSuccess = { result ->
                    _queryResult.value = result
                    _uiState.update { it.copy(isQuerying = false, showQueryResult = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isQuerying = false,
                            error = "查询失败: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    /**
     * 确认执行意图
     */
    fun confirmIntent(): CommandIntent? {
        val intent = _pendingIntent.value
        _pendingIntent.value = null
        _uiState.update { it.copy(showConfirmDialog = false) }
        return intent
    }

    /**
     * 取消执行意图
     */
    fun cancelIntent() {
        _pendingIntent.value = null
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    /**
     * 忽略建议
     */
    fun dismissSuggestion(suggestionId: String) {
        suggestionService.dismissSuggestion(suggestionId)
    }

    /**
     * 刷新建议
     */
    fun refreshSuggestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingSuggestions = true) }
            suggestionService.generateDailySuggestions()
            _uiState.update { it.copy(isRefreshingSuggestions = false) }
        }
    }

    /**
     * 生成AI个性化建议
     */
    fun generateAISuggestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingAISuggestions = true) }

            suggestionService.generateAIPersonalizedSuggestions().fold(
                onSuccess = { suggestions ->
                    _uiState.update {
                        it.copy(
                            isGeneratingAISuggestions = false,
                            aiSuggestions = suggestions
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isGeneratingAISuggestions = false,
                            error = "生成建议失败: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    /**
     * 切换AI模式
     */
    fun setAIMode(mode: AIMode) {
        conversationalAIService.setAIMode(mode)
    }

    /**
     * 清除对话历史
     */
    fun clearConversation() {
        conversationalAIService.clearConversation()
        loadWelcomeMessage()
    }

    /**
     * 关闭查询结果
     */
    fun dismissQueryResult() {
        _uiState.update { it.copy(showQueryResult = false) }
        _queryResult.value = null
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI状态
 */
data class AIAssistantUiState(
    val welcomeMessage: ChatMessage? = null,
    val showConfirmDialog: Boolean = false,
    val showQueryResult: Boolean = false,
    val isQuerying: Boolean = false,
    val isRefreshingSuggestions: Boolean = false,
    val isGeneratingAISuggestions: Boolean = false,
    val aiSuggestions: List<AISuggestion> = emptyList(),
    val error: String? = null
)
