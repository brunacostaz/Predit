package br.com.fiap.predit.risk.api;

import br.com.fiap.predit.risk.api.LeadDtos.*;
import br.com.fiap.predit.risk.service.LeadService;
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
@RequestMapping("/api/v1/leads")
@Tag(name = "Leads", description = "Fila de acoes proativas de retencao")
@ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado")
public class LeadController {
    private final LeadService service;
    public LeadController(LeadService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Lista leads por prioridade")
    @ApiResponse(responseCode = "200", description = "Lista de leads")
    public List<LeadResponse> list() { return service.list(); }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um lead")
    @ApiResponse(responseCode = "200", description = "Lead encontrado")
    @ApiResponse(responseCode = "404", description = "Lead inexistente")
    public LeadResponse find(@PathVariable UUID id) { return service.find(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Cria lead para cliente com consentimento de contato (ADMIN, MANAGER)")
    @ApiResponse(responseCode = "201", description = "Lead criado; header Location aponta para o recurso")
    @ApiResponse(responseCode = "400", description = "Payload invalido")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissao")
    @ApiResponse(responseCode = "404", description = "Cliente ou veiculo inexistente")
    @ApiResponse(responseCode = "422", description = "Veiculo de outro cliente ou cliente sem consentimento")
    public ResponseEntity<LeadResponse> create(@Valid @RequestBody CreateLeadRequest request) {
        LeadResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/leads/" + created.id())).body(created);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ADVISOR')")
    @Operation(summary = "Atualiza status e responsavel do lead (todos os perfis)")
    @ApiResponse(responseCode = "200", description = "Lead atualizado")
    @ApiResponse(responseCode = "400", description = "Payload invalido")
    @ApiResponse(responseCode = "404", description = "Lead inexistente")
    public LeadResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateLeadStatusRequest request) {
        return service.updateStatus(id, request);
    }
}
