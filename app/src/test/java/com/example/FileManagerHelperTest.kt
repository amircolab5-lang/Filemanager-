package com.example

import com.example.model.CategoryType
import com.example.util.FileManagerHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FileManagerHelperTest {

    @Test
    fun testFormatFileSize() {
        assertEquals("0 B", FileManagerHelper.formatFileSize(0))
        assertEquals("500 B", FileManagerHelper.formatFileSize(500))
        assertEquals("1.0 KB", FileManagerHelper.formatFileSize(1024))
        assertEquals("1.0 MB", FileManagerHelper.formatFileSize(1024 * 1024))
        assertEquals("1.0 GB", FileManagerHelper.formatFileSize(1024L * 1024L * 1024L))
    }

    @Test
    fun testFileItemProperties() {
        val jpgFile = File("/storage/emulated/0/DCIM/photo.jpg")
        val item = FileManagerHelper.toFileItem(jpgFile)

        assertEquals("photo.jpg", item.name)
        assertEquals("jpg", item.extension)
        assertTrue(item.isImage)
        assertFalse(item.isVideo)
        assertFalse(item.isDirectory)
    }

    @Test
    fun testHiddenFileDetection() {
        val hiddenFile = File("/storage/emulated/0/.hidden_file")
        val normalFile = File("/storage/emulated/0/normal_file.txt")

        val hiddenItem = FileManagerHelper.toFileItem(hiddenFile)
        val normalItem = FileManagerHelper.toFileItem(normalFile)

        assertTrue(hiddenItem.isHidden)
        assertFalse(normalItem.isHidden)
    }
}
