package com.assembled.chat.models

/**
 * User data for authenticated chat sessions.
 *
 * @property userId Unique identifier for the user
 * @property email User's email address
 * @property name User's display name
 * @property phone User's phone number
 * @property metadata Additional custom metadata as key-value pairs
 */
data class UserData(
    val userId: String,
    val email: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val metadata: Map<String, Any>? = null
) {
    init {
        require(userId.isNotBlank()) { "userId must not be blank" }
    }

    /**
     * Converts UserData to JavaScript object literal for WebView injection.
     */
    fun toJavaScript(): String {
        val parts = mutableListOf<String>()
        
        parts.add("userId: '${userId.escapeForJs()}'")
        
        email?.let { parts.add("email: '${it.escapeForJs()}'") }
        name?.let { parts.add("name: '${it.escapeForJs()}'") }
        phone?.let { parts.add("phone: '${it.escapeForJs()}'") }
        
        metadata?.let { meta ->
            val metadataStr = meta.entries.joinToString(", ") { (key, value) ->
                "'${key.escapeForJs()}': '${value.toString().escapeForJs()}'"
            }
            parts.add("metadata: { $metadataStr }")
        }
        
        return "{ ${parts.joinToString(", ")} }"
    }

    internal fun withCountryFallback(country: String): UserData {
        if (metadata?.containsKey("country") == true) return this

        return copy(metadata = metadata.orEmpty() + ("country" to country))
    }

    private fun String.escapeForJs(): String = this.replace("'", "\\'")

    internal companion object {
        /**
         * Builds a metadata-only JS payload for callers that haven't supplied a
         * [UserData], so the web widget still receives the device-region country
         * even without explicit user data or a JWT.
         */
        internal fun countryOnlyJavaScript(country: String): String {
            val escaped = country.replace("'", "\\'")
            return "{ metadata: { 'country': '$escaped' } }"
        }
    }
}
