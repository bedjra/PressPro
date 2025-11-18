package com.press.pro.Entity;

import jakarta.persistence.*;


@Entity
public class Pressing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    private byte[] logo;
    private String nom;
    private String email;
    private String telephone;
    private String cel;
    private String adresse;

    // ⚡ Ajout de fetch lazy pour éviter les doublons
    @OneToOne(mappedBy = "pressing", fetch = FetchType.LAZY)
    private Utilisateur admin;

    public Pressing() {}

    public Pressing(String nom, byte[]  logo, String email, String telephone,String cel, String adresse) {
        this.nom = nom;
        this.logo = logo;
        this.email = email;
        this.telephone = telephone;
        this.cel =cel;
        this.adresse = adresse;
    }

    // Getters / Setters


    public String getCel() {
        return cel;
    }

    public void setCel(String cel) {
        this.cel = cel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo( byte[]   logo) {
        this.logo = logo;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Utilisateur getAdmin() {
        return admin;
    }

    public void setAdmin(Utilisateur admin) {
        this.admin = admin;
    }
}
