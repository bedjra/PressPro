package com.press.pro.Dto;


public class CommandeDTO {
    private String nom;
    private String telephone;
    private String article;
    private String service;
    private int qte;

    // --- Constructeurs ---
    public CommandeDTO() {}

    public CommandeDTO(String nom, String telephone, String article, String service, int qte) {
        this.nom = nom;
        this.telephone = telephone;
        this.article = article;
        this.service = service;
        this.qte = qte;
    }

    // --- Getters & Setters ---
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
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

    public int getQte() {
        return qte;
    }

    public void setQte(int qte) {
        this.qte = qte;
    }
}
