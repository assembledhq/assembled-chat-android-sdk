package com.assembled.chat.models

import kotlin.test.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class AssembledChatConfigurationTest {

    @Test
    fun `configuration with valid company ID should be created`() {
        val config = AssembledChatConfiguration(companyId = "test-company-123")
        
        assertEquals("test-company-123", config.companyId)
        assertNull(config.profileId)
        assertTrue(config.activated)
        assertFalse(config.disableLauncher)
        assertNull(config.buttonColor)
        assertFalse(config.debug)
        assertNull(config.jwtToken)
        assertNull(config.userData)
    }

    @Test
    fun `configuration with all parameters should be created`() {
        val userData = UserData(userId = "user-123", email = "test@example.com")
        val config = AssembledChatConfiguration(
            companyId = "test-company",
            profileId = "profile-456",
            activated = false,
            disableLauncher = true,
            buttonColor = "#FF0000",
            debug = true,
            jwtToken = "test-token",
            userData = userData
        )
        
        assertEquals("test-company", config.companyId)
        assertEquals("profile-456", config.profileId)
        assertFalse(config.activated)
        assertTrue(config.disableLauncher)
        assertEquals("#FF0000", config.buttonColor)
        assertTrue(config.debug)
        assertEquals("test-token", config.jwtToken)
        assertEquals(userData, config.userData)
    }

    @ParameterizedTest
    @MethodSource("invalidConfigTestCases")
    fun `configuration with invalid parameters should throw exception`(testCase: InvalidConfigTestCase) {
        assertFailsWith<IllegalArgumentException> {
            testCase.createConfig()
        }
    }

    @Test
    fun `configuration with valid hex colors should be created`() {
        val config1 = AssembledChatConfiguration(
            companyId = "test-company",
            buttonColor = "#FF0000"
        )
        assertEquals("#FF0000", config1.buttonColor)
        
        val config2 = AssembledChatConfiguration(
            companyId = "test-company",
            buttonColor = "#ff0000"
        )
        assertEquals("#ff0000", config2.buttonColor)
    }

    @Test
    fun `toJavaScript should generate valid JavaScript object`() {
        val config = AssembledChatConfiguration(
            companyId = "test-company",
            profileId = "profile-123",
            buttonColor = "#FF0000",
            jwtToken = "test-token"
        )
        
        val js = config.toJavaScript()
        
        assertContains(js, "companyId: 'test-company'")
        assertContains(js, "profileId: 'profile-123'")
        assertContains(js, "activated: true")
        assertContains(js, "disableLauncher: false")
        assertContains(js, "buttonColor: '#FF0000'")
        assertContains(js, "jwtToken: 'test-token'")
    }

    @Test
    fun `toJavaScript should escape single quotes`() {
        val config = AssembledChatConfiguration(
            companyId = "test's-company"
        )
        
        val js = config.toJavaScript()
        
        assertContains(js, "test\\'s-company")
    }

    @Test
    fun `copy should create new instance with updated values`() {
        val original = AssembledChatConfiguration(
            companyId = "test-company",
            debug = false
        )
        
        val copy = original.copy(debug = true)
        
        assertEquals("test-company", copy.companyId)
        assertTrue(copy.debug)
        assertFalse(original.debug)
    }

    data class InvalidConfigTestCase(
        val createConfig: () -> AssembledChatConfiguration,
        val description: String
    ) {
        override fun toString() = description
    }

    companion object {
        @JvmStatic
        fun invalidConfigTestCases() = listOf(
            InvalidConfigTestCase(
                { AssembledChatConfiguration(companyId = "") },
                "empty company ID"
            ),
            InvalidConfigTestCase(
                { AssembledChatConfiguration(companyId = "   ") },
                "blank company ID"
            ),
            InvalidConfigTestCase(
                { AssembledChatConfiguration(companyId = "test", buttonColor = "invalid-color") },
                "invalid button color"
            )
        )
    }
}
