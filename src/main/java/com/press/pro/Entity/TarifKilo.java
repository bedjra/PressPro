package com.press.pro.Entity;


import jakarta.persistence.*;

@Entity
@Table(name = "tarif_kilo")
public class TarifKilo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Exemple : "1Kg-4Kg", "5Kg-9Kg", "10Kg-20Kg", "Supérieur à 20Kg"
    @Column(nullable = false)
    private String tranchePoids;

    // Exemple : "Lavage simple", "Lavage + Séchage", "L+S + Repassage", "Lavage Express"
    @Column(nullable = false)
    private String service;

    @Column(nullable = false)
    private double prix;

    @ManyToOne
    @JoinColumn(name = "pressing_id")
    private Pressing pressing;

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pressing getPressing() {
        return pressing;
    }

    public void setPressing(Pressing pressing) {
        this.pressing = pressing;
    }

    public String getTranchePoids() {
        return tranchePoids;
    }

    public void setTranchePoids(String tranchePoids) {
        this.tranchePoids = tranchePoids;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }
}
