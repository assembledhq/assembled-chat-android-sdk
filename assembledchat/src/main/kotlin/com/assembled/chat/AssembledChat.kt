package com.assembled.chat

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.assembled.chat.models.AssembledChatConfiguration
import com.assembled.chat.models.ChatError
import com.assembled.chat.models.UserData
import com.assembled.chat.network.MessageBridge

/**
 * Main SDK class for Assembled Chat integration.
 *
 * Usage:
 * ```
 * val config = AssembledChatConfiguration(companyId = "your-company-id")
 * val chat = AssembledChat(config)
 * chat.listener = yourListener
 * chat.initialize(context)
 * chat.open()
 * ```
 */
class AssembledChat(private val configuration: AssembledChatConfiguration) {

    companion object {
        private const val TAG = "AssembledChat"
        private const val CHAT_BASE_URL = "https://app.assembledhq.com"
        private const val BRIDGE_NAME = "AssembledChatAndroid"
    }

    /**
     * Listener for chat events.
     */
    var listener: AssembledChatListener? = null

    private var webView: WebView? = null
    private var messageBridge: MessageBridge? = null
    private var isInitialized = false
    private var isInitializing = false

    /**
     * Initialize the chat SDK.
     *
     * @param context Android context
     * @throws ChatError.AlreadyInitialized if already initialized
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun initialize(context: Context) {
        if (isInitialized) {
            listener?.onError(ChatError.AlreadyInitialized)
            return
        }

        if (isInitializing) {
            return
        }

        isInitializing = true

        try {
            if (configuration.debug) {
                Log.d(TAG, "Initializing Assembled Chat with company ID: ${configuration.companyId}")
            }

            // Create WebView
            webView = WebView(context.applicationContext).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                visibility = View.GONE
            }

            // Setup WebView settings
            webView?.settings?.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            // Create and add JavaScript bridge
            messageBridge = MessageBridge(listener, configuration.debug)
            webView?.addJavascriptInterface(messageBridge!!, BRIDGE_NAME)

            // Setup WebView client
            webView?.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (configuration.debug) {
                        Log.d(TAG, "Page loaded: $url")
                    }
                    injectConfiguration()
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    if (request?.isForMainFrame == true) {
                        val errorMessage = "Failed to load chat: ${error?.description}"
                        if (configuration.debug) {
                            Log.e(TAG, errorMessage)
                        }
                        listener?.onError(ChatError.LoadError(errorMessage, error?.errorCode))
                    }
                }
            }

            // Setup WebChromeClient for console logs (in debug mode)
            if (configuration.debug) {
                webView?.webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        consoleMessage?.let {
                            Log.d(
                                TAG,
                                "WebView Console [${it.messageLevel()}]: ${it.message()} (${it.sourceId()}:${it.lineNumber()})"
                            )
                        }
                        return true
                    }
                }
            }

            // Load chat URL
            val chatUrl = buildChatUrl()
            if (configuration.debug) {
                Log.d(TAG, "Loading chat URL: $chatUrl")
            }
            webView?.loadUrl(chatUrl)

            isInitialized = true
            isInitializing = false

        } catch (e: Exception) {
            isInitializing = false
            val errorMessage = "Failed to initialize chat: ${e.message}"
            if (configuration.debug) {
                Log.e(TAG, errorMessage, e)
            }
            listener?.onError(ChatError.InitializationFailed(errorMessage))
        }
    }

    /**
     * Open the chat widget.
     */
    fun open() {
        if (!isInitialized) {
            listener?.onError(ChatError.NotReady)
            return
        }

        webView?.visibility = View.VISIBLE
        executeJavaScript("window.AssembledChat?.open()")

        if (configuration.debug) {
            Log.d(TAG, "Chat opened")
        }
    }

    /**
     * Close the chat widget.
     */
    fun close() {
        if (!isInitialized) {
            listener?.onError(ChatError.NotReady)
            return
        }

        webView?.visibility = View.GONE
        executeJavaScript("window.AssembledChat?.close()")

        if (configuration.debug) {
            Log.d(TAG, "Chat closed")
        }
    }

    /**
     * Update user data for the current session.
     *
     * @param userData New user data
     */
    fun updateUserData(userData: UserData) {
        if (!isInitialized) {
            listener?.onError(ChatError.NotReady)
            return
        }

        val userDataJs = userData.toJavaScript()
        executeJavaScript("window.AssembledChat?.updateUserData($userDataJs)")

        if (configuration.debug) {
            Log.d(TAG, "User data updated")
        }
    }

    /**
     * Update JWT token for authenticated sessions.
     *
     * @param token New JWT token
     */
    fun updateToken(token: String) {
        if (!isInitialized) {
            listener?.onError(ChatError.NotReady)
            return
        }

        executeJavaScript("window.AssembledChat?.updateToken('$token')")

        if (configuration.debug) {
            Log.d(TAG, "Token updated")
        }
    }

    /**
     * Get the WebView instance for custom integration.
     *
     * @return WebView instance or null if not initialized
     */
    fun getWebView(): WebView? = webView

    /**
     * Check if chat is initialized.
     *
     * @return true if initialized
     */
    fun isReady(): Boolean = isInitialized

    /**
     * Destroy the chat instance and clean up resources.
     * Call this in your Activity/Fragment's onDestroy() to prevent memory leaks.
     */
    fun destroy() {
        if (configuration.debug) {
            Log.d(TAG, "Destroying chat instance")
        }

        webView?.apply {
            stopLoading()
            removeJavascriptInterface(BRIDGE_NAME)
            destroy()
        }
        webView = null
        messageBridge = null
        isInitialized = false
        isInitializing = false
    }

    private fun buildChatUrl(): String {
        val profilePath = configuration.profileId?.let { "/profile/$it" } ?: ""
        return "$CHAT_BASE_URL/public_chat/${configuration.companyId}$profilePath"
    }

    private fun injectConfiguration() {
        val configJs = configuration.toJavaScript()
        val script = """
            (function() {
                if (typeof window.AssembledChat !== 'undefined') {
                    window.AssembledChat.init($configJs);
                    
                    // Setup bridge callbacks
                    if (window.$BRIDGE_NAME) {
                        window.AssembledChat.on('ready', function() {
                            window.$BRIDGE_NAME.onReady();
                        });
                        window.AssembledChat.on('open', function() {
                            window.$BRIDGE_NAME.onOpen();
                        });
                        window.AssembledChat.on('close', function() {
                            window.$BRIDGE_NAME.onClose();
                        });
                        window.AssembledChat.on('error', function(error) {
                            window.$BRIDGE_NAME.onError(JSON.stringify(error));
                        });
                    }
                } else {
                    console.error('AssembledChat not found on window');
                    window.$BRIDGE_NAME?.onError('AssembledChat not found');
                }
            })();
        """.trimIndent()

        executeJavaScript(script)

        if (configuration.debug) {
            Log.d(TAG, "Configuration injected")
        }
    }

    private fun executeJavaScript(script: String) {
        webView?.evaluateJavascript(script) { result ->
            if (configuration.debug && result != null && result != "null") {
                Log.d(TAG, "JavaScript result: $result")
            }
        }
    }
}

