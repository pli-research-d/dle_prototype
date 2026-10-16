import React, { useRef } from 'react';
import { ArrowLeft, RefreshCw, ExternalLink } from 'lucide-react';

interface UxTestViewScreenProps {
  onBack: () => void;
}

export const UxTestViewScreen: React.FC<UxTestViewScreenProps> = ({ onBack }) => {
  const iframeRef = useRef<HTMLIFrameElement>(null);

  const handleReload = () => {
    if (iframeRef.current) {
      iframeRef.current.src = '/ux_test_view.html';
    }
  };

  return (
    <div className="min-h-screen bg-[#0B0F19] text-white flex flex-col justify-between max-w-2xl mx-auto">
      {/* Top Header */}
      <div className="flex items-center justify-between p-4 border-b border-slate-800 bg-[#0F172A]">
        <div className="flex items-center space-x-3">
          <button
            onClick={onBack}
            className="p-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:text-white"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-sm font-bold text-white">UX Iteration Test Harness</h1>
            <p className="text-[11px] text-slate-400">Adaptive Engine Visual Iteration Suite</p>
          </div>
        </div>

        <button
          onClick={handleReload}
          className="p-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:text-white"
        >
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>

      {/* Embedded IFrame */}
      <div className="flex-1 w-full h-[85vh] bg-[#0B0F19]">
        <iframe
          ref={iframeRef}
          src="/ux_test_view.html"
          className="w-full h-full border-none"
          title="UX Iteration Test Harness"
        />
      </div>
    </div>
  );
};
