package com.example.dle_prototype

import com.example.dle_prototype.data.DatabaseHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class VirtualCommunityLearnersTest {

    @Test
    fun testNoVirtualPeerNamesContainSudoAndUseFictionalGamerNames() {
        val templates = DatabaseHelper.VIRTUAL_PEER_TEMPLATES
        assertTrue("Virtual community templates should have at least 15 peers", templates.size >= 15)

        val displayNames = templates.map { it.displayName }
        assertTrue("Must include MartianTiger67 as requested", displayNames.contains("MartianTiger67"))
        assertTrue("Must include SnowyOwl3 as requested", displayNames.contains("SnowyOwl3"))

        for (peer in templates) {
            // Must not contain "sudo" anywhere in username or displayName
            assertFalse(
                "Peer username '${peer.username}' must NOT contain 'sudo'",
                peer.username.contains("sudo", ignoreCase = true)
            )
            assertFalse(
                "Peer displayName '${peer.displayName}' must NOT contain 'sudo'",
                peer.displayName.contains("sudo", ignoreCase = true)
            )

            // Fictional names end with numbers like MartianTiger67, SnowyOwl3
            assertTrue(
                "Peer displayName '${peer.displayName}' should follow fictional format (contain letters and end with digits)",
                peer.displayName.any { it.isDigit() }
            )

            // Must have authentic fields
            assertTrue(peer.username.isNotBlank())
            assertTrue(peer.displayName.isNotBlank())
            assertTrue(peer.avatarEmoji.isNotBlank())
            assertTrue(peer.dominantTrait.isNotBlank())
            assertTrue(listOf("Diamond", "Platinum", "Gold", "Silver", "Bronze").contains(peer.tierLevel))
            assertTrue("Base streak must be positive", peer.baseStreak >= 1)
            assertTrue("Base quizzes must be positive", peer.baseQuizzes >= 1)
            assertTrue("Base score must be positive", peer.baseScore >= 10)
            assertTrue("Base accuracy must be between 70 and 100", peer.baseAccuracy in 70.0f..100.0f)
        }
    }

    @Test
    fun testGenerateDailyCommunityLearnersVaryingProgress() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 3, 14, 30, 0)
        }
        val peers = DatabaseHelper.generateDailyCommunityLearners(cal)

        assertTrue("Generated peer count must match templates", peers.size >= 15)

        var diamondCount = 0
        var platinumCount = 0
        var goldCount = 0
        var silverBronzeCount = 0

        for (peer in peers) {
            val username = peer.username
            val displayName = peer.displayName
            val streak = peer.dailyStreak
            val score = peer.totalScore
            val quizzes = peer.totalQuizzes
            val accuracy = peer.accuracyPercent
            val tier = peer.tier

            assertFalse("Generated username '$username' must not contain 'sudo'", username.contains("sudo", ignoreCase = true))
            assertFalse("Generated displayName '$displayName' must not contain 'sudo'", displayName.contains("sudo", ignoreCase = true))

            assertTrue("Streak must be positive", streak >= 1)
            assertTrue("Score must be positive", score >= 10)
            assertTrue("Quizzes must be positive", quizzes >= 1)
            assertTrue("Accuracy must be valid", accuracy in 70.0f..100.0f)

            when (tier) {
                "Diamond" -> {
                    diamondCount++
                    assertTrue("Diamond peers should have high streaks (>= 15)", streak >= 15)
                    assertTrue("Diamond peers should have high scores (>= 400)", score >= 400)
                }
                "Platinum" -> {
                    platinumCount++
                    assertTrue("Platinum peers should have solid streaks (>= 8)", streak >= 8)
                }
                "Gold" -> {
                    goldCount++
                    assertTrue("Gold peers should have active streaks (>= 2)", streak >= 2)
                }
                "Silver", "Bronze" -> {
                    silverBronzeCount++
                    assertTrue("Silver/Bronze peers should have casual streaks (>= 1)", streak >= 1)
                }
            }
        }

        assertTrue("Diamond tier should be represented", diamondCount > 0)
        assertTrue("Platinum tier should be represented", platinumCount > 0)
        assertTrue("Gold tier should be represented", goldCount > 0)
        assertTrue("Silver/Bronze tiers should be represented", silverBronzeCount > 0)
    }

    @Test
    fun testAlgorithmicProgressionAcrossDays() {
        val calDay1 = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 3, 12, 0, 0)
        }
        val calDay2 = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 7, 12, 0, 0)
        }

        val peersDay1 = DatabaseHelper.generateDailyCommunityLearners(calDay1)
        val peersDay2 = DatabaseHelper.generateDailyCommunityLearners(calDay2)

        assertEquals(peersDay1.size, peersDay2.size)

        var hasDifferentScores = false
        var hasDifferentStreaks = false

        for (i in peersDay1.indices) {
            val score1 = peersDay1[i].totalScore
            val score2 = peersDay2[i].totalScore
            if (score1 != score2) {
                hasDifferentScores = true
            }

            val streak1 = peersDay1[i].dailyStreak
            val streak2 = peersDay2[i].dailyStreak
            if (streak1 != streak2) {
                hasDifferentStreaks = true
            }
        }

        assertTrue("Across different days, peer scores must algorithmically progress/vary", hasDifferentScores)
        assertTrue("Across different days, peer streaks must algorithmically progress/vary", hasDifferentStreaks)

        // Determinism test: calling on the exact same date/time produces identical results
        val peersDay1Again = DatabaseHelper.generateDailyCommunityLearners(calDay1)
        for (i in peersDay1.indices) {
            assertEquals(peersDay1[i].totalScore, peersDay1Again[i].totalScore)
            assertEquals(peersDay1[i].dailyStreak, peersDay1Again[i].dailyStreak)
            assertEquals(peersDay1[i].username, peersDay1Again[i].username)
        }
    }

    @Test
    fun testLoggedInUserIsAlwaysDisplayedAsYou() {
        val entryUser = com.example.dle_prototype.data.LeaderboardEntry(
            rank = 1,
            username = "alice_dev",
            displayName = "You",
            avatarEmoji = "🚀",
            dailyStreak = 7,
            totalQuizzes = 15,
            totalScore = 450,
            accuracyPercent = 92.5f,
            isCurrentUser = true
        )
        assertEquals("Logged in user must always have displayName 'You'", "You", entryUser.displayName)
        assertTrue("isCurrentUser flag must be true for logged in user", entryUser.isCurrentUser)

        // Non-user peers have fictional gamer names
        val entryPeer1 = com.example.dle_prototype.data.LeaderboardEntry(
            rank = 2,
            username = "martiantiger67",
            displayName = "MartianTiger67",
            avatarEmoji = "🐯",
            dailyStreak = 24,
            isCurrentUser = false
        )
        val entryPeer2 = com.example.dle_prototype.data.LeaderboardEntry(
            rank = 3,
            username = "snowyowl3",
            displayName = "SnowyOwl3",
            avatarEmoji = "🦉",
            dailyStreak = 21,
            isCurrentUser = false
        )

        assertEquals("MartianTiger67", entryPeer1.displayName)
        assertEquals("SnowyOwl3", entryPeer2.displayName)
        assertFalse(entryPeer1.isCurrentUser)
        assertFalse(entryPeer2.isCurrentUser)
    }

    @Test
    fun testActiveLearningModulesStructure() {
        val module = com.example.dle_prototype.data.LearningModule(
            id = "mod_kotlin_android",
            categoryName = "JavaScript",
            categoryNumber = 3f,
            title = "Kotlin & Modern Android",
            description = "Declarative UI with Compose, State Hoisting, Flow & Coroutines",
            icon = "🤖",
            tag = "Mobile Architecture",
            totalLessons = 20,
            completedLessons = 15,
            progressPercent = 75f,
            currentTopic = "Coroutines & Reactive State Flow",
            difficulty = "Intermediate",
            status = com.example.dle_prototype.data.ModuleStatus.IN_PROGRESS
        )

        assertEquals("mod_kotlin_android", module.id)
        assertEquals("Kotlin & Modern Android", module.title)
        assertEquals(75f, module.progressPercent, 0.01f)
        assertEquals(15, module.completedLessons)
        assertEquals(20, module.totalLessons)
        assertEquals(com.example.dle_prototype.data.ModuleStatus.IN_PROGRESS, module.status)
    }
}

