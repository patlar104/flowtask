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
}
