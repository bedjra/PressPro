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
    @JoinColumn(
            name = "pressing_id"
    )
    private Pressing pressing;

    // -----------------------------
    // 🔹 Mode ARTICLE
    // -----------------------------


    @ManyToOne
    @JoinColumn(name = "parametre_id", nullable = true)
    private Parametre parametre;

    @PrePersist
    @PreUpdate
    private void checkMode() {
        if (parametre == null && tarifKilo == null) {
            throw new RuntimeException("Une ligne doit avoir soit un paramètre, soit un tarif kilo.");
        }
    }


    private Integer quantite;

    // -----------------------------
    // 🔹 Mode KILO
    // -----------------------------
    @ManyToOne
    @JoinColumn(name = "tarif_kilo_id")
    private TarifKilo tarifKilo;

    private Double poids;

    // -----------------------------
    // Montants
    // -----------------------------
    @Column(nullable = false)
    private double montantBrut;

    @Column(nullable = false)
    private double montantNet;

    // -----------------------------
    // Calcul automatique pour articles
    // -----------------------------
    public void recalcMontantBrut() {
        if (parametre != null && parametre.getPrix() != null && quantite != null) {
            this.montantBrut = parametre.getPrix() * quantite;
        } else {
            this.montantBrut = 0;
        }
    }

    // -----------------------------
    // GETTERS & SETTERS
    // -----------------------------
    public Long getId() { return id; }

    public Commande getCommande() { return commande; }
    public void setCommande(Commande commande) { this.commande = commande; }

    public Parametre getParametre() { return parametre; }
    public void setParametre(Parametre parametre) { this.parametre = parametre; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public TarifKilo getTarifKilo() { return tarifKilo; }
    public void setTarifKilo(TarifKilo tarifKilo) { this.tarifKilo = tarifKilo; }

    public Double getPoids() { return poids; }
    public void setPoids(Double poids) { this.poids = poids; }

    public double getMontantBrut() { return montantBrut; }
    public void setMontantBrut(double montantBrut) { this.montantBrut = montantBrut; }

    public double getMontantNet() { return montantNet; }
    public void setMontantNet(double montantNet) { this.montantNet = montantNet; }

    public void setId(Long id) {
        this.id = id;
    }

    public Pressing getPressing() {
        return pressing;
    }

    public void setPressing(Pressing pressing) {
        this.pressing = pressing;
    }
}
