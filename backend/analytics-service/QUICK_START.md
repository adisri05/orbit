# Analytics Service - Quick Start Guide

## Quick Verification (5 minutes)

### 1. Start Services
```bash
# Terminal 1: Start Redis
redis-server

# Terminal 2: Start Analytics Service
cd backend/analytics-service
mvn spring-boot:run
```

### 2. Send Test Events

**Option A: Using kafka-console-producer**
```bash
# Send LESSON_STARTED
echo '{"eventType":"LESSON_STARTED","userId":"user123","pathId":"pathA","courseId":"courseB","lessonId":"lesson1","occurredAt":"2025-01-26T10:00:00Z"}' | \
  kafka-console-producer --bootstrap-server localhost:9092 --topic learning-events

# Send LESSON_COMPLETED
echo '{"eventType":"LESSON_COMPLETED","userId":"user123","pathId":"pathA","courseId":"courseB","lessonId":"lesson1","occurredAt":"2025-01-26T10:10:00Z"}' | \
  kafka-console-producer --bootstrap-server localhost:9092 --topic learning-events
```

**Option B: Using Python script**
```bash
cd backend/analytics-service
pip install kafka-python
python3 send-events.py
```

**Option C: Using provided shell script**
```bash
cd backend/analytics-service
./send-test-events.sh
```

### 3. Verify Results

**Check Redis:**
```bash
redis-cli GET "analytics:user:user123"
redis-cli GET "analytics:course:courseB"
redis-cli GET "analytics:platform"
```

**Check APIs:**
```bash
curl http://localhost:8083/analytics/users/user123
curl http://localhost:8083/analytics/courses/courseB
curl http://localhost:8083/analytics/platform/overview
```

**Check Logs:**
Look for in Analytics Service logs:
- `Consumed learning event: eventType=LESSON_STARTED, userId=user123...`
- `Consumed learning event: eventType=LESSON_COMPLETED, userId=user123...`

### 4. Run Full Verification
```bash
cd backend/analytics-service
./verify-analytics.sh
```

## Expected Results

After sending both events, you should see:

**User Analytics** (`/analytics/users/user123`):
```json
{
  "userId": "user123",
  "lessonsStartedCount": 1,
  "lessonsCompletedCount": 1,
  "lastActiveAt": "2025-01-26T10:10:00Z"
}
```

**Course Analytics** (`/analytics/courses/courseB`):
```json
{
  "courseId": "courseB",
  "totalLessonStarts": 1,
  "totalLessonCompletions": 1,
  "dropOffCount": 0
}
```

**Platform Analytics** (`/analytics/platform/overview`):
```json
{
  "totalEventsProcessed": 2,
  "totalLessonCompletions": 1
}
```

## Troubleshooting

- **Events not consumed?** Check Kafka is running and topic exists
- **Redis keys missing?** Check Redis is running and service logs for errors
- **API returns 404?** Verify service is running on port 8083
- **Metrics wrong?** Check event JSON format matches schema exactly

See `VERIFICATION.md` for detailed troubleshooting.

