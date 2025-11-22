package com.press.pro.Entity;

import jakarta.persistence.*;

@Entity
public class Kilo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double prix; // prix pour 1 kilo

    @ManyToOne
    @JoinColumn(name = "pressing_id")
    private Pressing pressing;

    // Constructeur par défaut requis par JPA
    public Kilo() {
    }

    // Constructeur avec prix
    public Kilo(double prix, Pressing pressing) {
        this.prix = prix;
        this.pressing = pressing;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public Pressing getPressing() {
        return pressing;
    }

    public void setPressing(Pressing pressing) {
        this.pressing = pressing;
    }

    @Override
    public String toString() {
        return "Prix pour 1 kilo: " + prix + " F, Pressing: " + (pressing != null ? pressing.getNom() : "N/A");
    }
}
