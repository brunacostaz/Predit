package br.com.fiap.predit.risk.service;

import br.com.fiap.predit.risk.api.DashboardResponse;
import br.com.fiap.predit.risk.api.DashboardResponse.ModelRisk;
import br.com.fiap.predit.risk.api.DashboardResponse.RiskBand;
import br.com.fiap.predit.risk.domain.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private static final double AVERAGE_SERVICE_PLAN_REVENUE = 11_910.0;
    private final VehicleRepository vehicles;
    private final RiskAssessmentRepository risks;
    private final LeadRepository leads;
    private final double vinSharePercent;

    public DashboardService(VehicleRepository vehicles, RiskAssessmentRepository risks, LeadRepository leads,
                            @Value("${predit.dashboard.vin-share-percent:68.0}") double vinSharePercent) {
        this.vehicles = vehicles; this.risks = risks; this.leads = leads; this.vinSharePercent = vinSharePercent;
    }

    @Transactional(readOnly = true)
    public DashboardResponse summary() {
        List<Vehicle> allVehicles = vehicles.findAll();
        Map<UUID, RiskAssessment> latest = latestAssessments();
        List<RiskAssessment> currentRisks = new ArrayList<>(latest.values());
        long atRisk = currentRisks.stream().filter(r -> r.getLevel() == RiskLevel.MEDIUM
                || r.getLevel() == RiskLevel.HIGH || r.getLevel() == RiskLevel.CRITICAL).count();
        double average = currentRisks.stream().mapToInt(RiskAssessment::getScore).average().orElse(0);

        List<RiskBand> distribution = Arrays.stream(RiskLevel.values())
                .map(level -> new RiskBand(level.name(), currentRisks.stream()
                        .filter(risk -> risk.getLevel() == level).count()))
                .toList();

        List<ModelRisk> modelRisk = allVehicles.stream()
                .collect(Collectors.groupingBy(Vehicle::getModel))
                .entrySet().stream()
                .map(entry -> new ModelRisk(entry.getKey(), entry.getValue().size(), round(entry.getValue().stream()
                        .map(Vehicle::getId).map(latest::get).filter(Objects::nonNull)
                        .mapToInt(RiskAssessment::getScore).average().orElse(0))))
                .sorted(Comparator.comparingDouble(ModelRisk::averageScore).reversed())
                .toList();

        long readyLeads = leads.countByStatusIn(List.of(LeadStatus.OPEN, LeadStatus.CONTACTED, LeadStatus.IN_PROGRESS));
        return new DashboardResponse(vinSharePercent, allVehicles.size(), atRisk, readyLeads,
                leads.countByStatus(LeadStatus.CONVERTED), round(average),
                atRisk * AVERAGE_SERVICE_PLAN_REVENUE, distribution, modelRisk);
    }

    private Map<UUID, RiskAssessment> latestAssessments() {
        return risks.findAll().stream().collect(Collectors.toMap(
                risk -> risk.getVehicle().getId(), Function.identity(),
                (left, right) -> left.getAssessedAt().isAfter(right.getAssessedAt()) ? left : right));
    }

    private double round(double value) { return Math.round(value * 10.0) / 10.0; }
}
