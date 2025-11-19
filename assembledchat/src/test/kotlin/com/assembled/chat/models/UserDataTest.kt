package com.assembled.chat.models

import org.json.JSONObject
import kotlin.test.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class UserDataTest {

    @Test
    fun `empty UserData should serialize to empty JSON object`() {
        val userData = UserData()
        val json = userData.toJson()
        
        assertEquals(0, json.length())
    }

    @ParameterizedTest
    @MethodSource("singleFieldTestCases")
    fun `UserData with single field should serialize correctly`(testCase: SingleFieldTestCase) {
        val json = testCase.userData.toJson()
        assertEquals(testCase.expectedValue, json.getString(testCase.fieldName))
    }

    @Test
    fun `UserData with all fields should serialize correctly`() {
        val metadata = mapOf(
            "plan" to "premium",
            "segment" to "enterprise"
        )
        
        val userData = UserData(
            userId = "user-123",
            email = "test@example.com",
            name = "John Doe",
            metadata = metadata
        )
        
        val json = userData.toJson()
        
        assertEquals("user-123", json.getString("user_id"))
        assertEquals("test@example.com", json.getString("email"))
        assertEquals("John Doe", json.getString("name"))
        
        val metaJson = json.getJSONObject("metadata")
        assertEquals("premium", metaJson.getString("plan"))
        assertEquals("enterprise", metaJson.getString("segment"))
    }

    @Test
    fun `UserData with complex metadata should serialize correctly`() {
        val metadata = mapOf(
            "preferences" to mapOf(
                "language" to "en",
                "notifications" to true
            ),
            "tags" to listOf("vip", "premium", "early-adopter"),
            "age" to 25,
            "score" to 98.5,
            "is_verified" to true,
            "is_active" to false,
            "optional_field" to null,
            "present_field" to "value"
        )
        
        val userData = UserData(metadata = metadata)
        val json = userData.toJson()
        val metaJson = json.getJSONObject("metadata")
        
        // Nested object
        val prefsJson = metaJson.getJSONObject("preferences")
        assertEquals("en", prefsJson.getString("language"))
        assertTrue(prefsJson.getBoolean("notifications"))
        
        // List/Array
        val tagsArray = metaJson.getJSONArray("tags")
        assertEquals(3, tagsArray.length())
        assertEquals("vip", tagsArray.getString(0))
        assertEquals("premium", tagsArray.getString(1))
        assertEquals("early-adopter", tagsArray.getString(2))
        
        // Numeric values
        assertEquals(25, metaJson.getInt("age"))
        assertEquals(98.5, metaJson.getDouble("score"), 0.01)
        
        // Boolean values
        assertTrue(metaJson.getBoolean("is_verified"))
        assertFalse(metaJson.getBoolean("is_active"))
        
        // Null values
        assertTrue(metaJson.isNull("optional_field"))
        assertEquals("value", metaJson.getString("present_field"))
    }

    @Test
    fun `fromJson should deserialize UserData correctly`() {
        val json = JSONObject().apply {
            put("user_id", "user-123")
            put("email", "test@example.com")
            put("name", "John Doe")
            put("metadata", JSONObject().apply {
                put("plan", "premium")
                put("segment", "enterprise")
            })
        }
        
        val userData = UserData.fromJson(json)
        
        assertEquals("user-123", userData.userId)
        assertEquals("test@example.com", userData.email)
        assertEquals("John Doe", userData.name)
        assertNotNull(userData.metadata)
        assertEquals("premium", userData.metadata?.get("plan"))
        assertEquals("enterprise", userData.metadata?.get("segment"))
    }

    @Test
    fun `fromJson should handle missing fields gracefully`() {
        val json = JSONObject().apply {
            put("user_id", "user-123")
        }
        
        val userData = UserData.fromJson(json)
        
        assertEquals("user-123", userData.userId)
        assertNull(userData.email)
        assertNull(userData.name)
        assertNull(userData.metadata)
    }

    @Test
    fun `serialization and deserialization should be reversible`() {
        val original = UserData(
            userId = "user-123",
            email = "test@example.com",
            name = "John Doe",
            metadata = mapOf(
                "plan" to "premium",
                "age" to 25,
                "is_active" to true
            )
        )
        
        val json = original.toJson()
        val deserialized = UserData.fromJson(json)
        
        assertEquals(original.userId, deserialized.userId)
        assertEquals(original.email, deserialized.email)
        assertEquals(original.name, deserialized.name)
        assertNotNull(deserialized.metadata)
    }

    data class SingleFieldTestCase(
        val userData: UserData,
        val fieldName: String,
        val expectedValue: String,
        val description: String
    ) {
        override fun toString() = description
    }

    companion object {
        @JvmStatic
        fun singleFieldTestCases() = listOf(
            SingleFieldTestCase(
                UserData(userId = "user-123"),
                "user_id",
                "user-123",
                "userId field"
            ),
            SingleFieldTestCase(
                UserData(email = "test@example.com"),
                "email",
                "test@example.com",
                "email field"
            ),
            SingleFieldTestCase(
                UserData(name = "John Doe"),
                "name",
                "John Doe",
                "name field"
            )
        )
    }
}
