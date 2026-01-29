#!/bin/bash

# Script to verify Redis keys and values for Analytics Service
# Usage: ./verify-redis.sh

REDIS_HOST="localhost"
REDIS_PORT="6379"

echo "=========================================="
echo "Verifying Redis Analytics Keys"
echo "=========================================="

echo -e "\n[1] Checking analytics:user:user123"
redis-cli -h $REDIS_HOST -p $REDIS_PORT GET "analytics:user:user123" | python3 -m json.tool 2>/dev/null || redis-cli -h $REDIS_HOST -p $REDIS_PORT GET "analytics:user:user123"
echo ""

echo -e "\n[2] Checking analytics:course:courseB"
redis-cli -h $REDIS_HOST -p $REDIS_PORT GET "analytics:course:courseB" | python3 -m json.tool 2>/dev/null || redis-cli -h $REDIS_HOST -p $REDIS_PORT GET "analytics:course:courseB"
echo ""

echo -e "\n[3] Checking analytics:platform"
redis-cli -h $REDIS_HOST -p $REDIS_PORT GET "analytics:platform" | python3 -m json.tool 2>/dev/null || redis-cli -h $REDIS_HOST -p $REDIS_PORT GET "analytics:platform"
echo ""

echo "=========================================="
echo "Redis Verification Complete"
echo "=========================================="

