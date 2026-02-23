package com.patrick.flowtask.domain

import com.patrick.flowtask.prompting.AiConfig

enum class AiClientErrorType {
    MISCONFIGURED,
    NETWORK,
    TIMEOUT,
    UNAUTHORIZED,
    SERVER,
    UNKNOWN
}

data class AiClientFailure(
    val type: AiClientErrorType,
    val message: String
)

sealed interface AiClientResult {
    data class Success(val content: String) : AiClientResult
    data class Failure(val error: AiClientFailure) : AiClientResult
}

interface RuntimeAiClient {
    suspend fun generateContent(prompt: String, config: AiConfig): AiClientResult
}
