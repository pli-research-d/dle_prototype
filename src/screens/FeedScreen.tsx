import React, { useState, useEffect } from 'react';
import { User, UserGamificationState, UserStreakData, FeedCard } from '../types';
import { FeedCardsRepository } from '../ml/feedCardsRepository';
import { AppStore } from '../services/store';
import {
  Rocket,
  Flame,
  Fuel,
  Sparkles,
  Bug,
  HelpCircle,
  Code2,
  CheckCircle2,
  XCircle,
  ArrowRight,
  Gift,
  ChevronDown,
  RefreshCw,
  Swords,
} from 'lucide-react';
import { triggerBadgeConfetti } from '../utils/confetti';

interface FeedScreenProps {
  user: User;
  gamification: UserGamificationState;
  streakData: UserStreakData;
  onOpenCompete: () => void;
  onOpenQuiz: (categoryName: string, categoryNumber: number) => void;
}

export const FeedScreen: React.FC<FeedScreenProps> = ({
  user,
  gamification,
  streakData,
  onOpenCompete,
  onOpenQuiz,
}) => {
  const store = AppStore.getInstance();
  const [cards, setCards] = useState<FeedCard[]>([]);
  const [currentCardIndex, setCurrentCardIndex] = useState(0);
  const [selectedOption, setSelectedOption] = useState<string | null>(null);
  const [selectedBugLine, setSelectedBugLine] = useState<number | null>(null);
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [isCorrect, setIsCorrect] = useState(false);
  const [sessionXpEarned, setSessionXpEarned] = useState(0);

  useEffect(() => {
    loadCards();
  }, []);

  const loadCards = () => {
    const due = store.getDueFlashcards(user.username);
    const feed = FeedCardsRepository.getCuratedCards(due);
    setCards(feed);
    setCurrentCardIndex(0);
    resetCardState();
  };

  const resetCardState = () => {
    setSelectedOption(null);
    setSelectedBugLine(null);
    setIsSubmitted(false);
    setIsCorrect(false);
  };

  const currentCard = cards[currentCardIndex];

  const handleOptionSelect = (option: string) => {
    if (isSubmitted) return;
    setSelectedOption(option);
  };

  const handleBugLineSelect = (lineIndex: number) => {
    if (isSubmitted) return;
    setSelectedBugLine(lineIndex);
  };

  const handleSubmitAnswer = () => {
    if (!currentCard || isSubmitted) return;

    let correct = false;
    if (currentCard.type === 'SPOT_THE_BUG') {
      correct = selectedBugLine === currentCard.bugLineIndex;
    } else if (currentCard.type === 'MYSTERY_CARD' || currentCard.type === 'EXPLAINER_15S') {
      correct = true;
    } else {
      correct = selectedOption === currentCard.correctAnswer;
    }

    setIsCorrect(correct);
    setIsSubmitted(true);

    if (correct) {
      triggerBadgeConfetti();
      store.updateGamification(user.username, {
        totalXp: gamification.totalXp + currentCard.xpReward,
        fuel: gamification.fuel + currentCard.fuelReward,
        altitudeKm: Math.round((gamification.altitudeKm + currentCard.fuelReward * 0.08) * 10) / 10,
      });
      setSessionXpEarned(prev => prev + currentCard.xpReward);
    }
  };

  const handleNextCard = () => {
    if (currentCardIndex < cards.length - 1) {
      setCurrentCardIndex(prev => prev + 1);
      resetCardState();
    } else {
      loadCards();
    }
  };

  if (!currentCard) {
    return (
      <div className="flex flex-col items-center justify-center p-8 text-center min-h-[60vh]">
        <div className="animate-spin text-cyan-400 mb-3">
          <RefreshCw className="w-8 h-8" />
        </div>
        <p className="text-slate-400">Loading personalized micro-learning stream...</p>
      </div>
    );
  }

  return (
    <div className="max-w-md mx-auto px-4 py-4 pb-24 space-y-4">
      {/* Top Altitude / Rocket Progress Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-indigo-950/70 to-slate-900 border border-slate-800 rounded-2xl p-3.5 shadow-lg flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-xl bg-cyan-500/15 border border-cyan-500/30 flex items-center justify-center text-cyan-400 shadow-md shadow-cyan-500/10">
            <Rocket className="w-5 h-5 animate-pulse" />
          </div>
          <div>
            <div className="text-[11px] text-slate-400 font-semibold uppercase tracking-wider">
              Rocket Orbit Altitude
            </div>
            <div className="text-base font-extrabold text-white flex items-center gap-1.5">
              <span>{gamification.altitudeKm} km</span>
              <span className="text-[11px] font-normal text-cyan-400">· {gamification.selectedSkin}</span>
            </div>
          </div>
        </div>

        <button
          onClick={onOpenCompete}
          className="flex items-center space-x-1.5 px-3 py-1.5 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 text-xs font-bold shadow-md shadow-amber-500/20 transition-all transform active:scale-95"
        >
          <Swords className="w-3.5 h-3.5" />
          <span>Daily Duel</span>
        </button>
      </div>

      {/* Main Interactive Feed Card */}
      <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-2xl relative overflow-hidden transition-all">
        {/* Glow ambient light */}
        <div className="absolute top-0 right-0 w-32 h-32 bg-cyan-500/10 rounded-full blur-2xl pointer-events-none" />

        {/* Card Header metadata */}
        <div className="flex items-center justify-between mb-3.5">
          <div className="flex items-center space-x-2">
            <span className="text-xs font-bold px-2.5 py-1 rounded-full bg-cyan-950 text-cyan-300 border border-cyan-800/60 flex items-center gap-1">
              {currentCard.type === 'SPOT_THE_BUG' && <Bug className="w-3 h-3 text-rose-400" />}
              {currentCard.type === 'WHATS_THE_OUTPUT' && <Code2 className="w-3 h-3 text-cyan-400" />}
              {currentCard.type === 'MYSTERY_CARD' && <Gift className="w-3 h-3 text-amber-400" />}
              <span>{currentCard.category}</span>
            </span>
            <span className="text-xs text-slate-400 font-medium">
              Card {currentCardIndex + 1} of {cards.length}
            </span>
          </div>

          <div className="flex items-center space-x-2 text-xs font-bold text-amber-400">
            <Sparkles className="w-3.5 h-3.5 text-amber-400" />
            <span>+{currentCard.xpReward} XP</span>
          </div>
        </div>

        {/* Card Title */}
        <h2 className="text-base sm:text-lg font-bold text-white mb-2 leading-snug">
          {currentCard.title}
        </h2>

        {/* Prompt */}
        <p className="text-sm text-slate-300 mb-4 leading-relaxed font-normal">
          {currentCard.prompt}
        </p>

        {/* SPOT THE BUG INTERACTIVE CODE BLOCK */}
        {currentCard.type === 'SPOT_THE_BUG' && currentCard.bugLines && (
          <div className="my-3 bg-[#0B0F19] rounded-2xl border border-slate-800 overflow-hidden text-xs font-mono">
            <div className="px-3 py-1.5 bg-slate-900 border-b border-slate-800 text-[11px] text-slate-400 flex items-center justify-between">
              <span>source_inspect.ts</span>
              <span className="text-[10px] text-cyan-400">Tap faulty line</span>
            </div>
            <div className="p-2 space-y-1">
              {currentCard.bugLines.map((line, idx) => {
                const isSelected = selectedBugLine === idx;
                const isBug = idx === currentCard.bugLineIndex;

                let lineClass = 'hover:bg-slate-800/60 text-slate-300';
                if (isSelected) {
                  lineClass = 'bg-cyan-950/80 border border-cyan-500/60 text-cyan-200';
                }
                if (isSubmitted) {
                  if (isBug) {
                    lineClass = 'bg-rose-950/80 border border-rose-500 text-rose-200 font-bold';
                  } else if (isSelected && !isBug) {
                    lineClass = 'bg-slate-800/80 text-slate-500 line-through';
                  }
                }

                return (
                  <button
                    key={idx}
                    type="button"
                    disabled={isSubmitted}
                    onClick={() => handleBugLineSelect(idx)}
                    className={`w-full text-left px-3 py-1.5 rounded-lg flex items-center space-x-2.5 transition-all ${lineClass}`}
                  >
                    <span className="text-slate-500 select-none w-5 text-right font-semibold">
                      {idx + 1}
                    </span>
                    <span className="flex-1 truncate">{line}</span>
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {/* CODE SNIPPET (Non-interactive display) */}
        {currentCard.type !== 'SPOT_THE_BUG' && currentCard.codeSnippet && (
          <div className="my-3 bg-[#0B0F19] rounded-2xl border border-slate-800 p-3 text-xs font-mono text-cyan-300 overflow-x-auto shadow-inner">
            <pre className="whitespace-pre">{currentCard.codeSnippet}</pre>
          </div>
        )}

        {/* MULTIPLE CHOICE / OPTIONS BUTTONS */}
        {currentCard.options && currentCard.type !== 'SPOT_THE_BUG' && (
          <div className="space-y-2.5 my-4">
            {currentCard.options.map((opt, idx) => {
              const isSelected = selectedOption === opt;
              const isAnswer = opt === currentCard.correctAnswer;

              let btnStyle = 'bg-slate-900/90 border-slate-700/80 text-slate-200 hover:border-cyan-500/50';
              if (isSelected) {
                btnStyle = 'bg-cyan-950 border-cyan-400 text-cyan-200 font-semibold ring-1 ring-cyan-400';
              }
              if (isSubmitted) {
                if (isAnswer) {
                  btnStyle = 'bg-emerald-950/90 border-emerald-400 text-emerald-200 font-semibold ring-1 ring-emerald-400';
                } else if (isSelected && !isAnswer) {
                  btnStyle = 'bg-rose-950/90 border-rose-500 text-rose-300 line-through';
                } else {
                  btnStyle = 'bg-slate-900/50 border-slate-800 text-slate-500 opacity-60';
                }
              }

              return (
                <button
                  key={idx}
                  type="button"
                  disabled={isSubmitted}
                  onClick={() => handleOptionSelect(opt)}
                  className={`w-full text-left px-4 py-3 rounded-2xl border transition-all text-xs sm:text-sm flex items-center justify-between ${btnStyle}`}
                >
                  <span className="font-medium">{opt}</span>
                  {isSubmitted && isAnswer && (
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 ml-2" />
                  )}
                  {isSubmitted && isSelected && !isAnswer && (
                    <XCircle className="w-4 h-4 text-rose-400 shrink-0 ml-2" />
                  )}
                </button>
              );
            })}
          </div>
        )}

        {/* SUBMIT / NEXT ACTION BAR */}
        {!isSubmitted ? (
          <button
            onClick={handleSubmitAnswer}
            disabled={
              currentCard.type === 'SPOT_THE_BUG'
                ? selectedBugLine === null
                : currentCard.options && currentCard.options.length > 0
                ? selectedOption === null
                : false
            }
            className="w-full mt-4 py-3 rounded-2xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 disabled:opacity-40 disabled:cursor-not-allowed text-slate-950 font-bold text-sm shadow-lg shadow-cyan-500/20 transition-all transform active:scale-95 flex items-center justify-center space-x-2"
          >
            <span>Confirm Answer</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        ) : (
          <div className="mt-4 space-y-3 animate-fade-in">
            {/* Instant pedagogical explanation */}
            <div
              className={`p-3.5 rounded-2xl border text-xs leading-relaxed ${
                isCorrect
                  ? 'bg-emerald-950/40 border-emerald-500/40 text-emerald-200'
                  : 'bg-rose-950/40 border-rose-500/40 text-rose-200'
              }`}
            >
              <div className="font-bold flex items-center gap-1.5 mb-1">
                {isCorrect ? (
                  <>
                    <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                    <span>Brilliant! Correct reasoning.</span>
                  </>
                ) : (
                  <>
                    <XCircle className="w-4 h-4 text-rose-400" />
                    <span>Learning opportunity</span>
                  </>
                )}
              </div>
              <p className="text-slate-300 text-xs">{currentCard.explanation}</p>
            </div>

            <button
              onClick={handleNextCard}
              className="w-full py-3 rounded-2xl bg-gradient-to-r from-cyan-500 to-indigo-600 hover:from-cyan-400 hover:to-indigo-500 text-white font-bold text-sm shadow-lg shadow-cyan-500/25 transition-all transform active:scale-95 flex items-center justify-center space-x-2"
            >
              <span>Next Card</span>
              <ChevronDown className="w-4 h-4" />
            </button>
          </div>
        )}
      </div>

      {/* Quick Category Jump Bar */}
      <div className="bg-[#151D2F] border border-slate-800 rounded-2xl p-3 shadow-md">
        <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">
          Jump to Adaptive Quiz Topic
        </div>
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 text-xs">
          {[
            { name: 'JavaScript', num: 3, icon: '⚡' },
            { name: 'HTML', num: 1, icon: '🌐' },
            { name: 'CSS', num: 2, icon: '🎨' },
            { name: 'Python', num: 6, icon: '🐍' },
            { name: 'MySQL', num: 5, icon: '🗄️' },
            { name: 'C Language', num: 7, icon: '⚙️' },
          ].map(cat => (
            <button
              key={cat.name}
              onClick={() => onOpenQuiz(cat.name, cat.num)}
              className="flex items-center space-x-1 px-3 py-1.5 rounded-xl bg-slate-900 hover:bg-slate-800 border border-slate-800 text-slate-300 hover:text-cyan-400 shrink-0 font-medium transition-colors"
            >
              <span>{cat.icon}</span>
              <span>{cat.name}</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};
