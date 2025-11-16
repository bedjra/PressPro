package com.press.pro.Dto;

import java.sql.Blob;

public class PressingRequest {
    private Long id;
    private Blob logo;
    private String nom;
    private String telephone;
    private String adresse;
    private String email; // ⚡ On ne met pas @Column ici, c'est un DTO

    public PressingRequest() {}

    public PressingRequest(Long id, Blob logo, String nom, String telephone, String adresse, String email) {
        this.id = id;
        this.logo = logo;
        this.nom = nom;
        this.telephone = telephone;
        this.adresse = adresse;
        this.email = email;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Blob getLogo() { return logo; }
    public void setLogo(Blob logo) { this.logo = logo; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
