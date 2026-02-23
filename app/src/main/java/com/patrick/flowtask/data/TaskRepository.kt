package com.patrick.flowtask.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.patrick.flowtask.observability.AppLogger
import com.patrick.flowtask.prompting.ParsedTask
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

enum class TaskPriority {
    LOW,
    NORMAL,
    HIGH
}

enum class TaskSyncState {
    LOCAL_ONLY,
    SYNCED,
    PENDING_RETRY,
    FAILED
}

data class TaskItem(
    val id: String,
    val title: String,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val completed: Boolean = false,
    val updatedAtEpochMs: Long,
    val syncState: TaskSyncState = TaskSyncState.LOCAL_ONLY
)

interface TaskRepository {
    val tasks: StateFlow<List<TaskItem>>
    suspend fun addTask(title: String, priority: TaskPriority = TaskPriority.NORMAL)
    suspend fun addParsedTasks(tasks: List<ParsedTask>)
    suspend fun toggleTask(taskId: String)
    suspend fun deleteTask(taskId: String)
}

class InMemoryTaskRepository : TaskRepository {
    private val mutableTasks = MutableStateFlow<List<TaskItem>>(emptyList())

    override val tasks: StateFlow<List<TaskItem>> = mutableTasks.asStateFlow()

    override suspend fun addTask(title: String, priority: TaskPriority) {
        val sanitizedTitle = title.trim()
        if (sanitizedTitle.isBlank()) return
        mutableTasks.update { current ->
            current + TaskItem(
                id = UUID.randomUUID().toString(),
                title = sanitizedTitle,
                priority = priority,
                updatedAtEpochMs = nowEpochMillis()
            )
        }
    }

    override suspend fun addParsedTasks(tasks: List<ParsedTask>) {
        mutableTasks.update { current ->
            current + tasks
                .mapNotNull { parsed ->
                    val title = parsed.title.trim()
                    if (title.isBlank()) return@mapNotNull null
                    TaskItem(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        priority = parsed.priority.toTaskPriority(),
                        updatedAtEpochMs = nowEpochMillis()
                    )
                }
        }
    }

    override suspend fun toggleTask(taskId: String) {
        mutableTasks.update { current ->
            current.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        completed = !task.completed,
                        updatedAtEpochMs = nowEpochMillis(),
                        syncState = TaskSyncState.PENDING_RETRY
                    )
                } else {
                    task
                }
            }
        }
    }

    override suspend fun deleteTask(taskId: String) {
        mutableTasks.update { current ->
            current.filterNot { it.id == taskId }
        }
    }
}

internal const val TASK_REPOSITORY_STORE_NAME = "task_repository_store"
private val Context.taskDataStore by preferencesDataStore(name = TASK_REPOSITORY_STORE_NAME)

class PersistentTaskRepository(context: Context) : TaskRepository {
    private val dataStore = context.taskDataStore
    private val storageScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    override val tasks: StateFlow<List<TaskItem>> = dataStore.data
        .map { preferences ->
            val decoded = decodeFromPreferences(preferences)
            if (decoded.usedFallback) {
                AppLogger.warning(
                    "task_store_corruption",
                    "Primary task payload was invalid; fallback payload was used."
                )
            }
            decoded.tasks
        }
        .stateIn(storageScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, emptyList())

    override suspend fun addTask(title: String, priority: TaskPriority) {
        val sanitizedTitle = title.trim()
        if (sanitizedTitle.isBlank()) return
        mutateTasks { current ->
            current + TaskItem(
                id = UUID.randomUUID().toString(),
                title = sanitizedTitle,
                priority = priority,
                updatedAtEpochMs = nowEpochMillis()
            )
        }
    }

    override suspend fun addParsedTasks(tasks: List<ParsedTask>) {
        val additions = tasks.mapNotNull { parsed ->
            val title = parsed.title.trim()
            if (title.isBlank()) return@mapNotNull null
            TaskItem(
                id = UUID.randomUUID().toString(),
                title = title,
                priority = parsed.priority.toTaskPriority(),
                updatedAtEpochMs = nowEpochMillis()
            )
        }
        if (additions.isEmpty()) return
        mutateTasks { current ->
            current + additions
        }
    }

    override suspend fun toggleTask(taskId: String) {
        mutateTasks { current ->
            current.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        completed = !task.completed,
                        updatedAtEpochMs = nowEpochMillis(),
                        syncState = TaskSyncState.PENDING_RETRY
                    )
                } else {
                    task
                }
            }
        }
    }

    override suspend fun deleteTask(taskId: String) {
        mutateTasks { current ->
            current.filterNot { it.id == taskId }
        }
    }

    private suspend fun mutateTasks(mutation: (List<TaskItem>) -> List<TaskItem>) {
        dataStore.edit { preferences ->
            val current = decodeFromPreferences(preferences)
            val updated = mutation(current.tasks)
            val payload = json.encodeToString(
                StoredTaskList.serializer(),
                StoredTaskList(updated.map { it.toStoredTask() })
            )
            preferences[TASKS_KEY] = payload
            preferences[TASKS_BACKUP_KEY] = payload
        }
    }

    private fun decodeFromPreferences(preferences: Preferences): DecodedTaskPayload {
        val primary = decodeTasks(preferences[TASKS_KEY].orEmpty())
        if (primary != null) {
            return DecodedTaskPayload(tasks = primary, usedFallback = false)
        }

        val backup = decodeTasks(preferences[TASKS_BACKUP_KEY].orEmpty())
        return if (backup != null) {
            DecodedTaskPayload(tasks = backup, usedFallback = true)
        } else {
            DecodedTaskPayload(tasks = emptyList(), usedFallback = false)
        }
    }

    private fun decodeTasks(payload: String): List<TaskItem>? {
        if (payload.isBlank()) return emptyList()
        return runCatching {
            json.decodeFromString(StoredTaskList.serializer(), payload).tasks.map { stored ->
                TaskItem(
                    id = stored.id,
                    title = stored.title,
                    priority = stored.priority,
                    completed = stored.completed,
                    updatedAtEpochMs = stored.updatedAtEpochMs,
                    syncState = stored.syncState
                )
            }
        }.onFailure {
            AppLogger.warning(
                "task_store_decode_failure",
                "Failed to decode persisted task payload: ${it.message.orEmpty()}"
            )
        }.getOrNull()
    }
}

private data class DecodedTaskPayload(
    val tasks: List<TaskItem>,
    val usedFallback: Boolean
)

@Serializable
private data class StoredTask(
    val id: String,
    val title: String,
    val priority: TaskPriority,
    val completed: Boolean,
    val updatedAtEpochMs: Long,
    val syncState: TaskSyncState
)

@Serializable
private data class StoredTaskList(
    val tasks: List<StoredTask>
)

private fun TaskItem.toStoredTask(): StoredTask {
    return StoredTask(
        id = id,
        title = title,
        priority = priority,
        completed = completed,
        updatedAtEpochMs = updatedAtEpochMs,
        syncState = syncState
    )
}

internal val TASKS_KEY = stringPreferencesKey("tasks_json")
internal val TASKS_BACKUP_KEY = stringPreferencesKey("tasks_json_backup")

private fun String.toTaskPriority(): TaskPriority {
    return when (uppercase()) {
        "HIGH" -> TaskPriority.HIGH
        "LOW" -> TaskPriority.LOW
        else -> TaskPriority.NORMAL
    }
}

private fun nowEpochMillis(): Long = System.currentTimeMillis()
