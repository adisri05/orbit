#!/bin/bash

# Script to test Analytics Service API endpoints
# Usage: ./test-apis.sh

ANALYTICS_SERVICE_URL="http://localhost:8083"

echo "=========================================="
echo "Testing Analytics Service APIs"
echo "=========================================="

echo -e "\n[1] Testing GET /analytics/users/user123"
curl -s -X GET "$ANALYTICS_SERVICE_URL/analytics/users/user123" | python3 -m json.tool || echo "Failed or invalid JSON"
echo ""

echo -e "\n[2] Testing GET /analytics/courses/courseB"
curl -s -X GET "$ANALYTICS_SERVICE_URL/analytics/courses/courseB" | python3 -m json.tool || echo "Failed or invalid JSON"
echo ""

echo -e "\n[3] Testing GET /analytics/platform/overview"
curl -s -X GET "$ANALYTICS_SERVICE_URL/analytics/platform/overview" | python3 -m json.tool || echo "Failed or invalid JSON"
echo ""

echo "=========================================="
echo "API Testing Complete"
echo "=========================================="

