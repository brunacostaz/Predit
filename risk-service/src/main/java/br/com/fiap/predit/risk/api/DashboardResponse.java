package br.com.fiap.predit.risk.api;

import java.util.List;

public record DashboardResponse(
        double vinSharePercent,
        long monitoredVehicles,
        long customersAtRisk,
        long readyLeads,
        long convertedLeads,
        double averageRiskScore,
        double estimatedRevenueAtRisk,
        List<RiskBand> riskDistribution,
        List<ModelRisk> modelRisk
) {
    public record RiskBand(String level, long customers) {}
    public record ModelRisk(String model, long vehicles, double averageScore) {}
}
