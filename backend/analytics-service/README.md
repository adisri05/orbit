# Analytics Service

Phase 8: Analytics Service - A Spring Boot microservice that derives insights and trends from learning events.

## Overview

The Analytics Service observes learning behavior over time and provides aggregated insights. It:
- Consumes events from Kafka (`learning-events` topic)
- Aggregates metrics at user, course, and platform levels
- Stores aggregated analytics in Redis
- Exposes read-only REST APIs for insights
- Is replay-safe and rebuildable from Kafka

## Architecture

- **Service Type**: Kafka Consumer
- **Topic**: `learning-events`
- **Storage**: Redis (aggregated analytics only)
- **Port**: 8083
- **Consumer Group**: `analytics-service-group`

## Features

### Event Consumption
- Consumes `LESSON_STARTED` events
- Consumes `LESSON_COMPLETED` events
- Processes events as immutable facts (no idempotency enforcement)
- No ordering validation

### Analytics Metrics

**User-level Analytics:**
- `lessonsStartedCount` - Total lessons started by user
- `lessonsCompletedCount` - Total lessons completed by user
- `lastActiveAt` - Last activity timestamp

**Course-level Analytics:**
- `totalLessonStarts` - Total lesson starts in course
- `totalLessonCompletions` - Total lesson completions in course
- `dropOffCount` - Lessons started but not completed

**Platform-level Analytics:**
- `totalEventsProcessed` - Total events processed
- `totalLessonCompletions` - Total completions across platform

### Storage Design

Redis keys:
- `analytics:user:{userId}` - User analytics
- `analytics:course:{courseId}` - Course analytics
- `analytics:platform` - Platform analytics

Only aggregated data is stored, not raw events.

### REST APIs

All endpoints are read-only (GET only):

- `GET /analytics/users/{userId}` - Get user analytics
- `GET /analytics/courses/{courseId}` - Get course analytics
- `GET /analytics/platform/overview` - Get platform overview

## Constraints

✅ **No authentication logic**  
✅ **No progress computation**  
✅ **No recommendation logic**  
✅ **No Kafka producer**  
✅ **No raw event storage**  
✅ **Fully decoupled from Progress Service**

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Kafka running on `localhost:9092`
- Redis running on `localhost:6379`

### Build
```bash
cd backend/analytics-service
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

Or:
```bash
java -jar target/analytics-service-0.0.1-SNAPSHOT.jar
```

### Configuration

Edit `src/main/resources/application.yaml`:
- Kafka bootstrap servers
- Redis host/port
- Server port
- Logging levels

## Verification

See [VERIFICATION.md](VERIFICATION.md) for detailed verification steps.

Quick verification:
```bash
# Send test events
./send-test-events.sh

# Verify Redis
./verify-redis.sh

# Test APIs
./test-apis.sh

# Full verification
./verify-analytics.sh
```

## Project Structure

```
analytics-service/
├── src/main/java/com/orbit/analytics/
│   ├── AnalyticsServiceApplication.java
│   ├── config/
│   │   ├── KafkaConsumerConfig.java
│   │   └── RedisConfig.java
│   ├── consumer/
│   │   └── LearningEventConsumer.java
│   ├── controller/
│   │   └── AnalyticsController.java
│   ├── event/
│   │   ├── LearningEvent.java
│   │   └── LearningEventType.java
│   ├── model/
│   │   ├── CourseAnalytics.java
│   │   ├── PlatformAnalytics.java
│   │   └── UserAnalytics.java
│   └── service/
│       └── AnalyticsService.java
├── src/main/resources/
│   └── application.yaml
└── pom.xml
```

## Event Schema

```json
{
  "eventType": "LESSON_STARTED" | "LESSON_COMPLETED",
  "userId": "string",
  "pathId": "string",
  "courseId": "string",
  "lessonId": "string",
  "occurredAt": "ISO-8601 timestamp"
}
```

## Replay Safety

The service is designed to be replay-safe:
- Consumer group configured with `auto-offset-reset: earliest`
- Events treated as immutable facts
- Aggregates recalculated on each event
- Can rebuild analytics from Kafka event stream

To test replay safety:
1. Stop the service
2. Clear Redis analytics keys (optional)
3. Restart the service
4. Service will reprocess events from earliest offset
5. Analytics will be rebuilt correctly

## Logging

Log levels (configurable in `application.yaml`):
- `com.orbit.analytics: DEBUG` - Detailed service logs
- `org.springframework.kafka: INFO` - Kafka consumer logs
- `org.springframework.data.redis: INFO` - Redis connection logs

## Success Criteria

✅ Service consumes events from Kafka  
✅ Analytics update only via Kafka events  
✅ APIs expose insights only (read-only)  
✅ Service is independent and rebuildable  
✅ No coupling with Progress Service  
✅ Replay-safe from Kafka event stream

## License

Part of the ORBIT platform.

