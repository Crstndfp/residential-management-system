import React from 'react';
import {
  ShieldCheck,
  Building2,
  Car,
  Vote,
  CalendarDays,
  Wrench,
  Wallet,
  Radio,
  LogOut,
  UserCheck,
  Sun,
  Moon
} from 'lucide-react';
import { AuthUser, ThemeMode } from '../../types';

export type ViewMode = 'GARITA' | 'ADMIN';
export type AdminTab = 'PARKING' | 'ASSEMBLIES' | 'AMENITIES' | 'TICKETS' | 'FINANCE';

interface NavbarProps {
  viewMode: ViewMode;
  setViewMode: (mode: ViewMode) => void;
  adminTab: AdminTab;
  setAdminTab: (tab: AdminTab) => void;
  currentUser: AuthUser | null;
  onLogout: () => void;
  theme: ThemeMode;
  onToggleTheme: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  viewMode,
  setViewMode,
  adminTab,
  setAdminTab,
  currentUser,
  onLogout,
  theme,
  onToggleTheme
}) => {
  const isGuard = currentUser?.role === 'GUARD';

  return (
    <header className="bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800 text-slate-800 dark:text-white sticky top-0 z-40 shadow-sm dark:shadow-lg transition-colors">
      {/* Top Banner */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo & Residential Name */}
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-500 to-teal-400 flex items-center justify-center shadow-md shadow-emerald-500/20">
              <Building2 className="w-6 h-6 text-slate-950 font-bold" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <span className="font-bold text-lg tracking-tight text-slate-900 dark:text-slate-100">
                  Residencial Las Brisas
                </span>
                <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200 dark:bg-emerald-950 dark:text-emerald-400 dark:border-emerald-800/60">
                  <Radio className="w-3 h-3 mr-1 animate-pulse text-emerald-500 dark:text-emerald-400" />
                  En Línea
                </span>
              </div>
              <p className="text-xs text-slate-500 dark:text-slate-400 hidden sm:block">
                {isGuard
                  ? 'Puesto de Control de Garita Vehicular · Acceso Exclusivo'
                  : 'Sistema Integral de Gestión · Garita & Administración'}
              </p>
            </div>
          </div>

          {/* Right Action Bar: Mode Switcher, Theme Switcher, User Profile & Logout */}
          <div className="flex items-center space-x-2.5 sm:space-x-3">
            {/* Theme Toggle Button (Light / Dark) */}
            <button
              onClick={onToggleTheme}
              title={theme === 'dark' ? 'Cambiar a Tema Claro' : 'Cambiar a Tema Oscuro'}
              className="p-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-100 dark:bg-slate-950 text-slate-700 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-800 transition-colors shadow-sm"
            >
              {theme === 'dark' ? (
                <Sun className="w-4 h-4 text-amber-400 animate-spin-once" />
              ) : (
                <Moon className="w-4 h-4 text-slate-700" />
              )}
            </button>

            {/* Mode Switcher Buttons (Hidden if Guard) */}
            {!isGuard && (
              <div className="hidden md:flex items-center bg-slate-100 dark:bg-slate-950 p-1.5 rounded-xl border border-slate-200 dark:border-slate-800">
                <button
                  onClick={() => setViewMode('GARITA')}
                  className={`flex items-center space-x-2 px-3.5 py-1.5 rounded-lg font-medium text-xs sm:text-sm transition-all duration-200 ${
                    viewMode === 'GARITA'
                      ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/30'
                      : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-200 dark:hover:bg-slate-900'
                  }`}
                >
                  <ShieldCheck className="w-4 h-4" />
                  <span>Garita (Tablet)</span>
                </button>

                <button
                  onClick={() => setViewMode('ADMIN')}
                  className={`flex items-center space-x-2 px-3.5 py-1.5 rounded-lg font-medium text-xs sm:text-sm transition-all duration-200 ${
                    viewMode === 'ADMIN'
                      ? 'bg-blue-600 text-white shadow-md shadow-blue-600/30'
                      : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-200 dark:hover:bg-slate-900'
                  }`}
                >
                  <Building2 className="w-4 h-4" />
                  <span>Junta Directiva</span>
                </button>
              </div>
            )}

            {/* Authenticated User Badge & Logout Button */}
            {currentUser && (
              <div className="flex items-center space-x-2.5 bg-slate-100 dark:bg-slate-950/80 border border-slate-200 dark:border-slate-800 px-3 py-1.5 rounded-xl">
                <div className="flex items-center space-x-2">
                  <div
                    className={`w-7 h-7 rounded-lg flex items-center justify-center text-xs font-bold ${
                      isGuard
                        ? 'bg-emerald-500/10 dark:bg-emerald-500/20 text-emerald-600 dark:text-emerald-400 border border-emerald-500/30 dark:border-emerald-500/40'
                        : 'bg-blue-500/10 dark:bg-blue-500/20 text-blue-600 dark:text-blue-400 border border-blue-500/30 dark:border-blue-500/40'
                    }`}
                  >
                    {isGuard ? <ShieldCheck className="w-4 h-4" /> : <UserCheck className="w-4 h-4" />}
                  </div>

                  <div className="hidden sm:block text-left">
                    <div className="text-xs font-bold text-slate-900 dark:text-white leading-tight">
                      {currentUser.name}
                    </div>
                    <div className="text-[10px] text-slate-500 dark:text-slate-400 flex items-center space-x-1">
                      <span
                        className={`font-semibold ${
                          isGuard ? 'text-emerald-600 dark:text-emerald-400' : 'text-blue-600 dark:text-blue-400'
                        }`}
                      >
                        {isGuard ? 'GUARDIA GARITA' : currentUser.role}
                      </span>
                      {currentUser.guardShift && (
                        <span>&middot; {currentUser.guardShift}</span>
                      )}
                    </div>
                  </div>
                </div>

                <div className="h-5 w-px bg-slate-300 dark:bg-slate-800 hidden sm:block" />

                <button
                  onClick={onLogout}
                  title="Cerrar Sesión"
                  className="flex items-center space-x-1 px-2.5 py-1.5 bg-white dark:bg-slate-800 hover:bg-rose-50 dark:hover:bg-rose-950/70 hover:border-rose-200 dark:hover:border-rose-800/80 text-slate-700 dark:text-slate-300 hover:text-rose-600 dark:hover:text-rose-300 rounded-lg text-xs font-semibold border border-slate-300 dark:border-slate-700 transition"
                >
                  <LogOut className="w-3.5 h-3.5" />
                  <span className="hidden md:inline">Salir</span>
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Admin Subtabs navigation (Visible when viewMode === 'ADMIN' and not Guard) */}
      {!isGuard && viewMode === 'ADMIN' && (
        <div className="bg-slate-50/90 dark:bg-slate-950/70 border-t border-slate-200 dark:border-slate-800/80 px-4 sm:px-6 lg:px-8">
          <div className="max-w-7xl mx-auto flex space-x-1 sm:space-x-4 overflow-x-auto py-2.5">
            <button
              onClick={() => setAdminTab('PARKING')}
              className={`flex items-center space-x-2 px-3.5 py-1.5 rounded-lg text-sm font-medium transition-colors whitespace-nowrap ${
                adminTab === 'PARKING'
                  ? 'bg-white dark:bg-slate-800 text-emerald-600 dark:text-emerald-400 border border-slate-300 dark:border-slate-700 shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-200/60 dark:hover:bg-slate-900'
              }`}
            >
              <Car className="w-4 h-4" />
              <span>Seguridad &amp; Parqueos</span>
            </button>

            <button
              onClick={() => setAdminTab('ASSEMBLIES')}
              className={`flex items-center space-x-2 px-3.5 py-1.5 rounded-lg text-sm font-medium transition-colors whitespace-nowrap ${
                adminTab === 'ASSEMBLIES'
                  ? 'bg-white dark:bg-slate-800 text-blue-600 dark:text-blue-400 border border-slate-300 dark:border-slate-700 shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-200/60 dark:hover:bg-slate-900'
              }`}
            >
              <Vote className="w-4 h-4" />
              <span>Asambleas &amp; Quórum</span>
            </button>

            <button
              onClick={() => setAdminTab('AMENITIES')}
              className={`flex items-center space-x-2 px-3.5 py-1.5 rounded-lg text-sm font-medium transition-colors whitespace-nowrap ${
                adminTab === 'AMENITIES'
                  ? 'bg-white dark:bg-slate-800 text-purple-600 dark:text-purple-400 border border-slate-300 dark:border-slate-700 shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-200/60 dark:hover:bg-slate-900'
              }`}
            >
              <CalendarDays className="w-4 h-4" />
              <span>Amenidades &amp; Reservas</span>
            </button>

            <button
              onClick={() => setAdminTab('TICKETS')}
              className={`flex items-center space-x-2 px-3.5 py-1.5 rounded-lg text-sm font-medium transition-colors whitespace-nowrap ${
                adminTab === 'TICKETS'
                  ? 'bg-white dark:bg-slate-800 text-amber-600 dark:text-amber-400 border border-slate-300 dark:border-slate-700 shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-200/60 dark:hover:bg-slate-900'
              }`}
            >
              <Wrench className="w-4 h-4" />
              <span>Mantenimiento &amp; Tickets</span>
            </button>

            <button
              onClick={() => setAdminTab('FINANCE')}
              className={`flex items-center space-x-2 px-3.5 py-1.5 rounded-lg text-sm font-medium transition-colors whitespace-nowrap ${
                adminTab === 'FINANCE'
                  ? 'bg-white dark:bg-slate-800 text-teal-600 dark:text-teal-400 border border-slate-300 dark:border-slate-700 shadow-sm'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-200/60 dark:hover:bg-slate-900'
              }`}
            >
              <Wallet className="w-4 h-4" />
              <span>Finanzas &amp; Activos</span>
            </button>
          </div>
        </div>
      )}
    </header>
  );
};
