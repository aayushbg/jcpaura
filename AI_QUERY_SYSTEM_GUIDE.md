# AI-Powered Query System Documentation

## Overview

The new AI-powered query system implements a 4-step intelligent query pipeline that converts natural language questions into MongoDB queries, executes them, and returns formatted responses.

## Architecture

### 4-Step Pipeline

1. **Step 1: Entity Identification (getEntities)**
   - Analyzes the user's natural language message
   - Identifies which database table/collection is being queried
   - Provides context about why this table was selected

2. **Step 2: Query Builder (getQueryBuilder)**
   - Takes the identified entity and original user message
   - Generates appropriate MongoDB query or aggregation pipeline
   - Returns valid JSON that can be directly executed

3. **Step 3: Query Execution (executeQuery)**
   - Executes the generated MongoDB query
   - Returns raw data from the database
   - Handles both simple queries and complex aggregation pipelines

4. **Step 4: Response Formatting (getResponse)**
   - Takes the query results and original question
   - Generates a user-friendly response using AI
   - Provides context and insights about the data

## API Endpoints

### 1. Process Natural Language Query (Main Endpoint)

**Endpoint:** `POST /api/aura-ai/message`

**Description:** Process a natural language message and return AI-powered response with data

**Request Body:**
```json
{
  "message": "What is the availability in Karnataka?"
}
```

**Response Body:**
```json
{
  "success": true,
  "originalMessage": "What is the availability in Karnataka?",
  "entityIdentification": "Entity: auraGeneralMetrics - The user is asking about network performance metrics for a specific region.",
  "mongoQuery": "{ \"circle\": \"Karnataka\" }",
  "resultCount": 5,
  "queryResults": [
    {
      "_id": "6982edd620ae344dcc511a1e",
      "circle": "Karnataka",
      "service_type": "Jio5G",
      "site_id": "KA-BLR-021",
      "availability_pct": 99.85,
      "active_users": 1120,
      "error_rate_pct": 0.5,
      "kpi_health_score": 90.3,
      "health_status": "GOOD",
      "kpi_timestamp": "2026-01-28T14:10:00Z",
      "avg_latency_ms": 20.1,
      "packet_loss_pct": 0.3,
      "throughput_mbps": 295.6
    }
    // ... more results
  ],
  "response": "Based on the Karnataka circle metrics, the availability is 99.85%, which is excellent. The region has 1120 active users with a very low error rate of 0.5%. The health status is GOOD with a health score of 90.3 out of 100.",
  "timestamp": 1707458688000
}
```

### 2. Health Check

**Endpoint:** `GET /api/aura-ai/health`

**Description:** Verify that the AI Query Service is running

**Response:**
```json
{
  "success": true,
  "status": "AI Query Service is running",
  "timestamp": 1707458688000
}
```

### 3. API Documentation

**Endpoint:** `GET /api/aura-ai/docs`

**Description:** Get API documentation and examples

## Request/Response Details

### Request Format

All requests should use JSON with `Content-Type: application/json`

```json
{
  "message": "<Natural language query string>"
}
```

### Response Format

All successful responses follow this format:

```json
{
  "success": true,
  "originalMessage": "<The user's original message>",
  "entityIdentification": "<AI's entity identification>",
  "mongoQuery": "<Generated MongoDB query>",
  "resultCount": <Number of results>,
  "queryResults": [<Array of results>],
  "response": "<AI-formatted response>",
  "timestamp": <Unix timestamp>
}
```

### Error Response Format

```json
{
  "success": false,
  "error": "<Error message>",
  "timestamp": <Unix timestamp>
}
```

## Supported Queries

### Examples

#### 1. Simple Attribute Query
```
User Query: "What is the availability in Karnataka?"

Expected Flow:
- Step 1: Identifies auraGeneralMetrics table
- Step 2: Generates query: { "circle": "Karnataka" }
- Step 3: Executes query, returns matching records
- Step 4: Formats response with availability metrics
```

#### 2. Filtered Query with Conditions
```
User Query: "Show me all sites with error rate above 5%"

Expected Flow:
- Step 1: Identifies auraGeneralMetrics table
- Step 2: Generates query: { "error_rate_pct": { "$gt": 5 } }
- Step 3: Executes query, returns filtered records
- Step 4: Formats response with site information
```

#### 3. Aggregation Query
```
User Query: "How many active users are in each service type?"

Expected Flow:
- Step 1: Identifies auraGeneralMetrics table
- Step 2: Generates aggregation pipeline with $group operation
- Step 3: Executes aggregation, returns grouped results
- Step 4: Formats response with summary statistics
```

#### 4. Complex Multi-Condition Query
```
User Query: "Get all GOOD health status sites in North circle with availability above 95%"

Expected Flow:
- Step 1: Identifies auraGeneralMetrics table
- Step 2: Generates query with multiple conditions
- Step 3: Executes query with AND logic
- Step 4: Formats response with matching sites
```

## Available Fields in auraGeneralMetrics Collection

| Field | Type | Description |
|-------|------|-------------|
| _id | ObjectId | Unique document identifier |
| active_users | Integer | Number of active users (0-10000+) |
| availability_pct | Double | Availability percentage (0-100) |
| avg_latency_ms | Double | Average latency in milliseconds |
| circle | String | Geographic circle/region (e.g., "Karnataka", "North", "South") |
| error_rate_pct | Double | Error rate percentage (0-100) |
| health_status | String | System health status (GOOD, WARNING, CRITICAL) |
| kpi_health_score | Double | Health score (0-100) |
| kpi_timestamp | Date | Timestamp of the metric |
| packet_loss_pct | Double | Packet loss percentage (0-100) |
| service_type | String | Type of service (e.g., "Jio5G", "Jio4G") |
| site_id | String | Site identifier |
| throughput_mbps | Double | Throughput in Megabits per second |

## Implementation Details

### Files Modified/Created

1. **AuraAIQueryService.java** - New service handling the 4-step pipeline
   - `processQuery()` - Main method orchestrating the entire pipeline
   - `stepGetEntities()` - Entity identification
   - `stepGetQueryBuilder()` - Query building
   - `stepExecuteQuery()` - Query execution
   - `stepGetResponse()` - Response formatting

2. **AuraAIController.java** - New REST controller
   - `POST /api/aura-ai/message` - Main endpoint
   - `GET /api/aura-ai/health` - Health check
   - `GET /api/aura-ai/docs` - Documentation

### System Prompts

The system uses three specialized prompts:

1. **Entity Identification Prompt** - Teaches the model about available tables and their schemas
2. **Query Builder Prompt** - Provides MongoDB syntax and examples
3. **Response Formatting Prompt** - Guides professional data presentation

## Integration with Existing Services

The AI Query Service integrates with:

- **OllamaService** - For AI model interactions
- **AuraGeneralMetricsService** - For query execution
- **AuraGeneralMetricsRepository** - For data access

## Error Handling

The system handles various error scenarios:

1. **Empty Message** - Returns 400 Bad Request
2. **Invalid JSON in Generated Query** - Attempts to extract valid JSON
3. **Query Execution Failure** - Returns detailed error message
4. **Ollama Service Unavailable** - Returns service error

## Testing the System

### Using cURL

```bash
# Test with a simple query
curl -X POST http://localhost:8081/api/aura-ai/message \
  -H "Content-Type: application/json" \
  -d '{"message": "What is the availability in Karnataka?"}'

# Test with aggregation
curl -X POST http://localhost:8081/api/aura-ai/message \
  -H "Content-Type: application/json" \
  -d '{"message": "How many active users are in each service type?"}'

# Check health
curl http://localhost:8081/api/aura-ai/health

# Get documentation
curl http://localhost:8081/api/aura-ai/docs
```

### Using Postman

1. Create a new POST request
2. URL: `http://localhost:8081/api/aura-ai/message`
3. Headers: `Content-Type: application/json`
4. Body (raw JSON):
```json
{
  "message": "What is the availability in Karnataka?"
}
```
5. Send and view the response

## Performance Considerations

1. **Ollama Service** - Ensure Ollama is running with qwen2.5-coder:3b model
2. **MongoDB Connection** - Verify MongoDB is accessible
3. **Network Latency** - AI model inference may take 2-5 seconds per step
4. **Data Size** - Large result sets may take longer to process

## Future Enhancements

1. Caching of entity identification for common queries
2. Query optimization and validation before execution
3. Support for multiple tables/entities
4. Advanced filtering and sorting options
5. Real-time result streaming
6. Query performance metrics
7. User feedback and learning system

## Troubleshooting

### "mongoTemplate bean not found"
Ensure MongoDB configuration is properly set up in `MongoDBConfiguration.java`

### "Ollama service is not available"
- Check if Ollama is running: `curl http://localhost:11434/api/tags`
- Verify the model is available: `ollama list`
- Default model: `qwen2.5-coder:3b`

### "Empty message" error
Ensure the request body contains a non-empty `message` field

### Null fields in response
This is normal for optional fields that don't have data in the database

## References

- MongoDB Query Documentation: https://docs.mongodb.com/manual/reference/method/db.collection.find/
- Ollama Documentation: https://github.com/ollama/ollama
- Spring Data MongoDB: https://spring.io/projects/spring-data-mongodb

