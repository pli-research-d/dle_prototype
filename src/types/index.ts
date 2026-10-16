export interface User {
  id: number;
  username: string;
  createdAt: number;
}

export interface Question {
  type: 'mcq' | 'true_false' | string;
  category: string;
  difficulty: 'Easy' | 'Medium' | 'Hard' | string;
  question: string;
  options?: string[];
  answer: string | boolean;
  explanation: string;
  example?: string;
}

export interface QuizAttempt {
  id: number;
  username: string;
  category: string;
  score: number;
  totalQuestions: number;
  difficultyLevel: number;
  timestamp: number;
  accuracyPercent?: number;
  elapsedTimeSeconds?: number;
}

export interface LearningTelemetry {
  loginCount: number;
  totalTimeMinutes: number;
  lastQuizScore: number;
  lastDifficultyReached: number;
  lastCategorySelected: number;
  pageVisits: number;
  searchQueries: number;
  activityCompletionRate: number; // 0..100%
  reactionsPositive: number;
  reactionsNegative: number;
  feedbackRating: number;          // 0..10
  isEncryptedInSandbox: boolean;
  sandboxToken: string;
}

export interface TraitScore {
  name: string;
  score: number; // 0.0 to 1.0 clamped
  rawValue: number;
  description: string;
  levelLabel: string;
}

export interface PersonalizationProfile {
  conscientiousness: TraitScore;
  motivation: TraitScore;
  understanding: TraitScore;
  engagement: TraitScore;
  primaryStrength: string;
  focusArea: string;
  tips: string[];
  inferenceTimeMs: number;
  inferenceLatencyMs: number;
  isPersonalizedWeights: boolean;
  modelSource: string;
}

export interface TraitSnapshot {
  id: number;
  username: string;
  conscientiousness: number;
  motivation: number;
  understanding: number;
  engagement: number;
  primaryStrength: string;
  focusArea: string;
  modelSource: string;
  timestamp: number;
}

export interface FlashcardItem {
  id: number;
  username: string;
  category: string;
  question: string;
  optionsJson?: string;
  options?: string[];
  correctAnswer: string;
  explanation: string;
  boxLevel: number; // 1 to 4 (Mastered)
  nextReviewTimestamp: number;
  reviewCount: number;
  lastReviewedTimestamp: number;
}

export interface StudySessionRecord {
  id: number;
  username: string;
  durationSeconds: number;
  targetMinutes: number;
  pauseCount: number;
  focusScore: number;
  timestamp: number;
}

export interface DdaQuizState {
  currentDifficulty: 'Easy' | 'Medium' | 'Hard';
  streak: number;
  highestStreak: number;
  levelChanges: number;
  zpdZone: 'Scaffolding Needed' | 'Balanced Challenge (ZPD)' | 'Mastery Peak';
}

export interface QuizPerformanceStats {
  totalQuizzes: number;
  totalQuestionsAnswered: number;
  totalScore: number;
  averageAccuracyPercent: number;
  perfectQuizzesCount: number;
  highestScore: number;
  earlyBirdQuizzesCount: number;
  currentDailyStreak: number;
}

export type LeaderboardScope = 'GLOBAL' | 'FRIENDS';
export type LeaderboardSort = 'STREAK' | 'SCORE' | 'ACCURACY';

export interface LeaderboardEntry {
  rank: number;
  username: string;
  displayName: string;
  avatarEmoji: string;
  dailyStreak: number;
  totalQuizzes: number;
  totalScore: number;
  accuracyPercent: number;
  isCurrentUser: boolean;
  isFriend: boolean;
  dominantTrait: string;
  tier: string;
  rankDelta: number;
}

export interface DailyGoalProgress {
  targetQuestions: number;
  answeredToday: number;
  percentComplete: number;
  isAchieved: boolean;
  targetHours: number;
  hoursCompletedToday: number;
  minutesCompletedToday: number;
  hoursPercentComplete: number;
}

export interface CategoryMastery {
  categoryName: string;
  icon: string;
  totalAttempts: number;
  totalQuestions: number;
  correctAnswers: number;
  accuracyPercent: number;
  masteryLevel: string; // Novice, Practitioner, Proficient, Master
  highestDifficultyReached: number;
  statusBadge: string;
}

export interface LearnerArchetype {
  title: string;
  subtitle: string;
  iconEmoji: string;
  description: string;
  cognitiveSuperpower: string;
  suggestedPacing: string;
  optimalStudyWindow: string;
}

export interface PersonalizedActionPlan {
  archetype: LearnerArchetype;
  recommendedCategory: string;
  recommendedCategoryNumber: number;
  recommendationReason: string;
  recommendedCategoryIcon: string;
  recommendedInitialTier: 'Easy' | 'Medium' | 'Hard';
  dueFlashcardsCount: number;
  categoryMasteries: CategoryMastery[];
  optimalTimeWindow: string;
  cognitiveReadinessScore: number;
}

export interface UserStreakData {
  username: string;
  currentStreak: number;
  longestStreak: number;
  lastActiveDate: string;
  totalDaysActive: number;
  lastMilestoneReached: number;
  isStreakActiveToday: boolean;
  updatedAt: number;
}

export type FeedCardType =
  | 'MCQ'
  | 'SPOT_THE_BUG'
  | 'WHATS_THE_OUTPUT'
  | 'FILL_BLANK'
  | 'EXPLAINER_15S'
  | 'REVIEW_CARD'
  | 'MYSTERY_CARD';

export interface FeedCard {
  id: string;
  type: FeedCardType;
  category: string;
  title: string;
  prompt: string;
  codeSnippet?: string;
  bugLineIndex?: number;
  bugLines?: string[];
  options?: string[];
  correctAnswer: string;
  explanation: string;
  xpReward: number;
  fuelReward: number;
  isMystery?: boolean;
}

export interface UserGamificationState {
  username: string;
  fuel: number;
  totalXp: number;
  streakFreezes: number;
  altitudeKm: number;
  selectedSkin: string;
  selectedTheme: string;
  selectedFrame: string;
  onboardingCompleted: boolean;
  selectedTopics: string[];
  selectedSkillLevel: string;
  dailyGoalQuestions: number;
  currentLeague: string;
  leagueRank: number;
  unopenedChestsCount: number;
}

export interface MysteryChest {
  id: number;
  username: string;
  chestType: string;
  isOpened: boolean;
  rewardXp: number;
  rewardFuel: number;
  rewardBadge: string;
  rewardSkin: string;
  createdAt: number;
}

export interface DuelMatchRecord {
  id: number;
  username: string;
  opponentName: string;
  opponentAvatar: string;
  opponentTier: string;
  category: string;
  userScore: number;
  opponentScore: number;
  isWin: boolean;
  xpEarned: number;
  fuelEarned: number;
  timestamp: number;
}

export interface WellbeingSettings {
  username: string;
  dailyTimeCapMinutes: number;
  quietHoursEnabled: boolean;
  quietStartHour: number;
  quietEndHour: number;
  softStopReminderEnabled: boolean;
  soundEnabled: boolean;
  hapticsEnabled: boolean;
  learningStyle: string;
}

export interface UserSettings {
  themeMode: 'dark' | 'light' | 'system';
  accentColor: string;
  fontSizeScale: number;
  highContrastEnabled: boolean;
  wellbeing: WellbeingSettings;
}

export type BadgeCategory = 'STREAK_MILESTONE' | 'HIGH_VOLUME_STUDY' | 'MASTERY';
export type BadgeTier = 'BRONZE' | 'SILVER' | 'GOLD' | 'PLATINUM' | 'DIAMOND';

export interface DigitalBadge {
  id: string;
  title: string;
  description: string;
  category: BadgeCategory;
  tier: BadgeTier;
  iconEmoji: string;
  isUnlocked: boolean;
  unlockedAt: number | null;
  currentProgress: number;
  targetProgress: number;
  progressFraction: number; // 0.0 to 1.0
  progressLabel: string;
  xpReward: number;
  rarityText: string;
}

export interface ModelWeights {
  w1: number[][]; // [hiddenDim][inputDim]
  b1: number[];   // [hiddenDim]
  w2: number[][]; // [outputDim][hiddenDim]
  b2: number[];   // [outputDim]
  version: number;
  trainedEpochs: number;
  finalLoss: number;
  updatedAt: number;
}

export interface TrainingSample {
  inputs: number[];
  targets: number[];
}

export interface TrainingProgress {
  epoch: number;
  totalEpochs: number;
  loss: number;
  isComplete: boolean;
}

export interface KnowledgeTopicNode {
  id: string;
  title: string;
  category: string;
  categoryNumber: number;
  accuracyPercent: number;
  attemptsCount: number;
  status: 'UNEXPLORED' | 'NEEDS_REVIEW' | 'SOLIDIFYING' | 'STRENGTH';
  normX: number;
  normY: number;
  masteryFraction: number; // 0.0 to 1.0
  recommendedNext: boolean;
}

export interface KnowledgeEdge {
  fromNodeId: string;
  toNodeId: string;
  relationship: string;
}

export interface KnowledgeCluster {
  name: string;
  color: string;
  normCenterX: number;
  normCenterY: number;
  description: string;
}

export interface KnowledgeGraphData {
  nodes: KnowledgeTopicNode[];
  edges: KnowledgeEdge[];
  clusters: KnowledgeCluster[];
  reinforcementPriorityNode?: KnowledgeTopicNode;
  averageMasteryPercent: number;
}

export interface QaChatMessage {
  id: string;
  sender: 'user' | 'assistant';
  text: string;
  timestamp: number;
  codeSnippet?: string;
  followUpSuggestions?: string[];
  referencedTopic?: string;
}
