package com.example.dle_prototype.data.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Multimodal Visual QA Processor.
 * Connects Camera & Image Upload flows with Ultralytics YOLO11n (Nano) and on-device OCR.
 */
object VisualQaProcessor {

    val PRESETS: List<VisualQaPreset> = listOf(
        VisualQaPreset(
            id = "physics_inclined_plane",
            title = "Physics: Inclined Plane Dynamics & Friction",
            subject = "Classical Mechanics",
            description = "A textbook problem analyzing force vectors, friction, and acceleration on an inclined ramp.",
            rawText = """
                PROBLEM 3.4: Dynamics on an Inclined Plane
                A block of mass m = 5.0 kg is placed on a ramp inclined at angle θ = 30° above the horizontal.
                The coefficient of kinetic friction between the block and the surface is μ_k = 0.25.
                (Take acceleration due to gravity g = 9.8 m/s²)
                
                EQUATIONS OF MOTION:
                • Normal Force: N = m * g * cos(θ)
                • Friction Force: f_k = μ_k * N = μ_k * m * g * cos(θ)
                • Downward Component: F_down = m * g * sin(θ)
                • Net Force: F_net = F_down - f_k = m * a
                
                QUESTIONS:
                1. Calculate the magnitude of the normal force N acting on the block.
                2. Determine the kinetic friction force f_k opposing the motion.
                3. Calculate the linear acceleration a of the block down the incline.
            """.trimIndent(),
            detectedClasses = listOf(
                Yolo11nClass.HEADING_TITLE,
                Yolo11nClass.QUESTION_STATEMENT,
                Yolo11nClass.DIAGRAM,
                Yolo11nClass.MATH_FORMULA
            )
        ),
        VisualQaPreset(
            id = "cs_coroutines_flow",
            title = "CS: Kotlin Coroutines & Flow Pipeline",
            subject = "Software Architecture",
            description = "Educational diagram and code block illustrating asynchronous stream buffering and flowOn.",
            rawText = """
                TOPIC: Asynchronous Reactive Streams with Kotlin Flow
                
                ```kotlin
                fun fetchTelemetryPipeline(context: CoroutineContext): Flow<SensorReading> = flow {
                    while (currentCoroutineContext().isActive) {
                        val sample = hardwareSensor.readBatch()
                        emit(sample)
                        delay(50)
                    }
                }
                .flowOn(Dispatchers.IO)
                .buffer(capacity = 64)
                .map { reading -> reading.normalize() }
                ```
                
                SYSTEM ARCHITECTURE:
                [Producer: Sensors (IO Thread)] ---> [Buffer Channel] ---> [Collector: Compose UI (Main Thread)]
                
                KEY QUESTIONS:
                1. Why does flowOn only change the upstream context and not the downstream collector?
                2. How does the buffer operator mitigate backpressure during heavy UI recompositions?
            """.trimIndent(),
            detectedClasses = listOf(
                Yolo11nClass.HEADING_TITLE,
                Yolo11nClass.CODE_BLOCK,
                Yolo11nClass.DIAGRAM,
                Yolo11nClass.QUESTION_STATEMENT
            )
        ),
        VisualQaPreset(
            id = "ml_yolo11n_architecture",
            title = "AI / ML: YOLO11n Nano Edge Model Architecture",
            subject = "Computer Vision & Edge ML",
            description = "Detailed neural network architecture of Ultralytics YOLO11n with quantization benchmarks.",
            rawText = """
                MODEL SPECIFICATION: Ultralytics YOLO11n (Nano Architecture)
                Parameter Count: 2.6M  |  GFLOPs: 6.5  |  Input Tensor: 640x640x3
                
                PIPELINE LAYOUT:
                [Input Image 640x640] ➔ [Backbone: Conv + C3k2 + SPPF] ➔ [Neck: C2PSA / PAN-FPN] ➔ [Head: Decoupled Anchor-Free]
                
                EDGE QUANTIZATION BENCHMARKS:
                • FP32 Baseline: 10.4 MB, Latency = 24.5 ms, mAP50-95 = 39.5
                • FP16 Half: 5.2 MB, Latency = 14.2 ms, mAP50-95 = 39.4
                • INT8 Quantized: 2.7 MB, Latency = 8.1 ms (NPU), mAP50-95 = 38.9
                
                ANALYSIS QUESTIONS:
                1. What architectural advantages does C3k2 offer over earlier C3/C2f blocks?
                2. Explain how INT8 post-training quantization achieves 3x latency reduction with minimal mAP loss.
            """.trimIndent(),
            detectedClasses = listOf(
                Yolo11nClass.HEADING_TITLE,
                Yolo11nClass.DIAGRAM,
                Yolo11nClass.TABLE_DATA,
                Yolo11nClass.QUESTION_STATEMENT
            )
        ),
        VisualQaPreset(
            id = "math_calculus_integration",
            title = "Math: Definite Integration & Area Under Curve",
            subject = "Calculus & Analysis",
            description = "Calculus textbook problem computing definite integrals and area between curves.",
            rawText = """
                CALCULUS II: Applications of Integration
                
                EVALUATION PROBLEM:
                Find the area of the region bounded by f(x) = x * sin(x) and the x-axis on the interval [0, π].
                
                ANALYTICAL FORMULA:
                Area = ∫[0 to π] (x * sin(x)) dx
                Using Integration by Parts:
                Let u = x  =>  du = dx
                Let dv = sin(x) dx  =>  v = -cos(x)
                
                ∫ u dv = u * v - ∫ v du
                = [-x * cos(x)][0 to π] - ∫[0 to π] (-cos(x)) dx
                = [-(π * cos(π)) - 0] + [sin(x)][0 to π]
                = -(π * (-1)) + (0 - 0) = π ≈ 3.14159
                
                QUESTIONS:
                1. Verify the integration by parts formula application.
                2. How does the trigonometric oscillation affect convergence on [0, 2π]?
            """.trimIndent(),
            detectedClasses = listOf(
                Yolo11nClass.HEADING_TITLE,
                Yolo11nClass.QUESTION_STATEMENT,
                Yolo11nClass.MATH_FORMULA,
                Yolo11nClass.DIAGRAM
            )
        )
    )

    /**
     * Processes any user-provided Bitmap (from device camera capture or gallery picker)
     * through YOLO11n Nano visual detection and on-device OCR.
     */
    suspend fun processImage(
        context: Context,
        bitmap: Bitmap,
        title: String,
        sourceType: VisualQaSourceType,
        customTextHint: String? = null
    ): VisualQaSource = withContext(Dispatchers.Default) {
        // 1. Run YOLO11n Nano detection
        val yoloResult = Yolo11nDetector.detect(bitmap)

        // 2. Run On-Device OCR text recognition
        val ocrResult = OnDeviceOcrEngine.recognizeText(bitmap, customTextHint)

        // 3. Correlate detections with extracted text
        val correlatedDetections = yoloResult.detections.map { detection ->
            val matchingBlocks = ocrResult.blocks.filter { block ->
                detection.box.intersects(block.box)
            }
            if (matchingBlocks.isNotEmpty()) {
                val snippet = matchingBlocks.take(3).joinToString("\n") { it.text }
                detection.copy(extractedText = snippet)
            } else {
                detection
            }
        }

        val enrichedYoloResult = yoloResult.copy(detections = correlatedDetections)

        // 4. Cache thumbnail/full image to file provider cache
        val imageUriString = saveBitmapToCache(context, bitmap)

        VisualQaSource(
            id = UUID.randomUUID().toString(),
            title = title,
            sourceType = sourceType,
            bitmap = bitmap,
            imageUri = imageUriString,
            yoloResult = enrichedYoloResult,
            ocrResult = ocrResult
        )
    }

    /**
     * Generates a high-resolution, graphically rich synthetic educational document bitmap
     * corresponding to a curated study preset.
     */
    suspend fun loadPreset(
        context: Context,
        preset: VisualQaPreset
    ): VisualQaSource = withContext(Dispatchers.Default) {
        val bitmap = renderPresetBitmap(preset)
        processImage(
            context = context,
            bitmap = bitmap,
            title = preset.title,
            sourceType = VisualQaSourceType.SAMPLE_PRESET,
            customTextHint = preset.rawText
        )
    }

    /**
     * Renders a crisp 800x1000 educational document card Bitmap with diagrams,
     * equations, headers, and code snippets.
     */
    private fun renderPresetBitmap(preset: VisualQaPreset): Bitmap {
        val width = 800
        val height = 1000
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply {
            color = AndroidColor.rgb(15, 23, 42) // Slate 900
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Border card
        val cardPaint = Paint().apply {
            color = AndroidColor.rgb(30, 41, 59) // Slate 800
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(24f, 24f, width - 24f, height - 24f), 24f, 24f, cardPaint)

        // Header accent bar
        val accentPaint = Paint().apply {
            color = AndroidColor.rgb(6, 182, 212) // Cyan 500
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(24f, 24f, width - 24f, 36f), 4f, 4f, accentPaint)

        // Title text
        val titlePaint = Paint().apply {
            color = AndroidColor.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(preset.subject.uppercase(), 48f, 75f, titlePaint)

        val subtitlePaint = Paint().apply {
            color = AndroidColor.rgb(148, 163, 184)
            textSize = 18f
            isAntiAlias = true
        }
        canvas.drawText(preset.title, 48f, 105f, subtitlePaint)

        // Draw preset-specific visual diagrams & content
        when (preset.id) {
            "physics_inclined_plane" -> drawPhysicsDiagram(canvas, width)
            "cs_coroutines_flow" -> drawCsPipelineDiagram(canvas, width)
            "ml_yolo11n_architecture" -> drawYoloArchitectureDiagram(canvas, width)
            "math_calculus_integration" -> drawCalculusCurveDiagram(canvas, width)
            else -> drawGenericDiagram(canvas, width)
        }

        // Draw formatted study text
        val textPaint = Paint().apply {
            color = AndroidColor.rgb(226, 232, 240)
            textSize = 15f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        var yPos = 480f
        val lines = preset.rawText.lines()
        for (line in lines) {
            if (yPos > height - 40) break
            if (line.startsWith("PROBLEM") || line.startsWith("TOPIC") || line.startsWith("MODEL") || line.startsWith("CALCULUS")) {
                textPaint.color = AndroidColor.rgb(56, 189, 248)
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            } else if (line.startsWith("EQUATIONS") || line.startsWith("QUESTIONS") || line.startsWith("PIPELINE") || line.startsWith("BENCHMARKS")) {
                textPaint.color = AndroidColor.rgb(245, 158, 11)
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            } else if (line.startsWith("fun ") || line.startsWith("```") || line.contains("Flow<")) {
                textPaint.color = AndroidColor.rgb(16, 185, 129)
                textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            } else {
                textPaint.color = AndroidColor.rgb(203, 213, 225)
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            canvas.drawText(line, 48f, yPos, textPaint)
            yPos += 22f
        }

        return bitmap
    }

    private fun drawPhysicsDiagram(canvas: Canvas, width: Int) {
        val diagBox = RectF(48f, 130f, width - 48f, 440f)
        val bgPaint = Paint().apply {
            color = AndroidColor.rgb(15, 23, 42)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(diagBox, 16f, 16f, bgPaint)

        val strokePaint = Paint().apply {
            color = AndroidColor.rgb(51, 65, 85)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(diagBox, 16f, 16f, strokePaint)

        // Draw Incline Wedge
        val wedgePaint = Paint().apply {
            color = AndroidColor.rgb(56, 189, 248)
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        canvas.drawLine(100f, 400f, 650f, 400f, wedgePaint) // Base
        canvas.drawLine(100f, 400f, 650f, 200f, wedgePaint) // Incline plane
        canvas.drawLine(650f, 200f, 650f, 400f, wedgePaint) // Vertical wall

        // Angle theta text
        val labelPaint = Paint().apply {
            color = AndroidColor.rgb(245, 158, 11)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("θ = 30°", 170f, 390f, labelPaint)

        // Block on incline
        val blockPaint = Paint().apply {
            color = AndroidColor.rgb(236, 72, 153)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.save()
        canvas.rotate(-20f, 380f, 300f)
        canvas.drawRoundRect(RectF(340f, 260f, 420f, 340f), 8f, 8f, blockPaint)

        // Normal force arrow
        val arrowPaint = Paint().apply {
            color = AndroidColor.rgb(16, 185, 129)
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawLine(380f, 260f, 380f, 180f, arrowPaint) // Normal force
        canvas.restore()

        canvas.drawText("N (Normal Force)", 370f, 170f, labelPaint)
        canvas.drawText("m = 5.0 kg", 360f, 290f, labelPaint)
    }

    private fun drawCsPipelineDiagram(canvas: Canvas, width: Int) {
        val diagBox = RectF(48f, 130f, width - 48f, 440f)
        val bgPaint = Paint().apply {
            color = AndroidColor.rgb(15, 23, 42)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(diagBox, 16f, 16f, bgPaint)

        val boxPaint = Paint().apply {
            color = AndroidColor.rgb(30, 58, 138)
            style = Paint.Style.FILL
        }
        val textPaint = Paint().apply {
            color = AndroidColor.WHITE
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // 3 Nodes: Producer -> Buffer -> Consumer
        canvas.drawRoundRect(RectF(80f, 240f, 240f, 330f), 12f, 12f, boxPaint)
        canvas.drawText("Flow Producer", 100f, 275f, textPaint)
        canvas.drawText("Dispatchers.IO", 100f, 305f, textPaint)

        boxPaint.color = AndroidColor.rgb(4, 120, 87)
        canvas.drawRoundRect(RectF(320f, 240f, 480f, 330f), 12f, 12f, boxPaint)
        canvas.drawText("Channel Buffer", 335f, 275f, textPaint)
        canvas.drawText("capacity = 64", 345f, 305f, textPaint)

        boxPaint.color = AndroidColor.rgb(109, 40, 217)
        canvas.drawRoundRect(RectF(560f, 240f, 720f, 330f), 12f, 12f, boxPaint)
        canvas.drawText("UI Collector", 580f, 275f, textPaint)
        canvas.drawText("Main Thread", 580f, 305f, textPaint)

        // Connecting arrows
        val arrowPaint = Paint().apply {
            color = AndroidColor.rgb(6, 182, 212)
            strokeWidth = 4f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawLine(240f, 285f, 320f, 285f, arrowPaint)
        canvas.drawLine(480f, 285f, 560f, 285f, arrowPaint)
    }

    private fun drawYoloArchitectureDiagram(canvas: Canvas, width: Int) {
        val diagBox = RectF(48f, 130f, width - 48f, 440f)
        val bgPaint = Paint().apply {
            color = AndroidColor.rgb(15, 23, 42)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(diagBox, 16f, 16f, bgPaint)

        val textPaint = Paint().apply {
            color = AndroidColor.rgb(56, 189, 248)
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Ultralytics YOLO11n Nano Feedforward Backbone", 80f, 175f, textPaint)

        val blockPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Draw Architecture blocks
        val blocks = listOf(
            "640x640 Input" to AndroidColor.rgb(51, 65, 85),
            "C3k2 Backbone" to AndroidColor.rgb(14, 165, 233),
            "SPPF Pooling" to AndroidColor.rgb(168, 85, 247),
            "C2PSA Attention" to AndroidColor.rgb(236, 72, 153),
            "Anchor-Free Head" to AndroidColor.rgb(16, 185, 129)
        )

        var xStart = 80f
        for ((name, colorVal) in blocks) {
            blockPaint.color = colorVal
            canvas.drawRoundRect(RectF(xStart, 230f, xStart + 115f, 340f), 10f, 10f, blockPaint)
            textPaint.color = AndroidColor.WHITE
            textPaint.textSize = 13f
            canvas.drawText(name.split(" ").first(), xStart + 10f, 275f, textPaint)
            canvas.drawText(name.split(" ").getOrElse(1) { "" }, xStart + 10f, 305f, textPaint)
            xStart += 130f
        }
    }

    private fun drawCalculusCurveDiagram(canvas: Canvas, width: Int) {
        val diagBox = RectF(48f, 130f, width - 48f, 440f)
        val bgPaint = Paint().apply {
            color = AndroidColor.rgb(15, 23, 42)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(diagBox, 16f, 16f, bgPaint)

        // Axis
        val axisPaint = Paint().apply {
            color = AndroidColor.rgb(100, 116, 139)
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawLine(100f, 370f, 700f, 370f, axisPaint) // X axis
        canvas.drawLine(150f, 170f, 150f, 400f, axisPaint) // Y axis

        // Shaded curve area
        val curvePaint = Paint().apply {
            color = AndroidColor.rgb(16, 185, 129)
            strokeWidth = 4f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        var prevX = 150f
        var prevY = 370f
        for (i in 0..100) {
            val t = (i / 100f) * Math.PI.toFloat()
            val x = 150f + (i / 100f) * 450f
            val yVal = (t * Math.sin(t.toDouble())).toFloat()
            val y = 370f - (yVal * 90f)
            canvas.drawLine(prevX, prevY, x, y, curvePaint)
            prevX = x
            prevY = y
        }

        val textPaint = Paint().apply {
            color = AndroidColor.rgb(245, 158, 11)
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("f(x) = x · sin(x)", 300f, 210f, textPaint)
        canvas.drawText("Area = π ≈ 3.14159", 300f, 250f, textPaint)
    }

    private fun drawGenericDiagram(canvas: Canvas, width: Int) {
        val diagBox = RectF(48f, 130f, width - 48f, 440f)
        val bgPaint = Paint().apply {
            color = AndroidColor.rgb(15, 23, 42)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(diagBox, 16f, 16f, bgPaint)
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): String {
        return try {
            val cacheDir = File(context.cacheDir, "camera").apply { mkdirs() }
            val file = File(cacheDir, "visual_qa_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            uri.toString()
        } catch (e: Exception) {
            ""
        }
    }
}
