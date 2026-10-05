package com.assembled.chat.internal

import kotlin.test.Test
import kotlin.test.assertEquals

class FileChooserTest {

    private val extensions = mapOf("pdf" to "application/pdf", "png" to "image/png")
    private fun mimeTypes(vararg accept: String) = fileChooserMimeTypes(arrayOf(*accept)) { extensions[it] }

    @Test
    fun `chat widget accept list keeps every type`() {
        assertEquals(
            listOf("image/png", "image/jpeg", "image/gif", "image/webp", "application/pdf"),
            mimeTypes("image/png,image/jpeg,image/gif,image/webp,application/pdf")
        )
    }

    @Test
    fun `accept types already split by the WebView are normalized`() {
        assertEquals(listOf("image/png", "application/pdf"), mimeTypes(" IMAGE/PNG ", "application/pdf", ""))
    }

    @Test
    fun `extensions are mapped to mime types`() {
        assertEquals(listOf("application/pdf", "image/*"), mimeTypes(".pdf, image/*"))
    }

    @Test
    fun `missing, wildcard, or unknown accept values allow any file`() {
        assertEquals(emptyList(), fileChooserMimeTypes(null) { null })
        assertEquals(emptyList(), mimeTypes(""))
        assertEquals(emptyList(), mimeTypes("image/png,*/*"))
        assertEquals(emptyList(), mimeTypes("image/png,.heic"))
        assertEquals(emptyList(), mimeTypes("image"))
    }
}
