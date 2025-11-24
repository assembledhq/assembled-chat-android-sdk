package com.assembled.chat.example

import com.assembled.chat.models.AssembledChatConfiguration
import com.assembled.chat.models.UserData
import java.util.UUID

/**
 * Helper object that provides shared sample configuration data for the example app.
 */
object SampleData {

    private const val SAMPLE_COMPANY_ID = "your-company-id"

    fun configuration(): AssembledChatConfiguration {
        val userData = UserData(
            userId = "user-${UUID.randomUUID()}",
            email = "sample.user@example.com",
            name = "Sample User"
        )

        return AssembledChatConfiguration(
            companyId = SAMPLE_COMPANY_ID,
            buttonColor = "#4F46E5",
            debug = true,
            userData = userData
        )
    }
}

