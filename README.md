# PLi: Personal Learning Intelligence with On-Device AI

PLi is a Personalized Digital Learning Environment (DLE) powered by an on-device Private Learning Intelligence (PLI) framework rewritten as a high-performance React application.

The client leverages **on-device neural network inference and training**, **dynamic difficulty adjustment (DDA)**, **topological knowledge graph mastery**, and **federated-style aggregation hooks** to deliver real-time personalized learning while keeping all user telemetry local.

---

## Key Features Ported

### 1. On-Device Machine Learning & Personalization
- **2-Layer Neural Engine**: Normalizes user telemetry across 5 core dimensions (logins, study time, quiz score, difficulty reached, category) to predict four psychological learning traits:
  - **Conscientiousness**: Habit consistency & review rhythm
  - **Motivation**: Willingness to engage with challenging problems
  - **Understanding**: Conceptual retention & accuracy
  - **Engagement**: Interactive responsiveness & quiz tempo
- **On-Device Training Studio**: Train neural weights directly in the client using Momentum Gradient Descent, monitor real-time loss convergence curves, inspect weight matrices, and test forward-pass predictions.
- **Federated Learning Lab**: Simulate client weight delta exports (`exportWeights`), differential privacy Laplace noise injection, and federated model averaging (`FedAvg`).

### 2. Multi-Tab Core Navigation
1. **Feed**: Micro-learning vertical stream featuring interactive cards:
   - *Spot the Bug 🐛*: Tap faulty code lines to detect runtime errors.
   - *What's the Output? 💻*: Code execution puzzles.
   - *15-Second Explainers ⚡*: High-yield architecture overviews.
   - *Leitner Review Cards 📚*: Instant recall checks.
   - *Cognitive Mystery Drops 🎁*: Rocket fuel & XP bonuses.
2. **Practice (Learn)**:
   - 7 Full-stack curriculum tracks: HTML5, CSS3, JavaScript ES6+, PHP Backend, MySQL Relational, Python Core, C Language & Pointers.
   - **Visual Knowledge Graph**: Topological curriculum map evaluating real-time mastery flow and recommending priority reinforcement topics.
   - **Leitner Spaced Repetition**: 4-box spaced interval memory reinforcement.
   - **Deep Focus Mode**: Pomodoro timer with distraction tracking and focus score logging.
   - **PLi AI Coach**: On-device curriculum Q&A assistant grounded in curriculum concepts.
3. **Compete**:
   - **Live Peer Duels**: 3-round rapid telemetry matches against virtual community learners (*MartianTiger67*, *SnowyOwl3*, *CosmicFalcon42*, *NeonPanda88*).
   - **Weekly Season Leagues**: Bronze, Silver, Gold, Platinum, and Diamond tiers with promotion and demotion thresholds.
4. **Progress**:
   - **Cognitive Radar Chart**: Visualizing Conscientiousness, Motivation, Understanding, and Engagement.
   - **30-Day Mastery Trends & Weekly Streak Graphs**: Recharts-powered analytics.
   - **Community Leaderboard**: Filter by Global or Friends, sort by Streak, Score, or Accuracy.
   - **Digital Badges**: Tiered achievement unlock system with celebration moments.
   - **Quiz History Logs**: Chronological performance records.
5. **Profile & Labs**:
   - Learner archetype classification (*Master Strategist*, *Sprint Pioneer*, *Disciplined Sentinel*, *Adaptive Explorer*).
   - Real-time latency benchmarking.
   - Moodle LMS plugin sandbox with 9-feature interaction vectors, AES-256 GCM token sandboxing, and automated hyperparameter grid search.
   - UX Iteration test suite.

---

## Tech Stack
- **Framework**: React 19 + TypeScript + Vite
- **Styling**: Tailwind CSS v4
- **Charts**: Recharts
- **Icons**: Lucide React
- **Celebration Effects**: Canvas Confetti
- **Storage**: Local persistence engine with reactive state subscriptions

---

## Getting Started

```bash
# Install dependencies
npm install

# Start development server on port 3000
npm run dev

# Production build
npm run build
```
