import {
  ParkingSpot,
  VehicleEntry,
  CovertResidentAlert,
  ParkingFine,
  Assembly,
  AssemblyQuorum,
  AssemblyMotion,
  CloseMotionResult,
  MaintenanceTicket,
  Amenity,
  CalendarSlot,
  Invoice,
  FixedAsset
} from '../types';

const API_BASE = '/api/v1';

// Initial Mock / Seed State for offline / demo mode
let mockSpots: ParkingSpot[] = Array.from({ length: 20 }, (_, i) => {
  const spotNum = `P-${String(i + 1).padStart(2, '0')}`;
  const isOccupied = i === 1 || i === 4 || i === 8 || i === 14;
  return {
    id: `spot-${i + 1}`,
    spotNumber: spotNum,
    spotType: 'VISITOR',
    isOccupied,
    currentVehicleEntryId: isOccupied ? `entry-seed-${i + 1}` : null
  };
});

let mockActiveEntries: VehicleEntry[] = [
  {
    id: 'entry-seed-2',
    plate: 'P-123XYZ',
    houseId: 'h-101',
    houseNumber: 'Casa 101',
    visitorName: 'Carlos Mazariegos',
    visitorIdDocument: 'DPI-2489123450101',
    spotNumber: 'P-02',
    guardUserId: 'guard-1',
    entryTime: new Date(Date.now() - 3600000 * 3.5).toISOString(),
    status: 'ACTIVE',
    isOvernight: false,
    createdAt: new Date(Date.now() - 3600000 * 3.5).toISOString()
  },
  {
    id: 'entry-seed-5',
    plate: 'P-987ABC',
    houseId: 'h-105',
    houseNumber: 'Casa 105',
    visitorName: 'Andrea Morales',
    visitorIdDocument: 'DPI-1982736450101',
    spotNumber: 'P-05',
    guardUserId: 'guard-1',
    entryTime: new Date(Date.now() - 3600000 * 9).toISOString(),
    status: 'ACTIVE',
    isOvernight: true,
    createdAt: new Date(Date.now() - 3600000 * 9).toISOString()
  },
  {
    id: 'entry-seed-9',
    plate: 'P-555DUE',
    houseId: 'h-108',
    houseNumber: 'Casa 108',
    visitorName: 'Estuardo Rivas',
    visitorIdDocument: 'DPI-3001889920101',
    spotNumber: 'P-09',
    guardUserId: 'guard-1',
    entryTime: new Date(Date.now() - 3600000 * 14).toISOString(),
    status: 'ACTIVE',
    isOvernight: true,
    createdAt: new Date(Date.now() - 3600000 * 14).toISOString()
  },
  {
    id: 'entry-seed-15',
    plate: 'P-888NOCT',
    houseId: 'h-110',
    houseNumber: 'Casa 110',
    visitorName: 'Marcos Soto',
    visitorIdDocument: 'DPI-4445556660101',
    spotNumber: 'P-15',
    guardUserId: 'guard-1',
    entryTime: new Date(Date.now() - 3600000 * 18).toISOString(),
    status: 'ACTIVE',
    isOvernight: true,
    createdAt: new Date(Date.now() - 3600000 * 18).toISOString()
  }
];

let mockCovertAlerts: CovertResidentAlert[] = [
  {
    plate: 'P-888NOCT',
    houseId: 'h-110',
    houseNumber: 'Casa 110',
    overnightCount: 4,
    isSuspect: true,
    label: 'SOSPECHA DE RESIDENTE ENCUBIERTO (4 pernoctaciones)',
    suspicionLevel: 'CRITICAL',
    firstDetectedEntry: new Date(Date.now() - 86400000 * 6).toISOString(),
    lastDetectedEntry: new Date(Date.now() - 3600000 * 18).toISOString(),
    history: [
      {
        id: 'h-1',
        plate: 'P-888NOCT',
        houseId: 'h-110',
        houseNumber: 'Casa 110',
        visitorName: 'Marcos Soto',
        visitorIdDocument: 'DPI-4445556660101',
        spotNumber: 'P-15',
        guardUserId: 'guard-1',
        entryTime: new Date(Date.now() - 86400000 * 6 + 72000000).toISOString(),
        exitTime: new Date(Date.now() - 86400000 * 5 + 28800000).toISOString(),
        durationMinutes: 700,
        status: 'COMPLETED',
        isOvernight: true,
        createdAt: new Date(Date.now() - 86400000 * 6).toISOString()
      },
      {
        id: 'h-2',
        plate: 'P-888NOCT',
        houseId: 'h-110',
        houseNumber: 'Casa 110',
        visitorName: 'Marcos Soto',
        visitorIdDocument: 'DPI-4445556660101',
        spotNumber: 'P-12',
        guardUserId: 'guard-2',
        entryTime: new Date(Date.now() - 86400000 * 4 + 75600000).toISOString(),
        exitTime: new Date(Date.now() - 86400000 * 3 + 25200000).toISOString(),
        durationMinutes: 680,
        status: 'COMPLETED',
        isOvernight: true,
        createdAt: new Date(Date.now() - 86400000 * 4).toISOString()
      },
      {
        id: 'h-3',
        plate: 'P-888NOCT',
        houseId: 'h-110',
        houseNumber: 'Casa 110',
        visitorName: 'Marcos Soto',
        visitorIdDocument: 'DPI-4445556660101',
        spotNumber: 'P-03',
        guardUserId: 'guard-1',
        entryTime: new Date(Date.now() - 86400000 * 2 + 79200000).toISOString(),
        exitTime: new Date(Date.now() - 86400000 * 1 + 21600000).toISOString(),
        durationMinutes: 620,
        status: 'COMPLETED',
        isOvernight: true,
        createdAt: new Date(Date.now() - 86400000 * 2).toISOString()
      },
      {
        id: 'h-4',
        plate: 'P-888NOCT',
        houseId: 'h-110',
        houseNumber: 'Casa 110',
        visitorName: 'Marcos Soto',
        visitorIdDocument: 'DPI-4445556660101',
        spotNumber: 'P-15',
        guardUserId: 'guard-1',
        entryTime: new Date(Date.now() - 3600000 * 18).toISOString(),
        status: 'ACTIVE',
        isOvernight: true,
        createdAt: new Date(Date.now() - 3600000 * 18).toISOString()
      }
    ]
  },
  {
    plate: 'P-555DUE',
    houseId: 'h-108',
    houseNumber: 'Casa 108',
    overnightCount: 3,
    isSuspect: true,
    label: 'SOSPECHA DE RESIDENTE ENCUBIERTO (3 pernoctaciones)',
    suspicionLevel: 'HIGH',
    firstDetectedEntry: new Date(Date.now() - 86400000 * 5).toISOString(),
    lastDetectedEntry: new Date(Date.now() - 3600000 * 14).toISOString(),
    history: []
  }
];

let mockFines: ParkingFine[] = [
  {
    id: 'fine-001',
    plate: 'P-777GHOST',
    houseId: 'h-102',
    houseNumber: 'Casa 102',
    fineAmount: 250.0,
    reason: 'Uso no autorizado de parqueo de visitas como estacionamiento permanente (4 pernoctaciones)',
    evidenceSummary: 'Registros de entrada en fechas 12/10, 14/10, 16/10 y 18/10 con horario 23:00 - 07:00.',
    appliedByUserId: 'admin-1',
    appliedByName: 'Lic. Fernando Gómez (Admin)',
    financeChargeId: 'chg-fin-8821',
    status: 'APPLIED',
    createdAt: new Date(Date.now() - 86400000 * 2).toISOString()
  }
];

// Governance Mock
let mockAssemblies: Assembly[] = [
  {
    id: 'asm-2026-01',
    title: 'Asamblea General Ordinaria de Copropietarios 2026',
    assemblyType: 'ORDINARY',
    scheduledAt: new Date(Date.now() + 86400000 * 2).toISOString(),
    agenda: '1. Verificación de Quórum\n2. Aprobación Presupuesto Anual 2026\n3. Remodelación Área de Piscina\n4. Varios',
    status: 'IN_PROGRESS',
    totalResidentialHouses: 50,
    createdAt: new Date(Date.now() - 86400000 * 5).toISOString()
  }
];

let mockAttendees: Set<string> = new Set([
  'Casa 101', 'Casa 102', 'Casa 104', 'Casa 105', 'Casa 108',
  'Casa 110', 'Casa 112', 'Casa 115', 'Casa 118', 'Casa 120',
  'Casa 122', 'Casa 125', 'Casa 128', 'Casa 130', 'Casa 131',
  'Casa 133', 'Casa 135', 'Casa 138', 'Casa 140', 'Casa 142',
  'Casa 144', 'Casa 145', 'Casa 147', 'Casa 148', 'Casa 149',
  'Casa 150' // 26 of 50 = 52% (has quorum!)
]);

let mockMotions: AssemblyMotion[] = [
  {
    id: 'mot-001',
    assemblyId: 'asm-2026-01',
    title: 'Aprobación del Presupuesto Operativo Anual 2026 (Q. 450,000)',
    description: 'Incremento del 5% destinado a fondo de reserva de ascensores y garita automatizada.',
    status: 'OPEN',
    createdAt: new Date(Date.now() - 3600000).toISOString()
  },
  {
    id: 'mot-002',
    assemblyId: 'asm-2026-01',
    title: 'Instalación de Sistema de Cámaras LPR en Garita de Acceso',
    description: 'Adquisición de cámaras de lectura de placas para automatización de ingreso.',
    status: 'PENDING',
    createdAt: new Date(Date.now() - 1800000).toISOString()
  }
];

// Maintenance Mock
let mockTickets: MaintenanceTicket[] = [
  {
    id: 'tkt-001',
    folio: 'MNT-2026-001',
    title: 'Fuga de agua en área verde cerca de Casa 105',
    category: 'PLUMBING',
    location: 'Jardín central frente a lote 105',
    description: 'Se observa acumulación excesiva de agua y posible rotura de tubería de riego.',
    status: 'IN_PROGRESS',
    priority: 'HIGH',
    assignedTechnician: 'Mario López (Fontanero)',
    reporterUserId: 'usr-105',
    reporterName: 'Carlos Méndez',
    reporterRole: 'RESIDENT',
    houseId: 'h-105',
    houseNumber: 'Casa 105',
    createdAt: new Date(Date.now() - 86400000 * 1.5).toISOString(),
    updatedAt: new Date(Date.now() - 3600000 * 2).toISOString()
  },
  {
    id: 'tkt-002',
    folio: 'MNT-2026-002',
    title: 'Luminaria fundida en poste 4 de calle principal',
    category: 'LIGHTING',
    location: 'Calle Principal, Poste #4',
    description: 'El farol parpadea y por la noche el tramo queda a oscuras.',
    status: 'OPEN',
    priority: 'MEDIUM',
    reporterUserId: 'usr-guard',
    reporterName: 'Seguridad Turno Noche',
    reporterRole: 'GUARD',
    houseId: 'h-admin',
    houseNumber: 'Garita',
    createdAt: new Date(Date.now() - 3600000 * 5).toISOString(),
    updatedAt: new Date(Date.now() - 3600000 * 5).toISOString()
  },
  {
    id: 'tkt-003',
    folio: 'MNT-2026-003',
    title: 'Mantenimiento del filtro de la piscina',
    category: 'POOL',
    location: 'Casa Club - Área Piscina',
    description: 'Presión alta en el manómetro del filtro de arena.',
    status: 'RESOLVED',
    priority: 'LOW',
    assignedTechnician: 'Aquatec Servicios',
    reporterUserId: 'usr-admin',
    reporterName: 'Administración',
    reporterRole: 'ADMIN',
    houseId: 'h-club',
    houseNumber: 'Casa Club',
    createdAt: new Date(Date.now() - 86400000 * 4).toISOString(),
    updatedAt: new Date(Date.now() - 86400000 * 1).toISOString()
  }
];

// Amenities Mock
let mockAmenities: Amenity[] = [
  {
    id: 'am-01',
    code: 'SALON_SOCIAL',
    name: 'Salón Social Principal',
    description: 'Salón con aire acondicionado, mesas, sillas y cocina equipada para 80 personas.',
    capacity: 80,
    rentalCost: 350.0,
    depositCost: 500.0,
    openingTime: '09:00',
    closingTime: '23:00',
    allowedEventTypes: ['CUMPLEAÑOS', 'REUNION_FAMILIAR', 'ANIVERSARIO'],
    isActive: true
  },
  {
    id: 'am-02',
    code: 'AREA_CHURRASQUERAS',
    name: 'Pérgola & Churrasqueras',
    description: 'Área al aire libre con asadores de acero inoxidable y pérgola techada.',
    capacity: 30,
    rentalCost: 150.0,
    depositCost: 200.0,
    openingTime: '10:00',
    closingTime: '20:00',
    allowedEventTypes: ['ASADO', 'PIQUE', 'CONVIVIO'],
    isActive: true
  },
  {
    id: 'am-03',
    code: 'CANCHA_FUTBOL',
    name: 'Cancha Polideportiva Sintética',
    description: 'Cancha con iluminación LED para fútbol 5 y basquetbol.',
    capacity: 20,
    rentalCost: 75.0,
    depositCost: 100.0,
    openingTime: '06:00',
    closingTime: '22:00',
    allowedEventTypes: ['TORNEO', 'ENTRENAMIENTO', 'RECREATIVO'],
    isActive: true
  }
];

// Generic Fetch Wrapper with Fallback to Mock
async function fetchWithFallback<T>(url: string, options?: RequestInit, fallbackData?: () => T): Promise<T> {
  try {
    const res = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(options?.headers || {})
      }
    });
    if (res.ok) {
      return await res.json();
    }
  } catch {
    // Backend offline / network failed -> use fallback
  }

  if (fallbackData) {
    return fallbackData();
  }
  throw new Error(`Unable to fetch ${url} and no fallback available.`);
}

export const api = {
  // ==========================================
  // Parking & Security
  // ==========================================
  async getParkingSpots(): Promise<ParkingSpot[]> {
    return fetchWithFallback<ParkingSpot[]>(`${API_BASE}/parking/spots`, {}, () => [...mockSpots]);
  },

  async getActiveVehicleEntries(): Promise<VehicleEntry[]> {
    return fetchWithFallback<VehicleEntry[]>(`${API_BASE}/parking/entries?status=ACTIVE`, {}, () => [
      ...mockActiveEntries
    ]);
  },

  async registerEntry(payload: {
    plate: string;
    houseId: string;
    houseNumber: string;
    visitorName: string;
    visitorIdDocument: string;
    spotNumber: string;
    guardUserId: string;
  }): Promise<VehicleEntry> {
    try {
      const res = await fetch(`${API_BASE}/parking/entries`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (res.ok) return await res.json();
    } catch {
      // fallback
    }

    // Update in-memory mock
    const newEntry: VehicleEntry = {
      id: `entry-${Date.now()}`,
      plate: payload.plate.toUpperCase(),
      houseId: payload.houseId,
      houseNumber: payload.houseNumber,
      visitorName: payload.visitorName,
      visitorIdDocument: payload.visitorIdDocument,
      spotNumber: payload.spotNumber,
      guardUserId: payload.guardUserId || 'guard-current',
      entryTime: new Date().toISOString(),
      status: 'ACTIVE',
      isOvernight: false,
      createdAt: new Date().toISOString()
    };

    mockActiveEntries = [newEntry, ...mockActiveEntries];
    mockSpots = mockSpots.map(s =>
      s.spotNumber === payload.spotNumber
        ? { ...s, isOccupied: true, currentVehicleEntryId: newEntry.id }
        : s
    );

    return newEntry;
  },

  async registerExit(entryId: string, spotNumber?: string): Promise<{ success: boolean }> {
    try {
      const res = await fetch(`${API_BASE}/parking/entries/${entryId}/exit`, {
        method: 'POST'
      });
      if (res.ok) return await res.json();
    } catch {
      // fallback
    }

    const entry = mockActiveEntries.find(e => e.id === entryId);
    const resolvedSpot = spotNumber || entry?.spotNumber;

    mockActiveEntries = mockActiveEntries.filter(e => e.id !== entryId);
    if (resolvedSpot) {
      mockSpots = mockSpots.map(s =>
        s.spotNumber === resolvedSpot
          ? { ...s, isOccupied: false, currentVehicleEntryId: null }
          : s
      );
    }
    return { success: true };
  },

  async getCovertResidents(): Promise<CovertResidentAlert[]> {
    return fetchWithFallback<CovertResidentAlert[]>(
      `${API_BASE}/parking/covert-residents`,
      {},
      () => [...mockCovertAlerts]
    );
  },

  async applyParkingFine(payload: {
    plate: string;
    houseId: string;
    houseNumber: string;
    fineAmount: number;
    reason: string;
    evidenceSummary: string;
    appliedByUserId: string;
    appliedByName: string;
  }): Promise<ParkingFine> {
    try {
      const res = await fetch(`${API_BASE}/parking/fines`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (res.ok) return await res.json();
    } catch {
      // fallback
    }

    const fine: ParkingFine = {
      id: `fine-${Date.now()}`,
      plate: payload.plate,
      houseId: payload.houseId,
      houseNumber: payload.houseNumber,
      fineAmount: payload.fineAmount,
      reason: payload.reason,
      evidenceSummary: payload.evidenceSummary,
      appliedByUserId: payload.appliedByUserId,
      appliedByName: payload.appliedByName,
      financeChargeId: `chg-${Date.now().toString().slice(-4)}`,
      status: 'APPLIED',
      createdAt: new Date().toISOString()
    };
    mockFines = [fine, ...mockFines];
    return fine;
  },

  async getParkingFines(): Promise<ParkingFine[]> {
    return fetchWithFallback<ParkingFine[]>(`${API_BASE}/parking/fines`, {}, () => [...mockFines]);
  },

  // ==========================================
  // Governance & Assemblies
  // ==========================================
  async getAssemblies(): Promise<Assembly[]> {
    return fetchWithFallback<Assembly[]>(`${API_BASE}/assemblies`, {}, () => [...mockAssemblies]);
  },

  async convokeAssembly(payload: {
    title: string;
    assemblyType: 'ORDINARY' | 'EXTRAORDINARY';
    scheduledAt: string;
    agenda: string;
    totalResidentialHouses: number;
  }): Promise<Assembly> {
    try {
      const res = await fetch(`${API_BASE}/assemblies`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (res.ok) return await res.json();
    } catch {
      // fallback
    }

    const newAssembly: Assembly = {
      id: `asm-${Date.now()}`,
      ...payload,
      status: 'CONVOKED',
      createdAt: new Date().toISOString()
    };
    mockAssemblies = [newAssembly, ...mockAssemblies];
    return newAssembly;
  },

  async getAssemblyQuorum(assemblyId: string): Promise<AssemblyQuorum> {
    return fetchWithFallback<AssemblyQuorum>(
      `${API_BASE}/assemblies/${assemblyId}/quorum`,
      {},
      () => {
        const total = 50;
        const present = mockAttendees.size;
        const pct = Math.round((present / total) * 1000) / 10;
        return {
          assemblyId,
          assemblyTitle: 'Asamblea General Ordinaria de Copropietarios 2026',
          assemblyStatus: 'IN_PROGRESS',
          totalResidentialHouses: total,
          presentHouses: present,
          quorumPercentage: pct,
          hasQuorum: pct >= 50.0
        };
      }
    );
  },

  async registerAssemblyAttendance(
    assemblyId: string,
    houseNumber: string
  ): Promise<{ success: boolean; presentHouses: number; quorumPercentage: number }> {
    try {
      const res = await fetch(`${API_BASE}/assemblies/${assemblyId}/attendance`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ houseNumber, attendeeName: `Propietario ${houseNumber}` })
      });
      if (res.ok) return await res.json();
    } catch {
      // fallback
    }

    mockAttendees.add(houseNumber);
    const total = 50;
    const present = mockAttendees.size;
    const pct = Math.round((present / total) * 1000) / 10;
    return { success: true, presentHouses: present, quorumPercentage: pct };
  },

  async getAssemblyMotions(assemblyId: string): Promise<AssemblyMotion[]> {
    return fetchWithFallback<AssemblyMotion[]>(
      `${API_BASE}/assemblies/${assemblyId}/motions`,
      {},
      () => mockMotions.filter(m => m.assemblyId === assemblyId)
    );
  },

  async createMotion(assemblyId: string, title: string, description: string): Promise<AssemblyMotion> {
    try {
      const res = await fetch(`${API_BASE}/assemblies/${assemblyId}/motions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title, description })
      });
      if (res.ok) return await res.json();
    } catch {
      // fallback
    }

    const mot: AssemblyMotion = {
      id: `mot-${Date.now()}`,
      assemblyId,
      title,
      description,
      status: 'PENDING',
      createdAt: new Date().toISOString()
    };
    mockMotions.push(mot);
    return mot;
  },

  async openMotion(motionId: string): Promise<AssemblyMotion> {
    try {
      const res = await fetch(`${API_BASE}/assemblies/motions/${motionId}/open`, {
        method: 'PUT'
      });
      if (res.ok) return await res.json();
    } catch {
      // fallback
    }

    mockMotions = mockMotions.map(m =>
      m.id === motionId ? { ...m, status: 'OPEN', openedAt: new Date().toISOString() } : m
    );
    return mockMotions.find(m => m.id === motionId)!;
  },

  async closeMotion(motionId: string): Promise<CloseMotionResult> {
    try {
      const res = await fetch(`${API_BASE}/assemblies/motions/${motionId}/close`, {
        method: 'PUT'
      });
      if (res.ok) return await res.json();
    } catch {
      // fallback
    }

    const inFavor = 22;
    const against = 3;
    const abstain = 1;
    const total = inFavor + against + abstain;
    const pct = Math.round((inFavor / total) * 1000) / 10;
    const verdict = pct >= 50 ? 'APPROVED' : 'REJECTED';

    mockMotions = mockMotions.map(m =>
      m.id === motionId
        ? { ...m, status: 'CLOSED', verdict: verdict as 'APPROVED' | 'REJECTED', closedAt: new Date().toISOString() }
        : m
    );

    return {
      motionId,
      title: 'Aprobación del Presupuesto Operativo Anual 2026',
      status: 'CLOSED',
      verdict,
      inFavorCount: inFavor,
      againstCount: against,
      abstainCount: abstain,
      totalVotes: total,
      approvalPercentage: pct,
      closedAt: new Date().toISOString()
    };
  },

  // ==========================================
  // Maintenance Tickets
  // ==========================================
  async getTickets(): Promise<MaintenanceTicket[]> {
    return fetchWithFallback<MaintenanceTicket[]>(`${API_BASE}/tickets`, {}, () => [...mockTickets]);
  },

  async createTicket(payload: {
    title: string;
    category: MaintenanceTicket['category'];
    location: string;
    description: string;
    photoUrl?: string;
    priority: MaintenanceTicket['priority'];
    reporterName: string;
    houseNumber: string;
  }): Promise<MaintenanceTicket> {
    try {
      const res = await fetch(`${API_BASE}/tickets`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (res.ok) return await res.json();
    } catch {
      // fallback
    }

    const newTicket: MaintenanceTicket = {
      id: `tkt-${Date.now()}`,
      folio: `MNT-2026-${String(mockTickets.length + 1).padStart(3, '0')}`,
      title: payload.title,
      category: payload.category,
      location: payload.location,
      description: payload.description,
      photoUrl: payload.photoUrl,
      status: 'OPEN',
      priority: payload.priority,
      reporterUserId: 'usr-current',
      reporterName: payload.reporterName,
      reporterRole: 'RESIDENT',
      houseId: 'h-auto',
      houseNumber: payload.houseNumber,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    mockTickets = [newTicket, ...mockTickets];
    return newTicket;
  },

  async updateTicketStatus(
    id: string,
    newStatus: MaintenanceTicket['status'],
    notes: string,
    assignedTechnician?: string
  ): Promise<MaintenanceTicket> {
    try {
      const res = await fetch(`${API_BASE}/tickets/${id}/progress`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ newStatus, notes, assignedTechnician })
      });
      if (res.ok) return await res.json();
    } catch {
      // fallback
    }

    mockTickets = mockTickets.map(t => {
      if (t.id === id) {
        return {
          ...t,
          status: newStatus,
          assignedTechnician: assignedTechnician || t.assignedTechnician,
          updatedAt: new Date().toISOString(),
          closedAt: newStatus === 'CLOSED' ? new Date().toISOString() : t.closedAt
        };
      }
      return t;
    });

    return mockTickets.find(t => t.id === id)!;
  },

  // ==========================================
  // Amenities & Calendar
  // ==========================================
  async getAmenities(): Promise<Amenity[]> {
    return fetchWithFallback<Amenity[]>(`${API_BASE}/amenities`, {}, () => [...mockAmenities]);
  },

  async getCalendarSlots(amenityId: string, date: string): Promise<CalendarSlot[]> {
    return fetchWithFallback<CalendarSlot[]>(
      `${API_BASE}/amenities/${amenityId}/calendar?date=${date}`,
      {},
      () => {
        // Privacy Compliant slots: Only show "Disponible" or "Reservado"
        return [
          { date, startTime: '09:00', endTime: '12:00', displayStatus: 'Disponible', isOccupied: false },
          { date, startTime: '12:00', endTime: '15:00', displayStatus: 'Reservado', isOccupied: true },
          { date, startTime: '15:00', endTime: '18:00', displayStatus: 'Disponible', isOccupied: false },
          { date, startTime: '18:00', endTime: '22:00', displayStatus: 'Reservado', isOccupied: true }
        ];
      }
    );
  },

  // ==========================================
  // Finance & Fixed Assets
  // ==========================================
  async getInvoices(): Promise<Invoice[]> {
    return fetchWithFallback<Invoice[]>(`${API_BASE}/finance/invoices`, {}, () => [
      {
        id: 'inv-01',
        invoiceNumber: 'FAC-E-889102',
        providerName: 'Empresa Eléctrica de Guatemala (EEGSA)',
        expenseCategory: 'SERVICIOS_BASICOS',
        amount: 8450.0,
        invoiceDate: '2026-10-15',
        paymentDate: '2026-10-20',
        status: 'PAID',
        digitalSupportUrl: 'https://docs.residence.local/invoices/fac-889102.pdf'
      },
      {
        id: 'inv-02',
        invoiceNumber: 'FAC-B-4412',
        providerName: 'Seguridad Integral de Occidente S.A.',
        expenseCategory: 'SEGURIDAD_PRIVADA',
        amount: 14200.0,
        invoiceDate: '2026-10-01',
        paymentDate: '2026-10-05',
        status: 'PAID',
        digitalSupportUrl: 'https://docs.residence.local/invoices/fac-4412.pdf'
      },
      {
        id: 'inv-03',
        invoiceNumber: 'FAC-J-1209',
        providerName: 'Jardinería y Paisajismo Verde S.A.',
        expenseCategory: 'MANTENIMIENTO_AREAS_VERDES',
        amount: 3800.0,
        invoiceDate: '2026-10-25',
        status: 'PENDING'
      }
    ]);
  },

  async getFixedAssets(): Promise<FixedAsset[]> {
    return fetchWithFallback<FixedAsset[]>(`${API_BASE}/finance/assets`, {}, () => [
      {
        id: 'ast-01',
        assetCode: 'ACT-GAR-001',
        name: 'Brazo Mecánico de Talanquera Izquierda',
        category: 'SEGURIDAD_ACCESO',
        location: 'Garita de Entrada',
        acquisitionCost: 12500.0,
        physicalStatus: 'EXCELLENT'
      },
      {
        id: 'ast-02',
        assetCode: 'ACT-PIS-002',
        name: 'Bomba de Recirculación de Piscina 3HP',
        category: 'EQUIPOS_HIDRAULICOS',
        location: 'Cuarto de Máquinas Piscina',
        acquisitionCost: 8900.0,
        physicalStatus: 'NEEDS_MAINTENANCE'
      },
      {
        id: 'ast-03',
        assetCode: 'ACT-SEG-003',
        name: 'Servidor NVR Hikvision 32 Canales con RAID',
        category: 'TECNOLOGIA_SEGURIDAD',
        location: 'Rack de Telecomunicaciones Garita',
        acquisitionCost: 16800.0,
        physicalStatus: 'EXCELLENT'
      }
    ]);
  }
};
