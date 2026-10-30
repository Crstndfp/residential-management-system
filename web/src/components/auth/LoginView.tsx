import React, { useState } from 'react';
import {
  Building2,
  ShieldCheck,
  Lock,
  Mail,
  Eye,
  EyeOff,
  KeyRound,
  ShieldAlert,
  Sun,
  Moon
} from 'lucide-react';
import toast from 'react-hot-toast';
import { AuthUser, LoginCredentials, ThemeMode } from '../../types';

interface LoginViewProps {
  onLoginSuccess: (user: AuthUser) => void;
  onLogin: (credentials: LoginCredentials) => Promise<AuthUser>;
  theme: ThemeMode;
  onToggleTheme: () => void;
}

export const LoginView: React.FC<LoginViewProps> = ({
  onLoginSuccess,
  onLogin,
  theme,
  onToggleTheme
}) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim() || !password) {
      toast.error('Por favor ingrese su usuario/correo y contraseña.');
      return;
    }

    try {
      setIsLoading(true);
      const user = await onLogin({ email: email.trim(), password });
      toast.success(`¡Bienvenido, ${user.name}!`);
      onLoginSuccess(user);
    } catch (err: any) {
      const msg = err?.message || 'Error de autenticación. Verifique sus credenciales.';
      toast.error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleQuickFill = (testEmail: string, testPass: string, roleLabel: string) => {
    setEmail(testEmail);
    setPassword(testPass);
    toast(`Credenciales de ${roleLabel} seleccionadas`, { icon: '🔑' });
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col justify-center items-center px-4 sm:px-6 lg:px-8 py-12 relative overflow-hidden transition-colors duration-200">
      {/* Theme Toggle Button top right */}
      <div className="absolute top-6 right-6 z-20">
        <button
          onClick={onToggleTheme}
          title={theme === 'dark' ? 'Cambiar a Tema Claro' : 'Cambiar a Tema Oscuro'}
          className="p-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors shadow-sm"
        >
          {theme === 'dark' ? (
            <Sun className="w-5 h-5 text-amber-400" />
          ) : (
            <Moon className="w-5 h-5 text-slate-700" />
          )}
        </button>
      </div>

      {/* Ambient background glow */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 left-1/3 w-80 h-80 bg-blue-500/10 rounded-full blur-3xl pointer-events-none" />

      <div className="max-w-md w-full space-y-8 relative z-10">
        {/* Brand Header */}
        <div className="text-center space-y-3">
          <div className="mx-auto w-16 h-16 rounded-2xl bg-gradient-to-tr from-emerald-500 to-teal-400 flex items-center justify-center shadow-xl shadow-emerald-500/20">
            <Building2 className="w-9 h-9 text-slate-950 font-bold" />
          </div>

          <div>
            <h1 className="text-2xl sm:text-3xl font-black text-slate-900 dark:text-white tracking-tight">
              Residencial Las Brisas
            </h1>
            <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
              Plataforma de Control de Garita y Administración
            </p>
          </div>
        </div>

        {/* Login Card */}
        <div className="bg-white dark:bg-slate-900/90 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 sm:p-8 shadow-xl dark:shadow-2xl backdrop-blur-md space-y-6">
          <div className="flex items-center space-x-2 text-slate-800 dark:text-slate-300 text-sm font-semibold pb-2 border-b border-slate-100 dark:border-slate-800">
            <Lock className="w-4 h-4 text-emerald-500 dark:text-emerald-400" />
            <span>Iniciar Sesión en el Sistema</span>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Email / Username Input */}
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-300 mb-1.5">
                Usuario / Correo Electrónico
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Mail className="w-4 h-4" />
                </div>
                <input
                  type="email"
                  required
                  autoFocus
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  placeholder="guardia@lasbrisas.gt"
                  className="w-full pl-10 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800/90 border border-slate-300 dark:border-slate-700 rounded-xl text-slate-900 dark:text-white text-sm placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition"
                />
              </div>
            </div>

            {/* Password Input */}
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-300 mb-1.5">
                Contraseña
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                  <Lock className="w-4 h-4" />
                </div>
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  placeholder="••••••••••••"
                  className="w-full pl-10 pr-10 py-2.5 bg-slate-50 dark:bg-slate-800/90 border border-slate-300 dark:border-slate-700 rounded-xl text-slate-900 dark:text-white text-sm placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full mt-2 py-3 px-4 bg-gradient-to-r from-emerald-600 to-teal-500 hover:from-emerald-500 hover:to-teal-400 text-slate-950 font-bold rounded-xl shadow-lg shadow-emerald-600/20 transition-all duration-200 flex items-center justify-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
            >
              {isLoading ? (
                <>
                  <div className="w-4 h-4 border-2 border-slate-950 border-t-transparent rounded-full animate-spin" />
                  <span>Autenticando...</span>
                </>
              ) : (
                <>
                  <KeyRound className="w-4 h-4" />
                  <span>Ingresar al Sistema</span>
                </>
              )}
            </button>
          </form>

          {/* Quick-Fill Helper for Testing / Demo */}
          <div className="pt-4 border-t border-slate-100 dark:border-slate-800 space-y-2.5">
            <div className="flex items-center space-x-1.5 text-xs text-slate-500 dark:text-slate-400">
              <ShieldAlert className="w-3.5 h-3.5 text-amber-500" />
              <span className="font-semibold">Credenciales del Sistema (Demostración):</span>
            </div>

            <div className="grid grid-cols-2 gap-2 text-xs">
              <button
                type="button"
                onClick={() => handleQuickFill('guardia@lasbrisas.gt', 'Garita2026!', 'Garita')}
                className="p-2.5 bg-slate-50 dark:bg-slate-800 hover:bg-slate-100 dark:hover:bg-slate-700 border border-slate-200 dark:border-slate-700 hover:border-emerald-500/50 rounded-xl text-left transition space-y-1"
              >
                <div className="flex items-center space-x-1 text-emerald-600 dark:text-emerald-400 font-bold">
                  <ShieldCheck className="w-3.5 h-3.5" />
                  <span>Garita (Tablet)</span>
                </div>
                <div className="text-[11px] text-slate-500 dark:text-slate-400 font-mono">guardia@lasbrisas.gt</div>
              </button>

              <button
                type="button"
                onClick={() => handleQuickFill('admin@lasbrisas.gt', 'Admin2026!', 'Administrador')}
                className="p-2.5 bg-slate-50 dark:bg-slate-800 hover:bg-slate-100 dark:hover:bg-slate-700 border border-slate-200 dark:border-slate-700 hover:border-blue-500/50 rounded-xl text-left transition space-y-1"
              >
                <div className="flex items-center space-x-1 text-blue-600 dark:text-blue-400 font-bold">
                  <Building2 className="w-3.5 h-3.5" />
                  <span>Administración</span>
                </div>
                <div className="text-[11px] text-slate-500 dark:text-slate-400 font-mono">admin@lasbrisas.gt</div>
              </button>
            </div>
          </div>
        </div>

        {/* Security Notice Footer */}
        <p className="text-center text-xs text-slate-500">
          Acceso protegido con cifrado y control de perfiles por rol. Se auditan todos los ingresos
          vehiculares y transacciones.
        </p>
      </div>
    </div>
  );
};
