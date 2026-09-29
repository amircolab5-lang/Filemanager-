package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object FileIntentHelper {

    fun openFile(context: Context, file: File) {
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val authority = "${context.packageName}.provider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)
            val extension = file.extension.lowercase()
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(intent, "Open with Ticno").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open this file: ${e.localizedMessage ?: "No app found"}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareFile(context: Context, file: File) {
        shareFiles(context, listOf(file))
    }

    fun shareFiles(context: Context, files: List<File>) {
        if (files.isEmpty()) return

        try {
            val authority = "${context.packageName}.provider"
            // If folders are selected, gather files inside so external apps receive valid shareable streams
            val resolvedFiles = mutableListOf<File>()
            for (f in files) {
                if (f.isDirectory) {
                    val children = f.walkTopDown().filter { it.isFile && it.exists() && it.length() > 0 }.take(50).toList()
                    resolvedFiles.addAll(children)
                } else if (f.isFile && f.exists()) {
                    resolvedFiles.add(f)
                }
            }

            if (resolvedFiles.isEmpty()) {
                Toast.makeText(context, "No files found in selected item(s) to share", Toast.LENGTH_SHORT).show()
                return
            }

            if (resolvedFiles.size == 1) {
                val file = resolvedFiles.first()
                val uri = FileProvider.getUriForFile(context, authority, file)
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase()) ?: "*/*"

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share file via").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } else {
                val uris = ArrayList<Uri>()
                for (f in resolvedFiles) {
                    try {
                        uris.add(FileProvider.getUriForFile(context, authority, f))
                    } catch (e: Exception) {
                        // Skip if unresolvable
                    }
                }
                if (uris.isEmpty()) {
                    Toast.makeText(context, "Failed to resolve file URIs for sharing", Toast.LENGTH_SHORT).show()
                    return
                }
                val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "*/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share ${uris.size} files via").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
