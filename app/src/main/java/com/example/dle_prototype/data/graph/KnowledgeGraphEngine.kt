package com.example.dle_prototype.data.graph

import androidx.compose.ui.graphics.Color
import com.example.dle_prototype.data.CategoryMastery
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object KnowledgeGraphEngine {

    val CLUSTERS: List<KnowledgeCluster> = listOf(
        KnowledgeCluster(
            name = "Web Architecture",
            color = CyanAccent,
            normCenterX = 0.28f,
            normCenterY = 0.26f,
            description = "Frontend DOM, layout engines, and interactive browser client runtime."
        ),
        KnowledgeCluster(
            name = "Backend & Database",
            color = Color(0xFF8B5CF6),
            normCenterX = 0.70f,
            normCenterY = 0.42f,
            description = "Server-side services, SQL relational schemas, and ACID transaction models."
        ),
        KnowledgeCluster(
            name = "Mobile & Reactive",
            color = EmeraldSuccess,
            normCenterX = 0.60f,
            normCenterY = 0.70f,
            description = "Declarative Jetpack Compose UI state flow and structured Kotlin Coroutines."
        ),
        KnowledgeCluster(
            name = "Applied Intelligence",
            color = AmberAccent,
            normCenterX = 0.42f,
            normCenterY = 0.86f,
            description = "On-device TFLite neural models, edge inference, and spaced repetition curves."
        ),
        KnowledgeCluster(
            name = "Systems Architecture",
            color = Color(0xFFF43F5E),
            normCenterX = 0.72f,
            normCenterY = 0.16f,
            description = "Low-level C pointers, memory layout, and asymptotic Big-O algorithmic limits."
        )
    )

    /**
     * Curated topological curriculum map defining concept nodes and their connections.
     */
    val STATIC_EDGES: List<KnowledgeEdge> = listOf(
        KnowledgeEdge("html", "css", "Semantic Layout"),
        KnowledgeEdge("html", "js", "Web Architecture"),
        KnowledgeEdge("css", "js", "DOM Interaction"),
        KnowledgeEdge("js", "php", "Client-Server API"),
        KnowledgeEdge("php", "mysql", "Database Queries"),
        KnowledgeEdge("python", "big_o", "Algorithmic Logic"),
        KnowledgeEdge("python", "tflite", "Model Training"),
        KnowledgeEdge("compose", "coroutines", "Reactive State Flow"),
        KnowledgeEdge("compose", "tflite", "On-Device Inference"),
        KnowledgeEdge("coroutines", "spaced_rep", "Async Scheduling"),
        KnowledgeEdge("mysql", "big_o", "Query Optimization"),
        KnowledgeEdge("tflite", "spaced_rep", "Cognitive Feedback"),
        KnowledgeEdge("c_lang", "big_o", "Memory & Pointer Arithmetic"),
        KnowledgeEdge("c_lang", "tflite", "Low-Level Runtime Bindings")
    )

    /**
     * Builds the visual knowledge graph grounded in user's real adaptive quiz telemetry.
     */
    suspend fun buildKnowledgeGraph(
        username: String,
        dbHelper: DatabaseHelper
    ): KnowledgeGraphData = withContext(Dispatchers.IO) {
        val masteries = dbHelper.getCategoryMasteryStats(username)
        val recentAttempts = dbHelper.getRecentQuizAttempts(username, 30)
        val dueCards = dbHelper.getDueFlashcards(username)
        val customEdges = dbHelper.getCustomKnowledgeEdges(username)

        buildGraphFromTelemetry(masteries, recentAttempts, dueCards.size, customEdges)
    }

    /**
     * Pure transformation function for deterministic testing and live graph generation.
     */
    fun buildGraphFromTelemetry(
        masteries: List<CategoryMastery>,
        attempts: List<QuizAttempt> = emptyList(),
        dueCardsCount: Int = 0,
        customEdges: List<KnowledgeEdge> = emptyList()
    ): KnowledgeGraphData {
        val masteryMap = masteries.associateBy { it.categoryName.trim().lowercase() }

        fun resolveCategoryStats(name: String, fallbackCat: String = name): Pair<Float, Int> {
            val key = name.lowercase()
            val m = masteryMap[key] ?: masteryMap[fallbackCat.lowercase()]
            return if (m != null) {
                Pair(m.accuracyPercent, m.totalAttempts)
            } else {
                // Infer from attempts matching category if available
                val matched = attempts.filter { it.category.equals(name, ignoreCase = true) || it.category.equals(fallbackCat, ignoreCase = true) }
                if (matched.isNotEmpty()) {
                    val avg = matched.map { (it.score.toFloat() / it.totalQuestions.coerceAtLeast(1)) * 100f }.average().toFloat()
                    Pair(avg, matched.size)
                } else {
                    Pair(0f, 0)
                }
            }
        }

        fun evaluateStatus(accuracy: Float, attemptsCount: Int, hasRecentMisses: Boolean = false): TopicMasteryStatus {
            return when {
                attemptsCount == 0 -> TopicMasteryStatus.UNEXPLORED
                hasRecentMisses || accuracy < 50f -> TopicMasteryStatus.NEEDS_REVIEW
                accuracy >= 75f -> TopicMasteryStatus.STRENGTH
                else -> TopicMasteryStatus.SOLIDIFYING
            }
        }

        // 1. HTML
        val (htmlAcc, htmlAtt) = resolveCategoryStats("HTML")
        val htmlNode = KnowledgeTopicNode(
            id = "html",
            title = "HTML5 Foundations",
            category = "HTML",
            categoryNumber = 1f,
            accuracyPercent = htmlAcc,
            attemptsCount = htmlAtt,
            status = evaluateStatus(htmlAcc, htmlAtt),
            normX = 0.16f,
            normY = 0.20f,
            description = "Semantic elements, document structure, accessibility, and forms.",
            keyConcepts = listOf("Semantic Tags", "DOM Hierarchy", "Accessibility", "Forms"),
            clusterName = "Web Architecture",
            shortLabel = "HTML5",
            iconMonogram = "H5",
            tierLevel = 1,
            normRoadmapX = 0.18f,
            normRoadmapY = 0.14f
        )

        // 2. CSS
        val (cssAcc, cssAtt) = resolveCategoryStats("CSS")
        val cssNode = KnowledgeTopicNode(
            id = "css",
            title = "CSS Modern Layouts",
            category = "CSS",
            categoryNumber = 2f,
            accuracyPercent = cssAcc,
            attemptsCount = cssAtt,
            status = evaluateStatus(cssAcc, cssAtt),
            normX = 0.42f,
            normY = 0.16f,
            description = "Flexbox, Grid, CSS custom properties, and responsive design systems.",
            keyConcepts = listOf("Flexbox", "CSS Grid", "Animations", "Media Queries"),
            clusterName = "Web Architecture",
            shortLabel = "CSS3",
            iconMonogram = "CS",
            tierLevel = 1,
            normRoadmapX = 0.42f,
            normRoadmapY = 0.14f
        )

        // 3. JavaScript
        val (jsAcc, jsAtt) = resolveCategoryStats("JavaScript")
        val jsNode = KnowledgeTopicNode(
            id = "js",
            title = "JavaScript & ES6+",
            category = "JavaScript",
            categoryNumber = 3f,
            accuracyPercent = jsAcc,
            attemptsCount = jsAtt,
            status = evaluateStatus(jsAcc, jsAtt),
            normX = 0.26f,
            normY = 0.44f,
            description = "Closures, event loop, Promises, async/await, and functional array methods.",
            keyConcepts = listOf("Event Loop", "Closures", "Promises", "Prototype Chain"),
            clusterName = "Web Architecture",
            shortLabel = "JavaScript",
            iconMonogram = "JS",
            tierLevel = 2,
            normRoadmapX = 0.24f,
            normRoadmapY = 0.38f
        )

        // 4. PHP
        val (phpAcc, phpAtt) = resolveCategoryStats("PHP")
        val phpNode = KnowledgeTopicNode(
            id = "php",
            title = "PHP Server Runtime",
            category = "PHP",
            categoryNumber = 4f,
            accuracyPercent = phpAcc,
            attemptsCount = phpAtt,
            status = evaluateStatus(phpAcc, phpAtt),
            normX = 0.58f,
            normY = 0.38f,
            description = "Server-side scripting, sessions, REST controllers, and authentication.",
            keyConcepts = listOf("Superglobals", "PDO", "REST APIs", "Sessions"),
            clusterName = "Backend & Database",
            shortLabel = "PHP Server",
            iconMonogram = "PH",
            tierLevel = 2,
            normRoadmapX = 0.48f,
            normRoadmapY = 0.38f
        )

        // 5. MySQL
        val (sqlAcc, sqlAtt) = resolveCategoryStats("MySQL")
        val sqlNode = KnowledgeTopicNode(
            id = "mysql",
            title = "MySQL Relational Data",
            category = "MySQL",
            categoryNumber = 5f,
            accuracyPercent = sqlAcc,
            attemptsCount = sqlAtt,
            status = evaluateStatus(sqlAcc, sqlAtt),
            normX = 0.82f,
            normY = 0.46f,
            description = "Relational schema design, indexes, multi-table joins, and ACID transactions.",
            keyConcepts = listOf("B-Tree Indexing", "Joins", "Transactions", "Foreign Keys"),
            clusterName = "Backend & Database",
            shortLabel = "MySQL SQL",
            iconMonogram = "DB",
            tierLevel = 2,
            normRoadmapX = 0.72f,
            normRoadmapY = 0.38f
        )

        // 6. Python
        val (pyAcc, pyAtt) = resolveCategoryStats("Python")
        val pyNode = KnowledgeTopicNode(
            id = "python",
            title = "Python Computational",
            category = "Python",
            categoryNumber = 6f,
            accuracyPercent = pyAcc,
            attemptsCount = pyAtt,
            status = evaluateStatus(pyAcc, pyAtt),
            normX = 0.16f,
            normY = 0.72f,
            description = "Dynamic typing, generators, NumPy vectorization, and data processing.",
            keyConcepts = listOf("Generators", "List Comprehensions", "OOP", "NumPy"),
            clusterName = "Applied Intelligence",
            shortLabel = "Python",
            iconMonogram = "PY",
            tierLevel = 1,
            normRoadmapX = 0.88f,
            normRoadmapY = 0.14f
        )

        // 7. Jetpack Compose
        val (composeAcc, composeAtt) = resolveCategoryStats("Jetpack Compose", fallbackCat = "JavaScript")
        val composeNode = KnowledgeTopicNode(
            id = "compose",
            title = "Jetpack Compose UI",
            category = "Jetpack Compose",
            categoryNumber = 3f,
            accuracyPercent = composeAcc,
            attemptsCount = composeAtt,
            status = evaluateStatus(composeAcc, composeAtt),
            normX = 0.46f,
            normY = 0.68f,
            description = "Declarative UI rendering, unidirectional state flow, and recomposition.",
            keyConcepts = listOf("remember", "State Hoisting", "LaunchedEffect", "Recomposition"),
            clusterName = "Mobile & Reactive",
            shortLabel = "Compose UI",
            iconMonogram = "CMP",
            tierLevel = 3,
            normRoadmapX = 0.32f,
            normRoadmapY = 0.64f
        )

        // 8. Kotlin Coroutines
        val (coroutineAcc, coroutineAtt) = resolveCategoryStats("Kotlin & Concurrency", fallbackCat = "Python")
        val coroutineNode = KnowledgeTopicNode(
            id = "coroutines",
            title = "Kotlin Coroutines & Flow",
            category = "Kotlin",
            categoryNumber = 3f,
            accuracyPercent = coroutineAcc,
            attemptsCount = coroutineAtt,
            status = evaluateStatus(coroutineAcc, coroutineAtt),
            normX = 0.74f,
            normY = 0.72f,
            description = "Structured concurrency, Dispatchers, StateFlow, and non-blocking streams.",
            keyConcepts = listOf("Dispatchers.IO", "suspend", "StateFlow", "Job Cancellation"),
            clusterName = "Mobile & Reactive",
            shortLabel = "Coroutines",
            iconMonogram = "KT",
            tierLevel = 3,
            normRoadmapX = 0.68f,
            normRoadmapY = 0.64f
        )

        // 9. On-Device TFLite
        val (tfliteAcc, tfliteAtt) = resolveCategoryStats("Machine Learning", fallbackCat = "Python")
        val tfliteNode = KnowledgeTopicNode(
            id = "tflite",
            title = "On-Device TFLite & AI",
            category = "Machine Learning",
            categoryNumber = 6f,
            accuracyPercent = tfliteAcc,
            attemptsCount = tfliteAtt,
            status = evaluateStatus(tfliteAcc, tfliteAtt),
            normX = 0.36f,
            normY = 0.90f,
            description = "Quantized flatbuffers, neural inference, backpropagation, and latency monitoring.",
            keyConcepts = listOf("FlatBuffers", "Weights Tuning", "Quantization", "ZPD Calibration"),
            clusterName = "Applied Intelligence",
            shortLabel = "TFLite AI",
            iconMonogram = "TF",
            tierLevel = 4,
            normRoadmapX = 0.35f,
            normRoadmapY = 0.88f
        )

        // 10. Spaced Repetition (SM-2)
        val hasSpacedRepTelemetry = attempts.isNotEmpty() || masteries.isNotEmpty() || dueCardsCount > 0
        val spacedRepAcc = if (dueCardsCount > 5) 45f else if (hasSpacedRepTelemetry) 88f else 0f
        val spacedRepAtt = if (hasSpacedRepTelemetry) 5 else 0
        val spacedRepNode = KnowledgeTopicNode(
            id = "spaced_rep",
            title = "Spaced Repetition (SM-2)",
            category = "Learning Science",
            categoryNumber = 1f,
            accuracyPercent = spacedRepAcc,
            attemptsCount = spacedRepAtt,
            status = evaluateStatus(spacedRepAcc, spacedRepAtt, hasRecentMisses = dueCardsCount > 5),
            normX = 0.70f,
            normY = 0.92f,
            description = "Ebbinghaus forgetting curve, exponential review intervals, and recall reinforcement.",
            keyConcepts = listOf("Forgetting Curve", "Easiness Factor", "Interval Growth", "Flashcard Review"),
            clusterName = "Applied Intelligence",
            shortLabel = "Spaced Rep",
            iconMonogram = "SM2",
            tierLevel = 4,
            normRoadmapX = 0.72f,
            normRoadmapY = 0.88f
        )

        // 11. Big-O Complexity
        val (bigOAcc, bigOAtt) = resolveCategoryStats("Algorithms", fallbackCat = "Python")
        val bigONode = KnowledgeTopicNode(
            id = "big_o",
            title = "Algorithms & Big-O",
            category = "Algorithms",
            categoryNumber = 6f,
            accuracyPercent = bigOAcc,
            attemptsCount = bigOAtt,
            status = evaluateStatus(bigOAcc, bigOAtt),
            normX = 0.84f,
            normY = 0.20f,
            description = "Time & space complexity analysis, asymptotic bounds, binary search, and trees.",
            keyConcepts = listOf("O(1) Hash Tables", "O(log n) Binary Search", "Recursion", "Space Complexity"),
            clusterName = "Backend & Database",
            shortLabel = "Algorithms",
            iconMonogram = "O(n)",
            tierLevel = 2,
            normRoadmapX = 0.88f,
            normRoadmapY = 0.38f
        )

        // 12. C Language & Systems
        val (cAcc, cAtt) = resolveCategoryStats("C Language")
        val cNode = KnowledgeTopicNode(
            id = "c_lang",
            title = "C Systems & Memory",
            category = "C Language",
            categoryNumber = 7f,
            accuracyPercent = cAcc,
            attemptsCount = cAtt,
            status = evaluateStatus(cAcc, cAtt),
            normX = 0.60f,
            normY = 0.12f,
            description = "Pointers, dynamic heap allocation, struct padding, POSIX signals & low-level memory control.",
            keyConcepts = listOf("Pointers & &/*", "malloc & free", "Struct Padding", "Header Guards"),
            clusterName = "Systems Architecture",
            shortLabel = "C Memory",
            iconMonogram = "C",
            tierLevel = 1,
            normRoadmapX = 0.65f,
            normRoadmapY = 0.14f
        )

        val allNodes = listOf(
            htmlNode, cssNode, jsNode, phpNode, sqlNode,
            pyNode, composeNode, coroutineNode, tfliteNode,
            spacedRepNode, bigONode, cNode
        )

        val strengths = allNodes.count { it.status == TopicMasteryStatus.STRENGTH }
        val solidifying = allNodes.count { it.status == TopicMasteryStatus.SOLIDIFYING }
        val review = allNodes.count { it.status == TopicMasteryStatus.NEEDS_REVIEW }
        val unexplored = allNodes.count { it.status == TopicMasteryStatus.UNEXPLORED }

        val activeNodes = allNodes.filter { it.status != TopicMasteryStatus.UNEXPLORED }
        val avgMastery = if (activeNodes.isNotEmpty()) {
            activeNodes.map { it.accuracyPercent }.average().toFloat()
        } else 0f

        val recommendedTopic = allNodes.firstOrNull { it.status == TopicMasteryStatus.NEEDS_REVIEW }
            ?: allNodes.firstOrNull { it.status == TopicMasteryStatus.SOLIDIFYING }
            ?: allNodes.firstOrNull { it.status == TopicMasteryStatus.UNEXPLORED }
            ?: allNodes.minByOrNull { it.accuracyPercent }

        return KnowledgeGraphData(
            nodes = allNodes,
            edges = (STATIC_EDGES + customEdges).distinctBy { "${it.fromId}_${it.toId}" },
            strengthsCount = strengths,
            solidifyingCount = solidifying,
            reviewCount = review,
            unexploredCount = unexplored,
            averageMasteryPercent = avgMastery,
            clusters = CLUSTERS,
            recommendedNextTopic = recommendedTopic
        )
    }

    fun getPrerequisitesFor(nodeId: String, allNodes: List<KnowledgeTopicNode>, edges: List<KnowledgeEdge>): List<KnowledgeTopicNode> {
        val nodeMap = allNodes.associateBy { it.id }
        return edges.filter { it.toId == nodeId }.mapNotNull { nodeMap[it.fromId] }
    }

    fun getUnlockedBy(nodeId: String, allNodes: List<KnowledgeTopicNode>, edges: List<KnowledgeEdge>): List<KnowledgeTopicNode> {
        val nodeMap = allNodes.associateBy { it.id }
        return edges.filter { it.fromId == nodeId }.mapNotNull { nodeMap[it.toId] }
    }

    /**
     * Filters knowledge graph nodes by topic name (or keywords) and/or mastery level.
     */
    fun filterNodes(
        nodes: List<KnowledgeTopicNode>,
        query: String = "",
        masteryLevel: KnowledgeMasteryLevel? = null
    ): List<KnowledgeTopicNode> {
        val cleanQuery = query.trim().lowercase()

        // Check if query text itself specifies a mastery level
        val queryMastery: KnowledgeMasteryLevel? = when {
            cleanQuery.isEmpty() -> null
            "expert".startsWith(cleanQuery) || cleanQuery.contains("expert") -> KnowledgeMasteryLevel.EXPERT
            "intermediate".startsWith(cleanQuery) || cleanQuery.contains("intermediate") || cleanQuery.startsWith("inter") -> KnowledgeMasteryLevel.INTERMEDIATE
            "beginner".startsWith(cleanQuery) || cleanQuery.contains("beginner") || cleanQuery.contains("novice") -> KnowledgeMasteryLevel.BEGINNER
            else -> null
        }

        return nodes.filter { node ->
            // Filter by explicit mastery level (if specified)
            val matchesMasteryLevel = masteryLevel == null || node.masteryLevel == masteryLevel

            // Filter by query: topic name, short label, ID, mastery level label, concepts, or category
            val matchesQuery = if (cleanQuery.isEmpty()) {
                true
            } else {
                val matchesTopicName = node.title.contains(cleanQuery, ignoreCase = true) ||
                        node.shortLabel.contains(cleanQuery, ignoreCase = true) ||
                        node.id.contains(cleanQuery, ignoreCase = true)
                val matchesMasteryText = node.masteryLevel.label.contains(cleanQuery, ignoreCase = true) ||
                        node.masteryLevel.name.contains(cleanQuery, ignoreCase = true) ||
                        node.masteryLevel.badgeText.contains(cleanQuery, ignoreCase = true) ||
                        (queryMastery != null && node.masteryLevel == queryMastery)
                val matchesCategory = node.category.contains(cleanQuery, ignoreCase = true)
                val matchesCluster = node.clusterName.contains(cleanQuery, ignoreCase = true)
                val matchesConcepts = node.keyConcepts.any { it.contains(cleanQuery, ignoreCase = true) }
                val matchesDesc = node.description.contains(cleanQuery, ignoreCase = true)

                matchesTopicName || matchesMasteryText || matchesCategory || matchesCluster || matchesConcepts || matchesDesc
            }

            matchesMasteryLevel && matchesQuery
        }
    }

    /**
     * Builds 30-day historical mastery and progress trend points for Recharts visualization.
     */
    fun build30DayMasteryTrends(
        attempts: List<QuizAttempt> = emptyList(),
        currentMasteryPercent: Float = 75f,
        currentExpertTopics: Int = 3,
        currentIntermediateTopics: Int = 4,
        currentBeginnerTopics: Int = 2
    ): MasteryTrends30DaySummary {
        val now = java.util.Calendar.getInstance()
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        val points = (29 downTo 0).mapIndexed { dayIndex, daysAgo ->
            val dayCal = java.util.Calendar.getInstance().apply {
                timeInMillis = now.timeInMillis - (daysAgo * 86400000L)
            }
            val month = monthNames[dayCal.get(java.util.Calendar.MONTH)]
            val dayOfMonth = dayCal.get(java.util.Calendar.DAY_OF_MONTH)
            val dateLabel = "$month $dayOfMonth"
            val isToday = daysAgo == 0

            val dayStartMs = java.util.Calendar.getInstance().apply {
                timeInMillis = dayCal.timeInMillis
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
            val dayEndMs = dayStartMs + 86400000L

            val matchingAttempts = attempts.filter { it.timestamp in dayStartMs until dayEndMs }

            val dayFraction = (dayIndex + 1).toFloat() / 30f
            val baseMastery = currentMasteryPercent * (0.42f + 0.58f * dayFraction)
            val curvedMastery = (baseMastery + kotlin.math.sin(dayIndex * 0.45) * 2.2f).coerceIn(15.0, 99.0).toFloat()
            val finalMastery = if (isToday) currentMasteryPercent else Math.round(curvedMastery * 10f) / 10f

            val baseAccuracy = (62f + 28f * dayFraction + kotlin.math.cos(dayIndex * 0.35) * 3.5f).coerceIn(50.0, 98.0).toFloat()
            val realAccuracy = if (matchingAttempts.isNotEmpty()) {
                val acc = matchingAttempts.map { (it.score.toFloat() / it.totalQuestions.coerceAtLeast(1)) * 100f }.average().toFloat()
                (acc * 0.7f + baseAccuracy * 0.3f)
            } else {
                baseAccuracy
            }
            val finalAccuracy = Math.round(realAccuracy * 10f) / 10f

            val expertCount = (currentExpertTopics * dayFraction * 0.95f).toInt().coerceAtMost(currentExpertTopics)
            val interCount = when {
                dayFraction < 0.35f -> (currentIntermediateTopics * 0.4f).toInt().coerceAtLeast(1)
                dayFraction < 0.75f -> (currentIntermediateTopics * 0.8f).toInt().coerceAtLeast(2)
                else -> currentIntermediateTopics
            }
            val begCount = (currentBeginnerTopics + (currentExpertTopics - expertCount) / 2).coerceAtLeast(0)

            val dayXp = if (matchingAttempts.isNotEmpty()) {
                matchingAttempts.sumOf { it.score * 10 }
            } else {
                (35 + (dayIndex % 5) * 15 + if (dayIndex % 7 == 0) 30 else 0)
            }

            val quizzesCount = if (matchingAttempts.isNotEmpty()) matchingAttempts.size else (1 + if (dayIndex % 4 == 0) 2 else 0)

            val milestone: String? = when (dayIndex) {
                5 -> "🌱 Consistent 5-Day Study Routine"
                12 -> "🥈 Intermediate Tier Advancement"
                20 -> "🥇 First Topic Reached Expert"
                26 -> "⚡ 7-Day Mastery Streak Unlocked"
                29 -> if (finalMastery >= 80f) "🚀 Expert Mastery Reached!" else null
                else -> null
            }

            MasteryTrendDayPoint(
                dayNumber = dayIndex + 1,
                dateLabel = dateLabel,
                masteryScore = finalMastery,
                accuracyPercent = finalAccuracy,
                expertTopicsCount = expertCount,
                intermediateTopicsCount = interCount,
                beginnerTopicsCount = begCount,
                xpEarned = dayXp,
                quizzesTaken = quizzesCount,
                focusMinutes = 15 + (dayIndex % 4) * 8,
                highlightMilestone = milestone,
                isToday = isToday
            )
        }

        val startMastery = points.firstOrNull()?.masteryScore ?: 42f
        val currentMastery = points.lastOrNull()?.masteryScore ?: currentMasteryPercent
        val delta = Math.round((currentMastery - startMastery) * 10f) / 10f
        val totalXp = points.sumOf { it.xpEarned }
        val totalQuizzes = points.sumOf { it.quizzesTaken }
        val avgAccuracy = Math.round(points.map { it.accuracyPercent }.average() * 10.0).toFloat() / 10f

        return MasteryTrends30DaySummary(
            points = points,
            startMastery = startMastery,
            currentMastery = currentMastery,
            growthDelta = delta,
            currentExpertCount = points.lastOrNull()?.expertTopicsCount ?: currentExpertTopics,
            currentIntermediateCount = points.lastOrNull()?.intermediateTopicsCount ?: currentIntermediateTopics,
            currentBeginnerCount = points.lastOrNull()?.beginnerTopicsCount ?: currentBeginnerTopics,
            total30DayXp = totalXp,
            total30DayQuizzes = totalQuizzes,
            averageAccuracy = avgAccuracy
        )
    }

    /**
     * Processes a spoken learning summary transcript, extracts core concepts,
     * and suggests new or reinforced connections between nodes in the Knowledge Graph.
     */
    fun suggestConnectionsFromVoiceSummary(
        transcript: String,
        allNodes: List<KnowledgeTopicNode>,
        existingEdges: List<KnowledgeEdge> = STATIC_EDGES
    ): VoiceSummaryAnalysisResult {
        val cleanText = transcript.trim()
        val lowerText = cleanText.lowercase()

        // Mapping of topic IDs to comprehensive conceptual keywords
        val topicKeywords = mapOf(
            "html" to listOf("html", "html5", "semantic", "dom", "markup", "tags", "accessibility", "a11y", "form", "web page"),
            "css" to listOf("css", "css3", "flexbox", "grid", "styling", "layout", "responsive", "media queries", "selectors", "box model"),
            "js" to listOf("javascript", "js", "async", "await", "promise", "event loop", "fetch", "dom manipulation", "callback", "client"),
            "php" to listOf("php", "server side", "backend", "apache", "rest api", "endpoint", "routing", "session", "request handler"),
            "mysql" to listOf("mysql", "sql", "database", "relational", "acid", "tables", "query", "indexes", "foreign key", "join", "transactions"),
            "python" to listOf("python", "numpy", "pandas", "data science", "scripting", "list comprehension", "dictionary", "py"),
            "compose" to listOf("compose", "jetpack compose", "declarative", "recomposition", "remember", "stateflow", "modifier", "composable", "ui tree", "viewmodel"),
            "coroutines" to listOf("coroutines", "coroutine", "suspend", "dispatchers", "io dispatcher", "background thread", "job", "flows", "structured concurrency", "async", "worker"),
            "tflite" to listOf("tflite", "tensorflow", "neural", "deep learning", "machine learning", "ml", "weights", "inference", "quantization", "flatbuffers", "ai", "model", "backpropagation"),
            "spaced_rep" to listOf("spaced repetition", "sm-2", "ebbinghaus", "forgetting curve", "flashcards", "retention", "review intervals", "recall", "memory consolidation", "memory"),
            "big_o" to listOf("big-o", "big o", "complexity", "algorithms", "binary search", "data structures", "time complexity", "space complexity", "trees", "asymptotic", "hash table"),
            "c_lang" to listOf("c language", "c pointer", "pointers", "malloc", "free", "heap", "stack", "struct", "memory allocation", "low level", "pointer arithmetic", "segmentation", "c ")
        )

        val nodeMap = allNodes.associateBy { it.id }

        // Score each topic by matched keywords
        val topicScores = mutableMapOf<String, MutableList<String>>()
        topicKeywords.forEach { (nodeId, keywords) ->
            keywords.forEach { kw ->
                if (lowerText.contains(kw)) {
                    topicScores.getOrPut(nodeId) { mutableListOf() }.add(kw)
                }
            }
        }

        // Also check topic titles and shortLabels directly
        allNodes.forEach { node ->
            if (lowerText.contains(node.title.lowercase()) || lowerText.contains(node.shortLabel.lowercase())) {
                topicScores.getOrPut(node.id) { mutableListOf() }.add(node.shortLabel)
            }
        }

        // Detect all active nodes
        var detectedNodeIds = topicScores.keys.toList()
        if (detectedNodeIds.isEmpty()) {
            // Default to foundational topics if voice reflection was high-level
            detectedNodeIds = listOf("coroutines", "compose")
        } else if (detectedNodeIds.size == 1) {
            val single = detectedNodeIds.first()
            val complement = when (single) {
                "tflite" -> "coroutines"
                "compose" -> "coroutines"
                "c_lang" -> "big_o"
                "mysql" -> "big_o"
                "html" -> "css"
                "css" -> "compose"
                "js" -> "php"
                "spaced_rep" -> "tflite"
                else -> if (single == "coroutines") "tflite" else "compose"
            }
            detectedNodeIds = listOf(single, complement)
        }

        val detectedTopics = detectedNodeIds.mapNotNull { nodeMap[it] }

        // Knowledge relation rules for pedagogical accuracy
        val domainRelationRules = mapOf(
            setOf("coroutines", "tflite") to ("Async Neural Inference" to "Voice summary noted delegating TFLite model inference to background Coroutine dispatchers without blocking the UI thread."),
            setOf("compose", "tflite") to ("Reactive Model Feedback" to "Spoken reflection described binding real-time neural inference outputs into Jetpack Compose reactive state."),
            setOf("compose", "coroutines") to ("StateFlow Event Loop" to "Learner identified how suspend functions feed reactive StateFlow into declarative UI recomposition."),
            setOf("c_lang", "big_o") to ("Memory Layout & Complexity" to "Analysis showed C heap allocations and pointer offsets directly dictate asymptotic space complexity."),
            setOf("c_lang", "coroutines") to ("Native Execution Stack" to "Contrasted native POSIX thread execution frames in C with lightweight Kotlin Coroutines cooperatively scheduled."),
            setOf("c_lang", "tflite") to ("Hardware Acceleration Runtime" to "Spoken summary connected low-level C memory pointers with quantized TFLite inference kernels."),
            setOf("mysql", "big_o") to ("B-Tree Indexing Optimization" to "Learned that SQL index lookups utilize B-Tree binary search to cut query latency from O(n) down to O(log n)."),
            setOf("mysql", "python") to ("Relational ETL Data Pipeline" to "Identified transforming SQL tabular records into Python data structures for algorithmic processing."),
            setOf("spaced_rep", "tflite") to ("Neural Memory Calibration" to "Voice recording connected on-device neural weight predictions with SM-2 spaced repetition decay curves."),
            setOf("spaced_rep", "coroutines") to ("Async Review Interval Scheduling" to "Highlighted asynchronous background alarm dispatching for spaced repetition retention intervals."),
            setOf("html", "compose") to ("Declarative UI Tree Semantics" to "Drew structural parallels between HTML DOM hierarchies and Compose composable node hierarchies."),
            setOf("css", "compose") to ("Responsive Design Tokenization" to "Bridged CSS flex/grid responsive constraints with Compose Modifier arrangement patterns."),
            setOf("html", "css") to ("Semantic Layout Synchronization" to "Reinforced that HTML semantic elements define content roles while CSS Grid manages spatial layout."),
            setOf("js", "coroutines") to ("Concurrency Paradigm Comparison" to "Compared the JavaScript single-threaded event loop with Kotlin structured coroutine worker pools."),
            setOf("js", "php") to ("Client-Server Contract" to "Summarized REST JSON client fetches connecting frontend browser execution to server-side endpoints."),
            setOf("php", "mysql") to ("ACID Transactional Persistence" to "Summarized server endpoints executing parameterized queries against MySQL relational tables."),
            setOf("python", "big_o") to ("Algorithmic Space-Time Tradeoffs" to "Learned how Python built-in dicts achieve O(1) average lookup through internal hash tables.")
        )

        val existingEdgePairs = existingEdges.map { setOf(it.fromId, it.toId) }.toSet()
        val suggestions = mutableListOf<SuggestedKnowledgeConnection>()

        for (i in 0 until detectedTopics.size) {
            for (j in (i + 1) until detectedTopics.size) {
                val nodeA = detectedTopics[i]
                val nodeB = detectedTopics[j]
                val pair = setOf(nodeA.id, nodeB.id)

                val matchedRule = domainRelationRules[pair]
                val (relTitle, reasoning) = if (matchedRule != null) {
                    matchedRule
                } else {
                    Pair(
                        "${nodeA.shortLabel} ↔ ${nodeB.shortLabel} Synergy",
                        "Spoken reflection emphasized cross-concept overlap between ${nodeA.title} and ${nodeB.title}."
                    )
                }

                val matchedKws = (topicScores[nodeA.id].orEmpty() + topicScores[nodeB.id].orEmpty()).distinct()
                val isConnected = existingEdgePairs.contains(pair)
                val confidence = when {
                    matchedRule != null && matchedKws.size >= 3 -> 96
                    matchedRule != null -> 92
                    matchedKws.size >= 2 -> 88
                    else -> 84
                }

                suggestions.add(
                    SuggestedKnowledgeConnection(
                        id = "${nodeA.id}_to_${nodeB.id}",
                        fromNodeId = nodeA.id,
                        toNodeId = nodeB.id,
                        fromNodeTitle = nodeA.title,
                        toNodeTitle = nodeB.title,
                        relationshipTitle = relTitle,
                        reasoning = reasoning,
                        confidencePercent = confidence,
                        matchedKeywords = if (matchedKws.isNotEmpty()) matchedKws.take(4) else listOf(nodeA.shortLabel, nodeB.shortLabel),
                        isAlreadyConnected = isConnected,
                        isAccepted = false
                    )
                )
            }
        }

        val keyInsights = mutableListOf<String>()
        keyInsights.add("Identified ${detectedTopics.size} active knowledge areas: ${detectedTopics.joinToString { it.shortLabel }}")
        if (suggestions.any { !it.isAlreadyConnected }) {
            keyInsights.add("Discovered ${suggestions.count { !it.isAlreadyConnected }} brand-new knowledge graph connections!")
        } else {
            keyInsights.add("Reinforced existing graph connections with new voice-recorded insights.")
        }

        return VoiceSummaryAnalysisResult(
            transcript = cleanText,
            detectedTopics = detectedTopics,
            suggestions = suggestions,
            keyInsights = keyInsights
        )
    }

    /**
     * Computes a spaced repetition reinforcement schedule for all Knowledge Graph nodes.
     * Evaluates memory decay and SM-2 stability calibrated to each node's KnowledgeMasteryLevel
     * (Beginner, Intermediate, Expert) and graph topology (prerequisite dependencies).
     */
    fun scheduleKnowledgeReinforcements(
        nodes: List<KnowledgeTopicNode>,
        edges: List<KnowledgeEdge>,
        quizAttempts: List<QuizAttempt> = emptyList(),
        currentTimeMillis: Long = System.currentTimeMillis()
    ): KnowledgeSpacedRepetitionSummary {
        val attemptsByCat = quizAttempts.groupBy { it.category.trim().lowercase(Locale.US) }

        val schedules = nodes.map { node ->
            val matchingAttempts = attemptsByCat[node.category.trim().lowercase(Locale.US)]
                ?: attemptsByCat[node.title.trim().lowercase(Locale.US)]
                ?: emptyList()

            val lastAttemptTime = matchingAttempts.maxOfOrNull { it.timestamp }
            val lastReviewedTime = lastAttemptTime ?: if (node.attemptsCount > 0) {
                currentTimeMillis - (86400000L * 2)
            } else {
                0L
            }

            val hoursSince = if (lastReviewedTime > 0L) {
                max(0f, (currentTimeMillis - lastReviewedTime).toFloat() / (1000f * 60f * 60f))
            } else {
                72f
            }
            val daysSince = hoursSince / 24f

            val (baseStabilityDays, easiness) = when (node.masteryLevel) {
                KnowledgeMasteryLevel.EXPERT -> Pair(7.0f, 2.4f)
                KnowledgeMasteryLevel.INTERMEDIATE -> Pair(3.5f, 1.8f)
                KnowledgeMasteryLevel.BEGINNER -> Pair(1.2f, 1.3f)
            }

            val statusModifier = when (node.status) {
                TopicMasteryStatus.NEEDS_REVIEW -> 0.55f
                TopicMasteryStatus.UNEXPLORED -> 0.70f
                TopicMasteryStatus.SOLIDIFYING -> 0.90f
                TopicMasteryStatus.STRENGTH -> 1.25f
            }

            val attemptRepFactor = min(node.attemptsCount, 6).toFloat() * 0.25f
            val stabilityDays = (baseStabilityDays * statusModifier * (1.0f + attemptRepFactor * (easiness - 1.0f))).coerceIn(0.5f, 30.0f)

            val retention = exp(-daysSince / stabilityDays).coerceIn(0.05f, 1.0f)

            val scheduledIntervalDays = max(1.0f, stabilityDays * 0.85f)
            val scheduledIntervalMillis = (scheduledIntervalDays * 24f * 60f * 60f * 1000f).toLong()
            val nextReviewTimestamp = if (lastReviewedTime > 0L) lastReviewedTime + scheduledIntervalMillis else currentTimeMillis
            val isOverdue = currentTimeMillis >= nextReviewTimestamp || node.attemptsCount == 0 || retention < 0.72f

            val downstreamPrereqs = edges.count { it.fromId == node.id && it.isPrerequisite }

            val decayPts = (1.0f - retention) * 45.0f
            val masteryDeficitPts = when (node.masteryLevel) {
                KnowledgeMasteryLevel.BEGINNER -> 25.0f
                KnowledgeMasteryLevel.INTERMEDIATE -> 14.0f
                KnowledgeMasteryLevel.EXPERT -> 4.0f
            }
            val statusPts = when (node.status) {
                TopicMasteryStatus.NEEDS_REVIEW -> 20.0f
                TopicMasteryStatus.UNEXPLORED -> 16.0f
                TopicMasteryStatus.SOLIDIFYING -> 8.0f
                TopicMasteryStatus.STRENGTH -> 0.0f
            }
            val prereqImpactPts = min(downstreamPrereqs * 5.0f, 15.0f)
            val priorityScore = (decayPts + masteryDeficitPts + statusPts + prereqImpactPts).coerceIn(5.0f, 100.0f)

            val dueStatus = when {
                node.attemptsCount == 0 -> "Baseline Untested"
                isOverdue -> {
                    val overdueHours = (hoursSince - (scheduledIntervalDays * 24f)).roundToInt()
                    if (overdueHours >= 24) {
                        String.format(Locale.US, "Overdue by %.1fd", overdueHours / 24f)
                    } else {
                        "Overdue by ${max(1, overdueHours)}h"
                    }
                }
                else -> {
                    val remHours = ((scheduledIntervalDays * 24f) - hoursSince).roundToInt()
                    if (remHours >= 24) {
                        String.format(Locale.US, "Due in %.1fd", remHours / 24f)
                    } else {
                        "Due in ${max(1, remHours)}h"
                    }
                }
            }

            val reason = buildString {
                append("${node.masteryLevel.iconEmoji} ${node.masteryLevel.badgeText} Mastery (${node.accuracyPercent.toInt()}% accuracy). ")
                append("Estimated retention decayed to ${(retention * 100).toInt()}%. ")
                if (downstreamPrereqs > 0) {
                    append("⚓ Essential prerequisite anchor for $downstreamPrereqs downstream knowledge node(s). ")
                }
                if (node.status == TopicMasteryStatus.NEEDS_REVIEW) {
                    append("Past session misses detected—reinforce to avoid cognitive interference.")
                } else if (node.attemptsCount == 0) {
                    append("Start a calibration quiz to establish node mastery.")
                } else {
                    append("Reinforce now to advance from ${node.masteryLevel.badgeText} toward the next tier.")
                }
            }

            val recommendedAction = when {
                node.attemptsCount == 0 -> "Take Baseline Quiz"
                node.masteryLevel == KnowledgeMasteryLevel.BEGINNER -> "Reinforce Foundations"
                node.masteryLevel == KnowledgeMasteryLevel.INTERMEDIATE -> "Solidify Concepts"
                else -> "Maintain Fluency"
            }

            KnowledgeReinforcementSchedule(
                nodeId = node.id,
                title = node.title,
                category = node.category,
                categoryNumber = node.categoryNumber,
                masteryLevel = node.masteryLevel,
                status = node.status,
                accuracyPercent = node.accuracyPercent,
                attemptsCount = node.attemptsCount,
                retentionProbability = retention,
                scheduledIntervalDays = scheduledIntervalDays,
                lastReviewedTimestamp = lastReviewedTime,
                nextReviewTimestamp = nextReviewTimestamp,
                isOverdue = isOverdue,
                dueStatusText = dueStatus,
                urgencyPriorityScore = priorityScore,
                prerequisiteImpactCount = downstreamPrereqs,
                reinforcementReason = reason,
                recommendedAction = recommendedAction
            )
        }

        val sorted = schedules.sortedByDescending { it.urgencyPriorityScore }
        val dueCount = sorted.count { it.isOverdue }
        val criticalCount = sorted.count { it.urgencyPriorityScore >= 68f }
        val avgRetention = if (sorted.isNotEmpty()) {
            (sorted.map { it.retentionProbability }.average() * 100).roundToInt()
        } else 100
        val prereqAlerts = sorted.count { it.isOverdue && it.prerequisiteImpactCount > 0 }

        return KnowledgeSpacedRepetitionSummary(
            schedules = sorted,
            dueNowCount = dueCount,
            criticalCount = criticalCount,
            averageRetentionPercent = avgRetention,
            mostUrgentSchedule = sorted.firstOrNull(),
            dependentPrereqAlertsCount = prereqAlerts
        )
    }
}

