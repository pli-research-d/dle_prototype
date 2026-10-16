import {
  LearningTelemetry,
  PersonalizationProfile,
  TraitScore,
  ModelWeights,
  PersonalizedActionPlan,
  LearnerArchetype,
  CategoryMastery,
} from '../types';
import { OnDeviceTrainableModel } from './trainableModel';

export class TFLiteEngine {
  public static readonly MEANS = [4.2, 8.0, 45.7, 2.0, 3.8];
  public static readonly STDS = [2.0, 4.1, 28.5, 0.8, 1.9];

  private static traitLabel(score: number): string {
    if (score >= 0.75) return 'Advanced';
    if (score >= 0.5) return 'Proficient';
    if (score >= 0.3) return 'Developing';
    return 'Needs Attention';
  }

  public static runInference(
    telemetry: LearningTelemetry,
    customWeights?: ModelWeights | null
  ): PersonalizationProfile {
    const startTime = performance.now();

    const rawInput = [
      telemetry.loginCount,
      telemetry.totalTimeMinutes,
      telemetry.lastQuizScore,
      telemetry.lastDifficultyReached,
      telemetry.lastCategorySelected,
    ];

    const inputScaled = OnDeviceTrainableModel.normalize(rawInput);

    let preds: number[];
    let modelSource: string;
    let isPersonalized = false;

    if (customWeights && customWeights.trainedEpochs > 0) {
      const model = new OnDeviceTrainableModel(5, 8, 4, customWeights);
      const res = model.forward(inputScaled);
      preds = res.y;
      modelSource = `On-Device Trained (Epochs: ${customWeights.trainedEpochs})`;
      isPersonalized = true;
    } else {
      // Baseline model inference: simulation with standard baseline weights
      const baselineModel = new OnDeviceTrainableModel(5, 8, 4);
      const res = baselineModel.forward(inputScaled);
      preds = res.y;
      modelSource = 'Baseline Neural Model (TFLite Engine)';
      isPersonalized = false;
    }

    const latency = Math.max(1, Math.round(performance.now() - startTime));

    const clamp = (val: number) => Math.min(1.0, Math.max(0.05, val));

    const cScore = clamp(preds[0]);
    const mScore = clamp(preds[1]);
    const uScore = clamp(preds[2]);
    const eScore = clamp(preds[3]);

    const traits: TraitScore[] = [
      {
        name: 'Conscientiousness',
        score: cScore,
        rawValue: preds[0],
        description: 'Study consistency & daily habit tracking',
        levelLabel: this.traitLabel(cScore),
      },
      {
        name: 'Motivation',
        score: mScore,
        rawValue: preds[1],
        description: 'Drive to explore & master challenging topics',
        levelLabel: this.traitLabel(mScore),
      },
      {
        name: 'Understanding',
        score: uScore,
        rawValue: preds[2],
        description: 'Conceptual accuracy & retention across quizzes',
        levelLabel: this.traitLabel(uScore),
      },
      {
        name: 'Engagement',
        score: eScore,
        rawValue: preds[3],
        description: 'Activity frequency & interactive responsiveness',
        levelLabel: this.traitLabel(eScore),
      },
    ];

    const sorted = [...traits].sort((a, b) => b.score - a.score);
    const topTrait = sorted[0];
    const lowTrait = sorted[sorted.length - 1];

    const tips: string[] = [];
    switch (lowTrait.name) {
      case 'Conscientiousness':
        tips.push('Set a recurring 10-minute daily review habit to build consistent momentum.');
        break;
      case 'Motivation':
        tips.push('Pick topics aligned with a personal passion project to renew your drive.');
        break;
      case 'Understanding':
        tips.push('Review question explanations and try recreating example snippets by hand.');
        break;
      case 'Engagement':
        tips.push('Take a short quiz after every study session to reinforce active recall.');
        break;
    }

    switch (topTrait.name) {
      case 'Conscientiousness':
        tips.push('High consistency unlocked! Ready for medium & hard challenges.');
        break;
      case 'Motivation':
        tips.push('Excellent drive! Channel your enthusiasm into multi-topic mixed quizzes.');
        break;
      case 'Understanding':
        tips.push('Strong conceptual grasp! Solid retention across fundamental rules.');
        break;
      case 'Engagement':
        tips.push('Top engagement level! Maintain your active daily rhythm.');
        break;
    }

    return {
      conscientiousness: traits[0],
      motivation: traits[1],
      understanding: traits[2],
      engagement: traits[3],
      primaryStrength: topTrait.name,
      focusArea: lowTrait.name,
      tips,
      inferenceTimeMs: latency,
      inferenceLatencyMs: latency,
      isPersonalizedWeights: isPersonalized,
      modelSource,
    };
  }

  public static determineArchetype(profile: PersonalizationProfile): LearnerArchetype {
    const c = profile.conscientiousness.score;
    const m = profile.motivation.score;
    const u = profile.understanding.score;
    const e = profile.engagement.score;

    if (u >= 0.75 && c >= 0.7) {
      return {
        title: 'Master Strategist',
        subtitle: 'Deep Conceptual Architect',
        iconEmoji: '🧠',
        description: 'You assimilate complex syntax and algorithms with methodic precision.',
        cognitiveSuperpower: 'First-principles breakdown & high conceptual retention',
        suggestedPacing: '30-minute deep blocks with hard problems',
        optimalStudyWindow: 'Morning (09:00 - 11:30)',
      };
    }
    if (m >= 0.75 && e >= 0.7) {
      return {
        title: 'Sprint Pioneer',
        subtitle: 'Rapid Explorer & Rapid Iteration',
        iconEmoji: '⚡',
        description: 'High energy and drive keep you exploring diverse topics and peer challenges.',
        cognitiveSuperpower: 'Fast exploratory prototyping & competitive instinct',
        suggestedPacing: 'Short 15-minute high-tempo focus bursts',
        optimalStudyWindow: 'Evening (18:00 - 20:30)',
      };
    }
    if (c >= 0.7) {
      return {
        title: 'Disciplined Sentinel',
        subtitle: 'Daily Habit Builder',
        iconEmoji: '🛡️',
        description: 'Unshakable study rhythm with rock-solid daily streaks.',
        cognitiveSuperpower: 'Steadfast consistency and compounding retention',
        suggestedPacing: 'Fixed 20-minute daily sessions at the same hour',
        optimalStudyWindow: 'Early Morning (07:30 - 08:30)',
      };
    }

    return {
      title: 'Adaptive Explorer',
      subtitle: 'Dynamic Multi-Discipline Learner',
      iconEmoji: '🚀',
      description: 'You adjust fluidly between frontend, backend, and machine learning topics.',
      cognitiveSuperpower: 'Cross-domain synthesis and flexible adaptability',
      suggestedPacing: '25-minute Pomodoro intervals',
      optimalStudyWindow: 'Afternoon (14:00 - 16:30)',
    };
  }

  public static generateActionPlan(
    profile: PersonalizationProfile,
    masteries: CategoryMastery[],
    dueCardsCount: number
  ): PersonalizedActionPlan {
    const archetype = this.determineArchetype(profile);

    // Pick recommended category based on lowest accuracy or untested category
    let recommendedCategory = 'JavaScript';
    let recCatNum = 3;
    let recReason = 'Key foundational web language for full-stack fluency.';
    let recIcon = '⚡';
    let tier: 'Easy' | 'Medium' | 'Hard' = 'Medium';

    const weak = masteries.find(m => m.accuracyPercent < 65 && m.totalAttempts > 0);
    const unstarted = masteries.find(m => m.totalAttempts === 0);

    if (weak) {
      recommendedCategory = weak.categoryName;
      recReason = `Strengthen accuracy from ${Math.round(weak.accuracyPercent)}% to proficiency.`;
      tier = weak.accuracyPercent < 45 ? 'Easy' : 'Medium';
    } else if (unstarted) {
      recommendedCategory = unstarted.categoryName;
      recReason = 'Expand your curriculum breadth into unmastered territory.';
      tier = 'Easy';
    } else {
      recommendedCategory = 'Python';
      recCatNum = 6;
      recReason = 'Advanced algorithmic challenges to peak your mastery.';
      tier = 'Hard';
    }

    const catMap: Record<string, { num: number; icon: string }> = {
      HTML: { num: 1, icon: '🌐' },
      CSS: { num: 2, icon: '🎨' },
      JavaScript: { num: 3, icon: '⚡' },
      PHP: { num: 4, icon: '🐘' },
      MySQL: { num: 5, icon: '🗄️' },
      Python: { num: 6, icon: '🐍' },
      'C Language': { num: 7, icon: '⚙️' },
    };

    if (catMap[recommendedCategory]) {
      recCatNum = catMap[recommendedCategory].num;
      recIcon = catMap[recommendedCategory].icon;
    }

    const readiness = (profile.motivation.score * 0.4 + profile.conscientiousness.score * 0.4 + profile.engagement.score * 0.2);

    return {
      archetype,
      recommendedCategory,
      recommendedCategoryNumber: recCatNum,
      recommendationReason: recReason,
      recommendedCategoryIcon: recIcon,
      recommendedInitialTier: tier,
      dueFlashcardsCount: dueCardsCount,
      categoryMasteries: masteries,
      optimalTimeWindow: archetype.optimalStudyWindow,
      cognitiveReadinessScore: Math.round(readiness * 100) / 100,
    };
  }
}
