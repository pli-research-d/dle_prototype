import React from 'react';
import { User, PersonalizationProfile, UserSettings } from '../types';
import {
  User as UserIcon,
  Cpu,
  Flame,
  Zap,
  Activity,
  Network,
  Shield,
  Layers,
  Settings,
  LogOut,
  Trash2,
  ChevronRight,
  Sparkles,
  ExternalLink,
  Sliders,
  TrendingUp,
} from 'lucide-react';

interface ProfileTabViewProps {
  user: User;
  profile: PersonalizationProfile;
  settings: UserSettings;
  onOpenTraining: () => void;
  onOpenFederated: () => void;
  onOpenDiagnostics: () => void;
  onOpenMoodleSandbox: () => void;
  onOpenUxTest: () => void;
  onOpenEvolution: () => void;
  onOpenSettings: () => void;
  onClearData: () => void;
  onLogout: () => void;
}

export const ProfileTabView: React.FC<ProfileTabViewProps> = ({
  user,
  profile,
  settings,
  onOpenTraining,
  onOpenFederated,
  onOpenDiagnostics,
  onOpenMoodleSandbox,
  onOpenUxTest,
  onOpenEvolution,
  onOpenSettings,
  onClearData,
  onLogout,
}) => {
  return (
    <div className="max-w-md mx-auto px-4 py-4 pb-24 space-y-4">
      {/* 1. User Account Card */}
      <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl relative overflow-hidden">
        <div className="flex items-center space-x-4">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-cyan-500 via-indigo-500 to-purple-600 flex items-center justify-center text-white text-2xl font-black shadow-xl shadow-cyan-500/20">
            {user.username.slice(0, 2).toUpperCase()}
          </div>
          <div>
            <div className="text-xs font-semibold text-cyan-400">Personal Intelligence Profile</div>
            <h2 className="text-lg font-black text-white">{user.username}</h2>
            <div className="text-xs text-slate-400 mt-0.5">
              Member since {new Date(user.createdAt).toLocaleDateString()}
            </div>
          </div>
        </div>

        {/* Neural Model Source badge */}
        <div className="mt-4 pt-3 border-t border-slate-800 flex items-center justify-between text-xs">
          <div className="flex items-center space-x-2 text-slate-300">
            <Cpu className="w-4 h-4 text-cyan-400" />
            <span>Active Model:</span>
          </div>
          <span className="font-bold text-cyan-300 bg-cyan-950 px-2.5 py-0.5 rounded-full border border-cyan-800">
            {profile.modelSource}
          </span>
        </div>

        <div className="mt-2 flex items-center justify-between text-xs text-slate-400">
          <span>Inference Latency:</span>
          <span className="font-semibold text-emerald-400">{profile.inferenceLatencyMs} ms (on-device)</span>
        </div>
      </div>

      {/* 2. Studio Labs & Research Sandbox Tools */}
      <div className="space-y-2">
        <div className="text-xs font-bold text-slate-400 uppercase tracking-wider px-1">
          Machine Learning Labs & Sandboxes
        </div>

        {/* On-Device Training Studio */}
        <button
          onClick={onOpenTraining}
          className="w-full bg-[#151D2F] hover:bg-slate-800 border border-slate-800 rounded-2xl p-4 flex items-center justify-between transition-all group shadow-md"
        >
          <div className="flex items-center space-x-3.5">
            <div className="p-2.5 rounded-xl bg-cyan-500/15 text-cyan-400 group-hover:scale-105 transition-transform">
              <Cpu className="w-5 h-5" />
            </div>
            <div className="text-left">
              <div className="text-sm font-bold text-white">On-Device Training Studio</div>
              <div className="text-xs text-slate-400">Train neural weights on local telemetry</div>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-slate-500 group-hover:text-cyan-400 transition-colors" />
        </button>

        {/* Federated Learning Lab */}
        <button
          onClick={onOpenFederated}
          className="w-full bg-[#151D2F] hover:bg-slate-800 border border-slate-800 rounded-2xl p-4 flex items-center justify-between transition-all group shadow-md"
        >
          <div className="flex items-center space-x-3.5">
            <div className="p-2.5 rounded-xl bg-purple-500/15 text-purple-400 group-hover:scale-105 transition-transform">
              <Network className="w-5 h-5" />
            </div>
            <div className="text-left">
              <div className="text-sm font-bold text-white">Federated Learning Lab</div>
              <div className="text-xs text-slate-400">Weight delta export & FedAvg simulation</div>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-slate-500 group-hover:text-purple-400 transition-colors" />
        </button>

        {/* Moodle LMS & Secure ML Sandbox */}
        <button
          onClick={onOpenMoodleSandbox}
          className="w-full bg-[#151D2F] hover:bg-slate-800 border border-slate-800 rounded-2xl p-4 flex items-center justify-between transition-all group shadow-md"
        >
          <div className="flex items-center space-x-3.5">
            <div className="p-2.5 rounded-xl bg-emerald-500/15 text-emerald-400 group-hover:scale-105 transition-transform">
              <Shield className="w-5 h-5" />
            </div>
            <div className="text-left">
              <div className="text-sm font-bold text-white">Moodle LMS & Secure Sandbox</div>
              <div className="text-xs text-slate-400">9-feature telemetry ingestion & tuning</div>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-slate-500 group-hover:text-emerald-400 transition-colors" />
        </button>

        {/* Model Diagnostics & Latency Profiler */}
        <button
          onClick={onOpenDiagnostics}
          className="w-full bg-[#151D2F] hover:bg-slate-800 border border-slate-800 rounded-2xl p-4 flex items-center justify-between transition-all group shadow-md"
        >
          <div className="flex items-center space-x-3.5">
            <div className="p-2.5 rounded-xl bg-amber-500/15 text-amber-400 group-hover:scale-105 transition-transform">
              <Activity className="w-5 h-5" />
            </div>
            <div className="text-left">
              <div className="text-sm font-bold text-white">Model Diagnostics & Latency</div>
              <div className="text-xs text-slate-400">Vector inspection & runtime profiling</div>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-slate-500 group-hover:text-amber-400 transition-colors" />
        </button>

        {/* Trait Evolution Timeline */}
        <button
          onClick={onOpenEvolution}
          className="w-full bg-[#151D2F] hover:bg-slate-800 border border-slate-800 rounded-2xl p-4 flex items-center justify-between transition-all group shadow-md"
        >
          <div className="flex items-center space-x-3.5">
            <div className="p-2.5 rounded-xl bg-indigo-500/15 text-indigo-400 group-hover:scale-105 transition-transform">
              <TrendingUp className="w-5 h-5" />
            </div>
            <div className="text-left">
              <div className="text-sm font-bold text-white">Trait Evolution Timeline</div>
              <div className="text-xs text-slate-400">Historical snapshots of cognitive growth</div>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-slate-500 group-hover:text-indigo-400 transition-colors" />
        </button>

        {/* UX Test Harness */}
        <button
          onClick={onOpenUxTest}
          className="w-full bg-[#151D2F] hover:bg-slate-800 border border-slate-800 rounded-2xl p-4 flex items-center justify-between transition-all group shadow-md"
        >
          <div className="flex items-center space-x-3.5">
            <div className="p-2.5 rounded-xl bg-rose-500/15 text-rose-400 group-hover:scale-105 transition-transform">
              <ExternalLink className="w-5 h-5" />
            </div>
            <div className="text-left">
              <div className="text-sm font-bold text-white">UX Test Harness View</div>
              <div className="text-xs text-slate-400">Original design iteration suite</div>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-slate-500 group-hover:text-rose-400 transition-colors" />
        </button>
      </div>

      {/* 3. Settings & Account Controls */}
      <div className="space-y-2 pt-2">
        <div className="text-xs font-bold text-slate-400 uppercase tracking-wider px-1">
          Preferences & Data
        </div>

        <button
          onClick={onOpenSettings}
          className="w-full bg-[#151D2F] hover:bg-slate-800 border border-slate-800 rounded-2xl p-3.5 flex items-center justify-between transition-all text-xs font-bold text-slate-200"
        >
          <div className="flex items-center space-x-3">
            <Settings className="w-4 h-4 text-slate-400" />
            <span>Settings & Wellbeing Caps</span>
          </div>
          <ChevronRight className="w-4 h-4 text-slate-500" />
        </button>

        <button
          onClick={() => {
            if (confirm('Are you sure you want to reset all local data?')) {
              onClearData();
            }
          }}
          className="w-full bg-[#151D2F] hover:bg-rose-950/40 border border-slate-800 hover:border-rose-800/60 rounded-2xl p-3.5 flex items-center justify-between transition-all text-xs font-bold text-rose-400"
        >
          <div className="flex items-center space-x-3">
            <Trash2 className="w-4 h-4 text-rose-500" />
            <span>Clear Local Storage & Reset</span>
          </div>
        </button>

        <button
          onClick={onLogout}
          className="w-full bg-[#151D2F] hover:bg-slate-800 border border-slate-800 rounded-2xl p-3.5 flex items-center justify-between transition-all text-xs font-bold text-slate-400 hover:text-white"
        >
          <div className="flex items-center space-x-3">
            <LogOut className="w-4 h-4" />
            <span>Sign Out ({user.username})</span>
          </div>
        </button>
      </div>
    </div>
  );
};
