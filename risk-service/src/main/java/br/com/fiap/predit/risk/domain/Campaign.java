package br.com.fiap.predit.risk.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "campaigns")
public class Campaign {
    @Id private UUID id;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 400) private String description;
    @Column(nullable = false, length = 120) private String segment;
    @Column(name = "consent_required", nullable = false) private boolean consentRequired;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private CampaignStatus status;
    @Column(name = "eligible_customers", nullable = false) private int eligibleCustomers;
    @Column(name = "estimated_conversion_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal estimatedConversionRate;
    @Column(name = "estimated_revenue", nullable = false, precision = 14, scale = 2)
    private BigDecimal estimatedRevenue;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "activated_at") private Instant activatedAt;

    protected Campaign() {}

    public Campaign(String name, String description, String segment, boolean consentRequired,
                    int eligibleCustomers, BigDecimal estimatedConversionRate, BigDecimal estimatedRevenue) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.segment = segment;
        this.consentRequired = consentRequired;
        this.status = CampaignStatus.DRAFT;
        this.eligibleCustomers = eligibleCustomers;
        this.estimatedConversionRate = estimatedConversionRate;
        this.estimatedRevenue = estimatedRevenue;
        this.createdAt = Instant.now();
    }

    public void changeStatus(CampaignStatus status) {
        this.status = status;
        if (status == CampaignStatus.ACTIVE && activatedAt == null) activatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSegment() { return segment; }
    public boolean isConsentRequired() { return consentRequired; }
    public CampaignStatus getStatus() { return status; }
    public int getEligibleCustomers() { return eligibleCustomers; }
    public BigDecimal getEstimatedConversionRate() { return estimatedConversionRate; }
    public BigDecimal getEstimatedRevenue() { return estimatedRevenue; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getActivatedAt() { return activatedAt; }
}
