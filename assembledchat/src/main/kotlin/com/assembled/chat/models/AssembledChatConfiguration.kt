package com.assembled.chat.models

/**
 * Configuration for initializing Assembled Chat SDK.
 *
 * @property companyId Your Assembled company ID (required)
 * @property profileId Optional profile ID for custom chat configurations
 * @property activated Whether chat is activated (default: true)
 * @property disableLauncher Whether to disable the chat launcher button (default: false)
 * @property buttonColor Hex color code for the chat button (e.g., "#007AFF")
 * @property debug Enable debug mode with verbose logging (default: false)
 * @property jwtToken JWT token for authenticated chat sessions
 * @property userData User data for authenticated sessions
 */
data class AssembledChatConfiguration(
    val companyId: String,
    val profileId: String? = null,
    val activated: Boolean = true,
    val disableLauncher: Boolean = false,
    val buttonColor: String? = null,
    val debug: Boolean = false,
    val jwtToken: String? = null,
    val userData: UserData? = null
) {
    init {
        require(companyId.isNotBlank()) { "companyId must not be blank" }
        
        buttonColor?.let { color ->
            require(isValidHexColor(color)) { 
                "buttonColor must be a valid hex color (e.g., #FF0000 or #ff0000)" 
            }
        }
    }

    /**
     * Converts configuration to JavaScript object literal for WebView injection.
     */
    fun toJavaScript(): String {
        val parts = mutableListOf<String>()
        
        parts.add("companyId: '${companyId.escapeForJs()}'")
        
        profileId?.let { parts.add("profileId: '${it.escapeForJs()}'") }
        parts.add("activated: $activated")
        parts.add("disableLauncher: $disableLauncher")
        buttonColor?.let { parts.add("buttonColor: '${it.escapeForJs()}'") }
        jwtToken?.let { parts.add("jwtToken: '${it.escapeForJs()}'") }
        userData?.let { parts.add("userData: ${it.toJavaScript()}") }
        
        return "{ ${parts.joinToString(", ")} }"
    }

    private fun isValidHexColor(color: String): Boolean {
        val hexPattern = Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
        return hexPattern.matches(color)
    }

    private fun String.escapeForJs(): String = this.replace("'", "\\'")
}

