package com.press.pro.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.press.pro.enums.StatutCommande;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "commande")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "parametre_id", nullable = false)
    private Parametre parametre;

    @ManyToOne
    @JoinColumn(name = "pressing_id", nullable = false)
    private Pressing pressing;

    private int qte;

    private double montantBrut;
    private double remise;
    private double montantNet;

    @Enumerated(EnumType.STRING)
    private StatutCommande statut;


    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateReception;


    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateLivraison;

    // 🧠 Nouveau champ
    @Column(nullable = false)
    private boolean express = false; // valeur par défaut

    @PrePersist
    public void calculerDateLivraison() {
        if (dateReception == null) {
            dateReception = LocalDate.now();
        }
        if (express) {
            dateLivraison = dateReception.plusDays(1);
        } else {
            dateLivraison = dateReception.plusDays(3);
        }
    }

    // Getters et Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Parametre getParametre() {
        return parametre;
    }

    public void setParametre(Parametre parametre) {
        this.parametre = parametre;
    }

    public Pressing getPressing() {
        return pressing;
    }

    public void setPressing(Pressing pressing) {
        this.pressing = pressing;
    }

    public int getQte() {
        return qte;
    }

    public void setQte(int qte) {
        this.qte = qte;
    }

    public double getMontantBrut() {
        return montantBrut;
    }

    public void setMontantBrut(double montantBrut) {
        this.montantBrut = montantBrut;
    }

    public double getRemise() {
        return remise;
    }

    public void setRemise(double remise) {
        this.remise = remise;
    }

    public double getMontantNet() {
        return montantNet;
    }

    public void setMontantNet(double montantNet) {
        this.montantNet = montantNet;
    }

    public StatutCommande getStatut() {
        return statut;
    }

    public void setStatut(StatutCommande statut) {
        this.statut = statut;
    }

    public LocalDate getDateReception() {
        return dateReception;
    }

    public void setDateReception(LocalDate dateReception) {
        this.dateReception = dateReception;
    }

    public LocalDate getDateLivraison() {
        return dateLivraison;
    }

    public void setDateLivraison(LocalDate dateLivraison) {
        this.dateLivraison = dateLivraison;
    }

    public boolean isExpress() {
        return express;
    }

    public void setExpress(boolean express) {
        this.express = express;
    }
}
