package com.patrick.flowtask.data

import com.patrick.flowtask.prompting.ParsedTask
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskRepositoryTest {

    @Test
    fun `addTask stores task in state flow`() = runBlocking {
        val repository = InMemoryTaskRepository()

        repository.addTask("Write docs", TaskPriority.HIGH)

        val tasks = repository.tasks.value
        assertEquals(1, tasks.size)
        assertEquals("Write docs", tasks.first().title)
        assertEquals(TaskPriority.HIGH, tasks.first().priority)
    }

    @Test
    fun `addParsedTasks maps priorities and ignores blank titles`() = runBlocking {
        val repository = InMemoryTaskRepository()
        val parsedTasks = listOf(
            ParsedTask(title = "Valid task", priority = "LOW"),
            ParsedTask(title = "   ", priority = "HIGH")
        )

        repository.addParsedTasks(parsedTasks)

        val tasks = repository.tasks.value
        assertEquals(1, tasks.size)
        assertEquals(TaskPriority.LOW, tasks.first().priority)
    }

    @Test
    fun `toggle and delete update repository state`() = runBlocking {
        val repository = InMemoryTaskRepository()
        repository.addTask("Task")
        val id = repository.tasks.value.first().id

        repository.toggleTask(id)
        assertTrue(repository.tasks.value.first().completed)

        repository.deleteTask(id)
        assertTrue(repository.tasks.value.isEmpty())
    }
}
