package com.press.pro.Dto;

import com.press.pro.enums.StatutClient;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

public class ClientDto {

    private Long id;
    private String nom;
    private String telephone;
    private String adresse;
    @Enumerated(EnumType.STRING)
    private StatutClient statutClient;
    private LocalDateTime date;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public StatutClient getStatutClient() {
        return statutClient;
    }


    public void setStatutClient(StatutClient statutClient) {
        this.statutClient = statutClient;
    }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
}
