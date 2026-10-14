package com.example.dle_prototype.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dle_prototype.MainActivity
import com.example.dle_prototype.R
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.graph.KnowledgeMasteryLevel
import com.example.dle_prototype.data.graph.KnowledgeReinforcementSchedule
import com.example.dle_prototype.data.graph.TopicMasteryStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Manages Spaced Repetition Notification Reminders for the Knowledge Graph.
 * Monitors memory decay, prerequisite node dependencies, and mastery levels
 * (Beginner, Intermediate, Expert) to dispatch timely alerts when topics
 * are due for synaptic reinforcement.
 */
object KnowledgeSpacedRepetitionNotificationManager {

    const val CHANNEL_ID = "knowledge_spaced_repetition_alerts"
    const val CHANNEL_NAME = "Spaced Repetition & Knowledge Reinforcement"
    const val NOTIFICATION_ID = 2003

    private const val PREFS_NAME = "knowledge_spaced_repetition_prefs"
    private const val KEY_ENABLED = "reinforcement_alerts_enabled"
    private const val KEY_SCHEDULED_HOUR = "reinforcement_scheduled_hour"
    private const val KEY_SCHEDULED_MINUTE = "reinforcement_scheduled_minute"
    private const val KEY_RETENTION_THRESHOLD = "reinforcement_min_retention_threshold"

    const val ACTION_TRIGGER_KNOWLEDGE_REINFORCEMENT =
        "com.example.dle_prototype.ACTION_TRIGGER_KNOWLEDGE_REINFORCEMENT"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isReminderEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLED, true)
    }

    fun setReminderEnabled(context: Context, enabled: Boolean, hour: Int? = null, minute: Int? = null) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) {
            val targetHour = hour ?: getScheduledHour(context)
            val targetMinute = minute ?: getScheduledMinute(context)
            scheduleReinforcementAlarm(context, targetHour, targetMinute)
        } else {
            cancelReinforcementAlarm(context)
        }
    }

    fun getScheduledHour(context: Context): Int {
        return getPrefs(context).getInt(KEY_SCHEDULED_HOUR, 18) // Default 6:00 PM
    }

    fun getScheduledMinute(context: Context): Int {
        return getPrefs(context).getInt(KEY_SCHEDULED_MINUTE, 30) // Default :30
    }

    fun setScheduledTime(context: Context, hour: Int, minute: Int = 0) {
        getPrefs(context).edit()
            .putInt(KEY_SCHEDULED_HOUR, hour.coerceIn(0, 23))
            .putInt(KEY_SCHEDULED_MINUTE, minute.coerceIn(0, 59))
            .apply()
        if (isReminderEnabled(context)) {
            scheduleReinforcementAlarm(context, hour, minute)
        }
    }

    fun getRetentionThreshold(context: Context): Int {
        return getPrefs(context).getInt(KEY_RETENTION_THRESHOLD, 75) // Alert when retention < 75%
    }

    fun setRetentionThreshold(context: Context, thresholdPercent: Int) {
        getPrefs(context).edit()
            .putInt(KEY_RETENTION_THRESHOLD, thresholdPercent.coerceIn(40, 90))
            .apply()
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Intelligent spaced repetition reminders alerting you to reinforce decaying knowledge graph nodes and prerequisites."
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun scheduleReinforcementAlarm(context: Context, hour: Int, minute: Int = 0) {
        createNotificationChannel(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, KnowledgeSpacedRepetitionReceiver::class.java).apply {
            action = ACTION_TRIGGER_KNOWLEDGE_REINFORCEMENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (_: SecurityException) {
            // Graceful fallback for restricted background alarm policies
        }
    }

    fun cancelReinforcementAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, KnowledgeSpacedRepetitionReceiver::class.java).apply {
            action = ACTION_TRIGGER_KNOWLEDGE_REINFORCEMENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun syncAndScheduleKnowledgeReinforcementAlert(
        context: Context,
        dbHelper: DatabaseHelper,
        username: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isReminderEnabled(context)) {
                    val hour = getScheduledHour(context)
                    val minute = getScheduledMinute(context)
                    scheduleReinforcementAlarm(context, hour, minute)
                }
            } catch (_: Exception) {
                // Background sync safety
            }
        }
    }

    /**
     * Dispatches a rich local push notification alerting the user to reinforce
     * a specific topic node in their knowledge graph.
     */
    fun sendReinforcementNotification(
        context: Context,
        schedule: KnowledgeReinforcementSchedule,
        username: String
    ) {
        createNotificationChannel(context)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_START_QUIZ", true)
            putExtra("EXTRA_CATEGORY_NAME", schedule.category)
            putExtra("EXTRA_CATEGORY_NUMBER", schedule.categoryNumber)
            putExtra("EXTRA_FROM_REINFORCEMENT_NOTIFICATION", true)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID + schedule.nodeId.hashCode() % 1000,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val retentionInt = (schedule.retentionProbability * 100).toInt()
        val title = "${schedule.masteryLevel.iconEmoji} Reinforce ${schedule.title} (${schedule.masteryLevel.badgeText})"

        val prereqNote = if (schedule.prerequisiteImpactCount > 0) {
            "⚓ Prerequisite Anchor: ${schedule.prerequisiteImpactCount} downstream knowledge paths depend on this topic!\n\n"
        } else ""

        val bigText = "🧠 Memory Retention: $retentionInt% (${schedule.dueStatusText})\n" +
                "📊 Mastery Tier: ${schedule.masteryLevel.badgeText} (${schedule.accuracyPercent.toInt()}% accuracy)\n\n" +
                "$prereqNote" +
                "💡 ${schedule.reinforcementReason}\n\n" +
                "Recommended: ${schedule.recommendedAction} (3-min practice quiz)."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("Retention at $retentionInt% · ${schedule.dueStatusText}. Tap to review.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Reinforce Now",
                contentPendingIntent
            )

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID + (schedule.nodeId.hashCode() % 100),
                builder.build()
            )
        } catch (_: SecurityException) {
            // Permission handling
        }
    }

    /**
     * Sends an immediate test spaced repetition notification for user testing and verification.
     */
    fun sendTestNotification(
        context: Context,
        username: String,
        dbHelper: DatabaseHelper
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val summary = dbHelper.getKnowledgeSpacedRepetitionSummary(username)
                val targetSchedule = summary.mostUrgentSchedule ?: KnowledgeReinforcementSchedule(
                    nodeId = "coroutine",
                    title = "Coroutines & Concurrency",
                    category = "Python",
                    categoryNumber = 6f,
                    masteryLevel = KnowledgeMasteryLevel.BEGINNER,
                    status = TopicMasteryStatus.NEEDS_REVIEW,
                    accuracyPercent = 52f,
                    attemptsCount = 2,
                    retentionProbability = 0.58f,
                    scheduledIntervalDays = 1.2f,
                    lastReviewedTimestamp = System.currentTimeMillis() - (86400000L * 3),
                    nextReviewTimestamp = System.currentTimeMillis() - (86400000L * 1),
                    isOverdue = true,
                    dueStatusText = "Overdue by 1.1d",
                    urgencyPriorityScore = 84.5f,
                    prerequisiteImpactCount = 2,
                    reinforcementReason = "Beginner Tier (52% accuracy). Estimated retention decayed to 58%. ⚓ Essential prerequisite anchor for 2 downstream knowledge nodes.",
                    recommendedAction = "Reinforce Foundations"
                )

                sendReinforcementNotification(context, targetSchedule, username)
            } catch (_: Exception) {
                // Fallback direct notification
                val fallback = KnowledgeReinforcementSchedule(
                    nodeId = "test_reinforcement",
                    title = "Knowledge Graph Reinforcement",
                    category = "JavaScript",
                    categoryNumber = 3f,
                    masteryLevel = KnowledgeMasteryLevel.INTERMEDIATE,
                    status = TopicMasteryStatus.SOLIDIFYING,
                    accuracyPercent = 68f,
                    attemptsCount = 4,
                    retentionProbability = 0.64f,
                    scheduledIntervalDays = 2.0f,
                    lastReviewedTimestamp = System.currentTimeMillis() - 86400000L * 2,
                    nextReviewTimestamp = System.currentTimeMillis(),
                    isOverdue = true,
                    dueStatusText = "Review Due Today",
                    urgencyPriorityScore = 78f,
                    prerequisiteImpactCount = 1,
                    reinforcementReason = "Intermediate Tier (68% accuracy). Retention decayed to 64%. Reinforce to consolidate synaptic pathways.",
                    recommendedAction = "Solidify Concepts"
                )
                sendReinforcementNotification(context, fallback, username)
            }
        }
    }
}
