import React, { useState, useEffect } from 'react';
import { Navbar, ViewMode, AdminTab } from './components/layout/Navbar';
import { ParkingSpotGrid } from './components/garita/ParkingSpotGrid';
import { GateCheckInModal } from './components/garita/GateCheckInModal';
import { CovertResidentsPanel } from './components/admin/CovertResidentsPanel';
import { AssembliesPanel } from './components/admin/AssembliesPanel';
import { AmenitiesCalendarPanel } from './components/admin/AmenitiesCalendarPanel';
import { MaintenanceTicketsPanel } from './components/admin/MaintenanceTicketsPanel';
import { FinanceOverviewPanel } from './components/admin/FinanceOverviewPanel';
import { api } from './services/api';
import {
  ParkingSpot,
  VehicleEntry,
  CovertResidentAlert,
  ParkingFine,
  Assembly,
  AssemblyQuorum,
  AssemblyMotion,
  MaintenanceTicket,
  Amenity,
  Invoice,
  FixedAsset
} from './types';

export const App: React.FC = () => {
  const [viewMode, setViewMode] = useState<ViewMode>('GARITA');
  const [adminTab, setAdminTab] = useState<AdminTab>('PARKING');
  const [isLoading, setIsLoading] = useState(true);

  // State
  const [spots, setSpots] = useState<ParkingSpot[]>([]);
  const [activeEntries, setActiveEntries] = useState<VehicleEntry[]>([]);
  const [isCheckInModalOpen, setIsCheckInModalOpen] = useState(false);

  // Admin state
  const [alerts, setAlerts] = useState<CovertResidentAlert[]>([]);
  const [fines, setFines] = useState<ParkingFine[]>([]);
  const [assemblies, setAssemblies] = useState<Assembly[]>([]);
  const [currentQuorum, setCurrentQuorum] = useState<AssemblyQuorum>({
    assemblyId: 'asm-2026-01',
    assemblyTitle: 'Asamblea General Ordinaria de Copropietarios 2026',
    assemblyStatus: 'IN_PROGRESS',
    totalResidentialHouses: 50,
    presentHouses: 26,
    quorumPercentage: 52.0,
    hasQuorum: true
  });
  const [motions, setMotions] = useState<AssemblyMotion[]>([]);
  const [amenities, setAmenities] = useState<Amenity[]>([]);
  const [tickets, setTickets] = useState<MaintenanceTicket[]>([]);
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [assets, setAssets] = useState<FixedAsset[]>([]);

  // Load initial data
  useEffect(() => {
    loadAllData();
  }, []);

  const loadAllData = async () => {
    try {
      setIsLoading(true);
      const [
        spotsData,
        entriesData,
        alertsData,
        finesData,
        assembliesData,
        amenitiesData,
        ticketsData,
        invoicesData,
        assetsData
      ] = await Promise.all([
        api.getParkingSpots(),
        api.getActiveVehicleEntries(),
        api.getCovertResidents(),
        api.getParkingFines(),
        api.getAssemblies(),
        api.getAmenities(),
        api.getTickets(),
        api.getInvoices(),
        api.getFixedAssets()
      ]);

      setSpots(spotsData);
      setActiveEntries(entriesData);
      setAlerts(alertsData);
      setFines(finesData);
      setAssemblies(assembliesData);
      setAmenities(amenitiesData);
      setTickets(ticketsData);
      setInvoices(invoicesData);
      setAssets(assetsData);

      if (assembliesData.length > 0) {
        const q = await api.getAssemblyQuorum(assembliesData[0].id);
        setCurrentQuorum(q);
        const m = await api.getAssemblyMotions(assembliesData[0].id);
        setMotions(m);
      }
    } catch (err) {
      console.error('Error loading initial data:', err);
    } finally {
      setIsLoading(false);
    }
  };

  // Garita Actions
  const handleRegisterEntry = async (data: {
    plate: string;
    houseNumber: string;
    houseId: string;
    visitorName: string;
    visitorIdDocument: string;
    spotNumber: string;
    guardUserId: string;
  }) => {
    const newEntry = await api.registerEntry(data);
    setActiveEntries(prev => [newEntry, ...prev]);
    setSpots(prev =>
      prev.map(s =>
        s.spotNumber === data.spotNumber
          ? { ...s, isOccupied: true, currentVehicleEntryId: newEntry.id }
          : s
      )
    );
  };

  const handleRegisterExit = async (entryId: string, spotNumber: string) => {
    await api.registerExit(entryId, spotNumber);
    setActiveEntries(prev => prev.filter(e => e.id !== entryId));
    setSpots(prev =>
      prev.map(s =>
        s.spotNumber === spotNumber
          ? { ...s, isOccupied: false, currentVehicleEntryId: null }
          : s
      )
    );
  };

  // Admin Actions
  const handleApplyFine = async (payload: {
    plate: string;
    houseId: string;
    houseNumber: string;
    fineAmount: number;
    reason: string;
    evidenceSummary: string;
    appliedByUserId: string;
    appliedByName: string;
  }) => {
    const fine = await api.applyParkingFine(payload);
    setFines(prev => [fine, ...prev]);
  };

  const handleRegisterAttendance = async (houseNumber: string) => {
    if (assemblies.length === 0) return;
    const res = await api.registerAssemblyAttendance(assemblies[0].id, houseNumber);
    setCurrentQuorum(prev => ({
      ...prev,
      presentHouses: res.presentHouses,
      quorumPercentage: res.quorumPercentage,
      hasQuorum: res.quorumPercentage >= 50.0
    }));
  };

  const handleCreateMotion = async (title: string, description: string) => {
    if (assemblies.length === 0) return;
    const mot = await api.createMotion(assemblies[0].id, title, description);
    setMotions(prev => [...prev, mot]);
  };

  const handleOpenMotion = async (motionId: string) => {
    const updated = await api.openMotion(motionId);
    setMotions(prev => prev.map(m => (m.id === motionId ? updated : m)));
  };

  const handleCloseMotion = async (motionId: string) => {
    const result = await api.closeMotion(motionId);
    setMotions(prev =>
      prev.map(m =>
        m.id === motionId
          ? { ...m, status: 'CLOSED', verdict: result.verdict as any }
          : m
      )
    );
    return result;
  };

  const handleCreateTicket = async (payload: any) => {
    const t = await api.createTicket(payload);
    setTickets(prev => [t, ...prev]);
  };

  const handleUpdateTicketStatus = async (
    id: string,
    newStatus: any,
    notes: string,
    assignedTechnician?: string
  ) => {
    const t = await api.updateTicketStatus(id, newStatus, notes, assignedTechnician);
    setTickets(prev => prev.map(cur => (cur.id === id ? t : cur)));
  };

  const availableSpots = spots.filter(s => !s.isOccupied);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans antialiased selection:bg-emerald-500 selection:text-slate-950">
      {/* Navigation */}
      <Navbar
        viewMode={viewMode}
        setViewMode={setViewMode}
        adminTab={adminTab}
        setAdminTab={setAdminTab}
      />

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {isLoading ? (
          <div className="flex flex-col items-center justify-center py-24 space-y-3">
            <div className="w-10 h-10 border-4 border-slate-700 border-t-emerald-500 rounded-full animate-spin" />
            <p className="text-sm text-slate-400">Iniciando Sistema Residencial...</p>
          </div>
        ) : viewMode === 'GARITA' ? (
          /* Garita Tablet View (US-601) */
          <div>
            <ParkingSpotGrid
              spots={spots}
              activeEntries={activeEntries}
              onOpenCheckIn={() => setIsCheckInModalOpen(true)}
              onRegisterExit={handleRegisterExit}
              isLoading={isLoading}
            />

            <GateCheckInModal
              isOpen={isCheckInModalOpen}
              onClose={() => setIsCheckInModalOpen(false)}
              availableSpots={availableSpots}
              onSubmit={handleRegisterEntry}
            />
          </div>
        ) : (
          /* Administration & Board Tabs */
          <div>
            {adminTab === 'PARKING' && (
              <CovertResidentsPanel
                alerts={alerts}
                fines={fines}
                onApplyFine={handleApplyFine}
              />
            )}

            {adminTab === 'ASSEMBLIES' && assemblies.length > 0 && (
              <AssembliesPanel
                assembly={assemblies[0]}
                quorum={currentQuorum}
                motions={motions}
                onRegisterAttendance={handleRegisterAttendance}
                onCreateMotion={handleCreateMotion}
                onOpenMotion={handleOpenMotion}
                onCloseMotion={handleCloseMotion}
              />
            )}

            {adminTab === 'AMENITIES' && <AmenitiesCalendarPanel amenities={amenities} />}

            {adminTab === 'TICKETS' && (
              <MaintenanceTicketsPanel
                tickets={tickets}
                onCreateTicket={handleCreateTicket}
                onUpdateStatus={handleUpdateTicketStatus}
              />
            )}

            {adminTab === 'FINANCE' && (
              <FinanceOverviewPanel invoices={invoices} assets={assets} />
            )}
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-900 bg-slate-950/80 py-4 text-center text-xs text-slate-400">
        <p>
          Condominio Residencial Las Brisas &copy; {new Date().getFullYear()} &middot; Plataforma
          de Gestión Integrada (Épicas 1 a 6)
        </p>
      </footer>
    </div>
  );
};
export default App;
