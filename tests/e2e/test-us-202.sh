#!/usr/bin/env bash
# =============================================================================
# Script: test-us-202.sh
# Purpose: Autonomous E2E API Verification for US-202 (Bank Reconciliation)
# Intended for: Developers, QA Engineers, and Autonomous AI Agents
# =============================================================================

set -e

BASE_URL="http://127.0.0.1:8082/api/v1/finance/reconciliation"
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

echo -e "\n${YELLOW}=== STEP 1: Preparing Bank Statement File (CSV) ===${NC}"
TEST_CSV="/tmp/bank_statement_oct2026.csv"
cat << 'EOF' > "$TEST_CSV"
Authorization,HouseRef,Date,Amount
AUTH-GT-88001,M-01 L-01,2026-10-05,650.00
AUTH-GT-88002,M-01 L-02,2026-10-06,650.00
AUTH-GT-88003,M-99 L-99,2026-10-07,650.00
EOF

echo -e "  Created test bank report with 3 rows (2 matching pending charges, 1 unmatched house)."

echo -e "\n${YELLOW}=== STEP 2: Executing Bank Report Upload ===${NC}"
HTTP_CODE=$(curl -s -o /tmp/recon_resp.json -w "%{http_code}" -X POST "$BASE_URL/upload" \
  -F "file=@$TEST_CSV;type=text/csv")

assert_status 200 "$HTTP_CODE" "2.1: Upload bank statement successfully"

# Parse JSON response summary values
RECONCILED=$(grep -o '"successfullyReconciled":[0-9]*' /tmp/recon_resp.json | cut -d: -f2)
UNMATCHED=$(grep -o '"unmatchedTransactions":[0-9]*' /tmp/recon_resp.json | cut -d: -f2)
DUPLICATES=$(grep -o '"duplicateTransactions":[0-9]*' /tmp/recon_resp.json | cut -d: -f2)

if [ "$RECONCILED" -eq 2 ]; then
    echo -e "  [${GREEN}PASS${NC}] Summary: Exactly 2 transactions successfully reconciled."
    PASSED=$((PASSED + 1))
else
    echo -e "  [${RED}FAIL${NC}] Expected 2 reconciled, got $RECONCILED"
    FAILED=$((FAILED + 1))
fi

if [ "$UNMATCHED" -eq 1 ]; then
    echo -e "  [${GREEN}PASS${NC}] Summary: Exactly 1 transaction unmatched (saved for auditor review)."
    PASSED=$((PASSED + 1))
else
    echo -e "  [${RED}FAIL${NC}] Expected 1 unmatched, got $UNMATCHED"
    FAILED=$((FAILED + 1))
fi

echo -e "\n${YELLOW}=== STEP 3: Testing Idempotency & Duplicate Rejection ===${NC}"
# Re-upload the exact same file to verify duplicate authorization handling
HTTP_CODE=$(curl -s -o /tmp/recon_dup_resp.json -w "%{http_code}" -X POST "$BASE_URL/upload" \
  -F "file=@$TEST_CSV;type=text/csv")
assert_status 200 "$HTTP_CODE" "3.1: Re-upload statement handled gracefully"

DUP_COUNT=$(grep -o '"duplicateTransactions":[0-9]*' /tmp/recon_dup_resp.json | cut -d: -f2)
if [ "$DUP_COUNT" -ge 2 ]; then
    echo -e "  [${GREEN}PASS${NC}] Idempotency verified: Duplicate authorizations skipped without double-charging."
    PASSED=$((PASSED + 1))
else
    echo -e "  [${RED}FAIL${NC}] Expected duplicate authorizations to be skipped, got count $DUP_COUNT"
    FAILED=$((FAILED + 1))
fi

echo -e "\n${YELLOW}=== STEP 4: Direct Database State Inspection ===${NC}"
PAID_CHARGES=$(docker exec -i postgres_local psql -U postgres -d residencial_db -t -A -c \
  "SELECT count(*) FROM finance_schema.maintenance_charges WHERE status = 'PAID' AND house_number IN ('M-01 L-01', 'M-01 L-02');")

if [ "$PAID_CHARGES" -eq 2 ]; then
    echo -e "  [${GREEN}PASS${NC}] Database state verified: Both maintenance charges marked as PAID."
    PASSED=$((PASSED + 1))
else
    echo -e "  [${RED}FAIL${NC}] Expected 2 PAID charges, got $PAID_CHARGES"
    FAILED=$((FAILED + 1))
fi

echo -e "\n======================================================="
echo -e "E2E Test Results: ${GREEN}$PASSED Passed${NC}, ${RED}$FAILED Failed${NC}"
echo -e "=======================================================\n"

if [ "$FAILED" -gt 0 ]; then
    exit 1
fi
