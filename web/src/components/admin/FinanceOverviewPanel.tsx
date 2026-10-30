import React, { useState } from 'react';
import {
  Wallet,
  Receipt,
  FileSpreadsheet,
  Boxes,
  UploadCloud,
  CheckCircle2,
  FileText
} from 'lucide-react';
import toast from 'react-hot-toast';
import { Invoice, FixedAsset } from '../../types';

interface FinanceOverviewPanelProps {
  invoices: Invoice[];
  assets: FixedAsset[];
}

export const FinanceOverviewPanel: React.FC<FinanceOverviewPanelProps> = ({
  invoices,
  assets
}) => {
  const [activeFinanceTab, setActiveFinanceTab] = useState<'INVOICES' | 'RECONCILIATION' | 'ASSETS'>('INVOICES');
  const [isUploading, setIsUploading] = useState(false);
  const [reconciliationResult, setReconciliationResult] = useState<{
    totalRecords: number;
    matchedRecords: number;
    matchRate: number;
    totalAmountMatched: number;
  } | null>(null);

  const handleSimulateUpload = () => {
    setIsUploading(true);
    const toastId = toast.loading('Procesando archivo bancario...');
    setTimeout(() => {
      setIsUploading(false);
      setReconciliationResult({
        totalRecords: 48,
        matchedRecords: 46,
        matchRate: 95.8,
        totalAmountMatched: 42350.0
      });
      toast.success('Conciliación bancaria completada (95.8% de cruce)', { id: toastId });
    }, 1200);
  };

  const totalInvoicesAmount = invoices.reduce((acc, curr) => acc + curr.amount, 0);
  const totalAssetsValue = assets.reduce((acc, curr) => acc + curr.acquisitionCost, 0);

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="bg-gradient-to-r from-teal-50 via-white to-white dark:from-teal-950/60 dark:via-slate-900 dark:to-slate-900 border border-teal-200 dark:border-teal-900/40 p-6 rounded-2xl shadow-sm dark:shadow-xl transition-colors">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center space-x-2 text-teal-600 dark:text-teal-400 font-bold text-xs uppercase tracking-wider mb-1">
              <Wallet className="w-4 h-4" />
              <span>Finanzas y Activos &middot; Épica 2 (US-201, US-202, US-203, US-205)</span>
            </div>
            <h2 className="text-2xl font-black text-slate-900 dark:text-white tracking-tight">
              Control Financiero, Conciliación Bancaria y Activos Fijos
            </h2>
            <p className="text-slate-600 dark:text-slate-400 text-sm mt-1 max-w-2xl">
              Registro de facturas con respaldo digital, motor de conciliación automática de
              estados de cuenta bancarios (Excel) e inventario patrimonial valorizado.
            </p>
          </div>

          <div className="flex items-center space-x-3">
            <div className="bg-white dark:bg-slate-900/90 border border-slate-200 dark:border-slate-800 px-4 py-2.5 rounded-xl text-center shadow-sm">
              <span className="block text-xl font-mono font-bold text-teal-600 dark:text-teal-400">
                Q. {totalInvoicesAmount.toLocaleString('en-US', { minimumFractionDigits: 2 })}
              </span>
              <span className="text-[11px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                Gasto Mensual
              </span>
            </div>
            <div className="bg-white dark:bg-slate-900/90 border border-slate-200 dark:border-slate-800 px-4 py-2.5 rounded-xl text-center shadow-sm">
              <span className="block text-xl font-mono font-bold text-emerald-600 dark:text-emerald-400">
                Q. {totalAssetsValue.toLocaleString('en-US', { minimumFractionDigits: 2 })}
              </span>
              <span className="text-[11px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
                Activos Fijos
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Subtabs Switcher */}
      <div className="flex items-center space-x-3 border-b border-slate-200 dark:border-slate-800 pb-3">
        <button
          onClick={() => setActiveFinanceTab('INVOICES')}
          className={`flex items-center space-x-2 px-4 py-2 rounded-xl text-xs font-bold transition ${
            activeFinanceTab === 'INVOICES'
              ? 'bg-white dark:bg-slate-800 text-teal-600 dark:text-teal-400 border border-slate-300 dark:border-slate-700 shadow-sm'
              : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
          }`}
        >
          <Receipt className="w-4 h-4" />
          <span>Facturas &amp; Egresos (US-201, 202)</span>
        </button>

        <button
          onClick={() => setActiveFinanceTab('RECONCILIATION')}
          className={`flex items-center space-x-2 px-4 py-2 rounded-xl text-xs font-bold transition ${
            activeFinanceTab === 'RECONCILIATION'
              ? 'bg-white dark:bg-slate-800 text-teal-600 dark:text-teal-400 border border-slate-300 dark:border-slate-700 shadow-sm'
              : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
          }`}
        >
          <FileSpreadsheet className="w-4 h-4" />
          <span>Conciliación Bancaria Excel (US-203)</span>
        </button>

        <button
          onClick={() => setActiveFinanceTab('ASSETS')}
          className={`flex items-center space-x-2 px-4 py-2 rounded-xl text-xs font-bold transition ${
            activeFinanceTab === 'ASSETS'
              ? 'bg-white dark:bg-slate-800 text-teal-600 dark:text-teal-400 border border-slate-300 dark:border-slate-700 shadow-sm'
              : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200'
          }`}
        >
          <Boxes className="w-4 h-4" />
          <span>Inventario de Activos (US-205)</span>
        </button>
      </div>

      {/* Tab: Invoices */}
      {activeFinanceTab === 'INVOICES' && (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl overflow-hidden shadow-sm dark:shadow-lg">
          <div className="px-6 py-4 bg-slate-50 dark:bg-slate-800/40 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between">
            <h3 className="font-bold text-slate-900 dark:text-white text-base flex items-center space-x-2">
              <Receipt className="w-5 h-5 text-teal-600 dark:text-teal-400" />
              <span>Registro de Facturas con Soporte Digital</span>
            </h3>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-700 dark:text-slate-300">
              <thead className="bg-slate-50 dark:bg-slate-950/60 text-xs uppercase tracking-wider text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800">
                <tr>
                  <th className="px-6 py-3">No. Factura</th>
                  <th className="px-6 py-3">Proveedor</th>
                  <th className="px-6 py-3">Categoría de Gasto</th>
                  <th className="px-6 py-3">Fecha</th>
                  <th className="px-6 py-3">Monto (GTQ)</th>
                  <th className="px-6 py-3">Soporte Digital</th>
                  <th className="px-6 py-3 text-right">Estado</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200 dark:divide-slate-800">
                {invoices.map(inv => (
                  <tr key={inv.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition">
                    <td className="px-6 py-4 font-mono font-bold text-teal-700 dark:text-teal-300">
                      {inv.invoiceNumber}
                    </td>
                    <td className="px-6 py-4 font-semibold text-slate-900 dark:text-white">{inv.providerName}</td>
                    <td className="px-6 py-4 text-xs">
                      <span className="bg-slate-100 dark:bg-slate-800 px-2 py-0.5 rounded text-slate-700 dark:text-slate-300 border border-slate-200 dark:border-slate-700">
                        {inv.expenseCategory}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-xs text-slate-500 dark:text-slate-400">{inv.invoiceDate}</td>
                    <td className="px-6 py-4 font-mono font-bold text-emerald-600 dark:text-emerald-400">
                      Q. {inv.amount.toFixed(2)}
                    </td>
                    <td className="px-6 py-4">
                      {inv.digitalSupportUrl ? (
                        <a
                          href={inv.digitalSupportUrl}
                          target="_blank"
                          rel="noreferrer"
                          className="inline-flex items-center space-x-1 text-xs text-blue-600 dark:text-blue-400 hover:underline"
                        >
                          <FileText className="w-3.5 h-3.5" />
                          <span>Ver Comprobante PDF</span>
                        </a>
                      ) : (
                        <span className="text-xs text-slate-400 italic">Pendiente de adjuntar</span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <span
                        className={`text-[11px] font-bold uppercase tracking-wider px-2 py-0.5 rounded ${
                          inv.status === 'PAID'
                            ? 'bg-emerald-50 dark:bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-500/30'
                            : 'bg-amber-50 dark:bg-amber-500/10 text-amber-700 dark:text-amber-400 border border-amber-200 dark:border-amber-500/30'
                        }`}
                      >
                        {inv.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab: Bank Reconciliation */}
      {activeFinanceTab === 'RECONCILIATION' && (
        <div className="space-y-6">
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-8 text-center space-y-4 shadow-sm">
            <div className="w-16 h-16 mx-auto rounded-2xl bg-teal-50 dark:bg-teal-500/10 text-teal-600 dark:text-teal-400 flex items-center justify-center">
              <UploadCloud className="w-8 h-8" />
            </div>

            <div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                Carga y Conciliación Automática de Estado de Cuenta Bancario
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400 max-w-md mx-auto mt-1">
                Suba el archivo formato Excel (.xlsx, .csv) emitido por Banco Industrial, BAM o G&amp;T.
                El algoritmo emparejará depósitos contra cuotas ordinarias y pagos de visitas.
              </p>
            </div>

            <div className="max-w-md mx-auto">
              <button
                onClick={handleSimulateUpload}
                disabled={isUploading}
                className="w-full py-3 px-6 bg-teal-600 hover:bg-teal-500 text-white font-semibold rounded-xl text-sm shadow-lg shadow-teal-600/30 transition flex items-center justify-center space-x-2 disabled:opacity-50"
              >
                <FileSpreadsheet className="w-4 h-4" />
                <span>
                  {isUploading ? 'Procesando Conciliación...' : 'Cargar Archivo Excel de Prueba'}
                </span>
              </button>
            </div>
          </div>

          {reconciliationResult && (
            <div className="bg-white dark:bg-slate-900 border border-emerald-300 dark:border-emerald-900/60 rounded-2xl p-6 shadow-sm dark:shadow-xl space-y-4 animate-fade-in">
              <div className="flex items-center space-x-2 text-emerald-600 dark:text-emerald-400 font-bold text-base">
                <CheckCircle2 className="w-5 h-5" />
                <span>Conciliación Bancaria Procesada Exitosamente</span>
              </div>

              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div className="bg-slate-50 dark:bg-slate-950 p-4 rounded-xl border border-slate-200 dark:border-slate-800">
                  <span className="text-xs text-slate-500 dark:text-slate-400 block">Total Movimientos:</span>
                  <span className="text-xl font-mono font-bold text-slate-900 dark:text-white">
                    {reconciliationResult.totalRecords} transacciones
                  </span>
                </div>
                <div className="bg-slate-50 dark:bg-slate-950 p-4 rounded-xl border border-slate-200 dark:border-slate-800">
                  <span className="text-xs text-slate-500 dark:text-slate-400 block">Movimientos Cuadrados:</span>
                  <span className="text-xl font-mono font-bold text-emerald-600 dark:text-emerald-400">
                    {reconciliationResult.matchedRecords} coincidentes
                  </span>
                </div>
                <div className="bg-slate-50 dark:bg-slate-950 p-4 rounded-xl border border-slate-200 dark:border-slate-800">
                  <span className="text-xs text-slate-500 dark:text-slate-400 block">Efectividad de Cruce:</span>
                  <span className="text-xl font-mono font-bold text-teal-600 dark:text-teal-400">
                    {reconciliationResult.matchRate}%
                  </span>
                </div>
                <div className="bg-slate-50 dark:bg-slate-950 p-4 rounded-xl border border-slate-200 dark:border-slate-800">
                  <span className="text-xs text-slate-500 dark:text-slate-400 block">Monto Conciliado:</span>
                  <span className="text-xl font-mono font-bold text-emerald-600 dark:text-emerald-400">
                    Q. {reconciliationResult.totalAmountMatched.toFixed(2)}
                  </span>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Tab: Fixed Assets */}
      {activeFinanceTab === 'ASSETS' && (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl overflow-hidden shadow-sm dark:shadow-lg">
          <div className="px-6 py-4 bg-slate-50 dark:bg-slate-800/40 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between">
            <h3 className="font-bold text-slate-900 dark:text-white text-base flex items-center space-x-2">
              <Boxes className="w-5 h-5 text-teal-600 dark:text-teal-400" />
              <span>Inventario Físico de Activos Comunitarios</span>
            </h3>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-700 dark:text-slate-300">
              <thead className="bg-slate-50 dark:bg-slate-950/60 text-xs uppercase tracking-wider text-slate-500 dark:text-slate-400 border-b border-slate-200 dark:border-slate-800">
                <tr>
                  <th className="px-6 py-3">Código de Activo</th>
                  <th className="px-6 py-3">Descripción del Bien</th>
                  <th className="px-6 py-3">Categoría</th>
                  <th className="px-6 py-3">Ubicación Física</th>
                  <th className="px-6 py-3">Valor de Adquisición</th>
                  <th className="px-6 py-3 text-right">Estado Operativo</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200 dark:divide-slate-800">
                {assets.map(asset => (
                  <tr key={asset.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition">
                    <td className="px-6 py-4 font-mono font-bold text-teal-700 dark:text-teal-300">{asset.assetCode}</td>
                    <td className="px-6 py-4 font-semibold text-slate-900 dark:text-white">{asset.name}</td>
                    <td className="px-6 py-4 text-xs">
                      <span className="bg-slate-100 dark:bg-slate-800 px-2 py-0.5 rounded text-slate-700 dark:text-slate-300 border border-slate-200 dark:border-slate-700">
                        {asset.category}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-xs text-slate-500 dark:text-slate-400">{asset.location}</td>
                    <td className="px-6 py-4 font-mono font-bold text-emerald-600 dark:text-emerald-400">
                      Q. {asset.acquisitionCost.toFixed(2)}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <span
                        className={`text-[11px] font-bold uppercase tracking-wider px-2 py-0.5 rounded ${
                          asset.physicalStatus === 'EXCELLENT'
                            ? 'bg-emerald-50 dark:bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-500/30'
                            : 'bg-amber-50 dark:bg-amber-500/10 text-amber-700 dark:text-amber-400 border border-amber-200 dark:border-amber-500/30'
                        }`}
                      >
                        {asset.physicalStatus}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};
