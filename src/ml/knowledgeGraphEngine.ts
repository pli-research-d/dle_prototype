import {
  KnowledgeCluster,
  KnowledgeEdge,
  KnowledgeGraphData,
  KnowledgeTopicNode,
  CategoryMastery,
  QuizAttempt,
} from '../types';

export class KnowledgeGraphEngine {
  public static readonly CLUSTERS: KnowledgeCluster[] = [
    {
      name: 'Web Architecture',
      color: '#06B6D4', // Cyan
      normCenterX: 0.28,
      normCenterY: 0.26,
      description: 'Frontend DOM, layout engines, and interactive browser client runtime.',
    },
    {
      name: 'Backend & Database',
      color: '#8B5CF6', // Purple
      normCenterX: 0.70,
      normCenterY: 0.42,
      description: 'Server-side services, SQL relational schemas, and ACID transaction models.',
    },
    {
      name: 'Mobile & Reactive',
      color: '#10B981', // Emerald
      normCenterX: 0.60,
      normCenterY: 0.70,
      description: 'Declarative Jetpack Compose UI state flow and structured Kotlin Coroutines.',
    },
    {
      name: 'Applied Intelligence',
      color: '#F59E0B', // Amber
      normCenterX: 0.42,
      normCenterY: 0.86,
      description: 'On-device TFLite neural models, edge inference, and spaced repetition curves.',
    },
    {
      name: 'Systems Architecture',
      color: '#F43F5E', // Rose
      normCenterX: 0.72,
      normCenterY: 0.16,
      description: 'Low-level C pointers, memory layout, and asymptotic Big-O algorithmic limits.',
    },
  ];

  public static readonly STATIC_EDGES: KnowledgeEdge[] = [
    { fromNodeId: 'html', toNodeId: 'css', relationship: 'Semantic Layout' },
    { fromNodeId: 'html', toNodeId: 'js', relationship: 'Web Architecture' },
    { fromNodeId: 'css', toNodeId: 'js', relationship: 'DOM Interaction' },
    { fromNodeId: 'js', toNodeId: 'php', relationship: 'Client-Server API' },
    { fromNodeId: 'php', toNodeId: 'mysql', relationship: 'Database Queries' },
    { fromNodeId: 'python', toNodeId: 'big_o', relationship: 'Algorithmic Logic' },
    { fromNodeId: 'python', toNodeId: 'tflite', relationship: 'Model Training' },
    { fromNodeId: 'compose', toNodeId: 'coroutines', relationship: 'Reactive State Flow' },
    { fromNodeId: 'compose', toNodeId: 'tflite', relationship: 'On-Device Inference' },
    { fromNodeId: 'coroutines', toNodeId: 'spaced_rep', relationship: 'Async Scheduling' },
    { fromNodeId: 'mysql', toNodeId: 'big_o', relationship: 'Query Optimization' },
    { fromNodeId: 'tflite', toNodeId: 'spaced_rep', relationship: 'Cognitive Feedback' },
    { fromNodeId: 'c_lang', toNodeId: 'big_o', relationship: 'Memory & Pointer Arithmetic' },
    { fromNodeId: 'c_lang', toNodeId: 'tflite', relationship: 'Low-Level Runtime Bindings' },
  ];

  public static buildGraphFromTelemetry(
    masteries: CategoryMastery[],
    attempts: QuizAttempt[] = []
  ): KnowledgeGraphData {
    const masteryMap = new Map<string, CategoryMastery>();
    masteries.forEach(m => masteryMap.set(m.categoryName.toLowerCase().trim(), m));

    const getStats = (catName: string): { accuracy: number; attempts: number } => {
      const match = masteryMap.get(catName.toLowerCase().trim());
      if (match) {
        return { accuracy: match.accuracyPercent, attempts: match.totalAttempts };
      }
      const relevant = attempts.filter(
        a => a.category.toLowerCase().trim() === catName.toLowerCase().trim()
      );
      if (relevant.length > 0) {
        const totalScore = relevant.reduce((acc, r) => acc + r.score, 0);
        const totalQ = relevant.reduce((acc, r) => acc + r.totalQuestions, 0);
        return {
          accuracy: totalQ > 0 ? (totalScore / totalQ) * 100 : 0,
          attempts: relevant.length,
        };
      }
      return { accuracy: 0, attempts: 0 };
    };

    const evalStatus = (
      accuracy: number,
      attemptsCount: number
    ): 'UNEXPLORED' | 'NEEDS_REVIEW' | 'SOLIDIFYING' | 'STRENGTH' => {
      if (attemptsCount === 0) return 'UNEXPLORED';
      if (accuracy < 55) return 'NEEDS_REVIEW';
      if (accuracy >= 80) return 'STRENGTH';
      return 'SOLIDIFYING';
    };

    const rawNodes: Array<{
      id: string;
      title: string;
      category: string;
      catNum: number;
      normX: number;
      normY: number;
    }> = [
      { id: 'html', title: 'HTML5 Foundations', category: 'HTML', catNum: 1, normX: 0.16, normY: 0.22 },
      { id: 'css', title: 'CSS3 & Flex/Grid', category: 'CSS', catNum: 2, normX: 0.32, normY: 0.15 },
      { id: 'js', title: 'JavaScript ES6+', category: 'JavaScript', catNum: 3, normX: 0.36, normY: 0.38 },
      { id: 'php', title: 'PHP Server Backend', category: 'PHP', catNum: 4, normX: 0.62, normY: 0.32 },
      { id: 'mysql', title: 'MySQL Relational', category: 'MySQL', catNum: 5, normX: 0.82, normY: 0.44 },
      { id: 'python', title: 'Python Core', category: 'Python', catNum: 6, normX: 0.38, normY: 0.62 },
      { id: 'c_lang', title: 'C & Memory Pointers', category: 'C Language', catNum: 7, normX: 0.82, normY: 0.16 },
      { id: 'big_o', title: 'Big-O Complexity', category: 'Python', catNum: 6, normX: 0.64, normY: 0.18 },
      { id: 'compose', title: 'Declarative UI (Compose)', category: 'JavaScript', catNum: 3, normX: 0.60, normY: 0.68 },
      { id: 'coroutines', title: 'Reactive Async / Coroutines', category: 'JavaScript', catNum: 3, normX: 0.74, normY: 0.78 },
      { id: 'tflite', title: 'TFLite On-Device ML', category: 'Python', catNum: 6, normX: 0.42, normY: 0.84 },
      { id: 'spaced_rep', title: 'Spaced Repetition Engine', category: 'JavaScript', catNum: 3, normX: 0.22, normY: 0.82 },
    ];

    const nodes: KnowledgeTopicNode[] = rawNodes.map(rn => {
      const { accuracy, attempts } = getStats(rn.category);
      const status = evalStatus(accuracy, attempts);
      return {
        id: rn.id,
        title: rn.title,
        category: rn.category,
        categoryNumber: rn.catNum,
        accuracyPercent: accuracy,
        attemptsCount: attempts,
        status,
        normX: rn.normX,
        normY: rn.normY,
        masteryFraction: Math.min(1, accuracy / 100),
        recommendedNext: false,
      };
    });

    // Mark reinforcement priority (lowest accuracy among attempted, or first unexplored)
    let priorityNode = nodes.find(n => n.status === 'NEEDS_REVIEW');
    if (!priorityNode) {
      priorityNode = nodes.find(n => n.status === 'UNEXPLORED') || nodes[0];
    }
    if (priorityNode) {
      priorityNode.recommendedNext = true;
    }

    const totalAccuracy = nodes.reduce((sum, n) => sum + n.accuracyPercent, 0);
    const averageMastery = Math.round(totalAccuracy / nodes.length);

    return {
      nodes,
      edges: this.STATIC_EDGES,
      clusters: this.CLUSTERS,
      reinforcementPriorityNode: priorityNode,
      averageMasteryPercent: averageMastery,
    };
  }
}
