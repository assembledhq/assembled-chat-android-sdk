package com.assembled.chat.example

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.assembled.chat.models.AssembledChatConfiguration
import com.assembled.chat.ui.AssembledChatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupButtons()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnActivityExample).setOnClickListener {
            launchActivityExample()
        }

        findViewById<Button>(R.id.btnViewExample).setOnClickListener {
            launchViewExample()
        }

        findViewById<Button>(R.id.btnFragmentExample).setOnClickListener {
            launchFragmentExample()
        }

        findViewById<Button>(R.id.btnComposeExample).setOnClickListener {
            launchComposeExample()
        }
    }

    private fun launchActivityExample() {
        val configuration = createSampleConfiguration()
        val intent = AssembledChatActivity.createIntent(this, configuration)
        startActivity(intent)
    }

    private fun launchViewExample() {
        val intent = Intent(this, ViewExampleActivity::class.java)
        startActivity(intent)
    }

    private fun launchFragmentExample() {
        val intent = Intent(this, FragmentExampleActivity::class.java)
        startActivity(intent)
    }

    private fun launchComposeExample() {
        val intent = Intent(this, ComposeExampleActivity::class.java)
        startActivity(intent)
    }

    private fun createSampleConfiguration(): AssembledChatConfiguration {
        // Replace with your actual company ID
        return AssembledChatConfiguration(
            companyId = "your-company-id",
            brandColor = "#4F46E5",
            userId = "test-user-${System.currentTimeMillis()}",
            userName = "Test User",
            userEmail = "test@example.com",
            debugMode = true
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
