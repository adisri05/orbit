#!/bin/bash

# Recommendation Service End-to-End Verification Script
# This script verifies Phase 9: Recommendation Service

set -e

echo "=========================================="
echo "Recommendation Service Verification"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
PROGRESS_SERVICE_URL="http://localhost:8082"
ANALYTICS_SERVICE_URL="http://localhost:8083"
RECOMMENDATION_SERVICE_URL="http://localhost:8084"

# Step 1: Check Dependencies
echo -e "${BLUE}[Step 1] Checking Dependencies...${NC}"
echo ""

# Check Progress Service
echo -e "${YELLOW}Checking Progress Service...${NC}"
if curl -s "$PROGRESS_SERVICE_URL/progress/users/test/paths/test" > /dev/null 2>&1 || \
   curl -s "$PROGRESS_SERVICE_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Progress Service is running${NC}"
else
    echo -e "${RED}✗ Progress Service is NOT running on $PROGRESS_SERVICE_URL${NC}"
    echo "Please start Progress Service first"
    exit 1
fi

# Check Analytics Service
echo -e "${YELLOW}Checking Analytics Service...${NC}"
if curl -s "$ANALYTICS_SERVICE_URL/analytics/platform/overview" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Analytics Service is running${NC}"
else
    echo -e "${RED}✗ Analytics Service is NOT running on $ANALYTICS_SERVICE_URL${NC}"
    echo "Please start Analytics Service first"
    exit 1
fi

# Check Recommendation Service
echo -e "${YELLOW}Checking Recommendation Service...${NC}"
if curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/test/next" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Recommendation Service is running${NC}"
else
    echo -e "${RED}✗ Recommendation Service is NOT running on $RECOMMENDATION_SERVICE_URL${NC}"
    echo "Please start Recommendation Service first"
    exit 1
fi
echo ""

# Step 2: Verify No Kafka/Database Dependencies
echo -e "${BLUE}[Step 2] Verifying No Kafka/Database Dependencies...${NC}"
echo "Checking application.yaml..."
if grep -q "kafka" backend/recommendation-service/src/main/resources/application.yaml 2>/dev/null; then
    echo -e "${RED}✗ Kafka configuration found${NC}"
    exit 1
fi
if grep -q "datasource\|jpa\|mongodb" backend/recommendation-service/src/main/resources/application.yaml 2>/dev/null; then
    echo -e "${RED}✗ Database configuration found${NC}"
    exit 1
fi
echo -e "${GREEN}✓ No Kafka or database dependencies${NC}"
echo ""

# Step 3: Test API Contract
echo -e "${BLUE}[Step 3] Testing API Contract...${NC}"
RESPONSE=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/user123/next")
echo "Response: $RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
echo ""

# Validate response structure
if echo "$RESPONSE" | python3 -c "
import json, sys
data = json.load(sys.stdin)
required = ['type', 'targetId', 'title', 'reason', 'confidence', 'ruleApplied']
missing = [f for f in required if f not in data]
if missing:
    print(f'Missing fields: {missing}')
    sys.exit(1)
if data['type'] not in ['LESSON', 'COURSE', 'PATH']:
    print(f'Invalid type: {data[\"type\"]}')
    sys.exit(1)
if not 0 <= data['confidence'] <= 1:
    print(f'Invalid confidence: {data[\"confidence\"]}')
    sys.exit(1)
" 2>/dev/null; then
    echo -e "${GREEN}✓ API contract valid${NC}"
else
    echo -e "${RED}✗ API contract invalid${NC}"
    exit 1
fi
echo ""

# Step 4: Test Determinism
echo -e "${BLUE}[Step 4] Testing Determinism...${NC}"
RESPONSE1=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/user123/next")
sleep 1
RESPONSE2=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/user123/next")
sleep 1
RESPONSE3=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/user123/next")

if [ "$RESPONSE1" = "$RESPONSE2" ] && [ "$RESPONSE2" = "$RESPONSE3" ]; then
    echo -e "${GREEN}✓ Deterministic: Same input produces same output${NC}"
else
    echo -e "${RED}✗ Non-deterministic: Different outputs for same input${NC}"
    echo "Response 1: $RESPONSE1"
    echo "Response 2: $RESPONSE2"
    echo "Response 3: $RESPONSE3"
    exit 1
fi
echo ""

# Step 5: Test Rule Priority
echo -e "${BLUE}[Step 5] Testing Rule Priority...${NC}"
echo "Testing that higher priority rules win over lower priority ones..."
echo "This requires specific test data setup in Progress/Analytics services"
echo -e "${YELLOW}⚠ Manual verification needed: Ensure incomplete lesson exists${NC}"
echo ""

# Step 6: Test Statelessness
echo -e "${BLUE}[Step 6] Testing Statelessness...${NC}"
echo "Before restart:"
BEFORE_RESTART=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/user123/next")
echo "$BEFORE_RESTART" | python3 -m json.tool 2>/dev/null || echo "$BEFORE_RESTART"
echo ""
echo -e "${YELLOW}⚠ Manual test: Restart service and verify same recommendation${NC}"
echo ""

# Step 7: Test Failure Scenarios
echo -e "${BLUE}[Step 7] Testing Failure Scenarios...${NC}"
echo -e "${YELLOW}⚠ Manual test: Stop Progress Service and verify graceful failure${NC}"
echo -e "${YELLOW}⚠ Manual test: Stop Analytics Service and verify graceful fallback${NC}"
echo ""

echo -e "${GREEN}=========================================="
echo "Basic Verification Complete!"
echo "==========================================${NC}"
echo ""
echo "For detailed rule-by-rule testing, see:"
echo "  - test-rules.sh"
echo "  - VERIFICATION.md"

