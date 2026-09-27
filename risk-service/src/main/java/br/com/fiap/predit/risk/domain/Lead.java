package br.com.fiap.predit.risk.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "leads")
public class Lead {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "customer_id") private Customer customer;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "vehicle_id") private Vehicle vehicle;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 400) private String action;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private LeadStatus status;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private RiskLevel priority;
    @Column(name = "due_at") private Instant dueAt;
    @Column(name = "assigned_to", length = 180) private String assignedTo;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected Lead() {}

    public Lead(Customer customer, Vehicle vehicle, String title, String action, RiskLevel priority, Instant dueAt) {
        this.id = UUID.randomUUID(); this.customer = customer; this.vehicle = vehicle; this.title = title;
        this.action = action; this.priority = priority; this.dueAt = dueAt; this.status = LeadStatus.OPEN;
        this.createdAt = Instant.now(); this.updatedAt = this.createdAt;
    }

    public void changeStatus(LeadStatus status, String assignedTo) {
        this.status = status; this.assignedTo = assignedTo; this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Customer getCustomer() { return customer; }
    public Vehicle getVehicle() { return vehicle; }
    public String getTitle() { return title; }
    public String getAction() { return action; }
    public LeadStatus getStatus() { return status; }
    public RiskLevel getPriority() { return priority; }
    public Instant getDueAt() { return dueAt; }
    public String getAssignedTo() { return assignedTo; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
