package com.patrick.flowtask.prompting

import kotlinx.serialization.Serializable

@Serializable
data class ContextState(
    val activeTaskCount: Int,
    val timeOfDay: String,
    val recentActivity: List<String> = emptyList()
)

@Serializable
data class PromptTemplate(
    val id: String,
    val systemInstruction: String,
    val promptStructure: String
)
