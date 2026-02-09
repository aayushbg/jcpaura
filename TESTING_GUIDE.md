# Quick Testing Guide - JCP Aura Backend

## Prerequisites
- MongoDB connection configured and working
- Application running on port 8081
- MongoDB Atlas or local MongoDB instance accessible

## Starting the Application

```bash
# Navigate to project directory
cd A:\jcpaura

# Run the application
mvn spring-boot:run

# Or build and run the JAR
mvn clean package -DskipTests
java -jar target/jcpaura-0.0.1-SNAPSHOT.jar
```

---

## Quick Test Commands

### 1. Health Check - Get All Metrics
```bash
curl -X GET http://localhost:8081/api/aura-metrics/all
```

### 2. Create Sample Metrics
```bash
curl -X POST http://localhost:8081/api/aura-metrics/create \
  -H "Content-Type: application/json" \
  -d '{
    "active_users": 250,
    "availability_pct": 99.5,
    "avg_latency_ms": 45.3,
    "circle": "NORTH",
    "error_rate_pct": 0.3,
    "health_status": "HEALTHY",
    "kpi_health_score": 97.0,
    "kpi_timestamp": "2026-02-09T12:00:00",
    "packet_loss_pct": 0.1,
    "service_type": "API",
    "site_id": "SITE001",
    "throughput_mbps": 950.0
  }'
```

Save the returned `_id` for subsequent tests.

### 3. Simple Query - Find by Circle
```bash
curl -X POST http://localhost:8081/api/aura-metrics/query \
  -H "Content-Type: application/json" \
  -d '{
    "type": "query",
    "data": "{ \"circle\": \"NORTH\" }"
  }'
```

### 4. Query with Multiple Conditions
```bash
curl -X POST http://localhost:8081/api/aura-metrics/query \
  -H "Content-Type: application/json" \
  -d '{
    "type": "query",
    "data": "{ \"circle\": \"NORTH\", \"health_status\": \"HEALTHY\", \"availability_pct\": { \"$gt\": 99 } }"
  }'
```

### 5. Aggregation - Group by Service Type
```bash
curl -X POST http://localhost:8081/api/aura-metrics/query \
  -H "Content-Type: application/json" \
  -d '{
    "type": "aggregation",
    "data": "[{\"$group\": {\"_id\": \"$service_type\", \"count\": {\"$sum\": 1}, \"avg_health\": {\"$avg\": \"$kpi_health_score\"}}}]"
  }'
```

### 6. Aggregation - Count by Circle
```bash
curl -X POST http://localhost:8081/api/aura-metrics/query \
  -H "Content-Type: application/json" \
  -d '{
    "type": "aggregation",
    "data": "[{\"$group\": {\"_id\": \"$circle\", \"count\": {\"$sum\": 1}, \"avg_latency\": {\"$avg\": \"$avg_latency_ms\"}}}]"
  }'
```

### 7. Get by ID (replace with your ID)
```bash
curl -X GET http://localhost:8081/api/aura-metrics/507f1f77bcf86cd799439011
```

### 8. Update Metrics (replace with your ID)
```bash
curl -X PUT http://localhost:8081/api/aura-metrics/507f1f77bcf86cd799439011 \
  -H "Content-Type: application/json" \
  -d '{
    "health_status": "DEGRADED",
    "kpi_health_score": 75.0
  }'
```

### 9. Delete Metrics (replace with your ID)
```bash
curl -X DELETE http://localhost:8081/api/aura-metrics/507f1f77bcf86cd799439011
```

---

## Testing with Postman

### Import Collection
You can import these requests into Postman:

1. **Create New Request**
   - Method: POST
   - URL: `http://localhost:8081/api/aura-metrics/query`
   - Body (raw JSON): 
   ```json
   {
     "type": "query",
     "data": "{ \"circle\": \"NORTH\" }"
   }
   ```

2. **Test Aggregation**
   - Method: POST
   - URL: `http://localhost:8081/api/aura-metrics/query`
   - Body (raw JSON):
   ```json
   {
     "type": "aggregation",
     "data": "[{\"$match\": {\"health_status\": \"HEALTHY\"}}, {\"$group\": {\"_id\": \"$circle\", \"count\": {\"$sum\": 1}}}]"
   }
   ```

---

## Testing with MongoDB Compass

You can also verify data directly in MongoDB:

1. Connect to your MongoDB instance
2. Select database: `aura-metrics`
3. Select collection: `auraGeneralMetrics`
4. Run queries in the "Aggregation" or "Find" tabs

Sample MongoDB query:
```javascript
{
  "circle": "NORTH",
  "health_status": "HEALTHY"
}
```

Sample aggregation:
```javascript
[
  {
    "$group": {
      "_id": "$service_type",
      "count": { "$sum": 1 },
      "avg_health": { "$avg": "$kpi_health_score" }
    }
  }
]
```

---

## Common Issues & Solutions

### Issue: Connection Refused
**Solution**: Ensure MongoDB is running and connection string in `application.properties` is correct.

### Issue: JSON Parse Error
**Solution**: Verify JSON syntax in the request body. Use proper escaping for special characters.

### Issue: 404 Not Found
**Solution**: Ensure the ID exists. Copy the exact ID from the create response.

### Issue: 400 Bad Request
**Solution**: Check the error message in the response. Common causes:
- Invalid JSON syntax in the `data` field
- Missing required fields
- Incorrect MongoDB operator syntax

### Issue: Deprecated API Warning
**Solution**: This is a deprecation warning from Spring Data MongoDB. The code still works correctly.

---

## MongoDB Query Operators Reference

### Comparison Operators
- `$eq`: Equal
- `$ne`: Not equal
- `$gt`: Greater than
- `$gte`: Greater than or equal
- `$lt`: Less than
- `$lte`: Less than or equal
- `$in`: In array
- `$nin`: Not in array

### Logical Operators
- `$and`: All conditions true
- `$or`: Any condition true
- `$not`: Negate condition
- `$nor`: None of conditions true

### Array Operators
- `$elemMatch`: Match array element
- `$size`: Array length

### Aggregation Pipeline Stages
- `$match`: Filter documents
- `$group`: Group documents
- `$sort`: Sort documents
- `$limit`: Limit results
- `$skip`: Skip documents
- `$project`: Select fields
- `$lookup`: Join collections
- `$unwind`: Deconstruct array

---

## Sample Test Data

You can insert this sample data to test:

```bash
curl -X POST http://localhost:8081/api/aura-metrics/create \
  -H "Content-Type: application/json" \
  -d '{
    "active_users": 300,
    "availability_pct": 99.8,
    "avg_latency_ms": 35.5,
    "circle": "SOUTH",
    "error_rate_pct": 0.2,
    "health_status": "HEALTHY",
    "kpi_health_score": 98.5,
    "kpi_timestamp": "2026-02-09T12:00:00",
    "packet_loss_pct": 0.05,
    "service_type": "DATABASE",
    "site_id": "SITE002",
    "throughput_mbps": 1200.0
  }'
```

```bash
curl -X POST http://localhost:8081/api/aura-metrics/create \
  -H "Content-Type: application/json" \
  -d '{
    "active_users": 150,
    "availability_pct": 98.5,
    "avg_latency_ms": 75.2,
    "circle": "EAST",
    "error_rate_pct": 1.5,
    "health_status": "DEGRADED",
    "kpi_health_score": 80.0,
    "kpi_timestamp": "2026-02-09T12:00:00",
    "packet_loss_pct": 0.3,
    "service_type": "CACHE",
    "site_id": "SITE003",
    "throughput_mbps": 600.0
  }'
```

---

## Performance Tips

1. **Use indexing** for frequently queried fields:
   - `circle`
   - `service_type`
   - `health_status`
   - `kpi_timestamp`

2. **Use aggregation pipelines** for complex analytics instead of loading all documents

3. **Paginate** results for large datasets using `$skip` and `$limit`

4. **Cache** frequently used queries in your chatbot application

---

## Next Steps

1. ✅ Create sample data
2. ✅ Test all CRUD operations
3. ✅ Test simple queries
4. ✅ Test aggregation pipelines
5. ✅ Verify error handling
6. 🔄 Integrate with chatbot frontend
7. 🔄 Monitor performance and optimize as needed


