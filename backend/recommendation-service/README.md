# Recommendation Service

Phase 9: Recommendation Service - A stateless decision engine that generates deterministic, explainable learning recommendations.

## Overview

The Recommendation Service:
- Makes decisions based on user progress and analytics
- Stores no state (pure computation)
- Emits no events
- Uses read-only REST calls to Progress and Analytics services
- Implements 12 recommendation rules in priority order
- Provides explainable recommendations

## Architecture

- **Service Type**: Stateless Decision Engine
- **Port**: 8084
- **Data Sources**: 
  - Progress Service (read-only REST)
  - Analytics Service (read-only REST)
- **No Kafka**: No producer, no consumer
- **No Database**: Pure computation, no persistence

## Recommendation Rules (Priority Order)

### 🟢 CONTINUITY RULES (Highest Priority)

1. **RESUME_INCOMPLETE** (confidence: 0.9)
   - If user has started but not completed a lesson
   - Recommend resuming that lesson

2. **SEQUENTIAL_PROGRESS** (confidence: 0.85)
   - If user completed lesson N in a course
   - Recommend lesson N+1

3. **PATH_CONTINUATION** (confidence: 0.8)
   - If user completed all lessons in a course
   - Recommend next course in the path

### 🟢 ENGAGEMENT & BEHAVIOR RULES

4. **INACTIVITY_NUDGE** (confidence: 0.75)
   - If user inactive for >7 days
   - Recommend shortest pending lesson

5. **CONSISTENCY_REINFORCEMENT** (confidence: 0.8)
   - If user is consistently active
   - Recommend next lesson in most active course

6. **BINGE_CONTROL** (confidence: 0.65)
   - If user completes many lessons in short time
   - Recommend lighter or revision content

### 🟢 QUALITY & SAFETY RULES

7. **DROPOFF_AVOIDANCE** (confidence: 0.7)
   - If course drop-off rate is high (>30%)
   - Recommend alternative course or prerequisite

8. **PREREQUISITE_REINFORCEMENT** (confidence: 0.78)
   - If user frequently starts but doesn't complete advanced lessons
   - Recommend prerequisite lessons

### 🟢 EXPLORATION & VARIETY RULES

9. **EXPLORATION_BOOST** (confidence: 0.6)
   - If user completes a major milestone
   - Recommend related learning path

10. **SKILL_DIVERSIFICATION** (confidence: 0.55)
    - If user stays on single path too long
    - Recommend complementary path

### 🟢 FALLBACK RULES (Lowest Priority)

11. **COLD_START** (confidence: 0.5)
    - If new user with no events
    - Recommend first lesson of popular path

12. **SAFE_DEFAULT** (confidence: 0.4)
    - If no other rule matches
    - Recommend next lesson from most recently active path

## Recommendation Model

```json
{
  "type": "LESSON" | "COURSE" | "PATH",
  "targetId": "string",
  "title": "string",
  "reason": "human-readable explanation",
  "confidence": 0.0-1.0,
  "ruleApplied": "RULE_TYPE"
}
```

## APIs

### GET /recommendations/users/{userId}/next
Get the next recommendation for a user.

**Query Parameters:**
- `courseId` (optional): Filter by course context
- `pathId` (optional): Filter by path context

**Response:** `Recommendation`

### GET /recommendations/users/{userId}
Get all recommendations for a user (currently returns single next recommendation).

**Response:** `List<Recommendation>`

## Constraints

✅ **No authentication logic**  
✅ **No Kafka usage**  
✅ **No database**  
✅ **No progress mutation**  
✅ **No analytics mutation**  
✅ **No UI assumptions**  
✅ **Stateless computation only**

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Progress Service running on `localhost:8082`
- Analytics Service running on `localhost:8083`

### Build
```bash
cd backend/recommendation-service
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

### Configuration

Edit `src/main/resources/application.yaml`:
```yaml
services:
  progress:
    base-url: http://localhost:8082
  analytics:
    base-url: http://localhost:8083
```

## Example Usage

```bash
# Get next recommendation
curl http://localhost:8084/recommendations/users/user123/next

# Get next recommendation with context
curl http://localhost:8084/recommendations/users/user123/next?courseId=courseB&pathId=pathA

# Get all recommendations
curl http://localhost:8084/recommendations/users/user123
```

## Example Response

```json
{
  "type": "LESSON",
  "targetId": "courseB-lesson-3",
  "title": "Continue with next lesson",
  "reason": "You've completed 2 lessons. Continue with lesson 3 in this course.",
  "confidence": 0.85,
  "ruleApplied": "SEQUENTIAL_PROGRESS"
}
```

## Project Structure

```
recommendation-service/
├── src/main/java/com/orbit/recommendation/
│   ├── RecommendationServiceApplication.java
│   ├── config/
│   │   └── WebClientConfig.java
│   ├── controller/
│   │   └── RecommendationController.java
│   ├── model/
│   │   ├── Recommendation.java
│   │   ├── RecommendationType.java
│   │   ├── RuleType.java
│   │   ├── UserContext.java
│   │   ├── ProgressData.java
│   │   └── AnalyticsData.java
│   ├── client/
│   │   ├── ProgressServiceClient.java
│   │   └── AnalyticsServiceClient.java
│   └── service/
│       ├── ContextAggregationService.java
│       ├── RecommendationEngine.java
│       └── RecommendationService.java
└── pom.xml
```

## Design Principles

1. **Deterministic**: Same input always produces same output
2. **Explainable**: Every recommendation includes reason and rule
3. **Prioritized**: Rules evaluated in strict priority order
4. **Stateless**: No state storage, pure computation
5. **Replaceable**: Designed to be replaced by ML in future

## Success Criteria

✅ Recommendation logic is deterministic  
✅ Rules are explicit and ordered  
✅ Output is explainable  
✅ Service is replaceable by ML  
✅ Clearly answers: "What should the learner do next — and why?"

## License

Part of the ORBIT platform.

