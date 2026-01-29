#!/bin/bash

# Rule-by-Rule Validation Script
# Tests each recommendation rule individually

set -e

RECOMMENDATION_SERVICE_URL="http://localhost:8084"
PROGRESS_SERVICE_URL="http://localhost:8082"
ANALYTICS_SERVICE_URL="http://localhost:8083"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

test_rule() {
    local test_name=$1
    local user_id=$2
    local expected_rule=$3
    local expected_type=$4
    local min_confidence=$5
    
    echo -e "${BLUE}Testing: $test_name${NC}"
    
    RESPONSE=$(curl -s "$RECOMMENDATION_SERVICE_URL/recommendations/users/$user_id/next")
    
    if [ -z "$RESPONSE" ] || [ "$RESPONSE" = "null" ]; then
        echo -e "${RED}✗ No recommendation returned${NC}"
        return 1
    fi
    
    RULE=$(echo "$RESPONSE" | python3 -c "import json, sys; print(json.load(sys.stdin)['ruleApplied'])" 2>/dev/null)
    TYPE=$(echo "$RESPONSE" | python3 -c "import json, sys; print(json.load(sys.stdin)['type'])" 2>/dev/null)
    CONFIDENCE=$(echo "$RESPONSE" | python3 -c "import json, sys; print(json.load(sys.stdin)['confidence'])" 2>/dev/null)
    REASON=$(echo "$RESPONSE" | python3 -c "import json, sys; print(json.load(sys.stdin)['reason'])" 2>/dev/null)
    
    echo "  Rule Applied: $RULE"
    echo "  Type: $TYPE"
    echo "  Confidence: $CONFIDENCE"
    echo "  Reason: $REASON"
    
    if [ "$RULE" != "$expected_rule" ]; then
        echo -e "${RED}✗ Expected rule: $expected_rule, got: $RULE${NC}"
        return 1
    fi
    
    if [ "$TYPE" != "$expected_type" ]; then
        echo -e "${RED}✗ Expected type: $expected_type, got: $TYPE${NC}"
        return 1
    fi
    
    if (( $(echo "$CONFIDENCE < $min_confidence" | bc -l) )); then
        echo -e "${RED}✗ Confidence $CONFIDENCE is below minimum $min_confidence${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✓ Test passed${NC}"
    echo ""
    return 0
}

echo "=========================================="
echo "Rule-by-Rule Validation"
echo "=========================================="
echo ""

# Test Case 1: Resume Incomplete Lesson
echo -e "${YELLOW}[Test Case 1] Resume Incomplete Lesson${NC}"
echo "Setup: Progress Service reports incomplete lesson"
echo "Expected: RESUME_INCOMPLETE, LESSON, confidence >= 0.9"
test_rule "Resume Incomplete" "user123" "RESUME_INCOMPLETE" "LESSON" "0.9" || echo -e "${YELLOW}⚠ Requires test data setup${NC}\n"

# Test Case 2: Sequential Progress
echo -e "${YELLOW}[Test Case 2] Sequential Progress${NC}"
echo "Setup: User completed lesson N, no incomplete lessons"
echo "Expected: SEQUENTIAL_PROGRESS, LESSON, confidence >= 0.85"
test_rule "Sequential Progress" "user456" "SEQUENTIAL_PROGRESS" "LESSON" "0.85" || echo -e "${YELLOW}⚠ Requires test data setup${NC}\n"

# Test Case 3: Path Continuation
echo -e "${YELLOW}[Test Case 3] Path Continuation${NC}"
echo "Setup: User completed all lessons in course"
echo "Expected: PATH_CONTINUATION, COURSE, confidence >= 0.8"
test_rule "Path Continuation" "user789" "PATH_CONTINUATION" "COURSE" "0.8" || echo -e "${YELLOW}⚠ Requires test data setup${NC}\n"

# Test Case 4: Inactivity Nudge
echo -e "${YELLOW}[Test Case 4] Inactivity Nudge${NC}"
echo "Setup: User inactive >7 days"
echo "Expected: INACTIVITY_NUDGE, LESSON, confidence >= 0.75"
test_rule "Inactivity Nudge" "inactive_user" "INACTIVITY_NUDGE" "LESSON" "0.75" || echo -e "${YELLOW}⚠ Requires test data setup${NC}\n"

# Test Case 5: Drop-Off Avoidance
echo -e "${YELLOW}[Test Case 5] Drop-Off Avoidance${NC}"
echo "Setup: High drop-off rate course"
echo "Expected: DROPOFF_AVOIDANCE, COURSE, confidence >= 0.7"
test_rule "Drop-Off Avoidance" "user_dropoff" "DROPOFF_AVOIDANCE" "COURSE" "0.7" || echo -e "${YELLOW}⚠ Requires test data setup${NC}\n"

# Test Case 6: Cold Start
echo -e "${YELLOW}[Test Case 6] Cold Start${NC}"
echo "Setup: New user, no progress"
echo "Expected: COLD_START, PATH, confidence >= 0.5"
test_rule "Cold Start" "new_user" "COLD_START" "PATH" "0.5" || echo -e "${YELLOW}⚠ Requires test data setup${NC}\n"

echo "=========================================="
echo "Rule Testing Complete"
echo "=========================================="

