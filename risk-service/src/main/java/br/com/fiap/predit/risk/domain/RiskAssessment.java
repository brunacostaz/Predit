package br.com.fiap.predit.risk.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false) private Vehicle vehicle;
    @Column(nullable = false) private int score;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private RiskLevel level;
    @Column(nullable = false, columnDefinition = "TEXT") private String reasons;
    @Column(name = "recommended_action", nullable = false, length = 400) private String recommendedAction;
    @Column(name = "model_name", nullable = false, length = 80) private String modelName;
    @Column(name = "model_version", nullable = false, length = 40) private String modelVersion;
    @Column(name = "assessed_at", nullable = false) private Instant assessedAt;

    protected RiskAssessment() {}

    public RiskAssessment(Vehicle vehicle, int score, String reasons, String recommendedAction,
                          String modelName, String modelVersion) {
        this.id = UUID.randomUUID();
        this.vehicle = vehicle;
        this.score = score;
        this.level = levelFor(score);
        this.reasons = reasons;
        this.recommendedAction = recommendedAction;
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        this.assessedAt = Instant.now();
    }

    public static RiskLevel levelFor(int score) {
        if (score >= 80) return RiskLevel.CRITICAL;
        if (score >= 60) return RiskLevel.HIGH;
        if (score >= 35) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    public UUID getId() { return id; }
    public Vehicle getVehicle() { return vehicle; }
    public int getScore() { return score; }
    public RiskLevel getLevel() { return level; }
    public String getReasons() { return reasons; }
    public String getRecommendedAction() { return recommendedAction; }
    public String getModelName() { return modelName; }
    public String getModelVersion() { return modelVersion; }
    public Instant getAssessedAt() { return assessedAt; }
}
