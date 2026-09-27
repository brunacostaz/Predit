package br.com.fiap.predit.risk.api;

import br.com.fiap.predit.risk.domain.Campaign;
import br.com.fiap.predit.risk.domain.CampaignStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class CampaignDtos {
    private CampaignDtos() {}

    public record CreateCampaignRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 400) String description,
            @NotBlank @Size(max = 120) String segment,
            boolean consentRequired,
            @PositiveOrZero int eligibleCustomers,
            @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal estimatedConversionRate,
            @NotNull @PositiveOrZero BigDecimal estimatedRevenue
    ) {}

    public record UpdateCampaignStatusRequest(@NotNull CampaignStatus status) {}

    public record CampaignResponse(
            UUID id, String name, String description, String segment, boolean consentRequired,
            CampaignStatus status, int eligibleCustomers, BigDecimal estimatedConversionRate,
            BigDecimal estimatedRevenue, Instant createdAt, Instant activatedAt
    ) {
        public static CampaignResponse from(Campaign campaign) {
            return new CampaignResponse(campaign.getId(), campaign.getName(), campaign.getDescription(),
                    campaign.getSegment(), campaign.isConsentRequired(), campaign.getStatus(),
                    campaign.getEligibleCustomers(), campaign.getEstimatedConversionRate(),
                    campaign.getEstimatedRevenue(), campaign.getCreatedAt(), campaign.getActivatedAt());
        }
    }
}
