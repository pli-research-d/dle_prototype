package com.example.dle_prototype.data.graph

import android.content.Context
import com.example.dle_prototype.data.Question
import com.example.dle_prototype.data.QuestionsRepository

object KnowledgeGraphQuestionBank {

    val CURATED_QUESTIONS: List<KnowledgeGraphQuizQuestion> = listOf(
        // ==========================================
        // 1. HTML5 FOUNDATIONS (html)
        // ==========================================
        KnowledgeGraphQuizQuestion(
            id = "kg_html_found_1",
            nodeId = "html",
            nodeTitle = "HTML5 Foundations",
            conceptTag = "Semantic Elements",
            question = "Which HTML5 element represents the primary central content of the document body?",
            options = listOf("<main>", "<section>", "<div>", "<article>"),
            correctAnswer = "<main>",
            type = "mcq",
            explanation = "The <main> element specifies the main, unique content of a document, crucial for screen readers and SEO.",
            difficultyTier = KnowledgeQuizDifficultyTier.FOUNDATIONAL,
            intrinsicComplexity = 1.2f
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_html_found_2",
            nodeId = "html",
            nodeTitle = "HTML5 Foundations",
            conceptTag = "DOM Structure",
            question = "The 'alt' attribute on an <img> tag is required for WCAG accessibility compliance.",
            options = listOf("True", "False"),
            correctAnswer = "True",
            type = "true_false",
            explanation = "Alternative text conveys the image meaning to screen readers or when image bandwidth fails.",
            difficultyTier = KnowledgeQuizDifficultyTier.FOUNDATIONAL,
            intrinsicComplexity = 1.3f
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_html_core_1",
            nodeId = "html",
            nodeTitle = "HTML5 Foundations",
            conceptTag = "ARIA & Accessibility",
            question = "What is the purpose of 'aria-live=\"polite\"' on an asynchronous UI container?",
            options = listOf(
                "Announces dynamic DOM mutations when user is idle without interrupting speech",
                "Immediately interrupts screen reader speech with highest priority",
                "Forces synchronous blocking of the JavaScript main thread",
                "Applies smooth CSS cross-fade transitions to children"
            ),
            correctAnswer = "Announces dynamic DOM mutations when user is idle without interrupting speech",
            type = "mcq",
            explanation = "'aria-live=\"polite\"' waits until the current speech queue is clear before speaking updates.",
            difficultyTier = KnowledgeQuizDifficultyTier.CORE_APPLICATION,
            intrinsicComplexity = 2.1f
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_html_expert_1",
            nodeId = "html",
            nodeTitle = "HTML5 Foundations",
            conceptTag = "Critical Rendering Path",
            question = "How does 'defer' differ from 'async' when loading external <script> tags during HTML parsing?",
            options = listOf(
                "defer executes scripts in document order strictly after DOM parsing finishes but before DOMContentLoaded",
                "defer executes scripts immediately upon network arrival, interrupting HTML tokenization",
                "async guarantees deterministic sequential script execution order",
                "defer blocks layout calculation while async bypasses rendering altogether"
            ),
            correctAnswer = "defer executes scripts in document order strictly after DOM parsing finishes but before DOMContentLoaded",
            type = "mcq",
            explanation = "'defer' scripts preserve DOM execution order and never block HTML tokenization.",
            difficultyTier = KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE,
            intrinsicComplexity = 3.1f,
            isCrossNodeSynthesis = true,
            connectedNodeIds = listOf("js")
        ),

        // ==========================================
        // 2. CSS MODERN LAYOUTS (css)
        // ==========================================
        KnowledgeGraphQuizQuestion(
            id = "kg_css_found_1",
            nodeId = "css",
            nodeTitle = "CSS Modern Layouts",
            conceptTag = "Box Model",
            question = "In CSS Box Model, what does 'box-sizing: border-box' include within the declared element width?",
            options = listOf("Content, padding, and border", "Only content width", "Content and margin", "Border and margin only"),
            correctAnswer = "Content, padding, and border",
            type = "mcq",
            explanation = "'border-box' includes padding and border within the specified width, preventing layout overflow bugs.",
            difficultyTier = KnowledgeQuizDifficultyTier.FOUNDATIONAL,
            intrinsicComplexity = 1.2f
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_css_core_1",
            nodeId = "css",
            nodeTitle = "CSS Modern Layouts",
            conceptTag = "Flexbox Alignment",
            question = "In a column Flexbox container ('flex-direction: column'), which property controls horizontal alignment?",
            options = listOf("align-items", "justify-content", "align-content", "flex-grow"),
            correctAnswer = "align-items",
            type = "mcq",
            explanation = "In column direction, the cross axis is horizontal, controlled by 'align-items'.",
            difficultyTier = KnowledgeQuizDifficultyTier.CORE_APPLICATION,
            intrinsicComplexity = 2.2f
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_css_expert_1",
            nodeId = "css",
            nodeTitle = "CSS Modern Layouts",
            conceptTag = "Compositor Layers",
            question = "Which CSS property promotion triggers hardware GPU accelerated compositing without layout reflow?",
            options = listOf(
                "transform: translate3d(x,y,z) and opacity",
                "top, left, and position: absolute",
                "margin-top and margin-left transitions",
                "width and height animations"
            ),
            correctAnswer = "transform: translate3d(x,y,z) and opacity",
            type = "mcq",
            explanation = "Transforms and opacity bypass Layout and Paint phases directly to GPU Compositing, maintaining 60/120fps.",
            difficultyTier = KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE,
            intrinsicComplexity = 3.0f,
            isCrossNodeSynthesis = true,
            connectedNodeIds = listOf("html", "js")
        ),

        // ==========================================
        // 3. JAVASCRIPT & ES6+ (js)
        // ==========================================
        KnowledgeGraphQuizQuestion(
            id = "kg_js_found_1",
            nodeId = "js",
            nodeTitle = "JavaScript & ES6+",
            conceptTag = "Equality & Types",
            question = "In JavaScript, '===' (strict equality) checks value without type coercion.",
            options = listOf("True", "False"),
            correctAnswer = "True",
            type = "true_false",
            explanation = "Strict equality requires identical type and value, preventing subtle type coercion bugs.",
            difficultyTier = KnowledgeQuizDifficultyTier.FOUNDATIONAL,
            intrinsicComplexity = 1.1f,
            prerequisiteNodeId = "html"
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_js_core_1",
            nodeId = "js",
            nodeTitle = "JavaScript & ES6+",
            conceptTag = "Closures & Scope",
            question = "What is a JavaScript closure?",
            options = listOf(
                "A function bundled with references to its surrounding lexical environment",
                "A function that returns undefined upon termination",
                "An anonymous callback passed into setTimeout",
                "A method declared with the static keyword on an ES6 class"
            ),
            correctAnswer = "A function bundled with references to its surrounding lexical environment",
            type = "mcq",
            explanation = "Closures retain access to outer function variables even after the outer function has finished executing.",
            difficultyTier = KnowledgeQuizDifficultyTier.CORE_APPLICATION,
            intrinsicComplexity = 2.3f,
            prerequisiteNodeId = "html"
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_js_expert_1",
            nodeId = "js",
            nodeTitle = "JavaScript & ES6+",
            conceptTag = "Event Loop Microtasks",
            question = "In what order are Promise microtasks executed relative to setTimeout macrotasks in the V8 Event Loop?",
            options = listOf(
                "All microtasks in the microtask queue drain completely before the next macrotask is processed",
                "Macrotasks and microtasks alternate 1-to-1 in round-robin order",
                "setTimeout runs before Promise.resolve().then()",
                "Microtasks are handled on background threads while macrotasks block"
            ),
            correctAnswer = "All microtasks in the microtask queue drain completely before the next macrotask is processed",
            type = "mcq",
            explanation = "The microtask queue (Promises, queueMicrotask) empties completely before the event loop advances to the next macrotask.",
            difficultyTier = KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE,
            intrinsicComplexity = 3.2f,
            prerequisiteNodeId = "html",
            isCrossNodeSynthesis = true,
            connectedNodeIds = listOf("php", "coroutines")
        ),

        // ==========================================
        // 4. PHP SERVER RUNTIME (php)
        // ==========================================
        KnowledgeGraphQuizQuestion(
            id = "kg_php_found_1",
            nodeId = "php",
            nodeTitle = "PHP Server Runtime",
            conceptTag = "Superglobals",
            question = "Which PHP superglobal contains query parameters sent in an HTTP GET request URL?",
            options = listOf("\$_GET", "\$_POST", "\$_REQUEST", "\$_SERVER"),
            correctAnswer = "\$_GET",
            type = "mcq",
            explanation = "\$_GET is an associative array of variables passed via URL parameters (query string).",
            difficultyTier = KnowledgeQuizDifficultyTier.FOUNDATIONAL,
            intrinsicComplexity = 1.3f,
            prerequisiteNodeId = "js"
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_php_core_1",
            nodeId = "php",
            nodeTitle = "PHP Server Runtime",
            conceptTag = "PDO Prepared Statements",
            question = "Why are PDO prepared statements the industry standard defense against SQL injection?",
            options = listOf(
                "SQL syntax is pre-compiled separately from data parameters, preventing executable injection",
                "They automatically encrypt database records at rest in MySQL",
                "They strip all HTML and JavaScript tags from input strings",
                "They enforce client-side validation before the network request leaves"
            ),
            correctAnswer = "SQL syntax is pre-compiled separately from data parameters, preventing executable injection",
            type = "mcq",
            explanation = "Prepared statements treat parameters purely as literal values rather than executable SQL fragments.",
            difficultyTier = KnowledgeQuizDifficultyTier.CORE_APPLICATION,
            intrinsicComplexity = 2.4f,
            prerequisiteNodeId = "js",
            connectedNodeIds = listOf("mysql")
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_php_expert_1",
            nodeId = "php",
            nodeTitle = "PHP Server Runtime",
            conceptTag = "Opcache & Memory Architecture",
            question = "What performance benefit does OPcache provide in high-throughput PHP server environments?",
            options = listOf(
                "Stores precompiled script bytecode in shared memory, eliminating repetitive parsing on every request",
                "Converts PHP scripts into binary C executables on disk",
                "Automatically clusters multiple Apache web servers together",
                "Acts as an in-memory Redis cache for database results"
            ),
            correctAnswer = "Stores precompiled script bytecode in shared memory, eliminating repetitive parsing on every request",
            type = "mcq",
            explanation = "OPcache prevents CPU overhead by reusing precompiled opcodes in shared memory across requests.",
            difficultyTier = KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE,
            intrinsicComplexity = 3.1f,
            prerequisiteNodeId = "js",
            connectedNodeIds = listOf("mysql")
        ),

        // ==========================================
        // 5. MYSQL RELATIONAL DATA (mysql)
        // ==========================================
        KnowledgeGraphQuizQuestion(
            id = "kg_mysql_found_1",
            nodeId = "mysql",
            nodeTitle = "MySQL Relational Data",
            conceptTag = "Primary Keys",
            question = "A PRIMARY KEY constraint in MySQL guarantees both UNIQUE values and NOT NULL enforcement.",
            options = listOf("True", "False"),
            correctAnswer = "True",
            type = "true_false",
            explanation = "Primary keys uniquely identify rows in a relational table and cannot contain NULL values.",
            difficultyTier = KnowledgeQuizDifficultyTier.FOUNDATIONAL,
            intrinsicComplexity = 1.2f,
            prerequisiteNodeId = "php"
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_mysql_core_1",
            nodeId = "mysql",
            nodeTitle = "MySQL Relational Data",
            conceptTag = "JOIN Operations",
            question = "What is the result of an 'INNER JOIN' between two tables?",
            options = listOf(
                "Returns only rows that have matching values in both joined tables",
                "Returns all rows from the left table and matched rows from the right",
                "Returns all rows from both tables, filling unmatched columns with NULL",
                "Performs a Cartesian product multiplication of all rows"
            ),
            correctAnswer = "Returns only rows that have matching values in both joined tables",
            type = "mcq",
            explanation = "INNER JOIN filters out rows that do not satisfy the join predicate in both tables.",
            difficultyTier = KnowledgeQuizDifficultyTier.CORE_APPLICATION,
            intrinsicComplexity = 2.2f,
            prerequisiteNodeId = "php"
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_mysql_expert_1",
            nodeId = "mysql",
            nodeTitle = "MySQL Relational Data",
            conceptTag = "B-Tree Indexing & ACID",
            question = "Why does an InnoDB composite index (A, B) fail to accelerate a query filtered exclusively on WHERE B = 10?",
            options = listOf(
                "Violates the leftmost prefix rule: B-Tree traversal requires starting with the root indexed column A",
                "InnoDB only supports hash indexing, not B-Tree ordering",
                "Column B must always be declared as VARCHAR rather than INT",
                "Composite indexes cannot be used in WHERE clauses"
            ),
            correctAnswer = "Violates the leftmost prefix rule: B-Tree traversal requires starting with the root indexed column A",
            type = "mcq",
            explanation = "B-Trees are sorted hierarchically by leading columns. Without leading column A, an index seek is impossible.",
            difficultyTier = KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE,
            intrinsicComplexity = 3.3f,
            prerequisiteNodeId = "php",
            isCrossNodeSynthesis = true,
            connectedNodeIds = listOf("big_o")
        ),

        // ==========================================
        // 6. PYTHON COMPUTATIONAL (python)
        // ==========================================
        KnowledgeGraphQuizQuestion(
            id = "kg_py_found_1",
            nodeId = "python",
            nodeTitle = "Python Computational",
            conceptTag = "Data Structures",
            question = "Which Python data structure is immutable once created?",
            options = listOf("tuple", "list", "dict", "set"),
            correctAnswer = "tuple",
            type = "mcq",
            explanation = "Tuples are immutable sequences in Python; elements cannot be modified or appended after instantiation.",
            difficultyTier = KnowledgeQuizDifficultyTier.FOUNDATIONAL,
            intrinsicComplexity = 1.2f
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_py_core_1",
            nodeId = "python",
            nodeTitle = "Python Computational",
            conceptTag = "Generators & Yield",
            question = "What memory advantage do Python generators ('yield') have over list comprehensions for huge datasets?",
            options = listOf(
                "Lazy evaluation: yields items one by one on demand (O(1) memory) without materializing full dataset",
                "Generators automatically execute in parallel on all CPU cores",
                "Generators bypass Python's garbage collection system completely",
                "Generators store items in compiled C arrays"
            ),
            correctAnswer = "Lazy evaluation: yields items one by one on demand (O(1) memory) without materializing full dataset",
            type = "mcq",
            explanation = "Generators evaluate lazily, maintaining minimal memory footprint regardless of dataset scale.",
            difficultyTier = KnowledgeQuizDifficultyTier.CORE_APPLICATION,
            intrinsicComplexity = 2.3f,
            connectedNodeIds = listOf("big_o")
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_py_expert_1",
            nodeId = "python",
            nodeTitle = "Python Computational",
            conceptTag = "GIL & Threading",
            question = "Why does Python's Global Interpreter Lock (GIL) prevent multi-threaded CPU-bound speedups in CPython?",
            options = listOf(
                "A single mutex lock ensures only one native OS thread executes Python bytecode at any moment",
                "Python does not support OS native threads, only cooperative greenlets",
                "The GIL locks network sockets during I/O wait",
                "CPython compiles bytecode strictly to single-core WebAssembly"
            ),
            correctAnswer = "A single mutex lock ensures only one native OS thread executes Python bytecode at any moment",
            type = "mcq",
            explanation = "The GIL enforces single-thread bytecode execution in CPython; CPU-bound tasks require multiprocessing.",
            difficultyTier = KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE,
            intrinsicComplexity = 3.2f,
            isCrossNodeSynthesis = true,
            connectedNodeIds = listOf("tflite", "big_o")
        ),

        // ==========================================
        // 7. JETPACK COMPOSE UI (compose)
        // ==========================================
        KnowledgeGraphQuizQuestion(
            id = "kg_cmp_found_1",
            nodeId = "compose",
            nodeTitle = "Jetpack Compose UI",
            conceptTag = "Declarative UI",
            question = "In Jetpack Compose, UI is described as functions of state rather than mutating XML view hierarchies.",
            options = listOf("True", "False"),
            correctAnswer = "True",
            type = "true_false",
            explanation = "Compose is a declarative UI toolkit where UI automatically updates whenever observed state changes.",
            difficultyTier = KnowledgeQuizDifficultyTier.FOUNDATIONAL,
            intrinsicComplexity = 1.3f
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_cmp_core_1",
            nodeId = "compose",
            nodeTitle = "Jetpack Compose UI",
            conceptTag = "State & Recomposition",
            question = "What happens if state is updated inside a Composable without using 'remember' or ViewModel?",
            options = listOf(
                "The state resets to its initial value on every recomposition",
                "The app crashes with an IllegalStateException",
                "Compose freezes the main thread",
                "The state is permanently saved to SharedPreferences"
            ),
            correctAnswer = "The state resets to its initial value on every recomposition",
            type = "mcq",
            explanation = "'remember' preserves values across recomposition passes; without it, initialization re-runs every time.",
            difficultyTier = KnowledgeQuizDifficultyTier.CORE_APPLICATION,
            intrinsicComplexity = 2.4f,
            connectedNodeIds = listOf("coroutines")
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_cmp_expert_1",
            nodeId = "compose",
            nodeTitle = "Jetpack Compose UI",
            conceptTag = "Recomposition Optimization",
            question = "When should 'derivedStateOf' be preferred over raw state calculation in Jetpack Compose?",
            options = listOf(
                "When state changes rapidly (e.g. scroll offset) but calculation only triggers recomposition when a boundary is crossed",
                "Whenever creating any mutableStateOf variable in a ViewModel",
                "To automatically persist state to SQLite local database",
                "To execute blocking disk I/O on background thread"
            ),
            correctAnswer = "When state changes rapidly (e.g. scroll offset) but calculation only triggers recomposition when a boundary is crossed",
            type = "mcq",
            explanation = "'derivedStateOf' buffers high-frequency state updates, eliminating unnecessary recompositions.",
            difficultyTier = KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE,
            intrinsicComplexity = 3.3f,
            isCrossNodeSynthesis = true,
            connectedNodeIds = listOf("coroutines", "tflite")
        ),

        // ==========================================
        // 8. KOTLIN COROUTINES & FLOW (coroutines)
        // ==========================================
        KnowledgeGraphQuizQuestion(
            id = "kg_kt_found_1",
            nodeId = "coroutines",
            nodeTitle = "Kotlin Coroutines & Flow",
            conceptTag = "Suspend Functions",
            question = "A 'suspend' function can pause execution without blocking the underlying OS thread.",
            options = listOf("True", "False"),
            correctAnswer = "True",
            type = "true_false",
            explanation = "Coroutines suspend via state-machine continuation passing, releasing the worker thread for other tasks.",
            difficultyTier = KnowledgeQuizDifficultyTier.FOUNDATIONAL,
            intrinsicComplexity = 1.3f,
            prerequisiteNodeId = "compose"
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_kt_core_1",
            nodeId = "coroutines",
            nodeTitle = "Kotlin Coroutines & Flow",
            conceptTag = "Dispatchers",
            question = "Which CoroutineDispatcher is tailored for disk database queries and network calls in Android?",
            options = listOf("Dispatchers.IO", "Dispatchers.Main", "Dispatchers.Default", "Dispatchers.Unconfined"),
            correctAnswer = "Dispatchers.IO",
            type = "mcq",
            explanation = "Dispatchers.IO is backed by a shared pool of on-demand threads optimized for blocking I/O operations.",
            difficultyTier = KnowledgeQuizDifficultyTier.CORE_APPLICATION,
            intrinsicComplexity = 2.3f,
            prerequisiteNodeId = "compose"
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_kt_expert_1",
            nodeId = "coroutines",
            nodeTitle = "Kotlin Coroutines & Flow",
            conceptTag = "Structured Concurrency",
            question = "What occurs when a child coroutine fails with an unhandled exception inside a standard 'coroutineScope'?",
            options = listOf(
                "Cancels the parent scope and all sibling coroutines, propagating the failure up the hierarchy",
                "Only the failing child terminates; siblings continue unaffected without notifying parent",
                "Automatically restarts the child up to 3 times",
                "The exception is swallowed silently"
            ),
            correctAnswer = "Cancels the parent scope and all sibling coroutines, propagating the failure up the hierarchy",
            type = "mcq",
            explanation = "Structured concurrency ensures scopes cancel all children upon failure, preventing leaked background jobs.",
            difficultyTier = KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE,
            intrinsicComplexity = 3.3f,
            prerequisiteNodeId = "compose",
            isCrossNodeSynthesis = true,
            connectedNodeIds = listOf("spaced_rep")
        ),

        // ==========================================
        // 9. ON-DEVICE TFLITE & AI (tflite)
        // ==========================================
        KnowledgeGraphQuizQuestion(
            id = "kg_tf_found_1",
            nodeId = "tflite",
            nodeTitle = "On-Device TFLite & AI",
            conceptTag = "Edge AI Inference",
            question = "On-device AI inference executes locally on the user's phone hardware without sending telemetry to cloud servers.",
            options = listOf("True", "False"),
            correctAnswer = "True",
            type = "true_false",
            explanation = "TFLite executes on edge hardware, ensuring zero-latency offline performance and privacy.",
            difficultyTier = KnowledgeQuizDifficultyTier.FOUNDATIONAL,
            intrinsicComplexity = 1.4f,
            prerequisiteNodeId = "compose"
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_tf_core_1",
            nodeId = "tflite",
            nodeTitle = "On-Device TFLite & AI",
            conceptTag = "Model Quantization",
            question = "What is the primary benefit of 8-bit integer (INT8) model quantization in edge mobile devices?",
            options = listOf(
                "Reduces model file size by 75% and accelerates execution on mobile NPU/DSP hardware with minimal accuracy loss",
                "Increases floating-point accuracy beyond double precision",
                "Enables cloud streaming of model weights",
                "Eliminates the need for any input normalization"
            ),
            correctAnswer = "Reduces model file size by 75% and accelerates execution on mobile NPU/DSP hardware with minimal accuracy loss",
            type = "mcq",
            explanation = "INT8 quantization maps 32-bit floats to 8-bit integers, drastically reducing memory footprint and battery draw.",
            difficultyTier = KnowledgeQuizDifficultyTier.CORE_APPLICATION,
            intrinsicComplexity = 2.5f,
            prerequisiteNodeId = "python"
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_tf_expert_1",
            nodeId = "tflite",
            nodeTitle = "On-Device TFLite & AI",
            conceptTag = "ZPD Neural Convergence",
            question = "How does real-time edge telemetry tuning adjust the Zone of Proximal Development (ZPD) in adaptive learning?",
            options = listOf(
                "Calibrates challenge complexity to keep user success rate within the optimal 75%-85% flow state band",
                "Maximizes difficulty until the user scores below 30%",
                "Locks difficulty at the easiest tier forever",
                "Generates random questions regardless of historical error patterns"
            ),
            correctAnswer = "Calibrates challenge complexity to keep user success rate within the optimal 75%-85% flow state band",
            type = "mcq",
            explanation = "ZPD calibration balances cognitive load: neither trivial (boredom) nor overwhelming (frustration).",
            difficultyTier = KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE,
            intrinsicComplexity = 3.4f,
            prerequisiteNodeId = "python",
            isCrossNodeSynthesis = true,
            connectedNodeIds = listOf("spaced_rep")
        ),

        // ==========================================
        // 10. SPACED REPETITION SM-2 (spaced_rep)
        // ==========================================
        KnowledgeGraphQuizQuestion(
            id = "kg_sr_found_1",
            nodeId = "spaced_rep",
            nodeTitle = "Spaced Repetition (SM-2)",
            conceptTag = "Forgetting Curve",
            question = "The Ebbinghaus Forgetting Curve shows memory retention decays exponentially over time without active recall.",
            options = listOf("True", "False"),
            correctAnswer = "True",
            type = "true_false",
            explanation = "Without active spaced review, cognitive recall drops rapidly in the first 24-48 hours.",
            difficultyTier = KnowledgeQuizDifficultyTier.FOUNDATIONAL,
            intrinsicComplexity = 1.2f,
            prerequisiteNodeId = "coroutines"
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_sr_core_1",
            nodeId = "spaced_rep",
            nodeTitle = "Spaced Repetition (SM-2)",
            conceptTag = "Easiness Factor (EF)",
            question = "In the SM-2 algorithm, what does the Easiness Factor (EF) determine?",
            options = listOf(
                "The multiplier applied to expand consecutive review intervals after successful recall",
                "The number of total questions in a quiz deck",
                "The color theme of the flashcard interface",
                "The user's global leaderboard rank"
            ),
            correctAnswer = "The multiplier applied to expand consecutive review intervals after successful recall",
            type = "mcq",
            explanation = "EF scales review intervals: higher EF yields faster interval growth for well-retained concepts.",
            difficultyTier = KnowledgeQuizDifficultyTier.CORE_APPLICATION,
            intrinsicComplexity = 2.4f,
            prerequisiteNodeId = "coroutines"
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_sr_expert_1",
            nodeId = "spaced_rep",
            nodeTitle = "Spaced Repetition (SM-2)",
            conceptTag = "Knowledge Graph Retention Calibration",
            question = "When a Knowledge Graph node transitions from Novice to Expert, how does retention half-life behave?",
            options = listOf(
                "Memory stability factor increases, multiplying days between reinforcement intervals",
                "Retention half-life drops to zero, requiring reviews every hour",
                "Intervals remain strictly fixed at 24 hours regardless of score",
                "The node is erased from the spaced repetition schedule"
            ),
            correctAnswer = "Memory stability factor increases, multiplying days between reinforcement intervals",
            type = "mcq",
            explanation = "Mastered Expert concepts exhibit high stability (S-factor), sustaining recall across weeks rather than days.",
            difficultyTier = KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE,
            intrinsicComplexity = 3.2f,
            prerequisiteNodeId = "tflite",
            isCrossNodeSynthesis = true,
            connectedNodeIds = listOf("tflite")
        ),

        // ==========================================
        // 11. ALGORITHMS & BIG-O (big_o)
        // ==========================================
        KnowledgeGraphQuizQuestion(
            id = "kg_bo_found_1",
            nodeId = "big_o",
            nodeTitle = "Algorithms & Big-O",
            conceptTag = "Asymptotic Notation",
            question = "What is the Big-O time complexity of accessing an array element by index in memory?",
            options = listOf("O(1)", "O(n)", "O(log n)", "O(n²)"),
            correctAnswer = "O(1)",
            type = "mcq",
            explanation = "Direct pointer offset calculation provides constant time O(1) random access in arrays.",
            difficultyTier = KnowledgeQuizDifficultyTier.FOUNDATIONAL,
            intrinsicComplexity = 1.3f,
            prerequisiteNodeId = "python"
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_bo_core_1",
            nodeId = "big_o",
            nodeTitle = "Algorithms & Big-O",
            conceptTag = "Binary Search",
            question = "Binary search achieves O(log n) time complexity by halving search space on each comparison in a sorted array.",
            options = listOf("True", "False"),
            correctAnswer = "True",
            type = "true_false",
            explanation = "Binary search repeatedly divides the search interval in half, requiring logarithmic steps.",
            difficultyTier = KnowledgeQuizDifficultyTier.CORE_APPLICATION,
            intrinsicComplexity = 2.3f,
            prerequisiteNodeId = "python"
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_bo_expert_1",
            nodeId = "big_o",
            nodeTitle = "Algorithms & Big-O",
            conceptTag = "Amortized Analysis",
            question = "Why is appending to a dynamic array (e.g. ArrayList / Python list) considered O(1) amortized time?",
            options = listOf(
                "Occasional O(n) geometric resizing copies are distributed across n preceding O(1) inserts",
                "Resizing never allocates new memory blocks",
                "Dynamic arrays use linked lists under the hood",
                "The OS hardware automatically doubles memory at zero CPU cost"
            ),
            correctAnswer = "Occasional O(n) geometric resizing copies are distributed across n preceding O(1) inserts",
            type = "mcq",
            explanation = "Geometric doubling ensures the average cost per append remains constant O(1) amortized.",
            difficultyTier = KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE,
            intrinsicComplexity = 3.3f,
            prerequisiteNodeId = "python",
            isCrossNodeSynthesis = true,
            connectedNodeIds = listOf("c_lang", "mysql")
        ),

        // ==========================================
        // 12. C SYSTEMS & MEMORY (c_lang)
        // ==========================================
        KnowledgeGraphQuizQuestion(
            id = "kg_c_found_1",
            nodeId = "c_lang",
            nodeTitle = "C Systems & Memory",
            conceptTag = "Pointers & Addresses",
            question = "In C, which operator is used to obtain the physical memory address of a variable?",
            options = listOf("&", "*", "->", "%"),
            correctAnswer = "&",
            type = "mcq",
            explanation = "The '&' address-of operator returns the memory address of the operand variable.",
            difficultyTier = KnowledgeQuizDifficultyTier.FOUNDATIONAL,
            intrinsicComplexity = 1.4f
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_c_core_1",
            nodeId = "c_lang",
            nodeTitle = "C Systems & Memory",
            conceptTag = "Dynamic Allocation",
            question = "Every block of memory allocated with 'malloc()' must be explicitly released with 'free()' to prevent memory leaks.",
            options = listOf("True", "False"),
            correctAnswer = "True",
            type = "true_false",
            explanation = "C has no automated garbage collector; heap allocations remain allocated until freed.",
            difficultyTier = KnowledgeQuizDifficultyTier.CORE_APPLICATION,
            intrinsicComplexity = 2.4f
        ),
        KnowledgeGraphQuizQuestion(
            id = "kg_c_expert_1",
            nodeId = "c_lang",
            nodeTitle = "C Systems & Memory",
            conceptTag = "Cache Line Alignment",
            question = "Why does structure padding and alignment improve CPU memory throughput in systems programming?",
            options = listOf(
                "Aligns data words to CPU bus width (e.g. 64-bit boundaries), avoiding multiple split memory read cycles",
                "Reduces the compiled binary size on disk",
                "Forces data to be stored exclusively in L1 cache forever",
                "Enables runtime reflection on C structs"
            ),
            correctAnswer = "Aligns data words to CPU bus width (e.g. 64-bit boundaries), avoiding multiple split memory read cycles",
            type = "mcq",
            explanation = "Misaligned data forces modern CPUs to perform multiple memory bus transactions to read a single value.",
            difficultyTier = KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE,
            intrinsicComplexity = 3.5f,
            isCrossNodeSynthesis = true,
            connectedNodeIds = listOf("big_o", "tflite")
        )
    )

    /**
     * Maps database question categories to Knowledge Graph node IDs.
     */
    fun mapCategoryToNodeId(categoryName: String): String {
        return when (categoryName.trim().lowercase()) {
            "html" -> "html"
            "css" -> "css"
            "javascript", "js" -> "js"
            "php" -> "php"
            "mysql", "sql" -> "mysql"
            "python", "py" -> "python"
            "c language", "c", "c_lang" -> "c_lang"
            "compose", "jetpack compose" -> "compose"
            "kotlin", "coroutines" -> "coroutines"
            "machine learning", "tflite" -> "tflite"
            "learning science", "spaced repetition" -> "spaced_rep"
            "algorithms", "big-o" -> "big_o"
            else -> "js"
        }
    }

    /**
     * Converts generic questions into KnowledgeGraphQuizQuestions with mapped node metadata.
     */
    fun convertGenericQuestion(q: Question, index: Int): KnowledgeGraphQuizQuestion {
        val mappedNodeId = mapCategoryToNodeId(q.category)
        val tier = when (q.difficulty.trim().lowercase()) {
            "hard" -> KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE
            "medium" -> KnowledgeQuizDifficultyTier.CORE_APPLICATION
            else -> KnowledgeQuizDifficultyTier.FOUNDATIONAL
        }
        val complexity = when (tier) {
            KnowledgeQuizDifficultyTier.EXPERT_ARCHITECTURE -> 3.0f
            KnowledgeQuizDifficultyTier.CORE_APPLICATION -> 2.1f
            KnowledgeQuizDifficultyTier.FOUNDATIONAL -> 1.3f
        }

        return KnowledgeGraphQuizQuestion(
            id = "converted_${mappedNodeId}_${index}_${q.question.hashCode()}",
            nodeId = mappedNodeId,
            nodeTitle = q.category,
            conceptTag = "${q.category} Core",
            question = q.question,
            options = if (q.type == "mcq" && q.options.isNotEmpty()) q.options else listOf("True", "False"),
            correctAnswer = q.answer,
            type = q.type,
            explanation = q.explanation.ifBlank { "Verified correct answer based on domain curriculum rules." },
            difficultyTier = tier,
            intrinsicComplexity = complexity
        )
    }

    /**
     * Retrieves the combined question pool for a specific Knowledge Graph node.
     */
    suspend fun getQuestionsForNode(context: Context, nodeId: String): List<KnowledgeGraphQuizQuestion> {
        val curated = CURATED_QUESTIONS.filter { it.nodeId.equals(nodeId, ignoreCase = true) }

        // Also fetch from repository to ensure deep variety
        val genericQuestions = QuestionsRepository.getAllQuestions(context)
        val relevantGeneric = genericQuestions
            .filter { mapCategoryToNodeId(it.category).equals(nodeId, ignoreCase = true) }
            .mapIndexed { idx, q -> convertGenericQuestion(q, idx) }

        val combined = (curated + relevantGeneric).distinctBy { it.question.trim().lowercase() }
        return if (combined.isNotEmpty()) combined else CURATED_QUESTIONS
    }

    /**
     * Synchronous access to curated questions without needing Context (ideal for tests and instant preview).
     */
    fun getCuratedQuestionsForNode(nodeId: String): List<KnowledgeGraphQuizQuestion> {
        return CURATED_QUESTIONS.filter { it.nodeId.equals(nodeId, ignoreCase = true) }
    }
}
