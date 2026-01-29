#!/bin/bash

# Statelessness Test
# Verifies that service restart doesn't affect recommendations

RECOMMENDATION_SERVICE_URL="http://localhost:8084"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "=========================================="
echo "Statelessness Test"
echo "=========================================="
echo ""

USER_ID="stateless_test_user"

echo -e "${BLUE}Step 1: Get recommendation before restart${NC}"
BEFORE_RESTART=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/$USER_ID/next")
echo "$BEFORE_RESTART" | python3 -m json.tool 2>/dev/null || echo "$BEFORE_RESTART"
echo ""

echo -e "${YELLOW}⚠ Manual Step Required:${NC}"
echo "1. Stop Recommendation Service"
echo "2. Restart Recommendation Service"
echo "3. Press Enter to continue..."
read -p ""

echo -e "${BLUE}Step 2: Get recommendation after restart${NC}"
AFTER_RESTART=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/$USER_ID/next")
echo "$AFTER_RESTART" | python3 -m json.tool 2>/dev/null || echo "$AFTER_RESTART"
echo ""

if [ "$BEFORE_RESTART" = "$AFTER_RESTART" ]; then
    echo -e "${GREEN}✓ Stateless: Same recommendation before and after restart${NC}"
    echo -e "${GREEN}✓ No data loss or rebuild required${NC}"
    exit 0
else
    echo -e "${RED}✗ Stateful: Different recommendations before and after restart${NC}"
    echo "Before: $BEFORE_RESTART"
    echo "After: $AFTER_RESTART"
    exit 1
fi

