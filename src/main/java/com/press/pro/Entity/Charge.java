package com.press.pro.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "")
public class Charge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(name = "montant", nullable = false)
    private BigDecimal montant;

    private LocalDate dateCharge; // <-- PAS "date" !

    @PrePersist
    public void prePersist() {
        if (dateCharge == null) {
            dateCharge = LocalDate.now();
        }
    }
    @ManyToOne
    @JoinColumn(name = "pressing_id", nullable = false)
    private Pressing pressing;

    // Getters & Setters


    public LocalDate getDateCharge() {
        return dateCharge;
    }

    public void setDateCharge(LocalDate dateCharge) {
        this.dateCharge = dateCharge;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public Pressing getPressing() {
        return pressing;
    }

    public void setPressing(Pressing pressing) {
        this.pressing = pressing;
    }
}

