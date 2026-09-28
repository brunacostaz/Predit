package br.com.fiap.predit.risk.service;

import br.com.fiap.predit.risk.api.CustomerDtos.*;
import br.com.fiap.predit.risk.domain.*;
import br.com.fiap.predit.risk.error.ConflictException;
import br.com.fiap.predit.risk.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customers;
    private final VehicleRepository vehicles;
    private final RiskAssessmentRepository risks;

    public CustomerService(CustomerRepository customers, VehicleRepository vehicles, RiskAssessmentRepository risks) {
        this.customers = customers; this.vehicles = vehicles; this.risks = risks;
    }

    @Transactional
    public CustomerDetailResponse create(CreateCustomerRequest request) {
        if (customers.existsByEmailIgnoreCase(request.email())) throw new ConflictException("Email already registered");
        if (vehicles.existsByVin(request.vin())) throw new ConflictException("VIN already registered");
        Customer customer = customers.save(new Customer(request.name(), request.email(), request.phone(),
                request.dealership(), request.contactConsent()));
        Vehicle vehicle = vehicles.save(new Vehicle(customer, request.vin(), request.model(), request.modelYear(),
                request.mileage(), request.warrantyEndDate(), request.lastServiceDate()));
        return new CustomerDetailResponse(CustomerResponse.from(customer), VehicleResponse.from(vehicle), null);
    }

    @Transactional(readOnly = true)
    public List<CustomerDetailResponse> list(RiskLevel riskLevel, String dealership, String query) {
        return vehicles.findAll().stream()
                .map(vehicle -> detail(vehicle, risks.findFirstByVehicleIdOrderByAssessedAtDesc(vehicle.getId()).orElse(null)))
                .filter(detail -> riskLevel == null || detail.risk() != null && detail.risk().level() == riskLevel)
                .filter(detail -> dealership == null || detail.customer().dealership().equalsIgnoreCase(dealership))
                .filter(detail -> matchesQuery(detail, query))
                .sorted(Comparator.comparingInt((CustomerDetailResponse item) ->
                        item.risk() == null ? 0 : item.risk().score()).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerDetailResponse find(UUID customerId) {
        Customer customer = customers.findById(customerId).orElseThrow(() -> new NotFoundException("Customer not found"));
        Vehicle vehicle = vehicles.findAll().stream().filter(v -> v.getCustomer().getId().equals(customer.getId()))
                .findFirst().orElseThrow(() -> new NotFoundException("Vehicle not found"));
        return detail(vehicle, risks.findFirstByVehicleIdOrderByAssessedAtDesc(vehicle.getId()).orElse(null));
    }

    @Transactional
    public RiskResponse assess(UUID customerId, UUID vehicleId, CreateRiskRequest request) {
        Vehicle vehicle = vehicleOf(customerId, vehicleId);
        return RiskResponse.from(risks.save(new RiskAssessment(vehicle, request.score(), request.reasons(),
                request.recommendedAction(), request.modelName(), request.modelVersion())));
    }

    @Transactional(readOnly = true)
    public List<RiskResponse> assessments(UUID customerId, UUID vehicleId) {
        vehicleOf(customerId, vehicleId);
        return risks.findAllByVehicleIdOrderByAssessedAtDesc(vehicleId).stream().map(RiskResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RiskResponse assessment(UUID customerId, UUID vehicleId, UUID assessmentId) {
        vehicleOf(customerId, vehicleId);
        return risks.findById(assessmentId)
                .filter(risk -> risk.getVehicle().getId().equals(vehicleId))
                .map(RiskResponse::from)
                .orElseThrow(() -> new NotFoundException("Risk assessment not found"));
    }

    private Vehicle vehicleOf(UUID customerId, UUID vehicleId) {
        if (!customers.existsById(customerId)) throw new NotFoundException("Customer not found");
        return vehicles.findById(vehicleId)
                .filter(vehicle -> vehicle.getCustomer().getId().equals(customerId))
                .orElseThrow(() -> new NotFoundException("Vehicle not found for this customer"));
    }

    private boolean matchesQuery(CustomerDetailResponse detail, String query) {
        if (query == null || query.isBlank()) return true;
        String normalized = query.trim().toLowerCase();
        return detail.customer().name().toLowerCase().contains(normalized)
                || detail.vehicle().vin().toLowerCase().contains(normalized)
                || detail.vehicle().model().toLowerCase().contains(normalized);
    }

    private CustomerDetailResponse detail(Vehicle vehicle, RiskAssessment risk) {
        return new CustomerDetailResponse(CustomerResponse.from(vehicle.getCustomer()), VehicleResponse.from(vehicle),
                risk == null ? null : RiskResponse.from(risk));
    }
}
