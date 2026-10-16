import React, { useState } from 'react';
import { User, FlashcardItem } from '../types';
import { AppStore } from '../services/store';
import { ArrowLeft, Layers, RefreshCw, CheckCircle2, ChevronRight, Sparkles } from 'lucide-react';
import { triggerBadgeConfetti } from '../utils/confetti';

interface FlashcardReviewScreenProps {
  user: User;
  onBack: () => void;
}

export const FlashcardReviewScreen: React.FC<FlashcardReviewScreenProps> = ({
  user,
  onBack,
}) => {
  const store = AppStore.getInstance();
  const [cards, setCards] = useState<FlashcardItem[]>(() => {
    const due = store.getDueFlashcards(user.username);
    return due.length > 0 ? due : store.getAllFlashcards(user.username);
  });
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);
  const [reviewCompleted, setReviewCompleted] = useState(false);

  const currentCard = cards[currentIndex];

  const handleReview = (quality: 'hard' | 'good' | 'easy') => {
    if (!currentCard) return;

    store.reviewFlashcard(user.username, currentCard.id, quality);
    triggerBadgeConfetti();

    if (currentIndex < cards.length - 1) {
      setCurrentIndex(prev => prev + 1);
      setIsFlipped(false);
    } else {
      setReviewCompleted(true);
    }
  };

  return (
    <div className="min-h-screen bg-[#0B0F19] text-white flex flex-col justify-between p-4 max-w-md mx-auto">
      {/* Top Header */}
      <div className="flex items-center justify-between py-2 border-b border-slate-800/80 mb-4">
        <button
          onClick={onBack}
          className="p-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:text-white"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>

        <div className="text-center">
          <div className="text-xs font-bold text-slate-400 uppercase tracking-wider">
            Leitner Spaced Repetition
          </div>
          <div className="text-sm font-extrabold text-white">
            {cards.length > 0 ? `Card ${currentIndex + 1} of ${cards.length}` : 'Flashcards'}
          </div>
        </div>

        <div className="p-2 text-cyan-400">
          <Layers className="w-5 h-5" />
        </div>
      </div>

      {!reviewCompleted && currentCard ? (
        <div className="flex-1 flex flex-col justify-center space-y-4">
          {/* Box Level Indicator */}
          <div className="flex items-center justify-between px-1 text-xs">
            <span className="text-slate-400 font-semibold">{currentCard.category}</span>
            <span className="px-2.5 py-0.5 rounded-full bg-cyan-950 border border-cyan-800 text-cyan-300 font-bold">
              Box {currentCard.boxLevel} · {currentCard.boxLevel === 4 ? 'Mastered' : `${currentCard.boxLevel * 3}d interval`}
            </span>
          </div>

          {/* FLIP CARD CONTAINER */}
          <div
            onClick={() => setIsFlipped(prev => !prev)}
            className="w-full min-h-[300px] bg-[#151D2F] border border-slate-800 hover:border-slate-700 rounded-3xl p-6 shadow-2xl flex flex-col justify-between cursor-pointer transition-all transform active:scale-[0.99] select-none"
          >
            <div className="text-right text-[10px] uppercase font-bold text-slate-500 tracking-wider">
              {isFlipped ? 'Answer Revealed (Tap to flip back)' : 'Tap to reveal answer'}
            </div>

            <div className="my-auto text-center py-4">
              {!isFlipped ? (
                <div className="space-y-4">
                  <h3 className="text-lg font-bold text-white leading-relaxed">
                    {currentCard.question}
                  </h3>
                  <div className="text-xs text-cyan-400 font-medium">
                    🤔 What is the answer?
                  </div>
                </div>
              ) : (
                <div className="space-y-4 animate-fade-in">
                  <div className="text-xs text-emerald-400 font-bold uppercase tracking-wider">
                    Correct Answer
                  </div>
                  <div className="text-xl font-extrabold text-white">
                    {currentCard.correctAnswer}
                  </div>
                  <p className="text-xs text-slate-300 leading-relaxed max-w-xs mx-auto">
                    {currentCard.explanation}
                  </p>
                </div>
              )}
            </div>

            <div className="text-center text-xs text-slate-500">
              Reviewed {currentCard.reviewCount} times
            </div>
          </div>

          {/* RATING BUTTONS (Appears when flipped) */}
          {isFlipped ? (
            <div className="grid grid-cols-3 gap-2.5 pt-2 animate-fade-in">
              <button
                onClick={() => handleReview('hard')}
                className="py-3 rounded-2xl bg-rose-950/80 border border-rose-600/60 text-rose-300 font-bold text-xs hover:bg-rose-900 transition-all flex flex-col items-center"
              >
                <span>Hard</span>
                <span className="text-[10px] text-rose-400/80 font-normal">Box 1 (1d)</span>
              </button>

              <button
                onClick={() => handleReview('good')}
                className="py-3 rounded-2xl bg-slate-900 border border-slate-700 text-slate-200 font-bold text-xs hover:bg-slate-800 transition-all flex flex-col items-center"
              >
                <span>Good</span>
                <span className="text-[10px] text-slate-400 font-normal">Keep Box</span>
              </button>

              <button
                onClick={() => handleReview('easy')}
                className="py-3 rounded-2xl bg-emerald-950/80 border border-emerald-600/60 text-emerald-300 font-bold text-xs hover:bg-emerald-900 transition-all flex flex-col items-center"
              >
                <span>Easy</span>
                <span className="text-[10px] text-emerald-400/80 font-normal">+1 Box</span>
              </button>
            </div>
          ) : (
            <button
              onClick={() => setIsFlipped(true)}
              className="w-full py-3.5 rounded-2xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-sm shadow-md transition-all"
            >
              Flip Card
            </button>
          )}
        </div>
      ) : (
        /* Completion state */
        <div className="flex-1 flex flex-col justify-center items-center text-center p-6 bg-[#151D2F] border border-slate-800 rounded-3xl shadow-xl space-y-4 my-auto">
          <div className="text-5xl">✨</div>
          <h2 className="text-xl font-bold text-white">Spaced Review Complete!</h2>
          <p className="text-xs text-slate-300 max-w-xs leading-relaxed">
            All due cards reviewed according to Leitner intervals. Your long-term memory synaptic connections are strengthened!
          </p>
          <button
            onClick={onBack}
            className="w-full py-3 rounded-2xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-sm shadow-md transition-all mt-4"
          >
            Back to Practice
          </button>
        </div>
      )}
    </div>
  );
};
