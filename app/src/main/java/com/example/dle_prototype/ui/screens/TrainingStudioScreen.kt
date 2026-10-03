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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.dle_prototype.data.ml.TrainingLogEntry
import com.example.dle_prototype.data.ml.LogLevel
import com.example.dle_prototype.data.ml.LogCategory
import com.example.dle_prototype.data.ml.SystemResourceMonitor
import com.example.dle_prototype.ui.components.SystemResourceMonitorCard
import com.example.dle_prototype.ui.components.TrainingLogCard
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val resourceMonitor = remember { SystemResourceMonitor(context) }
    val resourceSnapshot by resourceMonitor.snapshot.collectAsStateWithLifecycle()
    val resourceHistory by resourceMonitor.history.collectAsStateWithLifecycle()
    var autoThrottleEnabled by remember { mutableStateOf(true) }
    var batterySaverEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        resourceMonitor.startMonitoring(1000L)
    }

    DisposableEffect(Unit) {
        onDispose { resourceMonitor.release() }
    }

    var currentWeights by remember { mutableStateOf<ModelWeights?>(null) }
    var trainingDataset by remember { mutableStateOf<List<TrainingSample>>(emptyList()) }
    var epochs by remember { mutableIntStateOf(30) }
    var learningRate by remember { mutableFloatStateOf(0.05f) }
    var checkpointInterval by remember { mutableIntStateOf(3) }
    var autoRevertOnDrop by remember { mutableStateOf(true) }

    var isTraining by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableStateOf<TrainingProgress?>(null) }
    var lastTrainingResult by remember { mutableStateOf<TrainingResult?>(null) }
    var activeCheckpoint by remember { mutableStateOf<TrainingCheckpoint?>(null) }
    var savedCheckpoints by remember { mutableStateOf<List<TrainingCheckpoint>>(emptyList()) }
    var trainingLogs by remember { mutableStateOf<List<TrainingLogEntry>>(emptyList()) }
    var accuracyDropAlert by remember { mutableStateOf<String?>(null) }
    var currentEvaluatedAccuracy by remember { mutableFloatStateOf(0f) }

    var showManualSnapshotDialog by remember { mutableStateOf(false) }
    var manualSnapshotName by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportPayload by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    fun evaluateValidationAccuracy(w: ModelWeights, dataset: List<TrainingSample>): Float {
        if (dataset.isEmpty()) return 85f
        val model = OnDeviceTrainableModel(5, 8, 4, initialWeights = w)
        var correct = 0
        var total = 0
        for (sample in dataset) {
            val pred = model.forward(sample.inputs)
            for (i in 0 until 4) {
                total++
                if (kotlin.math.abs(pred[i] - sample.targets[i]) <= 0.18f) {
                    correct++
                }
            }
        }
        return if (total > 0) (correct.toFloat() / total) * 100f else 80f
    }

    BackHandler { onBack() }

    suspend fun refreshData() {
        currentWeights = dbHelper.loadModelWeights(user.username)
        trainingDataset = dbHelper.generateTrainingDataset(user.username)
        activeCheckpoint = dbHelper.getLatestResumableCheckpoint(user.username)
        savedCheckpoints = dbHelper.getTrainingCheckpoints(user.username, 30)
        trainingLogs = dbHelper.initializeDefaultTrainingLogsIfEmpty(user.username)
        currentWeights?.let { w ->
            currentEvaluatedAccuracy = evaluateValidationAccuracy(w, trainingDataset)
        }
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Trained for ${w.trainedEpochs} epochs • Loss: ${"%.4f".format(w.finalLoss)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldSuccess.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Accuracy: ${"%.1f".format(currentEvaluatedAccuracy)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    manualSnapshotName = "Manual Snapshot ${savedCheckpoints.size + 1}"
                                    showManualSnapshotDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("take_snapshot_button")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Snapshot State")
                            }
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
                                Text("Export")
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
                                    .weight(0.8f)
                                    .testTag("reset_weights_button")
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
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

            // Real-Time System Resource & Thermal Monitor Card
            SystemResourceMonitorCard(
                snapshot = resourceSnapshot,
                history = resourceHistory,
                autoThrottleEnabled = autoThrottleEnabled,
                onToggleAutoThrottle = { autoThrottleEnabled = it },
                batterySaverEnabled = batterySaverEnabled,
                onToggleBatterySaver = {
                    batterySaverEnabled = it
                    resourceMonitor.setBatterySaverEnabled(it)
                },
                onToggleSimulation = { enableSim ->
                    resourceMonitor.setSimulationStressMode(enableSim)
                },
                onToggleSimulatedLowBattery = { enableLowBat ->
                    resourceMonitor.setSimulationLowBattery(enableLowBat)
                }
            )

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

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Checkpoint Interval: Save Every $checkpointInterval Epochs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 3, 5).forEach { interval ->
                            FilterChip(
                                selected = checkpointInterval == interval,
                                onClick = { checkpointInterval = interval },
                                label = { Text("Every $interval Ep") },
                                enabled = !isTraining
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Revert on Accuracy Drop",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Roll back weights to highest accuracy state if accuracy drops by >6%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoRevertOnDrop,
                            onCheckedChange = { autoRevertOnDrop = it },
                            enabled = !isTraining
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isTraining = true
                                lastTrainingResult = null
                                accuracyDropAlert = null
                                val sessionId = UUID.randomUUID().toString()
                                dbHelper.recordTrainingLog(
                                    TrainingLogEntry(
                                        username = user.username,
                                        sessionId = sessionId,
                                        timestamp = System.currentTimeMillis(),
                                        level = LogLevel.INFO,
                                        category = LogCategory.PARAM_ADJUSTMENT,
                                        title = "Training Dispatched (η: $learningRate, $epochs epochs)",
                                        message = "Optimizer: SGD Momentum (0.90), Checkpoint Interval: $checkpointInterval, Auto-revert guard: $autoRevertOnDrop",
                                        detailsJson = "{\"learningRate\": $learningRate, \"targetEpochs\": $epochs, \"interval\": $checkpointInterval, \"autoRevert\": $autoRevertOnDrop, \"samples\": ${trainingDataset.size}}"
                                    )
                                )
                                val pacingDelay = if (resourceSnapshot.isBatterySaverEngaged) {
                                    resourceSnapshot.trainingPacingDelayMs
                                } else if (autoThrottleEnabled && resourceSnapshot.isThrottling) {
                                    25L
                                } else {
                                    0L
                                }
                                if (resourceSnapshot.isBatterySaverEngaged) {
                                    dbHelper.recordTrainingLog(
                                        TrainingLogEntry(
                                            username = user.username,
                                            sessionId = sessionId,
                                            timestamp = System.currentTimeMillis(),
                                            level = LogLevel.WARNING,
                                            category = LogCategory.SYSTEM,
                                            title = "Battery Saver: Training Throttled",
                                            message = "Battery: ${resourceSnapshot.batteryPercent}%${if (resourceSnapshot.isCharging) " (Charging)" else ""} • Thermal: ${resourceSnapshot.thermalStatus.label}. Paced with +${pacingDelay}ms cooldown yield & 3s polling.",
                                            detailsJson = "{\"battery\": ${resourceSnapshot.batteryPercent}, \"isLowBattery\": ${resourceSnapshot.isLowBattery}, \"pacingMs\": $pacingDelay, \"pollingMs\": ${resourceSnapshot.pollingIntervalMs}}"
                                        )
                                    )
                                } else if (pacingDelay > 0) {
                                    dbHelper.recordTrainingLog(
                                        TrainingLogEntry(
                                            username = user.username,
                                            sessionId = sessionId,
                                            timestamp = System.currentTimeMillis(),
                                            level = LogLevel.WARNING,
                                            category = LogCategory.SYSTEM,
                                            title = "Thermal Guard: Inter-Epoch Pacing Engaged",
                                            message = "Thermal Status: ${resourceSnapshot.thermalStatus.label} (${"%.1f".format(resourceSnapshot.temperatureCelsius ?: 0f)}°C). Backpropagation paced with +${pacingDelay}ms cooldown yield.",
                                            detailsJson = "{\"thermalStatus\": \"${resourceSnapshot.thermalStatus.name}\", \"cpu\": ${resourceSnapshot.cpuUsagePercent}, \"pacingDelayMs\": $pacingDelay}"
                                        )
                                    )
                                }
                                val trainer = OnDeviceTrainableModel(initialWeights = currentWeights)
                                val result = trainer.train(
                                    dataset = trainingDataset,
                                    epochs = epochs,
                                    startEpoch = 1,
                                    learningRate = learningRate,
                                    momentum = 0.9f,
                                    checkpointInterval = checkpointInterval,
                                    accuracyEvaluator = { w -> evaluateValidationAccuracy(w, trainingDataset) },
                                    autoRevertOnDrop = autoRevertOnDrop,
                                    accuracyDropThreshold = 6.0f,
                                    thermalPacingDelayMs = pacingDelay,
                                    onCheckpointExtended = { ep, total, loss, acc, w, hist, isBest, isDrop ->
                                        val trigger = if (isDrop) "ACCURACY_DROP" else if (isBest) "BEST_ACCURACY" else "PERIODIC"
                                        val name = if (isDrop) "Epoch $ep (Accuracy Drop Alert)" else if (isBest) "Epoch $ep (Best Accuracy ⭐)" else "Epoch $ep Periodic Save"
                                        val cp = TrainingCheckpoint(
                                            username = user.username,
                                            sessionId = sessionId,
                                            checkpointName = name,
                                            currentEpoch = ep,
                                            targetEpochs = total,
                                            currentLoss = loss,
                                            accuracyPct = acc,
                                            lossHistory = hist,
                                            weights = w,
                                            isCompleted = (ep == total),
                                            isBest = isBest,
                                            triggerType = trigger
                                        )
                                        dbHelper.saveTrainingCheckpoint(cp)
                                        savedCheckpoints = dbHelper.getTrainingCheckpoints(user.username, 30)
                                        dbHelper.recordTrainingLog(
                                            TrainingLogEntry(
                                                username = user.username,
                                                sessionId = sessionId,
                                                timestamp = System.currentTimeMillis(),
                                                level = if (isDrop) LogLevel.WARNING else LogLevel.SUCCESS,
                                                category = LogCategory.CHECKPOINT,
                                                title = if (isDrop) "Checkpoint Alert ($name)" else "Checkpoint Saved ($name)",
                                                message = "Epoch $ep/$total • Loss: ${"%.4f".format(loss)} • Accuracy: ${"%.1f".format(acc)}% • Trigger: $trigger",
                                                detailsJson = "{\"epoch\": $ep, \"loss\": $loss, \"accuracy\": $acc, \"trigger\": \"$trigger\", \"isBest\": $isBest}"
                                            )
                                        )
                                    },
                                    onAccuracyDrop = { ep, currLoss, currAcc, bestEp, bestL, bestAcc, bestW ->
                                        accuracyDropAlert = if (autoRevertOnDrop) {
                                            "Overfitting detected at Epoch $ep! Accuracy dropped from ${"%.1f".format(bestAcc)}% to ${"%.1f".format(currAcc)}%. Weights automatically rolled back to Epoch $bestEp."
                                        } else {
                                            "Accuracy dropped from ${"%.1f".format(bestAcc)}% (Epoch $bestEp) to ${"%.1f".format(currAcc)}% (Epoch $ep)."
                                        }
                                        dbHelper.recordTrainingLog(
                                            TrainingLogEntry(
                                                username = user.username,
                                                sessionId = sessionId,
                                                timestamp = System.currentTimeMillis(),
                                                level = LogLevel.WARNING,
                                                category = LogCategory.EARLY_STOPPING,
                                                title = "Overfitting Alert: Generalization Degradation",
                                                message = "Accuracy dropped from ${"%.1f".format(bestAcc)}% to ${"%.1f".format(currAcc)}% at Epoch $ep. ${if (autoRevertOnDrop) "Weights rolled back to Epoch $bestEp." else "Early stopping recommended."}",
                                                detailsJson = "{\"epoch\": $ep, \"bestEpoch\": $bestEp, \"accuracy\": $currAcc, \"bestAccuracy\": $bestAcc, \"autoReverted\": $autoRevertOnDrop}"
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
                                dbHelper.recordTrainingLog(
                                    TrainingLogEntry(
                                        username = user.username,
                                        sessionId = sessionId,
                                        timestamp = System.currentTimeMillis(),
                                        level = LogLevel.SUCCESS,
                                        category = LogCategory.CONVERGENCE,
                                        title = "Training Session Finished",
                                        message = "Loss reduced from ${"%.4f".format(result.initialLoss)} to ${"%.4f".format(result.finalLoss)} (-${"%.1f".format(result.lossReductionPercent)}%).",
                                        detailsJson = "{\"initialLoss\": ${result.initialLoss}, \"finalLoss\": ${result.finalLoss}, \"reduction\": ${result.lossReductionPercent}}"
                                    )
                                )
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

            // Accuracy Drop / Overfitting Alert Banner
            accuracyDropAlert?.let { alertText ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("accuracy_drop_alert_banner"),
                    shape = RoundedCornerShape(16.dp),
                    color = AmberAccent.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AmberAccent)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Accuracy Drop / Overfitting Alert",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = alertText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val bestCp = savedCheckpoints.firstOrNull { it.isBest } ?: savedCheckpoints.minByOrNull { it.currentLoss }
                        if (bestCp != null && !autoRevertOnDrop) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        dbHelper.revertModelToCheckpoint(user.username, bestCp.id)
                                        refreshData()
                                        accuracyDropAlert = null
                                        snackbarMessage = "Reverted to Epoch ${bestCp.currentEpoch} (Loss: ${"%.4f".format(bestCp.currentLoss)}, Accuracy: ${"%.1f".format(bestCp.accuracyPct)}%)!"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("revert_to_best_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Revert to Best State (Epoch ${bestCp.currentEpoch} • ${"%.1f".format(bestCp.accuracyPct)}% Acc)",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Model Checkpoints & Reversion History Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkpoints_manager_card"),
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
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Checkpoints & Rollback",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${savedCheckpoints.size} saved states in local SQLite",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                manualSnapshotName = "Manual Snapshot ${savedCheckpoints.size + 1}"
                                showManualSnapshotDialog = true
                            },
                            modifier = Modifier.testTag("add_checkpoint_header_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "New Snapshot", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Model weights are periodically saved during training. If accuracy drops or overfitting occurs, you can instantly revert your active model to any previous checkpoint.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (savedCheckpoints.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No Checkpoints Recorded Yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Checkpoints will be saved automatically every $checkpointInterval epochs during training, or tap 'Snapshot State' to create one now.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            savedCheckpoints.forEach { cp ->
                                CheckpointItemRow(
                                    checkpoint = cp,
                                    isActive = currentWeights != null && (currentWeights?.finalLoss == cp.currentLoss || currentWeights?.trainedEpochs == cp.currentEpoch),
                                    onRevert = {
                                        coroutineScope.launch {
                                            dbHelper.revertModelToCheckpoint(user.username, cp.id)
                                            refreshData()
                                            snackbarMessage = "Reverted active model to ${cp.checkpointName} (Epoch ${cp.currentEpoch}, Loss: ${"%.4f".format(cp.currentLoss)})!"
                                        }
                                    },
                                    onDelete = {
                                        coroutineScope.launch {
                                            dbHelper.deleteCheckpoint(cp.id)
                                            savedCheckpoints = dbHelper.getTrainingCheckpoints(user.username, 30)
                                            snackbarMessage = "Checkpoint deleted."
                                        }
                                    }
                                )
                            }

                            if (savedCheckpoints.size > 2) {
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            dbHelper.clearTrainingCheckpoints(user.username)
                                            savedCheckpoints = emptyList()
                                            snackbarMessage = "All saved checkpoints cleared."
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("clear_all_checkpoints_button")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Clear Checkpoint History")
                                }
                            }
                        }
                    }
                }
            }

            // Scrollable Training Log Component (Checkpoints, Alerts & Hyperparameters)
            TrainingLogCard(
                logs = trainingLogs,
                onClearLogs = {
                    coroutineScope.launch {
                        dbHelper.clearTrainingLogs(user.username)
                        trainingLogs = emptyList()
                        snackbarMessage = "Training logs cleared."
                    }
                },
                onRefreshLogs = {
                    coroutineScope.launch {
                        trainingLogs = dbHelper.getTrainingLogs(user.username, 100)
                    }
                }
            )

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

    if (showManualSnapshotDialog) {
        AlertDialog(
            onDismissRequest = { showManualSnapshotDialog = false },
            title = { Text("Snapshot Model Checkpoint") },
            text = {
                Column {
                    Text(
                        text = "Snapshot the current on-device model weights to SQLite so you can revert to this exact point anytime:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = manualSnapshotName,
                        onValueChange = { manualSnapshotName = it },
                        label = { Text("Checkpoint Tag / Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_checkpoint_name_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentW = currentWeights ?: ModelWeights.defaultInit(5, 8, 4)
                        val name = manualSnapshotName.ifEmpty { "Manual Snapshot" }
                        val cp = TrainingCheckpoint(
                            username = user.username,
                            sessionId = UUID.randomUUID().toString(),
                            checkpointName = name,
                            currentEpoch = currentW.trainedEpochs,
                            targetEpochs = maxOf(epochs, currentW.trainedEpochs),
                            currentLoss = currentW.finalLoss,
                            accuracyPct = currentEvaluatedAccuracy,
                            lossHistory = emptyList(),
                            weights = currentW,
                            triggerType = "MANUAL"
                        )
                        coroutineScope.launch {
                            dbHelper.saveTrainingCheckpoint(cp)
                            savedCheckpoints = dbHelper.getTrainingCheckpoints(user.username, 30)
                            showManualSnapshotDialog = false
                            snackbarMessage = "Snapshot '$name' created!"
                        }
                    },
                    modifier = Modifier.testTag("confirm_manual_checkpoint_button")
                ) {
                    Text("Save Snapshot")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualSnapshotDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CheckpointItemRow(
    checkpoint: TrainingCheckpoint,
    isActive: Boolean,
    onRevert: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(checkpoint.savedAt) { dateFormat.format(Date(checkpoint.savedAt)) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("checkpoint_item_${checkpoint.id}"),
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                 else if (checkpoint.isBest) androidx.compose.foundation.BorderStroke(1.dp, AmberAccent)
                 else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = checkpoint.checkpointName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (checkpoint.isBest) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AmberAccent.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⭐ Best",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberAccent
                            )
                        }
                    }
                    if (checkpoint.triggerType == "ACCURACY_DROP") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AmberAccent.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚠️ Drop Alert",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AmberAccent
                            )
                        }
                    }
                    if (isActive) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldSuccess.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_checkpoint_${checkpoint.id}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Checkpoint",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Loss: ${"%.4f".format(checkpoint.currentLoss)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (checkpoint.accuracyPct > 0f) {
                    Text(
                        text = "Acc: ${"%.1f".format(checkpoint.accuracyPct)}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldSuccess
                    )
                }
                Text(
                    text = "Epoch ${checkpoint.currentEpoch}/${checkpoint.targetEpochs}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onRevert,
                enabled = !isActive,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("revert_checkpoint_button_${checkpoint.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (checkpoint.isBest) AmberAccent else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (checkpoint.isBest) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isActive) "Current Active Model" else "Revert to this State",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
