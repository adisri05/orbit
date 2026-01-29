#!/bin/bash

# Analytics Service End-to-End Verification Script
# This script verifies Phase 8: Analytics Service

set -e

echo "=========================================="
echo "Analytics Service Verification"
echo "=========================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
KAFKA_BOOTSTRAP="localhost:9092"
TOPIC="learning-events"
REDIS_HOST="localhost"
REDIS_PORT="6379"
ANALYTICS_SERVICE_URL="http://localhost:8083"

# Check if Kafka is running
echo -e "\n${YELLOW}[1/6] Checking Kafka...${NC}"
if ! nc -z localhost 9092 2>/dev/null; then
    echo -e "${RED}✗ Kafka is not running on localhost:9092${NC}"
    echo "Please start Kafka first"
    exit 1
fi
echo -e "${GREEN}✓ Kafka is running${NC}"

# Check if Redis is running
echo -e "\n${YELLOW}[2/6] Checking Redis...${NC}"
if ! redis-cli -h $REDIS_HOST -p $REDIS_PORT ping > /dev/null 2>&1; then
    echo -e "${RED}✗ Redis is not running on $REDIS_HOST:$REDIS_PORT${NC}"
    echo "Please start Redis first"
    exit 1
fi
echo -e "${GREEN}✓ Redis is running${NC}"

# Check if Analytics Service is running
echo -e "\n${YELLOW}[3/6] Checking Analytics Service...${NC}"
if ! curl -s "$ANALYTICS_SERVICE_URL/analytics/platform/overview" > /dev/null 2>&1; then
    echo -e "${RED}✗ Analytics Service is not running on $ANALYTICS_SERVICE_URL${NC}"
    echo "Please start the Analytics Service first"
    exit 1
fi
echo -e "${GREEN}✓ Analytics Service is running${NC}"

# Clear existing analytics data for clean test
echo -e "\n${YELLOW}[4/6] Clearing existing analytics data...${NC}"
redis-cli -h $REDIS_HOST -p $REDIS_PORT DEL "analytics:user:user123" "analytics:course:courseB" "analytics:platform" > /dev/null 2>&1 || true
echo -e "${GREEN}✓ Cleared test data${NC}"

# Send LESSON_STARTED event
echo -e "\n${YELLOW}[5/6] Sending LESSON_STARTED event to Kafka...${NC}"
LESSON_STARTED='{
  "eventType": "LESSON_STARTED",
  "userId": "user123",
  "pathId": "pathA",
  "courseId": "courseB",
  "lessonId": "lesson1",
  "occurredAt": "2025-01-26T10:00:00Z"
}'

echo "$LESSON_STARTED" | kafka-console-producer --bootstrap-server $KAFKA_BOOTSTRAP --topic $TOPIC 2>/dev/null || {
    echo -e "${RED}✗ Failed to send event. Make sure kafka-console-producer is available${NC}"
    echo "Alternative: Use kafkacat or another Kafka producer tool"
    exit 1
}
echo -e "${GREEN}✓ LESSON_STARTED event sent${NC}"

# Wait for processing
echo "Waiting 2 seconds for event processing..."
sleep 2

# Send LESSON_COMPLETED event
echo -e "\n${YELLOW}Sending LESSON_COMPLETED event to Kafka...${NC}"
LESSON_COMPLETED='{
  "eventType": "LESSON_COMPLETED",
  "userId": "user123",
  "pathId": "pathA",
  "courseId": "courseB",
  "lessonId": "lesson1",
  "occurredAt": "2025-01-26T10:10:00Z"
}'

echo "$LESSON_COMPLETED" | kafka-console-producer --bootstrap-server $KAFKA_BOOTSTRAP --topic $TOPIC 2>/dev/null || {
    echo -e "${RED}✗ Failed to send event${NC}"
    exit 1
}
echo -e "${GREEN}✓ LESSON_COMPLETED event sent${NC}"

# Wait for processing
echo "Waiting 2 seconds for event processing..."
sleep 2

# Verify Redis keys
echo -e "\n${YELLOW}[6/6] Verifying Redis keys...${NC}"

# Check user analytics
USER_KEY="analytics:user:user123"
USER_DATA=$(redis-cli -h $REDIS_HOST -p $REDIS_PORT GET "$USER_KEY" 2>/dev/null)
if [ -z "$USER_DATA" ]; then
    echo -e "${RED}✗ User analytics key not found: $USER_KEY${NC}"
    exit 1
fi
echo -e "${GREEN}✓ User analytics key exists${NC}"
echo "User Analytics: $USER_DATA"

# Check course analytics
COURSE_KEY="analytics:course:courseB"
COURSE_DATA=$(redis-cli -h $REDIS_HOST -p $REDIS_PORT GET "$COURSE_KEY" 2>/dev/null)
if [ -z "$COURSE_DATA" ]; then
    echo -e "${RED}✗ Course analytics key not found: $COURSE_KEY${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Course analytics key exists${NC}"
echo "Course Analytics: $COURSE_DATA"

# Check platform analytics
PLATFORM_KEY="analytics:platform"
PLATFORM_DATA=$(redis-cli -h $REDIS_HOST -p $REDIS_PORT GET "$PLATFORM_KEY" 2>/dev/null)
if [ -z "$PLATFORM_DATA" ]; then
    echo -e "${RED}✗ Platform analytics key not found: $PLATFORM_KEY${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Platform analytics key exists${NC}"
echo "Platform Analytics: $PLATFORM_DATA"

# Verify API endpoints
echo -e "\n${YELLOW}[API Verification] Testing REST endpoints...${NC}"

# Test user analytics API
echo -e "\nTesting GET /analytics/users/user123"
USER_RESPONSE=$(curl -s -w "\n%{http_code}" "$ANALYTICS_SERVICE_URL/analytics/users/user123")
HTTP_CODE=$(echo "$USER_RESPONSE" | tail -n1)
BODY=$(echo "$USER_RESPONSE" | head -n-1)
if [ "$HTTP_CODE" != "200" ]; then
    echo -e "${RED}✗ User analytics API returned HTTP $HTTP_CODE${NC}"
    exit 1
fi
echo -e "${GREEN}✓ User analytics API returned HTTP 200${NC}"
echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"

# Test course analytics API
echo -e "\nTesting GET /analytics/courses/courseB"
COURSE_RESPONSE=$(curl -s -w "\n%{http_code}" "$ANALYTICS_SERVICE_URL/analytics/courses/courseB")
HTTP_CODE=$(echo "$COURSE_RESPONSE" | tail -n1)
BODY=$(echo "$COURSE_RESPONSE" | head -n-1)
if [ "$HTTP_CODE" != "200" ]; then
    echo -e "${RED}✗ Course analytics API returned HTTP $HTTP_CODE${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Course analytics API returned HTTP 200${NC}"
echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"

# Test platform analytics API
echo -e "\nTesting GET /analytics/platform/overview"
PLATFORM_RESPONSE=$(curl -s -w "\n%{http_code}" "$ANALYTICS_SERVICE_URL/analytics/platform/overview")
HTTP_CODE=$(echo "$PLATFORM_RESPONSE" | tail -n1)
BODY=$(echo "$PLATFORM_RESPONSE" | head -n-1)
if [ "$HTTP_CODE" != "200" ]; then
    echo -e "${RED}✗ Platform analytics API returned HTTP $HTTP_CODE${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Platform analytics API returned HTTP 200${NC}"
echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"

# Verify metrics
echo -e "\n${YELLOW}[Metrics Verification] Checking analytics values...${NC}"

# Parse JSON and verify values (using Python for JSON parsing)
VERIFY_SCRIPT=$(cat <<'PYTHON_SCRIPT'
import json
import sys

user_data = json.loads(sys.argv[1])
course_data = json.loads(sys.argv[2])
platform_data = json.loads(sys.argv[3])

errors = []

# Verify user analytics
if user_data.get('lessonsStartedCount', 0) < 1:
    errors.append("User lessonsStartedCount should be >= 1")
if user_data.get('lessonsCompletedCount', 0) < 1:
    errors.append("User lessonsCompletedCount should be >= 1")
if not user_data.get('lastActiveAt'):
    errors.append("User lastActiveAt should be set")

# Verify course analytics
if course_data.get('totalLessonStarts', 0) < 1:
    errors.append("Course totalLessonStarts should be >= 1")
if course_data.get('totalLessonCompletions', 0) < 1:
    errors.append("Course totalLessonCompletions should be >= 1")
if course_data.get('dropOffCount', -1) < 0:
    errors.append("Course dropOffCount should be >= 0")

# Verify platform analytics
if platform_data.get('totalEventsProcessed', 0) < 2:
    errors.append("Platform totalEventsProcessed should be >= 2")
if platform_data.get('totalLessonCompletions', 0) < 1:
    errors.append("Platform totalLessonCompletions should be >= 1")

if errors:
    print("\n".join(errors))
    sys.exit(1)
else:
    print("✓ All metrics verified successfully")
PYTHON_SCRIPT
)

VERIFY_RESULT=$(echo "$VERIFY_SCRIPT" | python3 - "$BODY" "$(echo "$COURSE_RESPONSE" | head -n-1)" "$(echo "$PLATFORM_RESPONSE" | head -n-1)" 2>&1)
if [ $? -ne 0 ]; then
    echo -e "${RED}✗ Metrics verification failed:${NC}"
    echo "$VERIFY_RESULT"
    exit 1
fi
echo -e "${GREEN}$VERIFY_RESULT${NC}"

echo -e "\n${GREEN}=========================================="
echo "✓ All verifications passed!"
echo "==========================================${NC}"

