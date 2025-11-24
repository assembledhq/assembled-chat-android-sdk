package com.assembled.chat.models

/**
 * Sealed class representing possible chat errors.
 */
sealed class ChatError(open val message: String) {
    
    /**
     * Chat is not initialized. Call initialize() first.
     */
    data object NotReady : ChatError("Chat is not initialized. Call initialize() first.") {
        override fun toString(): String = "ChatError.NotReady: $message"
    }
    
    /**
     * Chat initialization failed.
     */
    data class InitializationFailed(override val message: String) : ChatError(message) {
        override fun toString(): String = "ChatError.InitializationFailed: $message"
    }
    
    /**
     * Error in JavaScript bridge communication.
     */
    data class BridgeError(override val message: String) : ChatError(message) {
        override fun toString(): String = "ChatError.BridgeError: $message"
    }
    
    /**
     * Error loading chat page in WebView.
     */
    data class LoadError(
        override val message: String,
        val errorCode: Int? = null
    ) : ChatError(message) {
        override fun toString(): String {
            val codeStr = errorCode?.let { " (code: $it)" } ?: ""
            return "ChatError.LoadError: $message$codeStr"
        }
    }
    
    /**
     * Network connectivity error.
     */
    data class NetworkError(override val message: String) : ChatError(message) {
        override fun toString(): String = "ChatError.NetworkError: $message"
    }
    
    /**
     * Invalid configuration provided.
     */
    data class InvalidConfiguration(override val message: String) : ChatError(message) {
        override fun toString(): String = "ChatError.InvalidConfiguration: $message"
    }
    
    /**
     * Chat is already initialized.
     */
    data object AlreadyInitialized : ChatError("Chat is already initialized.") {
        override fun toString(): String = "ChatError.AlreadyInitialized: $message"
    }
    
    /**
     * Unknown error occurred.
     */
    data class Unknown(override val message: String) : ChatError(message) {
        override fun toString(): String = "ChatError.Unknown: $message"
    }
}

