package com.assembled.chat.models

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserDataTest {

    @Test
    fun `userId must not be blank`() {
        assertFailsWith<IllegalArgumentException> {
            UserData(userId = "")
        }

        assertFailsWith<IllegalArgumentException> {
            UserData(userId = "   ")
        }
    }

    @Test
    fun `toJavaScript includes optional fields when provided`() {
        val userData = UserData(
            userId = "user-123",
            email = "test@example.com",
            name = "John Doe",
            phone = "+1-555-1234"
        )

        val js = userData.toJavaScript()

        assertContains(js, "userId: 'user-123'")
        assertContains(js, "email: 'test@example.com'")
        assertContains(js, "name: 'John Doe'")
        assertContains(js, "phone: '+1-555-1234'")
    }

    @Test
    fun `toJavaScript encodes metadata as JS object`() {
        val userData = UserData(
            userId = "user-123",
            metadata = mapOf(
                "plan" to "premium",
                "age" to 29,
                "isActive" to true,
                "score" to 98.5
            )
        )

        val js = userData.toJavaScript()

        assertContains(js, "metadata: {")
        assertContains(js, "'plan': 'premium'")
        assertContains(js, "'age': '29'")
        assertContains(js, "'isActive': 'true'")
        assertContains(js, "'score': '98.5'")
    }

    @Test
    fun `toJavaScript escapes single quotes`() {
        val userData = UserData(
            userId = "user-'123",
            email = "o'hara@example.com",
            metadata = mapOf(
                "custom'key" to "value with 'quotes'"
            )
        )

        val js = userData.toJavaScript()

        assertContains(js, "userId: 'user-\\'123'")
        assertContains(js, "email: 'o\\'hara@example.com'")
        assertContains(js, "'custom\\'key': 'value with \\'quotes\\''")
    }

    @Test
    fun `metadata is omitted when not provided`() {
        val userData = UserData(userId = "user-123")

        val js = userData.toJavaScript()

        assertFalse(js.contains("metadata:"))
    }
}
