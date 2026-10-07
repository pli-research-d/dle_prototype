package com.example.dle_prototype.data

import android.content.Context
import android.util.JsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object QuestionsRepository {

    private var cachedQuestions: List<Question>? = null

    val AVAILABLE_CATEGORIES = listOf(
        CategoryInfo(1, "HTML", "Web page structuring & elements", "🌐"),
        CategoryInfo(2, "CSS", "Styling, layouts & visual rules", "🎨"),
        CategoryInfo(3, "JavaScript", "Interactivity, events & logic", "⚡"),
        CategoryInfo(4, "PHP", "Server-side scripting & templates", "🐘"),
        CategoryInfo(5, "MySQL", "Relational data & query syntax", "🐬"),
        CategoryInfo(6, "Python", "Syntax, data structures & algorithms", "🐍"),
        CategoryInfo(7, "C Language", "Memory management, pointers, structs & systems programming", "⚙️")
    )

    data class CategoryInfo(
        val id: Int,
        val name: String,
        val description: String,
        val icon: String
    )

    suspend fun getQuestionsForCategory(
        context: Context,
        categoryName: String,
        count: Int = 10
    ): List<Question> = withContext(Dispatchers.IO) {
        val all = getAllQuestions(context)
        val filtered = all.filter { it.category.equals(categoryName.trim(), ignoreCase = true) }
        filtered.shuffled().take(count)
    }

    suspend fun getQuestionsByDifficulty(
        context: Context,
        categoryName: String
    ): Map<String, List<Question>> = withContext(Dispatchers.IO) {
        val all = getAllQuestions(context)
        val filtered = all.filter { it.category.equals(categoryName.trim(), ignoreCase = true) }
        filtered.groupBy { it.difficulty.trim().lowercase() }
    }

    suspend fun getAllQuestions(context: Context): List<Question> = withContext(Dispatchers.IO) {
        cachedQuestions?.let { return@withContext it }

        val list = mutableListOf<Question>()
        try {
            context.assets.open("questions.json").use { inputStream ->
                JsonReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                    reader.beginArray()
                    while (reader.hasNext()) {
                        parseQuestion(reader)?.let { list.add(it) }
                    }
                    reader.endArray()
                }
            }
            cachedQuestions = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    private fun parseQuestion(reader: JsonReader): Question? {
        var type = "true_false"
        var category = ""
        var difficulty = "Easy"
        var question = ""
        var explanation = ""
        var example = ""
        val options = mutableListOf<String>()
        var answer: Any = false

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "type" -> type = reader.nextString()
                "category" -> category = reader.nextString()
                "difficulty" -> difficulty = reader.nextString()
                "question" -> question = reader.nextString()
                "explanation" -> explanation = reader.nextString()
                "example" -> example = reader.nextString()
                "options" -> {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        options.add(reader.nextString())
                    }
                    reader.endArray()
                }
                "answer" -> {
                    if (type == "mcq") {
                        answer = reader.nextString()
                    } else {
                        answer = reader.nextBoolean()
                    }
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        if (question.isBlank()) return null
        return Question(
            type = type,
            category = category,
            difficulty = difficulty,
            question = question,
            options = options,
            answer = answer,
            explanation = explanation,
            example = example
        )
    }

    fun difficultyToFloat(difficulty: String): Float {
        return when (difficulty.trim().lowercase()) {
            "easy" -> 1.0f
            "medium" -> 2.0f
            "hard" -> 3.0f
            else -> difficulty.toFloatOrNull() ?: 1.0f
        }
    }
}
