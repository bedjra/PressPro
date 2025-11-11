package com.press.pro.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.press.pro.enums.StatutCommande;
import com.press.pro.enums.StatutPaiement;
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

    @Column(nullable = false)
    private boolean express = false;

    @Enumerated(EnumType.STRING)
    private StatutPaiement statutPaiement = StatutPaiement.NON_PAYE;

    @Column(nullable = false)
    private double montantPaye = 0; // montant déjà versé

    @PrePersist
    public void calculerDateLivraison() {
        if (dateReception == null) dateReception = LocalDate.now();
        dateLivraison = express ? dateReception.plusDays(1) : dateReception.plusDays(3);
    }

    public void setMontantPaye(double montantPaye) {
        this.montantPaye = montantPaye;
        updateStatutPaiement();
    }

    private void updateStatutPaiement() {
        if (montantPaye <= 0) {
            this.statutPaiement = StatutPaiement.NON_PAYE;
        } else if (montantPaye < this.montantNet) {
            this.statutPaiement = StatutPaiement.PARTIELLEMENT_PAYE;
        } else {
            this.statutPaiement = StatutPaiement.PAYE;
        }
    }

    @Transient
    public double getResteAPayer() {
        return this.montantNet - this.montantPaye;
    }

    // --- Getters & Setters classiques ---



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Parametre getParametre() { return parametre; }
    public void setParametre(Parametre parametre) { this.parametre = parametre; }

    public Pressing getPressing() { return pressing; }
    public void setPressing(Pressing pressing) { this.pressing = pressing; }

    public int getQte() { return qte; }
    public void setQte(int qte) { this.qte = qte; }

    public double getMontantBrut() { return montantBrut; }
    public void setMontantBrut(double montantBrut) { this.montantBrut = montantBrut; }

    public double getRemise() { return remise; }
    public void setRemise(double remise) { this.remise = remise; }

    public double getMontantNet() { return montantNet; }
    public void setMontantNet(double montantNet) { this.montantNet = montantNet; }

    public StatutCommande getStatut() { return statut; }
    public void setStatut(StatutCommande statut) { this.statut = statut; }

    public LocalDate getDateReception() { return dateReception; }
    public void setDateReception(LocalDate dateReception) { this.dateReception = dateReception; }

    public LocalDate getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(LocalDate dateLivraison) { this.dateLivraison = dateLivraison; }

    public boolean isExpress() { return express; }
    public void setExpress(boolean express) { this.express = express; }

    public StatutPaiement getStatutPaiement() { return statutPaiement; }
    public double getMontantPaye() { return montantPaye; }

    public void setStatutPaiement(StatutPaiement statutPaiement) {
        this.statutPaiement = statutPaiement;
    }
}
