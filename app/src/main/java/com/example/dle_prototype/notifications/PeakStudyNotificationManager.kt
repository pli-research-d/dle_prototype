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
import com.example.dle_prototype.data.ml.PeakLearningHoursAnalysis
import com.example.dle_prototype.data.ml.PeakLearningHoursAnalyzer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object PeakStudyNotificationManager {

    const val CHANNEL_ID = "peak_study_suggestions"
    const val CHANNEL_NAME = "Peak Study Suggestions"
    const val NOTIFICATION_ID = 2002

    private const val PREFS_NAME = "peak_study_notification_prefs"
    private const val KEY_ENABLED = "peak_study_enabled"
    private const val KEY_AUTO_DETECT = "peak_study_auto_detect"
    private const val KEY_SCHEDULED_HOUR = "peak_study_hour"
    private const val KEY_SCHEDULED_MINUTE = "peak_study_minute"

    const val ACTION_TRIGGER_PEAK_STUDY = "com.example.dle_prototype.ACTION_TRIGGER_PEAK_STUDY"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isPeakStudyEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ENABLED, true)
    }

    fun setPeakStudyEnabled(context: Context, enabled: Boolean, hour: Int? = null, minute: Int = 0) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) {
            val targetHour = hour ?: getScheduledHour(context)
            schedulePeakStudyAlarm(context, targetHour, minute)
        } else {
            cancelPeakStudyAlarm(context)
        }
    }

    fun isAutoDetectEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_DETECT, true)
    }

    fun setAutoDetectEnabled(context: Context, autoDetect: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_DETECT, autoDetect).apply()
    }

    fun getScheduledHour(context: Context): Int {
        return getPrefs(context).getInt(KEY_SCHEDULED_HOUR, PeakLearningHoursAnalyzer.DEFAULT_PEAK_HOUR)
    }

    fun setScheduledHour(context: Context, hour: Int, minute: Int = 0) {
        getPrefs(context).edit()
            .putInt(KEY_SCHEDULED_HOUR, hour.coerceIn(0, 23))
            .putInt(KEY_SCHEDULED_MINUTE, minute.coerceIn(0, 59))
            .apply()
        if (isPeakStudyEnabled(context)) {
            schedulePeakStudyAlarm(context, hour, minute)
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Intelligent study session suggestions scheduled at your historical peak learning hours."
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun schedulePeakStudyAlarm(context: Context, hour: Int, minute: Int = 0) {
        createNotificationChannel(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PeakStudySuggestionReceiver::class.java).apply {
            action = ACTION_TRIGGER_PEAK_STUDY
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

    fun cancelPeakStudyAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PeakStudySuggestionReceiver::class.java).apply {
            action = ACTION_TRIGGER_PEAK_STUDY
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Reads progress data from the database, computes peak learning hours,
     * updates scheduled time if auto-detect is enabled, and schedules the alarm.
     */
    fun syncAndSchedulePeakStudyAlert(
        context: Context,
        dbHelper: DatabaseHelper,
        username: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = username ?: dbHelper.getAllUsers().firstOrNull()?.username ?: return@launch
                val analysis = dbHelper.getPeakLearningHoursAnalysis(user)

                if (isAutoDetectEnabled(context)) {
                    getPrefs(context).edit().putInt(KEY_SCHEDULED_HOUR, analysis.peakHour).apply()
                }

                if (isPeakStudyEnabled(context)) {
                    val hour = if (isAutoDetectEnabled(context)) analysis.peakHour else getScheduledHour(context)
                    schedulePeakStudyAlarm(context, hour)
                }
            } catch (_: Exception) {
                // Ignore background sync errors
            }
        }
    }

    /**
     * Dispatches a local notification suggesting a study session based on peak learning hours data.
     */
    fun sendPeakStudyNotification(
        context: Context,
        username: String,
        analysis: PeakLearningHoursAnalysis
    ) {
        createNotificationChannel(context)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_START_QUIZ", true)
            putExtra("EXTRA_FROM_PEAK_NOTIFICATION", true)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (analysis.isCalibrated) {
            "⚡ Peak Focus Hour (${analysis.peakHourFormatted})"
        } else {
            "🧠 Time for Your Study Session (${analysis.peakHourFormatted})"
        }

        val accuracyText = if (analysis.accuracyAtPeakPercent > 0f) {
            "Your historical accuracy peaks at ${analysis.accuracyAtPeakPercent.toInt()}% around this time. "
        } else ""

        val body = "${accuracyText}Capitalize on your prime cognitive retention window with a quick 10-min adaptive session."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$body\n\n🎯 Optimal Window: ${analysis.peakWindowFormatted}\n🔥 Best Time: ${analysis.timeOfDayLabel}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Start Practice Now",
                contentPendingIntent
            )

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
            // Graceful handling for permission absence
        }
    }

    /**
     * Sends an immediate test notification for user testing and preview.
     */
    fun sendTestNotification(
        context: Context,
        username: String,
        analysis: PeakLearningHoursAnalysis? = null
    ) {
        val resolvedAnalysis = analysis ?: PeakLearningHoursAnalysis(
            peakHour = getScheduledHour(context),
            peakHourFormatted = PeakLearningHoursAnalyzer.formatHour(getScheduledHour(context)),
            peakWindowFormatted = PeakLearningHoursAnalyzer.formatWindow(getScheduledHour(context)),
            accuracyAtPeakPercent = 88f,
            overallAccuracyPercent = 78f,
            totalSessionsAnalyzed = 12,
            confidenceScore = 0.85f,
            timeOfDayLabel = PeakLearningHoursAnalyzer.getTimeOfDayLabel(getScheduledHour(context)),
            isCalibrated = true
        )
        sendPeakStudyNotification(context, username, resolvedAnalysis)
    }
}
