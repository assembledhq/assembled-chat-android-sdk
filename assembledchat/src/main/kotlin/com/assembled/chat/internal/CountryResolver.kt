package com.assembled.chat.internal

import android.content.Context
import android.os.Build
import java.util.Locale

internal object CountryResolver {
    private const val DEFAULT_COUNTRY = "US"

    fun resolve(context: Context): String {
        val locale = primaryLocale(context)
        val localeCountry = locale.country.normalizeCountry()
        return localeCountry ?: DEFAULT_COUNTRY
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

    private fun String?.normalizeCountry(): String? {
        val country = this?.trim()?.uppercase(Locale.US)
        return country?.takeIf { it.matches(Regex("^[A-Z]{2}$")) }
    }
}
