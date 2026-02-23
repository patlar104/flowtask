package com.patrick.flowtask.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistentTaskRepositoryTest {
    private val Context.testTaskDataStore by preferencesDataStore(name = TASK_REPOSITORY_STORE_NAME)

    private suspend fun resetStore(context: Context) {
        context.testTaskDataStore.edit { prefs ->
            prefs.remove(TASKS_KEY)
            prefs.remove(TASKS_BACKUP_KEY)
        }
    }

    @Test
    fun tasks_persist_across_repository_instances() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        resetStore(context)
        val repoA = PersistentTaskRepository(context)

        repoA.addTask("Persisted task")

        val repoB = PersistentTaskRepository(context)
        val taskTitles = repoB.tasks.first { tasks -> tasks.any { it.title == "Persisted task" } }.map { it.title }
        assertTrue(taskTitles.contains("Persisted task"))
    }

    @Test
    fun concurrent_adds_do_not_lose_updates() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        resetStore(context)
        val repo = PersistentTaskRepository(context)

        val jobs = (1..40).map { index ->
            launch {
                repo.addTask("Task-$index")
            }
        }
        jobs.forEach { it.join() }

        val count = repo.tasks.first().size
        assertEquals(40, count)
    }

    @Test
    fun corrupted_primary_payload_uses_backup_payload() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        resetStore(context)

        val validBackup = """{"tasks":[{"id":"1","title":"Recovered","priority":"NORMAL","completed":false,"updatedAtEpochMs":1,"syncState":"LOCAL_ONLY"}]}"""
        context.testTaskDataStore.edit { prefs ->
            prefs[TASKS_KEY] = "{not-json"
            prefs[TASKS_BACKUP_KEY] = validBackup
        }

        val repo = PersistentTaskRepository(context)
        val tasks = repo.tasks.first()
        assertEquals(1, tasks.size)
        assertEquals("Recovered", tasks.first().title)
    }
}
