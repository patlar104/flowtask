package com.patrick.flowtask.prompting

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ParsedTask(
    val title: String,
    val priority: String = "NORMAL"
)

@Serializable
private data class ParsedTasksResponse(
    val tasks: List<ParsedTask>
)

class StructuredResponseParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun parseTasks(response: String): List<ParsedTask> {
        val jsonString = extractJsonFromMarkdown(response) ?: response
        
        return try {
            val parsedResponse = json.decodeFromString<ParsedTasksResponse>(jsonString)
            parsedResponse.tasks
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun extractJsonFromMarkdown(markdown: String): String? {
        val regex = Regex("""```json\s*([\s\S]*?)\s*```""")
        val match = regex.find(markdown)
        return match?.groupValues?.get(1)
    }
}
