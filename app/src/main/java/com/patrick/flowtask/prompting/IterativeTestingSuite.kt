package com.patrick.flowtask.prompting

interface AiClient {
    fun generateContent(prompt: String, config: AiConfig): String
}

data class PromptTestCase(
    val name: String,
    val prompt: String,
    val expectedCondition: (String) -> Boolean
)

data class PromptTestResult(
    val testCase: PromptTestCase,
    val passed: Boolean,
    val actualOutput: String
)

class IterativeTestingSuite(private val aiClient: AiClient) {
    fun runSuite(testCases: List<PromptTestCase>, config: AiConfig): List<PromptTestResult> {
        return testCases.map { testCase ->
            val output = aiClient.generateContent(testCase.prompt, config)
            val passed = try {
                testCase.expectedCondition(output)
            } catch (e: Exception) {
                false
            }
            PromptTestResult(testCase, passed, output)
        }
    }
}
