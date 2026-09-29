package com.example.util

import android.os.Environment
import java.io.File

data class DocumentCollectionItem(
    val name: String,
    val folder: File,
    val fileCount: Int
)

object DocumentCollectionsManager {

    fun getCollectionsRoot(): File {
        val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val collectionsDir = File(docsDir, "Collections")
        if (!collectionsDir.exists()) {
            collectionsDir.mkdirs()
        }
        return collectionsDir
    }

    fun getCollections(): List<DocumentCollectionItem> {
        val root = getCollectionsRoot()
        val dirs = root.listFiles { f -> f.isDirectory } ?: emptyArray()
        return dirs.map { dir ->
            val count = dir.listFiles { f -> f.isFile }?.size ?: 0
            DocumentCollectionItem(
                name = dir.name,
                folder = dir,
                fileCount = count
            )
        }.sortedBy { it.name.lowercase() }
    }

    fun createCollection(name: String): File? {
        val sanitized = name.trim().replace(Regex("[/\\\\:*?\"<>|]"), "_")
        if (sanitized.isBlank()) return null
        val root = getCollectionsRoot()
        val target = File(root, sanitized)
        if (!target.exists()) {
            target.mkdirs()
        }
        return target
    }
}
