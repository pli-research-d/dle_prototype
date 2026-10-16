import {
  User,
  Question,
  QuizAttempt,
  LearningTelemetry,
  FlashcardItem,
  StudySessionRecord,
  QuizPerformanceStats,
  UserStreakData,
  UserGamificationState,
  CategoryMastery,
  LeaderboardEntry,
  LeaderboardScope,
  LeaderboardSort,
  ModelWeights,
  UserSettings,
  DigitalBadge,
} from '../types';
import rawQuestions from '../data/questions.json';
import { OnDeviceTrainableModel } from '../ml/trainableModel';
import { BadgeSystemEngine } from '../ml/badgeSystemEngine';

const STORAGE_KEY = 'pli_data_store_v1';

export class AppStore {
  private static instance: AppStore;

  private currentUser: User | null = null;
  private users: User[] = [];
  private telemetry: Record<string, LearningTelemetry> = {};
  private attempts: Record<string, QuizAttempt[]> = {};
  private flashcards: Record<string, FlashcardItem[]> = {};
  private studySessions: Record<string, StudySessionRecord[]> = {};
  private streaks: Record<string, UserStreakData> = {};
  private gamification: Record<string, UserGamificationState> = {};
  private modelWeights: Record<string, ModelWeights> = {};
  private friends: Record<string, string[]> = {};
  private unlockedBadges: Record<string, string[]> = {};
  private settings: Record<string, UserSettings> = {};
  private questions: Question[] = [];

  private listeners: Array<() => void> = [];

  private constructor() {
    this.questions = rawQuestions as Question[];
    this.loadFromStorage();
    if (this.users.length === 0) {
      this.seedInitialData();
    }
  }

  public static getInstance(): AppStore {
    if (!AppStore.instance) {
      AppStore.instance = new AppStore();
    }
    return AppStore.instance;
  }

  public subscribe(listener: () => void): () => void {
    this.listeners.push(listener);
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener);
    };
  }

  private notify() {
    this.saveToStorage();
    this.listeners.forEach(l => l());
  }

  private loadFromStorage() {
    try {
      const data = localStorage.getItem(STORAGE_KEY);
      if (data) {
        const parsed = JSON.parse(data);
        this.users = parsed.users || [];
        this.currentUser = parsed.currentUser || null;
        this.telemetry = parsed.telemetry || {};
        this.attempts = parsed.attempts || {};
        this.flashcards = parsed.flashcards || {};
        this.studySessions = parsed.studySessions || {};
        this.streaks = parsed.streaks || {};
        this.gamification = parsed.gamification || {};
        this.modelWeights = parsed.modelWeights || {};
        this.friends = parsed.friends || {};
        this.unlockedBadges = parsed.unlockedBadges || {};
        this.settings = parsed.settings || {};
      }
    } catch (e) {
      console.error('Failed to load store from localStorage', e);
    }
  }

  private saveToStorage() {
    try {
      const data = {
        users: this.users,
        currentUser: this.currentUser,
        telemetry: this.telemetry,
        attempts: this.attempts,
        flashcards: this.flashcards,
        studySessions: this.studySessions,
        streaks: this.streaks,
        gamification: this.gamification,
        modelWeights: this.modelWeights,
        friends: this.friends,
        unlockedBadges: this.unlockedBadges,
        settings: this.settings,
      };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    } catch (e) {
      console.error('Failed to persist store', e);
    }
  }

  private seedInitialData() {
    const defaultUser: User = {
      id: 1,
      username: 'demo_learner',
      createdAt: Date.now() - 86400000 * 7,
    };
    this.users.push(defaultUser);
    this.currentUser = defaultUser;

    const uname = defaultUser.username;

    this.telemetry[uname] = {
      loginCount: 9,
      totalTimeMinutes: 48.5,
      lastQuizScore: 82.0,
      lastDifficultyReached: 2.0,
      lastCategorySelected: 3.0,
      pageVisits: 28,
      searchQueries: 7,
      activityCompletionRate: 88.0,
      reactionsPositive: 8,
      reactionsNegative: 1,
      feedbackRating: 9.0,
      isEncryptedInSandbox: true,
      sandboxToken: 'token_pli_sec_01',
    };

    this.streaks[uname] = {
      username: uname,
      currentStreak: 5,
      longestStreak: 7,
      lastActiveDate: new Date().toISOString().split('T')[0],
      totalDaysActive: 9,
      lastMilestoneReached: 5,
      isStreakActiveToday: true,
      updatedAt: Date.now(),
    };

    this.gamification[uname] = {
      username: uname,
      fuel: 140,
      totalXp: 860,
      streakFreezes: 2,
      altitudeKm: 5.6,
      selectedSkin: 'Apollo Standard',
      selectedTheme: 'Deep Space',
      selectedFrame: 'Bronze Orbit',
      onboardingCompleted: true,
      selectedTopics: ['HTML', 'CSS', 'JavaScript', 'Python'],
      selectedSkillLevel: 'Surprise me',
      dailyGoalQuestions: 10,
      currentLeague: 'Silver',
      leagueRank: 3,
      unopenedChestsCount: 1,
    };

    this.modelWeights[uname] = OnDeviceTrainableModel.defaultInit(5, 8, 4);

    this.friends[uname] = ['MartianTiger67', 'SnowyOwl3'];

    this.unlockedBadges[uname] = ['streak_1d', 'streak_3d', 'quizzes_5'];

    this.settings[uname] = {
      themeMode: 'dark',
      accentColor: '#06B6D4',
      fontSizeScale: 1.0,
      highContrastEnabled: false,
      wellbeing: {
        username: uname,
        dailyTimeCapMinutes: 45,
        quietHoursEnabled: false,
        quietStartHour: 22,
        quietEndHour: 7,
        softStopReminderEnabled: true,
        soundEnabled: true,
        hapticsEnabled: true,
        learningStyle: 'Adaptive Dynamic',
      },
    };

    // Pre-populate realistic quiz attempts
    this.attempts[uname] = [
      {
        id: 1,
        username: uname,
        category: 'JavaScript',
        score: 5,
        totalQuestions: 5,
        difficultyLevel: 2,
        timestamp: Date.now() - 86400000 * 3,
        accuracyPercent: 100,
        elapsedTimeSeconds: 42,
      },
      {
        id: 2,
        username: uname,
        category: 'HTML',
        score: 4,
        totalQuestions: 5,
        difficultyLevel: 1,
        timestamp: Date.now() - 86400000 * 2,
        accuracyPercent: 80,
        elapsedTimeSeconds: 38,
      },
      {
        id: 3,
        username: uname,
        category: 'Python',
        score: 4,
        totalQuestions: 5,
        difficultyLevel: 2,
        timestamp: Date.now() - 86400000 * 1,
        accuracyPercent: 80,
        elapsedTimeSeconds: 51,
      },
      {
        id: 4,
        username: uname,
        category: 'CSS',
        score: 3,
        totalQuestions: 5,
        difficultyLevel: 2,
        timestamp: Date.now() - 3600000 * 6,
        accuracyPercent: 60,
        elapsedTimeSeconds: 49,
      },
      {
        id: 5,
        username: uname,
        category: 'MySQL',
        score: 2,
        totalQuestions: 5,
        difficultyLevel: 1,
        timestamp: Date.now() - 3600000 * 2,
        accuracyPercent: 40,
        elapsedTimeSeconds: 65,
      },
    ];

    // Pre-populate flashcards in Leitner boxes
    this.flashcards[uname] = [
      {
        id: 1,
        username: uname,
        category: 'JavaScript',
        question: 'What is the return type of typeof NaN in JavaScript?',
        options: ['number', 'NaN', 'undefined', 'object'],
        correctAnswer: 'number',
        explanation: 'In JavaScript, NaN (Not a Number) is a numeric data type representing an unrepresentable value.',
        boxLevel: 2,
        nextReviewTimestamp: Date.now() - 1000, // Due now!
        reviewCount: 3,
        lastReviewedTimestamp: Date.now() - 86400000 * 2,
      },
      {
        id: 2,
        username: uname,
        category: 'Python',
        question: 'Is a Python tuple mutable or immutable?',
        options: ['Mutable', 'Immutable'],
        correctAnswer: 'Immutable',
        explanation: 'Tuples are immutable sequence types in Python; elements cannot be modified after assignment.',
        boxLevel: 1,
        nextReviewTimestamp: Date.now() - 500, // Due now!
        reviewCount: 1,
        lastReviewedTimestamp: Date.now() - 86400000,
      },
      {
        id: 3,
        username: uname,
        category: 'CSS',
        question: 'Which CSS property creates a 3D context for transformed child elements?',
        options: ['transform-style: preserve-3d', 'perspective-origin', 'display: 3d', 'will-change: 3d'],
        correctAnswer: 'transform-style: preserve-3d',
        explanation: 'transform-style: preserve-3d allows child elements to retain their 3D transformations in space.',
        boxLevel: 3,
        nextReviewTimestamp: Date.now() + 86400000 * 4,
        reviewCount: 5,
        lastReviewedTimestamp: Date.now() - 86400000 * 3,
      },
    ];

    // Pre-populate study sessions
    this.studySessions[uname] = [
      {
        id: 1,
        username: uname,
        durationSeconds: 1500,
        targetMinutes: 25,
        pauseCount: 0,
        focusScore: 0.98,
        timestamp: Date.now() - 86400000 * 2,
      },
      {
        id: 2,
        username: uname,
        durationSeconds: 1200,
        targetMinutes: 20,
        pauseCount: 1,
        focusScore: 0.92,
        timestamp: Date.now() - 86400000 * 1,
      },
    ];

    this.saveToStorage();
  }

  // --- Auth & User ---
  public getCurrentUser(): User | null {
    return this.currentUser;
  }

  public login(username: string): User {
    const trimmed = username.trim().toLowerCase();
    let user = this.users.find(u => u.username.toLowerCase() === trimmed);
    if (!user) {
      user = {
        id: Date.now(),
        username: trimmed,
        createdAt: Date.now(),
      };
      this.users.push(user);
    }
    this.currentUser = user;
    if (!this.telemetry[user.username]) {
      this.telemetry[user.username] = {
        loginCount: 1,
        totalTimeMinutes: 2.0,
        lastQuizScore: 0,
        lastDifficultyReached: 1,
        lastCategorySelected: 1,
        pageVisits: 5,
        searchQueries: 1,
        activityCompletionRate: 50,
        reactionsPositive: 1,
        reactionsNegative: 0,
        feedbackRating: 8.0,
        isEncryptedInSandbox: true,
        sandboxToken: `token_${user.username}_0`,
      };
    }
    if (!this.streaks[user.username]) {
      this.streaks[user.username] = {
        username: user.username,
        currentStreak: 1,
        longestStreak: 1,
        lastActiveDate: new Date().toISOString().split('T')[0],
        totalDaysActive: 1,
        lastMilestoneReached: 0,
        isStreakActiveToday: true,
        updatedAt: Date.now(),
      };
    }
    if (!this.gamification[user.username]) {
      this.gamification[user.username] = {
        username: user.username,
        fuel: 80,
        totalXp: 120,
        streakFreezes: 1,
        altitudeKm: 1.2,
        selectedSkin: 'Apollo Standard',
        selectedTheme: 'Deep Space',
        selectedFrame: 'Bronze Orbit',
        onboardingCompleted: true,
        selectedTopics: ['HTML', 'CSS', 'JavaScript'],
        selectedSkillLevel: 'Surprise me',
        dailyGoalQuestions: 10,
        currentLeague: 'Bronze',
        leagueRank: 10,
        unopenedChestsCount: 1,
      };
    }
    this.notify();
    return user;
  }

  public logout() {
    this.currentUser = null;
    this.notify();
  }

  // --- Questions ---
  public getAllQuestions(): Question[] {
    return this.questions;
  }

  // --- Telemetry & Weights ---
  public getTelemetry(username: string): LearningTelemetry {
    return (
      this.telemetry[username] || {
        loginCount: 1,
        totalTimeMinutes: 5,
        lastQuizScore: 50,
        lastDifficultyReached: 1,
        lastCategorySelected: 1,
        pageVisits: 5,
        searchQueries: 2,
        activityCompletionRate: 60,
        reactionsPositive: 2,
        reactionsNegative: 0,
        feedbackRating: 8,
        isEncryptedInSandbox: true,
        sandboxToken: 'token_0',
      }
    );
  }

  public updateTelemetry(username: string, updates: Partial<LearningTelemetry>) {
    this.telemetry[username] = {
      ...this.getTelemetry(username),
      ...updates,
    };
    this.notify();
  }

  public getModelWeights(username: string): ModelWeights | null {
    return this.modelWeights[username] || null;
  }

  public saveModelWeights(username: string, weights: ModelWeights) {
    this.modelWeights[username] = weights;
    this.notify();
  }

  // --- Quiz Attempts & Stats ---
  public recordQuizAttempt(attempt: Omit<QuizAttempt, 'id'>): QuizAttempt {
    const username = attempt.username;
    if (!this.attempts[username]) {
      this.attempts[username] = [];
    }
    const newAttempt: QuizAttempt = {
      ...attempt,
      id: Date.now(),
    };
    this.attempts[username].unshift(newAttempt);

    // Update telemetry
    const curTele = this.getTelemetry(username);
    const scorePct = attempt.totalQuestions > 0 ? (attempt.score / attempt.totalQuestions) * 100 : 0;
    this.updateTelemetry(username, {
      lastQuizScore: scorePct,
      lastDifficultyReached: attempt.difficultyLevel,
      totalTimeMinutes: curTele.totalTimeMinutes + (attempt.elapsedTimeSeconds || 60) / 60,
    });

    // Award XP and Rocket Fuel
    const gam = this.getGamification(username);
    const earnedXp = attempt.score * 15 + 20;
    const earnedFuel = attempt.score * 3;
    this.updateGamification(username, {
      totalXp: gam.totalXp + earnedXp,
      fuel: gam.fuel + earnedFuel,
      altitudeKm: Math.round((gam.altitudeKm + earnedFuel * 0.05) * 10) / 10,
    });

    // Update streak if active today
    this.recordActivity(username);

    this.notify();
    return newAttempt;
  }

  public getRecentQuizAttempts(username: string, limit = 5): QuizAttempt[] {
    const list = this.attempts[username] || [];
    return list.slice(0, limit);
  }

  public getAllQuizAttempts(username: string): QuizAttempt[] {
    return this.attempts[username] || [];
  }

  public getQuizPerformanceStats(username: string): QuizPerformanceStats {
    const list = this.attempts[username] || [];
    const totalQuizzes = list.length;
    const totalQuestions = list.reduce((sum, a) => sum + a.totalQuestions, 0);
    const totalScore = list.reduce((sum, a) => sum + a.score, 0);
    const averageAccuracy = totalQuestions > 0 ? (totalScore / totalQuestions) * 100 : 0;
    const perfectCount = list.filter(a => a.totalQuestions > 0 && a.score === a.totalQuestions).length;
    const highestScore = list.reduce((max, a) => Math.max(max, a.score), 0);
    const streak = this.getStreakData(username).currentStreak;

    return {
      totalQuizzes,
      totalQuestionsAnswered: totalQuestions,
      totalScore,
      averageAccuracyPercent: Math.round(averageAccuracy * 10) / 10,
      perfectQuizzesCount: perfectCount,
      highestScore,
      earlyBirdQuizzesCount: 1,
      currentDailyStreak: streak,
    };
  }

  public getCategoryMasteryStats(username: string): CategoryMastery[] {
    const attempts = this.attempts[username] || [];
    const categories = ['HTML', 'CSS', 'JavaScript', 'PHP', 'MySQL', 'Python', 'C Language'];
    const icons: Record<string, string> = {
      HTML: '🌐',
      CSS: '🎨',
      JavaScript: '⚡',
      PHP: '🐘',
      MySQL: '🗄️',
      Python: '🐍',
      'C Language': '⚙️',
    };

    return categories.map(cat => {
      const catAttempts = attempts.filter(a => a.category.toLowerCase() === cat.toLowerCase());
      const totalAtt = catAttempts.length;
      const totalQ = catAttempts.reduce((sum, a) => sum + a.totalQuestions, 0);
      const correct = catAttempts.reduce((sum, a) => sum + a.score, 0);
      const acc = totalQ > 0 ? (correct / totalQ) * 100 : 0;
      const highestDiff = catAttempts.reduce((max, a) => Math.max(max, a.difficultyLevel), 1);

      let masteryLevel = 'Novice';
      let statusBadge = 'Untested';
      if (totalAtt > 0) {
        if (acc >= 90) {
          masteryLevel = 'Master';
          statusBadge = 'Mastered';
        } else if (acc >= 75) {
          masteryLevel = 'Proficient';
          statusBadge = 'Solidifying';
        } else if (acc >= 50) {
          masteryLevel = 'Practitioner';
          statusBadge = 'Solidifying';
        } else {
          masteryLevel = 'Novice';
          statusBadge = 'Needs Reinforcement';
        }
      }

      return {
        categoryName: cat,
        icon: icons[cat] || '📘',
        totalAttempts: totalAtt,
        totalQuestions: totalQ,
        correctAnswers: correct,
        accuracyPercent: Math.round(acc * 10) / 10,
        masteryLevel,
        highestDifficultyReached: highestDiff,
        statusBadge,
      };
    });
  }

  // --- Flashcards & Spaced Repetition ---
  public getDueFlashcards(username: string): FlashcardItem[] {
    const list = this.flashcards[username] || [];
    const now = Date.now();
    return list.filter(fc => fc.nextReviewTimestamp <= now);
  }

  public getAllFlashcards(username: string): FlashcardItem[] {
    return this.flashcards[username] || [];
  }

  public reviewFlashcard(
    username: string,
    flashcardId: number,
    quality: 'easy' | 'good' | 'hard'
  ) {
    const list = this.flashcards[username] || [];
    const index = list.findIndex(fc => fc.id === flashcardId);
    if (index === -1) return;

    const card = list[index];
    let nextBox = card.boxLevel;
    let daysToAdd = 1;

    if (quality === 'easy') {
      nextBox = Math.min(4, card.boxLevel + 1);
      daysToAdd = nextBox === 2 ? 3 : nextBox === 3 ? 7 : 14;
    } else if (quality === 'good') {
      nextBox = Math.min(4, card.boxLevel);
      daysToAdd = nextBox === 1 ? 1 : nextBox === 2 ? 3 : 7;
    } else {
      nextBox = 1;
      daysToAdd = 1;
    }

    const updated: FlashcardItem = {
      ...card,
      boxLevel: nextBox,
      reviewCount: card.reviewCount + 1,
      lastReviewedTimestamp: Date.now(),
      nextReviewTimestamp: Date.now() + daysToAdd * 86400000,
    };

    list[index] = updated;
    this.flashcards[username] = list;

    // Award XP
    const gam = this.getGamification(username);
    this.updateGamification(username, {
      totalXp: gam.totalXp + (quality === 'easy' ? 20 : 10),
      fuel: gam.fuel + 3,
    });

    this.notify();
  }

  public addFlashcard(item: Omit<FlashcardItem, 'id' | 'boxLevel' | 'reviewCount' | 'lastReviewedTimestamp' | 'nextReviewTimestamp'>) {
    const username = item.username;
    if (!this.flashcards[username]) {
      this.flashcards[username] = [];
    }
    const newCard: FlashcardItem = {
      ...item,
      id: Date.now(),
      boxLevel: 1,
      reviewCount: 0,
      lastReviewedTimestamp: 0,
      nextReviewTimestamp: Date.now(),
    };
    this.flashcards[username].push(newCard);
    this.notify();
    return newCard;
  }

  // --- Study Sessions ---
  public getStudySessions(username: string, limit = 10): StudySessionRecord[] {
    const list = this.studySessions[username] || [];
    return list.slice(0, limit);
  }

  public recordStudySession(session: Omit<StudySessionRecord, 'id' | 'timestamp'>) {
    const username = session.username;
    if (!this.studySessions[username]) {
      this.studySessions[username] = [];
    }
    const record: StudySessionRecord = {
      ...session,
      id: Date.now(),
      timestamp: Date.now(),
    };
    this.studySessions[username].unshift(record);

    // Update telemetry
    const tele = this.getTelemetry(username);
    this.updateTelemetry(username, {
      totalTimeMinutes: tele.totalTimeMinutes + session.durationSeconds / 60,
    });

    // Reward XP
    const gam = this.getGamification(username);
    const xpBonus = Math.round((session.durationSeconds / 60) * 8 * session.focusScore);
    this.updateGamification(username, {
      totalXp: gam.totalXp + xpBonus,
      fuel: gam.fuel + Math.round(session.durationSeconds / 180),
    });

    this.recordActivity(username);
    this.notify();
  }

  // --- Streaks ---
  public getStreakData(username: string): UserStreakData {
    return (
      this.streaks[username] || {
        username,
        currentStreak: 1,
        longestStreak: 1,
        lastActiveDate: new Date().toISOString().split('T')[0],
        totalDaysActive: 1,
        lastMilestoneReached: 0,
        isStreakActiveToday: true,
        updatedAt: Date.now(),
      }
    );
  }

  public recordActivity(username: string) {
    const streak = this.getStreakData(username);
    const today = new Date().toISOString().split('T')[0];

    if (streak.lastActiveDate === today) {
      return;
    }

    const yesterday = new Date(Date.now() - 86400000).toISOString().split('T')[0];
    let newStreak = 1;
    if (streak.lastActiveDate === yesterday) {
      newStreak = streak.currentStreak + 1;
    }

    const longest = Math.max(streak.longestStreak, newStreak);
    this.streaks[username] = {
      ...streak,
      currentStreak: newStreak,
      longestStreak: longest,
      lastActiveDate: today,
      totalDaysActive: streak.totalDaysActive + 1,
      isStreakActiveToday: true,
      updatedAt: Date.now(),
    };
    this.notify();
  }

  // --- Gamification ---
  public getGamification(username: string): UserGamificationState {
    return (
      this.gamification[username] || {
        username,
        fuel: 100,
        totalXp: 300,
        streakFreezes: 1,
        altitudeKm: 2.0,
        selectedSkin: 'Apollo Standard',
        selectedTheme: 'Deep Space',
        selectedFrame: 'Bronze Orbit',
        onboardingCompleted: true,
        selectedTopics: ['HTML', 'CSS', 'JavaScript'],
        selectedSkillLevel: 'Surprise me',
        dailyGoalQuestions: 10,
        currentLeague: 'Silver',
        leagueRank: 5,
        unopenedChestsCount: 1,
      }
    );
  }

  public updateGamification(username: string, updates: Partial<UserGamificationState>) {
    this.gamification[username] = {
      ...this.getGamification(username),
      ...updates,
    };
    this.notify();
  }

  // --- Leaderboard & Peers ---
  public getLeaderboardEntries(
    username: string,
    scope: LeaderboardScope = 'GLOBAL',
    sort: LeaderboardSort = 'STREAK'
  ): LeaderboardEntry[] {
    const virtualPeers = [
      { username: 'martiantiger67', displayName: 'MartianTiger67', avatarEmoji: '🐯', dominantTrait: 'Consistent', tier: 'Diamond', dailyStreak: 24, totalQuizzes: 82, totalScore: 580, accuracyPercent: 97.5 },
      { username: 'snowyowl3', displayName: 'SnowyOwl3', avatarEmoji: '🦉', dominantTrait: 'Deep Thinker', tier: 'Diamond', dailyStreak: 21, totalQuizzes: 74, totalScore: 530, accuracyPercent: 96.2 },
      { username: 'cosmicfalcon42', displayName: 'CosmicFalcon42', avatarEmoji: '🦅', dominantTrait: 'Speed Demon', tier: 'Diamond', dailyStreak: 18, totalQuizzes: 68, totalScore: 485, accuracyPercent: 95.0 },
      { username: 'cyberotter19', displayName: 'CyberOtter19', avatarEmoji: '🦦', dominantTrait: 'Algorithm Master', tier: 'Diamond', dailyStreak: 16, totalQuizzes: 62, totalScore: 440, accuracyPercent: 94.5 },
      { username: 'neonpanda88', displayName: 'NeonPanda88', avatarEmoji: '🐼', dominantTrait: 'Conscientious', tier: 'Platinum', dailyStreak: 14, totalQuizzes: 55, totalScore: 395, accuracyPercent: 93.0 },
      { username: 'solarhawk55', displayName: 'SolarHawk55', avatarEmoji: '⚡', dominantTrait: 'Streak Hunter', tier: 'Platinum', dailyStreak: 12, totalQuizzes: 48, totalScore: 350, accuracyPercent: 91.8 },
      { username: 'lunarlynx77', displayName: 'LunarLynx77', avatarEmoji: '🐱', dominantTrait: 'Understanding', tier: 'Platinum', dailyStreak: 10, totalQuizzes: 42, totalScore: 315, accuracyPercent: 90.5 },
      { username: 'astralfox91', displayName: 'AstralFox91', avatarEmoji: '🦊', dominantTrait: 'Night Owl', tier: 'Platinum', dailyStreak: 9, totalQuizzes: 38, totalScore: 280, accuracyPercent: 89.2 },
      { username: 'quantumbear23', displayName: 'QuantumBear23', avatarEmoji: '🐻', dominantTrait: 'Early Bird', tier: 'Gold', dailyStreak: 8, totalQuizzes: 33, totalScore: 245, accuracyPercent: 88.0 },
      { username: 'stellarwolf14', displayName: 'StellarWolf14', avatarEmoji: '🐺', dominantTrait: 'Bug Hunter', tier: 'Gold', dailyStreak: 7, totalQuizzes: 29, totalScore: 215, accuracyPercent: 86.8 },
      { username: 'turbohedgehog5', displayName: 'TurboHedgehog5', avatarEmoji: '🦔', dominantTrait: 'Explorer', tier: 'Silver', dailyStreak: 3, totalQuizzes: 13, totalScore: 105, accuracyPercent: 80.5 },
    ];

    const userStats = this.getQuizPerformanceStats(username);
    const userStreak = this.getStreakData(username);
    const friendsList = this.friends[username] || [];

    const currentUserEntry: LeaderboardEntry = {
      rank: 1,
      username,
      displayName: 'You (Local)',
      avatarEmoji: '🚀',
      dailyStreak: userStreak.currentStreak,
      totalQuizzes: userStats.totalQuizzes,
      totalScore: userStats.totalScore,
      accuracyPercent: userStats.averageAccuracyPercent || 85,
      isCurrentUser: true,
      isFriend: false,
      dominantTrait: 'Adaptive Learner',
      tier: userStreak.currentStreak >= 20 ? 'Diamond' : userStreak.currentStreak >= 10 ? 'Platinum' : 'Gold',
      rankDelta: 2,
    };

    let entries: LeaderboardEntry[] = [
      currentUserEntry,
      ...virtualPeers.map(vp => ({
        rank: 0,
        username: vp.username,
        displayName: vp.displayName,
        avatarEmoji: vp.avatarEmoji,
        dailyStreak: vp.dailyStreak,
        totalQuizzes: vp.totalQuizzes,
        totalScore: vp.totalScore,
        accuracyPercent: vp.accuracyPercent,
        isCurrentUser: false,
        isFriend: friendsList.includes(vp.displayName) || friendsList.includes(vp.username),
        dominantTrait: vp.dominantTrait,
        tier: vp.tier,
        rankDelta: 0,
      })),
    ];

    if (scope === 'FRIENDS') {
      entries = entries.filter(e => e.isCurrentUser || e.isFriend);
    }

    if (sort === 'STREAK') {
      entries.sort((a, b) => b.dailyStreak - a.dailyStreak);
    } else if (sort === 'SCORE') {
      entries.sort((a, b) => b.totalScore - a.totalScore);
    } else if (sort === 'ACCURACY') {
      entries.sort((a, b) => b.accuracyPercent - a.accuracyPercent);
    }

    return entries.map((e, idx) => ({ ...e, rank: idx + 1 }));
  }

  public toggleFriend(username: string, friendName: string) {
    if (!this.friends[username]) this.friends[username] = [];
    const list = this.friends[username];
    if (list.includes(friendName)) {
      this.friends[username] = list.filter(f => f !== friendName);
    } else {
      this.friends[username].push(friendName);
    }
    this.notify();
  }

  // --- Badges ---
  public getBadges(username: string): { allBadges: DigitalBadge[]; newlyUnlocked: DigitalBadge[] } {
    const streak = this.getStreakData(username);
    const stats = this.getQuizPerformanceStats(username);
    const unlocked = this.unlockedBadges[username] || [];

    const result = BadgeSystemEngine.evaluateBadges(streak, stats, unlocked);
    if (result.newlyUnlockedBadges.length > 0) {
      if (!this.unlockedBadges[username]) this.unlockedBadges[username] = [];
      result.newlyUnlockedBadges.forEach(b => {
        if (!this.unlockedBadges[username].includes(b.id)) {
          this.unlockedBadges[username].push(b.id);
        }
      });
      this.saveToStorage();
    }
    return { allBadges: result.allBadges, newlyUnlocked: result.newlyUnlockedBadges };
  }

  // --- Settings ---
  public getSettings(username: string): UserSettings {
    return (
      this.settings[username] || {
        themeMode: 'dark',
        accentColor: '#06B6D4',
        fontSizeScale: 1.0,
        highContrastEnabled: false,
        wellbeing: {
          username,
          dailyTimeCapMinutes: 45,
          quietHoursEnabled: false,
          quietStartHour: 22,
          quietEndHour: 7,
          softStopReminderEnabled: true,
          soundEnabled: true,
          hapticsEnabled: true,
          learningStyle: 'Adaptive Dynamic',
        },
      }
    );
  }

  public updateSettings(username: string, updates: Partial<UserSettings>) {
    this.settings[username] = {
      ...this.getSettings(username),
      ...updates,
    };
    this.notify();
  }

  public clearAllData() {
    localStorage.removeItem(STORAGE_KEY);
    this.users = [];
    this.currentUser = null;
    this.telemetry = {};
    this.attempts = {};
    this.flashcards = {};
    this.studySessions = {};
    this.streaks = {};
    this.gamification = {};
    this.modelWeights = {};
    this.friends = {};
    this.unlockedBadges = {};
    this.settings = {};
    this.seedInitialData();
    this.notify();
  }
}
