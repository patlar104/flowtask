package com.patrick.flowtask.domain

import com.patrick.flowtask.prompting.AiConfig
import kotlinx.coroutines.delay

class FakeRuntimeAiClient : RuntimeAiClient {
    override suspend fun generateContent(prompt: String, config: AiConfig): AiClientResult {
        delay(250)
        val rawInput = prompt.substringAfter("User says:", "").trim()
        val title = rawInput.ifBlank { "Review my day" }
        return AiClientResult.Success(
            """
                {
                  "tasks": [
                    { "title": "$title", "priority": "NORMAL" }
                  ]
                }
            """.trimIndent()
        )
    }
}
