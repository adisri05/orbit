# Phase 9: Recommendation Service - Step-by-Step Verification Guide

## Prerequisites
- Java 17+ installed
- Maven installed
- Kafka running (for Progress/Analytics services)
- Redis running (for Progress/Analytics services)

---

## Step 1: Start Progress Service

**Open Terminal 1:**

```bash
cd /Users/aditisrivastava/Desktop/orbit/backend/progress-service
mvn spring-boot:run
```

**Wait for:** `Started ProgressServiceApplication`

**Keep this terminal open!**

---

## Step 2: Start Analytics Service

**Open Terminal 2 (new terminal):**

```bash
cd /Users/aditisrivastava/Desktop/orbit/backend/analytics-service
mvn spring-boot:run
```

**Wait for:** `Started AnalyticsServiceApplication`

**Keep this terminal open!**

---

## Step 3: Start Recommendation Service

**Open Terminal 3 (new terminal):**

```bash
cd /Users/aditisrivastava/Desktop/orbit/backend/recommendation-service
mvn spring-boot:run
```

**Wait for:** `Started RecommendationServiceApplication`

**Keep this terminal open!**

---

## Step 4: Verify All Services Are Running

**Open Terminal 4 (new terminal):**

```bash
cd /Users/aditisrivastava/Desktop/orbit/backend/recommendation-service
chmod +x check-services.sh
./check-services.sh
```

**Expected output:**
```
✓ Progress Service is running on port 8082
✓ Analytics Service is running on port 8083
✓ Recommendation Service is running on port 8084
```

---

## Step 5: Quick API Test

**In Terminal 4:**

```bash
curl http://localhost:8084/recommendations/users/test/next | python3 -m json.tool
```

**Expected:** JSON response with:
- `type`: "LESSON", "COURSE", or "PATH"
- `targetId`: string
- `title`: string
- `reason`: string
- `confidence`: number (0.0-1.0)
- `ruleApplied`: one of the 12 rule types

**Example:**
```json
{
    "type": "LESSON",
    "targetId": "starter-lesson-1",
    "title": "Start learning",
    "reason": "Begin your learning journey with this starter lesson.",
    "confidence": 0.3,
    "ruleApplied": "COLD_START"
}
```

---

## Step 6: Test Determinism

**In Terminal 4:**

```bash
# Call the API 3 times - should get same result
curl -s http://localhost:8084/recommendations/users/test/next | python3 -c "import json, sys; d=json.load(sys.stdin); print(f\"Rule: {d['ruleApplied']}, Confidence: {d['confidence']}\")"

sleep 1

curl -s http://localhost:8084/recommendations/users/test/next | python3 -c "import json, sys; d=json.load(sys.stdin); print(f\"Rule: {d['ruleApplied']}, Confidence: {d['confidence']}\")"

sleep 1

curl -s http://localhost:8084/recommendations/users/test/next | python3 -c "import json, sys; d=json.load(sys.stdin); print(f\"Rule: {d['ruleApplied']}, Confidence: {d['confidence']}\")"
```

**Expected:** All 3 calls return identical `ruleApplied` and `confidence` values.

---

## Step 7: Run Full Verification Script

**In Terminal 4:**

```bash
cd /Users/aditisrivastava/Desktop/orbit/backend/recommendation-service
chmod +x verify-recommendation.sh
./verify-recommendation.sh
```

**This will verify:**
- ✓ All dependencies are accessible
- ✓ No Kafka/DB dependencies
- ✓ API contract is valid
- ✓ Determinism works
- ✓ Rule priority

---

## Step 8: Test Different Scenarios

**In Terminal 4:**

### Test with different users:
```bash
curl http://localhost:8084/recommendations/users/user1/next | python3 -m json.tool
curl http://localhost:8084/recommendations/users/user2/next | python3 -m json.tool
```

### Test with context (course/path):
```bash
curl "http://localhost:8084/recommendations/users/test/next?courseId=courseB&pathId=pathA" | python3 -m json.tool
```

### Test getAllRecommendations endpoint:
```bash
curl http://localhost:8084/recommendations/users/test | python3 -m json.tool
```

---

## Step 9: Verify No Dependencies on Kafka/DB

**In Terminal 4:**

```bash
# Check no Kafka config
grep -i kafka backend/recommendation-service/src/main/resources/application.yaml

# Check no database config
grep -i "datasource\|jpa\|mongodb" backend/recommendation-service/src/main/resources/application.yaml
```

**Expected:** No matches (empty output)

---

## Step 10: Test Failure Handling (Optional)

**Stop Progress Service** (Ctrl+C in Terminal 1), then:

```bash
curl http://localhost:8084/recommendations/users/test/next | python3 -m json.tool
```

**Expected:** Should still return a recommendation (fallback rule), not crash.

**Restart Progress Service** after testing.

---

## Success Criteria Checklist

- ✅ All 3 services start successfully
- ✅ Recommendation API returns valid JSON
- ✅ Response has all required fields (type, targetId, title, reason, confidence, ruleApplied)
- ✅ Same input produces same output (deterministic)
- ✅ Service handles missing users gracefully
- ✅ No Kafka dependency
- ✅ No database dependency
- ✅ Service is stateless (restart doesn't affect recommendations)

---

## Troubleshooting

**Service won't start?**
- Check if port is in use: `lsof -ti:8082` (or 8083, 8084)
- Check Java version: `java --version` (should be 17+)
- Check Maven: `mvn --version`

**500 Error?**
- Check service logs in Terminal 3
- Verify Progress/Analytics services are running
- Make sure you restarted Recommendation Service after the error handling fixes

**No response?**
- Verify service is running: `curl http://localhost:8084/recommendations/users/test/next`
- Check service logs for errors
- Verify all services are accessible

---

## Quick Reference

**Service Ports:**
- Progress Service: `http://localhost:8082`
- Analytics Service: `http://localhost:8083`
- Recommendation Service: `http://localhost:8084`

**Key Endpoints:**
- `GET /recommendations/users/{userId}/next` - Get next recommendation
- `GET /recommendations/users/{userId}` - Get all recommendations
- `GET /recommendations/users/{userId}/next?courseId=X&pathId=Y` - With context

**Test Commands:**
```bash
# Quick test
curl http://localhost:8084/recommendations/users/test/next | python3 -m json.tool

# Check services
./check-services.sh

# Full verification
./verify-recommendation.sh
```

---

## Summary

1. ✅ Start Progress Service (Terminal 1)
2. ✅ Start Analytics Service (Terminal 2)
3. ✅ Start Recommendation Service (Terminal 3)
4. ✅ Verify services running
5. ✅ Test API endpoint
6. ✅ Test determinism
7. ✅ Run full verification
8. ✅ Test different scenarios
9. ✅ Verify no Kafka/DB dependencies
10. ✅ Test failure handling

**If all steps pass, Phase 9 is complete! 🎉**

