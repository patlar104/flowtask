package com.patrick.flowtask.domain

import com.patrick.flowtask.prompting.AiConfig
import com.patrick.flowtask.prompting.ContextState
import com.patrick.flowtask.prompting.PromptInjector
import com.patrick.flowtask.prompting.StructuredResponseParser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ConversationUseCaseTest {

    @Test
    fun `handleUserInput returns parsed tasks on valid response`() = runBlocking {
        val aiClient = object : RuntimeAiClient {
            override suspend fun generateContent(prompt: String, config: AiConfig): AiClientResult {
                return AiClientResult.Success("""{"tasks":[{"title":"Ship feature","priority":"HIGH"}]}""")
            }
        }
        val useCase = ConversationUseCase(
            aiClient = aiClient,
            promptInjector = PromptInjector(),
            parser = StructuredResponseParser()
        )

        val result = useCase.handleUserInput(
            userInput = "Ship feature",
            context = ContextState(activeTaskCount = 2, timeOfDay = "Morning")
        )

        assertEquals(1, result.tasks.size)
        assertEquals("Ship feature", result.tasks.first().title)
        assertNull(result.error)
        assertNull(result.clientError)
    }

    @Test
    fun `handleUserInput returns error on invalid response`() = runBlocking {
        val aiClient = object : RuntimeAiClient {
            override suspend fun generateContent(prompt: String, config: AiConfig): AiClientResult {
                return AiClientResult.Success("not structured output")
            }
        }
        val useCase = ConversationUseCase(
            aiClient = aiClient,
            promptInjector = PromptInjector(),
            parser = StructuredResponseParser()
        )

        val result = useCase.handleUserInput(
            userInput = "Invalid",
            context = ContextState(activeTaskCount = 0, timeOfDay = "Night")
        )

        assertEquals(0, result.tasks.size)
        assertNotNull(result.error)
    }

    @Test
    fun `handleUserInput returns clientError when runtime client fails`() = runBlocking {
        val aiClient = object : RuntimeAiClient {
            override suspend fun generateContent(prompt: String, config: AiConfig): AiClientResult {
                return AiClientResult.Failure(
                    AiClientFailure(
                        type = AiClientErrorType.TIMEOUT,
                        message = "AI request timed out."
                    )
                )
            }
        }
        val useCase = ConversationUseCase(
            aiClient = aiClient,
            promptInjector = PromptInjector(),
            parser = StructuredResponseParser()
        )

        val result = useCase.handleUserInput(
            userInput = "Any",
            context = ContextState(activeTaskCount = 0, timeOfDay = "Night")
        )

        assertEquals(0, result.tasks.size)
        assertNull(result.error)
        assertNotNull(result.clientError)
    }
}
