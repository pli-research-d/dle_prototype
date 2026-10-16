import React from 'react';
import { User, UserGamificationState, UserStreakData } from '../types';
import { Flame, Fuel, Award, Sparkles } from 'lucide-react';

interface HeaderProps {
  user: User;
  gamification: UserGamificationState;
  streakData: UserStreakData;
  onOpenProfile: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  user,
  gamification,
  streakData,
  onOpenProfile,
}) => {
  return (
    <header className="sticky top-0 z-30 bg-[#0F172A]/90 backdrop-blur-md border-b border-slate-800 px-4 py-2.5 flex items-center justify-between shadow-lg">
      <div className="flex items-center space-x-2.5">
        <button
          onClick={onOpenProfile}
          className="flex items-center space-x-2 text-left group transition-all"
        >
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-cyan-500 to-indigo-600 flex items-center justify-center font-bold text-white shadow-md shadow-cyan-500/20 group-hover:scale-105 transition-transform">
            {user.username.slice(0, 2).toUpperCase()}
          </div>
          <div>
            <div className="text-xs text-slate-400 font-medium">PLi Engine</div>
            <div className="text-sm font-semibold text-slate-100 flex items-center gap-1">
              <span>{user.username}</span>
              <span className="text-[10px] bg-cyan-950 text-cyan-400 border border-cyan-800/60 rounded px-1.5 py-0.2">
                {gamification.currentLeague}
              </span>
            </div>
          </div>
        </button>
      </div>

      <div className="flex items-center space-x-2 sm:space-x-3 text-xs font-semibold">
        {/* Streak */}
        <div className="flex items-center space-x-1.5 bg-slate-900 border border-amber-500/30 text-amber-400 px-2.5 py-1.5 rounded-full shadow-inner">
          <Flame className="w-4 h-4 text-amber-500 fill-amber-500 animate-pulse" />
          <span>{streakData.currentStreak}d</span>
        </div>

        {/* Fuel */}
        <div className="flex items-center space-x-1.5 bg-slate-900 border border-cyan-500/30 text-cyan-400 px-2.5 py-1.5 rounded-full shadow-inner">
          <Fuel className="w-4 h-4 text-cyan-400" />
          <span>{gamification.fuel}</span>
        </div>

        {/* XP */}
        <div className="flex items-center space-x-1.5 bg-slate-900 border border-indigo-500/30 text-indigo-300 px-2.5 py-1.5 rounded-full shadow-inner">
          <Award className="w-4 h-4 text-indigo-400" />
          <span>{gamification.totalXp} XP</span>
        </div>
      </div>
    </header>
  );
};
