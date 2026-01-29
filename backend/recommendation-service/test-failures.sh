#!/bin/bash

# Failure Scenario Tests
# Tests graceful handling of service failures

RECOMMENDATION_SERVICE_URL="http://localhost:8084"
PROGRESS_SERVICE_URL="http://localhost:8082"
ANALYTICS_SERVICE_URL="http://localhost:8083"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "=========================================="
echo "Failure Scenario Tests"
echo "=========================================="
echo ""

USER_ID="failure_test_user"

# Test 1: Progress Service Down
echo -e "${BLUE}[Test 1] Progress Service Down${NC}"
echo -e "${YELLOW}⚠ Manual Test Required:${NC}"
echo "1. Stop Progress Service"
echo "2. Call: curl $RECOMMENDATION_SERVICE_URL/recommendations/users/$USER_ID/next"
echo "3. Expected: Graceful failure or fallback recommendation"
echo "4. Should NOT crash or return 500 error"
echo ""

# Test 2: Analytics Service Down
echo -e "${BLUE}[Test 2] Analytics Service Down${NC}"
echo -e "${YELLOW}⚠ Manual Test Required:${NC}"
echo "1. Stop Analytics Service"
echo "2. Call: curl $RECOMMENDATION_SERVICE_URL/recommendations/users/$USER_ID/next"
echo "3. Expected: Graceful fallback using available data"
echo "4. Should NOT crash or return 500 error"
echo ""

# Test 3: Both Services Down
echo -e "${BLUE}[Test 3] Both Services Down${NC}"
echo -e "${YELLOW}⚠ Manual Test Required:${NC}"
echo "1. Stop both Progress and Analytics Services"
echo "2. Call: curl $RECOMMENDATION_SERVICE_URL/recommendations/users/$USER_ID/next"
echo "3. Expected: Fallback recommendation (COLD_START or SAFE_DEFAULT)"
echo "4. Should NOT crash"
echo ""

# Test 4: Service Unavailable (timeout)
echo -e "${BLUE}[Test 4] Service Timeout${NC}"
echo -e "${YELLOW}⚠ Manual Test Required:${NC}"
echo "1. Block network access to Progress/Analytics services"
echo "2. Call recommendation API"
echo "3. Expected: Timeout handling, fallback recommendation"
echo ""

echo -e "${GREEN}=========================================="
echo "Failure Test Guide Complete"
echo "==========================================${NC}"
echo ""
echo "Run these tests manually to verify graceful failure handling"

