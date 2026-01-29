#!/bin/bash

# Determinism Test
# Verifies that same input produces same output

RECOMMENDATION_SERVICE_URL="http://localhost:8084"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "=========================================="
echo "Determinism Test"
echo "=========================================="
echo ""

USER_ID="determinism_test_user"
ITERATIONS=5

echo -e "${BLUE}Testing: Same user ID, $ITERATIONS iterations${NC}"
echo "Expected: Same recommendation every time"
echo ""

RESULTS=()
for i in $(seq 1 $ITERATIONS); do
    RESPONSE=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/$USER_ID/next")
    RESULTS+=("$RESPONSE")
    echo "Iteration $i:"
    echo "$RESPONSE" | python3 -m json.tool 2>/dev/null | head -10 || echo "$RESPONSE"
    echo ""
    sleep 0.5
done

# Check if all results are identical
FIRST_RESULT="${RESULTS[0]}"
ALL_SAME=true

for i in $(seq 1 $((ITERATIONS-1))); do
    if [ "${RESULTS[$i]}" != "$FIRST_RESULT" ]; then
        ALL_SAME=false
        echo -e "${RED}✗ Iteration $((i+1)) differs from first${NC}"
        echo "First: $FIRST_RESULT"
        echo "Different: ${RESULTS[$i]}"
    fi
done

if [ "$ALL_SAME" = true ]; then
    echo -e "${GREEN}✓ Deterministic: All $ITERATIONS iterations produced identical results${NC}"
    
    # Extract key fields for verification
    RULE=$(echo "$FIRST_RESULT" | python3 -c "import json, sys; print(json.load(sys.stdin)['ruleApplied'])" 2>/dev/null)
    CONFIDENCE=$(echo "$FIRST_RESULT" | python3 -c "import json, sys; print(json.load(sys.stdin)['confidence'])" 2>/dev/null)
    TARGET_ID=$(echo "$FIRST_RESULT" | python3 -c "import json, sys; print(json.load(sys.stdin)['targetId'])" 2>/dev/null)
    
    echo ""
    echo "Consistent values:"
    echo "  Rule: $RULE"
    echo "  Confidence: $CONFIDENCE"
    echo "  Target ID: $TARGET_ID"
    exit 0
else
    echo -e "${RED}✗ Non-deterministic: Different results for same input${NC}"
    exit 1
fi

