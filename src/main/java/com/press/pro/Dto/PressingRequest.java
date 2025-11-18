package com.press.pro.Dto;


import jakarta.persistence.Lob;

public class PressingRequest {
    private Long id;
    @Lob
    private byte[] logo;
    private String nom;
    private String telephone;
    private String adresse;
    private String email; // ⚡ On ne met pas @Column ici, c'est un DTO
    private String cel;

    public PressingRequest() {}

    public PressingRequest(Long id, byte[]  logo, String nom, String telephone,  String cel, String adresse, String email) {
        this.id = id;
        this.logo = logo;
        this.nom = nom;
        this.telephone = telephone;
        this.cel =cel;
        this.adresse = adresse;
        this.email = email;
    }

    // Getters & Setters

    public String getCel() {
        return cel;
    }

    public void setCel(String cel) {
        this.cel = cel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public byte[] getLogo() { return logo; }
    public void setLogo(byte[] logo) { this.logo = logo; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
