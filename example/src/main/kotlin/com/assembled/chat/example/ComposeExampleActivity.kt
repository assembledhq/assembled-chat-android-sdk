package com.assembled.chat.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.assembled.chat.ui.AssembledChatComposable

class ComposeExampleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeExampleScreen()
        }
    }
}

@Composable
private fun ComposeExampleScreen() {
    val configuration = remember { SampleData.configuration() }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AssembledChatComposable(
                configuration = configuration,
                modifier = Modifier.fillMaxSize(),
                onError = { error ->
                    Log.e("ComposeExample", "Chat error: $error")
                },
                onDebug = { message ->
                    Log.d("ComposeExample", message)
                }
            )
        }
    }
}

