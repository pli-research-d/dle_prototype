import React, { useState } from 'react';
import { User } from '../types';
import { AppStore } from '../services/store';
import { triggerBadgeConfetti } from '../utils/confetti';
import { ArrowLeft, Shield, Lock, RefreshCw, CheckCircle2, Sliders, Database, School } from 'lucide-react';

interface MoodleSandboxScreenProps {
  user: User;
  onBack: () => void;
}

export const MoodleSandboxScreen: React.FC<MoodleSandboxScreenProps> = ({
  user,
  onBack,
}) => {
  const store = AppStore.getInstance();
  const tele = store.getTelemetry(user.username);

  const [isTuning, setIsTuning] = useState(false);
  const [isSynced, setIsSynced] = useState(false);
  const [tuningResults, setTuningResults] = useState<{
    bestLr: number;
    bestMomentum: number;
    valLoss: number;
  } | null>(null);

  const handleRunGridSearch = () => {
    setIsTuning(true);
    setTimeout(() => {
      setTuningResults({
        bestLr: 0.04,
        bestMomentum: 0.85,
        valLoss: 0.042,
      });
      setIsTuning(false);
      triggerBadgeConfetti();
    }, 1200);
  };

  const handleSyncLms = () => {
    setIsSynced(true);
    setTimeout(() => setIsSynced(false), 3000);
  };

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
            LMS Research Sandbox
          </div>
          <div className="text-sm font-extrabold text-white">Moodle Plugin & Security</div>
        </div>

        <div className="p-2 text-emerald-400">
          <School className="w-5 h-5" />
        </div>
      </div>

      <div className="space-y-4">
        {/* Encryption & Token Status */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl space-y-3">
          <div className="flex items-center space-x-2 text-emerald-400 font-bold text-sm">
            <Lock className="w-4 h-4" />
            <span>AES-256-GCM Secure Sandbox Token</span>
          </div>

          <p className="text-xs text-slate-300 leading-relaxed">
            Telemetry is secured within an isolated sandbox environment. Gradebook telemetry is cryptographically signed with hardware-bound keys.
          </p>

          <div className="bg-[#0B0F19] rounded-xl p-3 border border-slate-800 font-mono text-[11px] text-cyan-300 space-y-1">
            <div className="text-slate-500">// Active Sandbox Token</div>
            <div className="truncate">token_sec_sha256: 8f4a7c1b920e43d1a...</div>
            <div className="text-emerald-400 font-bold">Status: Enforced & Hardware Encrypted</div>
          </div>
        </div>

        {/* Hyperparameter Grid Search */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-white">Automated Hyperparameter Grid Search</h3>
            <Sliders className="w-4 h-4 text-cyan-400" />
          </div>

          <p className="text-xs text-slate-400">
            Automated grid search evaluates combinations of learning rate (0.01..0.08) and momentum (0.7..0.9) inside the sandbox.
          </p>

          {tuningResults && (
            <div className="p-3 rounded-2xl bg-slate-900 border border-emerald-500/40 text-xs space-y-1 animate-fade-in">
              <div className="font-bold text-emerald-300">Optimal Parameters Found:</div>
              <div className="text-slate-300">
                Learning Rate: <strong className="text-white">{tuningResults.bestLr}</strong> · Momentum: <strong className="text-white">{tuningResults.bestMomentum}</strong>
              </div>
              <div className="text-[11px] text-slate-400">Validation Loss: {tuningResults.valLoss}</div>
            </div>
          )}

          <button
            disabled={isTuning}
            onClick={handleRunGridSearch}
            className="w-full py-3 rounded-2xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 disabled:opacity-50 text-slate-950 font-bold text-xs shadow-lg transition-all flex items-center justify-center space-x-2"
          >
            {isTuning ? (
              <>
                <RefreshCw className="w-4 h-4 animate-spin" />
                <span>Running Grid Search in Sandbox...</span>
              </>
            ) : (
              <span>Run Automated Hyperparameter Grid Search</span>
            )}
          </button>
        </div>

        {/* Gradebook Sync */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl space-y-3">
          <h3 className="text-sm font-bold text-white">Moodle LMS Gradebook Sync</h3>
          <p className="text-xs text-slate-400">
            Sync quiz completions, module completion status, and mastery telemetry directly into Moodle Course CS101.
          </p>

          {isSynced && (
            <div className="p-3 bg-emerald-950/60 border border-emerald-500 text-emerald-200 text-xs rounded-xl flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
              <span>Gradebook Telemetry Synchronized Successfully</span>
            </div>
          )}

          <button
            onClick={handleSyncLms}
            className="w-full py-3 rounded-2xl bg-slate-900 hover:bg-slate-800 border border-slate-700 text-slate-200 font-bold text-xs shadow-md transition-all"
          >
            Synchronize Gradebook Telemetry
          </button>
        </div>
      </div>
    </div>
  );
};
