package com.example.dle_prototype

import com.example.dle_prototype.data.badges.BadgeTier
import com.example.dle_prototype.data.graph.KnowledgeExpertBadgeCatalog
import com.example.dle_prototype.data.graph.KnowledgeMasteryBadgeEngine
import com.example.dle_prototype.data.graph.KnowledgeMasteryLevel
import com.example.dle_prototype.data.graph.KnowledgeTopicNode
import com.example.dle_prototype.data.graph.TopicMasteryStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeMasteryBadgeSystemTest {

    private fun createTopicNode(
        id: String,
        title: String,
        accuracy: Float,
        attempts: Int = 10,
        level: KnowledgeMasteryLevel
    ): KnowledgeTopicNode {
        return KnowledgeTopicNode(
            id = id,
            title = title,
            category = "Core CS",
            categoryNumber = 1f,
            accuracyPercent = accuracy,
            attemptsCount = attempts,
            status = if (accuracy >= 75f) TopicMasteryStatus.STRENGTH else TopicMasteryStatus.SOLIDIFYING,
            normX = 0.5f,
            normY = 0.5f,
            description = "Test description for $title",
            keyConcepts = listOf("fundamentals", "application"),
            clusterName = "Web Architecture",
            masteryLevel = level
        )
    }

    @Test
    fun testBadgeCatalogDefinitions() {
        val badges = KnowledgeExpertBadgeCatalog.ALL_DEFINITIONS
        assertEquals("Must contain 6 curated Knowledge Graph Expert Badge definitions", 6, badges.size)

        val webArch = KnowledgeExpertBadgeCatalog.getDefinition("kg_expert_web_architect")
        assertNotNull(webArch)
        assertEquals("Web Architect", webArch!!.badgeTitle)
        assertEquals("🌐", webArch.unlockedIconEmoji)
        assertEquals("Web Nexus", webArch.unlockedIconName)
        assertEquals(listOf("html", "css", "js"), webArch.requiredTopicIds)
        assertEquals(BadgeTier.GOLD, webArch.tier)
        assertEquals(300, webArch.xpReward)

        val stackDynamo = KnowledgeExpertBadgeCatalog.getDefinition("kg_expert_stack_dynamo")
        assertNotNull(stackDynamo)
        assertEquals("Full-Stack Dynamo", stackDynamo!!.badgeTitle)
        assertEquals("🐬", stackDynamo.unlockedIconEmoji)
        assertEquals("Database Titan", stackDynamo.unlockedIconName)
        assertEquals(listOf("js", "php", "mysql"), stackDynamo.requiredTopicIds)

        val reactive = KnowledgeExpertBadgeCatalog.getDefinition("kg_expert_reactive_luminary")
        assertNotNull(reactive)
        assertEquals("Reactive Luminary", reactive!!.badgeTitle)
        assertEquals("📱", reactive.unlockedIconEmoji)
        assertEquals(listOf("compose", "coroutines", "tflite"), reactive.requiredTopicIds)

        val silicon = KnowledgeExpertBadgeCatalog.getDefinition("kg_expert_silicon_sage")
        assertNotNull(silicon)
        assertEquals("Silicon Sage", silicon!!.badgeTitle)
        assertEquals("⚙️", silicon.unlockedIconEmoji)
        assertEquals(listOf("c_lang", "python", "big_o"), silicon.requiredTopicIds)

        val synapse = KnowledgeExpertBadgeCatalog.getDefinition("kg_expert_synapse_sovereign")
        assertNotNull(synapse)
        assertEquals("Synapse Sovereign", synapse!!.badgeTitle)
        assertEquals("🧠", synapse.unlockedIconEmoji)
        assertEquals(listOf("python", "tflite", "spaced_rep"), synapse.requiredTopicIds)

        val cosmic = KnowledgeExpertBadgeCatalog.getDefinition("kg_expert_cosmic_grandmaster")
        assertNotNull(cosmic)
        assertEquals("Cosmic Grandmaster", cosmic!!.badgeTitle)
        assertEquals("👑", cosmic.unlockedIconEmoji)
        assertEquals("Cosmic Crown", cosmic.unlockedIconName)
        assertEquals(6, cosmic.requiredExpertCount)
        assertEquals(BadgeTier.DIAMOND, cosmic.tier)
        assertEquals(1200, cosmic.xpReward)
    }

    @Test
    fun testAllBeginnerNodesKeepsBadgesLocked() {
        val beginnerNodes = listOf(
            createTopicNode("html", "HTML5 Foundations", 45f, 3, KnowledgeMasteryLevel.BEGINNER),
            createTopicNode("css", "CSS Modern Layouts", 50f, 4, KnowledgeMasteryLevel.BEGINNER),
            createTopicNode("js", "JavaScript & ES6+", 55f, 5, KnowledgeMasteryLevel.BEGINNER)
        )

        val statuses = KnowledgeMasteryBadgeEngine.evaluateBadges(
            nodes = beginnerNodes,
            currentAvatarEmoji = "👤",
            unlockedMap = emptyMap()
        )

        val webArchStatus = statuses.find { it.definition.id == "kg_expert_web_architect" }
        assertNotNull(webArchStatus)
        assertFalse("Web Architect must be locked when topics are at beginner level", webArchStatus!!.isUnlocked)
        assertEquals(0, webArchStatus.expertTopicsCount)
        assertEquals(3, webArchStatus.totalRequiredTopicsCount)
        assertEquals(0.0f, webArchStatus.progressFraction, 0.001f)
        assertFalse(webArchStatus.isEquipped)
    }

    @Test
    fun testPartialInterconnectedExpertMastery() {
        val partialNodes = listOf(
            createTopicNode("html", "HTML5 Foundations", 88f, 15, KnowledgeMasteryLevel.EXPERT),
            createTopicNode("css", "CSS Modern Layouts", 84f, 12, KnowledgeMasteryLevel.EXPERT),
            createTopicNode("js", "JavaScript & ES6+", 72f, 8, KnowledgeMasteryLevel.INTERMEDIATE)
        )

        val statuses = KnowledgeMasteryBadgeEngine.evaluateBadges(
            nodes = partialNodes,
            currentAvatarEmoji = "👤",
            unlockedMap = emptyMap()
        )

        val webArch = statuses.find { it.definition.id == "kg_expert_web_architect" }!!
        assertFalse("Must remain locked when JS is only intermediate (2/3 expert)", webArch.isUnlocked)
        assertEquals(2, webArch.expertTopicsCount)
        assertEquals(3, webArch.totalRequiredTopicsCount)
        assertEquals(2f / 3f, webArch.progressFraction, 0.01f)

        val htmlDetail = webArch.topicProgress.find { it.topicId == "html" }!!
        assertTrue(htmlDetail.isExpert)
        assertEquals(KnowledgeMasteryLevel.EXPERT, htmlDetail.masteryLevel)

        val jsDetail = webArch.topicProgress.find { it.topicId == "js" }!!
        assertFalse(jsDetail.isExpert)
        assertEquals(KnowledgeMasteryLevel.INTERMEDIATE, jsDetail.masteryLevel)
    }

    @Test
    fun testFullTriadExpertMasteryUnlocksWebArchitectAndEquipsIcon() {
        val expertNodes = listOf(
            createTopicNode("html", "HTML5 Foundations", 92f, 20, KnowledgeMasteryLevel.EXPERT),
            createTopicNode("css", "CSS Modern Layouts", 89f, 18, KnowledgeMasteryLevel.EXPERT),
            createTopicNode("js", "JavaScript & ES6+", 85f, 25, KnowledgeMasteryLevel.EXPERT)
        )

        val statuses = KnowledgeMasteryBadgeEngine.evaluateBadges(
            nodes = expertNodes,
            currentAvatarEmoji = "🌐", // User equipped Web Nexus icon
            unlockedMap = emptyMap()
        )

        val webArch = statuses.find { it.definition.id == "kg_expert_web_architect" }!!
        assertTrue("Web Architect must be unlocked when all 3 topics reach Expert", webArch.isUnlocked)
        assertEquals(3, webArch.expertTopicsCount)
        assertEquals(1.0f, webArch.progressFraction, 0.001f)
        assertTrue("isEquipped must be true when avatar emoji matches unlockedIconEmoji '🌐'", webArch.isEquipped)
        assertNotNull("unlockedAt timestamp must be assigned", webArch.unlockedAt)
    }

    @Test
    fun testCosmicGrandmasterUnlockWithSixInterconnectedTopics() {
        val constellationNodes = listOf(
            createTopicNode("html", "HTML5", 90f, 20, KnowledgeMasteryLevel.EXPERT),
            createTopicNode("css", "CSS3", 85f, 18, KnowledgeMasteryLevel.EXPERT),
            createTopicNode("js", "JavaScript", 88f, 22, KnowledgeMasteryLevel.EXPERT),
            createTopicNode("mysql", "MySQL", 82f, 15, KnowledgeMasteryLevel.EXPERT),
            createTopicNode("compose", "Jetpack Compose", 84f, 16, KnowledgeMasteryLevel.EXPERT),
            createTopicNode("coroutines", "Coroutines & Flow", 86f, 19, KnowledgeMasteryLevel.EXPERT),
            createTopicNode("tflite", "TFLite", 70f, 5, KnowledgeMasteryLevel.INTERMEDIATE),
            createTopicNode("c_lang", "C Language", 65f, 4, KnowledgeMasteryLevel.BEGINNER)
        )

        val statuses = KnowledgeMasteryBadgeEngine.evaluateBadges(
            nodes = constellationNodes,
            currentAvatarEmoji = "👑",
            unlockedMap = emptyMap()
        )

        val grandmaster = statuses.find { it.definition.id == "kg_expert_cosmic_grandmaster" }!!
        assertTrue("Cosmic Grandmaster must be unlocked with 6 Expert nodes", grandmaster.isUnlocked)
        assertEquals(6, grandmaster.expertTopicsCount)
        assertEquals(1.0f, grandmaster.progressFraction, 0.001f)
        assertTrue("Cosmic Crown emoji must be equipped", grandmaster.isEquipped)
    }

    @Test
    fun testPreviouslyUnlockedPersistenceViaMap() {
        val singleNode = listOf(
            createTopicNode("html", "HTML5", 50f, 2, KnowledgeMasteryLevel.BEGINNER)
        )
        val priorUnlockTimestamp = 1700000000000L
        val unlockedMap = mapOf("kg_expert_web_architect" to priorUnlockTimestamp)

        val statuses = KnowledgeMasteryBadgeEngine.evaluateBadges(
            nodes = singleNode,
            currentAvatarEmoji = "👤",
            unlockedMap = unlockedMap
        )

        val webArch = statuses.find { it.definition.id == "kg_expert_web_architect" }!!
        assertTrue("Badge must remain unlocked if present in unlockedMap", webArch.isUnlocked)
        assertEquals(priorUnlockTimestamp, webArch.unlockedAt)
    }
}
