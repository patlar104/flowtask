package com.patrick.flowtask.prompting

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
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

@Serializable
data class ParseTasksError(
    val reason: String
)

@Serializable
data class ParseTasksDetailedResult(
    val tasks: List<ParsedTask>,
    val error: ParseTasksError? = null
)

class StructuredResponseParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun parseTasks(response: String): List<ParsedTask> {
        return parseTasksDetailed(response).tasks
    }

    fun parseTasksDetailed(response: String): ParseTasksDetailedResult {
        val jsonString = extractJsonFromMarkdown(response) ?: response

        return try {
            val parsedResponse = json.decodeFromString<ParsedTasksResponse>(jsonString)
            ParseTasksDetailedResult(tasks = parsedResponse.tasks)
        } catch (e: SerializationException) {
            ParseTasksDetailedResult(
                tasks = emptyList(),
                error = ParseTasksError(
                    reason = "Invalid structured response: ${e.message ?: "serialization error"}"
                )
            )
        } catch (e: IllegalArgumentException) {
            ParseTasksDetailedResult(
                tasks = emptyList(),
                error = ParseTasksError(
                    reason = "Invalid parsed values: ${e.message ?: "validation error"}"
                )
            )
        }
    }

    private fun extractJsonFromMarkdown(markdown: String): String? {
        val regex = Regex("""```json\s*([\s\S]*?)\s*```""")
        val match = regex.find(markdown)
        return match?.groupValues?.get(1)
    }
}
