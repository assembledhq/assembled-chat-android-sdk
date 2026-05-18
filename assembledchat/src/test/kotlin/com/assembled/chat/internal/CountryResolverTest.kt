package com.assembled.chat.internal

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class CountryResolverTest {
    @Test
    fun `likelyCountryFor adds fallback country for language only locale`() {
        val country = CountryResolver.likelyCountryFor(Locale.forLanguageTag("en"))

        assertEquals("US", country)
    }

    @Test
    fun `likelyCountryFor uses script when selecting Chinese fallback country`() {
        val country = CountryResolver.likelyCountryFor(Locale.forLanguageTag("zh-Hant"))

        assertEquals("TW", country)
    }
}
