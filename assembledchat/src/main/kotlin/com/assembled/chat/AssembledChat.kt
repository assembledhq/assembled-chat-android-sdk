package com.assembled.chat

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
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
        private const val CHAT_SCRIPT_URL = "https://cal.assembledhq.com/static/js/public-chat.js"
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

            // Create WebView (use context directly for WindowManager access)
            webView = WebView(context).apply {
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
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    if (configuration.debug) {
                        Log.d(TAG, "URL Loading: ${request?.url}")
                    }
                    return false
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    if (configuration.debug) {
                        Log.d(TAG, "Page started loading: $url")
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (configuration.debug) {
                        Log.d(TAG, "Page finished loading: $url")
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
                        val description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            error?.description?.toString()
                        } else {
                            error?.toString()
                        }
                        val errorMessage = buildString {
                            append("Failed to load chat")
                            if (!description.isNullOrBlank()) {
                                append(": ")
                                append(description)
                            }
                        }
                        val errorCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            error?.errorCode
                        } else null
                        if (configuration.debug) {
                            Log.e(TAG, errorMessage)
                        }
                        listener?.onError(ChatError.LoadError(errorMessage, errorCode))
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

            // Load HTML page with embedded chat widget script
            val chatHtml = buildChatHtml()
            if (configuration.debug) {
                Log.d(TAG, "Loading chat widget HTML")
            }
            webView?.loadDataWithBaseURL(
                "https://cal.assembledhq.com",
                chatHtml,
                "text/html",
                "UTF-8",
                null
            )

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
        // Call open and notify the bridge; if .on('open') doesn't fire,
        // this ensures onChatOpened is still triggered
        executeJavaScript("""
            (function() {
                if (window.assembled && typeof window.assembled.open === 'function') {
                    window.assembled.open();
                }
                if (window.$BRIDGE_NAME) {
                    window.$BRIDGE_NAME.onOpen();
                }
            })()
        """.trimIndent())

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
        // Call close and notify the bridge; if .on('close') doesn't fire,
        // this ensures onChatClosed is still triggered
        executeJavaScript("""
            (function() {
                if (window.assembled && typeof window.assembled.close === 'function') {
                    window.assembled.close();
                }
                if (window.$BRIDGE_NAME) {
                    window.$BRIDGE_NAME.onClose();
                }
            })()
        """.trimIndent())

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
        executeJavaScript("window.assembled?.setUserData($userDataJs)")

        if (configuration.debug) {
            Log.d(TAG, "User data updated")
        }
    }

    /**
     * Update JWT token for authenticated sessions.
     * Uses the Assembled SDK's authenticateUser method.
     *
     * @param token New JWT token
     */
    fun updateToken(token: String) {
        if (!isInitialized) {
            listener?.onError(ChatError.NotReady)
            return
        }

        executeJavaScript("window.assembled?.authenticateUser('$token')")

        if (configuration.debug) {
            Log.d(TAG, "Token updated via authenticateUser")
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

    private fun buildChatHtml(): String {
        val userDataJs = configuration.userData?.toJavaScript() ?: "null"
        val jwtTokenJs = configuration.jwtToken?.let { "'${it.replace("'", "\\'")}'" } ?: "null"
        val profileIdAttr = configuration.profileId?.let { "data-profile-id=\"$it\"" } ?: ""
        val disableLauncherAttr = if (configuration.disableLauncher) "data-disable-launcher=\"true\"" else ""
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    html, body { 
                        width: 100%; 
                        height: 100%; 
                        overflow: hidden;
                        background: transparent;
                    }
                </style>
            </head>
            <body>
                <script
                    src="$CHAT_SCRIPT_URL"
                    data-company-id="${configuration.companyId}"
                    $profileIdAttr
                    $disableLauncherAttr
                ></script>
                <script>
                    (function() {
                        var DEBUG = ${configuration.debug};
                        var maxAttempts = 50;
                        var attemptInterval = 200;
                        var attempts = 0;
                        var disableLauncher = ${configuration.disableLauncher};

                        function log(msg) { if (DEBUG) console.log('[AssembledChat] ' + msg); }
                        function logError(msg) { console.error('[AssembledChat] ' + msg); }

                        function setupBridge() {
                            attempts++;

                            if (typeof window.assembled !== 'undefined') {
                                log('SDK ready after ' + attempts + ' attempt(s)');

                                if (disableLauncher && typeof window.assembled.setConfig === 'function') {
                                    window.assembled.setConfig({ disableLauncher: true });
                                }

                                var userData = $userDataJs;
                                if (userData) {
                                    window.assembled.setUserData(userData);
                                }

                                var jwtToken = $jwtTokenJs;
                                if (jwtToken) {
                                    window.assembled.authenticateUser(jwtToken);
                                }

                                if (window.$BRIDGE_NAME) {
                                    // Listen for events via SDK .on() method
                                    if (typeof window.assembled.on === 'function') {
                                        window.assembled.on('open', function() {
                                            log('Event: open');
                                            window.$BRIDGE_NAME.onOpen();
                                        });
                                        window.assembled.on('close', function() {
                                            log('Event: close');
                                            window.$BRIDGE_NAME.onClose();
                                        });
                                        window.assembled.on('error', function(error) {
                                            log('Event: error');
                                            window.$BRIDGE_NAME.onError(JSON.stringify(error));
                                        });
                                        window.assembled.on('message', function(data) {
                                            log('Event: message');
                                            window.$BRIDGE_NAME.onNewMessage(1);
                                        });
                                    }

                                    // Fallback: listen for postMessage events
                                    window.addEventListener('message', function(event) {
                                        try {
                                            var data = event.data;
                                            if (typeof data === 'string') {
                                                try { data = JSON.parse(data); } catch(e) { return; }
                                            }
                                            if (!data || typeof data !== 'object') return;

                                            var type = data.type || data.event || data.action;
                                            if (!type) return;

                                            var typeStr = String(type).toLowerCase();
                                            if (typeStr.indexOf('assembled') === -1 && typeStr.indexOf('chat') === -1) return;

                                            log('postMessage: ' + typeStr);

                                            if (typeStr.indexOf('open') !== -1) {
                                                window.$BRIDGE_NAME.onOpen();
                                            } else if (typeStr.indexOf('close') !== -1) {
                                                window.$BRIDGE_NAME.onClose();
                                            } else if (typeStr.indexOf('error') !== -1) {
                                                window.$BRIDGE_NAME.onError(JSON.stringify(data.error || data.message || data));
                                            } else if (typeStr.indexOf('message') !== -1 || typeStr.indexOf('new_message') !== -1) {
                                                window.$BRIDGE_NAME.onNewMessage(data.count || 1);
                                            }
                                        } catch(e) {
                                            log('postMessage error: ' + e.message);
                                        }
                                    });

                                    // MutationObserver fallback: detect chat close via DOM changes
                                    (function() {
                                        var chatContainer = null;
                                        var lastOpenState = null;

                                        function findChatContainer() {
                                            var selectors = [
                                                '[data-assembled-chat]',
                                                '[class*="assembled"]',
                                                '#assembled-chat',
                                                'iframe[src*="assembled"]',
                                                '[class*="chat-widget"]',
                                                '[class*="chat-container"]'
                                            ];
                                            for (var i = 0; i < selectors.length; i++) {
                                                var el = document.querySelector(selectors[i]);
                                                if (el) return el;
                                            }
                                            return null;
                                        }

                                        function checkChatState() {
                                            if (!chatContainer) chatContainer = findChatContainer();

                                            var isOpen = false;
                                            if (chatContainer) {
                                                var style = window.getComputedStyle(chatContainer);
                                                isOpen = style.display !== 'none' &&
                                                         style.visibility !== 'hidden' &&
                                                         style.opacity !== '0';
                                            }

                                            if (window.assembled && typeof window.assembled.isOpen === 'function') {
                                                isOpen = window.assembled.isOpen();
                                            } else if (window.assembled && typeof window.assembled.isOpen === 'boolean') {
                                                isOpen = window.assembled.isOpen;
                                            }

                                            if (lastOpenState !== null && lastOpenState !== isOpen) {
                                                log('State change detected: ' + (isOpen ? 'open' : 'close'));
                                                if (isOpen) {
                                                    window.$BRIDGE_NAME.onOpen();
                                                } else {
                                                    window.$BRIDGE_NAME.onClose();
                                                }
                                            }
                                            lastOpenState = isOpen;
                                        }

                                        if (typeof MutationObserver !== 'undefined') {
                                            var observer = new MutationObserver(checkChatState);
                                            observer.observe(document.body, {
                                                childList: true,
                                                subtree: true,
                                                attributes: true,
                                                attributeFilter: ['style', 'class', 'hidden']
                                            });
                                        }
                                        setInterval(checkChatState, 500);
                                    })();

                                    window.$BRIDGE_NAME.onReady();
                                }
                            } else if (attempts < maxAttempts) {
                                log('Waiting for SDK... attempt ' + attempts + '/' + maxAttempts);
                                setTimeout(setupBridge, attemptInterval);
                            } else {
                                logError('SDK not found after ' + maxAttempts + ' attempts');
                                if (window.$BRIDGE_NAME) {
                                    window.$BRIDGE_NAME.onError('Assembled chat SDK not found - timeout');
                                }
                            }
                        }

                        setTimeout(setupBridge, 100);
                    })();
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun injectConfiguration() {
        // Configuration is already injected via the HTML page
        // This method is called on page load and can be used for additional setup if needed
        if (configuration.debug) {
            val debugScript = """
                (function() {
                    var debugInfo = {
                        hasAssembled: typeof window.assembled !== 'undefined',
                        windowKeys: Object.keys(window).filter(function(k) { 
                            return k.toLowerCase().includes('assembled'); 
                        }),
                        currentUrl: window.location.href,
                        readyState: document.readyState
                    };
                    console.log('Debug Info:', JSON.stringify(debugInfo));
                    window.$BRIDGE_NAME?.onDebug('Window check: ' + JSON.stringify(debugInfo));
                })();
            """.trimIndent()
            executeJavaScript(debugScript)
            Log.d(TAG, "Configuration check executed")
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

