package com.assembled.chat

import com.assembled.chat.models.ChatError

/**
 * Listener interface for Assembled Chat events.
 */
interface AssembledChatListener {
    /**
     * Called when the chat is successfully initialized and ready to use.
     */
    fun onChatReady() {}

    /**
     * Called when the chat widget is opened.
     */
    fun onChatOpened() {}

    /**
     * Called when the chat widget is closed.
     */
    fun onChatClosed() {}

    /**
     * Called when an error occurs.
     *
     * @param error The error that occurred
     */
    fun onError(error: ChatError) {}

    /**
     * Called with debug messages when debug mode is enabled.
     *
     * @param message Debug message
     */
    fun onDebug(message: String) {}

    /**
     * Called when a new message is received (future enhancement).
     *
     * @param messageCount Number of unread messages
     */
    fun onNewMessage(messageCount: Int) {}
}

