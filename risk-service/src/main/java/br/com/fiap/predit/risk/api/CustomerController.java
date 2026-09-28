package br.com.fiap.predit.risk.api;

import br.com.fiap.predit.risk.api.CustomerDtos.*;
import br.com.fiap.predit.risk.domain.RiskLevel;
import br.com.fiap.predit.risk.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Clientes, veiculos e avaliacoes de risco")
@ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado")
public class CustomerController {
    private final CustomerService service;
    public CustomerController(CustomerService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Lista clientes com filtros por risco, concessionaria e busca textual")
    @ApiResponse(responseCode = "200", description = "Clientes ordenados pelo score mais alto")
    @ApiResponse(responseCode = "400", description = "Filtro invalido")
    public List<CustomerDetailResponse> list(@RequestParam(required = false) RiskLevel riskLevel,
                                             @RequestParam(required = false) String dealership,
                                             @RequestParam(required = false) String query) {
        return service.list(riskLevel, dealership, query);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta cliente, veiculo e avaliacao mais recente")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado")
    @ApiResponse(responseCode = "400", description = "Identificador invalido")
    @ApiResponse(responseCode = "404", description = "Cliente inexistente")
    public CustomerDetailResponse find(@PathVariable UUID id) { return service.find(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Cadastra cliente e veiculo (ADMIN, MANAGER)")
    @ApiResponse(responseCode = "201", description = "Cliente criado; header Location aponta para o recurso")
    @ApiResponse(responseCode = "400", description = "Payload invalido")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissao")
    @ApiResponse(responseCode = "409", description = "E-mail ou VIN ja cadastrado")
    public ResponseEntity<CustomerDetailResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerDetailResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/customers/" + created.customer().id())).body(created);
    }

    @GetMapping("/{customerId}/vehicles/{vehicleId}/risk-assessments")
    @Operation(summary = "Historico de avaliacoes do veiculo, mais recentes primeiro")
    @ApiResponse(responseCode = "200", description = "Historico de avaliacoes")
    @ApiResponse(responseCode = "404", description = "Cliente inexistente ou veiculo nao pertence ao cliente")
    public List<RiskResponse> assessments(@PathVariable UUID customerId, @PathVariable UUID vehicleId) {
        return service.assessments(customerId, vehicleId);
    }

    @GetMapping("/{customerId}/vehicles/{vehicleId}/risk-assessments/{assessmentId}")
    @Operation(summary = "Consulta uma avaliacao de risco")
    @ApiResponse(responseCode = "200", description = "Avaliacao encontrada")
    @ApiResponse(responseCode = "404", description = "Cliente, veiculo ou avaliacao inexistente")
    public RiskResponse assessment(@PathVariable UUID customerId, @PathVariable UUID vehicleId,
                                   @PathVariable UUID assessmentId) {
        return service.assessment(customerId, vehicleId, assessmentId);
    }

    @PostMapping("/{customerId}/vehicles/{vehicleId}/risk-assessments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Registra inferencia do modelo preditivo (ADMIN, MANAGER)")
    @ApiResponse(responseCode = "201", description = "Avaliacao registrada; header Location aponta para o recurso")
    @ApiResponse(responseCode = "400", description = "Payload invalido")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissao")
    @ApiResponse(responseCode = "404", description = "Cliente inexistente ou veiculo nao pertence ao cliente")
    public ResponseEntity<RiskResponse> assess(@PathVariable UUID customerId, @PathVariable UUID vehicleId,
                                                @Valid @RequestBody CreateRiskRequest request) {
        RiskResponse created = service.assess(customerId, vehicleId, request);
        return ResponseEntity.created(URI.create("/api/v1/customers/" + customerId + "/vehicles/" + vehicleId
                + "/risk-assessments/" + created.id())).body(created);
    }
}
