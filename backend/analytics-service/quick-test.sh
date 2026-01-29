#!/bin/bash
# Quick test - sends events and shows results

echo "🚀 Sending test events to Kafka..."
echo '{"eventType":"LESSON_STARTED","userId":"user123","pathId":"pathA","courseId":"courseB","lessonId":"lesson1","occurredAt":"2025-01-26T10:00:00Z"}' | \
  kafka-console-producer --bootstrap-server localhost:9092 --topic learning-events

sleep 2

echo '{"eventType":"LESSON_COMPLETED","userId":"user123","pathId":"pathA","courseId":"courseB","lessonId":"lesson1","occurredAt":"2025-01-26T10:10:00Z"}' | \
  kafka-console-producer --bootstrap-server localhost:9092 --topic learning-events

echo "✅ Events sent! Waiting 3 seconds..."
sleep 3

echo ""
echo "📊 Redis Data:"
echo "=============="
echo ""
echo "User Analytics:"
redis-cli GET "analytics:user:user123" | python3 -m json.tool 2>/dev/null || redis-cli GET "analytics:user:user123"
echo ""
echo "Course Analytics:"
redis-cli GET "analytics:course:courseB" | python3 -m json.tool 2>/dev/null || redis-cli GET "analytics:course:courseB"
echo ""
echo "Platform Analytics:"
redis-cli GET "analytics:platform" | python3 -m json.tool 2>/dev/null || redis-cli GET "analytics:platform"
echo ""
echo "🌐 API Responses:"
echo "================"
echo ""
echo "GET /analytics/users/user123:"
curl -s http://localhost:8083/analytics/users/user123 | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8083/analytics/users/user123
echo ""
echo "GET /analytics/courses/courseB:"
curl -s http://localhost:8083/analytics/courses/courseB | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8083/analytics/courses/courseB
echo ""
echo "GET /analytics/platform/overview:"
curl -s http://localhost:8083/analytics/platform/overview | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8083/analytics/platform/overview
echo ""

