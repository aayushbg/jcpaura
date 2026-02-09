# Quick Start Guide - JCP Aura Backend

## 5-Minute Setup

### 1. Build the Project
```bash
cd A:\jcpaura
mvn clean package -DskipTests
```

Expected output: `BUILD SUCCESS`

### 2. Start the Application
```bash
java -jar target/jcpaura-0.0.1-SNAPSHOT.jar
```

You should see:
```
... : Started JcpauraApplication in X seconds (JVM running for X seconds)
```

### 3. Verify it's Running
Open a new terminal and run:
```bash
curl http://localhost:8081/api/aura-metrics/all
```

Expected response:
```json
{
  "success": true,
  "count": 0,
  "data": []
}
```

---

## Creating Your First Metric

```bash
curl -X POST http://localhost:8081/api/aura-metrics/create \
  -H "Content-Type: application/json" \
  -d '{
    "active_users": 100,
    "availability_pct": 99.9,
    "avg_latency_ms": 25.5,
    "circle": "NORTH",
    "error_rate_pct": 0.1,
    "health_status": "HEALTHY",
    "kpi_health_score": 98.5,
    "kpi_timestamp": "2026-02-09T12:00:00",
    "packet_loss_pct": 0.05,
    "service_type": "API",
    "site_id": "SITE001",
    "throughput_mbps": 1000.0
  }'
```

Save the returned `_id` value.

---

## Your First Query

### Query by Circle
```bash
curl -X POST http://localhost:8081/api/aura-metrics/query \
  -H "Content-Type: application/json" \
  -d '{
    "type": "query",
    "data": "{ \"circle\": \"NORTH\" }"
  }'
```

### Query with Conditions
```bash
curl -X POST http://localhost:8081/api/aura-metrics/query \
  -H "Content-Type: application/json" \
  -d '{
    "type": "query",
    "data": "{ \"health_status\": \"HEALTHY\", \"availability_pct\": { \"$gt\": 99 } }"
  }'
```

---

## Your First Aggregation

Count metrics by service type and show average health score:

```bash
curl -X POST http://localhost:8081/api/aura-metrics/query \
  -H "Content-Type: application/json" \
  -d '{
    "type": "aggregation",
    "data": "[{\"$group\": {\"_id\": \"$service_type\", \"count\": {\"$sum\": 1}, \"avg_health\": {\"$avg\": \"$kpi_health_score\"}}}]"
  }'
```

---

## Common Operations

### Get All Metrics
```bash
curl http://localhost:8081/api/aura-metrics/all
```

### Get Single Metric by ID
```bash
curl http://localhost:8081/api/aura-metrics/{ID}
```

### Update Metric
```bash
curl -X PUT http://localhost:8081/api/aura-metrics/{ID} \
  -H "Content-Type: application/json" \
  -d '{"health_status": "DEGRADED"}'
```

### Delete Metric
```bash
curl -X DELETE http://localhost:8081/api/aura-metrics/{ID}
```

---

## Useful MongoDB Query Examples

### Find healthy services
```json
{ "health_status": "HEALTHY" }
```

### Find high latency (>100ms)
```json
{ "avg_latency_ms": { "$gt": 100 } }
```

### Find specific circle
```json
{ "circle": "NORTH" }
```

### Complex query
```json
{ 
  "circle": "NORTH", 
  "health_status": "HEALTHY",
  "availability_pct": { "$gte": 99 }
}
```

---

## Useful Aggregation Examples

### Count by circle
```json
[{
  "$group": {
    "_id": "$circle",
    "count": { "$sum": 1 }
  }
}]
```

### Average health by service type
```json
[{
  "$group": {
    "_id": "$service_type",
    "avg_health": { "$avg": "$kpi_health_score" }
  }
}]
```

### Top 5 circles by health
```json
[
  {
    "$group": {
      "_id": "$circle",
      "avg_health": { "$avg": "$kpi_health_score" }
    }
  },
  {
    "$sort": { "avg_health": -1 }
  },
  {
    "$limit": 5
  }
]
```

---

## Database Info

- **Database:** aura-metrics
- **Collection:** auraGeneralMetrics
- **Connection:** MongoDB Atlas (configured in application.properties)

---

## Troubleshooting

### Application won't start
- Check MongoDB connection string in `application.properties`
- Verify MongoDB is running and accessible
- Check firewall settings

### Port 8081 already in use
- Change port in `application.properties`:
  ```properties
  server.port=8082
  ```

### 404 on endpoints
- Ensure application is running
- Check URL spelling and method (POST/GET/PUT/DELETE)

### Invalid JSON error
- Check JSON syntax (use online JSON validator)
- Ensure quotes are properly escaped
- Validate MongoDB query syntax

---

## Need Help?

- **Full API Docs:** See `API_DOCUMENTATION.md`
- **Testing Guide:** See `TESTING_GUIDE.md`
- **Implementation Details:** See `IMPLEMENTATION_SUMMARY.md`

---

## Project Files

| File | Purpose |
|------|---------|
| `src/main/java/com/jio/jcpaura/entity/AuraGeneralMetrics.java` | Data model |
| `src/main/java/com/jio/jcpaura/repository/AuraGeneralMetricsRepository.java` | Database layer |
| `src/main/java/com/jio/jcpaura/service/AuraGeneralMetricsService.java` | Business logic |
| `src/main/java/com/jio/jcpaura/controller/AuraGeneralMetricsController.java` | REST API |
| `API_DOCUMENTATION.md` | Complete API reference |
| `TESTING_GUIDE.md` | Testing instructions |
| `IMPLEMENTATION_SUMMARY.md` | Technical details |
| `QUICK_START.md` | This file |

---

**You're all set! 🚀 Start the application and begin querying your metrics.**


