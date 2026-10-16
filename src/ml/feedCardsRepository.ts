import { FeedCard, FlashcardItem } from '../types';

export class FeedCardsRepository {
  public static getCuratedCards(dueCards: FlashcardItem[] = []): FeedCard[] {
    const list: FeedCard[] = [];

    // Add due flashcards as review cards
    dueCards.slice(0, 2).forEach(fc => {
      list.push({
        id: `review_${fc.id}_${Date.now()}`,
        type: 'REVIEW_CARD',
        category: fc.category,
        title: `Leitner Review · Box ${fc.boxLevel}`,
        prompt: fc.question,
        options: fc.options || ['True', 'False'],
        correctAnswer: fc.correctAnswer,
        explanation: fc.explanation,
        xpReward: 15,
        fuelReward: 4,
      });
    });

    const curated: FeedCard[] = [
      // SPOT THE BUG 1
      {
        id: 'bug_js_1',
        type: 'SPOT_THE_BUG',
        category: 'JavaScript',
        title: 'Spot the Bug 🐛',
        prompt: 'Tap the line that causes an uncaught TypeError at runtime:',
        codeSnippet: `1: const user = { name: "Alex", profile: null };\n2: const getCity = (u) => {\n3:   return u.profile.address.city;\n4: };\n5: console.log(getCity(user));`,
        bugLineIndex: 2,
        bugLines: [
          'const user = { name: "Alex", profile: null };',
          'const getCity = (u) => {',
          '  return u.profile.address.city;',
          '};',
          'console.log(getCity(user));',
        ],
        correctAnswer: '  return u.profile.address.city;',
        explanation:
          "Line 3 attempts to read 'address' on 'u.profile' which is null, triggering a TypeError. Use optional chaining: u.profile?.address?.city.",
        xpReward: 18,
        fuelReward: 5,
      },
      // SPOT THE BUG 2
      {
        id: 'bug_py_1',
        type: 'SPOT_THE_BUG',
        category: 'Python',
        title: 'Spot the Bug 🐛',
        prompt: 'Tap the line with the dangerous mutable default argument bug:',
        codeSnippet: `1: def append_item(val, target_list=[]):\n2:     target_list.append(val)\n3:     return target_list\n4: print(append_item(1))\n5: print(append_item(2))`,
        bugLineIndex: 0,
        bugLines: [
          'def append_item(val, target_list=[]):',
          '    target_list.append(val)',
          '    return target_list',
          'print(append_item(1))',
          'print(append_item(2))',
        ],
        correctAnswer: 'def append_item(val, target_list=[]):',
        explanation:
          'Default list arguments are evaluated once at definition time, causing accumulated state across calls. Use target_list=None instead.',
        xpReward: 18,
        fuelReward: 5,
      },
      // SPOT THE BUG 3
      {
        id: 'bug_sql_1',
        type: 'SPOT_THE_BUG',
        category: 'MySQL',
        title: 'Spot the Bug 🐛',
        prompt: 'Tap the faulty clause in this aggregation query:',
        codeSnippet: `1: SELECT dept, COUNT(*) as emp_count\n2: FROM employees\n3: WHERE COUNT(*) > 5\n4: GROUP BY dept\n5: ORDER BY emp_count DESC;`,
        bugLineIndex: 2,
        bugLines: [
          'SELECT dept, COUNT(*) as emp_count',
          'FROM employees',
          'WHERE COUNT(*) > 5',
          'GROUP BY dept',
          'ORDER BY emp_count DESC;',
        ],
        correctAnswer: 'WHERE COUNT(*) > 5',
        explanation:
          "Aggregate conditions cannot appear in WHERE clauses because WHERE runs before grouping. Use 'HAVING COUNT(*) > 5' after GROUP BY.",
        xpReward: 20,
        fuelReward: 5,
      },
      // WHAT'S THE OUTPUT 1
      {
        id: 'output_js_1',
        type: 'WHATS_THE_OUTPUT',
        category: 'JavaScript',
        title: "What's the Output? 💻",
        prompt: "What will be logged to the console by this code?",
        codeSnippet: `console.log(1 + "2" + "2");\nconsole.log(1 + +"2" + "2");`,
        options: ['"122" and "32"', '"14" and "32"', '"122" and "122"', 'NaN and "32"'],
        correctAnswer: '"122" and "32"',
        explanation:
          '1 + "2" performs string concatenation -> "12". Then "12" + "2" -> "122". In the second line, +"2" is unary plus coercing to number 2, so 1 + 2 = 3, then 3 + "2" = "32".',
        xpReward: 15,
        fuelReward: 4,
      },
      // WHAT'S THE OUTPUT 2
      {
        id: 'output_py_1',
        type: 'WHATS_THE_OUTPUT',
        category: 'Python',
        title: "What's the Output? 🐍",
        prompt: 'What does this slice expression evaluate to in Python?',
        codeSnippet: `nums = [10, 20, 30, 40, 50]\nprint(nums[::-2])`,
        options: ['[50, 30, 10]', '[10, 30, 50]', '[50, 40, 30]', '[40, 20]'],
        correctAnswer: '[50, 30, 10]',
        explanation:
          'The slice [::-2] reverses the list with a step of 2, starting from index 4 (50), then index 2 (30), then index 0 (10).',
        xpReward: 15,
        fuelReward: 4,
      },
      // EXPLAINER 15S
      {
        id: 'explainer_dom_1',
        type: 'EXPLAINER_15S',
        category: 'JavaScript',
        title: '15-Second Breakdown ⚡',
        prompt: 'DOM Event Delegation Pattern',
        codeSnippet: `// Instead of 1000 child listeners, attach 1 to the parent container:\ncontainer.addEventListener("click", (e) => {\n  if (e.target.matches(".card-btn")) {\n    handleAction(e.target.dataset.id);\n  }\n});`,
        explanation:
          'Event delegation leverages Event Bubbling: an event triggered on a leaf element propagates up through all ancestor elements in the DOM tree. Saves memory and automatically handles dynamic elements.',
        xpReward: 12,
        fuelReward: 3,
        correctAnswer: 'Understood!',
        options: ['Understood!', 'Save to Flashcards'],
      },
      // MYSTERY CARD
      {
        id: 'mystery_1',
        type: 'MYSTERY_CARD',
        category: 'Applied Intelligence',
        title: 'Cognitive Mystery Drop 🎁',
        prompt: 'A rare celestial learning anomaly has appeared in your orbit! Tap to claim your fuel infusion and bonus XP.',
        explanation:
          'Bonus drops occur periodically when you engage with diverse topics or maintain an active review streak.',
        xpReward: 50,
        fuelReward: 15,
        correctAnswer: 'Claim Reward',
        options: ['Claim Reward'],
        isMystery: true,
      },
    ];

    list.push(...curated);
    return list;
  }
}
