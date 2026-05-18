package com.assembled.chat.internal

import android.content.Context
import android.os.Build
import java.util.Locale

internal object CountryResolver {
    private const val DEFAULT_COUNTRY = "US"

    fun resolve(context: Context): String {
        val locale = primaryLocale(context)
        val localeCountry = locale.country.normalizeCountry()
        if (localeCountry != null) return localeCountry

        return likelyCountryFor(locale) ?: DEFAULT_COUNTRY
    }

    @Suppress("DEPRECATION")
    private fun primaryLocale(context: Context): Locale {
        val configuration = context.resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales[0] ?: Locale.getDefault()
        } else {
            configuration.locale ?: Locale.getDefault()
        }
    }

    internal fun likelyCountryFor(locale: Locale): String? {
        return when (locale.language.lowercase(Locale.US)) {
            "ar" -> "SA"
            "da" -> "DK"
            "de" -> "DE"
            "en" -> "US"
            "es" -> "ES"
            "fi" -> "FI"
            "fr" -> "FR"
            "hi" -> "IN"
            "id" -> "ID"
            "it" -> "IT"
            "ja" -> "JP"
            "ko" -> "KR"
            "ms" -> "MY"
            "nb", "no" -> "NO"
            "nl" -> "NL"
            "pl" -> "PL"
            "pt" -> "BR"
            "ru" -> "RU"
            "sv" -> "SE"
            "th" -> "TH"
            "tr" -> "TR"
            "uk" -> "UA"
            "vi" -> "VN"
            "zh" -> if (locale.script.equals("Hant", ignoreCase = true)) "TW" else "CN"
            else -> null
        }
    }

    private fun String?.normalizeCountry(): String? {
        val country = this?.trim()?.uppercase(Locale.US)
        return country?.takeIf { it.matches(Regex("^[A-Z]{2}$")) }
    }
}
