#!/usr/bin/env bash
# =============================================================================
# Script: test-us-203.sh
# Purpose: Autonomous E2E API Verification for US-203 (Fine Configuration & Application)
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

echo -e "\n${YELLOW}=== STEP 1: Testing US-203 (Fine Configuration & Application) ===${NC}"

HOUSE_UUID_1="11111111-2222-3333-4444-555555555555"
HOUSE_UUID_2="22222222-3333-4444-5555-666666666666"

# Case 1.1: Successfully apply fine with default catalog amount (NOISE_VIOLATION -> Q300.00)
HTTP_CODE=$(curl -s -o /tmp/us203_resp1.json -w "%{http_code}" -X POST "$BASE_URL/fines" \
  -H "Content-Type: application/json" \
  -d '{
    "houseId": "'"$HOUSE_UUID_1"'",
    "houseNumber": "C-101",
    "fineTypeCode": "NOISE_VIOLATION",
    "customAmount": null,
    "reason": "Música a alto volumen después de las 22:00 horas detectada por guardia de garita.",
    "evidenceUrl": "https://storage.example.com/fines/noise-c101.jpg",
    "appliedByUserId": "00000000-0000-0000-0000-000000000001"
  }')
assert_status 201 "$HTTP_CODE" "1.1: Apply fine with catalog default amount (NOISE_VIOLATION)"

AMOUNT=$(grep -o '"amount":[^,]*' /tmp/us203_resp1.json | cut -d: -f2 | tr -d ' ')
STATUS=$(grep -o '"status":"[^"]*' /tmp/us203_resp1.json | cut -d: -f2 | tr -d '"')
FINE_TYPE_NAME=$(grep -o '"fineTypeName":"[^"]*' /tmp/us203_resp1.json | cut -d: -f2 | tr -d '"')

if [ "$AMOUNT" = "300.00" ] && [ "$STATUS" = "PENDING" ]; then
    echo -e "  [${GREEN}PASS${NC}] Fine 1.1 amount is Q300.00 and status is PENDING"
    PASSED=$((PASSED + 1))
else
    echo -e "  [${RED}FAIL${NC}] Fine 1.1 unexpected values: amount=$AMOUNT, status=$STATUS"
    FAILED=$((FAILED + 1))
fi

# Case 1.2: Successfully apply fine with custom amount override (SPEEDING -> Q750.00 override)
HTTP_CODE=$(curl -s -o /tmp/us203_resp2.json -w "%{http_code}" -X POST "$BASE_URL/fines" \
  -H "Content-Type: application/json" \
  -d '{
    "houseId": "'"$HOUSE_UUID_2"'",
    "houseNumber": "C-102",
    "fineTypeCode": "SPEEDING",
    "customAmount": 750.00,
    "reason": "Vehículo conducido a 60 km/h en calle principal residencial (reincidencia).",
    "evidenceUrl": "https://storage.example.com/fines/radar-c102.mp4",
    "appliedByUserId": "00000000-0000-0000-0000-000000000001"
  }')
assert_status 201 "$HTTP_CODE" "1.2: Apply fine with custom amount override (Q750.00)"

AMOUNT2=$(grep -o '"amount":[^,]*' /tmp/us203_resp2.json | cut -d: -f2 | tr -d ' ')
if [ "$AMOUNT2" = "750.00" ]; then
    echo -e "  [${GREEN}PASS${NC}] Fine 1.2 custom amount verified as Q750.00"
    PASSED=$((PASSED + 1))
else
    echo -e "  [${RED}FAIL${NC}] Fine 1.2 unexpected custom amount: $AMOUNT2"
    FAILED=$((FAILED + 1))
fi

# Case 1.3: Invariant - Reject fine for non-existent fine type code
HTTP_CODE=$(curl -s -o /tmp/us203_resp3.json -w "%{http_code}" -X POST "$BASE_URL/fines" \
  -H "Content-Type: application/json" \
  -d '{
    "houseId": "'"$HOUSE_UUID_1"'",
    "houseNumber": "C-101",
    "fineTypeCode": "NON_EXISTENT_VIOLATION",
    "reason": "Motivo cualquiera",
    "appliedByUserId": "00000000-0000-0000-0000-000000000001"
  }')
assert_status 400 "$HTTP_CODE" "1.3: Invariant - Reject non-existent fine type (400 Bad Request)"

# Case 1.4: Invariant - Reject fine with missing required fields (empty houseNumber and reason)
HTTP_CODE=$(curl -s -o /tmp/us203_resp4.json -w "%{http_code}" -X POST "$BASE_URL/fines" \
  -H "Content-Type: application/json" \
  -d '{
    "houseId": "'"$HOUSE_UUID_1"'",
    "houseNumber": "",
    "fineTypeCode": "PARKING_ABUSE",
    "reason": ""
  }')
assert_status 400 "$HTTP_CODE" "1.4: Invariant - Reject payload with missing required fields (400 Bad Request)"

# Case 1.5: Invariant - Reject fine with customAmount <= 0
HTTP_CODE=$(curl -s -o /tmp/us203_resp5.json -w "%{http_code}" -X POST "$BASE_URL/fines" \
  -H "Content-Type: application/json" \
  -d '{
    "houseId": "'"$HOUSE_UUID_1"'",
    "houseNumber": "C-101",
    "fineTypeCode": "PARKING_ABUSE",
    "customAmount": -50.00,
    "reason": "Vehículo mal estacionado"
  }')
assert_status 400 "$HTTP_CODE" "1.5: Invariant - Reject non-positive custom amount (400 Bad Request)"

echo -e "\n${YELLOW}=== STEP 2: Database State Verification (PostgreSQL) ===${NC}"
DB_COUNT=$(docker exec -i postgres_local psql -U postgres -d residencial_db -t -A -c "SELECT COUNT(*) FROM finance_schema.fines;")
if [ "$DB_COUNT" -ge 2 ]; then
    echo -e "  [${GREEN}PASS${NC}] Database verification: $DB_COUNT fines recorded in finance_schema.fines"
    PASSED=$((PASSED + 1))
else
    echo -e "  [${RED}FAIL${NC}] Database verification failed: Expected at least 2 fines, found $DB_COUNT"
    FAILED=$((FAILED + 1))
fi

echo -e "\n========================================================"
echo -e "US-203 E2E Test Summary: ${GREEN}$PASSED Passed${NC}, ${RED}$FAILED Failed${NC}"
echo -e "========================================================"

if [ "$FAILED" -gt 0 ]; then
    exit 1
fi

exit 0
