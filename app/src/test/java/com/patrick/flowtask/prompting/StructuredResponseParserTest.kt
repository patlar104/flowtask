package com.patrick.flowtask.prompting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StructuredResponseParserTest {

    @Test
    fun `parseTasks extracts tasks from valid JSON`() {
        val jsonResponse = """
            {
                "tasks": [
                    { "title": "Buy groceries", "priority": "HIGH" },
                    { "title": "Call mom", "priority": "MEDIUM" }
                ]
            }
        """.trimIndent()
        
        val parser = StructuredResponseParser()
        val parsedTasks = parser.parseTasks(jsonResponse)
        
        assertEquals(2, parsedTasks.size)
        assertEquals("Buy groceries", parsedTasks[0].title)
        assertEquals("HIGH", parsedTasks[0].priority)
    }

    @Test
    fun `parseTasks extracts tasks from Markdown code block`() {
        val markdownResponse = """
            Here are your tasks:
            ```json
            {
                "tasks": [
                    { "title": "Finish report", "priority": "HIGH" }
                ]
            }
            ```
            Hope this helps!
        """.trimIndent()
        
        val parser = StructuredResponseParser()
        val parsedTasks = parser.parseTasks(markdownResponse)
        
        assertEquals(1, parsedTasks.size)
        assertEquals("Finish report", parsedTasks[0].title)
    }
    
    @Test
    fun `parseTasks returns empty list on invalid input`() {
        val invalidResponse = "This is just some text without structured tasks."
        val parser = StructuredResponseParser()
        val parsedTasks = parser.parseTasks(invalidResponse)
        
        assertTrue(parsedTasks.isEmpty())
    }

    @Test
    fun `parseTasksDetailed returns parsed tasks and no error for valid input`() {
        val jsonResponse = """
            {
                "tasks": [
                    { "title": "Buy groceries", "priority": "HIGH" }
                ]
            }
        """.trimIndent()

        val parser = StructuredResponseParser()
        val result = parser.parseTasksDetailed(jsonResponse)

        assertEquals(1, result.tasks.size)
        assertEquals("Buy groceries", result.tasks[0].title)
        assertTrue(result.error == null)
    }

    @Test
    fun `parseTasksDetailed returns error details for invalid input while parseTasks stays backward compatible`() {
        val invalidResponse = "This is just some text without structured tasks."
        val parser = StructuredResponseParser()

        val detailed = parser.parseTasksDetailed(invalidResponse)
        val legacy = parser.parseTasks(invalidResponse)

        assertTrue(legacy.isEmpty())
        assertTrue(detailed.tasks.isEmpty())
        assertTrue(detailed.error != null)
        assertTrue(detailed.error!!.reason.isNotBlank())
    }
}
