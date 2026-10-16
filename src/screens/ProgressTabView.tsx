import React, { useState } from 'react';
import {
  User,
  PersonalizationProfile,
  QuizPerformanceStats,
  QuizAttempt,
  LeaderboardEntry,
  LeaderboardScope,
  LeaderboardSort,
  DigitalBadge,
} from '../types';
import { AppStore } from '../services/store';
import {
  TrendingUp,
  Award,
  Flame,
  Target,
  Brain,
  History,
  Users,
  ChevronRight,
  Shield,
  Sparkles,
  CheckCircle2,
} from 'lucide-react';
import {
  AreaChart,
  Area,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar,
} from 'recharts';

interface ProgressTabViewProps {
  user: User;
  profile: PersonalizationProfile;
  stats: QuizPerformanceStats;
  historyAttempts: QuizAttempt[];
  leaderboardEntries: LeaderboardEntry[];
  badges: DigitalBadge[];
  dailyStreak: number;
  longestStreak: number;
  onScopeChange: (scope: LeaderboardScope) => void;
  onSortChange: (sort: LeaderboardSort) => void;
  onToggleFriend: (targetUsername: string) => void;
}

export const ProgressTabView: React.FC<ProgressTabViewProps> = ({
  user,
  profile,
  stats,
  historyAttempts,
  leaderboardEntries,
  badges,
  dailyStreak,
  longestStreak,
  onScopeChange,
  onSortChange,
  onToggleFriend,
}) => {
  const [activeLeaderboardScope, setActiveLeaderboardScope] = useState<LeaderboardScope>('GLOBAL');
  const [activeLeaderboardSort, setActiveLeaderboardSort] = useState<LeaderboardSort>('STREAK');

  const handleScope = (scope: LeaderboardScope) => {
    setActiveLeaderboardScope(scope);
    onScopeChange(scope);
  };

  const handleSort = (sort: LeaderboardSort) => {
    setActiveLeaderboardSort(sort);
    onSortChange(sort);
  };

  // Trait data for Radar Chart
  const radarData = [
    { trait: 'Conscientiousness', value: Math.round(profile.conscientiousness.score * 100) },
    { trait: 'Motivation', value: Math.round(profile.motivation.score * 100) },
    { trait: 'Understanding', value: Math.round(profile.understanding.score * 100) },
    { trait: 'Engagement', value: Math.round(profile.engagement.score * 100) },
  ];

  // 30-day mastery trends synthetic/historical data curve
  const masteryTrendsData = [
    { day: 'Day 1', mastery: 42, baseline: 40 },
    { day: 'Day 5', mastery: 50, baseline: 42 },
    { day: 'Day 10', mastery: 58, baseline: 45 },
    { day: 'Day 15', mastery: 65, baseline: 48 },
    { day: 'Day 20', mastery: 74, baseline: 52 },
    { day: 'Day 25', mastery: 82, baseline: 55 },
    { day: 'Day 30', mastery: Math.round(stats.averageAccuracyPercent || 85), baseline: 58 },
  ];

  // 7-day weekly streak trend
  const weeklyStreakData = [
    { day: 'Mon', questions: 8, streak: 1 },
    { day: 'Tue', questions: 12, streak: 2 },
    { day: 'Wed', questions: 15, streak: 3 },
    { day: 'Thu', questions: 10, streak: 4 },
    { day: 'Fri', questions: 18, streak: 5 },
    { day: 'Sat', questions: 14, streak: dailyStreak },
    { day: 'Sun', questions: 20, streak: dailyStreak },
  ];

  return (
    <div className="max-w-md mx-auto px-4 py-4 pb-24 space-y-4">
      {/* 1. Cognitive Traits Summary Card with Radar Chart */}
      <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl relative overflow-hidden">
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center space-x-2">
            <div className="p-2 rounded-xl bg-cyan-500/15 text-cyan-400">
              <Brain className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">Cognitive Trait Matrix</h3>
              <p className="text-[11px] text-slate-400">On-device neural inference weights</p>
            </div>
          </div>
          <span className="text-[11px] text-cyan-400 bg-cyan-950 border border-cyan-800 px-2 py-0.5 rounded-full font-semibold">
            {profile.modelSource.split(' ')[0]}
          </span>
        </div>

        {/* Radar Chart */}
        <div className="w-full h-52 -my-2 flex items-center justify-center">
          <ResponsiveContainer width="100%" height="100%">
            <RadarChart cx="50%" cy="50%" outerRadius="65%" data={radarData}>
              <PolarGrid stroke="#334155" />
              <PolarAngleAxis dataKey="trait" stroke="#94A3B8" tick={{ fontSize: 10 }} />
              <PolarRadiusAxis angle={30} domain={[0, 100]} stroke="#475569" tick={{ fontSize: 9 }} />
              <Radar
                name="Trait Score"
                dataKey="value"
                stroke="#22D3EE"
                fill="#06B6D4"
                fillOpacity={0.4}
              />
            </RadarChart>
          </ResponsiveContainer>
        </div>

        {/* Strengths & Focus Areas */}
        <div className="grid grid-cols-2 gap-2 mt-2 pt-2 border-t border-slate-800 text-xs">
          <div className="p-2.5 rounded-xl bg-slate-900 border border-emerald-500/30">
            <div className="text-[10px] text-emerald-400 font-bold uppercase tracking-wider">
              Primary Strength
            </div>
            <div className="text-xs font-bold text-white mt-0.5">{profile.primaryStrength}</div>
          </div>
          <div className="p-2.5 rounded-xl bg-slate-900 border border-amber-500/30">
            <div className="text-[10px] text-amber-400 font-bold uppercase tracking-wider">
              Focus Area
            </div>
            <div className="text-xs font-bold text-white mt-0.5">{profile.focusArea}</div>
          </div>
        </div>

        {/* Actionable Tips */}
        {profile.tips && profile.tips.length > 0 && (
          <div className="mt-3 p-3 rounded-2xl bg-cyan-950/30 border border-cyan-800/40 text-xs text-slate-300 leading-relaxed">
            <span className="font-bold text-cyan-400">Personalized Tip: </span>
            {profile.tips[0]}
          </div>
        )}
      </div>

      {/* 2. 30-Day Mastery Trend Chart */}
      <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center space-x-2">
            <div className="p-2 rounded-xl bg-indigo-500/15 text-indigo-400">
              <TrendingUp className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">30-Day Mastery Trend</h3>
              <p className="text-[11px] text-slate-400">Adaptive curriculum progression curve</p>
            </div>
          </div>
          <div className="text-xs font-bold text-indigo-400 bg-indigo-950 border border-indigo-800 px-2.5 py-0.5 rounded-full">
            {Math.round(stats.averageAccuracyPercent || 85)}%
          </div>
        </div>

        <div className="w-full h-44 -ml-2">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={masteryTrendsData}>
              <defs>
                <linearGradient id="masteryColor" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#6366F1" stopOpacity={0.5} />
                  <stop offset="95%" stopColor="#6366F1" stopOpacity={0.0} />
                </linearGradient>
              </defs>
              <XAxis dataKey="day" stroke="#64748B" tick={{ fontSize: 10 }} />
              <YAxis stroke="#64748B" tick={{ fontSize: 10 }} domain={[30, 100]} />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#0F172A',
                  border: '1px solid #334155',
                  borderRadius: '12px',
                  fontSize: '11px',
                }}
              />
              <Area
                type="monotone"
                dataKey="mastery"
                stroke="#818CF8"
                strokeWidth={2.5}
                fillOpacity={1}
                fill="url(#masteryColor)"
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* 3. Daily Goals & Streak Card */}
      <div className="grid grid-cols-2 gap-3">
        <div className="bg-[#151D2F] border border-slate-800 rounded-2xl p-4 shadow-md">
          <div className="flex items-center space-x-2 text-amber-400 mb-2">
            <Flame className="w-4 h-4 fill-amber-500" />
            <span className="text-xs font-bold uppercase tracking-wider">Streak</span>
          </div>
          <div className="text-2xl font-black text-white">{dailyStreak} Days</div>
          <div className="text-[11px] text-slate-400 mt-1">Longest: {longestStreak} days</div>
        </div>

        <div className="bg-[#151D2F] border border-slate-800 rounded-2xl p-4 shadow-md">
          <div className="flex items-center space-x-2 text-cyan-400 mb-2">
            <Target className="w-4 h-4" />
            <span className="text-xs font-bold uppercase tracking-wider">Quizzes</span>
          </div>
          <div className="text-2xl font-black text-white">{stats.totalQuizzes}</div>
          <div className="text-[11px] text-slate-400 mt-1">{stats.totalQuestionsAnswered} answered</div>
        </div>
      </div>

      {/* 4. Leaderboard Section */}
      <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2">
            <div className="p-2 rounded-xl bg-amber-500/15 text-amber-400">
              <Award className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">Community Leaderboard</h3>
              <p className="text-[11px] text-slate-400">Real-time peer learning cohort</p>
            </div>
          </div>

          {/* Scope Toggle: Global vs Friends */}
          <div className="flex items-center bg-slate-900 border border-slate-800 p-0.5 rounded-xl text-xs">
            <button
              onClick={() => handleScope('GLOBAL')}
              className={`px-2.5 py-1 rounded-lg font-bold transition-all ${
                activeLeaderboardScope === 'GLOBAL'
                  ? 'bg-cyan-500 text-slate-950 shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Global
            </button>
            <button
              onClick={() => handleScope('FRIENDS')}
              className={`px-2.5 py-1 rounded-lg font-bold transition-all ${
                activeLeaderboardScope === 'FRIENDS'
                  ? 'bg-cyan-500 text-slate-950 shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Friends
            </button>
          </div>
        </div>

        {/* Sort Controls */}
        <div className="flex items-center gap-1.5 mb-3 text-[11px] font-semibold text-slate-400">
          <span>Sort:</span>
          {(['STREAK', 'SCORE', 'ACCURACY'] as LeaderboardSort[]).map(s => (
            <button
              key={s}
              onClick={() => handleSort(s)}
              className={`px-2 py-0.5 rounded-md border transition-all ${
                activeLeaderboardSort === s
                  ? 'bg-slate-800 border-cyan-500 text-cyan-300 font-bold'
                  : 'border-slate-800 text-slate-500 hover:text-slate-300'
              }`}
            >
              {s.toLowerCase()}
            </button>
          ))}
        </div>

        {/* Leaderboard Entries List */}
        <div className="space-y-2">
          {leaderboardEntries.slice(0, 7).map(entry => {
            const isUser = entry.isCurrentUser;

            return (
              <div
                key={entry.username}
                className={`p-3 rounded-2xl border transition-all flex items-center justify-between ${
                  isUser
                    ? 'bg-gradient-to-r from-cyan-950/60 to-slate-900 border-cyan-400 ring-1 ring-cyan-400/40 shadow-md'
                    : 'bg-slate-900/70 border-slate-800/80 hover:border-slate-700'
                }`}
              >
                <div className="flex items-center space-x-3">
                  <span
                    className={`w-6 text-center text-xs font-black ${
                      entry.rank === 1
                        ? 'text-yellow-400'
                        : entry.rank === 2
                        ? 'text-slate-300'
                        : entry.rank === 3
                        ? 'text-amber-500'
                        : 'text-slate-500'
                    }`}
                  >
                    #{entry.rank}
                  </span>
                  <div className="w-8 h-8 rounded-xl bg-slate-800 flex items-center justify-center text-base">
                    {entry.avatarEmoji}
                  </div>
                  <div>
                    <div className="flex items-center space-x-1.5">
                      <span className={`text-xs font-bold ${isUser ? 'text-cyan-300' : 'text-white'}`}>
                        {entry.displayName}
                      </span>
                      <span className="text-[9px] px-1.5 py-0.2 rounded bg-slate-800 text-slate-400">
                        {entry.tier}
                      </span>
                    </div>
                    <div className="text-[10px] text-slate-400">{entry.dominantTrait}</div>
                  </div>
                </div>

                <div className="flex items-center space-x-3">
                  <div className="text-right">
                    <div className="text-xs font-bold text-amber-400 flex items-center justify-end gap-1">
                      <Flame className="w-3 h-3 fill-amber-400" />
                      <span>{entry.dailyStreak}d</span>
                    </div>
                    <div className="text-[10px] text-slate-400">{entry.totalScore} pts</div>
                  </div>

                  {!isUser && (
                    <button
                      onClick={() => onToggleFriend(entry.displayName)}
                      className={`text-[10px] px-2 py-1 rounded-lg border font-semibold transition-colors ${
                        entry.isFriend
                          ? 'bg-indigo-950 border-indigo-600 text-indigo-300'
                          : 'bg-slate-800 border-slate-700 text-slate-400 hover:text-white'
                      }`}
                    >
                      {entry.isFriend ? 'Friend' : '+Add'}
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* 5. Digital Badges Showcase */}
      <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center space-x-2">
            <div className="p-2 rounded-xl bg-yellow-500/15 text-yellow-400">
              <Award className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">Digital Badges</h3>
              <p className="text-[11px] text-slate-400">
                {badges.filter(b => b.isUnlocked).length} of {badges.length} Unlocked
              </p>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-2.5">
          {badges.map(b => (
            <div
              key={b.id}
              className={`p-3 rounded-2xl border transition-all flex flex-col justify-between ${
                b.isUnlocked
                  ? 'bg-slate-900/90 border-slate-700 shadow-md'
                  : 'bg-slate-900/30 border-slate-800/60 opacity-50'
              }`}
            >
              <div className="flex items-start justify-between">
                <span className="text-2xl">{b.iconEmoji}</span>
                <span className="text-[9px] font-extrabold uppercase px-1.5 py-0.2 rounded bg-slate-800 text-slate-400">
                  {b.tier}
                </span>
              </div>
              <div className="mt-2">
                <div className="text-xs font-bold text-white truncate">{b.title}</div>
                <div className="text-[10px] text-slate-400 line-clamp-1">{b.description}</div>
              </div>
              <div className="mt-2 pt-1 border-t border-slate-800/60 text-[10px] font-semibold text-cyan-400 flex items-center justify-between">
                <span>{b.progressLabel}</span>
                {b.isUnlocked && <CheckCircle2 className="w-3 h-3 text-emerald-400" />}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 6. Quiz History Section */}
      <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl">
        <div className="flex items-center space-x-2 mb-3">
          <div className="p-2 rounded-xl bg-slate-800 text-slate-300">
            <History className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-white">Recent Quiz History</h3>
            <p className="text-[11px] text-slate-400">Interactive telemetry logs</p>
          </div>
        </div>

        <div className="space-y-2">
          {historyAttempts.length === 0 ? (
            <div className="text-xs text-slate-500 text-center py-4">No quiz attempts yet.</div>
          ) : (
            historyAttempts.slice(0, 5).map(att => (
              <div
                key={att.id}
                className="bg-slate-900/80 border border-slate-800 rounded-xl p-3 flex items-center justify-between text-xs"
              >
                <div>
                  <div className="font-bold text-white">{att.category}</div>
                  <div className="text-[11px] text-slate-400 mt-0.5">
                    {new Date(att.timestamp).toLocaleDateString()} · Level {att.difficultyLevel}
                  </div>
                </div>
                <div className="text-right">
                  <div className="font-extrabold text-cyan-400">
                    {att.score} / {att.totalQuestions}
                  </div>
                  <div className="text-[10px] text-slate-400">
                    {Math.round((att.score / Math.max(1, att.totalQuestions)) * 100)}% Acc
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};
