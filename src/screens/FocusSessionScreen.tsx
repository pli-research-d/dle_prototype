import React, { useState, useEffect } from 'react';
import { User } from '../types';
import { AppStore } from '../services/store';
import { ArrowLeft, Play, Pause, RotateCcw, Clock, ShieldCheck, Sparkles } from 'lucide-react';
import { triggerBadgeConfetti } from '../utils/confetti';

interface FocusSessionScreenProps {
  user: User;
  onBack: () => void;
}

export const FocusSessionScreen: React.FC<FocusSessionScreenProps> = ({
  user,
  onBack,
}) => {
  const store = AppStore.getInstance();
  const [targetMinutes, setTargetMinutes] = useState(25);
  const [secondsRemaining, setSecondsRemaining] = useState(25 * 60);
  const [isRunning, setIsRunning] = useState(false);
  const [pauseCount, setPauseCount] = useState(0);
  const [isCompleted, setIsCompleted] = useState(false);

  useEffect(() => {
    let timer: any = null;
    if (isRunning && secondsRemaining > 0) {
      timer = setInterval(() => {
        setSecondsRemaining(prev => prev - 1);
      }, 1000);
    } else if (secondsRemaining === 0 && isRunning) {
      handleComplete();
    }
    return () => clearInterval(timer);
  }, [isRunning, secondsRemaining]);

  const handleToggleTimer = () => {
    if (isRunning) {
      setPauseCount(prev => prev + 1);
      setIsRunning(false);
    } else {
      setIsRunning(true);
    }
  };

  const handleReset = () => {
    setIsRunning(false);
    setSecondsRemaining(targetMinutes * 60);
    setPauseCount(0);
    setIsCompleted(false);
  };

  const handleChangeDuration = (mins: number) => {
    if (isRunning) return;
    setTargetMinutes(mins);
    setSecondsRemaining(mins * 60);
    setPauseCount(0);
  };

  const handleComplete = () => {
    setIsRunning(false);
    setIsCompleted(true);
    triggerBadgeConfetti();

    // Focus score: starts at 1.0, degrades 0.05 per pause
    const score = Math.max(0.6, 1.0 - pauseCount * 0.05);

    store.recordStudySession({
      username: user.username,
      durationSeconds: targetMinutes * 60,
      targetMinutes,
      pauseCount,
      focusScore: Math.round(score * 100) / 100,
    });
  };

  const mins = Math.floor(secondsRemaining / 60);
  const secs = secondsRemaining % 60;
  const progressPercent = ((targetMinutes * 60 - secondsRemaining) / (targetMinutes * 60)) * 100;

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
            Deep Focus Mode
          </div>
          <div className="text-sm font-extrabold text-white">Pomodoro Focus Block</div>
        </div>

        <div className="p-2 text-indigo-400">
          <Clock className="w-5 h-5" />
        </div>
      </div>

      {!isCompleted ? (
        <div className="flex-1 flex flex-col items-center justify-center space-y-6">
          {/* Target duration selector */}
          <div className="flex items-center space-x-2 bg-slate-900/80 border border-slate-800 p-1 rounded-2xl">
            {[15, 25, 45, 60].map(m => (
              <button
                key={m}
                disabled={isRunning}
                onClick={() => handleChangeDuration(m)}
                className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
                  targetMinutes === m
                    ? 'bg-indigo-600 text-white shadow-md'
                    : 'text-slate-400 hover:text-white disabled:opacity-50'
                }`}
              >
                {m}m
              </button>
            ))}
          </div>

          {/* Large Countdown Display Ring */}
          <div className="relative w-64 h-64 rounded-full border-4 border-slate-800 flex flex-col items-center justify-center shadow-2xl bg-[#151D2F]">
            {/* SVG progress circle */}
            <svg className="absolute inset-0 w-full h-full -rotate-90">
              <circle
                cx="128"
                cy="128"
                r="120"
                stroke="#6366F1"
                strokeWidth="6"
                fill="none"
                strokeDasharray={754}
                strokeDashoffset={754 - (754 * progressPercent) / 100}
                className="transition-all duration-1000 ease-linear"
              />
            </svg>

            <div className="text-5xl font-mono font-black text-white tracking-wider">
              {mins.toString().padStart(2, '0')}:{secs.toString().padStart(2, '0')}
            </div>

            <div className="text-xs text-indigo-300 font-semibold mt-2">
              {isRunning ? 'Session Active · Stay Focused' : 'Paused / Ready'}
            </div>

            {pauseCount > 0 && (
              <div className="text-[10px] text-amber-400 mt-1">
                {pauseCount} {pauseCount === 1 ? 'pause' : 'pauses'} recorded
              </div>
            )}
          </div>

          {/* Controls */}
          <div className="flex items-center space-x-4 pt-4">
            <button
              onClick={handleReset}
              className="p-3.5 rounded-2xl bg-slate-900 border border-slate-800 hover:border-slate-700 text-slate-300"
            >
              <RotateCcw className="w-5 h-5" />
            </button>

            <button
              onClick={handleToggleTimer}
              className={`px-8 py-3.5 rounded-2xl font-black text-sm flex items-center space-x-2 shadow-lg transition-all transform active:scale-95 ${
                isRunning
                  ? 'bg-amber-500 hover:bg-amber-400 text-slate-950 shadow-amber-500/20'
                  : 'bg-indigo-600 hover:bg-indigo-500 text-white shadow-indigo-600/30'
              }`}
            >
              {isRunning ? (
                <>
                  <Pause className="w-5 h-5 fill-current" />
                  <span>Pause Focus</span>
                </>
              ) : (
                <>
                  <Play className="w-5 h-5 fill-current" />
                  <span>{secondsRemaining === targetMinutes * 60 ? 'Start Focus' : 'Resume Focus'}</span>
                </>
              )}
            </button>
          </div>
        </div>
      ) : (
        /* Completed Focus Session */
        <div className="flex-1 flex flex-col justify-center items-center text-center p-6 bg-[#151D2F] border border-slate-800 rounded-3xl shadow-xl space-y-4 my-auto animate-fade-in">
          <div className="text-5xl">🧘‍♂️</div>
          <h2 className="text-xl font-bold text-white">Focus Session Complete!</h2>
          <p className="text-xs text-slate-300 max-w-xs leading-relaxed">
            You completed {targetMinutes} minutes of undistracted study with {pauseCount} pauses.
          </p>
          <div className="p-3 rounded-xl bg-slate-900 border border-indigo-500/30 text-indigo-300 text-xs font-bold">
            Focus Score: {Math.max(60, Math.round((1.0 - pauseCount * 0.05) * 100))}% · XP Awarded
          </div>
          <button
            onClick={onBack}
            className="w-full py-3 rounded-2xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-sm shadow-md transition-all mt-4"
          >
            Return
          </button>
        </div>
      )}
    </div>
  );
};
