# Assembled Chat Android SDK

[![Maven Central](https://img.shields.io/maven-central/v/io.github.assembledhq/assembledchat)](https://central.sonatype.com/artifact/io.github.assembledhq/assembledchat)
[![CI](https://github.com/assembledhq/assembled-chat-android-sdk/actions/workflows/ci.yml/badge.svg)](https://github.com/assembledhq/assembled-chat-android-sdk/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

The official Android SDK for integrating [Assembled Chat](https://www.assembled.com) into your Android application. This library provides a simple and flexible way to embed chat functionality with seamless integration for both traditional Android Views and Jetpack Compose.

## Requirements

- **Minimum SDK**: 21 (Android 5.0 Lollipop)
- **Target SDK**: 34 (Android 14)
- **Language**: Kotlin or Java
- **Jetpack Compose**: Optional (required only if using Composable)

## Installation

Add the dependency to your app's `build.gradle.kts` or `build.gradle` file:

### Kotlin DSL (build.gradle.kts)

```kotlin
dependencies {
    implementation("io.github.assembledhq:assembledchat:1.0.0")
}
```

### Groovy (build.gradle)

```groovy
dependencies {
    implementation 'io.github.assembledhq:assembledchat:1.0.0'
}
```

## Quick Start

### 1. Configure the SDK

First, create a configuration object with your Assembled Chat settings:

```kotlin
import com.assembled.chat.models.AssembledChatConfiguration
import com.assembled.chat.models.UserData

val config = AssembledChatConfiguration(
    url = "https://your-instance.assembled.com/chat",
    userData = UserData(
        userId = "user123",
        name = "John Doe",
        email = "john.doe@example.com"
    )
)
```

### 2. Choose Your Integration Method

The SDK offers multiple integration methods to fit your app's architecture:

#### Option A: Activity (Full Screen)

Launch the chat in a standalone Activity:

```kotlin
import com.assembled.chat.ui.AssembledChatActivity

// Launch the chat Activity
AssembledChatActivity.launch(context, config)
```

#### Option B: Fragment

Embed the chat in your own Activity using a Fragment:

```kotlin
import com.assembled.chat.ui.AssembledChatFragment

// In your Activity or Fragment
val chatFragment = AssembledChatFragment.newInstance(config)

supportFragmentManager.beginTransaction()
    .replace(R.id.fragment_container, chatFragment)
    .commit()
```

#### Option C: Custom View

Use the chat as a View in your XML layout:

```xml
<com.assembled.chat.AssembledChatView
    android:id="@+id/chatView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

```kotlin
import com.assembled.chat.AssembledChatView

// In your Activity
val chatView = findViewById<AssembledChatView>(R.id.chatView)
chatView.initialize(config)
```

#### Option D: Jetpack Compose

Use the Composable function in your Compose UI:

```kotlin
import com.assembled.chat.ui.AssembledChatComposable

@Composable
fun ChatScreen() {
    AssembledChatComposable(
        configuration = config,
        onError = { error ->
            // Handle error
            Log.e("Chat", "Error: ${error.message}")
        }
    )
}
```

## Advanced Usage

### Listening to Chat Events

Implement `AssembledChatListener` to receive chat events:

```kotlin
import com.assembled.chat.AssembledChatListener
import com.assembled.chat.models.ChatError

val listener = object : AssembledChatListener {
    override fun onChatLoaded() {
        // Chat has finished loading
        Log.d("Chat", "Chat loaded successfully")
    }
    
    override fun onChatError(error: ChatError) {
        // Handle error
        when (error.type) {
            ChatError.ErrorType.NETWORK_ERROR -> {
                // Handle network error
            }
            ChatError.ErrorType.CONFIGURATION_ERROR -> {
                // Handle configuration error
            }
            ChatError.ErrorType.UNKNOWN -> {
                // Handle unknown error
            }
        }
    }
    
    override fun onChatClosed() {
        // User closed the chat
        Log.d("Chat", "Chat closed by user")
    }
}

// Set the listener
chatView.setListener(listener)
// or when using Fragment
chatFragment.setListener(listener)
```

### Custom Configuration Options

```kotlin
val config = AssembledChatConfiguration(
    url = "https://your-instance.assembled.com/chat",
    userData = UserData(
        userId = "user123",
        name = "John Doe",
        email = "john.doe@example.com",
        // Optional custom attributes
        customAttributes = mapOf(
            "plan" to "premium",
            "signupDate" to "2024-01-15"
        )
    ),
    // Additional configuration options
    enableJavaScript = true,
    enableDomStorage = true
)
```

### Handling Deep Links

If you want to open specific chat conversations or handle deep links:

```kotlin
val config = AssembledChatConfiguration(
    url = "https://your-instance.assembled.com/chat/conversation/123",
    userData = userData
)
```

## Example App

Check out the `example` module in this repository for a complete sample application demonstrating all integration methods.

To run the example:

```bash
git clone https://github.com/assembledhq/assembled-chat-android-sdk.git
cd assembled-chat-android-sdk
./gradlew example:installDebug
```

## Architecture

The SDK is built with modern Android development practices:

- **Kotlin** - 100% Kotlin codebase
- **Jetpack Compose** - Modern UI toolkit support
- **WebView** - Embedded web chat with JavaScript bridge
- **AndroidX** - Uses latest AndroidX libraries
- **Material Design 3** - Follows Material Design guidelines

## Security

- All communication is encrypted via HTTPS
- User data is never stored locally by the SDK
- Follows Android security best practices
- No unnecessary permissions required

## Troubleshooting

### Chat not loading

- Verify your URL is correct and accessible
- Check your internet connection
- Ensure `enableJavaScript` is set to `true` (default)

### WebView compatibility issues

- The SDK requires WebView 51.0 or higher
- Update Android System WebView from Google Play Store if needed

### ProGuard/R8

The SDK is fully compatible with code obfuscation. Consumer ProGuard rules are automatically applied.


### Publishing to Maven Central

The publishing process is manual by default.

Quick publish command:
```bash
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


