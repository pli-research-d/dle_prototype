package com.example.dle_prototype.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.dle_prototype.data.DatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver responsible for delivering local spaced repetition
 * reinforcement notifications when knowledge graph topics are due, and restoring
 * schedules upon device reboot.
 */
class KnowledgeSpacedRepetitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        val dbHelper = DatabaseHelper.getInstance(context)

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            if (KnowledgeSpacedRepetitionNotificationManager.isReminderEnabled(context)) {
                KnowledgeSpacedRepetitionNotificationManager.syncAndScheduleKnowledgeReinforcementAlert(context, dbHelper)
            }
            return
        }

        // Triggered by scheduled Alarm
        if (!KnowledgeSpacedRepetitionNotificationManager.isReminderEnabled(context)) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val users = dbHelper.getAllUsers()
                val primaryUser = users.firstOrNull()?.username ?: "Learner"

                // Evaluate Spaced Repetition Reinforcements based on Knowledge Graph Mastery
                val summary = dbHelper.getKnowledgeSpacedRepetitionSummary(primaryUser)
                val threshold = KnowledgeSpacedRepetitionNotificationManager.getRetentionThreshold(context) / 100f

                val dueTopics = summary.schedules.filter {
                    it.isOverdue || it.retentionProbability <= threshold
                }

                val targetTopic = dueTopics.firstOrNull() ?: summary.mostUrgentSchedule

                if (targetTopic != null) {
                    KnowledgeSpacedRepetitionNotificationManager.sendReinforcementNotification(
                        context = context,
                        schedule = targetTopic,
                        username = primaryUser
                    )
                }

                // Reschedule for next day
                val hour = KnowledgeSpacedRepetitionNotificationManager.getScheduledHour(context)
                val minute = KnowledgeSpacedRepetitionNotificationManager.getScheduledMinute(context)
                KnowledgeSpacedRepetitionNotificationManager.scheduleReinforcementAlarm(context, hour, minute)
            } catch (_: Exception) {
                // Background exception safety
            }
        }
    }
}
