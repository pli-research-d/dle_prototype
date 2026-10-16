import React, { useState } from 'react';
import { KnowledgeGraphData, KnowledgeTopicNode } from '../types';
import { Brain, Sparkles, Play, ShieldAlert, CheckCircle2, ArrowRight } from 'lucide-react';

interface VisualKnowledgeGraphProps {
  graphData: KnowledgeGraphData;
  onSelectNodeQuiz: (categoryName: string, categoryNumber: number) => void;
}

export const VisualKnowledgeGraph: React.FC<VisualKnowledgeGraphProps> = ({
  graphData,
  onSelectNodeQuiz,
}) => {
  const [selectedNode, setSelectedNode] = useState<KnowledgeTopicNode | null>(
    graphData.reinforcementPriorityNode || graphData.nodes[0] || null
  );

  const statusColors = {
    STREAK: 'border-emerald-500 bg-emerald-950/80 text-emerald-300 shadow-emerald-500/30',
    STRENGTH: 'border-emerald-500 bg-emerald-950/80 text-emerald-300 shadow-emerald-500/30',
    SOLIDIFYING: 'border-cyan-500 bg-cyan-950/80 text-cyan-300 shadow-cyan-500/30',
    NEEDS_REVIEW: 'border-rose-500 bg-rose-950/80 text-rose-300 shadow-rose-500/30',
    UNEXPLORED: 'border-slate-700 bg-slate-900/80 text-slate-400 shadow-none',
  };

  const statusLabels = {
    STRENGTH: 'Mastered (80%+)',
    SOLIDIFYING: 'Solidifying (55-79%)',
    NEEDS_REVIEW: 'Needs Reinforcement (<55%)',
    UNEXPLORED: 'Unexplored (0 Attempts)',
  };

  // Convert normalized 0..1 to percentage coordinates
  const getNodePos = (nodeId: string) => {
    const node = graphData.nodes.find(n => n.id === nodeId);
    if (!node) return { x: 50, y: 50 };
    return { x: node.normX * 100, y: node.normY * 100 };
  };

  return (
    <div className="bg-[#151D2F] border border-slate-800 rounded-2xl p-4 shadow-xl overflow-hidden">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center space-x-2">
          <div className="p-1.5 rounded-lg bg-cyan-500/15 text-cyan-400">
            <Brain className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-white">Visual Knowledge Graph</h3>
            <p className="text-[11px] text-slate-400">
              Interactive topological curriculum map & mastery flow
            </p>
          </div>
        </div>
        <div className="text-xs bg-slate-900 border border-slate-700 text-cyan-400 px-2.5 py-1 rounded-full font-semibold">
          Avg Mastery: {graphData.averageMasteryPercent}%
        </div>
      </div>

      {/* SVG Canvas Area */}
      <div className="relative w-full h-72 sm:h-80 bg-[#0B0F19] rounded-xl border border-slate-800/80 overflow-hidden shadow-inner">
        {/* Subtle grid background */}
        <div className="absolute inset-0 opacity-15 bg-[radial-gradient(#38BDF8_1px,transparent_1px)] [background-size:16px_16px]" />

        {/* Cluster label tags */}
        {graphData.clusters.map(cluster => (
          <div
            key={cluster.name}
            style={{
              left: `${cluster.normCenterX * 100}%`,
              top: `${cluster.normCenterY * 100}%`,
              transform: 'translate(-50%, -50%)',
            }}
            className="absolute pointer-events-none text-[9px] uppercase tracking-wider font-extrabold px-2 py-0.5 rounded-full border border-slate-800/60 bg-slate-900/60 text-slate-400 opacity-60"
          >
            {cluster.name}
          </div>
        ))}

        {/* SVG Edges */}
        <svg className="absolute inset-0 w-full h-full pointer-events-none">
          {graphData.edges.map(edge => {
            const p1 = getNodePos(edge.fromNodeId);
            const p2 = getNodePos(edge.toNodeId);
            return (
              <line
                key={`${edge.fromNodeId}-${edge.toNodeId}`}
                x1={`${p1.x}%`}
                y1={`${p1.y}%`}
                x2={`${p2.x}%`}
                y2={`${p2.y}%`}
                stroke="#334155"
                strokeWidth="1.5"
                strokeDasharray="3 3"
              />
            );
          })}
        </svg>

        {/* Node Buttons */}
        {graphData.nodes.map(node => {
          const isSelected = selectedNode?.id === node.id;
          const isPriority = node.recommendedNext;
          const statusStyle = statusColors[node.status] || statusColors.UNEXPLORED;

          return (
            <button
              key={node.id}
              onClick={() => setSelectedNode(node)}
              style={{
                left: `${node.normX * 100}%`,
                top: `${node.normY * 100}%`,
                transform: 'translate(-50%, -50%)',
              }}
              className={`absolute flex items-center justify-center rounded-xl p-1.5 transition-all text-xs font-bold border ${
                isSelected
                  ? 'ring-2 ring-cyan-400 scale-110 z-20 shadow-lg'
                  : 'hover:scale-105 z-10'
              } ${statusStyle}`}
            >
              {isPriority && (
                <span className="absolute -top-1.5 -right-1.5 w-3 h-3 bg-amber-400 rounded-full animate-ping" />
              )}
              <span className="max-w-[70px] truncate text-[10px] px-1 font-semibold">
                {node.title.split(' ')[0]}
              </span>
            </button>
          );
        })}
      </div>

      {/* Selected Node Details Card */}
      {selectedNode && (
        <div className="mt-3 p-3 bg-slate-900/80 border border-slate-800 rounded-xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
          <div>
            <div className="flex items-center space-x-2">
              <span className="text-sm font-bold text-white">{selectedNode.title}</span>
              <span
                className={`text-[10px] font-semibold px-2 py-0.5 rounded-full border ${
                  statusColors[selectedNode.status]
                }`}
              >
                {statusLabels[selectedNode.status]}
              </span>
            </div>
            <div className="text-xs text-slate-400 mt-1 flex items-center gap-3">
              <span>Category: <strong className="text-slate-200">{selectedNode.category}</strong></span>
              <span>Accuracy: <strong className="text-cyan-400">{Math.round(selectedNode.accuracyPercent)}%</strong></span>
              <span>Attempts: <strong className="text-slate-200">{selectedNode.attemptsCount}</strong></span>
            </div>
          </div>

          <button
            onClick={() => onSelectNodeQuiz(selectedNode.category, selectedNode.categoryNumber)}
            className="flex items-center space-x-1.5 px-4 py-2 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-xs shadow-md shadow-cyan-500/20 transition-all shrink-0"
          >
            <Play className="w-3.5 h-3.5 fill-slate-950" />
            <span>Practice Topic</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>
      )}
    </div>
  );
};
