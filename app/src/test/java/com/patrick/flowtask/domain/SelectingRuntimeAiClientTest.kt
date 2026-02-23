package com.patrick.flowtask.domain

import com.patrick.flowtask.prompting.AiConfig
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SelectingRuntimeAiClientTest {

    @Test
    fun `uses fake client when toggle enabled`() = runBlocking {
        val fake = object : RuntimeAiClient {
            override suspend fun generateContent(prompt: String, config: AiConfig): AiClientResult {
                return AiClientResult.Success("fake")
            }
        }
        val http = object : RuntimeAiClient {
            override suspend fun generateContent(prompt: String, config: AiConfig): AiClientResult {
                return AiClientResult.Success("http")
            }
        }

        val client = SelectingRuntimeAiClient(
            useFakeClient = true,
            fakeClient = fake,
            httpClient = http
        )

        val result = client.generateContent("hello", AiConfig())
        assertEquals("fake", (result as AiClientResult.Success).content)
    }

    @Test
    fun `uses backend client when toggle disabled`() = runBlocking {
        val fake = object : RuntimeAiClient {
            override suspend fun generateContent(prompt: String, config: AiConfig): AiClientResult {
                return AiClientResult.Success("fake")
            }
        }
        val http = object : RuntimeAiClient {
            override suspend fun generateContent(prompt: String, config: AiConfig): AiClientResult {
                return AiClientResult.Success("http")
            }
        }

        val client = SelectingRuntimeAiClient(
            useFakeClient = false,
            fakeClient = fake,
            httpClient = http
        )

        val result = client.generateContent("hello", AiConfig())
        assertEquals("http", (result as AiClientResult.Success).content)
    }
}
