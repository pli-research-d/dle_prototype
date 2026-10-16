import React, { useState, useEffect } from 'react';
import { AppStore } from './services/store';
import {
  User,
  PersonalizationProfile,
  PersonalizedActionPlan,
  CategoryMastery,
  FlashcardItem,
  QuizPerformanceStats,
  UserStreakData,
  UserGamificationState,
  LeaderboardEntry,
  KnowledgeGraphData,
  DigitalBadge,
  UserSettings,
} from './types';
import { TFLiteEngine } from './ml/tfliteEngine';
import { KnowledgeGraphEngine } from './ml/knowledgeGraphEngine';
import { Header } from './components/Header';
import { BottomNavigation, TabRoute } from './components/BottomNavigation';
import { BadgeCelebrationModal } from './components/BadgeCelebrationModal';

// Screens
import { AuthScreen } from './screens/AuthScreen';
import { FeedScreen } from './screens/FeedScreen';
import { LearnTabView } from './screens/LearnTabView';
import { CompeteScreen } from './screens/CompeteScreen';
import { ProgressTabView } from './screens/ProgressTabView';
import { ProfileTabView } from './screens/ProfileTabView';
import { QuizScreen } from './screens/QuizScreen';
import { FlashcardReviewScreen } from './screens/FlashcardReviewScreen';
import { FocusSessionScreen } from './screens/FocusSessionScreen';
import { TrainingStudioScreen } from './screens/TrainingStudioScreen';
import { FederatedLabScreen } from './screens/FederatedLabScreen';
import { DiagnosticsScreen } from './screens/DiagnosticsScreen';
import { MoodleSandboxScreen } from './screens/MoodleSandboxScreen';
import { UxTestViewScreen } from './screens/UxTestViewScreen';
import { TraitEvolutionScreen } from './screens/TraitEvolutionScreen';
import { QaChatScreen } from './screens/QaChatScreen';
import { SettingsScreen } from './screens/SettingsScreen';

type ActiveView =
  | { type: 'main'; tab: TabRoute }
  | { type: 'quiz'; categoryName: string; categoryNumber: number; initialTier?: string }
  | { type: 'flashcards' }
  | { type: 'focus' }
  | { type: 'training' }
  | { type: 'federated' }
  | { type: 'diagnostics' }
  | { type: 'moodle' }
  | { type: 'ux_test' }
  | { type: 'evolution' }
  | { type: 'qa_chat' }
  | { type: 'settings' };

export const App: React.FC = () => {
  const store = AppStore.getInstance();

  const [currentUser, setCurrentUser] = useState<User | null>(() => store.getCurrentUser());
  const [activeView, setActiveView] = useState<ActiveView>({ type: 'main', tab: 'feed' });
  const [celebrationBadge, setCelebrationBadge] = useState<DigitalBadge | null>(null);

  // Reactive state derived from AppStore
  const [stateVersion, setStateVersion] = useState(0);

  useEffect(() => {
    const unsubscribe = store.subscribe(() => {
      setCurrentUser(store.getCurrentUser());
      setStateVersion(prev => prev + 1);
    });
    return unsubscribe;
  }, []);

  if (!currentUser) {
    return (
      <AuthScreen
        onLoginSuccess={user => {
          setCurrentUser(user);
          setActiveView({ type: 'main', tab: 'feed' });
        }}
      />
    );
  }

  // Derive all data for current user
  const uname = currentUser.username;
  const telemetry = store.getTelemetry(uname);
  const weights = store.getModelWeights(uname);
  const profile = TFLiteEngine.runInference(telemetry, weights);
  const masteries = store.getCategoryMasteryStats(uname);
  const dueCards = store.getDueFlashcards(uname);
  const stats = store.getQuizPerformanceStats(uname);
  const streakData = store.getStreakData(uname);
  const gamification = store.getGamification(uname);
  const settings = store.getSettings(uname);
  const historyAttempts = store.getAllQuizAttempts(uname);
  const actionPlan = TFLiteEngine.generateActionPlan(profile, masteries, dueCards.length);
  const graphData = KnowledgeGraphEngine.buildGraphFromTelemetry(masteries, historyAttempts);
  const { allBadges, newlyUnlocked } = store.getBadges(uname);

  // Check if any badge should celebrate
  const handleCheckBadges = () => {
    if (newlyUnlocked.length > 0 && !celebrationBadge) {
      setCelebrationBadge(newlyUnlocked[0]);
    }
  };

  const handleStartQuiz = (categoryName: string, categoryNumber: number, initialTier?: string) => {
    setActiveView({
      type: 'quiz',
      categoryName,
      categoryNumber,
      initialTier,
    });
  };

  return (
    <div className="min-h-screen bg-[#0B0F19] text-[#F1F5F9] selection:bg-cyan-500 selection:text-white">
      {/* BADGE CELEBRATION MODAL */}
      <BadgeCelebrationModal
        badge={celebrationBadge}
        onClose={() => setCelebrationBadge(null)}
      />

      {/* FULL-SCREEN MODALS */}
      {activeView.type === 'quiz' && (
        <QuizScreen
          user={currentUser}
          categoryName={activeView.categoryName}
          categoryNumber={activeView.categoryNumber}
          initialTier={activeView.initialTier}
          onBack={() => setActiveView({ type: 'main', tab: 'practice' })}
          onCompleted={() => {
            handleCheckBadges();
          }}
        />
      )}

      {activeView.type === 'flashcards' && (
        <FlashcardReviewScreen
          user={currentUser}
          onBack={() => setActiveView({ type: 'main', tab: 'practice' })}
        />
      )}

      {activeView.type === 'focus' && (
        <FocusSessionScreen
          user={currentUser}
          onBack={() => setActiveView({ type: 'main', tab: 'practice' })}
        />
      )}

      {activeView.type === 'training' && (
        <TrainingStudioScreen
          user={currentUser}
          onBack={() => setActiveView({ type: 'main', tab: 'profile' })}
        />
      )}

      {activeView.type === 'federated' && (
        <FederatedLabScreen
          user={currentUser}
          onBack={() => setActiveView({ type: 'main', tab: 'profile' })}
        />
      )}

      {activeView.type === 'diagnostics' && (
        <DiagnosticsScreen
          user={currentUser}
          onBack={() => setActiveView({ type: 'main', tab: 'profile' })}
        />
      )}

      {activeView.type === 'moodle' && (
        <MoodleSandboxScreen
          user={currentUser}
          onBack={() => setActiveView({ type: 'main', tab: 'profile' })}
        />
      )}

      {activeView.type === 'ux_test' && (
        <UxTestViewScreen
          onBack={() => setActiveView({ type: 'main', tab: 'profile' })}
        />
      )}

      {activeView.type === 'evolution' && (
        <TraitEvolutionScreen
          user={currentUser}
          profile={profile}
          onBack={() => setActiveView({ type: 'main', tab: 'profile' })}
        />
      )}

      {activeView.type === 'qa_chat' && (
        <QaChatScreen
          user={currentUser}
          onBack={() => setActiveView({ type: 'main', tab: 'practice' })}
        />
      )}

      {activeView.type === 'settings' && (
        <SettingsScreen
          user={currentUser}
          onBack={() => setActiveView({ type: 'main', tab: 'profile' })}
        />
      )}

      {/* MAIN 5-TAB DASHBOARD */}
      {activeView.type === 'main' && (
        <div className="flex flex-col min-h-screen">
          <Header
            user={currentUser}
            gamification={gamification}
            streakData={streakData}
            onOpenProfile={() => setActiveView({ type: 'main', tab: 'profile' })}
          />

          <main className="flex-1">
            {activeView.tab === 'feed' && (
              <FeedScreen
                user={currentUser}
                gamification={gamification}
                streakData={streakData}
                onOpenCompete={() => setActiveView({ type: 'main', tab: 'compete' })}
                onOpenQuiz={handleStartQuiz}
              />
            )}

            {activeView.tab === 'practice' && (
              <LearnTabView
                user={currentUser}
                masteries={masteries}
                dueCards={dueCards}
                actionPlan={actionPlan}
                graphData={graphData}
                onStartQuiz={handleStartQuiz}
                onOpenFlashcards={() => setActiveView({ type: 'flashcards' })}
                onOpenFocus={() => setActiveView({ type: 'focus' })}
                onOpenQaChat={() => setActiveView({ type: 'qa_chat' })}
              />
            )}

            {activeView.tab === 'compete' && (
              <CompeteScreen
                user={currentUser}
                gamification={gamification}
                onOpenFeed={() => setActiveView({ type: 'main', tab: 'feed' })}
              />
            )}

            {activeView.tab === 'progress' && (
              <ProgressTabView
                user={currentUser}
                profile={profile}
                stats={stats}
                historyAttempts={historyAttempts}
                leaderboardEntries={store.getLeaderboardEntries(uname)}
                badges={allBadges}
                dailyStreak={streakData.currentStreak}
                longestStreak={streakData.longestStreak}
                onScopeChange={scope => {
                  setStateVersion(prev => prev + 1);
                }}
                onSortChange={sort => {
                  setStateVersion(prev => prev + 1);
                }}
                onToggleFriend={target => {
                  store.toggleFriend(uname, target);
                }}
              />
            )}

            {activeView.tab === 'profile' && (
              <ProfileTabView
                user={currentUser}
                profile={profile}
                settings={settings}
                onOpenTraining={() => setActiveView({ type: 'training' })}
                onOpenFederated={() => setActiveView({ type: 'federated' })}
                onOpenDiagnostics={() => setActiveView({ type: 'diagnostics' })}
                onOpenMoodleSandbox={() => setActiveView({ type: 'moodle' })}
                onOpenUxTest={() => setActiveView({ type: 'ux_test' })}
                onOpenEvolution={() => setActiveView({ type: 'evolution' })}
                onOpenSettings={() => setActiveView({ type: 'settings' })}
                onClearData={() => store.clearAllData()}
                onLogout={() => store.logout()}
              />
            )}
          </main>

          <BottomNavigation
            currentTab={activeView.tab}
            onTabChange={tab => setActiveView({ type: 'main', tab })}
          />
        </div>
      )}
    </div>
  );
};

export default App;
