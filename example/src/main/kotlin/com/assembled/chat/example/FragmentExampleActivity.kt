package com.assembled.chat.example

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.assembled.chat.AssembledChatListener
import com.assembled.chat.models.ChatError
import com.assembled.chat.ui.AssembledChatFragment

class FragmentExampleActivity : AppCompatActivity(), AssembledChatListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_example)

        if (savedInstanceState == null) {
            addChatFragment()
        } else {
            (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? AssembledChatFragment)
                ?.let { fragment ->
                    fragment.chatListener = this
                }
        }
    }

    private fun addChatFragment() {
        val fragment = AssembledChatFragment.newInstance(SampleData.configuration()).apply {
            chatListener = this@FragmentExampleActivity
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onChatReady() {
        Log.d("FragmentExample", "Chat ready")
    }

    override fun onChatOpened() {
        Log.d("FragmentExample", "Chat opened")
    }

    override fun onChatClosed() {
        Log.d("FragmentExample", "Chat closed")
    }

    override fun onError(error: ChatError) {
        Log.e("FragmentExample", "Chat error: $error")
        Toast.makeText(this, "Chat error: ${error.message}", Toast.LENGTH_SHORT).show()
    }

    override fun onDebug(message: String) {
        Log.d("FragmentExample", "Debug: $message")
    }

    override fun onNewMessage(messageCount: Int) {
        Log.d("FragmentExample", "New messages: $messageCount")
    }
}

