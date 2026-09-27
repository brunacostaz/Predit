package br.com.fiap.predit.risk.api;

import br.com.fiap.predit.risk.domain.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class LeadDtos {
    private LeadDtos() {}

    public record CreateLeadRequest(
            @NotNull UUID customerId,
            @NotNull UUID vehicleId,
            @NotBlank @Size(max = 160) String title,
            @NotBlank @Size(max = 400) String action,
            @NotNull RiskLevel priority,
            Instant dueAt
    ) {}

    public record UpdateLeadStatusRequest(@NotNull LeadStatus status, @Size(max = 180) String assignedTo) {}

    public record LeadResponse(
            UUID id, UUID customerId, String customerName, UUID vehicleId, String vin, String model,
            String title, String action, LeadStatus status, RiskLevel priority, Instant dueAt,
            String assignedTo, Instant createdAt, Instant updatedAt
    ) {
        public static LeadResponse from(Lead lead) {
            return new LeadResponse(lead.getId(), lead.getCustomer().getId(), lead.getCustomer().getName(),
                    lead.getVehicle().getId(), lead.getVehicle().getVin(), lead.getVehicle().getModel(),
                    lead.getTitle(), lead.getAction(), lead.getStatus(), lead.getPriority(), lead.getDueAt(),
                    lead.getAssignedTo(), lead.getCreatedAt(), lead.getUpdatedAt());
        }
    }
}
