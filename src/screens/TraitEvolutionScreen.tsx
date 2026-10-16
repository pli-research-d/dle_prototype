import React from 'react';
import { User, PersonalizationProfile } from '../types';
import { ArrowLeft, TrendingUp, Sparkles, Brain, Award } from 'lucide-react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';

interface TraitEvolutionScreenProps {
  user: User;
  profile: PersonalizationProfile;
  onBack: () => void;
}

export const TraitEvolutionScreen: React.FC<TraitEvolutionScreenProps> = ({
  user,
  profile,
  onBack,
}) => {
  const evolutionData = [
    { session: 'Start', conscientiousness: 40, motivation: 55, understanding: 45, engagement: 50 },
    { session: 'Day 3', conscientiousness: 50, motivation: 62, understanding: 58, engagement: 65 },
    { session: 'Day 7', conscientiousness: 65, motivation: 70, understanding: 68, engagement: 74 },
    { session: 'Day 12', conscientiousness: 72, motivation: 75, understanding: 76, engagement: 80 },
    {
      session: 'Current',
      conscientiousness: Math.round(profile.conscientiousness.score * 100),
      motivation: Math.round(profile.motivation.score * 100),
      understanding: Math.round(profile.understanding.score * 100),
      engagement: Math.round(profile.engagement.score * 100),
    },
  ];

  return (
    <div className="min-h-screen bg-[#0B0F19] text-white flex flex-col justify-between p-4 max-w-md mx-auto space-y-4">
      {/* Top Header */}
      <div className="flex items-center justify-between py-2 border-b border-slate-800/80">
        <button
          onClick={onBack}
          className="p-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:text-white"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>

        <div className="text-center">
          <div className="text-xs font-bold text-slate-400 uppercase tracking-wider">
            Cognitive Growth
          </div>
          <div className="text-sm font-extrabold text-white">Trait Evolution Timeline</div>
        </div>

        <div className="p-2 text-cyan-400">
          <TrendingUp className="w-5 h-5" />
        </div>
      </div>

      <div className="space-y-4">
        {/* Evolution Chart */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl">
          <h3 className="text-sm font-bold text-white mb-1">Cognitive Trait Progression</h3>
          <p className="text-[11px] text-slate-400 mb-4">Historical growth curve across study milestones</p>

          <div className="w-full h-56 -ml-3">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={evolutionData}>
                <XAxis dataKey="session" stroke="#64748B" tick={{ fontSize: 10 }} />
                <YAxis stroke="#64748B" tick={{ fontSize: 10 }} domain={[30, 100]} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0F172A',
                    border: '1px solid #334155',
                    borderRadius: '12px',
                    fontSize: '11px',
                  }}
                />
                <Legend wrapperStyle={{ fontSize: '10px', paddingTop: '10px' }} />
                <Line type="monotone" dataKey="conscientiousness" stroke="#06B6D4" strokeWidth={2} name="Consistency" />
                <Line type="monotone" dataKey="motivation" stroke="#8B5CF6" strokeWidth={2} name="Motivation" />
                <Line type="monotone" dataKey="understanding" stroke="#10B981" strokeWidth={2} name="Understanding" />
                <Line type="monotone" dataKey="engagement" stroke="#F59E0B" strokeWidth={2} name="Engagement" />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Current Trait Scorecards */}
        <div className="grid grid-cols-2 gap-2.5">
          <div className="p-3.5 bg-[#151D2F] border border-cyan-500/30 rounded-2xl">
            <div className="text-[10px] text-cyan-400 font-bold uppercase">Consistency</div>
            <div className="text-xl font-extrabold text-white mt-0.5">
              {Math.round(profile.conscientiousness.score * 100)}%
            </div>
            <div className="text-[10px] text-slate-400 mt-1">{profile.conscientiousness.levelLabel}</div>
          </div>

          <div className="p-3.5 bg-[#151D2F] border border-purple-500/30 rounded-2xl">
            <div className="text-[10px] text-purple-400 font-bold uppercase">Motivation</div>
            <div className="text-xl font-extrabold text-white mt-0.5">
              {Math.round(profile.motivation.score * 100)}%
            </div>
            <div className="text-[10px] text-slate-400 mt-1">{profile.motivation.levelLabel}</div>
          </div>

          <div className="p-3.5 bg-[#151D2F] border border-emerald-500/30 rounded-2xl">
            <div className="text-[10px] text-emerald-400 font-bold uppercase">Understanding</div>
            <div className="text-xl font-extrabold text-white mt-0.5">
              {Math.round(profile.understanding.score * 100)}%
            </div>
            <div className="text-[10px] text-slate-400 mt-1">{profile.understanding.levelLabel}</div>
          </div>

          <div className="p-3.5 bg-[#151D2F] border border-amber-500/30 rounded-2xl">
            <div className="text-[10px] text-amber-400 font-bold uppercase">Engagement</div>
            <div className="text-xl font-extrabold text-white mt-0.5">
              {Math.round(profile.engagement.score * 100)}%
            </div>
            <div className="text-[10px] text-slate-400 mt-1">{profile.engagement.levelLabel}</div>
          </div>
        </div>
      </div>
    </div>
  );
};
