#!/usr/bin/env bash
# =============================================================================
# Script: test-us-201.sh
# Purpose: Autonomous E2E API Verification for US-201 (Expense Invoices)
# Intended for: Developers, QA Engineers, and Autonomous AI Agents
# =============================================================================

set -e

BASE_URL="http://127.0.0.1:8082/api/v1/finance"
PASSED=0
FAILED=0

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

assert_status() {
    local expected="$1"
    local actual="$2"
    local test_name="$3"
    if [ "$expected" -eq "$actual" ]; then
        echo -e "  [${GREEN}PASS${NC}] $test_name (HTTP $actual)"
        PASSED=$((PASSED + 1))
    else
        echo -e "  [${RED}FAIL${NC}] $test_name (Expected HTTP $expected, got $actual)"
        FAILED=$((FAILED + 1))
    fi
}

echo -e "\n${YELLOW}=== STEP 0: Finance Service Health Check ===${NC}"
HTTP_CODE=$(curl -s -o /tmp/finance_health.json -w "%{http_code}" "$BASE_URL/health")
assert_status 200 "$HTTP_CODE" "Finance Service is Healthy"

echo -e "\n${YELLOW}=== STEP 1: Testing US-201 (Register Expense Invoices) ===${NC}"

# Case 1.1: Successfully register invoice with auto-created vendor and valid category
HTTP_CODE=$(curl -s -o /tmp/us201_resp1.json -w "%{http_code}" -X POST "$BASE_URL/invoices" \
  -H "Content-Type: application/json" \
  -d '{
    "invoiceNumber": "FAC-2026-901",
    "vendorTaxId": "NIT-998877-K",
    "vendorName": "Seguridad Total Privada S.A.",
    "vendorEmail": "facturacion@seguridadtotal.com",
    "vendorPhone": "2333-8899",
    "categoryCode": "SECURITY",
    "issueDate": "2026-10-01",
    "dueDate": "2026-10-31",
    "amount": 14500.50,
    "documentUrl": "https://storage.example.com/invoices/fac-901.pdf",
    "notes": "Factura por servicio de vigilancia armada garita octubre 2026"
  }')
assert_status 201 "$HTTP_CODE" "1.1: Register Expense Invoice successfully"

# Case 1.2: Reject duplicate invoice number for the same vendor
HTTP_CODE=$(curl -s -o /tmp/us201_dup.json -w "%{http_code}" -X POST "$BASE_URL/invoices" \
  -H "Content-Type: application/json" \
  -d '{
    "invoiceNumber": "FAC-2026-901",
    "vendorTaxId": "NIT-998877-K",
    "vendorName": "Seguridad Total Privada S.A.",
    "categoryCode": "SECURITY",
    "issueDate": "2026-10-01",
    "dueDate": "2026-10-31",
    "amount": 14500.50
  }')
assert_status 400 "$HTTP_CODE" "1.2: Reject duplicate invoice for same vendor"

# Case 1.3: Reject invoice with non-existent category
HTTP_CODE=$(curl -s -o /tmp/us201_bad_cat.json -w "%{http_code}" -X POST "$BASE_URL/invoices" \
  -H "Content-Type: application/json" \
  -d '{
    "invoiceNumber": "FAC-2026-902",
    "vendorTaxId": "NIT-998877-K",
    "vendorName": "Seguridad Total Privada S.A.",
    "categoryCode": "NON_EXISTING_CAT",
    "issueDate": "2026-10-01",
    "dueDate": "2026-10-31",
    "amount": 500.00
  }')
assert_status 400 "$HTTP_CODE" "1.3: Reject invoice with invalid category"

# Case 1.4: Reject invoice where due date is before issue date
HTTP_CODE=$(curl -s -o /tmp/us201_bad_dates.json -w "%{http_code}" -X POST "$BASE_URL/invoices" \
  -H "Content-Type: application/json" \
  -d '{
    "invoiceNumber": "FAC-2026-903",
    "vendorTaxId": "NIT-112233-4",
    "vendorName": "Empresa Electrica Común",
    "categoryCode": "ELECTRICITY",
    "issueDate": "2026-10-25",
    "dueDate": "2026-10-10",
    "amount": 3200.00
  }')
assert_status 400 "$HTTP_CODE" "1.4: Reject invoice where due date is before issue date"

# Case 1.5: Register second invoice for different vendor and category (Electricity)
HTTP_CODE=$(curl -s -o /tmp/us201_resp2.json -w "%{http_code}" -X POST "$BASE_URL/invoices" \
  -H "Content-Type: application/json" \
  -d '{
    "invoiceNumber": "ELEC-2026-OCT",
    "vendorTaxId": "NIT-112233-4",
    "vendorName": "Empresa Electrica Común",
    "categoryCode": "ELECTRICITY",
    "issueDate": "2026-10-15",
    "dueDate": "2026-10-30",
    "amount": 4820.75,
    "documentUrl": "https://storage.example.com/invoices/elec-oct.pdf"
  }')
assert_status 201 "$HTTP_CODE" "1.5: Register Electricity Invoice successfully"

echo -e "\n${YELLOW}=== STEP 2: Database State Verification ===${NC}"
INVOICE_COUNT=$(docker exec -i postgres_local psql -U postgres -d residencial_db -t -A -c \
  "SELECT count(*) FROM finance_schema.invoices WHERE status = 'PENDING';")

if [ "$INVOICE_COUNT" -eq 2 ]; then
    echo -e "  [${GREEN}PASS${NC}] Database state verified: Exactly 2 pending expense invoices persisted."
    PASSED=$((PASSED + 1))
else
    echo -e "  [${RED}FAIL${NC}] Database state check failed: Expected 2 invoices, found $INVOICE_COUNT."
    FAILED=$((FAILED + 1))
fi

echo -e "\n======================================================="
echo -e "E2E Test Results: ${GREEN}$PASSED Passed${NC}, ${RED}$FAILED Failed${NC}"
echo -e "=======================================================\n"

if [ "$FAILED" -gt 0 ]; then
    exit 1
fi
