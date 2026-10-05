package com.assembled.chat.internal

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * Opens the system picker for `<input type="file">` in the chat WebView and returns the picked
 * content URIs to the page.
 */
internal class FileChooser(private val activity: ComponentActivity) {

    companion object {
        private const val TAG = "AssembledChat"
        private val nextKey = AtomicInteger()
    }

    private var pendingCallback: ValueCallback<Array<Uri>>? = null

    private val launcher: ActivityResultLauncher<Intent> = activity.activityResultRegistry.register(
        "com.assembled.chat.file_chooser.${nextKey.incrementAndGet()}",
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        deliver(parseFileChooserResult(result.resultCode, result.data))
    }

    /**
     * Always returns true: the callback is invoked exactly once, with null when the picker is
     * cancelled or can't be opened.
     */
    fun show(
        callback: ValueCallback<Array<Uri>>,
        params: WebChromeClient.FileChooserParams?
    ): Boolean {
        // WebView ignores further file inputs until the previous callback is answered.
        deliver(null)
        pendingCallback = callback

        val allowMultiple = params?.mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE
        val intent = buildFileChooserIntent(params?.acceptTypes, allowMultiple)
        try {
            launcher.launch(intent)
        } catch (error: ActivityNotFoundException) {
            Log.w(TAG, "No app available to pick files", error)
            deliver(null)
        }
        return true
    }

    fun dispose() {
        deliver(null)
        launcher.unregister()
    }

    private fun deliver(uris: Array<Uri>?) {
        val callback = pendingCallback ?: return
        pendingCallback = null
        callback.onReceiveValue(uris)
    }
}

internal fun Context.findComponentActivity(): ComponentActivity? {
    var context: Context? = this
    while (context != null) {
        if (context is ComponentActivity) return context
        if (context is Activity) return null
        context = (context as? ContextWrapper)?.baseContext
    }
    return null
}

internal fun buildFileChooserIntent(acceptTypes: Array<String>?, allowMultiple: Boolean): Intent {
    val mimeTypes = fileChooserMimeTypes(acceptTypes) { extension ->
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    return Intent(Intent.ACTION_GET_CONTENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = mimeTypes.singleOrNull() ?: "*/*"
        if (mimeTypes.size > 1) {
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes.toTypedArray())
        }
        if (allowMultiple) {
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
    }
}

/**
 * Converts the input's `accept` values (MIME types or `.ext` entries, possibly comma-separated)
 * into MIME types for the picker intent. Returns an empty list (any file) when the input accepts
 * anything or an entry can't be mapped, so we never hide files the page would accept.
 */
internal fun fileChooserMimeTypes(
    acceptTypes: Array<String>?,
    mimeTypeForExtension: (String) -> String?
): List<String> {
    val entries = acceptTypes.orEmpty()
        .flatMap { it.split(',') }
        .map { it.trim().lowercase(Locale.US) }
        .filter { it.isNotEmpty() }

    val mimeTypes = entries.map { entry ->
        when {
            entry.startsWith(".") -> mimeTypeForExtension(entry.substring(1)) ?: return emptyList()
            entry.contains('/') -> entry
            else -> return emptyList()
        }
    }.distinct()

    return if (mimeTypes.contains("*/*")) emptyList() else mimeTypes
}

internal fun parseFileChooserResult(resultCode: Int, data: Intent?): Array<Uri>? {
    if (resultCode != Activity.RESULT_OK || data == null) {
        return null
    }

    val uris = mutableListOf<Uri>()
    data.clipData?.let { clipData ->
        for (i in 0 until clipData.itemCount) {
            clipData.getItemAt(i).uri?.let(uris::add)
        }
    }
    data.data?.let(uris::add)

    return uris.distinct().takeIf { it.isNotEmpty() }?.toTypedArray()
}
