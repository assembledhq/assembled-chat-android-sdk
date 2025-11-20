package com.assembled.chat.network

import android.util.Log
import android.webkit.JavascriptInterface
import com.assembled.chat.AssembledChatListener
import com.assembled.chat.models.ChatError

/**
 * JavaScript bridge for bidirectional communication between WebView and native code.
 *
 * This class is exposed to JavaScript via @JavascriptInterface and handles messages
 * from the web-based chat widget.
 */
internal class MessageBridge(
    private val listener: AssembledChatListener?,
    private val debug: Boolean = false
) {
    companion object {
        private const val TAG = "AssembledChat"
    }

    /**
     * Called from JavaScript when the chat widget is ready.
     */
    @JavascriptInterface
    fun onReady() {
        if (debug) {
            Log.d(TAG, "Bridge: onReady called from JavaScript")
        }
        listener?.onChatReady()
    }

    /**
     * Called from JavaScript when the chat widget is opened.
     */
    @JavascriptInterface
    fun onOpen() {
        if (debug) {
            Log.d(TAG, "Bridge: onOpen called from JavaScript")
        }
        listener?.onChatOpened()
    }

    /**
     * Called from JavaScript when the chat widget is closed.
     */
    @JavascriptInterface
    fun onClose() {
        if (debug) {
            Log.d(TAG, "Bridge: onClose called from JavaScript")
        }
        listener?.onChatClosed()
    }

    /**
     * Called from JavaScript when an error occurs.
     *
     * @param message Error message from JavaScript
     */
    @JavascriptInterface
    fun onError(message: String) {
        if (debug) {
            Log.e(TAG, "Bridge: onError - $message")
        }
        listener?.onError(ChatError.BridgeError(message))
    }

    /**
     * Called from JavaScript with debug messages.
     *
     * @param message Debug message from JavaScript
     */
    @JavascriptInterface
    fun onDebug(message: String) {
        if (debug) {
            Log.d(TAG, "Bridge: $message")
        }
        listener?.onDebug(message)
    }

    /**
     * Called from JavaScript when a new message is received.
     *
     * @param count Number of unread messages
     */
    @JavascriptInterface
    fun onNewMessage(count: Int) {
        if (debug) {
            Log.d(TAG, "Bridge: onNewMessage - count: $count")
        }
        listener?.onNewMessage(count)
    }

    /**
     * Generic message handler for future extensibility.
     *
     * @param type Message type
     * @param data Message data as JSON string
     */
    @JavascriptInterface
    fun postMessage(type: String, data: String) {
        if (debug) {
            Log.d(TAG, "Bridge: postMessage - type: $type, data: $data")
        }
        
        when (type) {
            "ready" -> onReady()
            "open" -> onOpen()
            "close" -> onClose()
            "error" -> onError(data)
            "debug" -> onDebug(data)
            else -> {
                if (debug) {
                    Log.w(TAG, "Bridge: Unknown message type: $type")
                }
            }
        }
    }
}

