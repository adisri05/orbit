# Starting Services for Recommendation Service Verification

## Quick Start - Start All Required Services

### Option 1: Start Services in Separate Terminals

**Terminal 1 - Progress Service:**
```bash
cd /Users/aditisrivastava/Desktop/orbit/backend/progress-service
mvn spring-boot:run
```
Wait for: `Started ProgressServiceApplication`

**Terminal 2 - Analytics Service:**
```bash
cd /Users/aditisrivastava/Desktop/orbit/backend/analytics-service
mvn spring-boot:run
```
Wait for: `Started AnalyticsServiceApplication`

**Terminal 3 - Recommendation Service:**
```bash
cd /Users/aditisrivastava/Desktop/orbit/backend/recommendation-service
mvn spring-boot:run
```
Wait for: `Started RecommendationServiceApplication`

**Terminal 4 - Run Verification:**
```bash
cd /Users/aditisrivastava/Desktop/orbit/backend/recommendation-service
./verify-recommendation.sh
```

### Option 2: Check Service Status

```bash
# Check if services are running
lsof -ti:8082 && echo "✓ Progress Service (8082)" || echo "✗ Progress Service not running"
lsof -ti:8083 && echo "✓ Analytics Service (8083)" || echo "✗ Analytics Service not running"
lsof -ti:8084 && echo "✓ Recommendation Service (8084)" || echo "✗ Recommendation Service not running"
```

### Option 3: Quick Test Without Full Setup

If you just want to test the Recommendation Service API directly:

```bash
# Test with minimal setup (will use fallback rules)
curl http://localhost:8084/recommendations/users/test/next | python3 -m json.tool
```

This should return a recommendation even if Progress/Analytics services are down (using COLD_START or SAFE_DEFAULT rules).

## Service Ports

- **Progress Service**: `http://localhost:8082`
- **Analytics Service**: `http://localhost:8083`
- **Recommendation Service**: `http://localhost:8084`

## Verification Order

1. ✅ Start Progress Service (port 8082)
2. ✅ Start Analytics Service (port 8083)
3. ✅ Start Recommendation Service (port 8084)
4. ✅ Run verification script

## Troubleshooting

**Service won't start?**
- Check if port is already in use: `lsof -ti:8082`
- Check Maven is installed: `mvn --version`
- Check Java is installed: `java --version`

**Service starts but verification fails?**
- Wait a few seconds for service to fully initialize
- Check service logs for errors
- Verify service is accessible: `curl http://localhost:8082/actuator/health`

**Can't connect to service?**
- Verify service is running: `lsof -ti:8082`
- Check firewall/network settings
- Verify service started successfully (check logs)

