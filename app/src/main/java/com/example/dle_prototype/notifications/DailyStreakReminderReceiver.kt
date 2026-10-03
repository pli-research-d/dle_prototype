package com.example.dle_prototype.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.dle_prototype.data.DatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class DailyStreakReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Restore scheduled alarm after device reboot
            if (DailyStreakReminderManager.isReminderEnabled(context)) {
                val (hour, minute) = DailyStreakReminderManager.getReminderTime(context)
                DailyStreakReminderManager.scheduleDailyReminder(context, hour, minute)
            }
            return
        }

        // Check if reminder is active
        if (!DailyStreakReminderManager.isReminderEnabled(context)) {
            return
        }

        // Check in background if user has completed a quiz today
        val dbHelper = DatabaseHelper.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get all users in the local database
                val users = dbHelper.getAllUsers()
                val primaryUser = users.firstOrNull()?.username ?: "Learner"

                val streak = dbHelper.calculateDailyStreak(primaryUser)
                val recentAttempts = dbHelper.getRecentQuizAttempts(primaryUser, 1)

                val alreadyCompletedToday = if (recentAttempts.isNotEmpty()) {
                    val lastQuizCal = Calendar.getInstance().apply {
                        timeInMillis = recentAttempts.first().timestamp
                    }
                    val todayCal = Calendar.getInstance()
                    lastQuizCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                            lastQuizCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
                } else {
                    false
                }

                // If user hasn't completed quiz today, send reminder!
                if (!alreadyCompletedToday) {
                    DailyStreakReminderManager.sendStreakReminderNotification(
                        context = context,
                        username = primaryUser,
                        currentStreak = streak
                    )
                }

                // Reschedule for next day to ensure continual cadence
                val (hour, minute) = DailyStreakReminderManager.getReminderTime(context)
                DailyStreakReminderManager.scheduleDailyReminder(context, hour, minute)
            } catch (_: Exception) {
                // Fail-safe trigger if database check encounters transient state
                DailyStreakReminderManager.sendStreakReminderNotification(
                    context = context,
                    username = "Learner",
                    currentStreak = 1
                )
            }
        }
    }
}
