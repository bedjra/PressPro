package com.press.pro.Entity;

import com.press.pro.enums.StatutCommande;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "commande")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Relations
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "parametre_id", nullable = false)
    private Parametre parametre;

    // 🔹 Champs principaux
    private int qte;

    // 🔹 Dates
    private LocalDateTime date;
    private LocalDateTime dateLivraison;
    private LocalDateTime dateLivraisonExpress;

    // 🔹 Mode express
    private boolean express = false;
    private Double prixExpress; // Supplément
    private Double remise;      // En %

    // 🔹 Statut
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCommande statutCommande = StatutCommande.EN_COURS;

    @ManyToOne
    @JoinColumn(name = "pressing_id")
    private Pressing pressing;

    // --- Logique automatique à la création ---
    @PrePersist
    protected void onCreate() {
        this.date = LocalDateTime.now();

        // ⏰ Livraison selon type
        if (this.express) {
            this.dateLivraisonExpress = this.date.plusHours(24);
            this.dateLivraison = this.dateLivraisonExpress;
        } else {
            this.dateLivraison = this.date.plusHours(72);
        }

        // Valeurs par défaut
        if (this.statutCommande == null) {
            this.statutCommande = StatutCommande.EN_COURS;
        }
        if (this.remise == null) {
            this.remise = 0.0;
        }
    }

    // --- Getters & Setters ---


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

    public int getQte() {
        return qte;
    }

    public void setQte(int qte) {
        this.qte = qte;
    }



    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public LocalDateTime getDateLivraison() {
        return dateLivraison;
    }

    public void setDateLivraison(LocalDateTime dateLivraison) {
        this.dateLivraison = dateLivraison;
    }

    public LocalDateTime getDateLivraisonExpress() {
        return dateLivraisonExpress;
    }

    public void setDateLivraisonExpress(LocalDateTime dateLivraisonExpress) {
        this.dateLivraisonExpress = dateLivraisonExpress;
    }

    public boolean isExpress() {
        return express;
    }

    public void setExpress(boolean express) {
        this.express = express;
    }

    public Double getPrixExpress() {
        return prixExpress;
    }

    public void setPrixExpress(Double prixExpress) {
        this.prixExpress = prixExpress;
    }

    public Double getRemise() {
        return remise;
    }

    public void setRemise(Double remise) {
        this.remise = remise;
    }

    public StatutCommande getStatutCommande() {
        return statutCommande;
    }

    public void setStatutCommande(StatutCommande statutCommande) {
        this.statutCommande = statutCommande;
    }

    public Pressing getPressing() {
        return pressing;
    }

    public void setPressing(Pressing pressing) {
        this.pressing = pressing;
    }
}
