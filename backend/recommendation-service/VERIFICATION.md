# Recommendation Service Verification Guide

This guide provides comprehensive verification steps for Phase 9: Recommendation Service.

## Prerequisites

1. **Progress Service** running on `localhost:8082`
2. **Analytics Service** running on `localhost:8083`
3. **Recommendation Service** running on `localhost:8084`

## 1. Dependency Readiness Check

### Verify Services Are Running

```bash
# Check Progress Service
curl http://localhost:8082/progress/users/test/paths/test

# Check Analytics Service
curl http://localhost:8083/analytics/platform/overview

# Check Recommendation Service
curl http://localhost:8084/recommendations/users/test/next
```

### Verify No Kafka/Database Dependencies

```bash
# Check application.yaml
grep -i "kafka\|datasource\|jpa\|mongodb" \
  backend/recommendation-service/src/main/resources/application.yaml

# Should return nothing (no matches)
```

### Verify REST-Only Dependencies

```bash
# Check that service only uses WebClient (no Kafka/DB)
grep -r "Kafka\|@Entity\|@Document\|@Repository" \
  backend/recommendation-service/src/main/java/ \
  | grep -v ".class" || echo "✓ No Kafka/DB dependencies found"
```

## 2. Rule-by-Rule Validation

### Test Case 1: Resume Incomplete Lesson

**Setup:**
```bash
# Ensure Progress Service has data for user with incomplete lesson
# This requires Progress Service to track started but incomplete lessons
```

**Action:**
```bash
curl http://localhost:8084/recommendations/users/user123/next
```

**Expected Response:**
```json
{
  "type": "LESSON",
  "targetId": "lesson-id-that-was-started",
  "title": "Resume incomplete lesson",
  "reason": "You started this lesson but haven't completed it yet. Let's finish what you started!",
  "confidence": 0.9,
  "ruleApplied": "RESUME_INCOMPLETE"
}
```

**Validation:**
- ✅ `ruleApplied` = `RESUME_INCOMPLETE`
- ✅ `type` = `LESSON`
- ✅ `confidence` >= 0.9
- ✅ `reason` explains resuming incomplete lesson

### Test Case 2: Sequential Progress

**Setup:**
- User completed lesson N
- No incomplete lessons exist
- Active course has more lessons

**Action:**
```bash
curl http://localhost:8084/recommendations/users/user456/next
```

**Expected:**
- ✅ `ruleApplied` = `SEQUENTIAL_PROGRESS`
- ✅ `targetId` = lesson N+1
- ✅ `confidence` >= 0.85

### Test Case 3: Path Continuation

**Setup:**
- User completed all lessons in a course
- Another course exists in same path

**Action:**
```bash
curl http://localhost:8084/recommendations/users/user789/next
```

**Expected:**
- ✅ `ruleApplied` = `PATH_CONTINUATION`
- ✅ `type` = `COURSE`
- ✅ `confidence` >= 0.8

### Test Case 4: Inactivity Nudge

**Setup:**
- Analytics reports user inactive >7 days

**Action:**
```bash
curl http://localhost:8084/recommendations/users/inactive_user/next
```

**Expected:**
- ✅ `ruleApplied` = `INACTIVITY_NUDGE`
- ✅ Recommendation targets shortest pending lesson
- ✅ `confidence` >= 0.75

### Test Case 5: Drop-Off Avoidance

**Setup:**
- Analytics reports high drop-off rate (>30%) for current course

**Action:**
```bash
curl http://localhost:8084/recommendations/users/user_dropoff/next?courseId=high-dropoff-course
```

**Expected:**
- ✅ `ruleApplied` = `DROPOFF_AVOIDANCE`
- ✅ Alternative lesson or course recommended
- ✅ `confidence` >= 0.7

### Test Case 6: Cold Start

**Setup:**
- New user
- No progress data

**Action:**
```bash
curl http://localhost:8084/recommendations/users/new_user/next
```

**Expected:**
- ✅ `ruleApplied` = `COLD_START`
- ✅ Popular path or first lesson recommended
- ✅ `confidence` >= 0.5

## 3. Priority Enforcement Test

**Setup:**
- User has:
  - An incomplete lesson (matches RESUME_INCOMPLETE)
  - Also matches inactivity rule (inactive >7 days)

**Action:**
```bash
curl http://localhost:8084/recommendations/users/priority_test_user/next
```

**Expected:**
- ✅ `ruleApplied` = `RESUME_INCOMPLETE` (higher priority)
- ✅ `INACTIVITY_NUDGE` does NOT apply
- ✅ Higher priority rule wins

**Validation:**
```bash
./test-priority.sh
```

## 4. Determinism Test

**Action:**
```bash
# Call API multiple times with same input
for i in {1..5}; do
  curl http://localhost:8084/recommendations/users/user123/next
  echo ""
done
```

**Expected:**
- ✅ Same recommendation every time
- ✅ Same `ruleApplied`
- ✅ Same `confidence`
- ✅ Same `targetId`

**Validation:**
```bash
./test-determinism.sh
```

## 5. Statelessness Test

**Action:**
1. Get recommendation: `curl http://localhost:8084/recommendations/users/user123/next`
2. Restart Recommendation Service
3. Get recommendation again: `curl http://localhost:8084/recommendations/users/user123/next`

**Expected:**
- ✅ Recommendation unchanged
- ✅ No data loss
- ✅ No rebuild step required
- ✅ Service immediately functional after restart

**Validation:**
```bash
./test-stateless.sh
```

## 6. API Contract Validation

**Verify Response Structure:**
```bash
curl http://localhost:8084/recommendations/users/user123/next | python3 -m json.tool
```

**Required Fields:**
- ✅ `type`: "LESSON" | "COURSE" | "PATH"
- ✅ `targetId`: string
- ✅ `title`: string
- ✅ `reason`: string (human-readable explanation)
- ✅ `confidence`: number (0.0-1.0)
- ✅ `ruleApplied`: string (one of 12 rule types)

**Validation:**
```bash
# Validate structure
curl -s http://localhost:8084/recommendations/users/user123/next | \
  python3 -c "
import json, sys
data = json.load(sys.stdin)
assert 'type' in data and data['type'] in ['LESSON', 'COURSE', 'PATH']
assert 'targetId' in data and isinstance(data['targetId'], str)
assert 'title' in data and isinstance(data['title'], str)
assert 'reason' in data and isinstance(data['reason'], str)
assert 'confidence' in data and 0 <= data['confidence'] <= 1
assert 'ruleApplied' in data
print('✓ API contract valid')
"
```

**Ensure:**
- ✅ No internal data leaked
- ✅ No progress or analytics mutation
- ✅ Only recommendation data exposed

## 7. Failure Scenario Checks

### Progress Service Down

**Action:**
1. Stop Progress Service
2. Call: `curl http://localhost:8084/recommendations/users/user123/next`

**Expected:**
- ✅ Graceful failure or fallback
- ✅ No 500 error
- ✅ Service continues to function
- ✅ May return COLD_START or SAFE_DEFAULT recommendation

### Analytics Service Down

**Action:**
1. Stop Analytics Service
2. Call: `curl http://localhost:8084/recommendations/users/user123/next`

**Expected:**
- ✅ Graceful fallback using available data
- ✅ No 500 error
- ✅ Service continues to function
- ✅ Uses progress data only

### Both Services Down

**Action:**
1. Stop both Progress and Analytics Services
2. Call: `curl http://localhost:8084/recommendations/users/user123/next`

**Expected:**
- ✅ Fallback recommendation (COLD_START or SAFE_DEFAULT)
- ✅ No crash
- ✅ Service remains responsive

**Validation:**
```bash
./test-failures.sh
```

## Quick Verification Script

Run the comprehensive verification script:

```bash
cd backend/recommendation-service
chmod +x *.sh
./verify-recommendation.sh
```

## Success Criteria Checklist

- ✅ Recommendations depend only on Progress + Analytics services
- ✅ Rules are applied deterministically and in order
- ✅ Output is explainable (includes reason and rule)
- ✅ Service is stateless (no database, no state storage)
- ✅ Service is replaceable (designed for ML replacement)
- ✅ No Kafka dependency exists
- ✅ No database is used
- ✅ API contract is correct
- ✅ Failure scenarios handled gracefully
- ✅ Priority enforcement works correctly

## Troubleshooting

### Service Not Responding
- Check if service is running: `lsof -ti:8084`
- Check logs for errors
- Verify Progress and Analytics services are accessible

### Wrong Rule Applied
- Check user context data in Progress/Analytics services
- Verify rule priority order in `RecommendationEngine.java`
- Check logs for rule evaluation

### Non-Deterministic Results
- Verify no time-based logic in rules
- Check that external services return consistent data
- Ensure no random or non-deterministic operations

### API Contract Violations
- Verify response structure matches `Recommendation` model
- Check for any internal data leakage
- Ensure all required fields are present

## Notes

- The service is **stateless** - no data persistence
- Recommendations are **computed on-demand** - no caching
- Rules are **deterministic** - same input = same output
- Service is **replaceable** - designed for ML replacement
- All recommendations are **explainable** - includes reason and rule

