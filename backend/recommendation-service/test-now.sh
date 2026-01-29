#!/bin/bash

# Quick test script - Test Recommendation Service now that it's running

RECOMMENDATION_SERVICE_URL="http://localhost:8084"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "=========================================="
echo "Testing Recommendation Service"
echo "=========================================="
echo ""

# Test 1: Basic recommendation
echo -e "${BLUE}[Test 1] Get recommendation for user 'test'${NC}"
RESPONSE=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/test/next")

if [ -z "$RESPONSE" ] || [ "$RESPONSE" = "null" ]; then
    echo -e "${RED}✗ No response${NC}"
    exit 1
fi

echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
echo ""

# Validate response
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
    print(f'✓ Target: {data[\"targetId\"]}')
except Exception as e:
    print(f'✗ Error: {e}')
    sys.exit(1)
" 2>/dev/null; then
    echo -e "${GREEN}✓ Test 1 passed!${NC}"
else
    echo -e "${RED}✗ Test 1 failed${NC}"
    exit 1
fi

echo ""

# Test 2: Determinism (call multiple times)
echo -e "${BLUE}[Test 2] Testing Determinism (3 calls)${NC}"
RESPONSE1=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/test/next")
sleep 0.5
RESPONSE2=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/test/next")
sleep 0.5
RESPONSE3=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/test/next")

if [ "$RESPONSE1" = "$RESPONSE2" ] && [ "$RESPONSE2" = "$RESPONSE3" ]; then
    echo -e "${GREEN}✓ Deterministic: All 3 calls returned identical results${NC}"
    
    RULE=$(echo "$RESPONSE1" | python3 -c "import json, sys; print(json.load(sys.stdin)['ruleApplied'])" 2>/dev/null)
    CONFIDENCE=$(echo "$RESPONSE1" | python3 -c "import json, sys; print(json.load(sys.stdin)['confidence'])" 2>/dev/null)
    echo "  Consistent Rule: $RULE"
    echo "  Consistent Confidence: $CONFIDENCE"
else
    echo -e "${RED}✗ Non-deterministic: Different results${NC}"
    exit 1
fi

echo ""

# Test 3: Different users
echo -e "${BLUE}[Test 3] Testing with different user IDs${NC}"
USER1_RESPONSE=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/user1/next")
USER2_RESPONSE=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/user2/next")

if [ -n "$USER1_RESPONSE" ] && [ -n "$USER2_RESPONSE" ]; then
    echo -e "${GREEN}✓ Service handles different users${NC}"
    USER1_RULE=$(echo "$USER1_RESPONSE" | python3 -c "import json, sys; print(json.load(sys.stdin)['ruleApplied'])" 2>/dev/null)
    USER2_RULE=$(echo "$USER2_RESPONSE" | python3 -c "import json, sys; print(json.load(sys.stdin)['ruleApplied'])" 2>/dev/null)
    echo "  User1 Rule: $USER1_RULE"
    echo "  User2 Rule: $USER2_RULE"
else
    echo -e "${RED}✗ Failed to get recommendations for different users${NC}"
    exit 1
fi

echo ""

# Test 4: Get all recommendations endpoint
echo -e "${BLUE}[Test 4] Testing GET /recommendations/users/{userId}${NC}"
ALL_RESPONSE=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/test")

if [ -n "$ALL_RESPONSE" ] && [ "$ALL_RESPONSE" != "null" ]; then
    echo -e "${GREEN}✓ GetAllRecommendations endpoint works${NC}"
    echo "$ALL_RESPONSE" | python3 -m json.tool 2>/dev/null | head -15 || echo "$ALL_RESPONSE"
else
    echo -e "${YELLOW}⚠ GetAllRecommendations returned empty or null${NC}"
fi

echo ""

echo -e "${GREEN}=========================================="
echo "✓ All Tests Passed!"
echo "==========================================${NC}"
echo ""
echo "Next steps:"
echo "  - Run full verification: ./verify-recommendation.sh"
echo "  - Test specific rules: ./test-rules.sh"
echo "  - Test priority: ./test-priority.sh"

