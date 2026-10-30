import test, { describe, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import { api, ensureUuid } from './api.ts';

// Setup Mock Storage for Node test runner environment
if (typeof globalThis.localStorage === 'undefined') {
  const store = new Map<string, string>();
  globalThis.localStorage = {
    getItem: (key: string) => store.get(key) ?? null,
    setItem: (key: string, value: string) => store.set(key, String(value)),
    removeItem: (key: string) => store.delete(key),
    clear: () => store.clear(),
    key: (index: number) => Array.from(store.keys())[index] ?? null,
    get length() {
      return store.size;
    }
  } as Storage;
}

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

describe('API Service Unit Tests & Microservice Wiring', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  describe('ensureUuid Helper', () => {
    test('should preserve already valid UUIDs', () => {
      const validUuid = '123e4567-e89b-12d3-a456-426614174000';
      assert.strictEqual(ensureUuid(validUuid), validUuid);
    });

    test('should transform string IDs into valid UUID v4 compliant format', () => {
      const result = ensureUuid('h-101');
      assert.match(result, UUID_REGEX);
      assert.strictEqual(ensureUuid('h-101'), result, 'Transformation must be deterministic');
    });

    test('should provide valid UUID fallback for null or empty input', () => {
      assert.match(ensureUuid(null), UUID_REGEX);
      assert.match(ensureUuid(''), UUID_REGEX);
    });
  });

  describe('Authentication & Session Management', () => {
    test('should successfully log in with valid Guard credentials', async () => {
      const user = await api.login({
        email: 'guardia@lasbrisas.gt',
        password: 'Garita2026!'
      });

      assert.strictEqual(user.role, 'GUARD');
      assert.strictEqual(user.email, 'guardia@lasbrisas.gt');
      assert.strictEqual(api.getCurrentUser()?.email, 'guardia@lasbrisas.gt');
    });

    test('should successfully log in with valid Admin credentials', async () => {
      const user = await api.login({
        email: 'admin@lasbrisas.gt',
        password: 'Admin2026!'
      });

      assert.strictEqual(user.role, 'ADMIN');
      assert.ok(user.token);
    });

    test('should fail login with invalid password', async () => {
      await assert.rejects(
        async () => {
          await api.login({
            email: 'admin@lasbrisas.gt',
            password: 'WrongPassword!'
          });
        },
        /Credenciales inválidas/
      );
    });

    test('should clear session on logout', async () => {
      await api.login({
        email: 'junta@lasbrisas.gt',
        password: 'Junta2026!'
      });
      assert.ok(api.getCurrentUser() !== null);

      api.logout();
      assert.strictEqual(api.getCurrentUser(), null);
    });
  });

  describe('Parking & Security Module (US-601, US-602, US-603)', () => {
    test('should list 20 parking spots', async () => {
      const spots = await api.getParkingSpots();
      assert.strictEqual(spots.length, 20);
      assert.strictEqual(spots[0].spotNumber, 'P-01');
    });

    test('should register a vehicle entry and occupy the spot', async () => {
      const entry = await api.registerEntry({
        plate: 'P-999TEST',
        houseId: 'h-101',
        houseNumber: 'Casa 101',
        visitorName: 'Juan Pérez',
        visitorIdDocument: 'DPI-1234567890101',
        spotNumber: 'P-18',
        guardUserId: 'guard-1'
      });

      assert.strictEqual(entry.plate, 'P-999TEST');
      assert.strictEqual(entry.spotNumber, 'P-18');
      assert.strictEqual(entry.status, 'ACTIVE');

      const spots = await api.getParkingSpots();
      const spot18 = spots.find(s => s.spotNumber === 'P-18');
      assert.strictEqual(spot18?.isOccupied, true);
    });

    test('should register a vehicle exit and release the parking spot', async () => {
      const entry = await api.registerEntry({
        plate: 'P-EXIT01',
        houseId: 'h-102',
        houseNumber: 'Casa 102',
        visitorName: 'Maria Lopez',
        visitorIdDocument: 'DPI-9876543210101',
        spotNumber: 'P-20',
        guardUserId: 'guard-1'
      });

      const exitResult = await api.registerExit(entry.id, 'P-20');
      assert.strictEqual(exitResult.success, true);

      const spots = await api.getParkingSpots();
      const spot20 = spots.find(s => s.spotNumber === 'P-20');
      assert.strictEqual(spot20?.isOccupied, false);
    });

    test('should retrieve covert resident alerts', async () => {
      const suspects = await api.getCovertResidents(7);
      assert.ok(suspects.length >= 1);
      const critical = suspects.find(s => s.suspicionLevel === 'CRITICAL');
      assert.ok(critical);
      assert.ok(critical.overnightCount >= 3);
    });

    test('should apply and list parking fines', async () => {
      const fine = await api.applyParkingFine({
        plate: 'P-888NOCT',
        houseId: 'h-110',
        houseNumber: 'Casa 110',
        fineAmount: 250,
        reason: 'Uso indebido de parqueo de visitas',
        evidenceSummary: '4 pernoctaciones consecutivas',
        appliedByUserId: 'usr-admin-01',
        appliedByName: 'Administración'
      });

      assert.strictEqual(fine.fineAmount, 250);
      assert.strictEqual(fine.plate, 'P-888NOCT');

      const fines = await api.getParkingFines();
      assert.ok(fines.some(f => f.plate === 'P-888NOCT'));
    });
  });

  describe('Governance & Assemblies Module (US-401, US-402, US-403)', () => {
    test('should fetch active assemblies', async () => {
      const assemblies = await api.getAssemblies();
      assert.ok(assemblies.length >= 1);
      assert.strictEqual(assemblies[0].status, 'IN_PROGRESS');
    });

    test('should convoke a new extraordinary assembly', async () => {
      const newAsm = await api.convokeAssembly({
        title: 'Asamblea Extraordinaria Presupuesto Techo',
        assemblyType: 'EXTRAORDINARY',
        scheduledAt: new Date(Date.now() + 86400000).toISOString(),
        agenda: 'Discusión y votación de cuota extraordinaria',
        totalResidentialHouses: 50
      });

      assert.strictEqual(newAsm.assemblyType, 'EXTRAORDINARY');
      assert.strictEqual(newAsm.totalResidentialHouses, 50);
    });

    test('should calculate live assembly quorum', async () => {
      const quorum = await api.getAssemblyQuorum('asm-2026-01');
      assert.strictEqual(quorum.totalResidentialHouses, 50);
      assert.ok(quorum.presentHouses >= 26);
      assert.strictEqual(quorum.hasQuorum, true);
    });

    test('should register attendance and update quorum', async () => {
      const res = await api.registerAssemblyAttendance('asm-2026-01', 'Casa 119');
      assert.strictEqual(res.success, true);
      assert.ok(res.presentHouses >= 26);
    });

    test('should create, open and close an assembly motion', async () => {
      const motion = await api.createMotion(
        'asm-2026-01',
        'Cambio de luminarias a tecnología Solar LED',
        'Inversión comunitaria para reducir factura de alumbrado público'
      );
      assert.strictEqual(motion.status, 'PENDING');

      const opened = await api.openMotion(motion.id);
      assert.strictEqual(opened.status, 'OPEN');

      const closed = await api.closeMotion(motion.id);
      assert.strictEqual(closed.status, 'CLOSED');
      assert.ok(closed.verdict === 'APPROVED' || closed.verdict === 'REJECTED');
      assert.ok(closed.totalVotes > 0);
    });
  });

  describe('Maintenance Tickets Module (US-501, US-502)', () => {
    test('should list tickets and create a new ticket', async () => {
      const ticketsBefore = await api.getTickets();
      const countBefore = ticketsBefore.length;

      const created = await api.createTicket({
        title: 'Fuga en aspersor #3',
        category: 'GARDENING',
        location: 'Parque Infantil',
        description: 'El aspersor no gira y acumula lodo.',
        priority: 'MEDIUM',
        reporterName: 'Carlos Méndez',
        houseNumber: 'Casa 105'
      });

      assert.strictEqual(created.title, 'Fuga en aspersor #3');
      assert.strictEqual(created.status, 'OPEN');

      const ticketsAfter = await api.getTickets();
      assert.strictEqual(ticketsAfter.length, countBefore + 1);
    });

    test('should update ticket status to IN_PROGRESS and RESOLVED', async () => {
      const updated = await api.updateTicketStatus(
        'tkt-001',
        'RESOLVED',
        'Tubería reparada y sellada con empaque nuevo.',
        'Mario López (Fontanero)'
      );

      assert.strictEqual(updated.status, 'RESOLVED');
    });
  });

  describe('Amenities & Privacy Calendar Module (US-301, US-302)', () => {
    test('should list configured amenities', async () => {
      const amenities = await api.getAmenities();
      assert.ok(amenities.length >= 3);
      assert.ok(amenities.some(a => a.code === 'SALON_SOCIAL'));
    });

    test('should provide privacy-compliant calendar slots', async () => {
      const slots = await api.getCalendarSlots('am-01', '2026-11-01');
      assert.ok(slots.length > 0);
      slots.forEach(slot => {
        assert.ok(
          slot.displayStatus === 'Disponible' || slot.displayStatus === 'Reservado',
          'Must protect resident privacy by strictly showing Disponible or Reservado'
        );
      });
    });
  });

  describe('Finance & Fixed Assets Module (US-201, US-203, US-205)', () => {
    test('should list expense invoices and fixed assets', async () => {
      const invoices = await api.getInvoices();
      assert.ok(invoices.length >= 3);

      const assets = await api.getFixedAssets();
      assert.ok(assets.length >= 3);
      assert.ok(assets.some(a => a.assetCode === 'ACT-GAR-001'));
    });

    test('should process bank reconciliation summary', async () => {
      const dummyFile = new File(['Dummy excel content'], 'banco_industrial_octubre.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });

      const summary = await api.uploadBankStatement(dummyFile);
      assert.ok(summary.totalRecords > 0);
      assert.ok(summary.matchRate > 90);
      assert.ok(summary.totalAmountMatched > 0);
    });
  });
});
