package com.assembled.chat.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.assembled.chat.AssembledChat
import com.assembled.chat.AssembledChatListener
import com.assembled.chat.models.AssembledChatConfiguration
import com.assembled.chat.models.ChatError

/**
 * Fragment wrapper for Assembled Chat.
 *
 * Usage:
 * ```
 * val config = AssembledChatConfiguration(companyId = "your-company-id")
 * val fragment = AssembledChatFragment.newInstance(config)
 * supportFragmentManager.beginTransaction()
 *     .replace(R.id.container, fragment)
 *     .commit()
 * ```
 */
class AssembledChatFragment : Fragment(), AssembledChatListener {

    companion object {
        private const val TAG = "AssembledChatFragment"
        private const val ARG_COMPANY_ID = "company_id"
        private const val ARG_PROFILE_ID = "profile_id"
        private const val ARG_ACTIVATED = "activated"
        private const val ARG_DISABLE_LAUNCHER = "disable_launcher"
        private const val ARG_BUTTON_COLOR = "button_color"
        private const val ARG_DEBUG = "debug"
        private const val ARG_JWT_TOKEN = "jwt_token"

        /**
         * Create a new instance of AssembledChatFragment.
         *
         * @param configuration Chat configuration
         */
        fun newInstance(configuration: AssembledChatConfiguration): AssembledChatFragment {
            return AssembledChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_COMPANY_ID, configuration.companyId)
                    putString(ARG_PROFILE_ID, configuration.profileId)
                    putBoolean(ARG_ACTIVATED, configuration.activated)
                    putBoolean(ARG_DISABLE_LAUNCHER, configuration.disableLauncher)
                    putString(ARG_BUTTON_COLOR, configuration.buttonColor)
                    putBoolean(ARG_DEBUG, configuration.debug)
                    putString(ARG_JWT_TOKEN, configuration.jwtToken)
                }
            }
        }

        /**
         * Create a new instance with simple parameters.
         *
         * @param companyId Your Assembled company ID
         * @param profileId Optional profile ID
         * @param debug Enable debug mode
         */
        fun newInstance(
            companyId: String,
            profileId: String? = null,
            debug: Boolean = false
        ): AssembledChatFragment {
            val config = AssembledChatConfiguration(
                companyId = companyId,
                profileId = profileId,
                debug = debug
            )
            return newInstance(config)
        }
    }

    private var chat: AssembledChat? = null
    var chatListener: AssembledChatListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val configuration = parseConfiguration()

        chat = AssembledChat(configuration)
        chat?.listener = this
        chat?.initialize(requireContext())

        // Add WebView to container
        chat?.getWebView()?.let { webView ->
            (view as? FrameLayout)?.addView(webView)
        }

        // Auto-open chat
        chat?.open()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chat?.destroy()
        chat = null
    }

    /**
     * Get the AssembledChat instance.
     */
    fun getChat(): AssembledChat? = chat

    private fun parseConfiguration(): AssembledChatConfiguration {
        val args = arguments ?: throw IllegalStateException("Fragment requires arguments")
        val companyId = args.getString(ARG_COMPANY_ID)
            ?: throw IllegalArgumentException("companyId is required")

        return AssembledChatConfiguration(
            companyId = companyId,
            profileId = args.getString(ARG_PROFILE_ID),
            activated = args.getBoolean(ARG_ACTIVATED, true),
            disableLauncher = args.getBoolean(ARG_DISABLE_LAUNCHER, false),
            buttonColor = args.getString(ARG_BUTTON_COLOR),
            debug = args.getBoolean(ARG_DEBUG, false),
            jwtToken = args.getString(ARG_JWT_TOKEN)
        )
    }

    // AssembledChatListener implementation
    override fun onChatReady() {
        Log.d(TAG, "Chat ready")
        chatListener?.onChatReady()
    }

    override fun onChatOpened() {
        Log.d(TAG, "Chat opened")
        chatListener?.onChatOpened()
    }

    override fun onChatClosed() {
        Log.d(TAG, "Chat closed")
        chatListener?.onChatClosed()
    }

    override fun onError(error: ChatError) {
        Log.e(TAG, "Chat error: $error")
        chatListener?.onError(error)
    }

    override fun onDebug(message: String) {
        Log.d(TAG, "Debug: $message")
        chatListener?.onDebug(message)
    }

    override fun onNewMessage(messageCount: Int) {
        Log.d(TAG, "New message: count=$messageCount")
        chatListener?.onNewMessage(messageCount)
    }
}

