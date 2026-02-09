package com.jio.jcpaura.service;

import com.jio.jcpaura.entity.AuraGeneralMetrics;
import com.jio.jcpaura.repository.AuraGeneralMetricsRepository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AuraGeneralMetricsService {

    @Autowired
    private AuraGeneralMetricsRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Execute a JSON-based query against the auraGeneralMetrics collection
     * @param jsonQuery JSON string representing MongoDB query
     * @return List of matching AuraGeneralMetrics documents
     */
    public List<AuraGeneralMetrics> executeQuery(String jsonQuery) {
        try {
            Document queryDoc = Document.parse(jsonQuery);
            Query query = new BasicQuery(queryDoc);
            return mongoTemplate.find(query, AuraGeneralMetrics.class);
        } catch (Exception e) {
            throw new RuntimeException("Error executing query: " + e.getMessage(), e);
        }
    }

    /**
     * Execute a JSON-based aggregation pipeline against the auraGeneralMetrics collection
     * @param pipelineJson JSON string representing MongoDB aggregation pipeline
     * @return List of Document results from aggregation
     */
    public List<Document> executeAggregation(String pipelineJson) {
        try {
            // Parse the JSON pipeline and execute raw aggregation
            List<Document> pipeline = parsePipeline(pipelineJson);
            AggregationResults<Document> result = mongoTemplate.aggregate(
                    Aggregation.newAggregation(
                            pipeline.stream()
                                    .map(doc -> new RawAggregationOperation(doc))
                                    .toArray(RawAggregationOperation[]::new)
                    ),
                    "auraGeneralMetrics",
                    Document.class
            );
            return result.getMappedResults();
        } catch (Exception e) {
            throw new RuntimeException("Error executing aggregation pipeline: " + e.getMessage(), e);
        }
    }

    /**
     * Parse JSON pipeline string to list of Documents
     */
    private List<Document> parsePipeline(String pipelineJson) {
        List<Document> pipeline = new java.util.ArrayList<>();
        try {
            // Parse as array and convert to list of Documents
            String trimmed = pipelineJson.trim();
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                String arrayContent = trimmed.substring(1, trimmed.length() - 1);
                // Simple parsing for array of objects - use MongoDB's document parser
                int depth = 0;
                StringBuilder current = new StringBuilder();

                for (int i = 0; i < arrayContent.length(); i++) {
                    char c = arrayContent.charAt(i);
                    if (c == '{') depth++;
                    if (c == '}') depth--;

                    current.append(c);

                    if (depth == 0 && c == '}') {
                        String docStr = current.toString().trim();
                        if (!docStr.isEmpty()) {
                            pipeline.add(Document.parse(docStr));
                            current = new StringBuilder();
                        }
                    } else if (depth == 0 && c == ',') {
                        current = new StringBuilder();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing pipeline JSON: " + e.getMessage(), e);
        }
        return pipeline;
    }

    /**
     * Get all metrics
     * @return List of all AuraGeneralMetrics documents
     */
    public List<AuraGeneralMetrics> getAllMetrics() {
        return repository.findAll();
    }

    /**
     * Get metric by ID
     * @param id Document ID
     * @return Optional containing the AuraGeneralMetrics if found
     */
    public Optional<AuraGeneralMetrics> getMetricsById(String id) {
        return repository.findById(id);
    }

    /**
     * Save or update metrics
     * @param metrics AuraGeneralMetrics document to save
     * @return Saved AuraGeneralMetrics document
     */
    public AuraGeneralMetrics saveMetrics(AuraGeneralMetrics metrics) {
        return repository.save(metrics);
    }

    /**
     * Delete metric by ID
     * @param id Document ID to delete
     */
    public void deleteMetrics(String id) {
        repository.deleteById(id);
    }

    /**
     * Inner class to wrap raw Document stages for aggregation pipeline
     */
    private static class RawAggregationOperation implements AggregationOperation {
        private final Document stage;

        public RawAggregationOperation(Document stage) {
            this.stage = stage;
        }

        @Override
        public Document toDocument(AggregationOperationContext context) {
            return stage;
        }
    }
}





