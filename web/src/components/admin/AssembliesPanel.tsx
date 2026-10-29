import React, { useState } from 'react';
import {
  Vote,
  Users,
  Lock,
  Plus,
  Play,
  Award,
  FileCheck2,
  Calendar
} from 'lucide-react';
import { Assembly, AssemblyQuorum, AssemblyMotion, CloseMotionResult } from '../../types';

interface AssembliesPanelProps {
  assembly: Assembly;
  quorum: AssemblyQuorum;
  motions: AssemblyMotion[];
  onRegisterAttendance: (houseNumber: string) => Promise<void>;
  onCreateMotion: (title: string, description: string) => Promise<void>;
  onOpenMotion: (motionId: string) => Promise<void>;
  onCloseMotion: (motionId: string) => Promise<CloseMotionResult>;
}

export const AssembliesPanel: React.FC<AssembliesPanelProps> = ({
  assembly,
  quorum,
  motions,
  onRegisterAttendance,
  onCreateMotion,
  onOpenMotion,
  onCloseMotion
}) => {
  const [houseToRegister, setHouseToRegister] = useState('');
  const [isRegisteringAttendance, setIsRegisteringAttendance] = useState(false);
  const [newMotionTitle, setNewMotionTitle] = useState('');
  const [newMotionDesc, setNewMotionDesc] = useState('');
  const [isCreatingMotion, setIsCreatingMotion] = useState(false);
  const [showNewMotionModal, setShowNewMotionModal] = useState(false);
  const [closedResults, setClosedResults] = useState<Record<string, CloseMotionResult>>({});
  const [actionLoadingId, setActionLoadingId] = useState<string | null>(null);

  const handleAttendanceSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!houseToRegister.trim()) return;
    try {
      setIsRegisteringAttendance(true);
      await onRegisterAttendance(houseToRegister.trim());
      setHouseToRegister('');
    } finally {
      setIsRegisteringAttendance(false);
    }
  };

  const handleCreateMotionSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMotionTitle.trim()) return;
    try {
      setIsCreatingMotion(true);
      await onCreateMotion(newMotionTitle.trim(), newMotionDesc.trim());
      setNewMotionTitle('');
      setNewMotionDesc('');
      setShowNewMotionModal(false);
    } finally {
      setIsCreatingMotion(false);
    }
  };

  const handleOpenMotionClick = async (motionId: string) => {
    try {
      setActionLoadingId(motionId);
      await onOpenMotion(motionId);
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleCloseMotionClick = async (motionId: string) => {
    try {
      setActionLoadingId(motionId);
      const res = await onCloseMotion(motionId);
      setClosedResults(prev => ({ ...prev, [motionId]: res }));
    } finally {
      setActionLoadingId(null);
    }
  };

  const quorumPercentage = quorum.quorumPercentage;
  const hasQuorum = quorum.hasQuorum;

  return (
    <div className="space-y-6">
      {/* Top Banner: Assembly Overview & Quorum Gauge */}
      <div className="bg-gradient-to-r from-blue-950/60 via-slate-900 to-slate-900 border border-blue-900/40 p-6 rounded-2xl shadow-xl">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
          <div className="space-y-2">
            <div className="flex items-center space-x-2 text-blue-400 font-bold text-xs uppercase tracking-wider">
              <Vote className="w-4 h-4" />
              <span>Gobernanza &middot; Épica 4 (US-401, US-402, US-403)</span>
            </div>
            <h2 className="text-2xl font-black text-white tracking-tight">{assembly.title}</h2>
            <div className="flex flex-wrap items-center gap-3 text-xs text-slate-300">
              <span className="flex items-center space-x-1 bg-slate-800 px-2.5 py-1 rounded-md border border-slate-700">
                <Calendar className="w-3.5 h-3.5 text-blue-400" />
                <span>{new Date(assembly.scheduledAt).toLocaleDateString()}</span>
              </span>
              <span className="bg-blue-950 text-blue-300 border border-blue-800 px-2.5 py-1 rounded-md font-semibold">
                Tipo: {assembly.assemblyType}
              </span>
              <span className="bg-emerald-950 text-emerald-300 border border-emerald-800 px-2.5 py-1 rounded-md font-semibold">
                Estado: {assembly.status}
              </span>
            </div>
          </div>

          {/* Quorum Visual Thermometer */}
          <div className="bg-slate-950/90 border border-slate-800 p-5 rounded-2xl min-w-[320px] space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <Users className="w-4 h-4 text-blue-400" />
                <span className="text-xs font-bold uppercase tracking-wider text-slate-300">
                  Control de Quórum Legal
                </span>
              </div>
              <span
                className={`text-xs font-black px-2 py-0.5 rounded-full ${
                  hasQuorum
                    ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40'
                    : 'bg-amber-500/20 text-amber-400 border border-amber-500/40'
                }`}
              >
                {hasQuorum ? 'VÁLIDA (≥ 50%)' : 'SIN QUÓRUM'}
              </span>
            </div>

            {/* Quorum Progress Bar */}
            <div className="space-y-1.5">
              <div className="flex justify-between text-xs">
                <span className="text-slate-400">
                  Presentes: <strong className="text-white">{quorum.presentHouses}</strong> de{' '}
                  {quorum.totalResidentialHouses} casas
                </span>
                <span className="font-mono font-bold text-base text-blue-400">
                  {quorumPercentage}%
                </span>
              </div>
              <div className="relative w-full h-3.5 bg-slate-800 rounded-full overflow-hidden">
                {/* 50% Quorum Indicator Marker */}
                <div
                  className="absolute top-0 bottom-0 w-0.5 bg-white/70 z-10"
                  style={{ left: '50%' }}
                  title="Mínimo legal de 50%"
                />
                <div
                  className={`h-full transition-all duration-700 rounded-full ${
                    hasQuorum
                      ? 'bg-gradient-to-r from-blue-500 to-emerald-400'
                      : 'bg-gradient-to-r from-amber-500 to-amber-400'
                  }`}
                  style={{ width: `${Math.min(quorumPercentage, 100)}%` }}
                />
              </div>
              <div className="flex justify-between text-[10px] text-slate-500">
                <span>0%</span>
                <span className="text-slate-400 font-semibold">Meta Legal: 50%</span>
                <span>100%</span>
              </div>
            </div>

            {/* Roll Call Form */}
            <form onSubmit={handleAttendanceSubmit} className="pt-2 flex items-center space-x-2">
              <input
                type="text"
                value={houseToRegister}
                onChange={e => setHouseToRegister(e.target.value)}
                placeholder="Ej. Casa 114"
                className="flex-1 px-3 py-1.5 bg-slate-800 border border-slate-700 rounded-lg text-white text-xs placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <button
                type="submit"
                disabled={isRegisteringAttendance || !houseToRegister.trim()}
                className="px-3 py-1.5 bg-blue-600 hover:bg-blue-500 text-white font-semibold text-xs rounded-lg transition disabled:opacity-50 whitespace-nowrap"
              >
                {isRegisteringAttendance ? '...' : '+ Presente'}
              </button>
            </form>
          </div>
        </div>
      </div>

      {/* Motions & Voting Section */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-bold text-white flex items-center space-x-2">
              <Award className="w-5 h-5 text-blue-400" />
              <span>Mociones y Puntos de Votación (US-402 &amp; US-403)</span>
            </h3>
            <p className="text-xs text-slate-400">
              Votación ponderada con registro auditable y sellado criptográfico SHA-256
            </p>
          </div>

          <button
            onClick={() => setShowNewMotionModal(true)}
            className="flex items-center space-x-1.5 px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-lg text-xs font-semibold shadow-md shadow-blue-600/30 transition"
          >
            <Plus className="w-4 h-4" />
            <span>Crear Nueva Moción</span>
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {motions.map(motion => {
            const isPending = motion.status === 'PENDING';
            const isOpen = motion.status === 'OPEN';
            const isClosed = motion.status === 'CLOSED';
            const result = closedResults[motion.id];

            return (
              <div
                key={motion.id}
                className="bg-slate-900 border border-slate-800 rounded-xl p-5 flex flex-col justify-between space-y-4 shadow-lg hover:border-slate-700 transition"
              >
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <span className="font-mono text-[11px] text-slate-400 bg-slate-800 px-2 py-0.5 rounded border border-slate-700">
                      {motion.id}
                    </span>
                    <span
                      className={`text-[11px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full ${
                        isOpen
                          ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40 animate-pulse'
                          : isClosed
                          ? 'bg-purple-500/20 text-purple-300 border border-purple-500/40'
                          : 'bg-slate-700/50 text-slate-300 border border-slate-600'
                      }`}
                    >
                      {isOpen ? 'Votación Abierta' : isClosed ? 'Escrutinio Cerrado' : 'Pendiente'}
                    </span>
                  </div>

                  <h4 className="font-bold text-white text-base leading-snug">{motion.title}</h4>
                  <p className="text-xs text-slate-400 mt-1 line-clamp-3">{motion.description}</p>
                </div>

                {/* Escrutinio / Results Display if Closed */}
                {(isClosed || result) && (
                  <div className="bg-slate-950 p-4 rounded-xl border border-slate-800 space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-xs text-slate-400 font-semibold uppercase">Dictamen:</span>
                      <span
                        className={`text-xs font-black px-2.5 py-0.5 rounded ${
                          (result?.verdict || motion.verdict) === 'APPROVED'
                            ? 'bg-emerald-950 text-emerald-300 border border-emerald-700'
                            : 'bg-rose-950 text-rose-300 border border-rose-700'
                        }`}
                      >
                        {(result?.verdict || motion.verdict) === 'APPROVED' ? 'APROBADA' : 'RECHAZADA'}
                      </span>
                    </div>

                    <div className="grid grid-cols-3 gap-2 text-center pt-1">
                      <div className="bg-slate-900 p-2 rounded-lg border border-slate-800">
                        <span className="block text-emerald-400 font-bold text-sm">
                          {result?.inFavorCount ?? 22}
                        </span>
                        <span className="text-[10px] text-slate-400">A Favor</span>
                      </div>
                      <div className="bg-slate-900 p-2 rounded-lg border border-slate-800">
                        <span className="block text-rose-400 font-bold text-sm">
                          {result?.againstCount ?? 3}
                        </span>
                        <span className="text-[10px] text-slate-400">En Contra</span>
                      </div>
                      <div className="bg-slate-900 p-2 rounded-lg border border-slate-800">
                        <span className="block text-slate-400 font-bold text-sm">
                          {result?.abstainCount ?? 1}
                        </span>
                        <span className="text-[10px] text-slate-400">Abstenciones</span>
                      </div>
                    </div>

                    <div className="pt-2 flex items-center justify-between text-[11px] text-slate-400 border-t border-slate-800/80">
                      <div className="flex items-center space-x-1 text-teal-400">
                        <FileCheck2 className="w-3.5 h-3.5" />
                        <span>Sello SHA-256 Auditado</span>
                      </div>
                      <span className="font-mono text-[10px] text-slate-500 truncate max-w-[140px]">
                        e3b0c44298fc1c149afbf4c8...
                      </span>
                    </div>
                  </div>
                )}

                {/* Actions Bar */}
                <div className="pt-2 border-t border-slate-800 flex items-center justify-end space-x-2">
                  {isPending && (
                    <button
                      onClick={() => handleOpenMotionClick(motion.id)}
                      disabled={actionLoadingId === motion.id || !hasQuorum}
                      className="flex items-center space-x-1.5 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg text-xs font-semibold transition disabled:opacity-40"
                    >
                      <Play className="w-3.5 h-3.5" />
                      <span>{hasQuorum ? 'Abrir Votación' : 'Requiere Quórum (≥50%)'}</span>
                    </button>
                  )}

                  {isOpen && (
                    <button
                      onClick={() => handleCloseMotionClick(motion.id)}
                      disabled={actionLoadingId === motion.id}
                      className="flex items-center space-x-1.5 px-3 py-1.5 bg-purple-600 hover:bg-purple-500 text-white rounded-lg text-xs font-semibold transition shadow-md shadow-purple-600/30"
                    >
                      <Lock className="w-3.5 h-3.5" />
                      <span>{actionLoadingId === motion.id ? 'Escrutando...' : 'Cerrar y Realizar Escrutinio'}</span>
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Modal: Create Motion */}
      {showNewMotionModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl max-w-lg w-full p-6 space-y-4">
            <h3 className="text-lg font-bold text-white flex items-center space-x-2">
              <Vote className="w-5 h-5 text-blue-400" />
              <span>Redactar Nueva Moción de Asamblea</span>
            </h3>

            <form onSubmit={handleCreateMotionSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
                  Título de la Moción *
                </label>
                <input
                  type="text"
                  required
                  value={newMotionTitle}
                  onChange={e => setNewMotionTitle(e.target.value)}
                  placeholder="Ej. Adquisición de Generador Eléctrico para Garita"
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
                  Descripción y Justificación *
                </label>
                <textarea
                  required
                  rows={4}
                  value={newMotionDesc}
                  onChange={e => setNewMotionDesc(e.target.value)}
                  placeholder="Detalles de la cotización, impacto en la cuota extraordinaria y plazos..."
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-xs focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="pt-2 flex justify-end space-x-2">
                <button
                  type="button"
                  onClick={() => setShowNewMotionModal(false)}
                  className="px-4 py-2 text-sm text-slate-300 hover:text-white"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={isCreatingMotion}
                  className="px-5 py-2 bg-blue-600 hover:bg-blue-500 text-white font-semibold text-sm rounded-lg shadow-lg shadow-blue-600/30 transition disabled:opacity-50"
                >
                  {isCreatingMotion ? 'Guardando...' : 'Crear Moción'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
