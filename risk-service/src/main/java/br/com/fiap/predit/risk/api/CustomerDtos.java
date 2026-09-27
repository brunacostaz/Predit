package br.com.fiap.predit.risk.api;

import br.com.fiap.predit.risk.domain.*;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class CustomerDtos {
    private CustomerDtos() {}

    public record CreateCustomerRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 180) String email,
            @Pattern(regexp = "^\\+?[0-9]{10,15}$") String phone,
            @NotBlank @Size(max = 120) String dealership,
            boolean contactConsent,
            @NotBlank @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$") String vin,
            @NotBlank @Size(max = 80) String model,
            @Min(2000) @Max(2100) int modelYear,
            @PositiveOrZero int mileage,
            LocalDate warrantyEndDate,
            LocalDate lastServiceDate
    ) {}

    public record CustomerResponse(
            UUID id, String name, String email, String phone, String dealership,
            boolean contactConsent, Instant consentUpdatedAt, Instant createdAt
    ) {
        public static CustomerResponse from(Customer c) {
            return new CustomerResponse(c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getDealership(),
                    c.isContactConsent(), c.getConsentUpdatedAt(), c.getCreatedAt());
        }
    }

    public record VehicleResponse(
            UUID id, String vin, String model, int modelYear, int mileage,
            LocalDate warrantyEndDate, LocalDate lastServiceDate
    ) {
        public static VehicleResponse from(Vehicle v) {
            return new VehicleResponse(v.getId(), v.getVin(), v.getModel(), v.getModelYear(), v.getMileage(),
                    v.getWarrantyEndDate(), v.getLastServiceDate());
        }
    }

    public record RiskResponse(
            UUID id, int score, RiskLevel level, String reasons, String recommendedAction,
            String modelName, String modelVersion, Instant assessedAt
    ) {
        public static RiskResponse from(RiskAssessment r) {
            return new RiskResponse(r.getId(), r.getScore(), r.getLevel(), r.getReasons(),
                    r.getRecommendedAction(), r.getModelName(), r.getModelVersion(), r.getAssessedAt());
        }
    }

    public record CustomerDetailResponse(CustomerResponse customer, VehicleResponse vehicle, RiskResponse risk) {}

    public record CreateRiskRequest(
            @Min(0) @Max(100) int score,
            @NotBlank @Size(max = 1000) String reasons,
            @NotBlank @Size(max = 400) String recommendedAction,
            @NotBlank @Size(max = 80) String modelName,
            @NotBlank @Size(max = 40) String modelVersion
    ) {}
}
