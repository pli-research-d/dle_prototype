import React, { useState } from 'react';
import { User, UserSettings } from '../types';
import { AppStore } from '../services/store';
import { ArrowLeft, Settings, Moon, Bell, Volume2, ShieldCheck, Check } from 'lucide-react';

interface SettingsScreenProps {
  user: User;
  onBack: () => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({ user, onBack }) => {
  const store = AppStore.getInstance();
  const [settings, setSettings] = useState<UserSettings>(() => store.getSettings(user.username));
  const [saved, setSaved] = useState(false);

  const handleUpdate = (updated: Partial<UserSettings>) => {
    const next = { ...settings, ...updated };
    setSettings(next);
    store.updateSettings(user.username, next);
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  const handleWellbeing = (updated: Partial<UserSettings['wellbeing']>) => {
    const next = {
      ...settings,
      wellbeing: { ...settings.wellbeing, ...updated },
    };
    setSettings(next);
    store.updateSettings(user.username, next);
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
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
            Preferences & Health
          </div>
          <div className="text-sm font-extrabold text-white">App Settings</div>
        </div>

        <div className="p-2 text-cyan-400">
          <Settings className="w-5 h-5" />
        </div>
      </div>

      <div className="space-y-4">
        {/* Wellbeing Time Cap */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl space-y-3">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-sm font-bold text-white">Daily Screen Time Cap</h3>
              <p className="text-[11px] text-slate-400">Gentle soft-stop notification to prevent fatigue</p>
            </div>
          </div>

          <div className="grid grid-cols-4 gap-2 pt-2 text-xs">
            {[0, 15, 30, 45, 60].slice(1).map(cap => (
              <button
                key={cap}
                onClick={() => handleWellbeing({ dailyTimeCapMinutes: cap })}
                className={`py-2 rounded-xl border font-bold transition-all ${
                  settings.wellbeing.dailyTimeCapMinutes === cap
                    ? 'bg-cyan-500 text-slate-950 border-cyan-400 shadow-md'
                    : 'bg-slate-900 border-slate-800 text-slate-400 hover:text-white'
                }`}
              >
                {cap}m
              </button>
            ))}
          </div>
        </div>

        {/* Learning Pacing Style */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl space-y-3">
          <h3 className="text-sm font-bold text-white">Cognitive Learning Style</h3>
          <p className="text-[11px] text-slate-400">Directs the adaptive quiz selection weights</p>

          <div className="space-y-2 pt-1 text-xs">
            {['Adaptive Dynamic', 'Gentle Paced', 'High Challenge'].map(style => (
              <button
                key={style}
                onClick={() => handleWellbeing({ learningStyle: style })}
                className={`w-full p-3 rounded-2xl border text-left font-bold flex items-center justify-between transition-all ${
                  settings.wellbeing.learningStyle === style
                    ? 'bg-indigo-950/80 border-indigo-500 text-indigo-200 ring-1 ring-indigo-400'
                    : 'bg-slate-900 border-slate-800 text-slate-300 hover:border-slate-700'
                }`}
              >
                <span>{style}</span>
                {settings.wellbeing.learningStyle === style && (
                  <Check className="w-4 h-4 text-indigo-400" />
                )}
              </button>
            ))}
          </div>
        </div>

        {/* Notifications & Audio */}
        <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl space-y-3 text-xs">
          <h3 className="text-sm font-bold text-white">Audio & Reminders</h3>

          <div className="flex items-center justify-between py-2 border-b border-slate-800/80">
            <div>
              <div className="font-bold text-white">Streak Reminders</div>
              <div className="text-[10px] text-slate-400">Daily alert before streak expires</div>
            </div>
            <input
              type="checkbox"
              checked={settings.wellbeing.softStopReminderEnabled}
              onChange={e => handleWellbeing({ softStopReminderEnabled: e.target.checked })}
              className="w-4 h-4 accent-cyan-400 rounded"
            />
          </div>

          <div className="flex items-center justify-between py-2">
            <div>
              <div className="font-bold text-white">Quiz Audio Effects</div>
              <div className="text-[10px] text-slate-400">Audio feedback on answers & badges</div>
            </div>
            <input
              type="checkbox"
              checked={settings.wellbeing.soundEnabled}
              onChange={e => handleWellbeing({ soundEnabled: e.target.checked })}
              className="w-4 h-4 accent-cyan-400 rounded"
            />
          </div>
        </div>

        {saved && (
          <div className="p-3 bg-emerald-950/80 border border-emerald-500/60 rounded-xl text-emerald-300 text-xs text-center font-bold animate-fade-in">
            Preferences Saved Locally
          </div>
        )}
      </div>
    </div>
  );
};
