//package com.press.pro.Entity;
//
//import jakarta.persistence.*;
//
//@Entity
//public class Parametre {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String article;
//    private String service;
//    private Double prix; // FCFA
//
//    public Parametre() {
//    }
//
//    public Parametre(String article, String service, Double prix) {
//        this.article = article;
//        this.service = service;
//        this.prix = prix;
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getArticle() {
//        return article;
//    }
//
//    public void setArticle(String article) {
//        this.article = article;
//    }
//
//    public String getService() {
//        return service;
//    }
//
//    public void setService(String service) {
//        this.service = service;
//    }
//
//    public Double getPrix() {
//        return prix;
//    }
//
//    public void setPrix(Double prix) {
//        this.prix = prix;
//    }
//}
