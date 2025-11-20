package com.assembled.chat.models

/**
 * Sealed class representing possible chat errors.
 */
sealed class ChatError(open val message: String) {
    
    /**
     * Chat is not initialized. Call initialize() first.
     */
    data object NotReady : ChatError("Chat is not initialized. Call initialize() first.")
    
    /**
     * Chat initialization failed.
     */
    data class InitializationFailed(override val message: String) : ChatError(message)
    
    /**
     * Error in JavaScript bridge communication.
     */
    data class BridgeError(override val message: String) : ChatError(message)
    
    /**
     * Error loading chat page in WebView.
     */
    data class LoadError(
        override val message: String,
        val errorCode: Int? = null
    ) : ChatError(message)
    
    /**
     * Network connectivity error.
     */
    data class NetworkError(override val message: String) : ChatError(message)
    
    /**
     * Invalid configuration provided.
     */
    data class InvalidConfiguration(override val message: String) : ChatError(message)
    
    /**
     * Chat is already initialized.
     */
    data object AlreadyInitialized : ChatError("Chat is already initialized.")
    
    /**
     * Unknown error occurred.
     */
    data class Unknown(override val message: String) : ChatError(message)
    
    override fun toString(): String {
        return when (this) {
            is NotReady -> "ChatError.NotReady: $message"
            is InitializationFailed -> "ChatError.InitializationFailed: $message"
            is BridgeError -> "ChatError.BridgeError: $message"
            is LoadError -> {
                val codeStr = errorCode?.let { " (code: $it)" } ?: ""
                "ChatError.LoadError: $message$codeStr"
            }
            is NetworkError -> "ChatError.NetworkError: $message"
            is InvalidConfiguration -> "ChatError.InvalidConfiguration: $message"
            is AlreadyInitialized -> "ChatError.AlreadyInitialized: $message"
            is Unknown -> "ChatError.Unknown: $message"
        }
    }
}

