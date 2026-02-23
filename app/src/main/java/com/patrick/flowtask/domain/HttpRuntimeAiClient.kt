package com.patrick.flowtask.domain

import com.patrick.flowtask.prompting.AiConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class HttpRuntimeAiClient(
    private val backendUrl: String,
    private val sessionTokenProvider: () -> String? = { null }
) : RuntimeAiClient {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generateContent(prompt: String, config: AiConfig): AiClientResult {
        if (backendUrl.isBlank()) {
            return AiClientResult.Failure(
                AiClientFailure(
                    type = AiClientErrorType.MISCONFIGURED,
                    message = "AI backend is not configured. Add AI_BACKEND_URL."
                )
            )
        }

        return withContext(Dispatchers.IO) {
            val connection = (URL(backendUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 25_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                val sessionToken = sessionTokenProvider()
                if (!sessionToken.isNullOrBlank()) {
                    setRequestProperty("Authorization", "Bearer $sessionToken")
                }
            }

            try {
                val payload = buildJsonObject {
                    put("input", prompt)
                    put("temperature", config.temperature)
                    put("maxTokens", config.maxTokens)
                    put("systemInstruction", config.systemInstruction)
                }.toString()

                connection.outputStream.use { it.write(payload.toByteArray()) }
                val status = connection.responseCode
                val body = readBody(connection, status)

                when {
                    status == 401 || status == 403 -> AiClientResult.Failure(
                        AiClientFailure(
                            type = AiClientErrorType.UNAUTHORIZED,
                            message = "AI authorization failed."
                        )
                    )
                    status >= 500 -> AiClientResult.Failure(
                        AiClientFailure(
                            type = AiClientErrorType.SERVER,
                            message = "AI server error ($status)."
                        )
                    )
                    status >= 400 -> AiClientResult.Failure(
                        AiClientFailure(
                            type = AiClientErrorType.NETWORK,
                            message = "AI request failed ($status)."
                        )
                    )
                    else -> AiClientResult.Success(extractContent(body))
                }
            } catch (_: SocketTimeoutException) {
                AiClientResult.Failure(
                    AiClientFailure(
                        type = AiClientErrorType.TIMEOUT,
                        message = "AI request timed out."
                    )
                )
            } catch (_: IOException) {
                AiClientResult.Failure(
                    AiClientFailure(
                        type = AiClientErrorType.NETWORK,
                        message = "Could not reach AI backend."
                    )
                )
            } catch (_: Exception) {
                AiClientResult.Failure(
                    AiClientFailure(
                        type = AiClientErrorType.UNKNOWN,
                        message = "Unexpected AI client error."
                    )
                )
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun readBody(connection: HttpURLConnection, status: Int): String {
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        return stream?.bufferedReader()?.use { it.readText() }.orEmpty()
    }

    private fun extractContent(body: String): String {
        if (body.isBlank()) return body

        return try {
            val element = json.parseToJsonElement(body)
            val root = element.jsonObject
            root["content"]?.jsonPrimitive?.content
                ?: root["output"]?.jsonPrimitive?.content
                ?: root["choices"]?.jsonArray
                    ?.firstOrNull()
                    ?.jsonObject
                    ?.get("message")
                    ?.jsonObject
                    ?.get("content")
                    ?.jsonPrimitive
                    ?.content
                ?: body
        } catch (_: Exception) {
            body
        }
    }
}
