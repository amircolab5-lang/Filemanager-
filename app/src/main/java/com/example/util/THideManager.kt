package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import com.example.model.FileItem
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID

data class THideMetadata(
    val id: String,
    val originalName: String,
    val originalPath: String,
    val vaultFileName: String,
    val size: Long,
    val mimeType: String,
    val hiddenTimestamp: Long
)

object THideManager {

    private const val PREFS_NAME = "thide_secure_prefs"
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_SECURITY_QUESTION = "security_question"
    private const val KEY_SECURITY_ANSWER_HASH = "security_answer_hash"
    private const val KEY_METADATA_REGISTRY = "metadata_registry_json"
    private const val VAULT_DIR_NAME = ".thide_vault"

    // In-memory unlock state
    var isUnlocked: Boolean = false

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun hashString(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun isPinConfigured(context: Context): Boolean {
        return getPrefs(context).contains(KEY_PIN_HASH)
    }

    fun setupPin(context: Context, pin: String, securityQuestion: String, securityAnswer: String): Boolean {
        if (pin.length < 4) return false
        val pinHash = hashString(pin)
        val answerHash = hashString(securityAnswer.trim().lowercase())

        getPrefs(context).edit()
            .putString(KEY_PIN_HASH, pinHash)
            .putString(KEY_SECURITY_QUESTION, securityQuestion)
            .putString(KEY_SECURITY_ANSWER_HASH, answerHash)
            .apply()

        isUnlocked = true
        ensureVaultDir(context)
        return true
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val storedHash = getPrefs(context).getString(KEY_PIN_HASH, null) ?: return false
        val matches = storedHash == hashString(pin)
        if (matches) {
            isUnlocked = true
        }
        return matches
    }

    fun getSecurityQuestion(context: Context): String {
        return getPrefs(context).getString(KEY_SECURITY_QUESTION, "What is your secret recovery word?")
            ?: "What is your secret recovery word?"
    }

    fun verifySecurityAnswer(context: Context, answer: String): Boolean {
        val storedHash = getPrefs(context).getString(KEY_SECURITY_ANSWER_HASH, null) ?: return false
        return storedHash == hashString(answer.trim().lowercase())
    }

    fun resetPinWithSecurityAnswer(context: Context, answer: String, newPin: String): Boolean {
        if (!verifySecurityAnswer(context, answer)) return false
        if (newPin.length < 4) return false
        getPrefs(context).edit()
            .putString(KEY_PIN_HASH, hashString(newPin))
            .apply()
        isUnlocked = true
        return true
    }

    fun lock() {
        isUnlocked = false
    }

    fun getVaultDir(context: Context): File {
        // Use storage root hidden dir if writable, fallback to app filesDir
        val storageRoot = FileManagerHelper.getStorageRoot()
        val primaryDir = File(storageRoot, VAULT_DIR_NAME)
        val dir = if (primaryDir.exists() || primaryDir.mkdirs() || primaryDir.canWrite()) {
            primaryDir
        } else {
            File(context.filesDir, VAULT_DIR_NAME)
        }
        if (!dir.exists()) dir.mkdirs()

        // Ensure .nomedia exists so gallery/players ignore this directory
        val noMedia = File(dir, ".nomedia")
        if (!noMedia.exists()) {
            try { noMedia.createNewFile() } catch (e: Throwable) {}
        }
        return dir
    }

    private fun ensureVaultDir(context: Context) {
        getVaultDir(context)
    }

    private fun loadMetadataMap(context: Context): MutableMap<String, THideMetadata> {
        val jsonStr = getPrefs(context).getString(KEY_METADATA_REGISTRY, null) ?: return mutableMapOf()
        val result = mutableMapOf<String, THideMetadata>()
        try {
            val root = JSONObject(jsonStr)
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val obj = root.getJSONObject(key)
                result[key] = THideMetadata(
                    id = obj.getString("id"),
                    originalName = obj.getString("originalName"),
                    originalPath = obj.getString("originalPath"),
                    vaultFileName = obj.getString("vaultFileName"),
                    size = obj.optLong("size", 0L),
                    mimeType = obj.optString("mimeType", "*/*"),
                    hiddenTimestamp = obj.optLong("hiddenTimestamp", System.currentTimeMillis())
                )
            }
        } catch (e: Throwable) {}
        return result
    }

    private fun saveMetadataMap(context: Context, map: Map<String, THideMetadata>) {
        try {
            val root = JSONObject()
            for ((key, meta) in map) {
                val obj = JSONObject()
                obj.put("id", meta.id)
                obj.put("originalName", meta.originalName)
                obj.put("originalPath", meta.originalPath)
                obj.put("vaultFileName", meta.vaultFileName)
                obj.put("size", meta.size)
                obj.put("mimeType", meta.mimeType)
                obj.put("hiddenTimestamp", meta.hiddenTimestamp)
                root.put(key, obj)
            }
            getPrefs(context).edit().putString(KEY_METADATA_REGISTRY, root.toString()).apply()
        } catch (e: Throwable) {}
    }

    /**
     * Hides files securely into T-Hide vault.
     * Moves file bytes, removes from public storage, and updates MediaStore.
     */
    fun hideFiles(context: Context, files: List<File>): Int {
        val vaultDir = getVaultDir(context)
        val metadataMap = loadMetadataMap(context)
        var successCount = 0

        for (src in files) {
            if (!src.exists() || !src.isFile) continue
            try {
                val id = UUID.randomUUID().toString()
                val ext = src.extension.let { if (it.isNotBlank()) ".$it" else "" }
                val vaultFileName = "thd_${id}$ext.enc"
                val destFile = File(vaultDir, vaultFileName)

                // Move file
                var moved = false
                try {
                    moved = src.renameTo(destFile)
                } catch (e: Throwable) {
                    moved = false
                }

                if (!moved) {
                    // Fallback copy + delete
                    FileInputStream(src).use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    destFile.setLastModified(src.lastModified())
                    src.delete()
                }

                // Remove from MediaStore
                try {
                    context.contentResolver.delete(
                        MediaStore.Files.getContentUri("external"),
                        "${MediaStore.Files.FileColumns.DATA} = ?",
                        arrayOf(src.absolutePath)
                    )
                } catch (e: Throwable) {}

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(src.absolutePath),
                    null,
                    null
                )

                val mimeType = getMimeType(src)
                val meta = THideMetadata(
                    id = id,
                    originalName = src.name,
                    originalPath = src.absolutePath,
                    vaultFileName = vaultFileName,
                    size = destFile.length(),
                    mimeType = mimeType,
                    hiddenTimestamp = System.currentTimeMillis()
                )
                metadataMap[vaultFileName] = meta
                successCount++
            } catch (e: Throwable) {}
        }

        saveMetadataMap(context, metadataMap)
        return successCount
    }

    private fun getMimeType(file: File): String {
        return FileManagerHelper.getFastMimeType(file.extension.lowercase(), file.isDirectory)
    }

    /**
     * Unhides/Restores a file from T-Hide vault back to device storage.
     */
    fun unhideFile(context: Context, item: FileItem, targetDir: File? = null): Boolean {
        val vaultDir = getVaultDir(context)
        val metadataMap = loadMetadataMap(context)
        val vaultFile = File(item.path)
        if (!vaultFile.exists()) return false

        val meta = metadataMap[vaultFile.name]
        val originalName = meta?.originalName ?: item.name.removeSuffix(".enc").removePrefix("thd_")
        val destinationDir = targetDir ?: meta?.originalPath?.let { File(it).parentFile }
            ?: File(FileManagerHelper.getStorageRoot(), "Download/Restored")

        if (!destinationDir.exists()) destinationDir.mkdirs()

        // Avoid overwriting existing file
        var destFile = File(destinationDir, originalName)
        if (destFile.exists()) {
            val baseName = destFile.nameWithoutExtension
            val ext = destFile.extension.let { if (it.isNotBlank()) ".$it" else "" }
            destFile = File(destinationDir, "${baseName}_restored_${System.currentTimeMillis() % 10000}$ext")
        }

        var restored = false
        try {
            restored = vaultFile.renameTo(destFile)
        } catch (e: Throwable) {
            restored = false
        }

        if (!restored) {
            try {
                FileInputStream(vaultFile).use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                vaultFile.delete()
                restored = true
            } catch (e: Throwable) {
                return false
            }
        }

        metadataMap.remove(vaultFile.name)
        saveMetadataMap(context, metadataMap)

        // Rescan into MediaStore
        MediaScannerConnection.scanFile(
            context,
            arrayOf(destFile.absolutePath),
            arrayOf(getMimeType(destFile)),
            null
        )

        return true
    }

    /**
     * Permanently deletes a file from the vault.
     */
    fun deletePermanently(context: Context, item: FileItem): Boolean {
        val vaultFile = File(item.path)
        val deleted = vaultFile.delete()
        if (deleted) {
            val metadataMap = loadMetadataMap(context)
            metadataMap.remove(vaultFile.name)
            saveMetadataMap(context, metadataMap)
        }
        return deleted
    }

    /**
     * Lists all hidden files currently in the vault.
     */
    fun getVaultFiles(context: Context): List<FileItem> {
        val vaultDir = getVaultDir(context)
        val metadataMap = loadMetadataMap(context)
        val files = vaultDir.listFiles() ?: return emptyList()

        val result = mutableListOf<FileItem>()
        for (f in files) {
            if (f.name == ".nomedia" || !f.isFile) continue
            val meta = metadataMap[f.name]
            val displayName = meta?.originalName ?: f.name.removeSuffix(".enc").removePrefix("thd_")
            val mimeType = meta?.mimeType ?: getMimeType(File(displayName))
            val ext = displayName.substringAfterLast('.', "").lowercase()

            result.add(
                FileItem(
                    file = f,
                    name = displayName,
                    path = f.absolutePath,
                    size = f.length(),
                    isDirectory = false,
                    isHidden = true,
                    extension = ext,
                    mimeType = mimeType,
                    lastModified = meta?.hiddenTimestamp ?: f.lastModified(),
                    formattedSize = FileManagerHelper.formatFileSize(f.length()),
                    formattedDate = FileManagerHelper.formatDate(meta?.hiddenTimestamp ?: f.lastModified())
                )
            )
        }

        return result.sortedByDescending { it.lastModified }
    }

    fun getVaultCount(context: Context): Int {
        val vaultDir = getVaultDir(context)
        return vaultDir.listFiles()?.count { it.isFile && it.name != ".nomedia" } ?: 0
    }
}
