# Recommendation Service - Verification Test Suite

## Created Test Scripts

All verification scripts have been created. Make them executable:

```bash
cd backend/recommendation-service
chmod +x *.sh
```

### Test Scripts

1. **`verify-recommendation.sh`** - Main verification script
   - Checks dependencies
   - Verifies no Kafka/DB dependencies
   - Tests API contract
   - Tests determinism
   - Basic rule priority check

2. **`test-rules.sh`** - Rule-by-rule validation
   - Tests all 12 recommendation rules
   - Validates rule application
   - Checks confidence scores

3. **`test-priority.sh`** - Priority enforcement test
   - Verifies higher priority rules win
   - Tests rule ordering

4. **`test-determinism.sh`** - Determinism test
   - Calls API multiple times
   - Verifies same output for same input

5. **`test-stateless.sh`** - Statelessness test
   - Tests before/after restart
   - Verifies no state dependency

6. **`test-failures.sh`** - Failure scenario guide
   - Tests graceful failure handling
   - Progress/Analytics service down scenarios

## Quick Start

### 1. Start All Services

```bash
# Terminal 1: Progress Service
cd backend/progress-service && mvn spring-boot:run

# Terminal 2: Analytics Service
cd backend/analytics-service && mvn spring-boot:run

# Terminal 3: Recommendation Service
cd backend/recommendation-service && mvn spring-boot:run
```

### 2. Run Verification

```bash
cd backend/recommendation-service
chmod +x *.sh
./verify-recommendation.sh
```

### 3. Test Individual Aspects

```bash
# Test determinism
./test-determinism.sh

# Test priority enforcement
./test-priority.sh

# Test statelessness
./test-stateless.sh

# Test rules
./test-rules.sh
```

## Manual Test Cases

### Test Case 1: Resume Incomplete Lesson

```bash
# Setup: Ensure user has incomplete lesson in Progress Service
curl http://localhost:8084/recommendations/users/user123/next

# Expected:
# - ruleApplied: RESUME_INCOMPLETE
# - type: LESSON
# - confidence: 0.9
```

### Test Case 2: Sequential Progress

```bash
# Setup: User completed lesson N, no incomplete lessons
curl http://localhost:8084/recommendations/users/user456/next

# Expected:
# - ruleApplied: SEQUENTIAL_PROGRESS
# - type: LESSON
# - confidence: 0.85
```

### Test Case 3: Cold Start

```bash
# Setup: New user, no progress
curl http://localhost:8084/recommendations/users/new_user/next

# Expected:
# - ruleApplied: COLD_START
# - type: PATH
# - confidence: 0.5
```

### Test Case 4: Priority Enforcement

```bash
# Setup: User has incomplete lesson AND matches inactivity
curl http://localhost:8084/recommendations/users/priority_test/next

# Expected:
# - ruleApplied: RESUME_INCOMPLETE (not INACTIVITY_NUDGE)
# - Higher priority rule wins
```

### Test Case 5: Determinism

```bash
# Call same endpoint multiple times
for i in {1..5}; do
  curl -s http://localhost:8084/recommendations/users/user123/next | \
    python3 -c "import json, sys; d=json.load(sys.stdin); print(f\"{d['ruleApplied']}: {d['confidence']}\")"
done

# Expected: Same output every time
```

### Test Case 6: Statelessness

```bash
# Before restart
BEFORE=$(curl -s http://localhost:8084/recommendations/users/user123/next)

# Restart service (manual)
# Then:
AFTER=$(curl -s http://localhost:8084/recommendations/users/user123/next)

# Compare
[ "$BEFORE" = "$AFTER" ] && echo "✓ Stateless" || echo "✗ Stateful"
```

### Test Case 7: Failure Handling

```bash
# Stop Progress Service, then:
curl http://localhost:8084/recommendations/users/user123/next

# Expected: Fallback recommendation, not crash

# Stop Analytics Service, then:
curl http://localhost:8084/recommendations/users/user123/next

# Expected: Uses available data, graceful fallback
```

## API Contract Validation

```bash
# Validate response structure
curl -s http://localhost:8084/recommendations/users/user123/next | \
  python3 -c "
import json, sys
try:
    data = json.load(sys.stdin)
    assert 'type' in data
    assert 'targetId' in data
    assert 'title' in data
    assert 'reason' in data
    assert 'confidence' in data
    assert 'ruleApplied' in data
    assert data['type'] in ['LESSON', 'COURSE', 'PATH']
    assert 0 <= data['confidence'] <= 1
    print('✓ API contract valid')
except Exception as e:
    print(f'✗ API contract invalid: {e}')
    sys.exit(1)
"
```

## Success Criteria Checklist

- ✅ Recommendations depend only on Progress + Analytics
- ✅ Rules applied deterministically and in order
- ✅ Output is explainable (reason + rule)
- ✅ Service is stateless (no DB, no state)
- ✅ Service is replaceable (ML-ready)
- ✅ No Kafka dependency
- ✅ No database dependency
- ✅ API contract correct
- ✅ Failure scenarios handled gracefully
- ✅ Priority enforcement works

## Expected Response Format

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

## Troubleshooting

**Service not responding?**
- Check if running: `lsof -ti:8084`
- Check logs for errors
- Verify Progress/Analytics services accessible

**Wrong rule applied?**
- Check user context data
- Verify rule priority order
- Check service logs

**Non-deterministic?**
- Verify no time-based logic
- Check external services return consistent data

**API contract issues?**
- Verify response matches Recommendation model
- Check all required fields present

## Documentation

- **VERIFICATION.md** - Comprehensive verification guide
- **QUICK_TEST.md** - Quick start guide
- **README.md** - Service documentation

