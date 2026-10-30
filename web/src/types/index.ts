// ==========================================
// Authentication & RBAC Types
// ==========================================
export type UserRole = 'ADMIN' | 'BOARD' | 'GUARD' | 'RESIDENT';
export type ThemeMode = 'light' | 'dark';

export interface AuthUser {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  guardShift?: string;
  token: string;
}

export interface LoginCredentials {
  email: string;
  password: string;
}

// ==========================================
// Parking & Security Types (Épica 6)
// ==========================================
export interface ParkingSpot {
  id: string;
  spotNumber: string;
  spotType: string;
  isOccupied: boolean;
  currentVehicleEntryId?: string | null;
}

export interface VehicleEntry {
  id: string;
  plate: string;
  houseId: string;
  houseNumber: string;
  visitorName: string;
  visitorIdDocument: string;
  parkingSpotId?: string;
  spotNumber: string;
  guardUserId: string;
  entryTime: string;
  exitTime?: string | null;
  durationMinutes?: number | null;
  status: 'ACTIVE' | 'COMPLETED';
  isOvernight: boolean;
  createdAt: string;
}

export interface CovertResidentAlert {
  plate: string;
  houseId: string;
  houseNumber: string;
  overnightCount: number;
  isSuspect: boolean;
  label: string;
  suspicionLevel: 'NORMAL' | 'HIGH' | 'CRITICAL';
  firstDetectedEntry?: string;
  lastDetectedEntry?: string;
  history: VehicleEntry[];
}

export interface ParkingFine {
  id: string;
  plate: string;
  houseId: string;
  houseNumber: string;
  fineAmount: number;
  reason: string;
  evidenceSummary: string;
  appliedByUserId: string;
  appliedByName: string;
  financeChargeId?: string;
  status: string;
  createdAt: string;
}

// ==========================================
// Governance & Assemblies Types (Épica 4)
// ==========================================
export interface Assembly {
  id: string;
  title: string;
  assemblyType: 'ORDINARY' | 'EXTRAORDINARY';
  scheduledAt: string;
  agenda: string;
  status: 'CONVOKED' | 'IN_PROGRESS' | 'CONCLUDED' | 'CANCELLED';
  totalResidentialHouses: number;
  createdAt: string;
}

export interface AssemblyQuorum {
  assemblyId: string;
  assemblyTitle: string;
  assemblyStatus: string;
  totalResidentialHouses: number;
  presentHouses: number;
  quorumPercentage: number;
  hasQuorum: boolean;
}

export interface AssemblyMotion {
  id: string;
  assemblyId: string;
  title: string;
  description: string;
  status: 'PENDING' | 'OPEN' | 'CLOSED';
  verdict?: 'APPROVED' | 'REJECTED' | 'TIE' | null;
  openedAt?: string;
  closedAt?: string;
  createdAt: string;
}

export interface MotionVote {
  id: string;
  motionId: string;
  houseId: string;
  houseNumber: string;
  voterUserId: string;
  voteChoice: 'IN_FAVOR' | 'AGAINST' | 'ABSTAIN';
  voteHash: string;
  createdAt: string;
}

export interface CloseMotionResult {
  motionId: string;
  title: string;
  status: string;
  verdict: string;
  inFavorCount: number;
  againstCount: number;
  abstainCount: number;
  totalVotes: number;
  approvalPercentage: number;
  closedAt: string;
}

// ==========================================
// Maintenance Tickets Types (Épica 5)
// ==========================================
export interface MaintenanceTicket {
  id: string;
  folio: string;
  title: string;
  category: 'LIGHTING' | 'GARDENING' | 'POOL' | 'SECURITY_EQUIPMENT' | 'PLUMBING' | 'OTHER';
  location: string;
  description: string;
  photoUrl?: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  assignedTechnician?: string;
  reporterUserId: string;
  reporterName: string;
  reporterRole: string;
  houseId: string;
  houseNumber: string;
  createdAt: string;
  updatedAt: string;
  closedAt?: string;
}

export interface TicketUpdate {
  id: string;
  ticketId: string;
  authorUserId: string;
  authorName: string;
  authorRole: string;
  previousStatus: string;
  newStatus: string;
  notes: string;
  createdAt: string;
}

export interface TicketDetail {
  ticket: MaintenanceTicket;
  history: TicketUpdate[];
}

// ==========================================
// Amenities & Bookings Types (Épica 3)
// ==========================================
export interface Amenity {
  id: string;
  code: string;
  name: string;
  description: string;
  capacity: number;
  rentalCost: number;
  depositCost: number;
  openingTime: string;
  closingTime: string;
  allowedEventTypes: string[];
  isActive: boolean;
}

export interface CalendarSlot {
  date: string;
  startTime: string;
  endTime: string;
  displayStatus: 'Disponible' | 'Reservado';
  isOccupied: boolean;
}

// ==========================================
// Finance Types (Épica 2)
// ==========================================
export interface Invoice {
  id: string;
  invoiceNumber: string;
  providerName: string;
  expenseCategory: string;
  amount: number;
  invoiceDate: string;
  paymentDate?: string;
  status: string;
  digitalSupportUrl?: string;
}

export interface FixedAsset {
  id: string;
  assetCode: string;
  name: string;
  category: string;
  location: string;
  acquisitionCost: number;
  physicalStatus: 'EXCELLENT' | 'NEEDS_MAINTENANCE' | 'DAMAGED';
}
