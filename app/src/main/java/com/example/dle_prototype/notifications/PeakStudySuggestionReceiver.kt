package com.example.dle_prototype.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.dle_prototype.data.DatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver responsible for delivering local study session suggestions
 * scheduled at the user's historical peak learning hours, and restoring schedules upon device boot.
 */
class PeakStudySuggestionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        val dbHelper = DatabaseHelper.getInstance(context)

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            if (PeakStudyNotificationManager.isPeakStudyEnabled(context)) {
                PeakStudyNotificationManager.syncAndSchedulePeakStudyAlert(context, dbHelper)
            }
            return
        }

        // Triggered by scheduled Alarm
        if (!PeakStudyNotificationManager.isPeakStudyEnabled(context)) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val users = dbHelper.getAllUsers()
                val primaryUser = users.firstOrNull()?.username ?: "Learner"

                // Analyze historical progress data to fetch up-to-date peak hours
                val analysis = dbHelper.getPeakLearningHoursAnalysis(primaryUser)

                // Dispatch the suggestion notification
                PeakStudyNotificationManager.sendPeakStudyNotification(
                    context = context,
                    username = primaryUser,
                    analysis = analysis
                )

                // Reschedule for next day's peak window
                val nextHour = if (PeakStudyNotificationManager.isAutoDetectEnabled(context)) {
                    analysis.peakHour
                } else {
                    PeakStudyNotificationManager.getScheduledHour(context)
                }
                PeakStudyNotificationManager.schedulePeakStudyAlarm(context, nextHour)
            } catch (_: Exception) {
                // Background exception handling
            }
        }
    }
}
