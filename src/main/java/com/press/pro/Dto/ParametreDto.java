package com.press.pro.Dto;

public class ParametreDto {

    private Long id;
    private String article;
    private String service;
    private Double prix;

    // 🔹 Constructeur vide
    public ParametreDto() {
    }

    // 🔹 Constructeur sans id (utile pour la création)
    public ParametreDto(String article, String service, Double prix) {
        this.article = article;
        this.service = service;
        this.prix = prix;
    }

    // 🔹 Constructeur complet
    public ParametreDto(Long id, String article, String service, Double prix) {
        this.id = id;
        this.article = article;
        this.service = service;
        this.prix = prix;
    }

    // 🔹 Getters et setters
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
}
