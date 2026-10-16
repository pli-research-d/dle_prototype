import React, { useEffect } from 'react';
import { DigitalBadge } from '../types';
import { triggerBadgeConfetti } from '../utils/confetti';
import { Sparkles, Award, X } from 'lucide-react';

interface BadgeCelebrationModalProps {
  badge: DigitalBadge | null;
  onClose: () => void;
}

export const BadgeCelebrationModal: React.FC<BadgeCelebrationModalProps> = ({
  badge,
  onClose,
}) => {
  useEffect(() => {
    if (badge) {
      triggerBadgeConfetti();
    }
  }, [badge]);

  if (!badge) return null;

  const tierColors: Record<string, string> = {
    BRONZE: 'from-amber-700 to-amber-900 border-amber-600/40 text-amber-200',
    SILVER: 'from-slate-400 to-slate-600 border-slate-400/40 text-slate-100',
    GOLD: 'from-yellow-500 to-amber-600 border-yellow-400/50 text-yellow-100',
    PLATINUM: 'from-cyan-400 to-indigo-600 border-cyan-400/50 text-cyan-100',
    DIAMOND: 'from-sky-400 to-blue-700 border-sky-400/60 text-sky-100',
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
      <div className="relative w-full max-w-sm bg-gradient-to-b from-[#1E293B] to-[#0F172A] border border-cyan-500/40 rounded-3xl p-6 text-center shadow-2xl overflow-hidden">
        {/* Glow backdrop */}
        <div className="absolute -top-16 -left-16 w-40 h-40 bg-cyan-500/20 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -bottom-16 -right-16 w-40 h-40 bg-indigo-500/20 rounded-full blur-3xl pointer-events-none" />

        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-white transition-colors"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="inline-flex items-center space-x-1.5 px-3 py-1 rounded-full bg-cyan-950/80 border border-cyan-500/30 text-cyan-400 text-xs font-semibold mb-4">
          <Sparkles className="w-3.5 h-3.5" />
          <span>Achievement Unlocked!</span>
        </div>

        {/* Badge Icon */}
        <div className="relative mx-auto my-3 w-24 h-24 rounded-2xl bg-gradient-to-tr from-slate-900 via-slate-800 to-slate-900 border-2 border-cyan-400/50 flex items-center justify-center shadow-xl shadow-cyan-500/20 animate-bounce">
          <span className="text-5xl">{badge.iconEmoji}</span>
          <div className="absolute -bottom-2 px-2.5 py-0.5 rounded-full bg-slate-900 border border-slate-700 text-[10px] font-bold uppercase tracking-wider text-cyan-300">
            {badge.tier}
          </div>
        </div>

        <h3 className="text-xl font-bold text-white mt-4">{badge.title}</h3>
        <p className="text-xs text-slate-300 mt-2 px-2 leading-relaxed">
          {badge.description}
        </p>

        <div className="mt-4 flex items-center justify-center gap-3">
          <div className="flex items-center space-x-1 px-3 py-1 rounded-xl bg-slate-900/80 border border-indigo-500/30 text-indigo-300 text-xs font-bold">
            <Award className="w-4 h-4 text-indigo-400" />
            <span>+{badge.xpReward} XP Earned</span>
          </div>
          <div className="text-xs text-slate-400 px-2 py-1 rounded-xl bg-slate-900/40">
            {badge.rarityText}
          </div>
        </div>

        <button
          onClick={onClose}
          className="mt-6 w-full py-3 rounded-2xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white font-semibold text-sm shadow-lg shadow-cyan-500/25 transition-all transform active:scale-95"
        >
          Claim & Continue
        </button>
      </div>
    </div>
  );
};
