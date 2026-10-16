import { DigitalBadge, QuizPerformanceStats, UserStreakData } from '../types';

export interface BadgeCheckResult {
  allBadges: DigitalBadge[];
  newlyUnlockedBadges: DigitalBadge[];
}

export class BadgeSystemEngine {
  public static evaluateBadges(
    streakData: UserStreakData,
    stats: QuizPerformanceStats,
    unlockedBadgeIds: string[] = []
  ): BadgeCheckResult {
    const unlockedSet = new Set(unlockedBadgeIds);
    const newlyUnlocked: DigitalBadge[] = [];

    const badgeDefs = [
      // Streak Milestones
      {
        id: 'streak_1d',
        title: 'Streak Spark',
        description: 'Ignite your daily learning habit with your first active study day.',
        category: 'STREAK_MILESTONE' as const,
        tier: 'BRONZE' as const,
        iconEmoji: '⚡',
        targetValue: 1,
        xpReward: 50,
        rarityText: 'Common · Day 1',
        currentValue: streakData.currentStreak,
        progressLabel: `${Math.min(streakData.currentStreak, 1)} / 1 Day`,
      },
      {
        id: 'streak_3d',
        title: '3-Day Flame',
        description: 'Maintain a 3-day consecutive study streak without breaking the chain.',
        category: 'STREAK_MILESTONE' as const,
        tier: 'SILVER' as const,
        iconEmoji: '🔥',
        targetValue: 3,
        xpReward: 100,
        rarityText: 'Uncommon · 3 Days',
        currentValue: streakData.currentStreak,
        progressLabel: `${Math.min(streakData.currentStreak, 3)} / 3 Days`,
      },
      {
        id: 'streak_7d',
        title: '7-Day Orbit',
        description: 'Achieve one whole week of uninterrupted daily learning.',
        category: 'STREAK_MILESTONE' as const,
        tier: 'GOLD' as const,
        iconEmoji: '🌟',
        targetValue: 7,
        xpReward: 250,
        rarityText: 'Rare · 7 Days',
        currentValue: streakData.currentStreak,
        progressLabel: `${Math.min(streakData.currentStreak, 7)} / 7 Days`,
      },
      {
        id: 'streak_14d',
        title: 'Fortnight Titan',
        description: 'Two continuous weeks of knowledge reinforcement.',
        category: 'STREAK_MILESTONE' as const,
        tier: 'PLATINUM' as const,
        iconEmoji: '🚀',
        targetValue: 14,
        xpReward: 500,
        rarityText: 'Epic · 14 Days',
        currentValue: streakData.currentStreak,
        progressLabel: `${Math.min(streakData.currentStreak, 14)} / 14 Days`,
      },
      {
        id: 'streak_30d',
        title: 'Celestial Orbit',
        description: 'A 30-day continuous learning streak. True mastery achieved.',
        category: 'STREAK_MILESTONE' as const,
        tier: 'DIAMOND' as const,
        iconEmoji: '👑',
        targetValue: 30,
        xpReward: 1000,
        rarityText: 'Legendary · 30 Days',
        currentValue: streakData.currentStreak,
        progressLabel: `${Math.min(streakData.currentStreak, 30)} / 30 Days`,
      },

      // High-Volume Study
      {
        id: 'quizzes_5',
        title: 'Curious Novice',
        description: 'Complete 5 adaptive learning quizzes across any topics.',
        category: 'HIGH_VOLUME_STUDY' as const,
        tier: 'BRONZE' as const,
        iconEmoji: '📚',
        targetValue: 5,
        xpReward: 60,
        rarityText: 'Common · 5 Quizzes',
        currentValue: stats.totalQuizzes,
        progressLabel: `${Math.min(stats.totalQuizzes, 5)} / 5 Quizzes`,
      },
      {
        id: 'quizzes_20',
        title: 'Quiz Enthusiast',
        description: 'Complete 20 learning quizzes in the curriculum.',
        category: 'HIGH_VOLUME_STUDY' as const,
        tier: 'SILVER' as const,
        iconEmoji: '🎯',
        targetValue: 20,
        xpReward: 200,
        rarityText: 'Uncommon · 20 Quizzes',
        currentValue: stats.totalQuizzes,
        progressLabel: `${Math.min(stats.totalQuizzes, 20)} / 20 Quizzes`,
      },
      {
        id: 'questions_100',
        title: 'Centurion Scholar',
        description: 'Answer 100 adaptive curriculum questions.',
        category: 'HIGH_VOLUME_STUDY' as const,
        tier: 'GOLD' as const,
        iconEmoji: '🛡️',
        targetValue: 100,
        xpReward: 350,
        rarityText: 'Rare · 100 Questions',
        currentValue: stats.totalQuestionsAnswered,
        progressLabel: `${Math.min(stats.totalQuestionsAnswered, 100)} / 100 Qs`,
      },

      // Mastery & Focus
      {
        id: 'perfect_quiz',
        title: 'Flawless Mind',
        description: 'Score 100% on any adaptive quiz session.',
        category: 'MASTERY' as const,
        tier: 'GOLD' as const,
        iconEmoji: '💎',
        targetValue: 1,
        xpReward: 150,
        rarityText: 'Rare · Flawless Score',
        currentValue: stats.perfectQuizzesCount,
        progressLabel: `${Math.min(stats.perfectQuizzesCount, 1)} / 1 Perfect`,
      },
      {
        id: 'early_bird',
        title: 'Dawn Patrol',
        description: 'Complete a study session or quiz before 9:00 AM.',
        category: 'MASTERY' as const,
        tier: 'SILVER' as const,
        iconEmoji: '🌅',
        targetValue: 1,
        xpReward: 90,
        rarityText: 'Uncommon · Early Bird',
        currentValue: stats.earlyBirdQuizzesCount,
        progressLabel: `${Math.min(stats.earlyBirdQuizzesCount, 1)} / 1 Dawn`,
      },
      {
        id: 'accuracy_90',
        title: 'Precision Architect',
        description: 'Maintain an overall average accuracy of 90% or higher.',
        category: 'MASTERY' as const,
        tier: 'PLATINUM' as const,
        iconEmoji: '🔬',
        targetValue: 90,
        xpReward: 400,
        rarityText: 'Epic · 90% Accuracy',
        currentValue: stats.averageAccuracyPercent,
        progressLabel: `${Math.round(stats.averageAccuracyPercent)}% / 90%`,
      },
    ];

    const allBadges: DigitalBadge[] = badgeDefs.map(b => {
      const isQualified = b.currentValue >= b.targetValue;
      const isAlreadyUnlocked = unlockedSet.has(b.id);
      const isUnlocked = isQualified || isAlreadyUnlocked;

      if (isQualified && !isAlreadyUnlocked) {
        newlyUnlocked.push({
          id: b.id,
          title: b.title,
          description: b.description,
          category: b.category,
          tier: b.tier,
          iconEmoji: b.iconEmoji,
          isUnlocked: true,
          unlockedAt: Date.now(),
          currentProgress: b.currentValue,
          targetProgress: b.targetValue,
          progressFraction: Math.min(1.0, b.currentValue / b.targetValue),
          progressLabel: b.progressLabel,
          xpReward: b.xpReward,
          rarityText: b.rarityText,
        });
      }

      return {
        id: b.id,
        title: b.title,
        description: b.description,
        category: b.category,
        tier: b.tier,
        iconEmoji: b.iconEmoji,
        isUnlocked,
        unlockedAt: isUnlocked ? Date.now() : null,
        currentProgress: b.currentValue,
        targetProgress: b.targetValue,
        progressFraction: Math.min(1.0, b.currentValue / b.targetValue),
        progressLabel: b.progressLabel,
        xpReward: b.xpReward,
        rarityText: b.rarityText,
      };
    });

    return { allBadges, newlyUnlockedBadges: newlyUnlocked };
  }
}
