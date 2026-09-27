package br.com.fiap.predit.risk.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false) private Customer customer;
    @Column(nullable = false, unique = true, length = 17) private String vin;
    @Column(nullable = false, length = 80) private String model;
    @Column(name = "model_year", nullable = false) private int modelYear;
    @Column(nullable = false) private int mileage;
    @Column(name = "warranty_end_date") private LocalDate warrantyEndDate;
    @Column(name = "last_service_date") private LocalDate lastServiceDate;

    protected Vehicle() {}

    public Vehicle(Customer customer, String vin, String model, int modelYear, int mileage,
                   LocalDate warrantyEndDate, LocalDate lastServiceDate) {
        this.id = UUID.randomUUID();
        this.customer = customer;
        this.vin = vin.toUpperCase();
        this.model = model;
        this.modelYear = modelYear;
        this.mileage = mileage;
        this.warrantyEndDate = warrantyEndDate;
        this.lastServiceDate = lastServiceDate;
    }

    public UUID getId() { return id; }
    public Customer getCustomer() { return customer; }
    public String getVin() { return vin; }
    public String getModel() { return model; }
    public int getModelYear() { return modelYear; }
    public int getMileage() { return mileage; }
    public LocalDate getWarrantyEndDate() { return warrantyEndDate; }
    public LocalDate getLastServiceDate() { return lastServiceDate; }
}
