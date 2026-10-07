package com.example.dle_prototype

import com.example.dle_prototype.data.ml.CustomModelInfo
import com.example.dle_prototype.data.ml.CustomModelManager
import com.example.dle_prototype.data.ml.ModelFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CustomModelManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testInspectModelHeaderGguf() {
        val file = tempFolder.newFile("test_model.gguf")
        FileOutputStream(file).use { fos ->
            val header = ByteBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN)
            // GGUF magic: 'G', 'G', 'U', 'F'
            header.put(0x47.toByte())
            header.put(0x47.toByte())
            header.put(0x55.toByte())
            header.put(0x46.toByte())
            // version: 3
            header.putInt(3)
            // tensor count: 128
            header.putLong(128L)
            // metadata kv count: 16
            header.putLong(16L)
            fos.write(header.array())
        }

        val (format, isValid, notice) = CustomModelManager.inspectModelHeader(file)
        assertEquals(ModelFormat.GGUF, format)
        assertTrue("GGUF header should be valid", isValid)
        assertTrue("Notice should mention GGUF and version", notice.contains("GGUF") && notice.contains("v3"))
    }

    @Test
    fun testInspectModelHeaderTflite() {
        val file = tempFolder.newFile("test_model.tflite")
        FileOutputStream(file).use { fos ->
            val header = ByteBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN)
            // offset 0..3: file length or padding
            header.putInt(1024)
            // offset 4..7: "TFL3"
            header.put('T'.code.toByte())
            header.put('F'.code.toByte())
            header.put('L'.code.toByte())
            header.put('3'.code.toByte())
            fos.write(header.array())
        }

        val (format, isValid, notice) = CustomModelManager.inspectModelHeader(file)
        assertEquals(ModelFormat.TFLITE, format)
        assertTrue("TFLite header should be valid", isValid)
        assertTrue("Notice should mention TFL3", notice.contains("TFL3"))
    }

    @Test
    fun testInspectModelHeaderUnknownBinary() {
        val file = tempFolder.newFile("random.bin")
        FileOutputStream(file).use { fos ->
            fos.write(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08))
        }

        val (format, isValid, _) = CustomModelManager.inspectModelHeader(file)
        assertEquals(ModelFormat.UNKNOWN, format)
        assertFalse("Random binary should not be considered valid", isValid)
    }

    @Test
    fun testInspectModelHeaderEmptyFile() {
        val file = tempFolder.newFile("empty.bin")
        val (format, isValid, notice) = CustomModelManager.inspectModelHeader(file)
        assertEquals(ModelFormat.UNKNOWN, format)
        assertFalse("Empty file should not be valid", isValid)
        assertTrue(notice.contains("empty"))
    }

    @Test
    fun testFormatFileSize() {
        assertEquals("0 B", CustomModelManager.formatFileSize(0))
        assertEquals("500 B", CustomModelManager.formatFileSize(500))
        assertEquals("1.0 KB", CustomModelManager.formatFileSize(1024))
        assertEquals("18.4 KB", CustomModelManager.formatFileSize(18841))
        assertEquals("45.00 MB", CustomModelManager.formatFileSize(45 * 1024 * 1024L))
        assertEquals("1.50 GB", CustomModelManager.formatFileSize((1.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun testCustomModelInfoJsonSerialization() {
        val model = CustomModelInfo(
            fileName = "gemma-2b-q4.gguf",
            fileSizeBytes = 1500000000L,
            format = ModelFormat.GGUF,
            importedAt = 1700000000000L,
            localFilePath = "/data/user/0/app/custom_models/gemma-2b-q4.gguf",
            isValid = true,
            magicHeader = "Magic: GGUF (v3)",
            architectureSummary = "Quantized Neural Weights Container",
            compatibilityNotice = "GGUF header verified"
        )

        val json = model.toJson()
        val parsed = CustomModelInfo.fromJson(json)

        assertNotNull("Parsed model should not be null", parsed)
        assertEquals(model.fileName, parsed!!.fileName)
        assertEquals(model.fileSizeBytes, parsed.fileSizeBytes)
        assertEquals(model.format, parsed.format)
        assertEquals(model.importedAt, parsed.importedAt)
        assertEquals(model.localFilePath, parsed.localFilePath)
        assertEquals(model.isValid, parsed.isValid)
        assertEquals(model.magicHeader, parsed.magicHeader)
        assertEquals(model.architectureSummary, parsed.architectureSummary)
        assertEquals(model.compatibilityNotice, parsed.compatibilityNotice)
    }
}
