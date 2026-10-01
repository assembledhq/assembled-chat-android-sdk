package com.assembled.chat

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExternalNavigationTest {

    @Test
    fun `main frame web links open externally`() {
        assertTrue(shouldOpenInExternalBrowser("https://support.example.com/survey", true))
        assertTrue(shouldOpenInExternalBrowser("HTTP://support.example.com/survey", true))
    }

    @Test
    fun `subframe links remain in the webview`() {
        assertFalse(shouldOpenInExternalBrowser("https://cal.assembledhq.com/widget", false))
    }

    @Test
    fun `non-web and invalid links do not open externally`() {
        assertFalse(shouldOpenInExternalBrowser("javascript:alert('test')", true))
        assertFalse(shouldOpenInExternalBrowser("not a url", true))
        assertFalse(shouldOpenInExternalBrowser(null, true))
    }
}
