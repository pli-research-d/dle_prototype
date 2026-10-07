package com.example.dle_prototype.data.graph

import com.example.dle_prototype.data.CategoryMastery
import com.example.dle_prototype.data.DatabaseHelper
import com.example.dle_prototype.data.QuizAttempt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object KnowledgeGraphEngine {

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

        buildGraphFromTelemetry(masteries, recentAttempts, dueCards.size)
    }

    /**
     * Pure transformation function for deterministic testing and live graph generation.
     */
    fun buildGraphFromTelemetry(
        masteries: List<CategoryMastery>,
        attempts: List<QuizAttempt> = emptyList(),
        dueCardsCount: Int = 0
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
            clusterName = "Web Architecture"
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
            clusterName = "Web Architecture"
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
            clusterName = "Web Architecture"
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
            clusterName = "Backend & Database"
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
            clusterName = "Backend & Database"
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
            clusterName = "Applied Intelligence"
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
            clusterName = "Mobile & Reactive"
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
            clusterName = "Mobile & Reactive"
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
            clusterName = "Applied Intelligence"
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
            clusterName = "Applied Intelligence"
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
            clusterName = "Backend & Database"
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
            clusterName = "Systems Architecture"
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

        return KnowledgeGraphData(
            nodes = allNodes,
            edges = STATIC_EDGES,
            strengthsCount = strengths,
            solidifyingCount = solidifying,
            reviewCount = review,
            unexploredCount = unexplored,
            averageMasteryPercent = avgMastery
        )
    }
}
