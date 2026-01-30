package com.assembled.chat.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.assembled.chat.AssembledChat
import com.assembled.chat.AssembledChatListener
import com.assembled.chat.models.AssembledChatConfiguration
import com.assembled.chat.models.ChatError
import com.assembled.chat.models.UserData

/**
 * Pre-built activity for displaying Assembled Chat in full-screen mode.
 *
 * Usage:
 * ```
 * val intent = AssembledChatActivity.createIntent(
 *     context = this,
 *     companyId = "your-company-id"
 * )
 * startActivity(intent)
 * ```
 */
class AssembledChatActivity : AppCompatActivity(), AssembledChatListener {

    companion object {
        private const val TAG = "AssembledChatActivity"
        private const val EXTRA_COMPANY_ID = "extra_company_id"
        private const val EXTRA_PROFILE_ID = "extra_profile_id"
        private const val EXTRA_ACTIVATED = "extra_activated"
        private const val EXTRA_DISABLE_LAUNCHER = "extra_disable_launcher"
        private const val EXTRA_BUTTON_COLOR = "extra_button_color"
        private const val EXTRA_DEBUG = "extra_debug"
        private const val EXTRA_JWT_TOKEN = "extra_jwt_token"
        private const val EXTRA_USER_ID = "extra_user_id"
        private const val EXTRA_USER_EMAIL = "extra_user_email"
        private const val EXTRA_USER_NAME = "extra_user_name"

        /**
         * Create an intent to launch AssembledChatActivity.
         *
         * @param context Android context
         * @param companyId Your Assembled company ID
         * @param profileId Optional profile ID
         * @param activated Whether chat is activated
         * @param disableLauncher Whether to disable launcher
         * @param buttonColor Hex color for button
         * @param debug Enable debug mode
         * @param jwtToken JWT token for authentication
         * @param userId User ID for authentication
         * @param userEmail User email for authentication
         * @param userName User name for authentication
         */
        fun createIntent(
            context: Context,
            companyId: String,
            profileId: String? = null,
            activated: Boolean = true,
            disableLauncher: Boolean = false,
            buttonColor: String? = null,
            debug: Boolean = false,
            jwtToken: String? = null,
            userId: String? = null,
            userEmail: String? = null,
            userName: String? = null
        ): Intent {
            return Intent(context, AssembledChatActivity::class.java).apply {
                putExtra(EXTRA_COMPANY_ID, companyId)
                putExtra(EXTRA_PROFILE_ID, profileId)
                putExtra(EXTRA_ACTIVATED, activated)
                putExtra(EXTRA_DISABLE_LAUNCHER, disableLauncher)
                putExtra(EXTRA_BUTTON_COLOR, buttonColor)
                putExtra(EXTRA_DEBUG, debug)
                putExtra(EXTRA_JWT_TOKEN, jwtToken)
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_USER_EMAIL, userEmail)
                putExtra(EXTRA_USER_NAME, userName)
            }
        }
    }

    private lateinit var chat: AssembledChat
    private lateinit var container: FrameLayout
    private var disableLauncher = false
    private var debug = false
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Chat"

        // Create container for WebView
        container = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(container)

        // Parse configuration from intent
        val configuration = parseConfiguration()
        disableLauncher = configuration.disableLauncher
        debug = configuration.debug

        // Initialize chat
        chat = AssembledChat(configuration)
        chat.listener = this
        chat.initialize(this)

        // Add WebView to container
        chat.getWebView()?.let { webView ->
            container.addView(webView)
        }

        // Auto-open chat (skip if disableLauncher — will open in onChatReady)
        if (!disableLauncher) {
            chat.open()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        chat.destroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun parseConfiguration(): AssembledChatConfiguration {
        val companyId = intent.getStringExtra(EXTRA_COMPANY_ID)
            ?: throw IllegalArgumentException("companyId is required")

        val userData = intent.getStringExtra(EXTRA_USER_ID)?.let { userId ->
            UserData(
                userId = userId,
                email = intent.getStringExtra(EXTRA_USER_EMAIL),
                name = intent.getStringExtra(EXTRA_USER_NAME)
            )
        }

        return AssembledChatConfiguration(
            companyId = companyId,
            profileId = intent.getStringExtra(EXTRA_PROFILE_ID),
            activated = intent.getBooleanExtra(EXTRA_ACTIVATED, true),
            disableLauncher = intent.getBooleanExtra(EXTRA_DISABLE_LAUNCHER, false),
            buttonColor = intent.getStringExtra(EXTRA_BUTTON_COLOR),
            debug = intent.getBooleanExtra(EXTRA_DEBUG, false),
            jwtToken = intent.getStringExtra(EXTRA_JWT_TOKEN),
            userData = userData
        )
    }

    // AssembledChatListener implementation
    override fun onChatReady() {
        if (debug) Log.d(TAG, "Chat ready")

        // Auto-open chat when disableLauncher is true
        // Must post to main thread since this callback runs on JavaBridge thread
        if (disableLauncher) {
            mainHandler.post {
                chat.open()
            }
        }
    }

    override fun onChatOpened() {
        if (debug) Log.d(TAG, "Chat opened")
    }

    override fun onChatClosed() {
        if (debug) Log.d(TAG, "Chat closed")
        finish()
    }

    override fun onError(error: ChatError) {
        if (debug) Log.e(TAG, "Chat error: $error")
    }

    override fun onDebug(message: String) {
        if (debug) Log.d(TAG, "Debug: $message")
    }
}

