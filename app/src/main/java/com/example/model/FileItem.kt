package com.example.model

import java.io.File

data class FileItem(
    val file: File,
    val name: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
    val isHidden: Boolean,
    val extension: String,
    val mimeType: String,
    val lastModified: Long,
    val formattedSize: String,
    val formattedDate: String,
    val itemCount: Int? = null,
    val isStatusMedia: Boolean = false,
    val isNomediaChild: Boolean = false,
    val bucketName: String = file.parentFile?.name ?: "Other",
    val isInstalledApk: Boolean = false,
    val packageName: String? = null,
    val versionName: String? = null,
    val apkAppIcon: android.graphics.drawable.Drawable? = null,
    val apkAppBitmap: android.graphics.Bitmap? = null
) {
    val isAudio: Boolean
        get() = !isDirectory && (extension.lowercase() in listOf("mp3", "wav", "m4a", "aac", "ogg", "opus", "flac", "wma", "amr", "m4p", "mid") || 
                (mimeType.startsWith("audio/") && extension.lowercase() !in listOf("mp4", "mkv", "avi", "mov", "3gp", "webm")))

    val isVideo: Boolean
        get() = !isDirectory && !isAudio && (extension.lowercase() in listOf("mp4", "mkv", "avi", "mov", "3gp", "webm", "ts", "flv", "wmv", "m4v") || mimeType.startsWith("video/"))

    val isImage: Boolean
        get() = !isDirectory && !isVideo && !isAudio && (extension.lowercase() in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "svg") || mimeType.startsWith("image/"))

    val isDocument: Boolean
        get() = !isDirectory && (extension.lowercase() in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "json", "xml", "log", "md", "rtf", "epub", "html", "htm") ||
                mimeType.startsWith("text/") || mimeType.contains("pdf") || mimeType.contains("document") || mimeType.contains("sheet") || mimeType.contains("presentation"))

    val isApk: Boolean
        get() = !isDirectory && (extension.lowercase() == "apk" || mimeType == "application/vnd.android.package-archive")

    val isArchive: Boolean
        get() = !isDirectory && (extension.lowercase() in listOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso") || mimeType.contains("zip") || mimeType.contains("compressed"))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileItem) return false
        return path == other.path
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}

enum class SortMode(val label: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    DATE_DESC("Date (Newest)"),
    DATE_ASC("Date (Oldest)"),
    SIZE_DESC("Size (Largest)"),
    SIZE_ASC("Size (Smallest)")
}

enum class ViewMode {
    LIST,
    GRID
}
