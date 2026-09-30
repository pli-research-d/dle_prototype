package com.example.dle_prototype.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.User
import com.example.dle_prototype.data.ml.ModelWeights
import com.example.dle_prototype.data.ml.OnDeviceTrainableModel
import com.example.dle_prototype.data.ml.TrainingCheckpoint
import com.example.dle_prototype.data.ml.TrainingProgress
import com.example.dle_prototype.data.ml.TrainingResult
import com.example.dle_prototype.data.ml.TrainingSample
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingStudioScreen(
    user: User,
    dbHelper: DatabaseHelper,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var currentWeights by remember { mutableStateOf<ModelWeights?>(null) }
    var trainingDataset by remember { mutableStateOf<List<TrainingSample>>(emptyList()) }
    var epochs by remember { mutableIntStateOf(30) }
    var learningRate by remember { mutableFloatStateOf(0.05f) }

    var isTraining by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableStateOf<TrainingProgress?>(null) }
    var lastTrainingResult by remember { mutableStateOf<TrainingResult?>(null) }
    var activeCheckpoint by remember { mutableStateOf<TrainingCheckpoint?>(null) }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportPayload by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    BackHandler { onBack() }

    suspend fun refreshData() {
        currentWeights = dbHelper.loadModelWeights(user.username)
        trainingDataset = dbHelper.generateTrainingDataset(user.username)
        activeCheckpoint = dbHelper.getLatestResumableCheckpoint(user.username)
    }

    LaunchedEffect(user.username) {
        refreshData()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "On-Device Training Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Edge Backpropagation & Weight Tuning",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("training_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Model Status Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ModelTraining,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Active Model Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (currentWeights != null) EmeraldSuccess.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.primaryContainer
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (currentWeights != null) "Personalized v${currentWeights?.version}" else "Baseline TFLite",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (currentWeights != null) EmeraldSuccess else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    currentWeights?.let { w ->
                        Text(
                            text = "Trained on device for ${w.trainedEpochs} epochs. Final Loss: ${"%.4f".format(w.finalLoss)}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    exportPayload = w.toJson()
                                    showExportDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_weights_button")
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export Weights")
                            }
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        dbHelper.resetModelWeights(user.username)
                                        refreshData()
                                        snackbarMessage = "Reset to baseline TFLite model."
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reset_weights_button")
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reset")
                            }
                        }
                    } ?: run {
                        Text(
                            text = "Currently using static pre-trained TFLite weights. Train on device below to personalize weights to your learning pace!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Resumable Checkpoint Card (Crash & App Restart Recovery)
            activeCheckpoint?.let { cp ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resumable_checkpoint_card"),
                    shape = RoundedCornerShape(20.dp),
                    color = AmberAccent.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AmberAccent)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(AmberAccent.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Restore,
                                        contentDescription = null,
                                        tint = AmberAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Unfinished Training Session",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Recovered from local SQLite storage",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AmberAccent.copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Epoch ${cp.currentEpoch}/${cp.targetEpochs}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "A prior training session was safely checkpointed at Epoch ${cp.currentEpoch} (Loss: ${"%.4f".format(cp.currentLoss)}). You can resume backpropagation from Epoch ${cp.currentEpoch + 1} without losing gradient progress.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isTraining = true
                                        lastTrainingResult = null
                                        val trainer = OnDeviceTrainableModel(initialWeights = cp.weights)
                                        val result = trainer.train(
                                            dataset = trainingDataset,
                                            epochs = cp.targetEpochs,
                                            startEpoch = cp.currentEpoch + 1,
                                            initialLossHistory = cp.lossHistory,
                                            learningRate = learningRate,
                                            momentum = 0.9f,
                                            checkpointInterval = 3,
                                            onCheckpoint = { ep, total, loss, w, hist ->
                                                dbHelper.saveTrainingCheckpoint(
                                                    TrainingCheckpoint(
                                                        username = user.username,
                                                        sessionId = cp.sessionId,
                                                        currentEpoch = ep,
                                                        targetEpochs = total,
                                                        currentLoss = loss,
                                                        lossHistory = hist,
                                                        weights = w
                                                    )
                                                )
                                            },
                                            onProgress = { prog -> currentProgress = prog }
                                        )
                                        lastTrainingResult = result
                                        isTraining = false
                                        dbHelper.saveModelWeights(user.username, result.weights)
                                        dbHelper.markCheckpointCompleted(user.username)
                                        refreshData()
                                        snackbarMessage = "Resumed training successfully completed!"
                                    }
                                },
                                enabled = !isTraining,
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(44.dp)
                                    .testTag("resume_training_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Resume Epoch ${cp.currentEpoch + 1}", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        dbHelper.clearTrainingCheckpoint(user.username)
                                        activeCheckpoint = null
                                        snackbarMessage = "Checkpoint cleared."
                                    }
                                },
                                enabled = !isTraining,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("discard_checkpoint_button")
                            ) {
                                Text("Discard")
                            }
                        }
                    }
                }
            }

            // Training Dataset Specs Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Training Dataset (${trainingDataset.size} Samples)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Extracted from your local SQLite quiz attempts, session duration telemetry, and calibrated domain heuristics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Hyperparameters Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Training Hyperparameters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Training Epochs: $epochs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = epochs.toFloat(),
                        onValueChange = { epochs = it.toInt() },
                        valueRange = 10f..100f,
                        steps = 8,
                        enabled = !isTraining,
                        modifier = Modifier.testTag("epochs_slider")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Learning Rate (η): ${"%.2f".format(learningRate)} (Momentum: 0.9)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = learningRate,
                        onValueChange = { learningRate = it },
                        valueRange = 0.01f..0.15f,
                        enabled = !isTraining,
                        modifier = Modifier.testTag("lr_slider")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isTraining = true
                                lastTrainingResult = null
                                val sessionId = UUID.randomUUID().toString()
                                val trainer = OnDeviceTrainableModel(initialWeights = currentWeights)
                                val result = trainer.train(
                                    dataset = trainingDataset,
                                    epochs = epochs,
                                    startEpoch = 1,
                                    learningRate = learningRate,
                                    momentum = 0.9f,
                                    checkpointInterval = 3,
                                    onCheckpoint = { ep, total, loss, w, hist ->
                                        dbHelper.saveTrainingCheckpoint(
                                            TrainingCheckpoint(
                                                username = user.username,
                                                sessionId = sessionId,
                                                currentEpoch = ep,
                                                targetEpochs = total,
                                                currentLoss = loss,
                                                lossHistory = hist,
                                                weights = w
                                            )
                                        )
                                    },
                                    onProgress = { prog -> currentProgress = prog }
                                )
                                lastTrainingResult = result
                                isTraining = false
                                // Auto-save to SQLite and mark checkpoint completed
                                dbHelper.saveModelWeights(user.username, result.weights)
                                dbHelper.markCheckpointCompleted(user.username)
                                refreshData()
                                snackbarMessage = "Model trained & deployed locally!"
                            }
                        },
                        enabled = !isTraining && trainingDataset.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_training_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isTraining) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Training Epoch ${currentProgress?.epoch ?: 1}/$epochs...")
                        } else {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start On-Device Training")
                        }
                    }
                }
            }

            // Training Progress & Results
            if (isTraining) {
                currentProgress?.let { prog ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Backpropagation Epoch ${prog.epoch}/${prog.totalEpochs}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Loss: ${"%.4f".format(prog.loss)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { prog.epoch.toFloat() / prog.totalEpochs.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }

            // Completed Training Summary Card
            lastTrainingResult?.let { res ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = EmeraldSuccess.copy(alpha = 0.10f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Training Complete!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Initial MSE Loss",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "%.4f".format(res.initialLoss),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column {
                                Text(
                                    text = "Final MSE Loss",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "%.4f".format(res.finalLoss),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column {
                                Text(
                                    text = "Loss Reduction",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "-${"%.1f".format(res.lossReductionPercent)}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "New model weights saved in SQLite. Your cognitive trait radar on the dashboard will now reflect your newly personalized model!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            snackbarMessage?.let { msg ->
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Federated Learning Weight Vector") },
            text = {
                Column {
                    Text(
                        text = "Serialized JSON model parameters for federated gradient aggregation:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Text(
                            text = exportPayload,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportPayload))
                        showExportDialog = false
                        snackbarMessage = "Weight vector copied to clipboard!"
                    }
                ) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
