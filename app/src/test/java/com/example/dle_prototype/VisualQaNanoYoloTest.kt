package com.example.dle_prototype

import com.example.dle_prototype.data.qa.QaChatEngine
import com.example.dle_prototype.data.vision.NormalizedBoundingBox
import com.example.dle_prototype.data.vision.OcrBlockType
import com.example.dle_prototype.data.vision.OnDeviceOcrEngine
import com.example.dle_prototype.data.vision.VisualQaProcessor
import com.example.dle_prototype.data.vision.Yolo11nClass
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VisualQaNanoYoloTest {

    @Test
    fun testNormalizedBoundingBoxCalculations() {
        val box = NormalizedBoundingBox(0.1f, 0.2f, 0.6f, 0.8f)
        assertEquals(0.5f, box.width, 0.001f)
        assertEquals(0.6f, box.height, 0.001f)
        assertEquals(0.35f, box.centerX, 0.001f)
        assertEquals(0.50f, box.centerY, 0.001f)

        val intersecting = NormalizedBoundingBox(0.3f, 0.4f, 0.8f, 0.9f)
        assertTrue(box.intersects(intersecting))

        val nonIntersecting = NormalizedBoundingBox(0.7f, 0.85f, 0.95f, 0.99f)
        assertFalse(box.intersects(nonIntersecting))
    }

    @Test
    fun testOcrTextClassification() = runBlocking {
        // Test question, code, and formula recognition
        val testText = """
            PROBLEM 1: Calculate the acceleration of the block down the plane.
            EQUATION: F_net = m * a
            val pipeline = flow { emit(1) }
            This is general descriptive textbook background text.
        """.trimIndent()

        val ocrResult = OnDeviceOcrEngine.recognizeText(bitmap = null, customExtractedText = testText)

        assertNotNull(ocrResult)
        assertTrue(ocrResult.wordCount > 10)
        assertTrue(ocrResult.blocks.isNotEmpty())

        val questionBlock = ocrResult.blocks.find { it.type == OcrBlockType.QUESTION }
        assertNotNull("Should identify question statement", questionBlock)

        val formulaBlock = ocrResult.blocks.find { it.type == OcrBlockType.MATH_EQUATION }
        assertNotNull("Should identify math equation", formulaBlock)

        val codeBlock = ocrResult.blocks.find { it.type == OcrBlockType.CODE }
        assertNotNull("Should identify code snippet", codeBlock)
    }

    @Test
    fun testVisualQaPresetsCoverage() {
        val presets = VisualQaProcessor.PRESETS
        assertEquals(4, presets.size)

        val physics = presets.find { it.id == "physics_inclined_plane" }
        assertNotNull(physics)
        assertTrue(physics!!.rawText.contains("Dynamics on an Inclined Plane"))
        assertTrue(physics.detectedClasses.contains(Yolo11nClass.DIAGRAM))
        assertTrue(physics.detectedClasses.contains(Yolo11nClass.MATH_FORMULA))

        val cs = presets.find { it.id == "cs_coroutines_flow" }
        assertNotNull(cs)
        assertTrue(cs!!.rawText.contains("flowOn(Dispatchers.IO)"))
        assertTrue(cs.detectedClasses.contains(Yolo11nClass.CODE_BLOCK))

        val yolo = presets.find { it.id == "ml_yolo11n_architecture" }
        assertNotNull(yolo)
        assertTrue(yolo!!.rawText.contains("Ultralytics YOLO11n"))
        assertTrue(yolo.rawText.contains("C3k2"))

        val math = presets.find { it.id == "math_calculus_integration" }
        assertNotNull(math)
        assertTrue(math!!.rawText.contains("Integration by Parts"))
    }

    @Test
    fun testQaChatEngineElevatedVisualResponses() = runBlocking {
        // Test physics response
        val physicsPrompt = "📸 **[ELEVATED VISUAL QA SOURCE: YOLO11n + OCR]**\nProblem: Dynamics on an inclined plane with friction"
        val physicsResponse = QaChatEngine.generateResponse(physicsPrompt, "testUser")

        assertTrue(physicsResponse.answer.contains("Normal Force"))
        assertTrue(physicsResponse.answer.contains("42.44"))
        assertTrue(physicsResponse.answer.contains("2.78"))
        assertEquals("Visual Physics QA (YOLO11n + OCR)", physicsResponse.category)
        assertTrue(physicsResponse.confidence >= 0.95f)

        // Test Coroutines Flow response
        val coroutinesPrompt = "📸 **[ELEVATED VISUAL QA SOURCE: YOLO11n + OCR]**\nExplain flowOn and buffer in this coroutines diagram"
        val csResponse = QaChatEngine.generateResponse(coroutinesPrompt, "testUser")

        assertTrue(csResponse.answer.contains("Context Preservation"))
        assertTrue(csResponse.answer.contains("Producer"))
        assertEquals("Visual Code QA (YOLO11n + OCR)", csResponse.category)

        // Test YOLO11n ML architecture response
        val yoloPrompt = "📸 **[ELEVATED VISUAL QA SOURCE: YOLO11n + OCR]**\nExplain the YOLO11n architecture and INT8 quantization"
        val yoloResponse = QaChatEngine.generateResponse(yoloPrompt, "testUser")

        assertTrue(yoloResponse.answer.contains("C3k2 Backbone"))
        assertTrue(yoloResponse.answer.contains("8.1 ms"))
        assertEquals("Edge AI QA (YOLO11n + OCR)", yoloResponse.category)

        // Test Calculus response
        val mathPrompt = "📸 **[ELEVATED VISUAL QA SOURCE: YOLO11n + OCR]**\nCalculate the calculus definite integral for area bounded"
        val mathResponse = QaChatEngine.generateResponse(mathPrompt, "testUser")

        assertTrue(mathResponse.answer.contains("Integration by Parts"))
        assertTrue(mathResponse.answer.contains("3.14159"))
        assertEquals("Visual Mathematics QA (YOLO11n + OCR)", mathResponse.category)
    }
}
