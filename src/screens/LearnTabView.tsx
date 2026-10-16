import React from 'react';
import { User, CategoryMastery, FlashcardItem, KnowledgeGraphData, PersonalizedActionPlan } from '../types';
import { VisualKnowledgeGraph } from '../components/VisualKnowledgeGraph';
import {
  BookOpen,
  Sparkles,
  Layers,
  Clock,
  MessageSquare,
  Play,
  ArrowRight,
  Brain,
  ShieldCheck,
  ChevronRight,
  Flame,
} from 'lucide-react';

interface LearnTabViewProps {
  user: User;
  masteries: CategoryMastery[];
  dueCards: FlashcardItem[];
  actionPlan: PersonalizedActionPlan;
  graphData: KnowledgeGraphData;
  onStartQuiz: (categoryName: string, categoryNumber: number, initialTier?: string) => void;
  onOpenFlashcards: () => void;
  onOpenFocus: () => void;
  onOpenQaChat: () => void;
}

export const LearnTabView: React.FC<LearnTabViewProps> = ({
  user,
  masteries,
  dueCards,
  actionPlan,
  graphData,
  onStartQuiz,
  onOpenFlashcards,
  onOpenFocus,
  onOpenQaChat,
}) => {
  return (
    <div className="max-w-md mx-auto px-4 py-4 pb-24 space-y-4">
      {/* 1. Quick Tool Shortcuts (Flashcards, Focus, Q&A) */}
      <div className="grid grid-cols-3 gap-2.5">
        <button
          onClick={onOpenFlashcards}
          className="p-3 rounded-2xl bg-[#151D2F] border border-slate-800 hover:border-cyan-500/50 transition-all flex flex-col items-center text-center group shadow-md"
        >
          <div className="p-2 rounded-xl bg-cyan-500/15 text-cyan-400 group-hover:scale-110 transition-transform">
            <Layers className="w-4 h-4" />
          </div>
          <span className="text-xs font-bold text-white mt-1.5">Flashcards</span>
          <span className="text-[10px] text-cyan-400 font-semibold">
            {dueCards.length} Due Now
          </span>
        </button>

        <button
          onClick={onOpenFocus}
          className="p-3 rounded-2xl bg-[#151D2F] border border-slate-800 hover:border-indigo-500/50 transition-all flex flex-col items-center text-center group shadow-md"
        >
          <div className="p-2 rounded-xl bg-indigo-500/15 text-indigo-400 group-hover:scale-110 transition-transform">
            <Clock className="w-4 h-4" />
          </div>
          <span className="text-xs font-bold text-white mt-1.5">Focus Mode</span>
          <span className="text-[10px] text-indigo-400 font-semibold">Pomodoro</span>
        </button>

        <button
          onClick={onOpenQaChat}
          className="p-3 rounded-2xl bg-[#151D2F] border border-slate-800 hover:border-amber-500/50 transition-all flex flex-col items-center text-center group shadow-md"
        >
          <div className="p-2 rounded-xl bg-amber-500/15 text-amber-400 group-hover:scale-110 transition-transform">
            <MessageSquare className="w-4 h-4" />
          </div>
          <span className="text-xs font-bold text-white mt-1.5">PLi AI Coach</span>
          <span className="text-[10px] text-amber-400 font-semibold">Curriculum</span>
        </button>
      </div>

      {/* 2. Due Leitner Flashcards Alert (if any) */}
      {dueCards.length > 0 && (
        <div className="bg-gradient-to-r from-cyan-950/80 via-slate-900 to-indigo-950/80 border border-cyan-500/40 rounded-2xl p-3.5 shadow-lg flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-9 h-9 rounded-xl bg-cyan-500/20 border border-cyan-400/30 flex items-center justify-center text-cyan-400">
              <Layers className="w-5 h-5" />
            </div>
            <div>
              <div className="text-xs font-bold text-white">
                {dueCards.length} Leitner Cards Due for Spaced Review
              </div>
              <div className="text-[11px] text-slate-300">
                Reinforce memory before synaptic decay sets in
              </div>
            </div>
          </div>
          <button
            onClick={onOpenFlashcards}
            className="px-3 py-1.5 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-xs shadow-md shadow-cyan-500/20 transition-all shrink-0"
          >
            Review Now
          </button>
        </div>
      )}

      {/* 3. Personalized Knowledge Reinforcement Recommendation Card */}
      <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl relative overflow-hidden">
        <div className="absolute top-0 right-0 w-32 h-32 bg-amber-500/10 rounded-full blur-2xl pointer-events-none" />

        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center space-x-2">
            <div className="p-1.5 rounded-lg bg-amber-500/15 text-amber-400">
              <Sparkles className="w-4 h-4" />
            </div>
            <span className="text-xs font-bold text-amber-400 uppercase tracking-wider">
              Adaptive Recommendation
            </span>
          </div>
          <span className="text-[11px] font-semibold text-slate-400 bg-slate-900 px-2.5 py-0.5 rounded-full border border-slate-800">
            Readiness {Math.round(actionPlan.cognitiveReadinessScore * 100)}%
          </span>
        </div>

        <div className="flex items-start justify-between gap-3">
          <div>
            <h3 className="text-lg font-extrabold text-white flex items-center gap-2">
              <span>{actionPlan.recommendedCategoryIcon}</span>
              <span>{actionPlan.recommendedCategory}</span>
              <span className="text-xs px-2 py-0.5 rounded-md bg-indigo-950 text-indigo-300 border border-indigo-800 font-semibold">
                Tier: {actionPlan.recommendedInitialTier}
              </span>
            </h3>
            <p className="text-xs text-slate-300 mt-1 leading-relaxed">
              {actionPlan.recommendationReason}
            </p>
          </div>
        </div>

        <button
          onClick={() =>
            onStartQuiz(
              actionPlan.recommendedCategory,
              actionPlan.recommendedCategoryNumber,
              actionPlan.recommendedInitialTier
            )
          }
          className="mt-4 w-full py-3 rounded-2xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-extrabold text-xs shadow-lg shadow-amber-500/20 transition-all transform active:scale-95 flex items-center justify-center space-x-2"
        >
          <Play className="w-4 h-4 fill-slate-950" />
          <span>Launch Adaptive Practice Session</span>
          <ArrowRight className="w-4 h-4" />
        </button>
      </div>

      {/* 4. Interactive Visual Knowledge Graph */}
      <VisualKnowledgeGraph
        graphData={graphData}
        onSelectNodeQuiz={(cat, num) => onStartQuiz(cat, num)}
      />

      {/* 5. Curriculum Category Grid */}
      <div className="space-y-3">
        <div className="flex items-center justify-between px-1">
          <h3 className="text-sm font-bold text-white flex items-center gap-2">
            <BookOpen className="w-4 h-4 text-cyan-400" />
            <span>Curriculum Categories</span>
          </h3>
          <span className="text-xs text-slate-400">7 Full-Stack Tracks</span>
        </div>

        <div className="space-y-2.5">
          {masteries.map((cat, idx) => {
            const catNumber = idx + 1;
            const isMastered = cat.accuracyPercent >= 90;

            return (
              <div
                key={cat.categoryName}
                className="bg-[#151D2F] border border-slate-800 hover:border-slate-700 rounded-2xl p-4 shadow-md transition-all flex items-center justify-between gap-3 group"
              >
                <div className="flex items-center space-x-3.5 min-w-0">
                  <div className="w-11 h-11 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-center text-xl shrink-0 group-hover:scale-105 transition-transform shadow-inner">
                    {cat.icon}
                  </div>
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-bold text-white truncate">
                        {cat.categoryName}
                      </span>
                      <span
                        className={`text-[10px] font-bold px-1.5 py-0.2 rounded-full border ${
                          isMastered
                            ? 'bg-emerald-950 text-emerald-300 border-emerald-700/60'
                            : cat.accuracyPercent >= 70
                            ? 'bg-cyan-950 text-cyan-300 border-cyan-700/60'
                            : 'bg-slate-900 text-slate-400 border-slate-800'
                        }`}
                      >
                        {cat.masteryLevel}
                      </span>
                    </div>

                    <div className="flex items-center gap-3 mt-1.5 text-xs text-slate-400">
                      <div className="w-24 bg-slate-800 h-1.5 rounded-full overflow-hidden">
                        <div
                          style={{ width: `${Math.min(100, cat.accuracyPercent)}%` }}
                          className={`h-full rounded-full ${
                            cat.accuracyPercent >= 75
                              ? 'bg-emerald-400'
                              : cat.accuracyPercent >= 50
                              ? 'bg-cyan-400'
                              : 'bg-amber-400'
                          }`}
                        />
                      </div>
                      <span className="text-[11px] font-semibold text-slate-300">
                        {Math.round(cat.accuracyPercent)}% Acc
                      </span>
                      <span className="text-[11px] text-slate-500">
                        {cat.totalAttempts} runs
                      </span>
                    </div>
                  </div>
                </div>

                <button
                  onClick={() => onStartQuiz(cat.categoryName, catNumber)}
                  className="px-3.5 py-2 rounded-xl bg-slate-900 hover:bg-cyan-500 border border-slate-700 hover:border-cyan-400 text-slate-200 hover:text-slate-950 font-bold text-xs transition-all flex items-center space-x-1 shrink-0"
                >
                  <Play className="w-3.5 h-3.5 fill-current" />
                  <span>Start</span>
                </button>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
