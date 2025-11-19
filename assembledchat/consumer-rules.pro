# Consumer ProGuard rules for AssembledChat SDK

# Keep JavaScript interface
-keepclassmembers class com.assembled.chat.network.MessageBridge {
    @android.webkit.JavascriptInterface <methods>;
}

