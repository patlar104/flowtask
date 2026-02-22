package com.patrick.flowtask.prompting

data class AiConfig(
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1024,
    val systemInstruction: String = "You are a helpful task management assistant."
) {
    init {
        require(temperature in 0.0f..2.0f) { "Temperature must be between 0.0 and 2.0" }
        require(maxTokens > 0) { "Max tokens must be greater than 0" }
    }
}
