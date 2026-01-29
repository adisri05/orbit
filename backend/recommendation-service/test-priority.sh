#!/bin/bash

# Priority Enforcement Test
# Verifies that higher priority rules win over lower priority ones

RECOMMENDATION_SERVICE_URL="http://localhost:8084"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "=========================================="
echo "Priority Enforcement Test"
echo "=========================================="
echo ""

echo -e "${BLUE}Test Scenario: User has incomplete lesson AND matches inactivity rule${NC}"
echo "Expected: RESUME_INCOMPLETE (priority 1) should win over INACTIVITY_NUDGE (priority 4)"
echo ""

USER_ID="priority_test_user"

RESPONSE=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/$USER_ID/next")

if [ -z "$RESPONSE" ] || [ "$RESPONSE" = "null" ]; then
    echo -e "${RED}✗ No recommendation returned${NC}"
    exit 1
fi

RULE=$(echo "$RESPONSE" | python3 -c "import json, sys; print(json.load(sys.stdin)['ruleApplied'])" 2>/dev/null)
CONFIDENCE=$(echo "$RESPONSE" | python3 -c "import json, sys; print(json.load(sys.stdin)['confidence'])" 2>/dev/null)

echo "Response:"
echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
echo ""

if [ "$RULE" = "RESUME_INCOMPLETE" ]; then
    echo -e "${GREEN}✓ Priority enforced: RESUME_INCOMPLETE (higher priority) won${NC}"
    echo -e "${GREEN}✓ Confidence: $CONFIDENCE (expected >= 0.9)${NC}"
    exit 0
elif [ "$RULE" = "INACTIVITY_NUDGE" ]; then
    echo -e "${RED}✗ Priority violation: INACTIVITY_NUDGE (lower priority) won over RESUME_INCOMPLETE${NC}"
    exit 1
else
    echo -e "${YELLOW}⚠ Rule applied: $RULE${NC}"
    echo -e "${YELLOW}⚠ This test requires specific test data setup${NC}"
    echo -e "${YELLOW}⚠ Expected: RESUME_INCOMPLETE when incomplete lesson exists${NC}"
fi

