package com.assembled.chat.example

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.assembled.chat.AssembledChatListener
import com.assembled.chat.AssembledChatView
import com.assembled.chat.models.ChatError

class ViewExampleActivity : AppCompatActivity(), AssembledChatListener {

    private lateinit var chatView: AssembledChatView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_example)

        chatView = findViewById(R.id.chatView)
        chatView.listener = this
        chatView.initialize(SampleData.configuration())

        findViewById<Button>(R.id.btnOpen).setOnClickListener {
            chatView.open()
        }
        findViewById<Button>(R.id.btnClose).setOnClickListener {
            chatView.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        chatView.destroy()
    }

    override fun onChatReady() {
        Log.d("ViewExampleActivity", "Chat ready")
    }

    override fun onChatOpened() {
        Log.d("ViewExampleActivity", "Chat opened")
    }

    override fun onChatClosed() {
        Log.d("ViewExampleActivity", "Chat closed")
    }

    override fun onError(error: ChatError) {
        Log.e("ViewExampleActivity", "Chat error: $error")
        Toast.makeText(this, "Chat error: ${error.message}", Toast.LENGTH_SHORT).show()
    }

    override fun onDebug(message: String) {
        Log.d("ViewExampleActivity", "Debug: $message")
    }

    override fun onNewMessage(messageCount: Int) {
        Log.d("ViewExampleActivity", "New messages: $messageCount")
    }
}

