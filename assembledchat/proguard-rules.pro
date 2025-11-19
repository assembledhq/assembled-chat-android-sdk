# Add project specific ProGuard rules here.

# Keep AssembledChat public API
-keep public class com.assembled.chat.AssembledChat { *; }
-keep public class com.assembled.chat.AssembledChatView { *; }
-keep public class com.assembled.chat.models.** { *; }
-keep public class com.assembled.chat.ui.** { *; }

# Keep JavaScript interface for WebView bridge
-keepclassmembers class com.assembled.chat.network.MessageBridge {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

