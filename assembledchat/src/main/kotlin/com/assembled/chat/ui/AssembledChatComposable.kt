package com.assembled.chat.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.atomic.AtomicReference
import com.assembled.chat.AssembledChat
import com.assembled.chat.AssembledChatListener
import com.assembled.chat.models.AssembledChatConfiguration
import com.assembled.chat.models.ChatError
import com.assembled.chat.models.UserData

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

/**
 * Jetpack Compose composable for Assembled Chat.
 *
 * Usage:
 * ```
 * @Composable
 * fun ChatScreen() {
 *     AssembledChatComposable(
 *         companyId = "your-company-id",
 *         onReady = { Log.d("Chat", "Ready") },
 *         onError = { error -> Log.e("Chat", "Error: $error") }
 *     )
 * }
 * ```
 */
@Composable
fun AssembledChatComposable(
    companyId: String,
    profileId: String? = null,
    activated: Boolean = true,
    disableLauncher: Boolean = false,
    buttonColor: String? = null,
    debug: Boolean = false,
    jwtToken: String? = null,
    userData: UserData? = null,
    modifier: Modifier = Modifier,
    onReady: () -> Unit = {},
    onOpened: () -> Unit = {},
    onClosed: () -> Unit = {},
    onError: (ChatError) -> Unit = {},
    onDebug: (String) -> Unit = {},
    onNewMessage: (Int) -> Unit = {}
) {
    val configuration = remember(
        companyId,
        profileId,
        activated,
        disableLauncher,
        buttonColor,
        debug,
        jwtToken,
        userData
    ) {
        AssembledChatConfiguration(
            companyId = companyId,
            profileId = profileId,
            activated = activated,
            disableLauncher = disableLauncher,
            buttonColor = buttonColor,
            debug = debug,
            jwtToken = jwtToken,
            userData = userData
        )
    }

    AssembledChatComposable(
        configuration = configuration,
        modifier = modifier,
        onReady = onReady,
        onOpened = onOpened,
        onClosed = onClosed,
        onError = onError,
        onDebug = onDebug,
        onNewMessage = onNewMessage
    )
}

/**
 * Jetpack Compose composable for Assembled Chat with configuration object.
 *
 * Usage:
 * ```
 * @Composable
 * fun ChatScreen() {
 *     val config = AssembledChatConfiguration(companyId = "your-company-id")
 *     AssembledChatComposable(
 *         configuration = config,
 *         onReady = { Log.d("Chat", "Ready") }
 *     )
 * }
 * ```
 */
@Composable
fun AssembledChatComposable(
    configuration: AssembledChatConfiguration,
    modifier: Modifier = Modifier,
    onReady: () -> Unit = {},
    onOpened: () -> Unit = {},
    onClosed: () -> Unit = {},
    onError: (ChatError) -> Unit = {},
    onDebug: (String) -> Unit = {},
    onNewMessage: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    
    // Find the Activity context to ensure WebView has access to WindowManager
    val activityContext = remember(context) {
        context.findActivity() ?: context
    }

    val chatInstanceRef = remember { AtomicReference<AssembledChat?>(null) }
    val disableLauncher = configuration.disableLauncher

    val listener = remember(onReady, onOpened, onClosed, onError, onDebug, onNewMessage, disableLauncher) {
        object : AssembledChatListener {
            override fun onChatReady() {
                Log.d("AssembledChat", "Chat ready")
                onReady()
                
                // Auto-open chat when disableLauncher is true
                if (disableLauncher) {
                    chatInstanceRef.get()?.open()
                }
            }

            override fun onChatOpened() {
                Log.d("AssembledChat", "Chat opened")
                onOpened()
            }

            override fun onChatClosed() {
                Log.d("AssembledChat", "Chat closed")
                onClosed()
            }

            override fun onError(error: ChatError) {
                Log.e("AssembledChat", "Chat error: $error")
                onError(error)
            }

            override fun onDebug(message: String) {
                Log.d("AssembledChat", "Debug: $message")
                onDebug(message)
            }

            override fun onNewMessage(messageCount: Int) {
                Log.d("AssembledChat", "New message: count=$messageCount")
                onNewMessage(messageCount)
            }
        }
    }

    val chat = remember(configuration, activityContext) {
        AssembledChat(configuration).apply {
            this.listener = listener
            chatInstanceRef.set(this)
            initialize(activityContext)
        }
    }

    DisposableEffect(chat) {
        // Only auto-open if disableLauncher is false (normal launcher behavior)
        // If disableLauncher is true, chat will open automatically in onChatReady callback
        if (!disableLauncher) {
            chat.open()
        }

        onDispose {
            chat.destroy()
            chatInstanceRef.set(null)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            chat.getWebView() ?: WebView(activityContext).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }
    )
}

