import React, { useState } from 'react';
import {
  Wrench,
  Plus,
  User,
  MapPin,
  ArrowRight,
  Filter,
  X
} from 'lucide-react';
import { MaintenanceTicket } from '../../types';

interface MaintenanceTicketsPanelProps {
  tickets: MaintenanceTicket[];
  onCreateTicket: (payload: {
    title: string;
    category: MaintenanceTicket['category'];
    location: string;
    description: string;
    photoUrl?: string;
    priority: MaintenanceTicket['priority'];
    reporterName: string;
    houseNumber: string;
  }) => Promise<void>;
  onUpdateStatus: (
    id: string,
    newStatus: MaintenanceTicket['status'],
    notes: string,
    assignedTechnician?: string
  ) => Promise<void>;
}

export const MaintenanceTicketsPanel: React.FC<MaintenanceTicketsPanelProps> = ({
  tickets,
  onCreateTicket,
  onUpdateStatus
}) => {
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedTicketForProgress, setSelectedTicketForProgress] = useState<MaintenanceTicket | null>(
    null
  );

  // Create form state
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState<MaintenanceTicket['category']>('LIGHTING');
  const [location, setLocation] = useState('');
  const [description, setDescription] = useState('');
  const [photoUrl, setPhotoUrl] = useState('');
  const [priority, setPriority] = useState<MaintenanceTicket['priority']>('MEDIUM');
  const [reporterName, setReporterName] = useState('Administración');
  const [houseNumber, setHouseNumber] = useState('Áreas Comunes');
  const [isCreating, setIsCreating] = useState(false);

  // Update status form state
  const [newStatus, setNewStatus] = useState<MaintenanceTicket['status']>('IN_PROGRESS');
  const [progressNotes, setProgressNotes] = useState('');
  const [assignedTech, setAssignedTech] = useState('');
  const [isUpdating, setIsUpdating] = useState(false);

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setIsCreating(true);
      await onCreateTicket({
        title,
        category,
        location,
        description,
        photoUrl: photoUrl.trim() || undefined,
        priority,
        reporterName,
        houseNumber
      });
      setShowCreateModal(false);
      setTitle('');
      setDescription('');
      setLocation('');
      setPhotoUrl('');
    } finally {
      setIsCreating(false);
    }
  };

  const handleOpenProgress = (ticket: MaintenanceTicket) => {
    setSelectedTicketForProgress(ticket);
    const nextMap: Record<MaintenanceTicket['status'], MaintenanceTicket['status']> = {
      OPEN: 'IN_PROGRESS',
      IN_PROGRESS: 'RESOLVED',
      RESOLVED: 'CLOSED',
      CLOSED: 'CLOSED'
    };
    setNewStatus(nextMap[ticket.status]);
    setAssignedTech(ticket.assignedTechnician || '');
    setProgressNotes('');
  };

  const handleUpdateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedTicketForProgress) return;
    try {
      setIsUpdating(true);
      await onUpdateStatus(
        selectedTicketForProgress.id,
        newStatus,
        progressNotes || `Transición a estado ${newStatus}`,
        assignedTech
      );
      setSelectedTicketForProgress(null);
    } finally {
      setIsUpdating(false);
    }
  };

  const filteredTickets = tickets.filter(t => {
    if (filterStatus === 'ALL') return true;
    return t.status === filterStatus;
  });

  const getPriorityBadge = (p: MaintenanceTicket['priority']) => {
    const map = {
      URGENT: 'bg-red-500/20 text-red-300 border-red-500/40',
      HIGH: 'bg-rose-500/20 text-rose-300 border-rose-500/40',
      MEDIUM: 'bg-amber-500/20 text-amber-300 border-amber-500/40',
      LOW: 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40'
    };
    return map[p] || 'bg-slate-700 text-slate-300';
  };

  const getStatusBadge = (s: MaintenanceTicket['status']) => {
    const map = {
      OPEN: 'bg-blue-500/10 text-blue-400 border-blue-500/30',
      IN_PROGRESS: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
      RESOLVED: 'bg-teal-500/10 text-teal-400 border-teal-500/30',
      CLOSED: 'bg-slate-700/50 text-slate-400 border-slate-600'
    };
    return map[s] || 'bg-slate-700 text-slate-300';
  };

  return (
    <div className="space-y-6">
      {/* Banner */}
      <div className="bg-gradient-to-r from-amber-950/60 via-slate-900 to-slate-900 border border-amber-900/40 p-6 rounded-2xl shadow-xl">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center space-x-2 text-amber-400 font-bold text-xs uppercase tracking-wider mb-1">
              <Wrench className="w-4 h-4" />
              <span>Mantenimiento y Fallas &middot; Épica 5 (US-501 &amp; US-502)</span>
            </div>
            <h2 className="text-2xl font-black text-white tracking-tight">
              Gestión de Incidencias y Asignación a Técnicos
            </h2>
            <p className="text-slate-400 text-sm mt-1 max-w-2xl">
              Recepción, clasificación y seguimiento del ciclo de vida de tickets de
              mantenimiento (Abierto &rarr; En Proceso &rarr; Resuelto &rarr; Cerrado) con
              registro auditable de actualizaciones.
            </p>
          </div>

          <button
            onClick={() => setShowCreateModal(true)}
            className="flex items-center space-x-2 px-5 py-2.5 bg-amber-600 hover:bg-amber-500 text-white font-semibold rounded-xl text-sm shadow-lg shadow-amber-600/30 transition whitespace-nowrap"
          >
            <Plus className="w-4 h-4" />
            <span>Reportar Incidencia</span>
          </button>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center space-x-2 border-b border-slate-800 pb-3">
        <Filter className="w-4 h-4 text-slate-400 mr-1" />
        {['ALL', 'OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'].map(s => (
          <button
            key={s}
            onClick={() => setFilterStatus(s)}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
              filterStatus === s
                ? 'bg-slate-800 text-amber-400 border border-slate-700'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            {s === 'ALL'
              ? 'Todos'
              : s === 'OPEN'
              ? 'Abiertos'
              : s === 'IN_PROGRESS'
              ? 'En Proceso'
              : s === 'RESOLVED'
              ? 'Resueltos'
              : 'Cerrados'}
          </button>
        ))}
      </div>

      {/* Tickets List */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredTickets.map(ticket => (
          <div
            key={ticket.id}
            className="bg-slate-900 border border-slate-800 rounded-2xl p-5 flex flex-col justify-between space-y-4 shadow-lg hover:border-slate-700 transition"
          >
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="font-mono text-xs font-bold text-slate-400 bg-slate-800 px-2 py-0.5 rounded border border-slate-700">
                  {ticket.folio}
                </span>
                <div className="flex items-center space-x-2">
                  <span
                    className={`text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full border ${getPriorityBadge(
                      ticket.priority
                    )}`}
                  >
                    {ticket.priority}
                  </span>
                  <span
                    className={`text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full border ${getStatusBadge(
                      ticket.status
                    )}`}
                  >
                    {ticket.status}
                  </span>
                </div>
              </div>

              <h4 className="font-bold text-white text-base leading-snug">{ticket.title}</h4>
              <p className="text-xs text-slate-400 mt-1 line-clamp-3">{ticket.description}</p>

              <div className="mt-4 pt-3 border-t border-slate-800 space-y-2 text-xs text-slate-300">
                <div className="flex items-center space-x-1.5 text-slate-400">
                  <MapPin className="w-3.5 h-3.5 text-amber-400 flex-shrink-0" />
                  <span className="truncate">{ticket.location}</span>
                </div>
                <div className="flex items-center space-x-1.5 text-slate-400">
                  <User className="w-3.5 h-3.5 text-slate-500 flex-shrink-0" />
                  <span>
                    Reporta: {ticket.reporterName} ({ticket.houseNumber})
                  </span>
                </div>
                {ticket.assignedTechnician && (
                  <div className="flex items-center space-x-1.5 text-teal-400 font-medium">
                    <Wrench className="w-3.5 h-3.5 flex-shrink-0" />
                    <span>Técnico: {ticket.assignedTechnician}</span>
                  </div>
                )}
              </div>
            </div>

            {ticket.status !== 'CLOSED' && (
              <button
                onClick={() => handleOpenProgress(ticket)}
                className="w-full py-2 px-3 bg-slate-800 hover:bg-slate-700 text-amber-400 hover:text-amber-300 font-semibold rounded-lg text-xs border border-slate-700 transition flex items-center justify-center space-x-1.5"
              >
                <span>Avanzar Estado / Asignar</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </button>
            )}
          </div>
        ))}
      </div>

      {/* Modal: Create Ticket */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl max-w-lg w-full p-6 space-y-4">
            <div className="flex items-center justify-between pb-2 border-b border-slate-800">
              <h3 className="text-lg font-bold text-white flex items-center space-x-2">
                <Wrench className="w-5 h-5 text-amber-400" />
                <span>Nuevo Ticket de Mantenimiento</span>
              </h3>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreateSubmit} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold uppercase text-slate-300 mb-1">
                  Título de la Falla *
                </label>
                <input
                  type="text"
                  required
                  value={title}
                  onChange={e => setTitle(e.target.value)}
                  placeholder="Ej. Fuga en cisterna principal"
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-sm focus:outline-none focus:ring-2 focus:ring-amber-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold uppercase text-slate-300 mb-1">
                    Categoría *
                  </label>
                  <select
                    value={category}
                    onChange={e => setCategory(e.target.value as any)}
                    className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-xs focus:outline-none focus:ring-2 focus:ring-amber-500"
                  >
                    <option value="LIGHTING">Iluminación</option>
                    <option value="PLUMBING">Fontanería</option>
                    <option value="POOL">Piscina</option>
                    <option value="GARDENING">Jardinería</option>
                    <option value="SECURITY_EQUIPMENT">Seguridad / Talanqueras</option>
                    <option value="OTHER">Otros</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold uppercase text-slate-300 mb-1">
                    Prioridad *
                  </label>
                  <select
                    value={priority}
                    onChange={e => setPriority(e.target.value as any)}
                    className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-xs focus:outline-none focus:ring-2 focus:ring-amber-500"
                  >
                    <option value="LOW">Baja</option>
                    <option value="MEDIUM">Media</option>
                    <option value="HIGH">Alta</option>
                    <option value="URGENT">Urgente</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold uppercase text-slate-300 mb-1">
                    Reportado Por *
                  </label>
                  <input
                    type="text"
                    required
                    value={reporterName}
                    onChange={e => setReporterName(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-xs focus:outline-none focus:ring-2 focus:ring-amber-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold uppercase text-slate-300 mb-1">
                    Inmueble / Sector *
                  </label>
                  <input
                    type="text"
                    required
                    value={houseNumber}
                    onChange={e => setHouseNumber(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-xs focus:outline-none focus:ring-2 focus:ring-amber-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-slate-300 mb-1">
                  Ubicación Exacta *
                </label>
                <input
                  type="text"
                  required
                  value={location}
                  onChange={e => setLocation(e.target.value)}
                  placeholder="Ej. Calle 3 frente a Casa 104"
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-xs focus:outline-none focus:ring-2 focus:ring-amber-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-slate-300 mb-1">
                  Descripción Detallada *
                </label>
                <textarea
                  rows={3}
                  required
                  value={description}
                  onChange={e => setDescription(e.target.value)}
                  placeholder="Explique el desperfecto y posibles riesgos..."
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-xs focus:outline-none focus:ring-2 focus:ring-amber-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-slate-300 mb-1">
                  URL de Fotografía / Evidencia (Opcional)
                </label>
                <input
                  type="url"
                  value={photoUrl}
                  onChange={e => setPhotoUrl(e.target.value)}
                  placeholder="https://images.residence.local/evidence1.jpg"
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-xs focus:outline-none focus:ring-2 focus:ring-amber-500"
                />
              </div>

              <div className="pt-3 flex justify-end space-x-2 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 text-sm text-slate-300 hover:text-white"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={isCreating}
                  className="px-5 py-2 bg-amber-600 hover:bg-amber-500 text-white font-semibold text-sm rounded-lg shadow-lg shadow-amber-600/30 transition disabled:opacity-50"
                >
                  {isCreating ? 'Guardando...' : 'Registrar Incidencia'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Update Ticket Progress */}
      {selectedTicketForProgress && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl max-w-lg w-full p-6 space-y-4">
            <div className="flex items-center justify-between pb-2 border-b border-slate-800">
              <h3 className="text-lg font-bold text-white flex items-center space-x-2">
                <Wrench className="w-5 h-5 text-amber-400" />
                <span>Actualizar Ciclo de Vida: {selectedTicketForProgress.folio}</span>
              </h3>
              <button
                onClick={() => setSelectedTicketForProgress(null)}
                className="text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleUpdateSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold uppercase text-slate-300 mb-1">
                  Nuevo Estado del Ticket *
                </label>
                <select
                  value={newStatus}
                  onChange={e => setNewStatus(e.target.value as any)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-sm font-semibold focus:outline-none focus:ring-2 focus:ring-amber-500"
                >
                  <option value="IN_PROGRESS">EN PROCESO (Asignado)</option>
                  <option value="RESOLVED">RESUELTO (Trabajo Finalizado)</option>
                  <option value="CLOSED">CERRADO (Aprobado y Concluido)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-slate-300 mb-1">
                  Técnico o Proveedor Asignado
                </label>
                <input
                  type="text"
                  value={assignedTech}
                  onChange={e => setAssignedTech(e.target.value)}
                  placeholder="Ej. Servipro Fontanería / Juan Morales"
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-xs focus:outline-none focus:ring-2 focus:ring-amber-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-slate-300 mb-1">
                  Notas de Avance y Hallazgos
                </label>
                <textarea
                  rows={3}
                  value={progressNotes}
                  onChange={e => setProgressNotes(e.target.value)}
                  placeholder="Describa el trabajo realizado, materiales o refacciones..."
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-xs focus:outline-none focus:ring-2 focus:ring-amber-500"
                />
              </div>

              <div className="pt-3 flex justify-end space-x-2 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setSelectedTicketForProgress(null)}
                  className="px-4 py-2 text-sm text-slate-300 hover:text-white"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={isUpdating}
                  className="px-5 py-2 bg-amber-600 hover:bg-amber-500 text-white font-semibold text-sm rounded-lg shadow-lg shadow-amber-600/30 transition disabled:opacity-50"
                >
                  {isUpdating ? 'Actualizando...' : 'Guardar Estado'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
