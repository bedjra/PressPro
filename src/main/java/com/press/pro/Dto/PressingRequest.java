package com.press.pro.Dto;


import jakarta.persistence.Lob;

import java.time.LocalDateTime;

public class PressingRequest {
    private Long id;
    @Lob
    private byte[] logo;
    private String nom;
    private String telephone;
    private String adresse;
    private String email;
    private String cel;
    private boolean actif;
    private LocalDateTime dateCreation;

    public PressingRequest() {
    }

    public PressingRequest(Long id, byte[] logo, String nom, String telephone, String cel, String adresse, String email) {
        this.id = id;
        this.logo = logo;
        this.nom = nom;
        this.telephone = telephone;
        this.cel = cel;
        this.adresse = adresse;
        this.email = email;
    }

    public boolean isActif() {
        return this.actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public LocalDateTime getDateCreation() {
        return this.dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getCel() {
        return this.cel;
    }

    public void setCel(String cel) {
        this.cel = cel;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getLogo() {
        return this.logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    public String getNom() {
        return this.nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return this.adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
