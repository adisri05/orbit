#!/bin/bash

# Quick test of Recommendation Service
# Works even if Progress/Analytics services are down (uses fallback rules)

RECOMMENDATION_SERVICE_URL="http://localhost:8084"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "=========================================="
echo "Quick Recommendation Service Test"
echo "=========================================="
echo ""

# Check if Recommendation Service is running
if ! lsof -ti:8084 > /dev/null 2>&1; then
    echo -e "${RED}✗ Recommendation Service is NOT running on port 8084${NC}"
    echo ""
    echo "Start it with:"
    echo "  cd backend/recommendation-service"
    echo "  mvn spring-boot:run"
    exit 1
fi

echo -e "${BLUE}Testing Recommendation Service...${NC}"
echo ""

# Test 1: Basic recommendation
echo "Test 1: Get recommendation for user 'test'"
RESPONSE=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/test/next")

if [ -z "$RESPONSE" ] || [ "$RESPONSE" = "null" ]; then
    echo -e "${RED}✗ No response${NC}"
    exit 1
fi

echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
echo ""

# Validate response structure
if echo "$RESPONSE" | python3 -c "
import json, sys
try:
    data = json.load(sys.stdin)
    required = ['type', 'targetId', 'title', 'reason', 'confidence', 'ruleApplied']
    missing = [f for f in required if f not in data]
    if missing:
        print(f'✗ Missing fields: {missing}')
        sys.exit(1)
    if data['type'] not in ['LESSON', 'COURSE', 'PATH']:
        print(f'✗ Invalid type: {data[\"type\"]}')
        sys.exit(1)
    if not 0 <= data['confidence'] <= 1:
        print(f'✗ Invalid confidence: {data[\"confidence\"]}')
        sys.exit(1)
    print('✓ API contract valid')
    print(f'✓ Rule: {data[\"ruleApplied\"]}')
    print(f'✓ Type: {data[\"type\"]}')
    print(f'✓ Confidence: {data[\"confidence\"]}')
except Exception as e:
    print(f'✗ Invalid JSON: {e}')
    sys.exit(1)
" 2>/dev/null; then
    echo -e "${GREEN}✓ Test passed!${NC}"
else
    echo -e "${RED}✗ Test failed${NC}"
    exit 1
fi

echo ""
echo "=========================================="
echo "Test Complete!"
echo "=========================================="
echo ""
echo -e "${YELLOW}Note:${NC} This test works even if Progress/Analytics services are down."
echo "The service uses fallback rules (COLD_START or SAFE_DEFAULT)."

