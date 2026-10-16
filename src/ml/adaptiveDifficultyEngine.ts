import { DdaQuizState, Question } from '../types';

export class AdaptiveDifficultyEngine {
  public static calculateNextState(
    currentState: DdaQuizState,
    isCorrect: boolean
  ): DdaQuizState {
    const newStreak = isCorrect ? currentState.streak + 1 : 0;
    const highestStreak = Math.max(currentState.highestStreak, newStreak);

    let nextDifficulty = currentState.currentDifficulty;
    let levelChanges = currentState.levelChanges;

    if (isCorrect) {
      if (currentState.currentDifficulty === 'Easy' && newStreak >= 2) {
        nextDifficulty = 'Medium';
        levelChanges++;
      } else if (currentState.currentDifficulty === 'Medium' && newStreak >= 3) {
        nextDifficulty = 'Hard';
        levelChanges++;
      }
    } else {
      if (currentState.currentDifficulty === 'Hard') {
        nextDifficulty = 'Medium';
        levelChanges++;
      } else if (currentState.currentDifficulty === 'Medium' && currentState.streak === 0) {
        nextDifficulty = 'Easy';
        levelChanges++;
      }
    }

    let zpdZone: 'Scaffolding Needed' | 'Balanced Challenge (ZPD)' | 'Mastery Peak';
    if (!isCorrect && nextDifficulty === 'Easy') {
      zpdZone = 'Scaffolding Needed';
    } else if (newStreak >= 4 && nextDifficulty === 'Hard') {
      zpdZone = 'Mastery Peak';
    } else {
      zpdZone = 'Balanced Challenge (ZPD)';
    }

    return {
      currentDifficulty: nextDifficulty,
      streak: newStreak,
      highestStreak,
      levelChanges,
      zpdZone,
    };
  }

  public static filterAdaptiveQuestions(
    allQuestions: Question[],
    category: string,
    targetDifficulty: 'Easy' | 'Medium' | 'Hard'
  ): Question[] {
    const categoryQuestions = allQuestions.filter(
      q => q.category.toLowerCase() === category.toLowerCase()
    );

    const matchDifficulty = categoryQuestions.filter(
      q => q.difficulty.toLowerCase() === targetDifficulty.toLowerCase()
    );

    if (matchDifficulty.length >= 5) {
      return matchDifficulty;
    }

    // Fallback: return any available in the category, or all questions
    return categoryQuestions.length > 0 ? categoryQuestions : allQuestions;
  }
}
