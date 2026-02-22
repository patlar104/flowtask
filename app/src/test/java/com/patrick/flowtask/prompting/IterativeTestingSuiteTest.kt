package com.patrick.flowtask.prompting

import org.junit.Assert.assertEquals
import org.junit.Test

class IterativeTestingSuiteTest {

    @Test
    fun `runSuite processes all test cases and returns results`() {
        // Arrange
        val fakeAiClient = object : AiClient {
            override fun generateContent(prompt: String, config: AiConfig): String {
                return if (prompt.contains("Task A")) {
                    """{ "tasks": [{ "title": "Task A" }] }"""
                } else {
                    """{ "tasks": [] }"""
                }
            }
        }
        val suite = IterativeTestingSuite(fakeAiClient)
        val testCases = listOf(
            PromptTestCase(
                name = "Extract Task A",
                prompt = "Please extract Task A",
                expectedCondition = { output -> output.contains("Task A") }
            ),
            PromptTestCase(
                name = "Extract Empty",
                prompt = "Nothing here",
                expectedCondition = { output -> output.contains("tasks") }
            )
        )

        // Act
        val results = suite.runSuite(testCases, AiConfig())

        // Assert
        assertEquals(2, results.size)
        assertEquals(true, results[0].passed)
        assertEquals(true, results[1].passed)
    }

    @Test
    fun `runSuite detects failing test cases`() {
        // Arrange
        val fakeAiClient = object : AiClient {
            override fun generateContent(prompt: String, config: AiConfig): String {
                return "unexpected output"
            }
        }
        val suite = IterativeTestingSuite(fakeAiClient)
        val testCases = listOf(
            PromptTestCase(
                name = "Failing case",
                prompt = "test",
                expectedCondition = { output -> output == "expected" }
            )
        )

        // Act
        val results = suite.runSuite(testCases, AiConfig())

        // Assert
        assertEquals(1, results.size)
        assertEquals(false, results[0].passed)
    }
}
