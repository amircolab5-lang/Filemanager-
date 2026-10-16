package com.example.model

import java.io.File

data class StorageVolumeInfo(
    val name: String,
    val rootDir: File,
    val isRemovable: Boolean,
    val isSdCard: Boolean,
    val totalBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val formattedTotal: String = "0 B",
    val formattedUsed: String = "0 B",
    val formattedFree: String = "0 B",
    val isMounted: Boolean = true
)
