import React, { useState } from 'react';
import { User, ModelWeights } from '../types';
import { AppStore } from '../services/store';
import { OnDeviceTrainableModel } from '../ml/trainableModel';
import { triggerBadgeConfetti } from '../utils/confetti';
import {
  ArrowLeft,
  Cpu,
  Play,
  CheckCircle2,
  Sliders,
  Layers,
  Sparkles,
  RefreshCw,
  Save,
} from 'lucide-react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';

interface TrainingStudioScreenProps {
  user: User;
  onBack: () => void;
}

export const TrainingStudioScreen: React.FC<TrainingStudioScreenProps> = ({
  user,
  onBack,
}) => {
  const store = AppStore.getInstance();
  const currentWeights = store.getModelWeights(user.username) || OnDeviceTrainableModel.defaultInit(5, 8, 4);

  const [epochs, setEpochs] = useState(30);
  const [learningRate, setLearningRate] = useState(0.04);
  const [momentum, setMomentum] = useState(0.85);

  const [isTraining, setIsTraining] = useState(false);
  const [currentEpoch, setCurrentEpoch] = useState(0);
  const [currentLoss, setCurrentLoss] = useState(currentWeights.finalLoss || 0.28);
  const [lossData, setLossData] = useState<Array<{ epoch: number; loss: number }>>([
    { epoch: 0, loss: currentWeights.finalLoss || 0.28 },
  ]);

  // Forward pass test inputs
  const [testInputs, setTestInputs] = useState<[number, number, number, number, number]>([
    5, // logins
    15, // time minutes
    75, // quiz score
    2, // difficulty
    3, // category
  ]);
  const [testOutputs, setTestOutputs] = useState<number[] | null>(null);

  const handleStartTraining = async () => {
    setIsTraining(true);
    setCurrentEpoch(0);

    const model = new OnDeviceTrainableModel(5, 8, 4, currentWeights);
    const dataset = OnDeviceTrainableModel.generateSyntheticDataset(30);

    const chartPoints: Array<{ epoch: number; loss: number }> = [];

    const result = await model.train(
      dataset,
      epochs,
      learningRate,
      momentum,
      progress => {
        setCurrentEpoch(progress.epoch);
        setCurrentLoss(progress.loss);
        chartPoints.push({ epoch: progress.epoch, loss: Math.round(progress.loss * 1000) / 1000 });
        setLossData([...chartPoints]);
      }
    );

    setIsTraining(false);
    store.saveModelWeights(user.username, result.weights);
    triggerBadgeConfetti();
  };

  const handleTestInference = () => {
    const model = new OnDeviceTrainableModel(5, 8, 4, currentWeights);
    const norm = OnDeviceTrainableModel.normalize(testInputs);
    const res = model.forward(norm);
    setTestOutputs(res.y.map(v => Math.round(v * 100) / 100));
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
            Machine Learning Studio
          </div>
          <div className="text-sm font-extrabold text-white">On-Device Trainer</div>
        </div>

        <div className="p-2 text-cyan-400">
          <Cpu className="w-5 h-5" />
        </div>
      </div>

      <div className="space-y-4">
        {/* Model Spec Card */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-4 shadow-xl text-xs flex items-center justify-between">
          <div>
            <div className="text-[10px] text-slate-400 font-bold uppercase">Architecture</div>
            <div className="text-sm font-extrabold text-cyan-400 mt-0.5">
              5 Inputs → 8 Hidden (ReLU) → 4 Outputs (Sigmoid)
            </div>
            <div className="text-[11px] text-slate-400 mt-1">
              Trained Epochs: <strong className="text-white">{currentWeights.trainedEpochs}</strong> · Loss: <strong className="text-emerald-400">{Math.round(currentLoss * 1000) / 1000}</strong>
            </div>
          </div>
        </div>

        {/* Live Loss Curve Chart */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl">
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-sm font-bold text-white">Loss Convergence Curve</h3>
            <span className="text-xs font-mono text-cyan-400">MSE Loss: {currentLoss.toFixed(4)}</span>
          </div>

          <div className="w-full h-44 -ml-2">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={lossData}>
                <XAxis dataKey="epoch" stroke="#64748B" tick={{ fontSize: 10 }} />
                <YAxis stroke="#64748B" tick={{ fontSize: 10 }} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0F172A',
                    border: '1px solid #334155',
                    borderRadius: '12px',
                    fontSize: '11px',
                  }}
                />
                <Line
                  type="monotone"
                  dataKey="loss"
                  stroke="#22D3EE"
                  strokeWidth={2.5}
                  dot={false}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>

          {/* Hyperparameter Controls */}
          <div className="grid grid-cols-3 gap-2 mt-3 pt-3 border-t border-slate-800 text-xs">
            <div>
              <label className="text-[10px] text-slate-400 font-bold uppercase">Epochs</label>
              <select
                disabled={isTraining}
                value={epochs}
                onChange={e => setEpochs(Number(e.target.value))}
                className="w-full mt-1 bg-slate-900 border border-slate-800 rounded-xl p-1.5 text-xs text-white"
              >
                <option value={15}>15</option>
                <option value={30}>30</option>
                <option value={50}>50</option>
              </select>
            </div>

            <div>
              <label className="text-[10px] text-slate-400 font-bold uppercase">Learning Rate</label>
              <select
                disabled={isTraining}
                value={learningRate}
                onChange={e => setLearningRate(Number(e.target.value))}
                className="w-full mt-1 bg-slate-900 border border-slate-800 rounded-xl p-1.5 text-xs text-white"
              >
                <option value={0.02}>0.02</option>
                <option value={0.04}>0.04</option>
                <option value={0.08}>0.08</option>
              </select>
            </div>

            <div>
              <label className="text-[10px] text-slate-400 font-bold uppercase">Momentum</label>
              <select
                disabled={isTraining}
                value={momentum}
                onChange={e => setMomentum(Number(e.target.value))}
                className="w-full mt-1 bg-slate-900 border border-slate-800 rounded-xl p-1.5 text-xs text-white"
              >
                <option value={0.75}>0.75</option>
                <option value={0.85}>0.85</option>
                <option value={0.9}>0.90</option>
              </select>
            </div>
          </div>

          <button
            disabled={isTraining}
            onClick={handleStartTraining}
            className="w-full mt-4 py-3 rounded-2xl bg-gradient-to-r from-cyan-500 to-indigo-600 hover:from-cyan-400 hover:to-indigo-500 disabled:opacity-50 text-white font-extrabold text-xs shadow-lg shadow-cyan-500/25 transition-all flex items-center justify-center space-x-2"
          >
            {isTraining ? (
              <>
                <RefreshCw className="w-4 h-4 animate-spin" />
                <span>Training on local device... (Epoch {currentEpoch}/{epochs})</span>
              </>
            ) : (
              <>
                <Play className="w-4 h-4 fill-white" />
                <span>Run On-Device Training Loop</span>
              </>
            )}
          </button>
        </div>

        {/* Forward Pass Testing Panel */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl text-xs space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-white">Forward Pass Verification</h3>
            <button
              onClick={handleTestInference}
              className="px-3 py-1 bg-cyan-500 hover:bg-cyan-400 text-slate-950 rounded-xl font-bold text-xs"
            >
              Test Forward Pass
            </button>
          </div>

          <div className="grid grid-cols-5 gap-1.5 text-center">
            {[
              { label: 'Logins', val: testInputs[0], idx: 0 },
              { label: 'Time (m)', val: testInputs[1], idx: 1 },
              { label: 'Score %', val: testInputs[2], idx: 2 },
              { label: 'Diff', val: testInputs[3], idx: 3 },
              { label: 'Cat', val: testInputs[4], idx: 4 },
            ].map(item => (
              <div key={item.label} className="bg-slate-900 p-2 rounded-xl border border-slate-800">
                <div className="text-[9px] text-slate-400 truncate">{item.label}</div>
                <input
                  type="number"
                  value={item.val}
                  onChange={e => {
                    const next = [...testInputs] as [number, number, number, number, number];
                    next[item.idx] = Number(e.target.value);
                    setTestInputs(next);
                  }}
                  className="w-full bg-transparent text-center font-bold text-white text-xs mt-1 focus:outline-none"
                />
              </div>
            ))}
          </div>

          {testOutputs && (
            <div className="grid grid-cols-4 gap-2 pt-2 border-t border-slate-800 text-center animate-fade-in">
              <div className="bg-slate-900/80 p-2 rounded-xl border border-cyan-500/30">
                <div className="text-[9px] text-cyan-400 font-bold">Conscientious</div>
                <div className="text-xs font-bold text-white mt-0.5">{testOutputs[0]}</div>
              </div>
              <div className="bg-slate-900/80 p-2 rounded-xl border border-indigo-500/30">
                <div className="text-[9px] text-indigo-400 font-bold">Motivation</div>
                <div className="text-xs font-bold text-white mt-0.5">{testOutputs[1]}</div>
              </div>
              <div className="bg-slate-900/80 p-2 rounded-xl border border-emerald-500/30">
                <div className="text-[9px] text-emerald-400 font-bold">Understanding</div>
                <div className="text-xs font-bold text-white mt-0.5">{testOutputs[2]}</div>
              </div>
              <div className="bg-slate-900/80 p-2 rounded-xl border border-amber-500/30">
                <div className="text-[9px] text-amber-400 font-bold">Engagement</div>
                <div className="text-xs font-bold text-white mt-0.5">{testOutputs[3]}</div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
