package com.patrick.flowtask.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.patrick.flowtask.data.TaskItem
import com.patrick.flowtask.data.TaskPriority
import com.patrick.flowtask.data.TaskRepository
import com.patrick.flowtask.domain.ConversationUseCase
import com.patrick.flowtask.observability.AppLogger
import com.patrick.flowtask.prompting.ContextState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0
)

data class TasksUiState(
    val allTasks: List<TaskItem> = emptyList(),
    val query: String = "",
    val visibleTasks: List<TaskItem> = emptyList()
)

data class ChatMessage(
    val content: String,
    val fromUser: Boolean
)

data class ConversationUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val latestError: String? = null
)

data class SettingsUiState(
    val appName: String = "FlowTask",
    val conversationalFlowName: String = "Conversational Flow"
)

class HomeViewModel(taskRepository: TaskRepository) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = taskRepository.tasks
        .map { tasks ->
            HomeUiState(
                totalTasks = tasks.size,
                completedTasks = tasks.count { it.completed }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}

class TasksViewModel(private val taskRepository: TaskRepository) : ViewModel() {
    private val query = MutableStateFlow("")

    val uiState: StateFlow<TasksUiState> = combine(taskRepository.tasks, query) { tasks, currentQuery ->
        val normalizedQuery = currentQuery.trim()
        val visible = if (normalizedQuery.isBlank()) {
            tasks
        } else {
            tasks.filter { it.title.contains(normalizedQuery, ignoreCase = true) }
        }
        TasksUiState(
            allTasks = tasks,
            query = currentQuery,
            visibleTasks = visible
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun addTask(title: String, priority: TaskPriority = TaskPriority.NORMAL) {
        viewModelScope.launch {
            taskRepository.addTask(title, priority)
        }
    }

    fun toggleTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.toggleTask(taskId)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
        }
    }
}

class ConversationViewModel(
    private val taskRepository: TaskRepository,
    private val useCase: ConversationUseCase
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = mutableUiState.asStateFlow()

    fun submitMessage(input: String) {
        val text = input.trim()
        if (text.isBlank()) return

        mutableUiState.update { state ->
            state.copy(
                isLoading = true,
                latestError = null,
                messages = state.messages + ChatMessage(content = text, fromUser = true)
            )
        }

        viewModelScope.launch {
            val outcome = runCatching {
                val currentTasks = taskRepository.tasks.value
                val context = ContextState(
                    activeTaskCount = currentTasks.count { !it.completed },
                    timeOfDay = "Anytime",
                    recentActivity = currentTasks.takeLast(3).map { it.title }
                )
                AppLogger.info("conversation_request", "Submitting conversational request")
                val result = useCase.handleUserInput(text, context)
                taskRepository.addParsedTasks(result.tasks)
                result
            }

            val result = outcome.getOrNull()
            val fatalError = outcome.exceptionOrNull()

            val assistantText = when {
                fatalError != null -> {
                    AppLogger.error("conversation_unexpected_error", "Conversation processing failed", fatalError)
                    "Something went wrong. Please try again."
                }
                result?.error != null -> {
                    AppLogger.warning("conversation_parse_error", result.error.reason)
                    result.error.reason
                }
                result?.clientError != null -> {
                    AppLogger.warning("conversation_client_error", result.clientError.message)
                    result.clientError.message
                }
                result?.tasks?.isEmpty() == true -> {
                    AppLogger.info("conversation_empty_result", "No tasks parsed from AI response")
                    "No tasks found in that request."
                }
                result != null -> {
                    AppLogger.info("conversation_success", "Parsed ${result.tasks.size} tasks")
                    "Added ${result.tasks.size} task(s) from Conversational Flow."
                }
                else -> "Something went wrong. Please try again."
            }

            mutableUiState.update { state ->
                state.copy(
                    isLoading = false,
                    latestError = result?.error?.reason ?: result?.clientError?.message ?: fatalError?.message,
                    messages = state.messages + ChatMessage(content = assistantText, fromUser = false)
                )
            }
        }
    }
}

class SettingsViewModel : ViewModel() {
    val uiState: StateFlow<SettingsUiState> =
        MutableStateFlow(SettingsUiState()).asStateFlow()
}

class HomeViewModelFactory(
    private val taskRepository: TaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(taskRepository) as T
    }
}

class TasksViewModelFactory(
    private val taskRepository: TaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TasksViewModel(taskRepository) as T
    }
}

class ConversationViewModelFactory(
    private val taskRepository: TaskRepository,
    private val useCase: ConversationUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConversationViewModel(taskRepository, useCase) as T
    }
}
