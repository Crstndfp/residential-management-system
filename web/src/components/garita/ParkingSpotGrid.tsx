import React, { useState } from 'react';
import {
  Car,
  Clock,
  Moon,
  LogOut,
  Plus,
  Search,
  CheckCircle,
  AlertCircle,
  User,
  Home
} from 'lucide-react';
import { ParkingSpot, VehicleEntry } from '../../types';

interface ParkingSpotGridProps {
  spots: ParkingSpot[];
  activeEntries: VehicleEntry[];
  onOpenCheckIn: () => void;
  onRegisterExit: (entryId: string, spotNumber: string) => Promise<void>;
  isLoading: boolean;
}

export const ParkingSpotGrid: React.FC<ParkingSpotGridProps> = ({
  spots,
  activeEntries,
  onOpenCheckIn,
  onRegisterExit,
  isLoading
}) => {
  const [filterQuery, setFilterQuery] = useState('');
  const [exitingId, setExitingId] = useState<string | null>(null);

  // Map entries by spotNumber for instant lookup
  const entryBySpot = new Map<string, VehicleEntry>();
  activeEntries.forEach(e => {
    if (e.spotNumber) {
      entryBySpot.set(e.spotNumber, e);
    }
  });

  const occupiedCount = spots.filter(s => s.isOccupied).length;
  const availableCount = spots.length - occupiedCount;
  const occupancyRate = spots.length > 0 ? Math.round((occupiedCount / spots.length) * 100) : 0;

  const handleExitClick = async (entryId: string, spotNumber: string) => {
    try {
      setExitingId(entryId);
      await onRegisterExit(entryId, spotNumber);
    } finally {
      setExitingId(null);
    }
  };

  const calculateDuration = (entryTimeStr: string) => {
    const entryDate = new Date(entryTimeStr);
    const diffMs = Date.now() - entryDate.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const hours = Math.floor(diffMins / 60);
    const mins = diffMins % 60;
    if (hours > 0) {
      return `${hours}h ${mins}m`;
    }
    return `${mins}m`;
  };

  const filteredSpots = spots.filter(spot => {
    if (!filterQuery.trim()) return true;
    const q = filterQuery.toLowerCase();
    const entry = entryBySpot.get(spot.spotNumber);
    if (spot.spotNumber.toLowerCase().includes(q)) return true;
    if (entry) {
      if (entry.plate.toLowerCase().includes(q)) return true;
      if (entry.houseNumber.toLowerCase().includes(q)) return true;
      if (entry.visitorName.toLowerCase().includes(q)) return true;
    }
    return false;
  });

  return (
    <div className="space-y-6">
      {/* Overview Stat Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl flex items-center justify-between">
          <div>
            <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Total Espacios</p>
            <p className="text-2xl font-black text-white mt-1">{spots.length}</p>
          </div>
          <div className="p-3 bg-blue-500/10 text-blue-400 rounded-xl">
            <Car className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl flex items-center justify-between">
          <div>
            <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Disponibles</p>
            <p className="text-2xl font-black text-emerald-400 mt-1">{availableCount}</p>
          </div>
          <div className="p-3 bg-emerald-500/10 text-emerald-400 rounded-xl">
            <CheckCircle className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl flex items-center justify-between">
          <div>
            <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Ocupados</p>
            <p className="text-2xl font-black text-rose-400 mt-1">{occupiedCount}</p>
          </div>
          <div className="p-3 bg-rose-500/10 text-rose-400 rounded-xl">
            <AlertCircle className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl flex items-center justify-between">
          <div>
            <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Tasa Ocupación</p>
            <p className="text-2xl font-black text-amber-400 mt-1">{occupancyRate}%</p>
          </div>
          <div className="w-12 h-12 flex items-center justify-center relative">
            <svg className="w-12 h-12 transform -rotate-90">
              <circle
                cx="24"
                cy="24"
                r="18"
                className="text-slate-800"
                strokeWidth="4"
                stroke="currentColor"
                fill="transparent"
              />
              <circle
                cx="24"
                cy="24"
                r="18"
                className="text-amber-400 transition-all duration-500"
                strokeWidth="4"
                strokeDasharray={113}
                strokeDashoffset={113 - (113 * occupancyRate) / 100}
                strokeLinecap="round"
                stroke="currentColor"
                fill="transparent"
              />
            </svg>
          </div>
        </div>
      </div>

      {/* Control Bar: Search & Action Button */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4 bg-slate-900/60 p-4 rounded-xl border border-slate-800">
        <div className="relative flex-1 max-w-md">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
            <Search className="w-4 h-4" />
          </div>
          <input
            type="text"
            value={filterQuery}
            onChange={e => setFilterQuery(e.target.value)}
            placeholder="Buscar por placa, espacio o casa..."
            className="w-full pl-9 pr-4 py-2 bg-slate-800 border border-slate-700 rounded-lg text-white text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500"
          />
        </div>

        <button
          onClick={onOpenCheckIn}
          disabled={availableCount === 0 || isLoading}
          className="flex items-center justify-center space-x-2 px-5 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white font-semibold rounded-lg shadow-lg shadow-emerald-600/30 transition disabled:opacity-50 disabled:cursor-not-allowed text-sm"
        >
          <Plus className="w-4 h-4" />
          <span>Registrar Ingreso de Visita</span>
        </button>
      </div>

      {/* 20-Spot Interactive Visual Grid */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-base font-bold text-slate-200">
            Cuadrícula Interactiva de Parqueos de Visita (P-01 al P-20)
          </h2>
          <div className="flex items-center space-x-4 text-xs text-slate-400">
            <span className="flex items-center space-x-1.5">
              <span className="w-3 h-3 rounded bg-emerald-500/20 border border-emerald-500/60"></span>
              <span>Disponible</span>
            </span>
            <span className="flex items-center space-x-1.5">
              <span className="w-3 h-3 rounded bg-rose-500/20 border border-rose-500/60"></span>
              <span>Ocupado</span>
            </span>
          </div>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3.5">
          {filteredSpots.map(spot => {
            const entry = entryBySpot.get(spot.spotNumber);
            const isOccupied = spot.isOccupied;

            return (
              <div
                key={spot.id}
                className={`relative flex flex-col justify-between p-3.5 rounded-xl border transition-all duration-200 ${
                  isOccupied
                    ? 'bg-slate-900 border-rose-900/60 shadow-lg shadow-rose-950/20 hover:border-rose-700'
                    : 'bg-slate-900/50 border-emerald-900/40 hover:border-emerald-600/60'
                }`}
              >
                {/* Spot Header */}
                <div className="flex items-center justify-between">
                  <span
                    className={`font-mono text-sm font-black px-2 py-0.5 rounded ${
                      isOccupied
                        ? 'bg-rose-950 text-rose-300 border border-rose-800'
                        : 'bg-emerald-950 text-emerald-300 border border-emerald-800'
                    }`}
                  >
                    {spot.spotNumber}
                  </span>

                  <span
                    className={`text-[11px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full ${
                      isOccupied
                        ? 'bg-rose-500/10 text-rose-400'
                        : 'bg-emerald-500/10 text-emerald-400'
                    }`}
                  >
                    {isOccupied ? 'Ocupado' : 'Libre'}
                  </span>
                </div>

                {/* Spot Content */}
                {isOccupied && entry ? (
                  <div className="mt-3 space-y-2">
                    {/* Plate Display */}
                    <div className="flex items-center justify-between bg-slate-950 px-2.5 py-1.5 rounded-lg border border-slate-800">
                      <span className="font-mono text-base font-extrabold text-amber-300 tracking-wider">
                        {entry.plate}
                      </span>
                      {entry.isOvernight && (
                        <span
                          title="Vehículo pernoctó en el condominio"
                          className="flex items-center text-[10px] text-amber-400 bg-amber-950/60 border border-amber-800/80 px-1.5 py-0.5 rounded font-medium"
                        >
                          <Moon className="w-3 h-3 mr-1 text-amber-400" />
                          Noche
                        </span>
                      )}
                    </div>

                    {/* Metadata */}
                    <div className="text-xs space-y-1 text-slate-300">
                      <div className="flex items-center space-x-1.5 truncate">
                        <Home className="w-3.5 h-3.5 text-slate-500 flex-shrink-0" />
                        <span className="font-medium text-slate-200 truncate">
                          {entry.houseNumber}
                        </span>
                      </div>
                      <div className="flex items-center space-x-1.5 truncate">
                        <User className="w-3.5 h-3.5 text-slate-500 flex-shrink-0" />
                        <span className="text-slate-400 truncate">{entry.visitorName}</span>
                      </div>
                      <div className="flex items-center space-x-1.5 text-slate-400 text-[11px]">
                        <Clock className="w-3 h-3 text-slate-500 flex-shrink-0" />
                        <span>{calculateDuration(entry.entryTime)}</span>
                      </div>
                    </div>

                    {/* Fast 1-click Exit Button */}
                    <button
                      onClick={() => handleExitClick(entry.id, spot.spotNumber)}
                      disabled={exitingId === entry.id}
                      className="w-full mt-2 flex items-center justify-center space-x-1.5 py-1.5 px-2 bg-rose-600/20 hover:bg-rose-600 text-rose-300 hover:text-white rounded-lg text-xs font-semibold border border-rose-700/60 hover:border-transparent transition-all"
                    >
                      <LogOut className="w-3.5 h-3.5" />
                      <span>{exitingId === entry.id ? 'Registrando...' : 'Registrar Salida'}</span>
                    </button>
                  </div>
                ) : (
                  <div className="my-6 flex flex-col items-center justify-center text-center">
                    <Car className="w-8 h-8 text-slate-700" />
                    <span className="text-xs text-slate-500 mt-1">Listo para asignar</span>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Active Vehicles List Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden mt-6">
        <div className="px-6 py-4 bg-slate-800/40 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Car className="w-5 h-5 text-emerald-400" />
            <h3 className="font-bold text-white text-base">
              Lista Detallada de Vehículos de Visita Activos ({activeEntries.length})
            </h3>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-950/60 text-xs uppercase tracking-wider text-slate-400 border-b border-slate-800">
              <tr>
                <th className="px-6 py-3">Espacio</th>
                <th className="px-6 py-3">Placa</th>
                <th className="px-6 py-3">Destino</th>
                <th className="px-6 py-3">Visitante</th>
                <th className="px-6 py-3">Hora de Ingreso</th>
                <th className="px-6 py-3">Tiempo Transcurrido</th>
                <th className="px-6 py-3 text-right">Acción</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {activeEntries.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-8 text-center text-slate-500 text-sm">
                    No hay vehículos de visita estacionados actualmente en el residencial.
                  </td>
                </tr>
              ) : (
                activeEntries.map(entry => (
                  <tr key={entry.id} className="hover:bg-slate-800/40 transition">
                    <td className="px-6 py-4">
                      <span className="font-mono font-bold bg-slate-800 px-2 py-1 rounded text-white text-xs border border-slate-700">
                        {entry.spotNumber}
                      </span>
                    </td>
                    <td className="px-6 py-4 font-mono font-bold text-amber-300 text-sm">
                      {entry.plate}
                    </td>
                    <td className="px-6 py-4 text-white font-medium">{entry.houseNumber}</td>
                    <td className="px-6 py-4">
                      <div className="text-white font-medium">{entry.visitorName}</div>
                      <div className="text-xs text-slate-400">{entry.visitorIdDocument}</div>
                    </td>
                    <td className="px-6 py-4 text-slate-300 text-xs">
                      {new Date(entry.entryTime).toLocaleTimeString([], {
                        hour: '2-digit',
                        minute: '2-digit'
                      })}
                    </td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center text-xs text-slate-300 bg-slate-800 px-2.5 py-1 rounded-md">
                        <Clock className="w-3 h-3 mr-1 text-slate-400" />
                        {calculateDuration(entry.entryTime)}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => handleExitClick(entry.id, entry.spotNumber)}
                        disabled={exitingId === entry.id}
                        className="px-3 py-1.5 bg-rose-600/20 hover:bg-rose-600 text-rose-300 hover:text-white rounded-lg text-xs font-semibold border border-rose-700/60 hover:border-transparent transition"
                      >
                        {exitingId === entry.id ? 'Registrando...' : 'Marcar Salida'}
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
