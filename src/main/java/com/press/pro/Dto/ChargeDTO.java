package com.press.pro.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ChargeDTO {

    private Long id;
    private String description;
    private BigDecimal montant;
    private LocalDate dateCharge;

    // Infos simplifiées du pressing
    private Long pressingId;
    private String pressingNom;

    public ChargeDTO() {}

    public ChargeDTO(Long id, String description, BigDecimal montant, LocalDate dateCharge, Long pressingId, String pressingNom) {
        this.id = id;
        this.description = description;
        this.montant = montant;
        this.dateCharge = dateCharge;
        this.pressingId = pressingId;
        this.pressingNom = pressingNom;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }
    public LocalDate getDateCharge() { return dateCharge; }
    public void setDateCharge(LocalDate dateCharge) { this.dateCharge = dateCharge; }
    public Long getPressingId() { return pressingId; }
    public void setPressingId(Long pressingId) { this.pressingId = pressingId; }
    public String getPressingNom() { return pressingNom; }
    public void setPressingNom(String pressingNom) { this.pressingNom = pressingNom; }
}
