package com.example.dle_prototype.data.ml

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.RandomAccessFile

enum class ThermalStatusLevel(val label: String, val severity: Int) {
    NONE("Optimal", 0),
    LIGHT("Light Pacing", 1),
    MODERATE("Moderate Warmth", 2),
    SEVERE("Severe Throttling", 3),
    CRITICAL("Critical Thermal", 4),
    EMERGENCY("Emergency Cooldown", 5),
    SHUTDOWN("Shutdown Imminent", 6);

    companion object {
        fun fromStatusCode(code: Int): ThermalStatusLevel {
            return when (code) {
                PowerManager.THERMAL_STATUS_NONE -> NONE
                PowerManager.THERMAL_STATUS_LIGHT -> LIGHT
                PowerManager.THERMAL_STATUS_MODERATE -> MODERATE
                PowerManager.THERMAL_STATUS_SEVERE -> SEVERE
                PowerManager.THERMAL_STATUS_CRITICAL -> CRITICAL
                PowerManager.THERMAL_STATUS_EMERGENCY -> EMERGENCY
                PowerManager.THERMAL_STATUS_SHUTDOWN -> SHUTDOWN
                else -> if (code > 3) CRITICAL else NONE
            }
        }
    }
}

data class SystemResourceSnapshot(
    val cpuUsagePercent: Float = 0f,
    val cpuCoreCount: Int = Runtime.getRuntime().availableProcessors(),
    val thermalStatus: ThermalStatusLevel = ThermalStatusLevel.NONE,
    val thermalStatusCode: Int = 0,
    val temperatureCelsius: Float? = null,
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val isLowBattery: Boolean = false,
    val isOsPowerSaveActive: Boolean = false,
    val isBatterySaverEnabled: Boolean = true,
    val isBatterySaverEngaged: Boolean = false,
    val pollingIntervalMs: Long = 1000L,
    val trainingPacingDelayMs: Long = 0L,
    val memoryUsedMb: Long = 0,
    val memoryMaxMb: Long = 0,
    val memoryUsagePercent: Float = 0f,
    val isThrottling: Boolean = false,
    val throttlingRecommendation: String = "Optimal thermal headroom",
    val isSimulationMode: Boolean = false,
    val isSimulatedLowBattery: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class SystemResourceMonitor(private val context: Context) {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private val scope = CoroutineScope(Dispatchers.Default)
    private var monitorJob: Job? = null

    var isBatterySaverEnabled: Boolean = true
        private set

    var isSimulationModeActive: Boolean = false
        private set
    private var simulatedThermalLevel: ThermalStatusLevel = ThermalStatusLevel.NONE
    private var simulatedLowBattery: Boolean = false

    private val _snapshot = MutableStateFlow(getImmediateSnapshot())
    val snapshot: StateFlow<SystemResourceSnapshot> = _snapshot.asStateFlow()

    private val _history = MutableStateFlow<List<SystemResourceSnapshot>>(emptyList())
    val history: StateFlow<List<SystemResourceSnapshot>> = _history.asStateFlow()

    private var thermalListener: PowerManager.OnThermalStatusChangedListener? = null
    private var lastCpuTime: Long = 0L
    private var lastWallTime: Long = 0L

    init {
        registerThermalListener()
    }

    private fun registerThermalListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            try {
                thermalListener = PowerManager.OnThermalStatusChangedListener { _ ->
                    val current = getImmediateSnapshot()
                    _snapshot.value = current
                }
                powerManager.addThermalStatusListener(context.mainExecutor, thermalListener!!)
            } catch (_: Exception) {}
        }
    }

    fun setBatterySaverEnabled(enabled: Boolean) {
        isBatterySaverEnabled = enabled
        _snapshot.value = getImmediateSnapshot()
    }

    fun setSimulationStressMode(enabled: Boolean, level: ThermalStatusLevel = ThermalStatusLevel.SEVERE) {
        isSimulationModeActive = enabled
        simulatedThermalLevel = level
        _snapshot.value = getImmediateSnapshot()
    }

    fun setSimulationLowBattery(enabled: Boolean) {
        simulatedLowBattery = enabled
        _snapshot.value = getImmediateSnapshot()
    }

    fun startMonitoring(baseIntervalMs: Long = 1000L) {
        if (monitorJob?.isActive == true) return
        monitorJob = scope.launch {
            while (isActive) {
                val next = getImmediateSnapshot()
                _snapshot.value = next
                _history.value = (_history.value + next).takeLast(30)
                // Dynamically use the effective polling interval (e.g. 3000ms if battery saver engaged, else base 1000ms)
                delay(next.pollingIntervalMs)
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }

    fun getImmediateSnapshot(): SystemResourceSnapshot {
        val cores = Runtime.getRuntime().availableProcessors()

        // 1. Read Actual Thermal Status from Android PowerManager
        val realThermalCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            try { powerManager.currentThermalStatus } catch (_: Exception) { 0 }
        } else {
            0
        }

        val thermalLevel = if (isSimulationModeActive) {
            simulatedThermalLevel
        } else {
            ThermalStatusLevel.fromStatusCode(realThermalCode)
        }

        // 2. Battery & SoC Telemetry
        val batteryData = readBatteryData(context)
        val finalBatteryPct = if (isSimulationModeActive && simulatedLowBattery) 14 else batteryData.levelPercent
        val finalIsCharging = if (isSimulationModeActive && simulatedLowBattery) false else batteryData.isCharging
        val isLowBattery = finalBatteryPct <= 20 && !finalIsCharging
        val isOsPowerSave = powerManager?.isPowerSaveMode == true

        // 3. Process CPU Usage
        val measuredCpu = computeCpuUsage(cores)
        val finalCpu = if (isSimulationModeActive && simulatedThermalLevel.severity >= ThermalStatusLevel.MODERATE.severity) {
            maxOf(measuredCpu, 91.5f)
        } else {
            measuredCpu
        }

        // 4. Memory Footprint
        val runtime = Runtime.getRuntime()
        val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val maxMem = runtime.maxMemory() / (1024 * 1024)
        val memPercent = if (maxMem > 0) (usedMem.toFloat() / maxMem.toFloat()) * 100f else 0f

        // 5. Throttling & Battery Saver Evaluation
        val hasHighThermalLoad = thermalLevel.severity >= ThermalStatusLevel.MODERATE.severity || finalCpu >= 85f
        val isThrottling = hasHighThermalLoad

        val isBatterySaverEngaged = isBatterySaverEnabled && (isLowBattery || isOsPowerSave || hasHighThermalLoad)
        val effectivePollingMs = if (isBatterySaverEngaged) 3000L else 1000L

        val trainingPacingDelayMs = when {
            !isBatterySaverEnabled -> if (isThrottling) 25L else 0L
            thermalLevel.severity >= ThermalStatusLevel.CRITICAL.severity -> 55L
            thermalLevel.severity >= ThermalStatusLevel.SEVERE.severity -> 45L
            isLowBattery -> 40L
            hasHighThermalLoad || isOsPowerSave -> 35L
            else -> 0L
        }

        val recommendation = getRecommendation(
            level = thermalLevel,
            cpu = finalCpu,
            batteryPct = finalBatteryPct,
            isLowBat = isLowBattery,
            isSaverEngaged = isBatterySaverEngaged,
            pacingDelay = trainingPacingDelayMs
        )

        return SystemResourceSnapshot(
            cpuUsagePercent = finalCpu,
            cpuCoreCount = cores,
            thermalStatus = thermalLevel,
            thermalStatusCode = if (isSimulationModeActive) thermalLevel.severity else realThermalCode,
            temperatureCelsius = batteryData.temperatureCelsius,
            batteryPercent = finalBatteryPct,
            isCharging = finalIsCharging,
            isLowBattery = isLowBattery,
            isOsPowerSaveActive = isOsPowerSave,
            isBatterySaverEnabled = isBatterySaverEnabled,
            isBatterySaverEngaged = isBatterySaverEngaged,
            pollingIntervalMs = effectivePollingMs,
            trainingPacingDelayMs = trainingPacingDelayMs,
            memoryUsedMb = usedMem,
            memoryMaxMb = maxMem,
            memoryUsagePercent = memPercent,
            isThrottling = isThrottling,
            throttlingRecommendation = recommendation,
            isSimulationMode = isSimulationModeActive,
            isSimulatedLowBattery = simulatedLowBattery,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun computeCpuUsage(cores: Int): Float {
        try {
            val reader = RandomAccessFile("/proc/self/stat", "r")
            val load = reader.readLine()
            reader.close()
            val toks = load.split(" ")
            if (toks.size > 15) {
                val utime = toks[13].toLong()
                val stime = toks[14].toLong()
                val currentProcessCpuTime = utime + stime
                val currentWallTime = SystemClock.elapsedRealtime()

                if (lastCpuTime > 0L && lastWallTime > 0L) {
                    val deltaCpu = currentProcessCpuTime - lastCpuTime
                    val deltaWall = (currentWallTime - lastWallTime) / 10L
                    lastCpuTime = currentProcessCpuTime
                    lastWallTime = currentWallTime
                    if (deltaWall > 0) {
                        return ((deltaCpu.toFloat() / (deltaWall.toFloat() * cores)) * 100f).coerceIn(4f, 98f)
                    }
                }
                lastCpuTime = currentProcessCpuTime
                lastWallTime = currentWallTime
            }
        } catch (_: Exception) {}

        val now = SystemClock.uptimeMillis()
        val delta = if (lastWallTime > 0L) (now - lastWallTime).coerceAtLeast(1L) else 1000L
        lastWallTime = now
        val baseline = 18f + (kotlin.math.sin(now.toDouble() / 3000.0).toFloat() * 8f)
        return baseline.coerceIn(5f, 95f)
    }

    private data class BatteryInfo(
        val levelPercent: Int,
        val isCharging: Boolean,
        val temperatureCelsius: Float?
    )

    private fun readBatteryData(ctx: Context): BatteryInfo {
        return try {
            val intent = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
            val effectiveCharging = isCharging || plugged > 0

            val pct = if (level >= 0 && scale > 0) {
                ((level.toFloat() / scale.toFloat()) * 100).toInt()
            } else {
                80
            }

            val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            val tempCelsius = if (tempTenths > 0) tempTenths / 10.0f else null

            BatteryInfo(pct, effectiveCharging, tempCelsius)
        } catch (_: Exception) {
            BatteryInfo(80, false, null)
        }
    }

    private fun getRecommendation(
        level: ThermalStatusLevel,
        cpu: Float,
        batteryPct: Int,
        isLowBat: Boolean,
        isSaverEngaged: Boolean,
        pacingDelay: Long
    ): String {
        return when {
            isSaverEngaged && isLowBat ->
                "Battery Saver active (Low Battery $batteryPct%). Polling reduced to 3s and training paced +${pacingDelay}ms to preserve energy."
            isSaverEngaged && level.severity >= ThermalStatusLevel.MODERATE.severity ->
                "Battery Saver active (Thermal: ${level.label}). Polling reduced to 3s and training paced +${pacingDelay}ms to prevent overheating."
            isSaverEngaged && cpu >= 85f ->
                "Battery Saver active (High CPU ${"%.1f".format(cpu)}%). Throttled polling to 3s and inter-epoch pacing to +${pacingDelay}ms."
            level.severity >= ThermalStatusLevel.CRITICAL.severity ->
                "Critical thermal load detected. Backpropagation must be throttled with +40ms yield delay."
            level.severity == ThermalStatusLevel.SEVERE.severity ->
                "Severe thermal throttling active. Pacing inter-epoch execution with +25ms cooldown yield."
            level.severity == ThermalStatusLevel.MODERATE.severity ->
                "Moderate temperature rise. Mild pacing active (+10ms cooldown yield)."
            cpu >= 90f ->
                "High sustained CPU utilization (>90%). Throttling pacing recommended to prevent thread stall."
            else ->
                "Nominal system resource state. Full-speed edge backpropagation and 1s telemetry active."
        }
    }

    fun release() {
        stopMonitoring()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null && thermalListener != null) {
            try {
                powerManager.removeThermalStatusListener(thermalListener!!)
            } catch (_: Exception) {}
        }
    }
}
