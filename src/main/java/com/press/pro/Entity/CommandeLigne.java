package com.press.pro.Entity;


import jakarta.persistence.*;

@Entity
@Table(name = "commande_ligne")
public class CommandeLigne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    private String article;
    private String service;
    private double prix;
    private int qte;
    private double montantBrut;
    private double remise;
    private double montantNet;

    // GETTERS & SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Commande getCommande() { return commande; }
    public void setCommande(Commande commande) { this.commande = commande; }

    public String getArticle() { return article; }
    public void setArticle(String article) { this.article = article; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public int getQte() { return qte; }
    public void setQte(int qte) { this.qte = qte; }

    public double getMontantBrut() { return montantBrut; }
    public void setMontantBrut(double montantBrut) { this.montantBrut = montantBrut; }

    public double getRemise() { return remise; }
    public void setRemise(double remise) { this.remise = remise; }

    public double getMontantNet() { return montantNet; }
    public void setMontantNet(double montantNet) { this.montantNet = montantNet; }
}
