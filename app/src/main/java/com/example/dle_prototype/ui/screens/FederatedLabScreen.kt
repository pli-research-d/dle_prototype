package com.example.dle_prototype.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.User
import com.example.dle_prototype.data.ml.ModelWeights
import com.example.dle_prototype.data.ml.OnDeviceTrainableModel
import com.example.dle_prototype.data.ml.TrainingSample
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent
import kotlinx.coroutines.launch
import kotlin.math.sqrt

data class SimulatedPeer(
    val id: String,
    val name: String,
    val archetype: String,
    val avatarEmoji: String,
    val sampleCount: Int,
    val weights: ModelWeights,
    val divergenceFromLocal: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FederatedLabScreen(
    user: User,
    dbHelper: DatabaseHelper,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var localWeights by remember { mutableStateOf<ModelWeights?>(null) }
    var peers by remember { mutableStateOf<List<SimulatedPeer>>(emptyList()) }
    var isAggregating by remember { mutableStateOf(false) }
    var aggregationResultMsg by remember { mutableStateOf<String?>(null) }
    var showInspector by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    fun computeDivergence(wA: ModelWeights, wB: ModelWeights): Float {
        var sumSquares = 0.0
        var count = 0
        for (i in wA.w1.indices) {
            for (j in wA.w1[i].indices) {
                val diff = (wA.w1[i][j] - wB.w1[i][j]).toDouble()
                sumSquares += diff * diff
                count++
            }
        }
        for (i in wA.w2.indices) {
            for (j in wA.w2[i].indices) {
                val diff = (wA.w2[i][j] - wB.w2[i][j]).toDouble()
                sumSquares += diff * diff
                count++
            }
        }
        return if (count > 0) sqrt(sumSquares / count).toFloat() else 0f
    }

    suspend fun generatePeers(baseWeights: ModelWeights) {
        val trainerAlpha = OnDeviceTrainableModel(5, 8, 4, initialWeights = baseWeights)
        val trainerBeta = OnDeviceTrainableModel(5, 8, 4, initialWeights = baseWeights)
        val trainerGamma = OnDeviceTrainableModel(5, 8, 4, initialWeights = baseWeights)

        // Train peer Alpha (Speed Learner dataset)
        val datasetAlpha = listOf(
            TrainingSample(OnDeviceTrainableModel.normalize(floatArrayOf(10f, 15f, 85f, 2f, 3f)), floatArrayOf(0.7f, 0.9f, 0.8f, 0.95f)),
            TrainingSample(OnDeviceTrainableModel.normalize(floatArrayOf(12f, 20f, 90f, 3f, 3f)), floatArrayOf(0.8f, 0.95f, 0.85f, 0.95f))
        )
        val resA = trainerAlpha.train(datasetAlpha, epochs = 25, learningRate = 0.08f)

        // Train peer Beta (Deep Conceptualist dataset)
        val datasetBeta = listOf(
            TrainingSample(OnDeviceTrainableModel.normalize(floatArrayOf(4f, 60f, 95f, 3f, 6f)), floatArrayOf(0.95f, 0.85f, 0.98f, 0.80f)),
            TrainingSample(OnDeviceTrainableModel.normalize(floatArrayOf(5f, 75f, 92f, 3f, 6f)), floatArrayOf(0.96f, 0.88f, 0.95f, 0.85f))
        )
        val resB = trainerBeta.train(datasetBeta, epochs = 25, learningRate = 0.08f)

        // Train peer Gamma (Foundational Striver dataset)
        val datasetGamma = listOf(
            TrainingSample(OnDeviceTrainableModel.normalize(floatArrayOf(2f, 10f, 50f, 1f, 1f)), floatArrayOf(0.4f, 0.5f, 0.45f, 0.4f)),
            TrainingSample(OnDeviceTrainableModel.normalize(floatArrayOf(3f, 18f, 65f, 2f, 2f)), floatArrayOf(0.55f, 0.65f, 0.60f, 0.55f))
        )
        val resC = trainerGamma.train(datasetGamma, epochs = 25, learningRate = 0.08f)

        peers = listOf(
            SimulatedPeer(
                id = "peer_alpha",
                name = "Client Alpha",
                archetype = "High-Velocity Learner",
                avatarEmoji = "⚡",
                sampleCount = 38,
                weights = resA.weights,
                divergenceFromLocal = computeDivergence(baseWeights, resA.weights)
            ),
            SimulatedPeer(
                id = "peer_beta",
                name = "Client Beta",
                archetype = "Deep Conceptualist",
                avatarEmoji = "🧠",
                sampleCount = 44,
                weights = resB.weights,
                divergenceFromLocal = computeDivergence(baseWeights, resB.weights)
            ),
            SimulatedPeer(
                id = "peer_gamma",
                name = "Client Gamma",
                archetype = "Foundational Striver",
                avatarEmoji = "🌱",
                sampleCount = 29,
                weights = resC.weights,
                divergenceFromLocal = computeDivergence(baseWeights, resC.weights)
            )
        )
    }

    LaunchedEffect(user.username) {
        val w = dbHelper.loadModelWeights(user.username) ?: ModelWeights.defaultInit(5, 8, 4)
        localWeights = w
        generatePeers(w)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Federated Learning Lab",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Privacy-Preserving Consensus & FedAvg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("fed_back_button")
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
            // Privacy Shield Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CyanAccent.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Zero Raw Telemetry Shared",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Only mathematical weight gradients are aggregated across peer cohorts via element-wise FedAvg.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Local Model Status Card
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
                        Text(
                            text = "Local Node Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "v${localWeights?.version ?: 1} Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Trained on device for ${localWeights?.trainedEpochs ?: 0} epochs. Model parameters: 76 total floating-point weights (W1: 40, b1: 8, W2: 32, b2: 4).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showInspector = !showInspector },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showInspector) "Hide Weight Tensors" else "Inspect Weight Tensors")
                    }

                    if (showInspector) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = localWeights?.toJson() ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // Peer Simulation Cohorts
            Text(
                text = "Participating Peer Cohorts (${peers.size} Nodes)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            peers.forEach { peer ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = peer.avatarEmoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = peer.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = peer.archetype,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Divergence (L2)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Δ ${"%.4f".format(peer.divergenceFromLocal)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (peer.divergenceFromLocal > 0.15f) AmberAccent else EmeraldSuccess,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // FedAvg Trigger Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        isAggregating = true
                        val local = localWeights ?: ModelWeights.defaultInit(5, 8, 4)
                        val trainer = OnDeviceTrainableModel(5, 8, 4, initialWeights = local)
                        val peerWeightsList = peers.map { it.weights }

                        // Run real Federated Averaging algorithm
                        val globalWeights = trainer.federatedAverage(peerWeightsList)
                        dbHelper.saveModelWeights(user.username, globalWeights)
                        localWeights = globalWeights

                        // Recompute divergences against updated global weights
                        peers = peers.map {
                            it.copy(divergenceFromLocal = computeDivergence(globalWeights, it.weights))
                        }

                        isAggregating = false
                        aggregationResultMsg = "FedAvg consensus achieved! 3 peer weight vectors merged into Local Model v${globalWeights.version}."
                    }
                },
                enabled = !isAggregating && peers.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("run_fedavg_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isAggregating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Aggregating Peer Tensors...")
                } else {
                    Icon(Icons.Default.MergeType, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Execute FedAvg Consensus Update")
                }
            }

            aggregationResultMsg?.let { msg ->
                Surface(
                    color = EmeraldSuccess.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
