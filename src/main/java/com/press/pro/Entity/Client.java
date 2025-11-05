package com.press.pro.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String telephone;
    private String adresse;

    private LocalDateTime date; // sera générée automatiquement

    @ManyToOne
    @JoinColumn(name = "pressing_id")
    private Pressing pressing;

    @PrePersist
    protected void onCreate() {
        this.date = LocalDateTime.now();
    }

    // Constructeurs
    public Client() {}

    public Client(String nom, String telephone, String adresse, Pressing pressing) {
        this.nom = nom;
        this.telephone = telephone;
        this.adresse = adresse;
        this.pressing = pressing;
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public Pressing getPressing() { return pressing; }
    public void setPressing(Pressing pressing) { this.pressing = pressing; }
}
