import React, { useState } from 'react';
import { User } from '../types';
import { AppStore } from '../services/store';
import { ArrowLeft, Network, Shield, UploadCloud, RefreshCw, CheckCircle2, Lock } from 'lucide-react';
import { triggerBadgeConfetti } from '../utils/confetti';

interface FederatedLabScreenProps {
  user: User;
  onBack: () => void;
}

export const FederatedLabScreen: React.FC<FederatedLabScreenProps> = ({
  user,
  onBack,
}) => {
  const store = AppStore.getInstance();
  const [epsilon, setEpsilon] = useState(1.5);
  const [isExporting, setIsExporting] = useState(false);
  const [exportComplete, setExportComplete] = useState(false);
  const [deltaBytes, setDeltaBytes] = useState(256);

  const handleSimulateFederatedSync = () => {
    setIsExporting(true);
    setTimeout(() => {
      setIsExporting(false);
      setExportComplete(true);
      triggerBadgeConfetti();
    }, 1500);
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
            Research Sandbox
          </div>
          <div className="text-sm font-extrabold text-white">Federated Learning Lab</div>
        </div>

        <div className="p-2 text-purple-400">
          <Network className="w-5 h-5" />
        </div>
      </div>

      <div className="space-y-4">
        {/* Architecture overview card */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl text-xs space-y-2">
          <div className="flex items-center space-x-2 text-purple-400 font-bold">
            <Lock className="w-4 h-4" />
            <span>Differential Privacy & FedAvg</span>
          </div>
          <p className="text-slate-300 leading-relaxed">
            Local weights never leak raw quiz interactions. Only encrypted weight deltas with Laplace noise perturbation (ε = {epsilon}) are shared with the global aggregation node.
          </p>
        </div>

        {/* Delta Export Inspector */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl space-y-4">
          <h3 className="text-sm font-bold text-white">Local Weight Deltas Export</h3>

          <div className="space-y-2 text-xs">
            <div className="flex justify-between text-slate-300">
              <span>Differential Privacy Epsilon (ε):</span>
              <span className="font-bold text-cyan-400">{epsilon} (Laplace)</span>
            </div>
            <input
              type="range"
              min="0.5"
              max="3.0"
              step="0.1"
              value={epsilon}
              onChange={e => setEpsilon(Number(e.target.value))}
              className="w-full accent-cyan-400"
            />
            <div className="flex justify-between text-[10px] text-slate-500">
              <span>Higher Noise / Max Privacy (0.5)</span>
              <span>Lower Noise / Exact Gradients (3.0)</span>
            </div>
          </div>

          <div className="bg-[#0B0F19] rounded-2xl p-3 border border-slate-800 font-mono text-xs text-purple-300 space-y-1">
            <div className="text-slate-500">// Serialized Delta ByteBuffer</div>
            <div className="truncate">ΔW1: [0.0142, -0.0215, 0.0089, -0.0112...]</div>
            <div className="truncate">ΔW2: [0.0312, 0.0105, -0.0078, 0.0221...]</div>
            <div className="text-[10px] text-slate-500 pt-1">
              Payload size: {deltaBytes} bytes (Zero telemetry leakage)
            </div>
          </div>

          {exportComplete && (
            <div className="p-3 rounded-2xl bg-emerald-950/40 border border-emerald-500/40 text-emerald-200 text-xs flex items-center space-x-2">
              <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />
              <span>Weight deltas aggregated into Global Model v2.4</span>
            </div>
          )}

          <button
            disabled={isExporting}
            onClick={handleSimulateFederatedSync}
            className="w-full py-3.5 rounded-2xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 disabled:opacity-50 text-white font-bold text-xs shadow-lg shadow-purple-600/25 transition-all flex items-center justify-center space-x-2"
          >
            {isExporting ? (
              <>
                <RefreshCw className="w-4 h-4 animate-spin" />
                <span>Aggregating with Peer Nodes...</span>
              </>
            ) : (
              <>
                <UploadCloud className="w-4 h-4" />
                <span>Simulate Federated Aggregation (FedAvg)</span>
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};
