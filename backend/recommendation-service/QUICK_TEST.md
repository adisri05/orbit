# Quick Test Guide - Recommendation Service

## Quick Verification (5 minutes)

### 1. Start All Services

```bash
# Terminal 1: Progress Service
cd backend/progress-service
mvn spring-boot:run

# Terminal 2: Analytics Service  
cd backend/analytics-service
mvn spring-boot:run

# Terminal 3: Recommendation Service
cd backend/recommendation-service
mvn spring-boot:run
```

### 2. Basic API Test

```bash
# Get recommendation
curl http://localhost:8084/recommendations/users/user123/next | python3 -m json.tool

# Expected: JSON with type, targetId, title, reason, confidence, ruleApplied
```

### 3. Determinism Test

```bash
# Call 3 times - should get same result
for i in {1..3}; do
  echo "Call $i:"
  curl -s http://localhost:8084/recommendations/users/user123/next | \
    python3 -c "import json, sys; d=json.load(sys.stdin); print(f\"Rule: {d['ruleApplied']}, Confidence: {d['confidence']}\")"
done
```

### 4. Verify No Dependencies

```bash
# Check no Kafka
grep -i kafka backend/recommendation-service/src/main/resources/application.yaml

# Check no database
grep -i "datasource\|jpa\|mongodb" backend/recommendation-service/src/main/resources/application.yaml
```

### 5. Test Failure Handling

```bash
# Stop Progress Service, then:
curl http://localhost:8084/recommendations/users/user123/next

# Should return fallback recommendation, not crash
```

## Expected Results

✅ **API Returns Valid JSON**
```json
{
  "type": "LESSON",
  "targetId": "...",
  "title": "...",
  "reason": "...",
  "confidence": 0.85,
  "ruleApplied": "SEQUENTIAL_PROGRESS"
}
```

✅ **Deterministic**: Same input = same output

✅ **Stateless**: Restart doesn't affect recommendations

✅ **No Dependencies**: No Kafka, no database

✅ **Graceful Failures**: Service handles missing dependencies

## Full Test Suite

```bash
cd backend/recommendation-service
./verify-recommendation.sh    # Basic verification
./test-determinism.sh         # Determinism test
./test-priority.sh           # Priority enforcement
./test-stateless.sh          # Statelessness test
./test-rules.sh              # Rule-by-rule tests
./test-failures.sh           # Failure scenarios
```

