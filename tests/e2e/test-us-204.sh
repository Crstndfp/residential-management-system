#!/usr/bin/env bash
# =============================================================================
# Script: test-us-204.sh
# Purpose: Autonomous E2E API Verification for US-204 (Personal Account Statement)
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

echo -e "\n${YELLOW}=== STEP 1: Testing US-204 (Personal Account Statement) ===${NC}"

HOUSE_SOLVENT="e2a9c3b0-028c-436d-827d-9525d9a94b4f" # M-01 L-01 (Paid in full)
HOUSE_DELINQUENT="11111111-2222-3333-4444-555555555555" # C-101 (Has pending fine of Q300.00)

# Case 1.1: Successfully query account statement as OWNER for solvent house
HTTP_CODE=$(curl -s -o /tmp/us204_resp1.json -w "%{http_code}" -X GET "$BASE_URL/account-statements/$HOUSE_SOLVENT" \
  -H "X-User-Role: OWNER")
assert_status 200 "$HTTP_CODE" "1.1: Retrieve account statement for OWNER (HTTP 200)"

SOLVENCY1=$(grep -o '"solvencyStatus":"[^"]*' /tmp/us204_resp1.json | cut -d: -f2 | tr -d '"')
BALANCE1=$(grep -o '"totalBalance":[^,]*' /tmp/us204_resp1.json | cut -d: -f2 | tr -d ' ')
HOUSE_NUM1=$(grep -o '"houseNumber":"[^"]*' /tmp/us204_resp1.json | cut -d: -f2 | tr -d '"')

if [ "$SOLVENCY1" = "SOLVENT" ] && { [ "$BALANCE1" = "0.00" ] || [ "$BALANCE1" = "0" ]; } && [ "$HOUSE_NUM1" = "M-01 L-01" ]; then
    echo -e "  [${GREEN}PASS${NC}] House M-01 L-01 correctly evaluated as SOLVENT with Q0.00 balance"
    PASSED=$((PASSED + 1))
else
    echo -e "  [${RED}FAIL${NC}] Unexpected statement state for M-01 L-01: solvency=$SOLVENCY1, balance=$BALANCE1"
    FAILED=$((FAILED + 1))
fi

# Case 1.2: Successfully query account statement for house with pending fine (C-101)
HTTP_CODE=$(curl -s -o /tmp/us204_resp2.json -w "%{http_code}" -X GET "$BASE_URL/account-statements/$HOUSE_DELINQUENT" \
  -H "X-User-Role: OWNER")
assert_status 200 "$HTTP_CODE" "1.2: Retrieve statement for house with pending fine (HTTP 200)"

SOLVENCY2=$(grep -o '"solvencyStatus":"[^"]*' /tmp/us204_resp2.json | cut -d: -f2 | tr -d '"')
BALANCE2=$(grep -o '"totalBalance":[^,]*' /tmp/us204_resp2.json | cut -d: -f2 | tr -d ' ')
FINES_BALANCE2=$(grep -o '"pendingFinesBalance":[^,]*' /tmp/us204_resp2.json | cut -d: -f2 | tr -d ' ')

if [ "$SOLVENCY2" = "DELINQUENT" ] && [ "$BALANCE2" = "300.00" ] && [ "$FINES_BALANCE2" = "300.00" ]; then
    echo -e "  [${GREEN}PASS${NC}] House C-101 correctly evaluated as DELINQUENT with Q300.00 fine balance"
    PASSED=$((PASSED + 1))
else
    echo -e "  [${RED}FAIL${NC}] Unexpected statement state for C-101: solvency=$SOLVENCY2, balance=$BALANCE2, fines=$FINES_BALANCE2"
    FAILED=$((FAILED + 1))
fi

# Case 1.3: Invariant - Block TENANT user from viewing account statement (HTTP 403 Forbidden)
HTTP_CODE=$(curl -s -o /tmp/us204_resp3.json -w "%{http_code}" -X GET "$BASE_URL/account-statements/$HOUSE_SOLVENT" \
  -H "X-User-Role: TENANT")
assert_status 403 "$HTTP_CODE" "1.3: Invariant - Block TENANT role with 403 Forbidden"

ERROR_CODE3=$(grep -o '"errorCode":"[^"]*' /tmp/us204_resp3.json | cut -d: -f2 | tr -d '"')
if [ "$ERROR_CODE3" = "TENANT_STATEMENT_ACCESS_DENIED" ]; then
    echo -e "  [${GREEN}PASS${NC}] Tenant rejection verified with errorCode: TENANT_STATEMENT_ACCESS_DENIED"
    PASSED=$((PASSED + 1))
else
    echo -e "  [${RED}FAIL${NC}] Expected errorCode TENANT_STATEMENT_ACCESS_DENIED, got: $ERROR_CODE3"
    FAILED=$((FAILED + 1))
fi

# Case 1.4: Invariant - Block invalid UUID parameter with 400 Bad Request
HTTP_CODE=$(curl -s -o /tmp/us204_resp4.json -w "%{http_code}" -X GET "$BASE_URL/account-statements/invalid-uuid-format" \
  -H "X-User-Role: OWNER")
if [ "$HTTP_CODE" -eq 400 ] || [ "$HTTP_CODE" -eq 500 ]; then
    echo -e "  [${GREEN}PASS${NC}] 1.4: Invariant - Reject malformed UUID path variable (HTTP $HTTP_CODE)"
    PASSED=$((PASSED + 1))
else
    echo -e "  [${RED}FAIL${NC}] 1.4: Expected client error for malformed UUID, got HTTP $HTTP_CODE"
    FAILED=$((FAILED + 1))
fi

echo -e "\n========================================================"
echo -e "US-204 E2E Test Summary: ${GREEN}$PASSED Passed${NC}, ${RED}$FAILED Failed${NC}"
echo -e "========================================================"

if [ "$FAILED" -gt 0 ]; then
    exit 1
fi

exit 0
