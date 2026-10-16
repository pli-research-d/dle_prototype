import React from 'react';
import { Rocket, BookOpen, Swords, TrendingUp, User as UserIcon } from 'lucide-react';

export type TabRoute = 'feed' | 'practice' | 'compete' | 'progress' | 'profile';

interface BottomNavigationProps {
  currentTab: TabRoute;
  onTabChange: (tab: TabRoute) => void;
}

export const BottomNavigation: React.FC<BottomNavigationProps> = ({
  currentTab,
  onTabChange,
}) => {
  const tabs: Array<{ id: TabRoute; label: string; icon: React.FC<{ className?: string }> }> = [
    { id: 'feed', label: 'Feed', icon: Rocket },
    { id: 'practice', label: 'Practice', icon: BookOpen },
    { id: 'compete', label: 'Compete', icon: Swords },
    { id: 'progress', label: 'Progress', icon: TrendingUp },
    { id: 'profile', label: 'Profile', icon: UserIcon },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-[#0F172A]/95 backdrop-blur-md border-t border-slate-800/80 px-2 py-1.5 shadow-2xl">
      <div className="max-w-md mx-auto flex items-center justify-around">
        {tabs.map(tab => {
          const isSelected = currentTab === tab.id;
          const Icon = tab.icon;
          return (
            <button
              key={tab.id}
              onClick={() => onTabChange(tab.id)}
              className={`flex flex-col items-center justify-center py-1 px-3 rounded-xl transition-all relative ${
                isSelected
                  ? 'text-cyan-400 font-semibold'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              {isSelected && (
                <div className="absolute -top-1.5 w-8 h-1 bg-cyan-400 rounded-full shadow-[0_0_8px_#22D3EE]" />
              )}
              <div
                className={`p-1.5 rounded-lg transition-transform ${
                  isSelected ? 'bg-cyan-500/15 scale-110' : ''
                }`}
              >
                <Icon className="w-5 h-5" />
              </div>
              <span className="text-[11px] mt-0.5 tracking-tight">{tab.label}</span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
