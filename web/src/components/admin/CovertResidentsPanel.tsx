import React, { useState } from 'react';
import {
  ShieldAlert,
  Moon,
  AlertTriangle,
  History,
  DollarSign,
  FileText,
  Calendar,
  X
} from 'lucide-react';
import toast from 'react-hot-toast';
import { CovertResidentAlert, ParkingFine } from '../../types';

interface CovertResidentsPanelProps {
  alerts: CovertResidentAlert[];
  fines: ParkingFine[];
  onApplyFine: (payload: {
    plate: string;
    houseId: string;
    houseNumber: string;
    fineAmount: number;
    reason: string;
    evidenceSummary: string;
    appliedByUserId: string;
    appliedByName: string;
  }) => Promise<void>;
}

export const CovertResidentsPanel: React.FC<CovertResidentsPanelProps> = ({
  alerts,
  fines,
  onApplyFine
}) => {
  const [selectedAlertForHistory, setSelectedAlertForHistory] = useState<CovertResidentAlert | null>(null);
  const [fineModalAlert, setFineModalAlert] = useState<CovertResidentAlert | null>(null);
  const [fineAmount, setFineAmount] = useState(250.0);
  const [fineReason, setFineReason] = useState(
    'Uso no autorizado de parqueo de visitas como estacionamiento permanente (≥ 3 pernoctaciones sin autorización)'
  );
  const [isSubmittingFine, setIsSubmittingFine] = useState(false);

  const handleOpenFineModal = (alert: CovertResidentAlert) => {
    setFineModalAlert(alert);
    setFineReason(
      `Uso abusivo de parqueo de visitas. Se detectaron ${alert.overnightCount} pernoctaciones del vehículo placa ${alert.plate} vinculado a ${alert.houseNumber}.`
    );
  };

  const handleConfirmFine = async () => {
    if (!fineModalAlert) return;
    try {
      setIsSubmittingFine(true);
      await onApplyFine({
        plate: fineModalAlert.plate,
        houseId: fineModalAlert.houseId,
        houseNumber: fineModalAlert.houseNumber,
        fineAmount: Number(fineAmount),
        reason: fineReason,
        evidenceSummary: `Detección algorítmica: ${fineModalAlert.overnightCount} pernoctaciones registradas. Último ingreso: ${new Date(
          fineModalAlert.lastDetectedEntry || Date.now()
        ).toLocaleString()}`,
        appliedByUserId: 'admin-board-current',
        appliedByName: 'Administración Residencial'
      });
      toast.success(`Multa de Q. ${fineAmount.toFixed(2)} aplicada exitosamente a ${fineModalAlert.houseNumber}`);
      setFineModalAlert(null);
    } catch {
      toast.error('Error al aplicar la multa');
    } finally {
      setIsSubmittingFine(false);
    }
  };

  const suspectCount = alerts.filter(a => a.isSuspect).length;
  const totalOvernights = alerts.reduce((acc, curr) => acc + curr.overnightCount, 0);

  return (
    <div className="space-y-6">
      {/* Summary Header */}
      <div className="bg-gradient-to-r from-rose-50 via-white to-white dark:from-rose-950/60 dark:via-slate-900 dark:to-slate-900 border border-rose-200 dark:border-rose-900/40 p-6 rounded-2xl shadow-sm dark:shadow-xl transition-colors">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center space-x-2 text-rose-600 dark:text-rose-400 font-bold text-sm uppercase tracking-wider mb-1">
              <ShieldAlert className="w-4 h-4" />
              <span>Auditoría de Seguridad &middot; US-602 &amp; US-603</span>
            </div>
            <h2 className="text-2xl font-black text-slate-900 dark:text-white tracking-tight">
              Detección de Residentes Encubiertos y Multas de Parqueo
            </h2>
            <p className="text-slate-600 dark:text-slate-400 text-sm mt-1 max-w-2xl">
              Monitoreo analítico de vehículos visitantes que pernoctan 3 o más noches en el
              residencial. Generación de evidencia auditable y aplicación de sanciones
              financieras directas.
            </p>
          </div>

          <div className="flex items-center space-x-3">
            <div className="bg-white dark:bg-slate-900/90 border border-rose-200 dark:border-rose-800/80 px-4 py-2.5 rounded-xl text-center shadow-sm">
              <span className="block text-2xl font-black text-rose-600 dark:text-rose-400">{suspectCount}</span>
              <span className="text-[11px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                Sospechosos
              </span>
            </div>
            <div className="bg-white dark:bg-slate-900/90 border border-slate-200 dark:border-slate-800 px-4 py-2.5 rounded-xl text-center shadow-sm">
              <span className="block text-2xl font-black text-amber-600 dark:text-amber-400">{totalOvernights}</span>
              <span className="text-[11px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                Pernoctaciones
              </span>
            </div>
            <div className="bg-white dark:bg-slate-900/90 border border-slate-200 dark:border-slate-800 px-4 py-2.5 rounded-xl text-center shadow-sm">
              <span className="block text-2xl font-black text-emerald-600 dark:text-emerald-400">{fines.length}</span>
              <span className="text-[11px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                Multas
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Suspect Alert Table */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl overflow-hidden shadow-sm dark:shadow-lg">
        <div className="px-6 py-4 bg-slate-50 dark:bg-slate-800/40 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <AlertTriangle className="w-5 h-5 text-rose-600 dark:text-rose-400" />
            <h3 className="font-bold text-slate-900 dark:text-white text-base">
              Vehículos con Alerta de Residente Encubierto (&ge; 3 Pernoctaciones)
            </h3>
          </div>
          <span className="text-xs bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300 border border-rose-300 dark:border-rose-800/80 px-2.5 py-1 rounded-full font-semibold">
            {alerts.length} casos detectados
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-700 dark:text-slate-300">
            <thead className="bg-slate-50 dark:bg-slate-950/60 text-xs uppercase tracking-wider text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800">
              <tr>
                <th className="px-6 py-3">Placa Vehicular</th>
                <th className="px-6 py-3">Inmueble Vinculado</th>
                <th className="px-6 py-3">Pernoctaciones</th>
                <th className="px-6 py-3">Nivel de Sospecha</th>
                <th className="px-6 py-3">Última Detección</th>
                <th className="px-6 py-3 text-right">Acciones de Junta</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200 dark:divide-slate-800">
              {alerts.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-8 text-center text-slate-400 text-sm">
                    Excelente: No se registran vehículos sospechosos de residencia encubierta.
                  </td>
                </tr>
              ) : (
                alerts.map(alert => (
                  <tr key={alert.plate} className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition">
                    <td className="px-6 py-4 font-mono font-extrabold text-amber-600 dark:text-amber-300 text-base">
                      {alert.plate}
                    </td>
                    <td className="px-6 py-4 font-semibold text-slate-900 dark:text-white">{alert.houseNumber}</td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center space-x-1.5 px-2.5 py-1 rounded-md text-xs font-bold bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300 border border-rose-300 dark:border-rose-800">
                        <Moon className="w-3.5 h-3.5 text-rose-600 dark:text-rose-400" />
                        <span>{alert.overnightCount} noches</span>
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-block px-2.5 py-0.5 rounded text-[11px] font-bold uppercase tracking-wider ${
                          alert.suspicionLevel === 'CRITICAL'
                            ? 'bg-rose-100 dark:bg-rose-500/20 text-rose-700 dark:text-rose-300 border border-rose-300 dark:border-rose-500/40'
                            : 'bg-amber-100 dark:bg-amber-500/20 text-amber-700 dark:text-amber-300 border border-amber-300 dark:border-amber-500/40'
                        }`}
                      >
                        {alert.suspicionLevel}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-xs text-slate-500 dark:text-slate-400">
                      {alert.lastDetectedEntry
                        ? new Date(alert.lastDetectedEntry).toLocaleString([], {
                            dateStyle: 'short',
                            timeStyle: 'short'
                          })
                        : 'Reciente'}
                    </td>
                    <td className="px-6 py-4 text-right space-x-2">
                      <button
                        onClick={() => setSelectedAlertForHistory(alert)}
                        className="inline-flex items-center space-x-1 px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 dark:bg-slate-800 dark:hover:bg-slate-700 dark:text-slate-200 rounded-lg text-xs font-medium border border-slate-200 dark:border-slate-700 transition"
                      >
                        <History className="w-3.5 h-3.5 text-slate-400" />
                        <span>Ver Evidencia</span>
                      </button>

                      <button
                        onClick={() => handleOpenFineModal(alert)}
                        className="inline-flex items-center space-x-1 px-3 py-1.5 bg-rose-600 hover:bg-rose-500 text-white rounded-lg text-xs font-semibold shadow-md shadow-rose-600/30 transition"
                      >
                        <DollarSign className="w-3.5 h-3.5" />
                        <span>Aplicar Multa</span>
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Applied Fines History Table */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl overflow-hidden shadow-sm dark:shadow-lg">
        <div className="px-6 py-4 bg-slate-50 dark:bg-slate-800/40 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <FileText className="w-5 h-5 text-emerald-600 dark:text-emerald-400" />
            <h3 className="font-bold text-slate-900 dark:text-white text-base">Historial de Multas Emitidas ({fines.length})</h3>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-700 dark:text-slate-300">
            <thead className="bg-slate-50 dark:bg-slate-950/60 text-xs uppercase tracking-wider text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800">
              <tr>
                <th className="px-6 py-3">Fecha</th>
                <th className="px-6 py-3">Placa</th>
                <th className="px-6 py-3">Inmueble</th>
                <th className="px-6 py-3">Monto (GTQ)</th>
                <th className="px-6 py-3">Motivo &amp; Evidencia</th>
                <th className="px-6 py-3">Autorizado Por</th>
                <th className="px-6 py-3 text-right">Estado</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200 dark:divide-slate-800">
              {fines.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-8 text-center text-slate-400 text-sm">
                    No se han emitido multas de parqueo aún.
                  </td>
                </tr>
              ) : (
                fines.map(fine => (
                  <tr key={fine.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition">
                    <td className="px-6 py-4 text-xs text-slate-500 dark:text-slate-400">
                      {new Date(fine.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 font-mono font-bold text-amber-600 dark:text-amber-300">{fine.plate}</td>
                    <td className="px-6 py-4 text-slate-900 dark:text-white font-medium">{fine.houseNumber}</td>
                    <td className="px-6 py-4 font-mono font-bold text-emerald-600 dark:text-emerald-400">
                      Q. {fine.fineAmount.toFixed(2)}
                    </td>
                    <td className="px-6 py-4 text-xs max-w-xs">
                      <p className="text-slate-800 dark:text-slate-200 line-clamp-1">{fine.reason}</p>
                      <p className="text-slate-500 text-[11px] line-clamp-1">{fine.evidenceSummary}</p>
                    </td>
                    <td className="px-6 py-4 text-xs text-slate-500 dark:text-slate-400">{fine.appliedByName}</td>
                    <td className="px-6 py-4 text-right">
                      <span className="inline-block px-2.5 py-0.5 rounded text-[11px] font-bold uppercase tracking-wider bg-emerald-50 dark:bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-500/30">
                        {fine.status}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal: View Evidence Timeline */}
      {selectedAlertForHistory && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl shadow-2xl max-w-xl w-full overflow-hidden transition-colors">
            <div className="flex items-center justify-between px-6 py-4 bg-slate-50 dark:bg-slate-800/60 border-b border-slate-200 dark:border-slate-800">
              <div className="flex items-center space-x-2">
                <History className="w-5 h-5 text-rose-600 dark:text-rose-400" />
                <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                  Historial de Ingresos &middot; Placa {selectedAlertForHistory.plate}
                </h3>
              </div>
              <button
                onClick={() => setSelectedAlertForHistory(null)}
                className="text-slate-400 hover:text-slate-600 dark:hover:text-white p-1 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-6 space-y-4 max-h-[70vh] overflow-y-auto">
              <div className="bg-slate-50 dark:bg-slate-950 p-4 rounded-xl border border-slate-200 dark:border-slate-800 flex justify-between text-sm">
                <div>
                  <span className="text-slate-500 dark:text-slate-400 block text-xs">Inmueble Vinculado:</span>
                  <span className="font-bold text-slate-900 dark:text-white">{selectedAlertForHistory.houseNumber}</span>
                </div>
                <div>
                  <span className="text-slate-500 dark:text-slate-400 block text-xs">Pernoctaciones:</span>
                  <span className="font-bold text-rose-600 dark:text-rose-400">{selectedAlertForHistory.overnightCount} noches</span>
                </div>
                <div>
                  <span className="text-slate-500 dark:text-slate-400 block text-xs">Nivel de Alerta:</span>
                  <span className="font-bold text-amber-600 dark:text-amber-400">{selectedAlertForHistory.suspicionLevel}</span>
                </div>
              </div>

              <div className="space-y-3">
                <h4 className="text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
                  Bitácora Forense de Entradas
                </h4>
                {selectedAlertForHistory.history && selectedAlertForHistory.history.length > 0 ? (
                  selectedAlertForHistory.history.map((h, idx) => (
                    <div
                      key={h.id || idx}
                      className="bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700/60 p-3 rounded-lg flex items-center justify-between text-xs"
                    >
                      <div>
                        <div className="flex items-center space-x-2">
                          <Calendar className="w-3.5 h-3.5 text-slate-400" />
                          <span className="font-semibold text-slate-800 dark:text-white">
                            Entrada: {new Date(h.entryTime).toLocaleString()}
                          </span>
                        </div>
                        {h.exitTime && (
                          <div className="text-slate-500 dark:text-slate-400 text-[11px] mt-0.5">
                            Salida: {new Date(h.exitTime).toLocaleString()} ({h.durationMinutes} mins)
                          </div>
                        )}
                      </div>
                      <div className="flex items-center space-x-2">
                        <span className="font-mono bg-slate-200 dark:bg-slate-900 px-1.5 py-0.5 rounded text-slate-700 dark:text-slate-300">
                          {h.spotNumber}
                        </span>
                        {h.isOvernight && (
                          <span className="px-1.5 py-0.5 rounded bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300 text-[10px] font-bold border border-rose-300 dark:border-rose-800">
                            Pernoctó
                          </span>
                        )}
                      </div>
                    </div>
                  ))
                ) : (
                  <p className="text-xs text-slate-400 italic">
                    Sin registros adicionales en el historial temporal inmediato.
                  </p>
                )}
              </div>
            </div>

            <div className="px-6 py-4 bg-slate-50 dark:bg-slate-950/60 border-t border-slate-200 dark:border-slate-800 text-right">
              <button
                onClick={() => setSelectedAlertForHistory(null)}
                className="px-4 py-2 bg-slate-200 hover:bg-slate-300 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-800 dark:text-white rounded-lg text-sm font-medium transition"
              >
                Cerrar Detalle
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal: Apply Fine */}
      {fineModalAlert && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl shadow-2xl max-w-lg w-full overflow-hidden transition-colors">
            <div className="flex items-center justify-between px-6 py-4 bg-slate-50 dark:bg-slate-800/60 border-b border-slate-200 dark:border-slate-800">
              <div className="flex items-center space-x-2">
                <DollarSign className="w-5 h-5 text-rose-600 dark:text-rose-400" />
                <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                  Aplicar Multa Directa por Residencia Encubierta
                </h3>
              </div>
              <button
                onClick={() => setFineModalAlert(null)}
                className="text-slate-400 hover:text-slate-600 dark:hover:text-white p-1 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-6 space-y-4">
              <div className="bg-slate-50 dark:bg-slate-950 p-4 rounded-xl border border-slate-200 dark:border-slate-800 space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-slate-500 dark:text-slate-400">Placa Infractora:</span>
                  <span className="font-mono font-bold text-amber-600 dark:text-amber-300">{fineModalAlert.plate}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500 dark:text-slate-400">Inmueble Responsable:</span>
                  <span className="font-bold text-slate-900 dark:text-white">{fineModalAlert.houseNumber}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500 dark:text-slate-400">Pernoctaciones registradas:</span>
                  <span className="font-bold text-rose-600 dark:text-rose-400">{fineModalAlert.overnightCount} noches</span>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 dark:text-slate-300 mb-1">
                  Monto de la Sanción (GTQ) *
                </label>
                <div className="relative">
                  <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500 font-bold">
                    Q.
                  </span>
                  <input
                    type="number"
                    step="0.01"
                    value={fineAmount}
                    onChange={e => setFineAmount(parseFloat(e.target.value) || 0)}
                    className="w-full pl-8 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white font-mono font-bold text-base focus:outline-none focus:ring-2 focus:ring-rose-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-700 dark:text-slate-300 mb-1">
                  Motivo y Dictamen de Junta *
                </label>
                <textarea
                  rows={3}
                  value={fineReason}
                  onChange={e => setFineReason(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-slate-800 border border-slate-300 dark:border-slate-700 rounded-lg text-slate-900 dark:text-white text-xs focus:outline-none focus:ring-2 focus:ring-rose-500"
                />
              </div>

              <div className="p-3 bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-800/80 rounded-lg text-xs text-amber-800 dark:text-amber-300 flex items-start space-x-2">
                <AlertTriangle className="w-4 h-4 flex-shrink-0 text-amber-600 dark:text-amber-400 mt-0.5" />
                <span>
                  Al confirmar, esta multa se emitirá e impactará automáticamente el estado de cuenta y
                  cobro del módulo financiero para el inmueble {fineModalAlert.houseNumber}.
                </span>
              </div>
            </div>

            <div className="px-6 py-4 bg-slate-50 dark:bg-slate-950/60 border-t border-slate-200 dark:border-slate-800 flex justify-end space-x-3">
              <button
                type="button"
                onClick={() => setFineModalAlert(null)}
                disabled={isSubmittingFine}
                className="px-4 py-2 text-sm text-slate-600 dark:text-slate-300 hover:text-slate-900 dark:hover:text-white"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={handleConfirmFine}
                disabled={isSubmittingFine || fineAmount <= 0}
                className="px-5 py-2.5 bg-rose-600 hover:bg-rose-500 text-white rounded-lg text-sm font-semibold shadow-lg shadow-rose-600/30 transition disabled:opacity-50"
              >
                {isSubmittingFine ? 'Aplicando Multa...' : 'Emitir Multa y Cargar a Cuenta'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
