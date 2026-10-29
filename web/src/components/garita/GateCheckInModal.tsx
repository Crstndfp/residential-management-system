import React, { useState } from 'react';
import { X, Car, Home, User, CreditCard, CheckCircle2, ShieldAlert } from 'lucide-react';
import { ParkingSpot } from '../../types';

interface GateCheckInModalProps {
  isOpen: boolean;
  onClose: () => void;
  availableSpots: ParkingSpot[];
  onSubmit: (data: {
    plate: string;
    houseNumber: string;
    houseId: string;
    visitorName: string;
    visitorIdDocument: string;
    spotNumber: string;
    guardUserId: string;
  }) => Promise<void>;
}

export const GateCheckInModal: React.FC<GateCheckInModalProps> = ({
  isOpen,
  onClose,
  availableSpots,
  onSubmit
}) => {
  const [plate, setPlate] = useState('');
  const [houseNumber, setHouseNumber] = useState('Casa 101');
  const [visitorName, setVisitorName] = useState('');
  const [visitorIdDocument, setVisitorIdDocument] = useState('');
  const [spotNumber, setSpotNumber] = useState(availableSpots[0]?.spotNumber || 'P-01');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!plate.trim()) {
      setError('Ingrese la placa del vehículo.');
      return;
    }
    if (!visitorName.trim()) {
      setError('Ingrese el nombre del visitante.');
      return;
    }
    if (!spotNumber) {
      setError('Seleccione un espacio de parqueo disponible.');
      return;
    }

    try {
      setIsSubmitting(true);
      setError(null);
      await onSubmit({
        plate: plate.toUpperCase().trim(),
        houseNumber: houseNumber.trim(),
        houseId: `h-${houseNumber.replace(/\D/g, '') || '101'}`,
        visitorName: visitorName.trim(),
        visitorIdDocument: visitorIdDocument.trim() || 'DPI No presentado',
        spotNumber,
        guardUserId: 'guard-on-duty'
      });
      // reset form & close
      setPlate('');
      setVisitorName('');
      setVisitorIdDocument('');
      onClose();
    } catch (err: any) {
      setError(err?.message || 'Error al registrar ingreso');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-fade-in">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl max-w-lg w-full overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 bg-slate-800/60 border-b border-slate-800">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-emerald-500/20 text-emerald-400 rounded-lg">
              <Car className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-white">Registro de Ingreso de Visita</h3>
              <p className="text-xs text-slate-400">Garita de Control de Acceso Vehicular (US-601)</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="p-3 bg-red-950/50 border border-red-800 rounded-lg text-sm text-red-300 flex items-center space-x-2">
              <ShieldAlert className="w-4 h-4 flex-shrink-0 text-red-400" />
              <span>{error}</span>
            </div>
          )}

          {/* License Plate Input */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
              Placa de Circulación *
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                <Car className="w-4 h-4" />
              </div>
              <input
                type="text"
                value={plate}
                onChange={e => setPlate(e.target.value.toUpperCase())}
                placeholder="P-123XYZ"
                autoFocus
                className="w-full pl-10 pr-4 py-2.5 bg-slate-800/90 border border-slate-700 rounded-lg text-white font-mono text-base tracking-widest placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent uppercase"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* House Destination */}
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
                Casa de Destino *
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                  <Home className="w-4 h-4" />
                </div>
                <input
                  type="text"
                  value={houseNumber}
                  onChange={e => setHouseNumber(e.target.value)}
                  placeholder="Casa 101"
                  className="w-full pl-10 pr-3 py-2.5 bg-slate-800/90 border border-slate-700 rounded-lg text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>
            </div>

            {/* Parking Spot Selection */}
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
                Espacio Asignado *
              </label>
              <select
                value={spotNumber}
                onChange={e => setSpotNumber(e.target.value)}
                className="w-full px-3 py-2.5 bg-slate-800/90 border border-slate-700 rounded-lg text-white text-sm font-semibold focus:outline-none focus:ring-2 focus:ring-emerald-500"
              >
                {availableSpots.length === 0 ? (
                  <option value="">No hay parqueos libres</option>
                ) : (
                  availableSpots.map(s => (
                    <option key={s.id} value={s.spotNumber}>
                      {s.spotNumber} (Disponible)
                    </option>
                  ))
                )}
              </select>
            </div>
          </div>

          {/* Visitor Name */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
              Nombre Completo del Conductor / Visitante *
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                <User className="w-4 h-4" />
              </div>
              <input
                type="text"
                value={visitorName}
                onChange={e => setVisitorName(e.target.value)}
                placeholder="Ej. Juan Pérez López"
                className="w-full pl-10 pr-3 py-2.5 bg-slate-800/90 border border-slate-700 rounded-lg text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
              />
            </div>
          </div>

          {/* DPI / Document */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
              Documento de Identificación (DPI / Licencia)
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                <CreditCard className="w-4 h-4" />
              </div>
              <input
                type="text"
                value={visitorIdDocument}
                onChange={e => setVisitorIdDocument(e.target.value)}
                placeholder="DPI-2489123450101"
                className="w-full pl-10 pr-3 py-2.5 bg-slate-800/90 border border-slate-700 rounded-lg text-white text-sm placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
              />
            </div>
          </div>

          {/* Actions */}
          <div className="pt-4 flex items-center justify-end space-x-3 border-t border-slate-800">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="px-4 py-2.5 text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800 rounded-lg transition"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isSubmitting || availableSpots.length === 0}
              className="flex items-center space-x-2 px-5 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-sm rounded-lg shadow-lg shadow-emerald-600/30 transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <CheckCircle2 className="w-4 h-4" />
              <span>{isSubmitting ? 'Registrando...' : 'Confirmar e Ingresar'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
