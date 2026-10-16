package com.example.model

data class StorageStats(
    val totalBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val usedBytes: Long = 0L,
    val usedPercentage: Float = 0f,
    val formattedTotal: String = "0 B",
    val formattedUsed: String = "0 B",
    val formattedFree: String = "0 B",
    val imageBytes: Long = 0L,
    val videoBytes: Long = 0L,
    val audioBytes: Long = 0L,
    val documentBytes: Long = 0L,
    val apkBytes: Long = 0L,
    val archiveBytes: Long = 0L,
    val otherBytes: Long = 0L
)
