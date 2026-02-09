package com.jio.jcpaura.repository;

import com.jio.jcpaura.entity.AuraGeneralMetrics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuraGeneralMetricsRepository extends MongoRepository<AuraGeneralMetrics, String> {
    // Basic CRUD operations inherited from MongoRepository
}

