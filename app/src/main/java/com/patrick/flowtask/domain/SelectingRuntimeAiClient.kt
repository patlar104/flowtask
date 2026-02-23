package com.patrick.flowtask.domain

import com.patrick.flowtask.prompting.AiConfig

class SelectingRuntimeAiClient(
    private val useFakeClient: Boolean,
    private val fakeClient: RuntimeAiClient,
    private val httpClient: RuntimeAiClient
) : RuntimeAiClient {
    override suspend fun generateContent(prompt: String, config: AiConfig): AiClientResult {
        return if (useFakeClient) {
            fakeClient.generateContent(prompt, config)
        } else {
            httpClient.generateContent(prompt, config)
        }
    }
}
