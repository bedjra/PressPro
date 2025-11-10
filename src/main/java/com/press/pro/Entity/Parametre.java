package com.press.pro.Entity;

import jakarta.persistence.*;

@Entity
public class Parametre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String article;
    private String service;
    private Double prix;

    @ManyToOne
    @JoinColumn(name = "pressing_id")
    private Pressing pressing;


    public Parametre() {
    }

    // 🔹 Constructeur sans id (utile pour créer un nouvel objet avant persistance)
    public Parametre(String article, String service, Double prix, Pressing pressing) {
        this.article = article;
        this.service = service;
        this.prix = prix;
        this.pressing = pressing;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public Pressing getPressing() {
        return pressing;
    }

    public void setPressing(Pressing pressing) {
        this.pressing = pressing;
    }


}
