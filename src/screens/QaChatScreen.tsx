import React, { useState, useRef, useEffect } from 'react';
import { User, QaChatMessage } from '../types';
import { QaChatEngine } from '../ml/qaChatEngine';
import { ArrowLeft, MessageSquare, Send, Sparkles, Bot, User as UserIcon } from 'lucide-react';

interface QaChatScreenProps {
  user: User;
  onBack: () => void;
}

export const QaChatScreen: React.FC<QaChatScreenProps> = ({ user, onBack }) => {
  const [messages, setMessages] = useState<QaChatMessage[]>([
    {
      id: 'welcome',
      sender: 'assistant',
      text: `Hello ${user.username}! I am your **Private Learning Intelligence (PLI)** cognitive coach.\n\nAsk me anything about your curriculum: Web Architecture (HTML/CSS/JS), Backend (PHP/SQL), Reactive systems, Algorithms (Big-O), or On-Device Machine Learning!`,
      timestamp: Date.now(),
      followUpSuggestions: [
        'How does On-Device TFLite work?',
        'Explain the JavaScript Event Loop',
        'Flexbox vs Grid in CSS',
        'What is Big-O Complexity?',
      ],
    },
  ]);
  const [inputText, setInputText] = useState('');
  const chatEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSendMessage = (textToSend?: string) => {
    const text = (textToSend || inputText).trim();
    if (!text) return;

    const userMsg: QaChatMessage = {
      id: `usr_${Date.now()}`,
      sender: 'user',
      text,
      timestamp: Date.now(),
    };

    setMessages(prev => [...prev, userMsg]);
    setInputText('');

    setTimeout(() => {
      const response = QaChatEngine.answerQuestion(text);
      setMessages(prev => [...prev, response]);
    }, 400);
  };

  return (
    <div className="min-h-screen bg-[#0B0F19] text-white flex flex-col justify-between max-w-md mx-auto">
      {/* Top Header */}
      <div className="flex items-center justify-between p-4 border-b border-slate-800 bg-[#0F172A] sticky top-0 z-20">
        <div className="flex items-center space-x-3">
          <button
            onClick={onBack}
            className="p-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:text-white"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-sm font-bold text-white flex items-center gap-1.5">
              <span>PLi Cognitive Coach</span>
              <span className="text-[10px] bg-cyan-950 text-cyan-400 border border-cyan-800 px-1.5 rounded">
                On-Device
              </span>
            </h1>
            <p className="text-[11px] text-slate-400">Curriculum & Telemetry Assistance</p>
          </div>
        </div>

        <div className="p-2 text-cyan-400">
          <Sparkles className="w-5 h-5" />
        </div>
      </div>

      {/* Message List */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.map(msg => {
          const isUser = msg.sender === 'user';

          return (
            <div
              key={msg.id}
              className={`flex flex-col ${isUser ? 'items-end' : 'items-start'} space-y-1.5`}
            >
              <div className="flex items-end space-x-2">
                {!isUser && (
                  <div className="w-7 h-7 rounded-lg bg-cyan-500/20 border border-cyan-500/40 flex items-center justify-center text-cyan-400 text-xs shrink-0">
                    <Bot className="w-4 h-4" />
                  </div>
                )}

                <div
                  className={`p-3.5 rounded-2xl text-xs max-w-[85%] leading-relaxed ${
                    isUser
                      ? 'bg-cyan-600 text-white rounded-br-none shadow-md shadow-cyan-600/20'
                      : 'bg-[#151D2F] text-slate-200 border border-slate-800 rounded-bl-none shadow-md'
                  }`}
                >
                  <p className="whitespace-pre-wrap">{msg.text}</p>

                  {/* Code snippet */}
                  {msg.codeSnippet && (
                    <div className="mt-2.5 bg-[#0B0F19] rounded-xl p-2.5 font-mono text-[11px] text-cyan-300 overflow-x-auto border border-slate-800">
                      <pre>{msg.codeSnippet}</pre>
                    </div>
                  )}
                </div>
              </div>

              {/* Follow-up suggestions */}
              {msg.followUpSuggestions && msg.followUpSuggestions.length > 0 && (
                <div className="flex flex-wrap gap-1.5 pl-9 pt-1">
                  {msg.followUpSuggestions.map(sug => (
                    <button
                      key={sug}
                      onClick={() => handleSendMessage(sug)}
                      className="px-2.5 py-1 rounded-full bg-slate-900 hover:bg-slate-800 border border-slate-700 hover:border-cyan-500/50 text-[11px] text-slate-300 hover:text-cyan-300 transition-all font-medium"
                    >
                      {sug}
                    </button>
                  ))}
                </div>
              )}
            </div>
          );
        })}
        <div ref={chatEndRef} />
      </div>

      {/* Input bar */}
      <div className="p-3 bg-[#0F172A] border-t border-slate-800 sticky bottom-0 z-20">
        <form
          onSubmit={e => {
            e.preventDefault();
            handleSendMessage();
          }}
          className="flex items-center space-x-2"
        >
          <input
            type="text"
            value={inputText}
            onChange={e => setInputText(e.target.value)}
            placeholder="Ask about JS, CSS, SQL, ML, Big-O..."
            className="flex-1 bg-slate-900 border border-slate-800 focus:border-cyan-500 rounded-2xl px-4 py-3 text-xs text-white placeholder-slate-500 focus:outline-none transition-colors"
          />
          <button
            type="submit"
            disabled={!inputText.trim()}
            className="p-3 rounded-2xl bg-cyan-500 hover:bg-cyan-400 disabled:opacity-40 text-slate-950 font-bold shadow-md shadow-cyan-500/20 transition-all"
          >
            <Send className="w-4 h-4" />
          </button>
        </form>
      </div>
    </div>
  );
};
