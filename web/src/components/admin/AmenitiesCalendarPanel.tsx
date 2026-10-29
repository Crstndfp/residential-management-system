import React, { useState, useEffect } from 'react';
import {
  CalendarDays,
  Clock,
  DollarSign,
  Users,
  CheckCircle,
  Plus,
  EyeOff
} from 'lucide-react';
import { Amenity, CalendarSlot } from '../../types';
import { api } from '../../services/api';

interface AmenitiesCalendarPanelProps {
  amenities: Amenity[];
}

export const AmenitiesCalendarPanel: React.FC<AmenitiesCalendarPanelProps> = ({
  amenities
}) => {
  const [selectedAmenity, setSelectedAmenity] = useState<Amenity>(amenities[0] || null);
  const [selectedDate, setSelectedDate] = useState<string>(
    new Date().toISOString().split('T')[0]
  );
  const [slots, setSlots] = useState<CalendarSlot[]>([]);
  const [isLoadingSlots, setIsLoadingSlots] = useState(false);
  const [bookingSuccess, setBookingSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (selectedAmenity) {
      loadSlots(selectedAmenity.id, selectedDate);
    }
  }, [selectedAmenity, selectedDate]);

  const loadSlots = async (amenityId: string, date: string) => {
    try {
      setIsLoadingSlots(true);
      const data = await api.getCalendarSlots(amenityId, date);
      setSlots(data);
    } finally {
      setIsLoadingSlots(false);
    }
  };

  const handleSimulateBooking = (slot: CalendarSlot) => {
    setSlots(prev =>
      prev.map(s =>
        s.startTime === slot.startTime
          ? { ...s, isOccupied: true, displayStatus: 'Reservado' }
          : s
      )
    );
    setBookingSuccess(`Horario ${slot.startTime} - ${slot.endTime} reservado con éxito.`);
    setTimeout(() => setBookingSuccess(null), 4000);
  };

  return (
    <div className="space-y-6">
      {/* Toast Notification */}
      {bookingSuccess && (
        <div className="fixed bottom-6 right-6 z-50 bg-purple-600 text-white px-5 py-3 rounded-xl shadow-2xl flex items-center space-x-3 border border-purple-400/40 animate-bounce">
          <CheckCircle className="w-5 h-5 flex-shrink-0" />
          <span className="font-medium text-sm">{bookingSuccess}</span>
        </div>
      )}

      {/* Header Banner */}
      <div className="bg-gradient-to-r from-purple-950/60 via-slate-900 to-slate-900 border border-purple-900/40 p-6 rounded-2xl shadow-xl">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center space-x-2 text-purple-400 font-bold text-xs uppercase tracking-wider mb-1">
              <CalendarDays className="w-4 h-4" />
              <span>Amenidades &amp; Privacidad &middot; Épica 3 (US-301 &amp; US-302)</span>
            </div>
            <h2 className="text-2xl font-black text-white tracking-tight">
              Gestión de Espacios Comunes y Calendario de Disponibilidad
            </h2>
            <p className="text-slate-400 text-sm mt-1 max-w-2xl">
              Configuración de áreas comunes y calendario protegido por privacidad: los
              residentes y usuarios sólo pueden visualizar los estados genéricos de
              disponibilidad sin exponer nombres ni números de casa.
            </p>
          </div>

          <div className="flex items-center space-x-2 bg-purple-950/60 border border-purple-800/80 px-4 py-2.5 rounded-xl text-xs text-purple-300">
            <EyeOff className="w-4 h-4 text-purple-400 flex-shrink-0" />
            <span>Filtro de Privacidad Activo (US-302)</span>
          </div>
        </div>
      </div>

      {/* Amenities Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {amenities.map(amenity => {
          const isSelected = selectedAmenity?.id === amenity.id;
          return (
            <div
              key={amenity.id}
              onClick={() => setSelectedAmenity(amenity)}
              className={`cursor-pointer rounded-2xl p-5 border transition-all duration-200 ${
                isSelected
                  ? 'bg-slate-900 border-purple-500 shadow-xl shadow-purple-950/30 ring-1 ring-purple-500'
                  : 'bg-slate-900/60 border-slate-800 hover:border-slate-700'
              }`}
            >
              <div className="flex items-center justify-between">
                <span className="font-mono text-xs font-bold px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
                  {amenity.code}
                </span>
                <span className="text-[11px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400">
                  Activo
                </span>
              </div>

              <h4 className="font-bold text-white text-base mt-2">{amenity.name}</h4>
              <p className="text-xs text-slate-400 mt-1 line-clamp-2">{amenity.description}</p>

              <div className="grid grid-cols-2 gap-2 mt-4 text-xs pt-3 border-t border-slate-800 text-slate-300">
                <div className="flex items-center space-x-1.5">
                  <Users className="w-3.5 h-3.5 text-purple-400" />
                  <span>Cap. {amenity.capacity} pers.</span>
                </div>
                <div className="flex items-center space-x-1.5 font-semibold text-emerald-400">
                  <DollarSign className="w-3.5 h-3.5 text-emerald-400" />
                  <span>Q. {amenity.rentalCost.toFixed(2)}</span>
                </div>
                <div className="flex items-center space-x-1.5 text-slate-400">
                  <Clock className="w-3.5 h-3.5" />
                  <span>
                    {amenity.openingTime} - {amenity.closingTime}
                  </span>
                </div>
                <div className="flex items-center space-x-1.5 text-slate-400">
                  <span>Depósito: Q. {amenity.depositCost.toFixed(2)}</span>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Selected Amenity Privacy Calendar */}
      {selectedAmenity && (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl p-6 space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
            <div>
              <h3 className="text-lg font-bold text-white flex items-center space-x-2">
                <span>Calendario de Disponibilidad: {selectedAmenity.name}</span>
              </h3>
              <p className="text-xs text-slate-400 mt-0.5">
                Cumplimiento estricto US-302: Visualización pública de bloques disponibles vs.
                reservados
              </p>
            </div>

            <div className="flex items-center space-x-2">
              <label className="text-xs font-semibold uppercase text-slate-400">Fecha:</label>
              <input
                type="date"
                value={selectedDate}
                onChange={e => setSelectedDate(e.target.value)}
                className="bg-slate-800 border border-slate-700 px-3 py-1.5 rounded-lg text-white text-xs focus:outline-none focus:ring-2 focus:ring-purple-500"
              />
            </div>
          </div>

          {/* Time Slot List */}
          <div className="space-y-3">
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400">
              Franjas Horarias del Día ({selectedDate})
            </h4>

            {isLoadingSlots ? (
              <div className="py-8 text-center text-slate-400 text-xs">Cargando franjas...</div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
                {slots.map((slot, idx) => (
                  <div
                    key={idx}
                    className={`p-4 rounded-xl border flex flex-col justify-between space-y-3 transition ${
                      slot.isOccupied
                        ? 'bg-slate-950/70 border-slate-800 text-slate-400'
                        : 'bg-emerald-950/20 border-emerald-800/40 hover:border-emerald-500'
                    }`}
                  >
                    <div>
                      <div className="flex items-center justify-between">
                        <span className="font-mono text-xs font-bold text-white">
                          {slot.startTime} - {slot.endTime}
                        </span>
                        <span
                          className={`text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full ${
                            slot.isOccupied
                              ? 'bg-slate-800 text-slate-400'
                              : 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40'
                          }`}
                        >
                          {slot.displayStatus}
                        </span>
                      </div>
                      <p className="text-[11px] text-slate-500 mt-2">
                        {slot.isOccupied
                          ? 'Bloque bloqueado por reservación existente.'
                          : 'Disponible para reservación inmediata.'}
                      </p>
                    </div>

                    {!slot.isOccupied && (
                      <button
                        onClick={() => handleSimulateBooking(slot)}
                        className="w-full py-1.5 px-3 bg-purple-600 hover:bg-purple-500 text-white rounded-lg text-xs font-semibold shadow-md shadow-purple-600/20 transition flex items-center justify-center space-x-1"
                      >
                        <Plus className="w-3.5 h-3.5" />
                        <span>Reservar Franja</span>
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
