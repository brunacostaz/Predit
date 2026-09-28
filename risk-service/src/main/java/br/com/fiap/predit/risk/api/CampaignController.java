package br.com.fiap.predit.risk.api;

import br.com.fiap.predit.risk.api.CampaignDtos.*;
import br.com.fiap.predit.risk.service.CampaignService;
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
@RequestMapping("/api/v1/campaigns")
@Tag(name = "Campaigns", description = "Campanhas de retencao por segmento")
@ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado")
public class CampaignController {
    private final CampaignService service;

    public CampaignController(CampaignService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Lista campanhas, mais recentes primeiro")
    @ApiResponse(responseCode = "200", description = "Lista de campanhas")
    public List<CampaignResponse> list() { return service.list(); }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma campanha")
    @ApiResponse(responseCode = "200", description = "Campanha encontrada")
    @ApiResponse(responseCode = "404", description = "Campanha inexistente")
    public CampaignResponse find(@PathVariable UUID id) { return service.find(id); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Cria campanha em rascunho (ADMIN, MANAGER)")
    @ApiResponse(responseCode = "201", description = "Campanha criada; header Location aponta para o recurso")
    @ApiResponse(responseCode = "400", description = "Payload invalido")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissao")
    public ResponseEntity<CampaignResponse> create(@Valid @RequestBody CreateCampaignRequest request) {
        CampaignResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/campaigns/" + created.id())).body(created);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Altera o status da campanha (ADMIN, MANAGER)")
    @ApiResponse(responseCode = "200", description = "Status atualizado")
    @ApiResponse(responseCode = "400", description = "Status invalido")
    @ApiResponse(responseCode = "403", description = "Perfil sem permissao")
    @ApiResponse(responseCode = "404", description = "Campanha inexistente")
    public CampaignResponse updateStatus(@PathVariable UUID id,
                                         @Valid @RequestBody UpdateCampaignStatusRequest request) {
        return service.updateStatus(id, request);
    }
}
