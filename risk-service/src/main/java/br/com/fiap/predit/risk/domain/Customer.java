package br.com.fiap.predit.risk.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {
    @Id private UUID id;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, unique = true, length = 180) private String email;
    @Column(length = 24) private String phone;
    @Column(nullable = false, length = 120) private String dealership;
    @Column(name = "contact_consent", nullable = false) private boolean contactConsent;
    @Column(name = "consent_updated_at") private Instant consentUpdatedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected Customer() {}

    public Customer(String name, String email, String phone, String dealership, boolean contactConsent) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email.toLowerCase();
        this.phone = phone;
        this.dealership = dealership;
        this.contactConsent = contactConsent;
        this.consentUpdatedAt = contactConsent ? Instant.now() : null;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDealership() { return dealership; }
    public boolean isContactConsent() { return contactConsent; }
    public Instant getConsentUpdatedAt() { return consentUpdatedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
