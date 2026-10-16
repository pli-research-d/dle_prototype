import React, { useState } from 'react';
import { User } from '../types';
import { AppStore } from '../services/store';
import { Rocket, ShieldCheck, UserCheck, ArrowRight, Lock, User as UserIcon } from 'lucide-react';

interface AuthScreenProps {
  onLoginSuccess: (user: User) => void;
}

export const AuthScreen: React.FC<AuthScreenProps> = ({ onLoginSuccess }) => {
  const store = AppStore.getInstance();
  const [username, setUsername] = useState('demo_learner');
  const [password, setPassword] = useState('password123');
  const [isRegisterMode, setIsRegisterMode] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim()) {
      setError('Please provide a username.');
      return;
    }
    const user = store.login(username);
    onLoginSuccess(user);
  };

  const handleDemoLogin = () => {
    const user = store.login('demo_learner');
    onLoginSuccess(user);
  };

  return (
    <div className="min-h-screen bg-[#0B0F19] text-white flex flex-col justify-center px-4 py-8 max-w-md mx-auto">
      {/* Brand Hero */}
      <div className="text-center space-y-3 mb-8">
        <div className="w-16 h-16 mx-auto rounded-3xl bg-gradient-to-tr from-cyan-500 via-indigo-500 to-purple-600 flex items-center justify-center shadow-xl shadow-cyan-500/25">
          <Rocket className="w-8 h-8 text-white animate-bounce" />
        </div>
        <h1 className="text-3xl font-black tracking-tight text-white">PLi</h1>
        <p className="text-xs text-slate-400 max-w-xs mx-auto leading-relaxed">
          Personal Learning Intelligence with On-Device AI & Private Neural Telemetry
        </p>
      </div>

      {/* Auth Card */}
      <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-6 shadow-2xl relative overflow-hidden">
        <div className="flex items-center justify-between pb-4 border-b border-slate-800 mb-5">
          <button
            type="button"
            onClick={() => setIsRegisterMode(false)}
            className={`text-xs font-bold pb-1 transition-all ${
              !isRegisterMode ? 'text-cyan-400 border-b-2 border-cyan-400' : 'text-slate-500'
            }`}
          >
            Sign In
          </button>
          <button
            type="button"
            onClick={() => setIsRegisterMode(true)}
            className={`text-xs font-bold pb-1 transition-all ${
              isRegisterMode ? 'text-cyan-400 border-b-2 border-cyan-400' : 'text-slate-500'
            }`}
          >
            Create Account
          </button>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded-xl bg-rose-950/80 border border-rose-600/60 text-rose-300 text-xs font-semibold">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block mb-1">
              Username
            </label>
            <div className="relative">
              <UserIcon className="w-4 h-4 text-slate-500 absolute left-3.5 top-3.5" />
              <input
                type="text"
                value={username}
                onChange={e => setUsername(e.target.value)}
                placeholder="Username"
                className="w-full bg-[#0B0F19] border border-slate-800 focus:border-cyan-500 rounded-xl pl-10 pr-4 py-3 text-xs text-white placeholder-slate-600 focus:outline-none"
              />
            </div>
          </div>

          <div>
            <label className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block mb-1">
              Password
            </label>
            <div className="relative">
              <Lock className="w-4 h-4 text-slate-500 absolute left-3.5 top-3.5" />
              <input
                type="password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                placeholder="Password"
                className="w-full bg-[#0B0F19] border border-slate-800 focus:border-cyan-500 rounded-xl pl-10 pr-4 py-3 text-xs text-white placeholder-slate-600 focus:outline-none"
              />
            </div>
          </div>

          <button
            type="submit"
            className="w-full py-3.5 rounded-2xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 font-black text-xs shadow-lg shadow-cyan-500/25 transition-all transform active:scale-95 flex items-center justify-center space-x-2 mt-2"
          >
            <span>{isRegisterMode ? 'Create Account' : 'Sign In'}</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </form>

        <div className="mt-5 pt-4 border-t border-slate-800/80">
          <button
            type="button"
            onClick={handleDemoLogin}
            className="w-full py-3 rounded-2xl bg-slate-900 hover:bg-slate-800 border border-slate-700 text-cyan-300 font-bold text-xs flex items-center justify-center space-x-2 transition-all"
          >
            <UserCheck className="w-4 h-4" />
            <span>Continue as Demo Learner (1-Tap)</span>
          </button>
        </div>
      </div>

      {/* Privacy Guarantee Note */}
      <div className="mt-6 flex items-center justify-center space-x-2 text-[11px] text-slate-500">
        <ShieldCheck className="w-4 h-4 text-emerald-500" />
        <span>100% On-Device Neural Processing & Local Storage</span>
      </div>
    </div>
  );
};
