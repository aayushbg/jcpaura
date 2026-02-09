package com.jio.jcpaura.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Document(collection = "auraGeneralMetrics")
public class AuraGeneralMetrics {

    @Id
    @JsonProperty("_id")
    private String id;

    @Field("active_users")
    @JsonProperty("active_users")
    private Integer activeUsers;

    @Field("availability_pct")
    @JsonProperty("availability_pct")
    private Double availabilityPct;

    @Field("avg_latency_ms")
    @JsonProperty("avg_latency_ms")
    private Double avgLatencyMs;

    @Field("circle")
    @JsonProperty("circle")
    private String circle;

    @Field("error_rate_pct")
    @JsonProperty("error_rate_pct")
    private Double errorRatePct;

    @Field("health_status")
    @JsonProperty("health_status")
    private String healthStatus;

    @Field("kpi_health_score")
    @JsonProperty("kpi_health_score")
    private Double kpiHealthScore;

    @Field("kpi_timestamp")
    @JsonProperty("kpi_timestamp")
    private LocalDateTime kpiTimestamp;

    @Field("packet_loss_pct")
    @JsonProperty("packet_loss_pct")
    private Double packetLossPct;

    @Field("service_type")
    @JsonProperty("service_type")
    private String serviceType;

    @Field("site_id")
    @JsonProperty("site_id")
    private String siteId;

    @Field("throughput_mbps")
    @JsonProperty("throughput_mbps")
    private Double throughputMbps;

    // Constructors
    public AuraGeneralMetrics() {
    }

    public AuraGeneralMetrics(Integer activeUsers, Double availabilityPct, Double avgLatencyMs,
                             String circle, Double errorRatePct, String healthStatus,
                             Double kpiHealthScore, LocalDateTime kpiTimestamp,
                             Double packetLossPct, String serviceType, String siteId,
                             Double throughputMbps) {
        this.activeUsers = activeUsers;
        this.availabilityPct = availabilityPct;
        this.avgLatencyMs = avgLatencyMs;
        this.circle = circle;
        this.errorRatePct = errorRatePct;
        this.healthStatus = healthStatus;
        this.kpiHealthScore = kpiHealthScore;
        this.kpiTimestamp = kpiTimestamp;
        this.packetLossPct = packetLossPct;
        this.serviceType = serviceType;
        this.siteId = siteId;
        this.throughputMbps = throughputMbps;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Integer activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Double getAvailabilityPct() {
        return availabilityPct;
    }

    public void setAvailabilityPct(Double availabilityPct) {
        this.availabilityPct = availabilityPct;
    }

    public Double getAvgLatencyMs() {
        return avgLatencyMs;
    }

    public void setAvgLatencyMs(Double avgLatencyMs) {
        this.avgLatencyMs = avgLatencyMs;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public Double getErrorRatePct() {
        return errorRatePct;
    }

    public void setErrorRatePct(Double errorRatePct) {
        this.errorRatePct = errorRatePct;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public Double getKpiHealthScore() {
        return kpiHealthScore;
    }

    public void setKpiHealthScore(Double kpiHealthScore) {
        this.kpiHealthScore = kpiHealthScore;
    }

    public LocalDateTime getKpiTimestamp() {
        return kpiTimestamp;
    }

    public void setKpiTimestamp(LocalDateTime kpiTimestamp) {
        this.kpiTimestamp = kpiTimestamp;
    }

    public Double getPacketLossPct() {
        return packetLossPct;
    }

    public void setPacketLossPct(Double packetLossPct) {
        this.packetLossPct = packetLossPct;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public Double getThroughputMbps() {
        return throughputMbps;
    }

    public void setThroughputMbps(Double throughputMbps) {
        this.throughputMbps = throughputMbps;
    }

    @Override
    public String toString() {
        return "AuraGeneralMetrics{" +
                "id='" + id + '\'' +
                ", activeUsers=" + activeUsers +
                ", availabilityPct=" + availabilityPct +
                ", avgLatencyMs=" + avgLatencyMs +
                ", circle='" + circle + '\'' +
                ", errorRatePct=" + errorRatePct +
                ", healthStatus='" + healthStatus + '\'' +
                ", kpiHealthScore=" + kpiHealthScore +
                ", kpiTimestamp=" + kpiTimestamp +
                ", packetLossPct=" + packetLossPct +
                ", serviceType='" + serviceType + '\'' +
                ", siteId='" + siteId + '\'' +
                ", throughputMbps=" + throughputMbps +
                '}';
    }
}

