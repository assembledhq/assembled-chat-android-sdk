package com.assembled.chat.example

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
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
        val configuration = SampleData.configuration()
        val intent = AssembledChatActivity.createIntent(
            context = this,
            companyId = configuration.companyId,
            profileId = configuration.profileId,
            activated = configuration.activated,
            disableLauncher = configuration.disableLauncher,
            buttonColor = configuration.buttonColor,
            debug = configuration.debug,
            jwtToken = configuration.jwtToken,
            userId = configuration.userData?.userId,
            userEmail = configuration.userData?.email,
            userName = configuration.userData?.name
        )
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
}
