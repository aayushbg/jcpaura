# JCP Aura Backend API Documentation

## Overview
The JCP Aura Backend provides REST API endpoints to query and manage KPI metrics stored in MongoDB. The backend supports both simple queries and complex aggregation pipelines.

## Entity: AuraGeneralMetrics

Maps to MongoDB collection: `auraGeneralMetrics`

### Fields
- `_id` (String): MongoDB ObjectId - Primary identifier
- `active_users` (Integer): Number of active users
- `availability_pct` (Double): Service availability percentage (0-100)
- `avg_latency_ms` (Double): Average latency in milliseconds
- `circle` (String): Geographic circle/region identifier
- `error_rate_pct` (Double or Integer): Error rate percentage
- `health_status` (String): Service health status (e.g., HEALTHY, DEGRADED, DOWN)
- `kpi_health_score` (Double): Overall KPI health score (0-100)
- `kpi_timestamp` (LocalDateTime): Timestamp when metrics were recorded
- `packet_loss_pct` (Double): Packet loss percentage
- `service_type` (String): Type of service (e.g., API, DATABASE, CACHE)
- `site_id` (String): Unique site identifier
- `throughput_mbps` (Double): Throughput in megabits per second

## API Endpoints

### Base URL
```
http://localhost:8081/api/aura-metrics
```

---

## 1. Execute Query or Aggregation Pipeline
**POST** `/api/aura-metrics/query`

Execute JSON-based MongoDB queries or aggregation pipelines.

### Request Body
```json
{
  "type": "query",
  "data": "{ \"circle\": \"NORTH\", \"health_status\": \"HEALTHY\" }"
}
```

Or for aggregation:
```json
{
  "type": "aggregation",
  "data": "[{\"$match\": {\"circle\": \"NORTH\"}}, {\"$group\": {\"_id\": \"$service_type\", \"count\": {\"$sum\": 1}}}]"
}
```

### Parameters
- `type` (string): "query" for simple MongoDB queries, "aggregation" for aggregation pipelines
- `data` (string): JSON string containing the query or pipeline

### Response (Success - 200 OK)
```json
{
  "success": true,
  "type": "query",
  "count": 5,
  "data": [
    {
      "_id": "507f1f77bcf86cd799439011",
      "active_users": 150,
      "availability_pct": 99.5,
      "avg_latency_ms": 45.2,
      "circle": "NORTH",
      "error_rate_pct": 0.5,
      "health_status": "HEALTHY",
      "kpi_health_score": 95.0,
      "kpi_timestamp": "2026-02-09T10:30:00",
      "packet_loss_pct": 0.1,
      "service_type": "API",
      "site_id": "SITE001",
      "throughput_mbps": 850.5
    }
  ]
}
```

### Response (Error - 400 Bad Request)
```json
{
  "success": false,
  "error": "Error executing query: Invalid JSON syntax",
  "timestamp": 1707547200000
}
```

### Example cURL Commands

#### Simple Query - Find all HEALTHY services in NORTH circle
```bash
curl -X POST http://localhost:8081/api/aura-metrics/query \
  -H "Content-Type: application/json" \
  -d '{
    "type": "query",
    "data": "{ \"circle\": \"NORTH\", \"health_status\": \"HEALTHY\" }"
  }'
```

#### Aggregation - Count services by type
```bash
curl -X POST http://localhost:8081/api/aura-metrics/query \
  -H "Content-Type: application/json" \
  -d '{
    "type": "aggregation",
    "data": "[{\"$group\": {\"_id\": \"$service_type\", \"count\": {\"$sum\": 1}, \"avg_health\": {\"$avg\": \"$kpi_health_score\"}}}]"
  }'
```

#### Complex Aggregation - Services with latency > 100ms grouped by circle
```bash
curl -X POST http://localhost:8081/api/aura-metrics/query \
  -H "Content-Type: application/json" \
  -d '{
    "type": "aggregation",
    "data": "[{\"$match\": {\"avg_latency_ms\": {\"$gt\": 100}}}, {\"$group\": {\"_id\": \"$circle\", \"count\": {\"$sum\": 1}, \"avg_latency\": {\"$avg\": \"$avg_latency_ms\"}}}]"
  }'
```

---

## 2. Get All Metrics
**GET** `/api/aura-metrics/all`

Retrieve all metrics from the collection.

### Response (Success - 200 OK)
```json
{
  "success": true,
  "count": 25,
  "data": [
    {
      "_id": "507f1f77bcf86cd799439011",
      "active_users": 150,
      ...
    }
  ]
}
```

### Example cURL
```bash
curl -X GET http://localhost:8081/api/aura-metrics/all
```

---

## 3. Get Metric by ID
**GET** `/api/aura-metrics/{id}`

Retrieve a specific metric by MongoDB ObjectId.

### Path Parameters
- `id` (string): MongoDB ObjectId (24-character hex string)

### Response (Success - 200 OK)
```json
{
  "success": true,
  "data": {
    "_id": "507f1f77bcf86cd799439011",
    ...
  }
}
```

### Response (Error - 404 Not Found)
```json
{
  "success": false,
  "message": "Metrics not found with ID: 507f1f77bcf86cd799439011"
}
```

### Example cURL
```bash
curl -X GET http://localhost:8081/api/aura-metrics/507f1f77bcf86cd799439011
```

---

## 4. Create New Metrics
**POST** `/api/aura-metrics/create`

Create a new metrics document.

### Request Body
```json
{
  "active_users": 200,
  "availability_pct": 99.8,
  "avg_latency_ms": 50.5,
  "circle": "NORTH",
  "error_rate_pct": 0.2,
  "health_status": "HEALTHY",
  "kpi_health_score": 96.5,
  "kpi_timestamp": "2026-02-09T10:30:00",
  "packet_loss_pct": 0.05,
  "service_type": "API",
  "site_id": "SITE001",
  "throughput_mbps": 900.0
}
```

### Response (Success - 201 Created)
```json
{
  "success": true,
  "message": "Metrics created successfully",
  "data": {
    "_id": "507f1f77bcf86cd799439012",
    "active_users": 200,
    ...
  }
}
```

### Example cURL
```bash
curl -X POST http://localhost:8081/api/aura-metrics/create \
  -H "Content-Type: application/json" \
  -d '{
    "active_users": 200,
    "availability_pct": 99.8,
    "avg_latency_ms": 50.5,
    "circle": "NORTH",
    "error_rate_pct": 0.2,
    "health_status": "HEALTHY",
    "kpi_health_score": 96.5,
    "kpi_timestamp": "2026-02-09T10:30:00",
    "packet_loss_pct": 0.05,
    "service_type": "API",
    "site_id": "SITE001",
    "throughput_mbps": 900.0
  }'
```

---

## 5. Update Existing Metrics
**PUT** `/api/aura-metrics/{id}`

Update a metric document.

### Path Parameters
- `id` (string): MongoDB ObjectId

### Request Body
Same as Create request - provide fields to update

### Response (Success - 200 OK)
```json
{
  "success": true,
  "message": "Metrics updated successfully",
  "data": {
    "_id": "507f1f77bcf86cd799439011",
    ...
  }
}
```

### Example cURL
```bash
curl -X PUT http://localhost:8081/api/aura-metrics/507f1f77bcf86cd799439011 \
  -H "Content-Type: application/json" \
  -d '{
    "health_status": "DEGRADED",
    "kpi_health_score": 75.0
  }'
```

---

## 6. Delete Metrics
**DELETE** `/api/aura-metrics/{id}`

Delete a metric document.

### Path Parameters
- `id` (string): MongoDB ObjectId

### Response (Success - 200 OK)
```json
{
  "success": true,
  "message": "Metrics deleted successfully"
}
```

### Example cURL
```bash
curl -X DELETE http://localhost:8081/api/aura-metrics/507f1f77bcf86cd799439011
```

---

## MongoDB Query Examples for the `/query` Endpoint

### 1. Find all metrics for a specific circle
```json
{
  "type": "query",
  "data": "{ \"circle\": \"NORTH\" }"
}
```

### 2. Find healthy services with availability > 99%
```json
{
  "type": "query",
  "data": "{ \"health_status\": \"HEALTHY\", \"availability_pct\": { \"$gt\": 99 } }"
}
```

### 3. Find services with latency issues (> 100ms)
```json
{
  "type": "query",
  "data": "{ \"avg_latency_ms\": { \"$gt\": 100 } }"
}
```

### 4. Find by service type with multiple conditions
```json
{
  "type": "query",
  "data": "{ \"service_type\": \"API\", \"error_rate_pct\": { \"$gt\": 1, \"$lt\": 5 } }"
}
```

---

## MongoDB Aggregation Pipeline Examples

### 1. Group metrics by circle and calculate averages
```json
{
  "type": "aggregation",
  "data": "[{\"$group\": {\"_id\": \"$circle\", \"count\": {\"$sum\": 1}, \"avg_health_score\": {\"$avg\": \"$kpi_health_score\"}, \"avg_latency\": {\"$avg\": \"$avg_latency_ms\"}}}]"
}
```

### 2. Find top 5 circles by average health score
```json
{
  "type": "aggregation",
  "data": "[{\"$group\": {\"_id\": \"$circle\", \"avg_health\": {\"$avg\": \"$kpi_health_score\"}}}, {\"$sort\": {\"avg_health\": -1}}, {\"$limit\": 5}]"
}
```

### 3. Count services by health status
```json
{
  "type": "aggregation",
  "data": "[{\"$group\": {\"_id\": \"$health_status\", \"count\": {\"$sum\": 1}}}]"
}
```

### 4. Find services with availability issues and group by site
```json
{
  "type": "aggregation",
  "data": "[{\"$match\": {\"availability_pct\": {\"$lt\": 99}}}, {\"$group\": {\"_id\": \"$site_id\", \"count\": {\"$sum\": 1}, \"avg_availability\": {\"$avg\": \"$availability_pct\"}}}, {\"$sort\": {\"avg_availability\": 1}}]"
}
```

### 5. Complex pipeline with multiple stages
```json
{
  "type": "aggregation",
  "data": "[{\"$match\": {\"circle\": \"NORTH\"}}, {\"$group\": {\"_id\": \"$service_type\", \"count\": {\"$sum\": 1}, \"avg_health\": {\"$avg\": \"$kpi_health_score\"}, \"max_latency\": {\"$max\": \"$avg_latency_ms\"}}}, {\"$sort\": {\"avg_health\": -1}}]"
}
```

---

## Error Handling

All endpoints return errors in the following format:

```json
{
  "success": false,
  "error": "Description of the error",
  "timestamp": 1707547200000
}
```

### Common HTTP Status Codes
- **200 OK**: Request successful
- **201 Created**: Resource created successfully
- **400 Bad Request**: Invalid request or query syntax
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server-side error

---

## Configuration

The MongoDB connection is configured in `application.properties`:

```properties
spring.data.mongodb.uri=mongodb+srv://root:root@aura-network-kpi-metric.oqwtf5o.mongodb.net/?appName=aura-network-kpi-metrics
spring.data.mongodb.database=aura-metrics
server.port=8081
```

Database: `aura-metrics`
Collection: `auraGeneralMetrics`

---

## Project Structure

```
src/main/java/com/jio/jcpaura/
├── entity/
│   └── AuraGeneralMetrics.java       # Entity class mapping to MongoDB collection
├── repository/
│   └── AuraGeneralMetricsRepository.java  # MongoDB Repository interface
├── service/
│   └── AuraGeneralMetricsService.java     # Business logic and query execution
├── controller/
│   └── AuraGeneralMetricsController.java  # REST API endpoints
└── JcpauraApplication.java           # Main Spring Boot application class
```

---

## Development Notes

- The application uses Spring Data MongoDB for ORM operations
- Custom aggregation support via MongoTemplate for complex queries
- All queries are executed as JSON strings, providing flexibility for dynamic query building
- Timestamps are stored as LocalDateTime objects and automatically converted by Spring
- Error handling includes detailed error messages for debugging


