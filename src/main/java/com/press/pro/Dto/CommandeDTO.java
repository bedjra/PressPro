package com.press.pro.Dto;

import com.press.pro.enums.StatutCommande;
import java.time.LocalDateTime;

public class CommandeDTO {

    private Long id;
    private String nom;
    private String telephone;
    private String article;
    private String service;

    private double prix;          // Prix unitaire
    private int qte;              // Quantité
    private boolean express;      // Mode de commande
    private Double prixExpress;   // Supplément express
    private Double remise;        // Remise en %

    private LocalDateTime date;            // Date de commande
    private LocalDateTime dateLivraison;   // Livraison prévue
    private StatutCommande statutCommande; // EN_COURS, PAYEE, etc.

    public CommandeDTO() {}

    public CommandeDTO(String nom, String telephone, String article, String service,
                       double prix, int qte, boolean express, Double prixExpress,
                       Double remise, LocalDateTime date, LocalDateTime dateLivraison,
                       StatutCommande statutCommande) {
        this.nom = nom;
        this.telephone = telephone;
        this.article = article;
        this.service = service;
        this.prix = prix;
        this.qte = qte;
        this.express = express;
        this.prixExpress = prixExpress;
        this.remise = remise;
        this.date = date;
        this.dateLivraison = dateLivraison;
        this.statutCommande = statutCommande;
    }

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getArticle() { return article; }
    public void setArticle(String article) { this.article = article; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public int getQte() { return qte; }
    public void setQte(int qte) { this.qte = qte; }

    public boolean isExpress() { return express; }
    public void setExpress(boolean express) { this.express = express; }

    public Double getPrixExpress() { return prixExpress; }
    public void setPrixExpress(Double prixExpress) { this.prixExpress = prixExpress; }

    public Double getRemise() { return remise; }
    public void setRemise(Double remise) { this.remise = remise; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public LocalDateTime getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(LocalDateTime dateLivraison) { this.dateLivraison = dateLivraison; }

    public StatutCommande getStatutCommande() { return statutCommande; }
    public void setStatutCommande(StatutCommande statutCommande) { this.statutCommande = statutCommande; }
}
