package com.assembled.chat

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.FrameLayout
import com.assembled.chat.models.AssembledChatConfiguration
import com.assembled.chat.models.ChatError

/**
 * Custom view wrapper for Assembled Chat that can be used in XML layouts.
 *
 * Usage in XML:
 * ```xml
 * <com.assembled.chat.AssembledChatView
 *     android:id="@+id/chatView"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent" />
 * ```
 *
 * Usage in code:
 * ```
 * val chatView = findViewById<AssembledChatView>(R.id.chatView)
 * val config = AssembledChatConfiguration(companyId = "your-company-id")
 * chatView.initialize(config)
 * ```
 */
class AssembledChatView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), AssembledChatListener {

    private var chat: AssembledChat? = null
    private var disableLauncher = false
    private val mainHandler = Handler(Looper.getMainLooper())
    var listener: AssembledChatListener? = null

    /**
     * Initialize the chat view with configuration.
     *
     * @param configuration Chat configuration
     */
    fun initialize(configuration: AssembledChatConfiguration) {
        // Clean up existing chat if any
        chat?.destroy()
        disableLauncher = configuration.disableLauncher

        // Create new chat instance
        chat = AssembledChat(configuration).apply {
            this.listener = this@AssembledChatView
            initialize(context)

            // Add WebView to this view
            getWebView()?.let { webView ->
                removeAllViews()
                addView(webView)
            }
        }
    }

    /**
     * Open the chat widget.
     */
    fun open() {
        chat?.open() ?: listener?.onError(ChatError.NotReady)
    }

    /**
     * Close the chat widget.
     */
    fun close() {
        chat?.close() ?: listener?.onError(ChatError.NotReady)
    }

    /**
     * Get the underlying AssembledChat instance.
     */
    fun getChat(): AssembledChat? = chat

    /**
     * Check if chat is ready.
     */
    fun isReady(): Boolean = chat?.isReady() == true

    /**
     * Clean up resources.
     */
    fun destroy() {
        chat?.destroy()
        chat = null
        removeAllViews()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Auto-cleanup when view is removed
        destroy()
    }

    // AssembledChatListener implementation - forward to external listener
    override fun onChatReady() {
        listener?.onChatReady()

        // Auto-open chat when disableLauncher is true
        // Must post to main thread since this callback runs on JavaBridge thread
        if (disableLauncher) {
            mainHandler.post {
                chat?.open()
            }
        }
    }

    override fun onChatOpened() {
        listener?.onChatOpened()
    }

    override fun onChatClosed() {
        listener?.onChatClosed()
    }

    override fun onError(error: ChatError) {
        listener?.onError(error)
    }

    override fun onDebug(message: String) {
        listener?.onDebug(message)
    }

    override fun onNewMessage(messageCount: Int) {
        listener?.onNewMessage(messageCount)
    }
}

