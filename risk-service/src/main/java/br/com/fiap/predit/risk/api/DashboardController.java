package br.com.fiap.predit.risk.api;

import br.com.fiap.predit.risk.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Indicadores gerenciais")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service) { this.service = service; }

    @GetMapping("/summary")
    @Operation(summary = "Resumo com VIN Share, risco medio, receita em risco e distribuicoes")
    @ApiResponse(responseCode = "200", description = "Indicadores calculados")
    @ApiResponse(responseCode = "401", description = "Token ausente, invalido ou expirado")
    public DashboardResponse summary() { return service.summary(); }
}
