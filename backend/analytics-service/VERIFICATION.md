# Analytics Service Verification Guide

This guide provides step-by-step instructions to verify Phase 8: Analytics Service end-to-end.

## Prerequisites

1. **Kafka** running on `localhost:9092`
2. **Redis** running on `localhost:6379`
3. **Analytics Service** running on `localhost:8083`

## Verification Steps

### Step 1: Start Required Services

```bash
# Start Kafka (if not already running)
# Start Redis (if not already running)
redis-server

# Start Analytics Service
cd backend/analytics-service
mvn spring-boot:run
```

### Step 2: Send Test Events to Kafka

#### Option A: Using the provided script
```bash
cd backend/analytics-service
chmod +x send-test-events.sh
./send-test-events.sh
```

#### Option B: Using kafka-console-producer manually
```bash
# Send LESSON_STARTED event
cat <<EOF | kafka-console-producer --bootstrap-server localhost:9092 --topic learning-events
{
  "eventType": "LESSON_STARTED",
  "userId": "user123",
  "pathId": "pathA",
  "courseId": "courseB",
  "lessonId": "lesson1",
  "occurredAt": "2025-01-26T10:00:00Z"
}
EOF

# Send LESSON_COMPLETED event
cat <<EOF | kafka-console-producer --bootstrap-server localhost:9092 --topic learning-events
{
  "eventType": "LESSON_COMPLETED",
  "userId": "user123",
  "pathId": "pathA",
  "courseId": "courseB",
  "lessonId": "lesson1",
  "occurredAt": "2025-01-26T10:10:00Z"
}
EOF
```

#### Option C: Using kafkacat
```bash
# Send LESSON_STARTED event
echo '{"eventType":"LESSON_STARTED","userId":"user123","pathId":"pathA","courseId":"courseB","lessonId":"lesson1","occurredAt":"2025-01-26T10:00:00Z"}' | \
  kafkacat -b localhost:9092 -t learning-events -P

# Send LESSON_COMPLETED event
echo '{"eventType":"LESSON_COMPLETED","userId":"user123","pathId":"pathA","courseId":"courseB","lessonId":"lesson1","occurredAt":"2025-01-26T10:10:00Z"}' | \
  kafkacat -b localhost:9092 -t learning-events -P
```

### Step 3: Verify Service Logs

Check the Analytics Service logs for:
- `Consumed learning event: ...`
- `Analytics updated` (implicitly through successful processing)

### Step 4: Verify Redis Keys

#### Option A: Using the provided script
```bash
cd backend/analytics-service
chmod +x verify-redis.sh
./verify-redis.sh
```

#### Option B: Manual verification
```bash
# Check user analytics
redis-cli GET "analytics:user:user123"

# Check course analytics
redis-cli GET "analytics:course:courseB"

# Check platform analytics
redis-cli GET "analytics:platform"
```

**Expected Results:**
- `analytics:user:user123` should contain:
  - `lessonsStartedCount`: 1
  - `lessonsCompletedCount`: 1
  - `lastActiveAt`: "2025-01-26T10:10:00Z"

- `analytics:course:courseB` should contain:
  - `totalLessonStarts`: 1
  - `totalLessonCompletions`: 1
  - `dropOffCount`: 0 (since start = completion)

- `analytics:platform` should contain:
  - `totalEventsProcessed`: 2
  - `totalLessonCompletions`: 1

### Step 5: Test API Endpoints

#### Option A: Using the provided script
```bash
cd backend/analytics-service
chmod +x test-apis.sh
./test-apis.sh
```

#### Option B: Manual API testing
```bash
# Test user analytics API
curl -X GET http://localhost:8083/analytics/users/user123

# Test course analytics API
curl -X GET http://localhost:8083/analytics/courses/courseB

# Test platform analytics API
curl -X GET http://localhost:8083/analytics/platform/overview
```

**Expected Results:**
- All endpoints should return HTTP 200
- JSON responses with aggregated analytics data

### Step 6: Replay Safety Test

1. **Stop the Analytics Service**
   ```bash
   # Find and kill the process
   pkill -f analytics-service
   ```

2. **Clear Redis analytics data** (optional, to test full rebuild)
   ```bash
   redis-cli DEL "analytics:user:user123" "analytics:course:courseB" "analytics:platform"
   ```

3. **Restart Analytics Service**
   ```bash
   cd backend/analytics-service
   mvn spring-boot:run
   ```

4. **Verify Kafka consumer group offset**
   The service should consume from `earliest` offset (configured in `application.yaml`), so it will reprocess all events.

5. **Wait for events to be reprocessed** (check logs)

6. **Verify Redis keys again** - analytics should be rebuilt correctly

### Step 7: Run Complete Verification Script

For automated end-to-end verification:

```bash
cd backend/analytics-service
chmod +x verify-analytics.sh
./verify-analytics.sh
```

This script will:
1. Check if Kafka, Redis, and Analytics Service are running
2. Clear existing test data
3. Send test events
4. Verify Redis keys
5. Test all API endpoints
6. Verify metrics values

## Success Criteria

✅ **Kafka Verification**
- Events are consumed successfully
- Service logs show event processing

✅ **Redis Validation**
- All three keys exist: `analytics:user:user123`, `analytics:course:courseB`, `analytics:platform`
- Metrics increment correctly:
  - `lessonsStartedCount` increments on LESSON_STARTED
  - `lessonsCompletedCount` increments on LESSON_COMPLETED
  - Platform totals update correctly

✅ **API Verification**
- `GET /analytics/users/user123` returns HTTP 200 with user analytics
- `GET /analytics/courses/courseB` returns HTTP 200 with course analytics
- `GET /analytics/platform/overview` returns HTTP 200 with platform analytics

✅ **Replay Safety**
- Service can be restarted and rebuilds analytics from Kafka
- Analytics are correctly recalculated from event stream

## Troubleshooting

### Events not being consumed
- Check Kafka is running: `kafka-topics --bootstrap-server localhost:9092 --list`
- Check topic exists: `kafka-topics --bootstrap-server localhost:9092 --describe --topic learning-events`
- Check service logs for errors
- Verify consumer group: `kafka-consumer-groups --bootstrap-server localhost:9092 --group analytics-service-group --describe`

### Redis keys not found
- Check Redis is running: `redis-cli ping`
- Check service logs for Redis connection errors
- Verify events were actually consumed (check service logs)

### API returns 404
- Verify service is running on port 8083
- Check service logs for startup errors
- Verify the endpoint paths match: `/analytics/users/{userId}`, `/analytics/courses/{courseId}`, `/analytics/platform/overview`

### Metrics not incrementing
- Verify events are being sent to Kafka correctly
- Check service logs for processing errors
- Ensure event JSON format matches the LearningEvent schema exactly

## Notes

- The service treats events as immutable facts
- No idempotency enforcement - duplicate events will increment counters
- No ordering validation - events are processed as received
- Service is fully decoupled from Progress Service
- Analytics update only via Kafka events (no direct API writes)

