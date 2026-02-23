package com.patrick.flowtask.domain

import com.patrick.flowtask.prompting.AiConfig
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpRuntimeAiClientTest {

    @Test
    fun `returns misconfigured when backend url missing`() = runBlocking {
        val client = HttpRuntimeAiClient(backendUrl = "")

        val result = client.generateContent("hello", AiConfig())
        assertTrue(result is AiClientResult.Failure)
        result as AiClientResult.Failure
        assertEquals(AiClientErrorType.MISCONFIGURED, result.error.type)
    }

    @Test
    fun `maps unauthorized status to unauthorized failure`() = runBlocking {
        val server = createServer(statusCode = 401, response = """{"error":"unauthorized"}""")
        try {
            val client = HttpRuntimeAiClient(backendUrl = "http://localhost:${server.address.port}/ai")
            val result = client.generateContent("hello", AiConfig())
            assertTrue(result is AiClientResult.Failure)
            result as AiClientResult.Failure
            assertEquals(AiClientErrorType.UNAUTHORIZED, result.error.type)
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `extracts content from successful backend payload`() = runBlocking {
        val server = createServer(statusCode = 200, response = """{"content":"{}"}""")
        try {
            val client = HttpRuntimeAiClient(backendUrl = "http://localhost:${server.address.port}/ai")
            val result = client.generateContent("hello", AiConfig())
            assertTrue(result is AiClientResult.Success)
            result as AiClientResult.Success
            assertEquals("{}", result.content)
        } finally {
            server.stop(0)
        }
    }

    private fun createServer(statusCode: Int, response: String): HttpServer {
        val server = HttpServer.create(InetSocketAddress(0), 0)
        server.createContext("/ai") { exchange: HttpExchange ->
            val body = response.toByteArray()
            exchange.sendResponseHeaders(statusCode, body.size.toLong())
            exchange.responseBody.use { output ->
                output.write(body)
            }
        }
        server.start()
        return server
    }
}
