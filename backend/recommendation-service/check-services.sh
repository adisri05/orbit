#!/bin/bash

# Quick service status checker

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "=========================================="
echo "Service Status Check"
echo "=========================================="
echo ""

# Check Progress Service
if lsof -ti:8082 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Progress Service is running on port 8082${NC}"
    curl -s http://localhost:8082/progress/users/test/paths/test > /dev/null 2>&1 && \
        echo "  API accessible: ✓" || echo "  API accessible: ✗"
else
    echo -e "${RED}✗ Progress Service is NOT running on port 8082${NC}"
    echo "  Start with: cd backend/progress-service && mvn spring-boot:run"
fi

# Check Analytics Service
if lsof -ti:8083 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Analytics Service is running on port 8083${NC}"
    curl -s http://localhost:8083/analytics/platform/overview > /dev/null 2>&1 && \
        echo "  API accessible: ✓" || echo "  API accessible: ✗"
else
    echo -e "${RED}✗ Analytics Service is NOT running on port 8083${NC}"
    echo "  Start with: cd backend/analytics-service && mvn spring-boot:run"
fi

# Check Recommendation Service
if lsof -ti:8084 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Recommendation Service is running on port 8084${NC}"
    curl -s http://localhost:8084/recommendations/users/test/next > /dev/null 2>&1 && \
        echo "  API accessible: ✓" || echo "  API accessible: ✗"
else
    echo -e "${RED}✗ Recommendation Service is NOT running on port 8084${NC}"
    echo "  Start with: cd backend/recommendation-service && mvn spring-boot:run"
fi

echo ""
echo "=========================================="
echo "Quick Test (works even without other services):"
echo "=========================================="
echo ""
echo "curl http://localhost:8084/recommendations/users/test/next"
echo ""

