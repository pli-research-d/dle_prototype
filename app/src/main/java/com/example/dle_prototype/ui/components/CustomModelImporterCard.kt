package com.example.dle_prototype.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.ml.CustomModelInfo
import com.example.dle_prototype.data.ml.CustomModelManager
import com.example.dle_prototype.data.ml.ModelFormat
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import kotlinx.coroutines.launch

@Composable
fun CustomModelImporterCard(
    modifier: Modifier = Modifier,
    onModelChanged: ((CustomModelInfo?) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeCustomModel by remember { mutableStateOf<CustomModelInfo?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }
    var isStatusError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        activeCustomModel = CustomModelManager.getActiveModel(context)
        onModelChanged?.invoke(activeCustomModel)
    }

    val modelFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isImporting = true
            importStatusMessage = "Streaming & validating model container..."
            isStatusError = false

            coroutineScope.launch {
                val result = CustomModelManager.importModelFromUri(context, uri)
                isImporting = false

                result.onSuccess { modelInfo ->
                    activeCustomModel = modelInfo
                    importStatusMessage = "Successfully imported ${modelInfo.fileName} (${modelInfo.format.displayName})!"
                    isStatusError = false
                    Toast.makeText(context, "Model ${modelInfo.fileName} imported", Toast.LENGTH_SHORT).show()
                    onModelChanged?.invoke(modelInfo)
                }.onFailure { err ->
                    importStatusMessage = "Failed to import model: ${err.message}"
                    isStatusError = true
                    Toast.makeText(context, "Import failed: ${err.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("custom_model_importer_card"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CyanAccent.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Custom Model Importer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Support for .gguf & .tflite weights",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (activeCustomModel != null) AmberAccent.copy(alpha = 0.15f) else EmeraldSuccess.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (activeCustomModel != null) activeCustomModel!!.format.displayName else "Baseline TFLite",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeCustomModel != null) AmberAccent else EmeraldSuccess,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Current Model Details Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF090E1A),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (activeCustomModel == null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Active Model: dle_model.tflite (Bundled)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFF1F5F9)
                            )
                        }
                        Text(
                            text = "Size: 18.4 KB • FlatBuffers Neural Weights • On-Device CPU/NNAPI",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    } else {
                        val model = activeCustomModel!!
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (model.isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (model.isValid) EmeraldSuccess else AmberAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = model.fileName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = CustomModelManager.formatFileSize(model.fileSizeBytes),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = model.magicHeader,
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = model.compatibilityNotice,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Status feedback message
            AnimatedVisibility(visible = importStatusMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isStatusError) Color(0xFF7F1D1D) else Color(0xFF064E3B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isStatusError) Icons.Default.Warning else Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = importStatusMessage ?: "",
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        // Open file picker allowing .gguf, .tflite, .bin, or any binary model
                        modelFilePickerLauncher.launch(
                            arrayOf(
                                "*/*",
                                "application/octet-stream"
                            )
                        )
                    },
                    enabled = !isImporting,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("import_model_file_button")
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                            color = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Importing...",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.FileOpen,
                            contentDescription = "Pick Model File",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Import Model",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                if (activeCustomModel != null) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                CustomModelManager.clearActiveModel(context)
                                activeCustomModel = null
                                importStatusMessage = "Restored default baseline TFLite model."
                                isStatusError = false
                                Toast.makeText(context, "Reset to baseline model", Toast.LENGTH_SHORT).show()
                                onModelChanged?.invoke(null)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_model_baseline_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset to Baseline",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Architectural explanation note
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "GGUF models (e.g. Llama/Gemma quantizations) are safely stored in app-private storage and verified via header inspection. TFLite models are loaded via FlatBuffers interpreter.",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
