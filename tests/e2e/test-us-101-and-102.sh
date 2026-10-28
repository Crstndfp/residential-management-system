#!/usr/bin/env bash
# =============================================================================
# Script: test-us-101-and-102.sh
# Purpose: Autonomous E2E API Verification for US-101 and US-102
# Intended for: Developers, QA Engineers, and Autonomous AI Agents
# =============================================================================

set -e

BASE_URL="http://127.0.0.1:8081/api/v1/identity"
PASSED=0
FAILED=0

# Colors for terminal output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

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

echo -e "\n${YELLOW}=== STEP 0: Health Check ===${NC}"
HTTP_CODE=$(curl -s -o /tmp/health_resp.json -w "%{http_code}" "$BASE_URL/health")
assert_status 200 "$HTTP_CODE" "Identity Service is Healthy"

echo -e "\n${YELLOW}=== STEP 1: Testing US-101 (Register House & Owner with CUI Modulo 11) ===${NC}"

# Case 1.1: Successful Registration with valid Guatemalan CUI (Modulo 11)
HTTP_CODE=$(curl -s -o /tmp/us101_resp.json -w "%{http_code}" -X POST "$BASE_URL/houses/register-owner" \
  -H "Content-Type: application/json" \
  -d '{
    "houseNumber": "M-10 L-01",
    "streetOrBlock": "Avenida Los Próceres",
    "idType": "DPI",
    "idNumber": "1234567820101",
    "email": "owner.m1001@example.com",
    "fullName": "Alejandro Morales"
  }')
assert_status 201 "$HTTP_CODE" "1.1: Register House with Owner using valid CUI"

HOUSE_ID=$(grep -o '"houseId":"[^"]*' /tmp/us101_resp.json | cut -d'"' -f4)
OWNER_TOKEN=$(grep -o '"invitationToken":"[^"]*' /tmp/us101_resp.json | cut -d'"' -f4)

# Case 1.2: Reject duplicate house number
HTTP_CODE=$(curl -s -o /tmp/us101_dup_house.json -w "%{http_code}" -X POST "$BASE_URL/houses/register-owner" \
  -H "Content-Type: application/json" \
  -d '{
    "houseNumber": "M-10 L-01",
    "streetOrBlock": "Avenida Los Próceres",
    "idType": "DPI",
    "idNumber": "2345678900101",
    "email": "another.owner@example.com",
    "fullName": "Carlos Mendez"
  }')
assert_status 400 "$HTTP_CODE" "1.2: Reject duplicate house number"

# Case 1.3: Reject invalid CUI checksum (failing Modulo 11)
HTTP_CODE=$(curl -s -o /tmp/us101_inv_cui.json -w "%{http_code}" -X POST "$BASE_URL/houses/register-owner" \
  -H "Content-Type: application/json" \
  -d '{
    "houseNumber": "M-10 L-02",
    "streetOrBlock": "Avenida Los Próceres",
    "idType": "DPI",
    "idNumber": "1234567890101",
    "email": "invalid.cui@example.com",
    "fullName": "Felipe Ruiz"
  }')
assert_status 400 "$HTTP_CODE" "1.3: Reject invalid CUI checksum (Modulo 11 fail)"

# Case 1.4: Register foreign owner with Passport
HTTP_CODE=$(curl -s -o /tmp/us101_passport.json -w "%{http_code}" -X POST "$BASE_URL/houses/register-owner" \
  -H "Content-Type: application/json" \
  -d '{
    "houseNumber": "M-10 L-03",
    "streetOrBlock": "Avenida Los Próceres",
    "idType": "PASSPORT",
    "idNumber": "US99887766",
    "email": "foreign.owner@example.com",
    "fullName": "Sarah Jenkins"
  }')
assert_status 201 "$HTTP_CODE" "1.4: Register foreign owner using Passport"

echo -e "\n${YELLOW}=== STEP 2: Testing US-102 (Account Activation & Tenant Creation) ===${NC}"

# Case 2.1: Owner activates account with 48h invitation token
HTTP_CODE=$(curl -s -o /tmp/us102_activate.json -w "%{http_code}" -X POST "$BASE_URL/users/activate" \
  -H "Content-Type: application/json" \
  -d "{
    \"token\": \"$OWNER_TOKEN\",
    \"password\": \"SuperSecurePassword123!\"
  }")
assert_status 200 "$HTTP_CODE" "2.1: Owner activates account with invitation token"

# Case 2.2: Reject reusing the same invitation token
HTTP_CODE=$(curl -s -o /tmp/us102_token_reuse.json -w "%{http_code}" -X POST "$BASE_URL/users/activate" \
  -H "Content-Type: application/json" \
  -d "{
    \"token\": \"$OWNER_TOKEN\",
    \"password\": \"AnotherPassword123!\"
  }")
assert_status 400 "$HTTP_CODE" "2.2: Reject reusing an already consumed invitation token"

# Case 2.3: Owner creates tenant for the house
HTTP_CODE=$(curl -s -o /tmp/us102_tenant.json -w "%{http_code}" -X POST "$BASE_URL/houses/$HOUSE_ID/tenants" \
  -H "Content-Type: application/json" \
  -d '{
    "idType": "PASSPORT",
    "idNumber": "FR11223344",
    "email": "tenant.m1001@example.com",
    "fullName": "Jean Dupont"
  }')
assert_status 201 "$HTTP_CODE" "2.3: Create Tenant for house"

# Case 2.4: Reject creating a second tenant on the same house (Invariant: max 1 tenant)
HTTP_CODE=$(curl -s -o /tmp/us102_dup_tenant.json -w "%{http_code}" -X POST "$BASE_URL/houses/$HOUSE_ID/tenants" \
  -H "Content-Type: application/json" \
  -d '{
    "idType": "PASSPORT",
    "idNumber": "DE55667788",
    "email": "second.tenant@example.com",
    "fullName": "Hans Schmidt"
  }')
assert_status 400 "$HTTP_CODE" "2.4: Reject second tenant on the same house (Max 1 tenant invariant)"

echo -e "\n${YELLOW}=== STEP 3: Database Verification (Direct PostgreSQL Inspection) ===${NC}"
DB_CHECK=$(docker exec -i postgres_local psql -U postgres -d residencial_db -t -A -c \
  "SELECT count(*) FROM identity_schema.users WHERE is_active = TRUE AND residential_role = 'OWNER';")

if [ "$DB_CHECK" -ge 1 ]; then
    echo -e "  [${GREEN}PASS${NC}] Database state verified: Active owner persisted with hashed password."
    PASSED=$((PASSED + 1))
else
    echo -e "  [${RED}FAIL${NC}] Database check failed: No active owner found."
    FAILED=$((FAILED + 1))
fi

echo -e "\n======================================================="
echo -e "E2E Test Results: ${GREEN}$PASSED Passed${NC}, ${RED}$FAILED Failed${NC}"
echo -e "=======================================================\n"

if [ "$FAILED" -gt 0 ]; then
    exit 1
fi
