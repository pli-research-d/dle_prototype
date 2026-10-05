package com.example.dle_prototype.data.ml

import android.content.Context
import com.example.dle_prototype.data.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.min
import kotlin.system.measureTimeMillis

data class GeneratedMcqQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String,
    val keyConcept: String,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val tfliteScore: Float
) {
    fun toAppQuestion(): Question {
        return Question(
            type = "mcq",
            category = "AI Generated",
            difficulty = difficulty,
            question = question,
            options = options,
            answer = options[correctOptionIndex],
            explanation = explanation,
            example = "Core Concept: $keyConcept"
        )
    }
}

data class GeneratedQuiz(
    val topicTitle: String,
    val questions: List<GeneratedMcqQuestion>,
    val generationLatencyMs: Long,
    val modelSource: String = "On-Device TFLite (dle_model)",
    val sourceBulletCount: Int,
    val averageDifficulty: String
)

/**
 * Automated quiz generator that uses the on-device TFLite neural model
 * to analyze summarized learning material and craft balanced multiple-choice questions (MCQs).
 */
object OnDeviceQuizGeneratorEngine {

    // Common contrastive distractors for CS / programming / architecture concepts
    private val GENERIC_DISTRACTORS = listOf(
        "Directly allocates synchronous OS threads, creating significant memory overhead",
        "Couples presentation logic directly to persistence layers, preventing isolation",
        "Blocks the main application execution thread during asynchronous I/O operations",
        "Disables reactive state recomposition, requiring manual UI canvas invalidation",
        "Imposes linear search complexity O(n) across unindexed bucket memory",
        "Bypasses backpropagation gradients, freezing all intermediate neural weights",
        "Forces global state pollution by eliminating structured lifecycle containment",
        "Converts cold streams into uncontrolled hot emissions that leak resources"
    )

    /**
     * Generates multiple-choice questions from summarized bullet points and text.
     */
    suspend fun generateQuizFromSummary(
        context: Context?,
        summaryBullets: List<String>,
        keyConcepts: List<String>,
        rawText: String = "",
        maxQuestions: Int = 4
    ): GeneratedQuiz = withContext(Dispatchers.Default) {
        val latencyMs: Long
        val candidateQuestions = mutableListOf<GeneratedMcqQuestion>()

        latencyMs = measureTimeMillis {
            // Filter and clean bullet points
            val validBullets = summaryBullets
                .map { it.removePrefix("•").trim() }
                .filter { it.length > 20 }

            if (validBullets.isEmpty()) {
                return@measureTimeMillis
            }

            val model = OnDeviceTrainableModel()

            var questionCounter = 1
            for (idx in validBullets.indices) {
                if (candidateQuestions.size >= maxQuestions * 2) break
                val bullet = validBullets[idx]

                // Extract primary concept and predicate
                val (concept, predicate) = extractConceptAndPredicate(bullet, keyConcepts)

                // 1. Craft Question Patterns
                val candidateA = craftConceptDefinitionQuestion(
                    id = questionCounter++,
                    concept = concept,
                    predicate = predicate,
                    allBullets = validBullets,
                    bulletIndex = idx
                )

                val candidateB = if (validBullets.size > 2) {
                    craftIdentifyConceptQuestion(
                        id = questionCounter++,
                        concept = concept,
                        predicate = predicate,
                        allConcepts = keyConcepts
                    )
                } else null

                // 2. Score candidates with On-Device TFLite Model
                for (cand in listOfNotNull(candidateA, candidateB)) {
                    val features = extractQuestionFeatures(cand, rawText, validBullets.size)
                    val preds = model.forward(features)
                    // Composite comprehension & clarity score
                    val score = (preds[0] * 0.35f) + (preds[2] * 0.45f) + (preds[3] * 0.20f)
                    val difficulty = when {
                        score > 0.65f -> "Hard"
                        score > 0.38f -> "Medium"
                        else -> "Easy"
                    }

                    candidateQuestions.add(
                        cand.copy(
                            tfliteScore = score,
                            difficulty = difficulty
                        )
                    )
                }
            }
        }

        // Rank by TFLite model quality score descending and take requested count
        val finalQuestions = candidateQuestions
            .distinctBy { it.keyConcept }
            .sortedByDescending { it.tfliteScore }
            .take(maxQuestions.coerceAtLeast(2))

        val avgDiff = if (finalQuestions.isNotEmpty()) {
            val hardCount = finalQuestions.count { it.difficulty == "Hard" }
            val easyCount = finalQuestions.count { it.difficulty == "Easy" }
            when {
                hardCount >= finalQuestions.size / 2 -> "Hard"
                easyCount >= finalQuestions.size / 2 -> "Easy"
                else -> "Medium"
            }
        } else "Medium"

        val topic = keyConcepts.firstOrNull() ?: "Learning Material"

        GeneratedQuiz(
            topicTitle = topic,
            questions = finalQuestions,
            generationLatencyMs = latencyMs.coerceAtLeast(1L),
            modelSource = "On-Device TFLite (dle_model)",
            sourceBulletCount = summaryBullets.size,
            averageDifficulty = avgDiff
        )
    }

    /**
     * Extracts the primary subject concept and explanatory predicate from a bullet point.
     */
    fun extractConceptAndPredicate(bullet: String, keyConcepts: List<String>): Pair<String, String> {
        val clean = bullet.removeSuffix(".")

        // Match against known key concepts first
        for (concept in keyConcepts) {
            if (clean.contains(concept, ignoreCase = true)) {
                val words = clean.split(Regex("\\s+"))
                val predicate = words.filterNot { it.equals(concept, ignoreCase = true) }.joinToString(" ")
                return Pair(concept, predicate)
            }
        }

        // Look for common copulas: "is", "refers to", "enables", "provides"
        val splitPatterns = listOf(" is ", " refers to ", " enables ", " provides ", " represents ")
        for (pat in splitPatterns) {
            if (clean.contains(pat, ignoreCase = true)) {
                val parts = clean.split(Regex(pat, RegexOption.IGNORE_CASE), limit = 2)
                if (parts.size == 2 && parts[0].length < 40) {
                    val subject = parts[0].trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
                    val pred = parts[1].trim()
                    return Pair(subject, pred)
                }
            }
        }

        // Fallback: take first 2-3 words as concept
        val words = clean.split(Regex("\\s+"))
        val concept = words.take(2).joinToString(" ")
        val predicate = words.drop(2).joinToString(" ")
        return Pair(concept, predicate)
    }

    /**
     * Pattern 1: What is the primary role of [Concept]?
     */
    private fun craftConceptDefinitionQuestion(
        id: Int,
        concept: String,
        predicate: String,
        allBullets: List<String>,
        bulletIndex: Int
    ): GeneratedMcqQuestion {
        val cleanPred = predicate.removeSuffix(".").trim()
        val correctOption = cleanPred.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }

        // Generate 3 plausible distractors
        val distractors = mutableListOf<String>()

        // Distractor from other bullets
        for (i in allBullets.indices) {
            if (i != bulletIndex && distractors.size < 2) {
                val other = allBullets[i].removeSuffix(".").trim()
                if (other.length > 15 && !other.contains(concept, ignoreCase = true)) {
                    distractors.add(other.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() })
                }
            }
        }

        // Fill remaining with generic plausible contrastive distractors
        var distractorPoolIndex = (id * 3) % GENERIC_DISTRACTORS.size
        while (distractors.size < 3) {
            val dist = GENERIC_DISTRACTORS[distractorPoolIndex % GENERIC_DISTRACTORS.size]
            if (dist !in distractors) {
                distractors.add(dist)
            }
            distractorPoolIndex++
        }

        val allOptions = (distractors.take(3) + correctOption).shuffled()
        val correctIdx = allOptions.indexOf(correctOption)

        val questionStem = "According to the summarized material, what is the primary role or function of $concept?"
        val explanation = "$concept is characterized as: $cleanPred."

        return GeneratedMcqQuestion(
            id = id,
            question = questionStem,
            options = allOptions,
            correctOptionIndex = correctIdx,
            explanation = explanation,
            keyConcept = concept,
            difficulty = "Medium",
            tfliteScore = 0.5f
        )
    }

    /**
     * Pattern 2: Which concept is responsible for [Predicate]?
     */
    private fun craftIdentifyConceptQuestion(
        id: Int,
        concept: String,
        predicate: String,
        allConcepts: List<String>
    ): GeneratedMcqQuestion {
        val cleanPred = predicate.removeSuffix(".").trim()
        val correctOption = concept

        val otherConcepts = allConcepts.filterNot { it.equals(concept, ignoreCase = true) }
        val distractors = mutableListOf<String>()

        for (c in otherConcepts) {
            if (distractors.size < 3) {
                distractors.add(c)
            }
        }

        val fallbackConcepts = listOf("StateFlow", "LaunchedEffect", "CoroutineScope", "Backpropagation", "BinarySearch", "Dispatcher")
        for (fc in fallbackConcepts) {
            if (distractors.size < 3 && fc != concept && fc !in distractors) {
                distractors.add(fc)
            }
        }

        val allOptions = (distractors.take(3) + correctOption).shuffled()
        val correctIdx = allOptions.indexOf(correctOption)

        val questionStem = "Which core component or concept is specifically defined to $cleanPred?"
        val explanation = "$concept is the exact component defined in the learning text to $cleanPred."

        return GeneratedMcqQuestion(
            id = id,
            question = questionStem,
            options = allOptions,
            correctOptionIndex = correctIdx,
            explanation = explanation,
            keyConcept = concept,
            difficulty = "Medium",
            tfliteScore = 0.5f
        )
    }

    /**
     * Extracts 5 cognitive features to feed into the on-device TFLite model:
     * 1. Concept salience in source text
     * 2. Question length & grammar density
     * 3. Distractor diversity (lexical spread across options)
     * 4. Relational complexity (presence of architectural or causal terms)
     * 5. Explanation clarity
     */
    fun extractQuestionFeatures(
        q: GeneratedMcqQuestion,
        rawText: String,
        bulletCount: Int
    ): FloatArray {
        // Feature 1: Concept Salience
        val textLower = rawText.lowercase()
        val conceptLower = q.keyConcept.lowercase()
        val conceptOccurrences = if (conceptLower.isNotEmpty() && textLower.isNotEmpty()) {
            Regex(Regex.escape(conceptLower)).findAll(textLower).count()
        } else 1
        val salience = (conceptOccurrences.toFloat() / 5f).coerceIn(0.1f, 1.0f)

        // Feature 2: Question Length Score (ideal length is 12-25 words)
        val qWords = q.question.split(Regex("\\s+")).size
        val lengthScore = when {
            qWords in 10..24 -> 0.9f
            qWords in 6..9 -> 0.6f
            else -> 0.4f
        }

        // Feature 3: Distractor Diversity (average length variation across options)
        val optLengths = q.options.map { it.length }
        val avgLen = optLengths.average().toFloat().coerceAtLeast(1f)
        val variance = optLengths.map { kotlin.math.abs(it - avgLen) }.average().toFloat()
        val distractorDiversity = (1.0f - (variance / (avgLen * 2f))).coerceIn(0.2f, 1.0f)

        // Feature 4: Relational Complexity keywords in question
        val lowerQ = q.question.lowercase()
        var complexity = 0.3f
        if (lowerQ.contains("primary") || lowerQ.contains("specifically") || lowerQ.contains("defined")) complexity += 0.3f
        if (lowerQ.contains("function") || lowerQ.contains("role") || lowerQ.contains("component")) complexity += 0.3f
        val relationalComplexity = complexity.coerceIn(0.1f, 1.0f)

        // Feature 5: Explanation Density
        val explWords = q.explanation.split(Regex("\\s+")).size
        val explanationScore = (explWords.toFloat() / 20f).coerceIn(0.2f, 1.0f)

        return floatArrayOf(
            salience,
            lengthScore,
            distractorDiversity,
            relationalComplexity,
            explanationScore
        )
    }
}
