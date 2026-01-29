# Test Analytics Service - Copy & Paste Commands

## Quick Test (Run these commands in order)

### Step 1: Start Analytics Service (Terminal 1)
```bash
cd /Users/aditisrivastava/Desktop/orbit/backend/analytics-service
mvn spring-boot:run
```

Wait until you see: `Started AnalyticsServiceApplication`

### Step 2: Send Test Events (Terminal 2)
```bash
# Send LESSON_STARTED event
echo '{"eventType":"LESSON_STARTED","userId":"user123","pathId":"pathA","courseId":"courseB","lessonId":"lesson1","occurredAt":"2025-01-26T10:00:00Z"}' | \
  kafka-console-producer --bootstrap-server localhost:9092 --topic learning-events

# Wait 2 seconds, then send LESSON_COMPLETED event
sleep 2
echo '{"eventType":"LESSON_COMPLETED","userId":"user123","pathId":"pathA","courseId":"courseB","lessonId":"lesson1","occurredAt":"2025-01-26T10:10:00Z"}' | \
  kafka-console-producer --bootstrap-server localhost:9092 --topic learning-events
```

### Step 3: Check Analytics Service Logs (Terminal 1)
You should see:
```
Consumed learning event: eventType=LESSON_STARTED, userId=user123, courseId=courseB, lessonId=lesson1
Consumed learning event: eventType=LESSON_COMPLETED, userId=user123, courseId=courseB, lessonId=lesson1
```

### Step 4: Verify Redis (Terminal 2)
```bash
# Check user analytics
redis-cli GET "analytics:user:user123"

# Check course analytics  
redis-cli GET "analytics:course:courseB"

# Check platform analytics
redis-cli GET "analytics:platform"
```

Expected output (formatted):
```json
// User Analytics
{
  "userId": "user123",
  "lessonsStartedCount": 1,
  "lessonsCompletedCount": 1,
  "lastActiveAt": "2025-01-26T10:10:00Z"
}

// Course Analytics
{
  "courseId": "courseB",
  "totalLessonStarts": 1,
  "totalLessonCompletions": 1,
  "dropOffCount": 0
}

// Platform Analytics
{
  "totalEventsProcessed": 2,
  "totalLessonCompletions": 1
}
```

### Step 5: Test REST APIs (Terminal 2)
```bash
# Test user analytics API
curl http://localhost:8083/analytics/users/user123 | python3 -m json.tool

# Test course analytics API
curl http://localhost:8083/analytics/courses/courseB | python3 -m json.tool

# Test platform analytics API
curl http://localhost:8083/analytics/platform/overview | python3 -m json.tool
```

All should return HTTP 200 with JSON data.

## Or Use the Interactive Test Script

```bash
# Terminal 1: Start service
cd /Users/aditisrivastava/Desktop/orbit/backend/analytics-service
mvn spring-boot:run

# Terminal 2: Run test script
cd /Users/aditisrivastava/Desktop/orbit/backend/analytics-service
./test-interactive.sh
```

## Troubleshooting

**Service won't start?**
- Check Kafka is running: `lsof -ti:9092`
- Check Redis is running: `redis-cli ping`
- Check port 8083 is free: `lsof -ti:8083`

**Events not consumed?**
- Check Kafka topic exists: `kafka-topics --bootstrap-server localhost:9092 --list | grep learning-events`
- Check service logs for errors
- Verify JSON format is correct (no extra spaces, valid JSON)

**Redis keys missing?**
- Wait a few seconds after sending events
- Check service logs for Redis connection errors
- Verify events were actually consumed (check logs)

**API returns 404?**
- Verify service is running: `curl http://localhost:8083/analytics/platform/overview`
- Check service logs for startup errors

