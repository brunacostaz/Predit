package br.com.fiap.predit.risk.api;

import br.com.fiap.predit.risk.api.CustomerDtos.*;
import br.com.fiap.predit.risk.domain.RiskLevel;
import br.com.fiap.predit.risk.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService service;
    public CustomerController(CustomerService service) { this.service = service; }

    @GetMapping
    public List<CustomerDetailResponse> list(@RequestParam(required = false) RiskLevel riskLevel,
                                             @RequestParam(required = false) String dealership,
                                             @RequestParam(required = false) String query) {
        return service.list(riskLevel, dealership, query);
    }

    @GetMapping("/{id}")
    public CustomerDetailResponse find(@PathVariable UUID id) { return service.find(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<CustomerDetailResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerDetailResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/customers/" + created.customer().id())).body(created);
    }

    @PostMapping("/{customerId}/vehicles/{vehicleId}/risk-assessments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<RiskResponse> assess(@PathVariable UUID customerId, @PathVariable UUID vehicleId,
                                                @Valid @RequestBody CreateRiskRequest request) {
        service.find(customerId);
        RiskResponse created = service.assess(vehicleId, request);
        return ResponseEntity.created(URI.create("/api/v1/customers/" + customerId + "/vehicles/" + vehicleId
                + "/risk-assessments/" + created.id())).body(created);
    }
}
