#!/bin/bash

# Interactive test script for Analytics Service
# Run this to test the service step by step

set -e

echo "=========================================="
echo "Analytics Service - Interactive Test"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
KAFKA_BOOTSTRAP="localhost:9092"
TOPIC="learning-events"
REDIS_HOST="localhost"
REDIS_PORT="6379"
ANALYTICS_URL="http://localhost:8083"

# Step 1: Check Kafka
echo -e "${BLUE}[Step 1] Checking Kafka...${NC}"
if nc -z localhost 9092 2>/dev/null; then
    echo -e "${GREEN}✓ Kafka is running${NC}"
else
    echo -e "${RED}✗ Kafka is NOT running on localhost:9092${NC}"
    echo "Please start Kafka first:"
    echo "  brew services start kafka"
    echo "  OR start your Kafka server"
    exit 1
fi
echo ""

# Step 2: Check Redis
echo -e "${BLUE}[Step 2] Checking Redis...${NC}"
if redis-cli -h $REDIS_HOST -p $REDIS_PORT ping > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Redis is running${NC}"
else
    echo -e "${RED}✗ Redis is NOT running on $REDIS_HOST:$REDIS_PORT${NC}"
    echo "Please start Redis first:"
    echo "  redis-server"
    echo "  OR brew services start redis"
    exit 1
fi
echo ""

# Step 3: Check Analytics Service
echo -e "${BLUE}[Step 3] Checking Analytics Service...${NC}"
if curl -s "$ANALYTICS_URL/analytics/platform/overview" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Analytics Service is running${NC}"
else
    echo -e "${YELLOW}⚠ Analytics Service is NOT running${NC}"
    echo ""
    echo "Please start it in another terminal:"
    echo "  cd backend/analytics-service"
    echo "  mvn spring-boot:run"
    echo ""
    read -p "Press Enter when the service is running..."
    
    # Check again
    if ! curl -s "$ANALYTICS_URL/analytics/platform/overview" > /dev/null 2>&1; then
        echo -e "${RED}✗ Service still not accessible${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Analytics Service is now running${NC}"
fi
echo ""

# Step 4: Clear old test data
echo -e "${BLUE}[Step 4] Clearing old test data...${NC}"
redis-cli -h $REDIS_HOST -p $REDIS_PORT DEL "analytics:user:user123" "analytics:course:courseB" "analytics:platform" > /dev/null 2>&1 || true
echo -e "${GREEN}✓ Cleared test data${NC}"
echo ""

# Step 5: Send LESSON_STARTED event
echo -e "${BLUE}[Step 5] Sending LESSON_STARTED event...${NC}"
LESSON_STARTED='{"eventType":"LESSON_STARTED","userId":"user123","pathId":"pathA","courseId":"courseB","lessonId":"lesson1","occurredAt":"2025-01-26T10:00:00Z"}'

echo "$LESSON_STARTED" | kafka-console-producer --bootstrap-server $KAFKA_BOOTSTRAP --topic $TOPIC 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ LESSON_STARTED event sent${NC}"
else
    echo -e "${RED}✗ Failed to send event${NC}"
    exit 1
fi
echo "Waiting 2 seconds for processing..."
sleep 2
echo ""

# Step 6: Send LESSON_COMPLETED event
echo -e "${BLUE}[Step 6] Sending LESSON_COMPLETED event...${NC}"
LESSON_COMPLETED='{"eventType":"LESSON_COMPLETED","userId":"user123","pathId":"pathA","courseId":"courseB","lessonId":"lesson1","occurredAt":"2025-01-26T10:10:00Z"}'

echo "$LESSON_COMPLETED" | kafka-console-producer --bootstrap-server $KAFKA_BOOTSTRAP --topic $TOPIC 2>&1
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ LESSON_COMPLETED event sent${NC}"
else
    echo -e "${RED}✗ Failed to send event${NC}"
    exit 1
fi
echo "Waiting 2 seconds for processing..."
sleep 2
echo ""

# Step 7: Verify Redis
echo -e "${BLUE}[Step 7] Verifying Redis keys...${NC}"
echo ""

echo "User Analytics (analytics:user:user123):"
USER_DATA=$(redis-cli -h $REDIS_HOST -p $REDIS_PORT GET "analytics:user:user123" 2>/dev/null)
if [ -z "$USER_DATA" ]; then
    echo -e "${RED}✗ Key not found${NC}"
else
    echo -e "${GREEN}✓ Key found${NC}"
    echo "$USER_DATA" | python3 -m json.tool 2>/dev/null || echo "$USER_DATA"
fi
echo ""

echo "Course Analytics (analytics:course:courseB):"
COURSE_DATA=$(redis-cli -h $REDIS_HOST -p $REDIS_PORT GET "analytics:course:courseB" 2>/dev/null)
if [ -z "$COURSE_DATA" ]; then
    echo -e "${RED}✗ Key not found${NC}"
else
    echo -e "${GREEN}✓ Key found${NC}"
    echo "$COURSE_DATA" | python3 -m json.tool 2>/dev/null || echo "$COURSE_DATA"
fi
echo ""

echo "Platform Analytics (analytics:platform):"
PLATFORM_DATA=$(redis-cli -h $REDIS_HOST -p $REDIS_PORT GET "analytics:platform" 2>/dev/null)
if [ -z "$PLATFORM_DATA" ]; then
    echo -e "${RED}✗ Key not found${NC}"
else
    echo -e "${GREEN}✓ Key found${NC}"
    echo "$PLATFORM_DATA" | python3 -m json.tool 2>/dev/null || echo "$PLATFORM_DATA"
fi
echo ""

# Step 8: Test APIs
echo -e "${BLUE}[Step 8] Testing REST APIs...${NC}"
echo ""

echo "GET /analytics/users/user123:"
USER_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" "$ANALYTICS_URL/analytics/users/user123")
HTTP_CODE=$(echo "$USER_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$USER_RESPONSE" | grep -v "HTTP_CODE")
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ HTTP 200${NC}"
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
else
    echo -e "${RED}✗ HTTP $HTTP_CODE${NC}"
    echo "$BODY"
fi
echo ""

echo "GET /analytics/courses/courseB:"
COURSE_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" "$ANALYTICS_URL/analytics/courses/courseB")
HTTP_CODE=$(echo "$COURSE_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$COURSE_RESPONSE" | grep -v "HTTP_CODE")
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ HTTP 200${NC}"
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
else
    echo -e "${RED}✗ HTTP $HTTP_CODE${NC}"
    echo "$BODY"
fi
echo ""

echo "GET /analytics/platform/overview:"
PLATFORM_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" "$ANALYTICS_URL/analytics/platform/overview")
HTTP_CODE=$(echo "$PLATFORM_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$PLATFORM_RESPONSE" | grep -v "HTTP_CODE")
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ HTTP 200${NC}"
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
else
    echo -e "${RED}✗ HTTP $HTTP_CODE${NC}"
    echo "$BODY"
fi
echo ""

# Summary
echo -e "${GREEN}=========================================="
echo "✓ Test Complete!"
echo "==========================================${NC}"
echo ""
echo "Check the Analytics Service logs to see:"
echo "  - 'Consumed learning event: eventType=...'"
echo "  - Event processing messages"
echo ""

