package br.com.fiap.predit.risk.api;

import br.com.fiap.predit.risk.api.CampaignDtos.*;
import br.com.fiap.predit.risk.service.CampaignService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campaigns")
public class CampaignController {
    private final CampaignService service;

    public CampaignController(CampaignService service) { this.service = service; }

    @GetMapping
    public List<CampaignResponse> list() { return service.list(); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<CampaignResponse> create(@Valid @RequestBody CreateCampaignRequest request) {
        CampaignResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/campaigns/" + created.id())).body(created);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public CampaignResponse updateStatus(@PathVariable UUID id,
                                         @Valid @RequestBody UpdateCampaignStatusRequest request) {
        return service.updateStatus(id, request);
    }
}
