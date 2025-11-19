package com.assembled.chat.models

import kotlin.test.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ChatErrorTest {

    @ParameterizedTest
    @MethodSource("errorMessageTestCases")
    fun `error should have correct message`(testCase: ErrorMessageTestCase) {
        assertEquals(testCase.expectedMessage, testCase.error.message)
    }

    @ParameterizedTest
    @MethodSource("toStringTestCases")
    fun `toString should format error correctly`(testCase: ToStringTestCase) {
        val str = testCase.error.toString()
        testCase.expectedSubstrings.forEach { substring ->
            assertContains(str, substring)
        }
    }

    @Test
    fun `sealed class should allow exhaustive when statements`() {
        val error: ChatError = ChatError.Unknown("Test")
        
        val result = when (error) {
            is ChatError.NotReady -> "not_ready"
            is ChatError.InitializationFailed -> "init_failed"
            is ChatError.BridgeError -> "bridge_error"
            is ChatError.LoadError -> "load_error"
            is ChatError.NetworkError -> "network_error"
            is ChatError.InvalidConfiguration -> "invalid_config"
            is ChatError.AlreadyInitialized -> "already_initialized"
            is ChatError.Unknown -> "unknown"
        }
        
        assertEquals("unknown", result)
    }

    data class ErrorMessageTestCase(
        val error: ChatError,
        val expectedMessage: String,
        val description: String
    ) {
        override fun toString() = description
    }

    data class ToStringTestCase(
        val error: ChatError,
        val expectedSubstrings: List<String>,
        val description: String
    ) {
        override fun toString() = description
    }

    companion object {
        @JvmStatic
        fun errorMessageTestCases() = listOf(
            ErrorMessageTestCase(
                ChatError.NotReady,
                "Chat is not initialized. Call initialize() first.",
                "NotReady"
            ),
            ErrorMessageTestCase(
                ChatError.InitializationFailed("Custom error message"),
                "Custom error message",
                "InitializationFailed"
            ),
            ErrorMessageTestCase(
                ChatError.BridgeError("Bridge communication failed"),
                "Bridge communication failed",
                "BridgeError"
            ),
            ErrorMessageTestCase(
                ChatError.LoadError("Page load failed", 404),
                "Page load failed",
                "LoadError with code"
            ),
            ErrorMessageTestCase(
                ChatError.LoadError("Page load failed"),
                "Page load failed",
                "LoadError without code"
            ),
            ErrorMessageTestCase(
                ChatError.NetworkError("No internet connection"),
                "No internet connection",
                "NetworkError"
            ),
            ErrorMessageTestCase(
                ChatError.InvalidConfiguration("Company ID is required"),
                "Company ID is required",
                "InvalidConfiguration"
            ),
            ErrorMessageTestCase(
                ChatError.AlreadyInitialized,
                "Chat is already initialized.",
                "AlreadyInitialized"
            ),
            ErrorMessageTestCase(
                ChatError.Unknown("Unexpected error occurred"),
                "Unexpected error occurred",
                "Unknown"
            )
        )

        @JvmStatic
        fun toStringTestCases() = listOf(
            ToStringTestCase(
                ChatError.NotReady,
                listOf("ChatError.NotReady", "Chat is not initialized"),
                "NotReady"
            ),
            ToStringTestCase(
                ChatError.InitializationFailed("Test error"),
                listOf("ChatError.InitializationFailed", "Test error"),
                "InitializationFailed"
            ),
            ToStringTestCase(
                ChatError.LoadError("Load failed", 404),
                listOf("ChatError.LoadError", "Load failed", "404"),
                "LoadError with code"
            ),
            ToStringTestCase(
                ChatError.AlreadyInitialized,
                listOf("ChatError.AlreadyInitialized", "already initialized"),
                "AlreadyInitialized"
            )
        )
    }
}
