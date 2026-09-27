package br.com.fiap.predit.risk.api;

import br.com.fiap.predit.risk.api.LeadDtos.*;
import br.com.fiap.predit.risk.service.LeadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leads")
public class LeadController {
    private final LeadService service;
    public LeadController(LeadService service) { this.service = service; }

    @GetMapping
    public List<LeadResponse> list() { return service.list(); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<LeadResponse> create(@Valid @RequestBody CreateLeadRequest request) {
        LeadResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/leads/" + created.id())).body(created);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ADVISOR')")
    public LeadResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateLeadStatusRequest request) {
        return service.updateStatus(id, request);
    }
}
