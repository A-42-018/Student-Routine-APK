package com.alif.studentroutine.data

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.webkit.MimeTypeMap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.alif.studentroutine.data.entity.ClassNote
import java.io.File

object NoteAttachmentHelper {

    fun checklistItemCount(json: String): Int {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "[]") return 0
        return Regex("\"text\":").findAll(trimmed).count()
    }

    fun parseJsonArray(json: String): List<String> {
        val trimmed = json.trim().removePrefix("[").removeSuffix("]").trim()
        if (trimmed.isEmpty()) return emptyList()
        return trimmed.split(",").map { it.trim().removeSurrounding("\"") }
    }

    fun deleteFile(path: String) {
        runCatching { File(path).delete() }
    }

    fun deleteNoteAttachments(note: ClassNote) {
        parseJsonArray(note.imagePaths).forEach { deleteFile(it) }
        parseJsonArray(note.filePaths).forEach { deleteFile(it) }
    }

    fun openAttachment(context: Context, path: String, isImage: Boolean) {
        val file = File(path)
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val mime = when {
            isImage -> "image/*"
            else -> guessMimeType(file) ?: "*/*"
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "Open with"))
        }
    }

    private fun guessMimeType(file: File): String? {
        val extension = file.extension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
}

@Composable
fun NoteImagePreview(path: String, modifier: Modifier = Modifier) {
    val bitmap = remember(path) {
        runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            contentScale = ContentScale.Fit
        )
    }
}
