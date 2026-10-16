import { QaChatMessage } from '../types';

interface QaKnowledgeEntry {
  id: string;
  category: string;
  title: string;
  keywords: string[];
  explanation: string;
  codeSnippet?: string;
  relatedFollowUps: string[];
}

export class QaChatEngine {
  public static readonly KNOWLEDGE_BASE: QaKnowledgeEntry[] = [
    {
      id: 'js_event_loop',
      category: 'JavaScript',
      title: 'JavaScript Event Loop & Microtasks',
      keywords: ['event loop', 'microtask', 'macrotask', 'async', 'promise', 'settimeout', 'call stack'],
      explanation:
        'The JavaScript Event Loop orchestrates asynchronous operations on a single thread. The Call Stack runs synchronous code first. When empty, it processes the **Microtask Queue** (Promise callbacks, queueMicrotask) before picking from the **Task / Macrotask Queue** (setTimeout, setInterval, I/O).',
      codeSnippet: `console.log('1'); // Sync
setTimeout(() => console.log('2'), 0); // Macrotask
Promise.resolve().then(() => console.log('3')); // Microtask
console.log('4'); // Sync
// Output sequence: 1, 4, 3, 2`,
      relatedFollowUps: ['What are JavaScript closures?', 'How does async/await work?', 'Explain Promises vs Callbacks'],
    },
    {
      id: 'js_closures',
      category: 'JavaScript',
      title: 'Closures & Lexical Scope',
      keywords: ['closure', 'closures', 'lexical scope', 'scope', 'encapsulation'],
      explanation:
        'A **Closure** is the combination of a function bundled together with references to its surrounding state (the lexical environment). Closures allow an inner function to access variables from an enclosing scope even after the outer function has executed.',
      codeSnippet: `function createCounter() {
  let count = 0;
  return () => ++count; // count is retained in lexical closure
}
const counter = createCounter();
console.log(counter()); // 1
console.log(counter()); // 2`,
      relatedFollowUps: ['What is the JavaScript Event Loop?', 'Difference between let, const, var'],
    },
    {
      id: 'tflite_on_device',
      category: 'Machine Learning',
      title: 'On-Device TensorFlow Lite & Privacy-Preserving ML',
      keywords: ['tflite', 'tensorflow', 'on device', 'neural network', 'inference', 'model', 'weights', 'privacy'],
      explanation:
        '**On-device machine learning** executes neural inference and weight adaptations directly within the local client runtime. User interaction telemetry (quiz scores, pacing, error patterns) never leaves the device, providing zero-latency personalization and mathematical privacy.',
      codeSnippet: `// 2-layer Neural Forward Pass (5 inputs -> 8 hidden -> 4 traits)
const h = relu(dotProduct(W1, inputs) + b1);
const traits = sigmoid(dotProduct(W2, h) + b2);
// Produces [Conscientiousness, Motivation, Understanding, Engagement]`,
      relatedFollowUps: ['How does Federated Learning work?', 'Explain Gradient Descent with Momentum', 'What are Trait Scores?'],
    },
    {
      id: 'css_flex_grid',
      category: 'CSS',
      title: 'CSS Flexbox vs CSS Grid',
      keywords: ['css', 'flexbox', 'grid', 'layout', 'justify-content', 'align-items'],
      explanation:
        '• **Flexbox (1D Layout)**: Best for linear rows or columns (toolbars, navigation bars, vertically centering items).\n• **CSS Grid (2D Layout)**: Best for complex grid templates with simultaneous row and column alignment (dashboard cards, gallery layouts).',
      codeSnippet: `/* Modern Responsive CSS Grid with auto-fit */
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}`,
      relatedFollowUps: ['How do CSS media queries work?', 'Explain CSS specificity'],
    },
    {
      id: 'sql_indexes',
      category: 'MySQL',
      title: 'SQL Indexes & B-Trees',
      keywords: ['sql', 'index', 'b-tree', 'query', 'explain', 'performance', 'database'],
      explanation:
        'Database indexes (typically B-Tree or B+Tree) allow the query optimizer to jump directly to records in O(log N) steps instead of performing an O(N) full table scan. Composite indexes must match the leftmost prefix of query filter conditions.',
      codeSnippet: `CREATE INDEX idx_user_quiz ON quiz_history (username, timestamp DESC);
EXPLAIN SELECT * FROM quiz_history WHERE username = 'alex' ORDER BY timestamp DESC;`,
      relatedFollowUps: ['What are SQL JOIN types?', 'Explain ACID Transactions', 'What is Normalization?'],
    },
    {
      id: 'python_decorators',
      category: 'Python',
      title: 'Python Decorators & Higher-Order Functions',
      keywords: ['python', 'decorator', 'decorators', '@', 'wrapper', 'functools'],
      explanation:
        'In Python, a **decorator** is a design pattern that allows you to dynamically alter or extend the behavior of a function or method without modifying its original source code. It wraps the target function.',
      codeSnippet: `from functools import wraps
import time

def measure_time(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        start = time.time()
        result = func(*args, **kwargs)
        print(f"{func.__name__} took {time.time() - start:.4f}s")
        return result
    return wrapper`,
      relatedFollowUps: ['Explain Python list comprehensions', 'What is Python GIL?'],
    },
    {
      id: 'big_o',
      category: 'Algorithms',
      title: 'Big-O Complexity & Asymptotic Limits',
      keywords: ['big o', 'complexity', 'algorithm', 'asymptotic', 'time complexity', 'space complexity'],
      explanation:
        'Big-O notation describes the limiting behavior of an algorithm as the input size N tends toward infinity:\n• O(1): Constant time (Hash map lookup)\n• O(log N): Logarithmic time (Binary search)\n• O(N): Linear time (Single array scan)\n• O(N log N): Linearithmic (MergeSort, QuickSort avg)\n• O(N²): Quadratic (Nested loops)',
      codeSnippet: `// Binary Search O(log N)
function binarySearch(arr, target) {
  let low = 0, high = arr.length - 1;
  while (low <= high) {
    const mid = (low + high) >> 1;
    if (arr[mid] === target) return mid;
    arr[mid] < target ? low = mid + 1 : high = mid - 1;
  }
  return -1;
}`,
      relatedFollowUps: ['What is the difference between BFS and DFS?', 'How does QuickSort work?'],
    },
  ];

  public static answerQuestion(userQuery: string): QaChatMessage {
    const qLower = userQuery.toLowerCase().trim();

    // Check match against knowledge base
    let bestMatch: QaKnowledgeEntry | null = null;
    let highestScore = 0;

    for (const entry of this.KNOWLEDGE_BASE) {
      let score = 0;
      for (const kw of entry.keywords) {
        if (qLower.includes(kw)) {
          score += kw.length;
        }
      }
      if (qLower.includes(entry.category.toLowerCase())) {
        score += 5;
      }
      if (score > highestScore) {
        highestScore = score;
        bestMatch = entry;
      }
    }

    if (bestMatch && highestScore >= 3) {
      return {
        id: `qa_${Date.now()}`,
        sender: 'assistant',
        text: `### ${bestMatch.title}\n\n${bestMatch.explanation}`,
        timestamp: Date.now(),
        codeSnippet: bestMatch.codeSnippet,
        followUpSuggestions: bestMatch.relatedFollowUps,
        referencedTopic: bestMatch.category,
      };
    }

    // Default conversational learning response
    return {
      id: `qa_${Date.now()}`,
      sender: 'assistant',
      text: `Great inquiry regarding "${userQuery}"!\n\nAs your on-device Private Learning Intelligence (PLI), I analyze curriculum topics across Web Architecture, Backend Databases, Mobile Reactive Flow, Systems Architecture, and On-Device Machine Learning.\n\nTry asking me about:\n• **JavaScript**: Event Loop, Closures, Microtasks vs Macrotasks\n• **CSS**: Flexbox vs Grid, Responsive design\n• **Databases**: SQL B-Tree Indexes, Query Optimization\n• **Algorithms**: Big-O Complexity, Search algorithms\n• **Machine Learning**: On-Device TFLite, Neural Weights & Momentum Training`,
      timestamp: Date.now(),
      followUpSuggestions: [
        'How does On-Device TFLite work?',
        'Explain the JavaScript Event Loop',
        'Flexbox vs Grid in CSS',
        'What is Big-O Complexity?',
      ],
    };
  }
}
