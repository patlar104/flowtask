package com.patrick.flowtask.prompting

import org.junit.Assert.assertEquals
import org.junit.Test

class PromptInjectorTest {

    @Test
    fun `inject correctly formats template with context and input`() {
        val template = PromptTemplate(
            id = "test_template",
            systemInstruction = "You are an assistant.",
            promptStructure = "Active tasks: {activeTaskCount}. Time: {timeOfDay}. User says: {userInput}"
        )
        val context = ContextState(
            activeTaskCount = 3,
            timeOfDay = "Afternoon",
            recentActivity = listOf("did something")
        )
        
        val injector = PromptInjector()
        val result = injector.inject(template, context, "Add a new task")
        
        val expected = "Active tasks: 3. Time: Afternoon. User says: Add a new task"
        assertEquals(expected, result)
    }

    @Test
    fun `inject handles missing template variables gracefully`() {
        val template = PromptTemplate(
            id = "test_template",
            systemInstruction = "You are an assistant.",
            promptStructure = "User says: {userInput}."
        )
        val context = ContextState(
            activeTaskCount = 3,
            timeOfDay = "Afternoon",
            recentActivity = emptyList()
        )
        
        val injector = PromptInjector()
        val result = injector.inject(template, context, "Hello")
        
        val expected = "User says: Hello."
        assertEquals(expected, result)
    }
}
