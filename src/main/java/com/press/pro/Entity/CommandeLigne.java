package com.press.pro.Entity;

import jakarta.persistence.*;

@Entity
public class CommandeLigne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne
    @JoinColumn(name = "parametre_id", nullable = false)
    private Parametre parametre;

    @Column(nullable = false)
    private int quantite;

    @Column(nullable = false)
    private double montantBrut;  // ⚠ obligatoire pour MySQL

    @Column(nullable = false)
    private double montantNet;    // ⚠ obligatoire pour MySQL

    // Calcul automatique du montant brut à partir du paramètre
    public void recalcMontantBrut() {
        if (parametre != null && parametre.getPrix() != null) {
            this.montantBrut = parametre.getPrix() * quantite;
        } else {
            this.montantBrut = 0;
        }
    }

    // GETTERS & SETTERS
    public double getMontantBrut() { return montantBrut; }
    public void setMontantBrut(double montantBrut) { this.montantBrut = montantBrut; }

    public double getMontantNet() { return montantNet; }
    public void setMontantNet(double montantNet) { this.montantNet = montantNet; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public Commande getCommande() { return commande; }
    public void setCommande(Commande commande) { this.commande = commande; }

    public Parametre getParametre() { return parametre; }
    public void setParametre(Parametre parametre) { this.parametre = parametre; }
}

