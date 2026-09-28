package br.com.fiap.predit.risk.service;

import br.com.fiap.predit.risk.api.LeadDtos.*;
import br.com.fiap.predit.risk.domain.*;
import br.com.fiap.predit.risk.error.BusinessRuleException;
import br.com.fiap.predit.risk.error.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LeadService {
    private static final Logger log = LoggerFactory.getLogger(LeadService.class);
    private final LeadRepository leads;
    private final CustomerRepository customers;
    private final VehicleRepository vehicles;

    public LeadService(LeadRepository leads, CustomerRepository customers, VehicleRepository vehicles) {
        this.leads = leads; this.customers = customers; this.vehicles = vehicles;
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> list() {
        return leads.findAllByOrderByPriorityDescCreatedAtAsc().stream().map(LeadResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public LeadResponse find(UUID id) {
        return LeadResponse.from(leads.findById(id).orElseThrow(() -> new NotFoundException("Lead not found")));
    }

    @Transactional
    public LeadResponse create(CreateLeadRequest request) {
        Customer customer = customers.findById(request.customerId()).orElseThrow(() -> new NotFoundException("Customer not found"));
        Vehicle vehicle = vehicles.findById(request.vehicleId()).orElseThrow(() -> new NotFoundException("Vehicle not found"));
        if (!vehicle.getCustomer().getId().equals(customer.getId())) throw new BusinessRuleException("Vehicle does not belong to customer");
        if (!customer.isContactConsent()) throw new BusinessRuleException("Customer has not consented to contact");
        Lead lead = leads.save(new Lead(customer, vehicle, request.title(), request.action(), request.priority(), request.dueAt()));
        log.info("event=lead_created leadId={} actor={}", lead.getId(), actor());
        return LeadResponse.from(lead);
    }

    @Transactional
    public LeadResponse updateStatus(UUID id, UpdateLeadStatusRequest request) {
        Lead lead = leads.findById(id).orElseThrow(() -> new NotFoundException("Lead not found"));
        lead.changeStatus(request.status(), request.assignedTo());
        log.info("event=lead_status_changed leadId={} status={} actor={}", id, request.status(), actor());
        return LeadResponse.from(lead);
    }

    private String actor() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
