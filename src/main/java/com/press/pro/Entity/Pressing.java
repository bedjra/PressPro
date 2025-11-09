package com.press.pro.Entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Pressing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String logo; // ⚡ URL du logo
    private String nom;
    private String email; // Email de l’admin
    private String telephone;
    private String adresse;

    @OneToOne(mappedBy = "pressing")
    private Utilisateur admin;

    // Constructeurs
    public Pressing() {}

    public Pressing(String nom, String logo, String email, String telephone, String adresse) {
        this.nom = nom;
        this.logo = logo;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public Utilisateur getAdmin() { return admin; }
    public void setAdmin(Utilisateur admin) { this.admin = admin; }
}
