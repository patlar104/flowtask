package com.patrick.flowtask.prompting

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Assert.assertThrows

class AiConfigTest {

    @Test
    fun `AiConfig initializes with default valid values`() {
        val config = AiConfig()
        assertEquals(0.7f, config.temperature)
        assertEquals(1024, config.maxTokens)
    }

    @Test
    fun `AiConfig throws exception for temperature out of bounds`() {
        assertThrows(IllegalArgumentException::class.java) {
            AiConfig(temperature = -0.1f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AiConfig(temperature = 2.1f)
        }
    }

    @Test
    fun `AiConfig throws exception for invalid maxTokens`() {
        assertThrows(IllegalArgumentException::class.java) {
            AiConfig(maxTokens = 0)
        }
    }
}
