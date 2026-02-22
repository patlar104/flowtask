package com.patrick.flowtask.prompting

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class DataModelsTest {

    @Test
    fun `ContextState serializes and deserializes correctly`() {
        val state = ContextState(
            activeTaskCount = 5,
            timeOfDay = "Morning",
            recentActivity = listOf("Created task A", "Completed task B")
        )
        
        val jsonString = Json.encodeToString(state)
        val decoded = Json.decodeFromString<ContextState>(jsonString)
        
        assertEquals(state, decoded)
    }

    @Test
    fun `PromptTemplate serializes and deserializes correctly`() {
        val template = PromptTemplate(
            id = "task_creation",
            systemInstruction = "You are a helpful task assistant.",
            promptStructure = "Current tasks: {task_count}. User request: {user_input}"
        )
        
        val jsonString = Json.encodeToString(template)
        val decoded = Json.decodeFromString<PromptTemplate>(jsonString)
        
        assertEquals(template, decoded)
    }
}
