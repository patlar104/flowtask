package com.patrick.flowtask.domain

import com.patrick.flowtask.prompting.AiConfig
import com.patrick.flowtask.prompting.ContextState
import com.patrick.flowtask.prompting.ParseTasksError
import com.patrick.flowtask.prompting.ParsedTask
import com.patrick.flowtask.prompting.PromptInjector
import com.patrick.flowtask.prompting.PromptTemplate
import com.patrick.flowtask.prompting.StructuredResponseParser

data class ConversationResult(
    val tasks: List<ParsedTask>,
    val error: ParseTasksError? = null,
    val clientError: AiClientFailure? = null,
    val rawResponse: String
)

class ConversationUseCase(
    private val aiClient: RuntimeAiClient,
    private val promptInjector: PromptInjector,
    private val parser: StructuredResponseParser,
    private val aiConfig: AiConfig = AiConfig()
) {
    private val taskPromptTemplate = PromptTemplate(
        id = "task_create_v1",
        systemInstruction = "You are a task assistant. Return strict JSON only.",
        promptStructure = """
            Active tasks: {activeTaskCount}
            Time of day: {timeOfDay}
            User says: {userInput}
            Return JSON in shape: {"tasks":[{"title":"...", "priority":"LOW|NORMAL|HIGH"}]}
        """.trimIndent()
    )

    suspend fun handleUserInput(userInput: String, context: ContextState): ConversationResult {
        val prompt = promptInjector.inject(taskPromptTemplate, context, userInput)
        return when (val response = aiClient.generateContent(prompt, aiConfig)) {
            is AiClientResult.Success -> {
                val parsed = parser.parseTasksDetailed(response.content)
                ConversationResult(
                    tasks = parsed.tasks,
                    error = parsed.error,
                    rawResponse = response.content
                )
            }
            is AiClientResult.Failure -> {
                ConversationResult(
                    tasks = emptyList(),
                    clientError = response.error,
                    rawResponse = ""
                )
            }
        }
    }
}
