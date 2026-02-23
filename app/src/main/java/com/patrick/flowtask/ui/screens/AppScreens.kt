package com.patrick.flowtask.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.patrick.flowtask.R
import com.patrick.flowtask.data.TaskItem
import com.patrick.flowtask.ui.viewmodel.ChatMessage
import com.patrick.flowtask.ui.viewmodel.ConversationUiState
import com.patrick.flowtask.ui.viewmodel.HomeUiState
import com.patrick.flowtask.ui.viewmodel.SettingsUiState
import com.patrick.flowtask.ui.viewmodel.TasksUiState

@Composable
fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.home_overview), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.home_total_tasks, state.totalTasks))
        Text(stringResource(R.string.home_completed_tasks, state.completedTasks))
    }
}

@Composable
fun TasksScreen(
    state: TasksUiState,
    onQueryChange: (String) -> Unit,
    onAddTask: (String) -> Unit,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newTaskTitle by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.tasks_title), style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            label = { Text(stringResource(R.string.tasks_search_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = newTaskTitle,
            onValueChange = { newTaskTitle = it },
            label = { Text(stringResource(R.string.tasks_add_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                onAddTask(newTaskTitle)
                newTaskTitle = ""
            },
            modifier = Modifier
                .sizeIn(minHeight = 48.dp)
                .fillMaxWidth()
        ) {
            Text(stringResource(R.string.tasks_save_button))
        }

        if (state.visibleTasks.isEmpty()) {
            Text(stringResource(R.string.tasks_empty))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.visibleTasks, key = TaskItem::id) { task ->
                    TaskRow(
                        task = task,
                        onToggle = { onToggleTask(task.id) },
                        onDelete = { onDeleteTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: TaskItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.title,
                textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onToggle)
                    .semantics { contentDescription = "task-${task.id}" }
            )
            Button(
                onClick = onDelete,
                modifier = Modifier.sizeIn(minHeight = 48.dp)
            ) {
                Text(stringResource(R.string.tasks_delete_button))
            }
        }
    }
}

@Composable
fun ConversationScreen(
    state: ConversationUiState,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var input by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.flow_title), style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text(stringResource(R.string.flow_prompt_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                onSubmit(input)
                input = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 48.dp)
        ) {
            Text(
                if (state.isLoading) {
                    stringResource(R.string.flow_working_button)
                } else {
                    stringResource(R.string.flow_submit_button)
                }
            )
        }
        state.latestError?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.messages) { message ->
                ChatBubble(message = message)
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = if (message.fromUser) "user-message" else "assistant-message" }
    ) {
        Text(
            text = message.content,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.settings_app_name, state.appName))
        Text(stringResource(R.string.settings_ai_surface, state.conversationalFlowName))
        Text(stringResource(R.string.settings_placeholder))
    }
}
