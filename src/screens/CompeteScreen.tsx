import React, { useState } from 'react';
import { User, UserGamificationState, Question } from '../types';
import { AppStore } from '../services/store';
import { triggerLevelUpConfetti } from '../utils/confetti';
import {
  Swords,
  Trophy,
  Shield,
  Zap,
  Clock,
  CheckCircle2,
  XCircle,
  Crown,
  ChevronRight,
  Flame,
  Award,
} from 'lucide-react';

interface CompeteScreenProps {
  user: User;
  gamification: UserGamificationState;
  onOpenFeed: () => void;
}

export const CompeteScreen: React.FC<CompeteScreenProps> = ({
  user,
  gamification,
  onOpenFeed,
}) => {
  const store = AppStore.getInstance();
  const allQuestions = store.getAllQuestions();

  const [duelActive, setDuelActive] = useState(false);
  const [currentRound, setCurrentRound] = useState(0);
  const [userScore, setUserScore] = useState(0);
  const [opponentScore, setOpponentScore] = useState(0);
  const [duelQuestions, setDuelQuestions] = useState<Question[]>([]);
  const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null);
  const [isAnswered, setIsAnswered] = useState(false);
  const [duelComplete, setDuelComplete] = useState(false);

  const virtualOpponents = [
    { name: 'MartianTiger67', avatar: '🐯', tier: 'Diamond', level: 42, trait: 'Consistent Speed' },
    { name: 'SnowyOwl3', avatar: '🦉', tier: 'Platinum', level: 38, trait: 'Deep Conceptual' },
    { name: 'CosmicFalcon42', avatar: '🦅', tier: 'Gold', level: 31, trait: 'Quick Reflexes' },
    { name: 'NeonPanda88', avatar: '🐼', tier: 'Silver', level: 25, trait: 'Syntax Specialist' },
  ];

  const [selectedOpponent, setSelectedOpponent] = useState(virtualOpponents[0]);

  const startDuel = (opponent: typeof virtualOpponents[0]) => {
    setSelectedOpponent(opponent);
    const shuffled = [...allQuestions].sort(() => 0.5 - Math.random()).slice(0, 3);
    setDuelQuestions(shuffled);
    setCurrentRound(0);
    setUserScore(0);
    setOpponentScore(0);
    setSelectedAnswer(null);
    setIsAnswered(false);
    setDuelComplete(false);
    setDuelActive(true);
  };

  const handleSelectAnswer = (ans: string) => {
    if (isAnswered) return;
    setSelectedAnswer(ans);
    setIsAnswered(true);

    const q = duelQuestions[currentRound];
    const isUserCorrect = String(ans).trim().toLowerCase() === String(q.answer).trim().toLowerCase();

    // Simulate opponent correctness based on tier probability
    const oppProb = selectedOpponent.tier === 'Diamond' ? 0.85 : 0.7;
    const isOppCorrect = Math.random() < oppProb;

    const nextUser = isUserCorrect ? userScore + 1 : userScore;
    const nextOpp = isOppCorrect ? opponentScore + 1 : opponentScore;

    setUserScore(nextUser);
    setOpponentScore(nextOpp);

    setTimeout(() => {
      if (currentRound < 2) {
        setCurrentRound(prev => prev + 1);
        setSelectedAnswer(null);
        setIsAnswered(false);
      } else {
        setDuelComplete(true);
        if (nextUser >= nextOpp) {
          triggerLevelUpConfetti();
          store.updateGamification(user.username, {
            totalXp: gamification.totalXp + 60,
            fuel: gamification.fuel + 15,
          });
        }
      }
    }, 1200);
  };

  const leagues = [
    { name: 'Diamond League', color: 'from-sky-400 to-blue-600', minXp: '1,500+ XP', active: gamification.currentLeague === 'Diamond' },
    { name: 'Platinum League', color: 'from-cyan-400 to-indigo-600', minXp: '1,000+ XP', active: gamification.currentLeague === 'Platinum' },
    { name: 'Gold League', color: 'from-yellow-400 to-amber-600', minXp: '600+ XP', active: gamification.currentLeague === 'Gold' },
    { name: 'Silver League', color: 'from-slate-300 to-slate-500', minXp: '300+ XP', active: gamification.currentLeague === 'Silver' },
    { name: 'Bronze League', color: 'from-amber-600 to-amber-800', minXp: '0+ XP', active: gamification.currentLeague === 'Bronze' },
  ];

  return (
    <div className="max-w-md mx-auto px-4 py-4 pb-24 space-y-4">
      {/* ACTIVE DUEL ARENA MODAL */}
      {duelActive && (
        <div className="bg-[#151D2F] border border-cyan-500/50 rounded-3xl p-5 shadow-2xl relative overflow-hidden animate-fade-in">
          {/* Header Scoreboard */}
          <div className="flex items-center justify-between pb-4 border-b border-slate-800">
            {/* User */}
            <div className="flex items-center space-x-2">
              <div className="w-9 h-9 rounded-xl bg-cyan-500/20 border border-cyan-500 flex items-center justify-center font-bold text-white text-xs">
                {user.username.slice(0, 2).toUpperCase()}
              </div>
              <div>
                <div className="text-xs font-bold text-white">You</div>
                <div className="text-sm font-extrabold text-cyan-400">{userScore} pts</div>
              </div>
            </div>

            {/* VS Badge & Round */}
            <div className="flex flex-col items-center">
              <span className="text-xs font-black px-2.5 py-0.5 rounded-full bg-slate-900 border border-slate-700 text-amber-400">
                VS · R{currentRound + 1}/3
              </span>
            </div>

            {/* Opponent */}
            <div className="flex items-center space-x-2 text-right">
              <div>
                <div className="text-xs font-bold text-white">{selectedOpponent.name}</div>
                <div className="text-sm font-extrabold text-rose-400">{opponentScore} pts</div>
              </div>
              <div className="w-9 h-9 rounded-xl bg-rose-500/20 border border-rose-500 flex items-center justify-center text-lg">
                {selectedOpponent.avatar}
              </div>
            </div>
          </div>

          {!duelComplete ? (
            <div className="mt-4 space-y-4">
              <div className="text-xs font-semibold text-slate-400">
                Category: <strong className="text-cyan-400">{duelQuestions[currentRound]?.category}</strong>
              </div>
              <p className="text-sm font-bold text-white leading-relaxed">
                {duelQuestions[currentRound]?.question}
              </p>

              {/* Options */}
              <div className="space-y-2 mt-4">
                {(
                  duelQuestions[currentRound]?.options || ['True', 'False']
                ).map((opt, idx) => {
                  const isSelected = selectedAnswer === opt;
                  const isCorrectAnswer =
                    String(opt).trim().toLowerCase() ===
                    String(duelQuestions[currentRound]?.answer).trim().toLowerCase();

                  let style = 'bg-slate-900 border-slate-800 text-slate-200 hover:border-cyan-500';
                  if (isAnswered) {
                    if (isCorrectAnswer) {
                      style = 'bg-emerald-950 border-emerald-400 text-emerald-200 font-bold';
                    } else if (isSelected && !isCorrectAnswer) {
                      style = 'bg-rose-950 border-rose-500 text-rose-300 line-through';
                    }
                  }

                  return (
                    <button
                      key={idx}
                      disabled={isAnswered}
                      onClick={() => handleSelectAnswer(opt)}
                      className={`w-full text-left p-3 rounded-2xl border text-xs sm:text-sm font-medium transition-all ${style}`}
                    >
                      {opt}
                    </button>
                  );
                })}
              </div>
            </div>
          ) : (
            <div className="mt-5 text-center space-y-3 py-2 animate-fade-in">
              <div className="text-4xl">
                {userScore > opponentScore ? '🏆' : userScore === opponentScore ? '🤝' : '⚔️'}
              </div>
              <h3 className="text-lg font-extrabold text-white">
                {userScore > opponentScore
                  ? 'Victory Claimed!'
                  : userScore === opponentScore
                  ? 'Stalemate Draw!'
                  : 'Defeat — Study and Rematch!'}
              </h3>
              <p className="text-xs text-slate-300">
                Final Score: You {userScore} - {opponentScore} {selectedOpponent.name}
              </p>
              {userScore >= opponentScore && (
                <div className="inline-flex items-center space-x-2 px-3 py-1 bg-amber-950 border border-amber-600/50 rounded-xl text-amber-300 text-xs font-bold">
                  <Award className="w-4 h-4 text-amber-400" />
                  <span>+60 XP & +15 Rocket Fuel Awarded</span>
                </div>
              )}
              <div className="pt-2">
                <button
                  onClick={() => setDuelActive(false)}
                  className="w-full py-2.5 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-xs shadow-md transition-all"
                >
                  Return to Duel Lobby
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Daily Duel Matchmaker Card */}
      {!duelActive && (
        <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl relative overflow-hidden">
          <div className="absolute top-0 right-0 w-36 h-36 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />

          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center space-x-2">
              <div className="p-2 rounded-xl bg-amber-500/15 text-amber-400">
                <Swords className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-white">Live Peer Duel Matchmaker</h3>
                <p className="text-[11px] text-slate-400">Fast 3-round real-time telemetry duel</p>
              </div>
            </div>
          </div>

          <div className="space-y-2 mt-4">
            {virtualOpponents.map((opp, idx) => (
              <div
                key={opp.name}
                className="bg-slate-900/90 border border-slate-800 hover:border-cyan-500/50 rounded-2xl p-3 flex items-center justify-between transition-all"
              >
                <div className="flex items-center space-x-3">
                  <div className="w-10 h-10 rounded-xl bg-slate-800 flex items-center justify-center text-xl shadow-inner">
                    {opp.avatar}
                  </div>
                  <div>
                    <div className="flex items-center space-x-1.5">
                      <span className="text-xs font-bold text-white">{opp.name}</span>
                      <span className="text-[9px] px-1.5 py-0.2 rounded bg-cyan-950 border border-cyan-800 text-cyan-300 font-semibold">
                        {opp.tier}
                      </span>
                    </div>
                    <div className="text-[11px] text-slate-400">{opp.trait}</div>
                  </div>
                </div>

                <button
                  onClick={() => startDuel(opp)}
                  className="px-3.5 py-1.5 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-bold text-xs shadow-md transition-all transform active:scale-95"
                >
                  Challenge
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Weekly League Standings Card */}
      <div className="bg-[#151D2F] border border-slate-800 rounded-3xl p-5 shadow-xl">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center space-x-2">
            <div className="p-2 rounded-xl bg-cyan-500/15 text-cyan-400">
              <Trophy className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">Weekly Season Leagues</h3>
              <p className="text-[11px] text-slate-400">Top 3 advance to the higher tier</p>
            </div>
          </div>
          <span className="text-xs font-extrabold text-cyan-400 bg-cyan-950 border border-cyan-800 px-2 py-0.5 rounded-full">
            Tier: {gamification.currentLeague}
          </span>
        </div>

        <div className="space-y-2 mt-4">
          {leagues.map(l => (
            <div
              key={l.name}
              className={`p-3 rounded-2xl border transition-all flex items-center justify-between ${
                l.active
                  ? 'bg-gradient-to-r from-slate-900 to-cyan-950/40 border-cyan-500 shadow-md ring-1 ring-cyan-500/40'
                  : 'bg-slate-900/60 border-slate-800/80 text-slate-400'
              }`}
            >
              <div className="flex items-center space-x-3">
                <Crown className={`w-4 h-4 ${l.active ? 'text-cyan-400' : 'text-slate-500'}`} />
                <span className={`text-xs font-bold ${l.active ? 'text-white' : 'text-slate-300'}`}>
                  {l.name}
                </span>
              </div>
              <span className="text-[11px] text-slate-400 font-medium">{l.minXp}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
