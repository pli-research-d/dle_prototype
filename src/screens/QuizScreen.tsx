import React, { useState, useEffect } from 'react';
import { User, Question, DdaQuizState, QuizAttempt } from '../types';
import { AppStore } from '../services/store';
import { AdaptiveDifficultyEngine } from '../ml/adaptiveDifficultyEngine';
import { triggerBadgeConfetti, triggerLevelUpConfetti } from '../utils/confetti';
import {
  ArrowLeft,
  Flame,
  Clock,
  CheckCircle2,
  XCircle,
  HelpCircle,
  Zap,
  Award,
  ChevronRight,
  Sparkles,
} from 'lucide-react';

interface QuizScreenProps {
  user: User;
  categoryName: string;
  categoryNumber: number;
  initialTier?: string;
  onBack: () => void;
  onCompleted: (attempt: QuizAttempt) => void;
}

export const QuizScreen: React.FC<QuizScreenProps> = ({
  user,
  categoryName,
  categoryNumber,
  initialTier = 'Easy',
  onBack,
  onCompleted,
}) => {
  const store = AppStore.getInstance();
  const allQuestions = store.getAllQuestions();

  const [questions, setQuestions] = useState<Question[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [score, setScore] = useState(0);
  const [selectedAnswer, setSelectedAnswer] = useState<string | boolean | null>(null);
  const [isAnswered, setIsAnswered] = useState(false);
  const [isFinished, setIsFinished] = useState(false);
  const [elapsedTime, setElapsedTime] = useState(0);

  const [ddaState, setDdaState] = useState<DdaQuizState>({
    currentDifficulty: (initialTier as any) || 'Easy',
    streak: 0,
    highestStreak: 0,
    levelChanges: 0,
    zpdZone: 'Balanced Challenge (ZPD)',
  });

  useEffect(() => {
    // Select 5 questions adapted to category and difficulty
    const filtered = AdaptiveDifficultyEngine.filterAdaptiveQuestions(
      allQuestions,
      categoryName,
      ddaState.currentDifficulty
    );
    const shuffled = [...filtered].sort(() => 0.5 - Math.random()).slice(0, 5);
    setQuestions(shuffled);
  }, [categoryName]);

  // Elapsed timer
  useEffect(() => {
    if (isFinished) return;
    const interval = setInterval(() => {
      setElapsedTime(prev => prev + 1);
    }, 1000);
    return () => clearInterval(interval);
  }, [isFinished]);

  const currentQ = questions[currentIndex];

  const handleSelectAnswer = (ans: string | boolean) => {
    if (isAnswered) return;
    setSelectedAnswer(ans);
    setIsAnswered(true);

    const isCorrect =
      String(ans).trim().toLowerCase() === String(currentQ.answer).trim().toLowerCase();

    if (isCorrect) {
      setScore(prev => prev + 1);
      triggerBadgeConfetti();
    }

    // Dynamic difficulty adjustment
    const nextDda = AdaptiveDifficultyEngine.calculateNextState(ddaState, isCorrect);
    setDdaState(nextDda);
  };

  const handleNextQuestion = () => {
    if (currentIndex < questions.length - 1) {
      setCurrentIndex(prev => prev + 1);
      setSelectedAnswer(null);
      setIsAnswered(false);
    } else {
      finishQuiz();
    }
  };

  const finishQuiz = () => {
    setIsFinished(true);
    triggerLevelUpConfetti();

    const finalScore = score + (selectedAnswer && String(selectedAnswer).trim().toLowerCase() === String(currentQ?.answer).trim().toLowerCase() ? 0 : 0);
    const attempt = store.recordQuizAttempt({
      username: user.username,
      category: categoryName,
      score: finalScore,
      totalQuestions: questions.length,
      difficultyLevel: categoryNumber,
      timestamp: Date.now(),
      accuracyPercent: (finalScore / Math.max(1, questions.length)) * 100,
      elapsedTimeSeconds: elapsedTime,
    });

    // Check badges
    store.getBadges(user.username);
  };

  if (!currentQ && !isFinished) {
    return (
      <div className="min-h-screen bg-[#0B0F19] text-white flex flex-col items-center justify-center p-6 text-center">
        <p className="text-slate-400">Loading adaptive quiz curriculum...</p>
        <button
          onClick={onBack}
          className="mt-4 px-4 py-2 bg-slate-800 rounded-xl text-xs font-bold"
        >
          Return
        </button>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#0B0F19] text-white flex flex-col justify-between p-4 max-w-md mx-auto">
      {/* Top Header */}
      <div>
        <div className="flex items-center justify-between py-2 border-b border-slate-800/80 mb-4">
          <button
            onClick={onBack}
            className="p-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:text-white"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>

          <div className="text-center">
            <div className="text-xs font-bold text-slate-400 uppercase tracking-wider">
              {categoryName}
            </div>
            <div className="text-sm font-extrabold text-white">
              Question {currentIndex + 1} of {questions.length}
            </div>
          </div>

          <div className="flex items-center space-x-1.5 px-2.5 py-1 rounded-xl bg-slate-900 border border-slate-800 text-xs font-mono text-cyan-400">
            <Clock className="w-3.5 h-3.5" />
            <span>{Math.floor(elapsedTime / 60)}:{(elapsedTime % 60).toString().padStart(2, '0')}</span>
          </div>
        </div>

        {/* Dynamic Difficulty & ZPD Status Bar */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-2xl p-3 mb-4 flex items-center justify-between text-xs">
          <div className="flex items-center space-x-2">
            <span className="text-[10px] uppercase font-bold text-slate-400">Tier:</span>
            <span
              className={`px-2 py-0.5 rounded-full font-bold text-[11px] border ${
                ddaState.currentDifficulty === 'Hard'
                  ? 'bg-rose-950 text-rose-300 border-rose-800'
                  : ddaState.currentDifficulty === 'Medium'
                  ? 'bg-amber-950 text-amber-300 border-amber-800'
                  : 'bg-cyan-950 text-cyan-300 border-cyan-800'
              }`}
            >
              {ddaState.currentDifficulty}
            </span>
          </div>

          <div className="flex items-center space-x-2">
            <span className="text-[11px] text-cyan-400 font-semibold">{ddaState.zpdZone}</span>
            {ddaState.streak > 1 && (
              <span className="flex items-center text-amber-400 font-bold">
                <Flame className="w-3.5 h-3.5 fill-amber-500 mr-0.5" />
                {ddaState.streak}x
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Main Question Body */}
      {!isFinished ? (
        <div className="flex-1 flex flex-col justify-center my-2 space-y-4">
          <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl">
            <p className="text-base sm:text-lg font-bold text-white leading-relaxed">
              {currentQ.question}
            </p>

            {currentQ.example && (
              <div className="mt-3 bg-[#0B0F19] rounded-xl p-3 border border-slate-800 font-mono text-xs text-cyan-300 overflow-x-auto">
                <pre>{currentQ.example}</pre>
              </div>
            )}
          </div>

          {/* Options */}
          <div className="space-y-2.5">
            {(currentQ.options && currentQ.options.length > 0
              ? currentQ.options
              : ['True', 'False']
            ).map((opt, idx) => {
              const isSelected = selectedAnswer === opt;
              const isCorrectAnswer =
                String(opt).trim().toLowerCase() === String(currentQ.answer).trim().toLowerCase();

              let style = 'bg-[#151D2F] border-slate-800 text-slate-200 hover:border-cyan-500/60';
              if (isSelected) {
                style = 'bg-cyan-950 border-cyan-400 text-cyan-200 font-bold ring-1 ring-cyan-400';
              }
              if (isAnswered) {
                if (isCorrectAnswer) {
                  style = 'bg-emerald-950/90 border-emerald-400 text-emerald-200 font-bold ring-1 ring-emerald-400';
                } else if (isSelected && !isCorrectAnswer) {
                  style = 'bg-rose-950/90 border-rose-500 text-rose-300 line-through';
                } else {
                  style = 'bg-slate-900/40 border-slate-800 text-slate-600 opacity-60';
                }
              }

              return (
                <button
                  key={idx}
                  disabled={isAnswered}
                  onClick={() => handleSelectAnswer(opt)}
                  className={`w-full text-left p-4 rounded-2xl border transition-all text-xs sm:text-sm font-medium flex items-center justify-between ${style}`}
                >
                  <span>{String(opt)}</span>
                  {isAnswered && isCorrectAnswer && (
                    <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />
                  )}
                  {isAnswered && isSelected && !isCorrectAnswer && (
                    <XCircle className="w-5 h-5 text-rose-400 shrink-0" />
                  )}
                </button>
              );
            })}
          </div>

          {/* Explanation if answered */}
          {isAnswered && (
            <div className="p-4 rounded-2xl bg-slate-900/90 border border-slate-800 text-xs text-slate-300 leading-relaxed animate-fade-in">
              <span className="font-bold text-cyan-400">Explanation: </span>
              {currentQ.explanation}
            </div>
          )}
        </div>
      ) : (
        /* Quiz Complete Results */
        <div className="flex-1 flex flex-col justify-center items-center text-center p-6 bg-[#151D2F] border border-slate-800 rounded-3xl shadow-2xl my-4 space-y-4 animate-fade-in">
          <div className="w-20 h-20 rounded-3xl bg-gradient-to-tr from-cyan-500 to-indigo-600 flex items-center justify-center text-4xl shadow-xl shadow-cyan-500/25">
            🎉
          </div>

          <h2 className="text-2xl font-black text-white">Quiz Completed!</h2>

          <div className="text-4xl font-extrabold text-cyan-400">
            {score} / {questions.length}
          </div>

          <p className="text-xs text-slate-300 max-w-xs leading-relaxed">
            {score === questions.length
              ? 'Flawless performance! Your on-device cognitive model has registered heightened understanding.'
              : score >= 3
              ? 'Solid grasp of core concepts. Neural weights updated locally.'
              : 'Keep practicing! Review explanations and reinforce with Leitner flashcards.'}
          </p>

          <div className="grid grid-cols-2 gap-3 w-full pt-2">
            <div className="p-3 bg-slate-900 rounded-xl border border-slate-800">
              <div className="text-[10px] text-slate-400 font-bold uppercase">Accuracy</div>
              <div className="text-base font-extrabold text-emerald-400 mt-0.5">
                {Math.round((score / Math.max(1, questions.length)) * 100)}%
              </div>
            </div>
            <div className="p-3 bg-slate-900 rounded-xl border border-slate-800">
              <div className="text-[10px] text-slate-400 font-bold uppercase">XP Earned</div>
              <div className="text-base font-extrabold text-amber-400 mt-0.5">
                +{score * 15 + 20} XP
              </div>
            </div>
          </div>

          <button
            onClick={onBack}
            className="w-full py-3.5 rounded-2xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-black text-sm shadow-lg shadow-cyan-500/25 transition-all transform active:scale-95 mt-4"
          >
            Continue Learning
          </button>
        </div>
      )}

      {/* Bottom Action */}
      {!isFinished && isAnswered && (
        <button
          onClick={handleNextQuestion}
          className="w-full py-3.5 rounded-2xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 font-bold text-sm shadow-lg shadow-cyan-500/20 transition-all flex items-center justify-center space-x-2"
        >
          <span>{currentIndex === questions.length - 1 ? 'Finish Quiz' : 'Next Question'}</span>
          <ChevronRight className="w-4 h-4" />
        </button>
      )}
    </div>
  );
};
