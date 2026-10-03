package com.example.dle_prototype.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FeedCardsRepository {

    suspend fun generateFeedCards(
        context: Context,
        topics: List<String>,
        dbHelper: DatabaseHelper,
        username: String,
        batchSize: Int = 12
    ): List<FeedCard> = withContext(Dispatchers.IO) {
        val list = mutableListOf<FeedCard>()
        val dueCards = dbHelper.getDueFlashcards(username).take(2)

        // 1. Convert any due flashcards into REVIEW_CARD
        dueCards.forEach { fc ->
            list.add(
                FeedCard(
                    id = "review_${fc.id}_${System.currentTimeMillis()}",
                    type = FeedCardType.REVIEW_CARD,
                    category = fc.category,
                    title = "Leitner Review · Box ${fc.boxLevel}",
                    prompt = fc.question,
                    correctAnswer = fc.correctAnswer,
                    explanation = fc.explanation,
                    xpReward = 15,
                    fuelReward = 4
                )
            )
        }

        // 2. Add rich built-in interactive cards
        val pool = getCuratedFeedPool(topics)
        list.addAll(pool.shuffled().take(batchSize - list.size))

        // Ensure we meet the batch size
        if (list.size < batchSize) {
            val filler = getCuratedFeedPool(listOf("HTML", "CSS", "JavaScript", "Python"))
            list.addAll(filler.shuffled().take(batchSize - list.size))
        }

        list.shuffled()
    }

    private fun getCuratedFeedPool(preferredTopics: List<String>): List<FeedCard> {
        val all = listOf(
            // SPOT THE BUG CARDS
            FeedCard(
                id = "bug_js_1",
                type = FeedCardType.SPOT_THE_BUG,
                category = "JavaScript",
                title = "Spot the Bug 🐛",
                prompt = "Tap the line that causes a TypeError at runtime:",
                codeSnippet = """
1: const user = { name: "Alex", profile: null };
2: const getCity = (u) => {
3:   return u.profile.address.city;
4: };
5: console.log(getCity(user));
                """.trimIndent(),
                bugLineIndex = 2, // line 3 (0-indexed 2)
                bugLines = listOf(
                    "const user = { name: \"Alex\", profile: null };",
                    "const getCity = (u) => {",
                    "  return u.profile.address.city;",
                    "};",
                    "console.log(getCity(user));"
                ),
                correctAnswer = "return u.profile.address.city;",
                explanation = "Line 3 attempts to access 'address' on 'u.profile' which is null, throwing a TypeError. Optional chaining (?.) should be used: u.profile?.address?.city.",
                xpReward = 18,
                fuelReward = 5
            ),
            FeedCard(
                id = "bug_py_1",
                type = FeedCardType.SPOT_THE_BUG,
                category = "Python",
                title = "Spot the Bug 🐛",
                prompt = "Tap the line containing a mutation bug with default arguments:",
                codeSnippet = """
1: def append_item(val, target_list=[]):
2:     target_list.append(val)
3:     return target_list
4: print(append_item(1))
5: print(append_item(2))
                """.trimIndent(),
                bugLineIndex = 0,
                bugLines = listOf(
                    "def append_item(val, target_list=[]):",
                    "    target_list.append(val)",
                    "    return target_list",
                    "print(append_item(1))",
                    "print(append_item(2))"
                ),
                correctAnswer = "def append_item(val, target_list=[]):",
                explanation = "Default list argument is evaluated once at function definition time. Use target_list=None and initialize inside the function.",
                xpReward = 18,
                fuelReward = 5
            ),
            FeedCard(
                id = "bug_sql_1",
                type = FeedCardType.SPOT_THE_BUG,
                category = "MySQL",
                title = "Spot the Bug 🐛",
                prompt = "Tap the faulty clause in this aggregation query:",
                codeSnippet = """
1: SELECT dept, COUNT(*) as emp_count
2: FROM employees
3: WHERE COUNT(*) > 5
4: GROUP BY dept
5: ORDER BY emp_count DESC;
                """.trimIndent(),
                bugLineIndex = 2,
                bugLines = listOf(
                    "SELECT dept, COUNT(*) as emp_count",
                    "FROM employees",
                    "WHERE COUNT(*) > 5",
                    "GROUP BY dept",
                    "ORDER BY emp_count DESC;"
                ),
                correctAnswer = "WHERE COUNT(*) > 5",
                explanation = "Aggregate functions cannot be used in a WHERE clause! Use HAVING COUNT(*) > 5 after GROUP BY.",
                xpReward = 18,
                fuelReward = 5
            ),

            // WHAT'S THE OUTPUT CARDS
            FeedCard(
                id = "output_js_1",
                type = FeedCardType.WHATS_THE_OUTPUT,
                category = "JavaScript",
                title = "What's the Output? 💻",
                prompt = "What does this console output in modern V8?",
                codeSnippet = """
console.log(typeof NaN);
console.log(NaN === NaN);
                """.trimIndent(),
                options = listOf(
                    "\"number\" and false",
                    "\"undefined\" and false",
                    "\"NaN\" and true",
                    "\"number\" and true"
                ),
                correctAnswer = "\"number\" and false",
                explanation = "NaN is technically of type 'number' in IEEE 754 float specs, and NaN is unique in that it never equals itself (NaN === NaN is false).",
                xpReward = 14,
                fuelReward = 4
            ),
            FeedCard(
                id = "output_py_1",
                type = FeedCardType.WHATS_THE_OUTPUT,
                category = "Python",
                title = "What's the Output? 🐍",
                prompt = "What will this print?",
                codeSnippet = """
a = [1, 2, 3]
b = a
b += [4]
print(a)
                """.trimIndent(),
                options = listOf(
                    "[1, 2, 3, 4]",
                    "[1, 2, 3]",
                    "TypeError",
                    "[1, 2, 3, [4]]"
                ),
                correctAnswer = "[1, 2, 3, 4]",
                explanation = "+= on lists mutates the original list in-place (calling extend), so 'a' reflects the mutation since 'b' references the same object.",
                xpReward = 14,
                fuelReward = 4
            ),
            FeedCard(
                id = "output_css_1",
                type = FeedCardType.WHATS_THE_OUTPUT,
                category = "CSS",
                title = "What's the Output? 🎨",
                prompt = "If an element has width: 200px, padding: 20px, and box-sizing: border-box, what is its total rendered width?",
                codeSnippet = """
.card {
  width: 200px;
  padding: 20px;
  box-sizing: border-box;
}
                """.trimIndent(),
                options = listOf(
                    "200px",
                    "240px",
                    "160px",
                    "220px"
                ),
                correctAnswer = "200px",
                explanation = "With 'border-box', padding and border are included within the declared width, so the element remains exactly 200px wide.",
                xpReward = 12,
                fuelReward = 3
            ),

            // FILL IN THE BLANK
            FeedCard(
                id = "fill_html_1",
                type = FeedCardType.FILL_BLANK,
                category = "HTML",
                title = "Fill in the Blank 🧩",
                prompt = "Which attribute connects an <input> with a <datalist> dropdown?",
                codeSnippet = """
<input type="text" ___="browsers">
<datalist id="browsers">
  <option value="Chrome">
</datalist>
                """.trimIndent(),
                options = listOf("list", "data", "options", "for"),
                correctAnswer = "list",
                explanation = "The 'list' attribute on the <input> must match the 'id' attribute of the <datalist> to activate browser autocomplete suggestions.",
                xpReward = 12,
                fuelReward = 3
            ),
            FeedCard(
                id = "fill_sql_1",
                type = FeedCardType.FILL_BLANK,
                category = "MySQL",
                title = "Fill in the Blank 🐬",
                prompt = "Complete the query to return only records where salary is not missing:",
                codeSnippet = """
SELECT * FROM engineers
WHERE salary IS ___ NULL;
                """.trimIndent(),
                options = listOf("NOT", "!", "NEVER", "NON"),
                correctAnswer = "NOT",
                explanation = "In SQL, NULL checks must always use IS NULL or IS NOT NULL because comparison operators like != return UNKNOWN on nulls.",
                xpReward = 12,
                fuelReward = 3
            ),

            // 15-SEC MICRO EXPLAINERS (micro-lesson, no scoring penalty)
            FeedCard(
                id = "explainer_1",
                type = FeedCardType.EXPLAINER_15S,
                category = "JavaScript",
                title = "15-Sec Micro Concept 💡",
                prompt = "Event Loop Microtasks vs Macrotasks",
                codeSnippet = """
console.log('1');
setTimeout(() => console.log('2'), 0);
Promise.resolve().then(() => console.log('3'));
console.log('4');
// Output: 1, 4, 3, 2
                """.trimIndent(),
                correctAnswer = "Understood",
                options = listOf("Got it!"),
                explanation = "Synchronous code runs first (1, 4). The microtask queue (Promise callbacks) empties completely before the next macrotask (setTimeout) runs.",
                xpReward = 10,
                fuelReward = 3
            ),
            FeedCard(
                id = "explainer_2",
                type = FeedCardType.EXPLAINER_15S,
                category = "CSS",
                title = "15-Sec Micro Concept 🎨",
                prompt = "CSS Stacking Context Triggers",
                codeSnippet = """
/* These trigger a new stacking context: */
opacity: 0.99;
transform: translateZ(0);
filter: blur(0px);
isolation: isolate;
                """.trimIndent(),
                correctAnswer = "Understood",
                options = listOf("Got it!"),
                explanation = "Changing opacity, transform, filter, or using 'isolation: isolate' creates a new local stacking context, trapping z-index within that subtree.",
                xpReward = 10,
                fuelReward = 3
            ),

            // MYSTERY CARDS (Rare, high stakes, bonus rewards!)
            FeedCard(
                id = "mystery_1",
                type = FeedCardType.MYSTERY_CARD,
                category = "Python",
                title = "✨ Mystery Card · High Stakes!",
                prompt = "What is the time complexity of searching a key in a Python dict on average vs worst case?",
                options = listOf(
                    "Average: O(1) · Worst: O(n)",
                    "Average: O(log n) · Worst: O(n)",
                    "Average: O(1) · Worst: O(1)",
                    "Average: O(n) · Worst: O(n^2)"
                ),
                correctAnswer = "Average: O(1) · Worst: O(n)",
                explanation = "Python hash tables provide average O(1) lookups via hash indexing. In worst case hash collision storms, it degrades to O(n).",
                xpReward = 35,
                fuelReward = 15,
                isMystery = true
            ),

            // MCQ STANDARD
            FeedCard(
                id = "mcq_php_1",
                type = FeedCardType.MCQ,
                category = "PHP",
                title = "PHP Mechanics 🐘",
                prompt = "In PHP 8+, what does the nullsafe operator (?->) do when evaluating null?",
                options = listOf(
                    "Short-circuits and returns null without throwing an Error",
                    "Throws a NullPointerException",
                    "Converts null to an empty string",
                    "Causes a fatal parse error"
                ),
                correctAnswer = "Short-circuits and returns null without throwing an Error",
                explanation = "The ?-> operator checks the target; if null, execution of the rest of the chain stops and immediately yields null.",
                xpReward = 12,
                fuelReward = 3
            )
        )

        val filtered = all.filter { card ->
            preferredTopics.any { it.equals(card.category, ignoreCase = true) }
        }
        return if (filtered.size >= 5) filtered else all
    }
}
